/* A real-time operating system event in the TM domain.

 Copyright (c) 2001-2012 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.domains.tm.kernel;

import ptolemy.actor.Actor;
import ptolemy.data.Token;

///////////////////////////////////////////////////////////////////
//// TMEvent

/**
 A TM event is an event that triggers the execution of a TM actor
 (task). It has the following information that assists the dispatching
 and processing of the event.
 <ul>
 <li> the destination receiver,
 <li> the destination actor,
 <li> a token,
 <li> the priority, inherited from the destination port or the destination
 actor. A smaller value represents a higher priority.
 <li> a flag <i>hasStarted</i> indicating whether the processing
 of this event has been started but has not yet
 finished (typically due to preemption),
 <li> a <i>processingTime</i> recording the remaining processing time
 needed to finish the processing of this event.
 Note that for an event
 that has been preempted, the <i>processingTime</i> is smalled than
 the execution time of the destination actor.
 </ul>
 <p>
 A event queue is used to sort these event, based on
 <pre>
 - priority, and
 - whether it has been started
 </pre>
 in that order.
 <p>
 Notice that an interrupt event (an event generated by calling fireAt() of
 the director) is not a TM event.
 They are external events that carries time stamps, implemented
 using the DEEvent class.
 @author  Jie Liu
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Yellow (liuj)
 @Pt.AcceptedRating Yellow (janneck)
 @see ptolemy.domains.de.kernel.DEEvent
 */
