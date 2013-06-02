/* This quantity manager actor implements an AFDX switch.

@Copyright (c) 2011-2012 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package ptolemy.actor.lib.qm;

import java.util.HashMap;
import java.util.LinkedList;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.IntermediateReceiver;
import ptolemy.actor.QuantityManager;
import ptolemy.actor.Receiver;
import ptolemy.actor.QuantityManagerListener.EventType;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/** A {@link QuantityManager} actor that, when its
 *  {@link #sendToken(Receiver, Receiver, Token)} method is called, delays
 *  the delivery of the specified token to the specified receiver
 *  according to a service rule. This quantity manager is used on
 *  input ports by setting a parameter with an ObjectToken that refers
 *  to this QuantityManager at the port. Note that the name of this
 *  parameter is irrelevant.
 *
 *  <p>This quantity manager implements an AFDX switch. It has a parameter
 *  specifying the number of ports. On each port, an actor is connected.
 *  Note that these ports are not represented as ptolemy actor ports.
 *  This actor can send tokens to the switch and receive tokens from the
 *  switch. The mapping of ports to actors is done via parameters of this
 *  quantity manager.
 *
 *  <p>Internally, this switch has a buffer for every input, a buffer
 *  for the switch fabric and a buffer for every output. The delays
 *  introduced by the buffers are configured via parameters. Tokens are
 *  processed simultaneously on the buffers.
 *
 *  <p> This switch implements the specific switch for the AFDX network.
 *  This implementation is based on the basic switch implementation.
 *
 *  @author Gilles Lasnier, Based on BasiSwitch.java by Patricia Derler
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating 
 *  @Pt.AcceptedRating
 */
public class AFDXSwitch extends BasicSwitch {

    /** Construct a Bus with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this actor.f
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public AFDXSwitch(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _inputTokens = new HashMap<Integer, LinkedList<TimedEvent>>();
        _outputTokens = new HashMap<Integer, LinkedList<TimedEvent>>();
        _switchFabricQueue = new LinkedList<TimedEvent>();
        _actorPorts = new HashMap<Actor, Integer>();
        _ioPortToSwitchInPort = new HashMap<Port, Integer>();
        _ioPortToSwitchOutPort = new HashMap<Port, Integer>();
        _tokenCount = 0;

        bitRate = new Parameter(this, "bitRate");
        bitRate.setDisplayName("bitRate (Mbit/s)");
        bitRate.setExpression("100");
        bitRate.setTypeEquals(BaseType.DOUBLE);
        _bitRate = 100;

        technologicalDelay = new Parameter(this, "technologicalDelay");
        technologicalDelay.setDisplayName("technologicalDelay (us)");
        technologicalDelay.setExpression("140");
        technologicalDelay.setTypeEquals(BaseType.DOUBLE);
        _technologicalDelay = 140 / 1000000;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create an intermediate receiver that wraps a given receiver.
     *  For now, we only support wrapping input ports.
     *  @param receiver The receiver that is being wrapped.
     *  @return A new intermediate receiver.
     *  @exception IllegalActionException If the receiver is an
     *  ouptut port.
     */
    public IntermediateReceiver getReceiver(Receiver receiver)
            throws IllegalActionException {
        if (receiver.getContainer().isOutput()) {
            throw new IllegalActionException(receiver.getContainer(),
                    "This quantity manager cannot be " + "used on port "
                            + receiver.getContainer()
                            + ", it only be specified on input port.");
        }
        IntermediateReceiver intermediateReceiver = new IntermediateReceiver(
                this, receiver);
        return intermediateReceiver;
    }

    /** Create a receiver to mediate a communication via the specified receiver. This
     *  receiver is linked to a specific port of the quantity manager.
     *  @param receiver Receiver whose communication is to be mediated.
     *  @param port Port of the quantity manager.
     *  @return A new receiver.
     *  @exception IllegalActionException If the receiver cannot be created.
     */
    public Receiver getReceiver(Receiver receiver, IOPort port)
            throws IllegalActionException {
        return getReceiver(receiver);
    }