public class TMEvent implements Comparable {
    /** Construct an event with the specified destination receiver,
     *  token, priority, and executionTime. Upon creation,
     *  the processing of an event has not started.
     *  The destination actor is the container's container of the
     *  destination receiver.
     *  @param receiver The destination receiver.
     *  @param token The transferred token.
     *  @param priority The priority of the port that contains the
     *         destination receiver.
     *  @param processingTime The time needed to finish processing the event.
     *  @exception NullPointerException If the receiver is null or is
     *   not contained by a port contained by an actor.
     */
    public TMEvent(TMReceiver receiver, Token token, int priority,
            double processingTime) {
        _receiver = receiver;

        if (receiver != null) {
            _actor = (Actor) receiver.getContainer().getContainer();
        }

        _token = token;
        _priority = priority;
        _hasStarted = false;
        _processingTime = processingTime;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the destination actor for this event.
     *  @return The destination actor.
     */
    public final Actor actor() {
        return _actor;
    }

    /** Compare the order of this event with the specified event
     *  for order.
     *  See compareTo(TMEvent event) for the comparison rules.
     *  The argument has to be an instance of TMEvent or a
     *  ClassCastException will be thrown.
     *
     * @param event The event to compare against.
     * @return -1, 0, or 1, depends on the order of the events.
     * @exception ClassCastException If the argument is not an instance
     *  of TMEvent.
     */
    public final int compareTo(Object event) {
        return compareTo((TMEvent) event);
    }

    /** Compare the tag of this event with the specified event for order.
     *  Return -1, zero, or +1 if this
     *  event is less than, equal to, or greater than the specified event.
     *  The priority is checked first. Return 1 if the priority of this
     *  event is strictly higher than that of the argument, (i.e.
     *  the priority is smaller in value).
     *  Return -1 if the priority of this event is strictly lower than
     *  the argument. If the two priorities are
     *  identical, then the hasStarted field is checked.
     *  return 1 if hasStarted of this event is true.
     *  Return -1 if this event has not started, but the argument
     *  has. Return 0 otherwise, i.e. they have the same priority
     *  and none of them has started. Notice that it is impossible
     *  that two events with the same priority have both started.
     *
     *  @param event The event to compare against.
     *  @return -1, 0, or 1, depends on the order of the events.
     */
    public final int compareTo(TMEvent event) {
        if (_priority > event._priority) {
            return 1;
        } else if (_priority < event._priority) {
            return -1;
        } else if (_hasStarted) {
            return -1;
        } else if (!_hasStarted && event._hasStarted) {
            return 1;
        } else {
            return 0;
        }
    }

    /** Return true if this TMEvent has the same sequence
     *  number as the given TMEvent.
     *  @param tmEvent The TMEvent object that this
     *  TMEvent object is compared to.
     *  @return True if the two TMEvent objects have the same
     *  sequence number, name and workspace
     */
    public boolean equals(Object tmEvent) {
        // See http://www.technofundo.com/tech/java/equalhash.html

        /* FindBugs says that TMEvent "defined
         * compareTo(Object) and uses Object.equals()"
         * http://findbugs.sourceforge.net/bugDescriptions.html#EQ_COMPARETO_USE_OBJECT_EQUALS
         * says: "This class defines a compareTo(...) method but
         * inherits its equals() method from
         * java.lang.Object. Generally, the value of compareTo should
         * return zero if and only if equals returns true. If this is
         * violated, weird and unpredictable failures will occur in
         * classes such as PriorityQueue. In Java 5 the
         * PriorityQueue.remove method uses the compareTo method,
         * while in Java 6 it uses the equals method.
         *
         *  From the JavaDoc for the compareTo method in the
         *  Comparable interface:
         *
         * It is strongly recommended, but not strictly required that
         * (x.compareTo(y)==0) == (x.equals(y)). Generally speaking,
         * any class that implements the Comparable interface and
         * violates this condition should clearly indicate this
         * fact. The recommended language is "Note: this class has a
         * natural ordering that is inconsistent with equals." "
         */
        if (tmEvent == this) {
            return true;
        }
        if ((tmEvent == null)
                || (tmEvent.getClass() != getClass())) {
            return false;
        } else {
            TMEvent event = (TMEvent) tmEvent;
            if (compareTo(event) == 0
                    && (receiver() != null && receiver().equals(event.receiver()))
                    && (token() != null && token().equals(event.token()))
                    && priority() == event.priority()
                    && processingTime() == event.processingTime()) {
                return true;
            }
        }
        return false;
    }

    /** Return the hash code for this TMEvent object.  If two TMEvent
     *  objects contain the receiver, token, name and workspace, then
     *  they will have the same hashCode.
     *  @return The hash code for this TimedEvent object.
     */
    public int hashCode() {
        // See http://www.technofundo.com/tech/java/equalhash.html
        int hashCode = 27;
        if (_receiver != null) {
            hashCode = 31 * hashCode + _receiver.hashCode();
        }
        if (_token != null) {
            hashCode = 31 * hashCode + _token.hashCode();
        }
        hashCode = 31 * hashCode + _priority;
        hashCode = 31 * hashCode + Double.valueOf(_processingTime).hashCode();
        return hashCode;
    }

    /** Return true if the processing of this event has started.
     *  @return True if the processing of this event has started.
     */
    public final boolean hasStarted() {
        return _hasStarted;
    }

    /** Compare the tag of this event with the specified and return true
     *  if they are equal and false otherwise.  This is provided along
     *  with compareTo() because it is slightly faster when all you need
     *  to know is whether the events are equivalent in terms of
     *  dispatching order.
     *  @param event The event to compare against.
     */

    // Commented out because it is not used anywhere
    //public final boolean isEquallyPriorTo(TMEvent event) {
    //    return (_priority == event._priority) &&
    //        (!_hasStarted) && (!event._hasStarted);
    //}
    /** Return the priority.
     *  @return The priority.
     */
    public final int priority() {
        return _priority;
    }

    /** Return the remaining time needed to finish processing this event.
     *  @return The time needed to finish processing this event.
     */
    public final double processingTime() {
        // FIXME: Don't use double for time, use actor.util.Time;
        return _processingTime;
    }

    /** Return the destination receiver of this event.
     *  @return The destination receiver.
     */
    public final TMReceiver receiver() {
        return _receiver;
    }

    /** Set the priority of the event. This method may be used for
     *  dynamic priority assignment scheduling strategies.
     *  @param newPriority The priority set to the event.
     */
    public final void setPriority(int newPriority) {
        _priority = newPriority;
    }

    /** Set the remaining processing time of the event. This method is
     *  typically used by TMDirector to keep tack of the remaining
     *  processing time when time is advance. Notice that
     *  this method does not insist that the time to be set is
     *  smaller than the original processing time. The caller
     *  should perform the comparison if that is the desired
     *  behavior.
     *  @param time The remaining processing time.
     */
    public final void setProcessingTime(double time) {
        _processingTime = time;
    }

    /** Start the processing of this event.
     */
    public final void startProcessing() {
        _hasStarted = true;
    }

    /** Reduce the remaining processing time of this event by a certain
     *  amount. This is a syntactic sugar for setProcessingTime().
     *  @param time The amount of time progressed.
     */
    public final void timeProgress(double time) {
        _processingTime = _processingTime - time;
    }

    /** Return the token contained by this event.
     *  @return The token in this event.
     */
    public final Token token() {
        return _token;
    }

    /** Return a description of the event, including the contained token
     *  (or "null" if there is none), the priority, the destination actor,
     *  whether it has been started, and the remaining processing time.
     *  @return The token as a string with necessary information.
     */
    public final String toString() {
        return "TMEvent(token = " + _token + ", priority = " + _priority
                + ", destination = " + _actor + ", hasStarted = " + _hasStarted
                + ", processingTime = " + _processingTime + ")";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The destination receiver.
    private TMReceiver _receiver;

    // The destination actor.
    private Actor _actor;

    // The token contained by this event.
    private Token _token;

    // The priority.
    private int _priority;

    // Indicate whether the processing of the event has started.
    private boolean _hasStarted;

    // The remaining time needed to finish processing the event.
    private double _processingTime;
}