    /** Make sure that this quantity manager is only used in the DE domain.
     *  @param container The container of this actor.
     *  @exception IllegalActionException If thrown by the super class or if the
     *  director of this actor is not a DEDirector.
     *  @exception NameDuplicationException If thrown by the super class.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        super.setContainer(container);
        if (getDirector() != null && !(getDirector() instanceof DEDirector)) {
            throw new IllegalActionException(this,
                    "This quantity manager is currently only supported in the DE domain.");
        }
    }

    /** If the attribute for the input, output, technological delay or bit rate is
     *  changed, then ensure that the value is non-negative.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the buffer delays are negative.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == inputBufferDelay) {
            double value = ((DoubleToken) inputBufferDelay.getToken())
                    .doubleValue();
            if (value < 0.0) {
                throw new IllegalActionException(this,
                        "Cannot have negative serviceTime: " + value);
            }
            _inputBufferDelay = value;
        } else if (attribute == outputBufferDelay) {
            double value = ((DoubleToken) outputBufferDelay.getToken())
                    .doubleValue();
            if (value < 0.0) {
                throw new IllegalActionException(this,
                        "Cannot have negative serviceTime: " + value);
            }
            _outputBufferDelay = value;
        } else if (attribute == technologicalDelay) {
            double value = ((DoubleToken) technologicalDelay.getToken())
                    .doubleValue();
            if (value < 0.0) {
                throw new IllegalActionException(this,
                        "Cannot have negative serviceTime: " + value);
            }
            _technologicalDelay = value / 1000000;
        } else if (attribute == numberOfPorts) {
            int ports = ((IntToken) numberOfPorts.getToken()).intValue();
            _numberOfPorts = ports;
            for (int i = 0; i < ports; i++) {
                _inputTokens.put(i, new LinkedList<TimedEvent>());
                _outputTokens.put(i, new LinkedList<TimedEvent>());
            }
        } else if (attribute == bitRate) {
            double value = ((DoubleToken) bitRate.getToken())
                    .doubleValue();
            if (value < 0.0) {
                throw new IllegalActionException(this,
                        "Cannot have negative bitRate: " + value);
            }
            _bitRate = value;
        }
        super.attributeChanged(attribute);
    }

    /** Clone this actor into the specified workspace. The new actor is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new actor with the same ports as the original, but
     *  no connections and no container.  A container must be set before
     *  much can be done with this actor.
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If cloned ports cannot have
     *   as their container the cloned entity (this should not occur), or
     *   if one of the attributes cannot be cloned.
     *  @return A new Bus.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        AFDXSwitch newObject = (AFDXSwitch) super.clone(workspace);
        _ioPortToSwitchInPort = new HashMap<Port, Integer>();
        _ioPortToSwitchOutPort = new HashMap<Port, Integer>();
        newObject._actorPorts = new HashMap();
        newObject._nextFireTime = null;
        newObject._inputTokens = new HashMap();
        newObject._outputTokens = new HashMap();
        newObject._switchFabricQueue = new LinkedList<TimedEvent>();
        return newObject;
    }

    /** Initialize the actor variables.
     *  @exception IllegalActionException If the superclass throws it or
     *  the switch table could not be parsed from the actor parameters.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _nextFireTime = null;
/*
        // Read the switching table from the parameters.
        for (int i = 0; i < attributeList().size(); i++) {
            Attribute attribute = (Attribute) attributeList().get(i);
            try {
                int portNumber = Integer.parseInt(attribute.getName());
                Parameter param = (Parameter) attribute;
                Token token = param.getToken();
                Actor actor = (Actor) ((ObjectToken) token).getValue();
                System.out.println("actor:"+actor.toString());
                _actorPorts.put(actor, portNumber);
            } catch (NumberFormatException ex) {
                // Parameter was not a number and therefore not a part of
                // the routing table.
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new IllegalActionException(this, "There was an error"
                        + "in the routing table information for "
                        + this.getName());
            }
        }*/
        for (int i = 0; i < _numberOfPorts; i++) {
            _inputTokens.put(i, new LinkedList<TimedEvent>());
            _outputTokens.put(i, new LinkedList<TimedEvent>());
        }
        _switchFabricQueue = new LinkedList<TimedEvent>();

    }

    /** Move tokens from the input queue to the switch fabric, move tokens
     *  from the switch fabric queue to the output queues and send tokens from the
     *  output queues to the target receivers. When moving tokens between
     *  queues the appropriate delays are considered.
     *  @exception IllegalActionException If the token cannot be sent to
     *  target receiver.
     */
    public void fire() throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        Time computedTimeStamp = null;
        boolean multicast = false;

        // In a continuous domain this actor could be fired before any token has
        // been received; _nextTimeFree could be null.
        if (_nextFireTime != null && currentTime.compareTo(_nextFireTime) == 0) {

            // Move tokens from input queue to switch fabric.

            TimedEvent event;
            for (int i = 0; i < _numberOfPorts; i++) {
                if (_inputTokens.get(i).size() > 0) {
                    event = _inputTokens.get(i).getFirst();
                    if (event.timeStamp.compareTo(currentTime) == 0) {
                        Time lastTimeStamp = currentTime;                  
                        if (_switchFabricQueue.size() > 0) {
                            Object[] last = (Object[]) _switchFabricQueue.getLast().contents;
                            Object[] eObj = (Object[]) event.contents;
                            
                            if (_debugging) {
                            	_debug("looo");
                            	
                            }
                            System.out.println("last" + ((AFDXVlink) last[2]).getSource().getFullName() + "time="+((Time) last[3]));
                            System.out.println("new" + ((AFDXVlink) eObj[2]).getSource().getFullName() + "time"+ eObj[3]);
                            

                            
                            if ( ((AFDXVlink) last[2]).getSource().equals(((AFDXVlink) eObj[2]).getSource())
                                    && ((Time) last[3]).compareTo(eObj[3]) == 0 ) {
                                multicast = true;
                                System.out.println("multicat="+multicast);

                            }

                            lastTimeStamp = _switchFabricQueue.getLast().timeStamp;
                        }

                        if (multicast) {
                            computedTimeStamp = lastTimeStamp;
                            multicast = false;
                        } else {
                            /*Object[] output = (Object[]) event.contents;
                            AFDXVlink vl = (AFDXVlink) output[2];
                            computedTimeStamp = lastTimeStamp
                                    .add(_technologicalDelay)
                                    .add(vl.getFrameSize()/(_bitRate*1000000));*/
                            computedTimeStamp = currentTime
                                    .add(_technologicalDelay);
                        }
                        _switchFabricQueue.add(new TimedEvent(computedTimeStamp, event.contents));

                        _inputTokens.get(i).remove(event);
                    }
                }
            }
            //  _scheduleRefire();

            // Move tokens from switch fabric to output queue.

            if (_switchFabricQueue.size() > 0) {
                computedTimeStamp = null;
                multicast = false;

                event = _switchFabricQueue.getFirst();
                if (event.timeStamp.compareTo(currentTime) == 0) {
                    Object[] output = (Object[]) event.contents;
                    Receiver receiver = (Receiver) output[0];

                    Actor actor;
                    if (receiver instanceof IntermediateReceiver) {
                        actor = (Actor) ((IntermediateReceiver) receiver).quantityManager;
                    } else {
                        actor = (Actor) receiver.getContainer().getContainer();
                    }
                    //GL: FIXME: XXX: change for new QM implem.
                    //int actorPort = _actorPorts.get(actor);
                    int actorPort = _getPortID(receiver, false);

                    Time lastTimeStamp = currentTime;
                    if (_outputTokens.get(actorPort).size() > 0) {
                        Object[] last = (Object[]) _outputTokens.get(actorPort).getLast().contents;
                        if ( ((AFDXVlink) last[2]).getSource()
                                .equals(((AFDXVlink) output[2]).getSource())
                                && ((Time) last[3]).compareTo(output[3]) == 0 ) {
                            multicast = true;
                        }

                        lastTimeStamp = _outputTokens.get(actorPort).getLast().timeStamp;
                    }

                    if (multicast) {
                        computedTimeStamp = lastTimeStamp;
                        multicast = false;
                    } else {
                        AFDXVlink vl = (AFDXVlink) output[2];
                        computedTimeStamp = lastTimeStamp
                                .add(_outputBufferDelay)
                                .add(vl.getFrameSize()/(_bitRate*1000000));
                    }
                    _outputTokens.get(actorPort)
                    .add(new TimedEvent(computedTimeStamp, event.contents));

                    _switchFabricQueue.remove(event);
                }
            }
            // _scheduleRefire();


            // Send tokens to target receiver.

            for (int i = 0; i < _numberOfPorts; i++) {
                if (_outputTokens.get(i).size() > 0) {
                    event = _outputTokens.get(i).getFirst();
                    if (event.timeStamp.compareTo(currentTime) == 0) {
                        Object[] output = (Object[]) event.contents;

                        // The receiver is an AFDXSwitch (qm). 
                        Receiver receiver = (Receiver) output[0];
                        if (receiver instanceof IntermediateReceiver) {
                            String[] labels = new String[] { timestamp, vlink, payload };
                            Token[] values = new Token[] {new DoubleToken(event.timeStamp.getDoubleValue()), 
                                    new ObjectToken (output[2]),
                                    (Token) output[1] };
                            RecordToken record = new RecordToken(labels, values);
                            _sendToReceiver((Receiver) output[0], record);
                        } else { // Else the receiver is an actor.
                            Token token = (Token) output[1];
                            _sendToReceiver((Receiver) output[0], token);                            
                        }
                        _outputTokens.get(i).remove(event);
                    }
                }

                if (_debugging) {
                    _debug("At time " + currentTime + ", completing send");
                }
            }
            _scheduleRefire();
        }
    }

    /** If there are still tokens in the queue and a token has been
     *  produced in the fire, schedule a refiring.
     *  @exception IllegalActionExeception If the refiring cannot be scheduled or
     *  by super class.
     */
    public boolean postfire() throws IllegalActionException {
        _scheduleRefire();
        return super.postfire();
    }

    /** Initiate a send of the specified token to the specified
     *  receiver. This method will schedule a refiring of this actor
     *  if there is not one already scheduled.
     *  @param source Sender of the token.
     *  @param receiver The sending receiver.
     *  @param token The token to send.
     *  @exception IllegalActionException If the refiring request fails.
     */
    public void sendToken(Receiver source, Receiver receiver, Token token)
            throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        Time computedTimeStamp = null;
        AFDXVlink vl = null;
        Token tok = null;
        boolean multicast = false;

        // FIXME add Continuous support.

        if (token instanceof RecordToken) {
            vl = (AFDXVlink) ((ObjectToken) ((RecordToken) token).get("vlink")).getValue();
            tok = ((RecordToken) token).get("payload");
        }

        // IntermediateReceiver ir = (IntermediateReceiver) source;

        int actorPortId = _getPortID(receiver, true);

        /*if (ir.source != null) {
            Actor sender = ir.source;
            actorPortId = _actorPorts.get(sender);
        } else {
            throw new IllegalActionException(this, "The receiver " + receiver
                    + "does not have a source");
        }*/

        Time lastTimeStamp = currentTime;
        if (_inputTokens.get(actorPortId).size() > 0) {
            if (currentTime.compareTo(
                    (Time) ((Object[]) _inputTokens.get(actorPortId).getLast().contents)[3]) 
                    == 0) {
                multicast = true;
            }

            lastTimeStamp = _inputTokens.get(actorPortId).getLast().timeStamp;
        }        

        if (multicast) {
            computedTimeStamp = lastTimeStamp;
            multicast = false;
        } else {
            computedTimeStamp = lastTimeStamp.add(_inputBufferDelay);
        }

        _inputTokens.get(actorPortId).add(
                new TimedEvent(computedTimeStamp,
                        new Object[] {receiver, tok , vl , currentTime}));

        _tokenCount++;
        sendQMTokenEvent((Actor) source.getContainer().getContainer(), 0,
                _tokenCount, EventType.RECEIVED);
        _scheduleRefire();

        if (_debugging) {
            _debug("At time " + getDirector().getModelTime()
                    + ", initiating send to "
                    + receiver.getContainer().getFullName() + ": " + token);
        }
    }

    /** Reset the quantity manager and clear the tokens.
     */
    public void reset() {
        _inputTokens.clear();
        _outputTokens.clear();
        _switchFabricQueue.clear();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Technological latency according to the AFDX specification.
     *  This parameter must contain a DoubleToken. The value defaults
     *  to 0.000140 s (140 us). */
    public Parameter technologicalDelay;

    /** The bit rate of the bus. The value defaults to 100 Mbits/s.
     */
    public Parameter bitRate;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Get next fire time for a set of tokens which is either the minimum
     *  next fire time passed as an argument or the smallest timestamp of
     *  the tokens in the set.
     *  @param nextFireTime Minimum next fire time.
     *  @param tokens The set of tokens.
     *  @return The next time this actor should be fired based on the tokens
     *  in the queue.
     */
    protected Time _getNextFireTime(Time nextFireTime,
            LinkedList<TimedEvent> tokens) {
        if (tokens.size() > 0) {
            TimedEvent event = tokens.getFirst();
            if (event.timeStamp.compareTo(nextFireTime) < 0) {
                nextFireTime = event.timeStamp;
            }
        }
        return nextFireTime;
    }

    /** Schedule a refiring of this actor based on the tokens in the queues.
     *  @exception IllegalActionException If actor cannot be refired
     *  at the computed time.
     */
    protected void _scheduleRefire() throws IllegalActionException {
        _nextFireTime = Time.POSITIVE_INFINITY;
        for (int i = 0; i < _numberOfPorts; i++) {
            _nextFireTime = _getNextFireTime(_nextFireTime, 
                    _inputTokens.get(i));
            _nextFireTime = _getNextFireTime(_nextFireTime,
                    _outputTokens.get(i));
        }
        _nextFireTime = _getNextFireTime(_nextFireTime, _switchFabricQueue);
        _fireAt(_nextFireTime);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Time it takes for a token to be processed by the switch fabric. */
    protected double _technologicalDelay;

    /** Mapping of actors to switch ports. */
    protected HashMap<Actor, Integer> _actorPorts;

    /** Next time a token is sent and the next token can be processed. */
    protected Time _nextFireTime;

    /** Tokens received by the switch. */
    protected HashMap<Integer, LinkedList<TimedEvent>> _inputTokens;

    /** Tokens to be sent to outputs. */
    protected HashMap<Integer, LinkedList<TimedEvent>> _outputTokens;

    /** Value of the bit rate of the bus. */
    protected double _bitRate;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Tokens processed by the switch fabric. */
    private LinkedList<TimedEvent> _switchFabricQueue;

    /** Label of the timestamp that is transmitted within the RecordToken. */
    private static final String timestamp = "timestamp";

    /** Label of the vlink that is transmitted within the RecordToken. */
    private static final String vlink = "vlink";

    /** Label of the payload that is transmitted within the RecordToken. */
    private static final String payload = "payload";
     


}
