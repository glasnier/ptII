/* A Director governs the execution of a CompositeActor.

 Copyright (c) 1997-1998 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
*/

package ptolemy.actor;

import ptolemy.graph.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.mutation.*;
import ptolemy.data.*;

import collections.LinkedList;
import java.util.Enumeration;


//////////////////////////////////////////////////////////////////////////
//// Director
/**
A Director governs the execution within a CompositeActor.  A compsite actor 
that contains a director is considered opaque, and the execution model 
within the composite actor is determined by the contained Director.   This
director is called the local director of a composite actor.   A composite
actor is also aware of the director of its container, which is referred to
as its executive director.  
<p>
A top-level composite actor is generally associated with a Manager as well as
a local director.  The Manager has overall responsibility for
executing the application, and is often associated with a GUI.   Top-level
composite actors have no executive director and getExecutiveDirector() will
return null.
<p>
A local director is responsible for invoking the actors contained by the
composite.  If there is no local director, then the executive director
is given the responsibility.  The getDirector() method of CompositeActor,
therefore, returns the local director, if there is one, and otherwise
returns the executive director.  Thus, it returns whichever director
is responsible for executing the contained actors, or null if there is none.
Whatever it returns is called simply the <i>director</i> (vs. local
director or executive director).
<p>
A director implements the action methods (initialize(), prefire(), fire(),
postfire(), and wrapup()).  In this base class, default implementations
are provided that may or may not be useful in specific domains.   In general,
these methods will perform domain-dependant actions, and then call the 
respective methods in all contained actors.
<p>
A director also provides services for cleanly handling mutations of the
topology.  Mutations include such changes as adding or removing an entity,
port, or relation, and creating or destroying a link.  Usually,
mutations cannot safely occur at arbitrary points in the execution of
an application.  Applications can queue mutations with the director,
and the director will then perform the mutations at the first opportunity,
when it is safe.  In this base-class implementation, mutations are performed
at the beginning of each iteration by the prefire() method.
<p>
A service is also provided whereby an object can be registered with the
director as a mutation listener.  A mutation listener is informed of
mutations that occur when they occur (in this base class, at the beginning
of the iterate() method).
<p>
One particular mutation listener, called an ActorListener, is added
to a director the first time a mutation is performed.  This listener
ignores all mutations except those that add or remove an actor.
For those mutations, it records the addition or deletion.
After all the mutations have been completed in the prefire() method,
any actors that are new to the composite have their initialize() methods
invoked.
<p>
An initialize() method may queue further mutations with the director.
Thus, the prefire() method repeatedly performs mutations and initializations
until there are no more mutations to perform.

@author Mudit Goel, Edward A. Lee, Lukito Muliadi, Steve Neuendorffer
@version: $Id$
*/
public class Director extends NamedObj implements Executable {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public Director() {
        super();
    }

    /** Construct a director in the default workspace with the given name.
     *  If the name argument is null, then the name is set to the empty
     *  string. The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param name Name of this object.
     */
    public Director(String name) {
        super(name);
    }

    /** Construct a director in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The director is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     */
    public Director(Workspace workspace, String name) {
        super(workspace, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Adds a new mutation listener to the list of listeners to be informed
     *  about any mutation that occurs in the container.
     *
     *  @param listener The new MutationListener.
     */
    public void addMutationListener(MutationListener listener) {
        if (_mutationListeners == null) {
            _mutationListeners = new LinkedList();
        }
        _mutationListeners.insertLast(listener);
    }

    /** Clone the director into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new director with no container, no pending mutations,
     *  and no mutation listeners.
     *
     *  @param ws The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     *  @return The new Director.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        Director newobj = (Director)super.clone(ws);
        newobj._container = null;
        newobj._pendingMutations = null;
        newobj._mutationListeners = null;
        newobj._actorListener = null;
        return newobj;
    }

    /** Invoke an iteration on all of the deeply contained actors of the
     *  container of this Director.  In general, this may be called more
     *  than once in the same iteration of the Directors container.
     *  An iteration is defined as multiple invocations of prefire(), until
     *  it returns true, any number of invocations of fire(),
     *  followed by one invocation of postfire().
     *  Notice that we ignore the return value of postfire() in this base
     *  class.   In general, derived classes will want to do something
     *  intelligent with the returned value.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If any called method of the
     *   container or one of the deeply contained actors throws it.
     */
    public void fire() throws IllegalActionException {
        CompositeActor container = ((CompositeActor)getContainer());
        if (container!= null) {
            Enumeration allactors = container.deepGetEntities();
            while (allactors.hasMoreElements()) {
                Actor actor = (Actor)allactors.nextElement();
                if(actor.prefire()) {
                    actor.fire();
                    actor.postfire();
                }
            }
        }
    }

    /** Return the container, which is the composite actor for which this
     *  is the local or executive director.
     *  @return The CompositeActor that this director is responsible for.
     */
    public Nameable getContainer() {
        return _container;
    }

    /** If this is the local director of its container, first create the
     *  receivers and then invoke the initialize()
     *  methods of all its deeply contained actors.  If this is the executive
     *  director of its container, first create the receivers and then
     *  invoke the initialize() method of the container.
     *  <p>
     *  This method should be invoked once per execution, before any
     *  iteration. It may produce output data.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If the initialize() method of the
     *   container or one of the deeply contained actors throws it.
     */
    public void initialize() throws IllegalActionException {
        CompositeActor container = ((CompositeActor)getContainer());
        if (container!= null) {
            Enumeration allactors = container.deepGetEntities();
            while (allactors.hasMoreElements()) {
                Actor actor = (Actor)allactors.nextElement();
                // FIXME: is the order here right??  shouldn't we initialize
                // first and then createReceivers, or deal with CreateReceivers
                // at the toplevel?
                actor.createReceivers();
                actor.initialize();
            }
        }
    }

    /** Return a new receiver of a type compatible with this director.
     *  In this base class, this returns an instance of Mailbox.
     *  @return A new Mailbox.
     */
    public Receiver newReceiver() {
        return new Mailbox();
    }

    /* If this is the local director of its container, invoke the postfire()
     *  methods of all its deeply contained actors, and return the logical AND
     *  of what they return.  If this is the executive director of its
     *  container then invoke the postfire() method of the container and return
     *  what it returns.  Otherwise, return false.
     *  <p>
     *  This method should be invoked once per iteration, after the last
     *  invocation of fire() in that iteration. It may produce output data.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @return True if the execution can continue into the next iteration.
     *  @exception IllegalActionException If the postfire() method of the
     *   container or one of the deeply contained actors throws it.
     */

    /** Return false.   The default director will only get fired once, and will
     *  terminate execution afterwards.   Domain Directors will probably want
     *  to override this method.   Note that this is called by the container of
     *  this Director to see if the Director wishes to execute anymore, and 
     *  should *NOT*, in general, just take the logical AND of calling
     *  postfire on all the contained actors.
     *
     *  @return True if the Director wishes to be scheduled for another
     *  iteration
     *  @exception IllegalActionException *Deprecate* If the postfire() 
     *  method of the container or one of the deeply contained actors 
     *  throws it.
     */

    public boolean postfire() throws IllegalActionException {
        /*        CompositeActor container = ((CompositeActor)getContainer());
        if (container!= null) {
            //           if (!_executivedirector) {
                // This is the local director.
                Enumeration allactors = container.deepGetEntities();
                boolean oktocontinue = true;
                while (allactors.hasMoreElements()) {
                    Actor actor = (Actor)allactors.nextElement();
                    oktocontinue = actor.postfire() && oktocontinue;
                }
                return oktocontinue;
   //           } else {
                // This is the executive director.
                //                return container.postfire();
                //            }
        }*/
        return false;
    }

    /* Prepare for firing and return true if firing can proceed.
     *  If there is no container, return false immediately.  Otherwise,
     *  the first step is to perform any pending mutations, and to initialize
     *  any actors that are added by those mutations.  This sequence is
     *  repeated until no more mutations are performed. This way, the
     *  initialize() method in actors can perform mutations, and the
     *  mutations will be fully executed before proceeding.
     *  Then, if this is the local director of its container, first create
     *  any new receivers if mutations occured. Then invoke the
     *  prefire() methods of all its deeply contained actors, and return the
     *  logical AND of what they return.  If this is the executive director
     *  of its container, then invoke the prefire() method of the container and
     *  return what it returns.  Otherwise, return false.
     *  <p>
     *  This method should be invoked once per iteration, before any
     *  invocation of fire() in that iteration. It may produce output data.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @return True if the iteration can proceed.
     *  @exception IllegalActionException If the prefire() method of the
     *   container or one of the deeply contained actors throws it, or a
     *   pending mutation throws it, or if all receivers could not be created.
     *  @exception NameDuplicationException If a pending mutation throws it.
     */
    /** return True, indicating that the Director is ready to fire.   
     *  Domain Directors will probably want
     *  to override this method.   Note that this is called by the container of
     *  this Director to see if the Director is ready to execute, and 
     *  should *NOT*, in general, just take the logical AND of calling
     *  prefire on all the contained actors.
     *
     *  @return True if the Director wishes to be scheduled for another
     *  iteration
     *  @exception IllegalActionException *Deprecate* If the postfire() 
     *  method of the container or one of the deeply contained actors 
     *  throws it.
     */
    public boolean prefire() throws IllegalActionException {
            /*        CompositeActor container = ((CompositeActor)getContainer());
        if (container!= null) {
            // Perform mutations and initializations until there are no
            // more to perform.
            boolean mutationOccured = false;
            while (_performMutations()) {
                mutationOccured = true;
                // Initialize any new actors
                _actorListener.initializeNewActors();
            }
                 // If a mutation occured, need to create create new receivers.
                if (mutationOccured) {
                    Enumeration allactors = container.deepGetEntities();
                    while (allactors.hasMoreElements()) {
                        ((Actor)allactors.nextElement()).createReceivers();
                    }
                }
                // Invoke the prefire() method of deeply contained entities.
                Enumeration allactors = container.deepGetEntities();
                boolean allready = true;
                while (allactors.hasMoreElements()) {
                    Actor actor = (Actor)allactors.nextElement();
                    allready = actor.prefire() && allready;
                }
                return allready;
         }*/
        return true;
    }

    /** Add a mutation object to the mutation queue. These mutations
     *  are executed when the _performMutations() method is called,
     *  which in this base class is in the prefire() method.  This method
     *  also arranges that all additions of new actors are recorded.
     *  The prefire() method then invokes the initialize() method of all
     *  new actors after the mutations have been completed.
     *
     *  @param mutation A object with a perform() and update() method that
     *   performs a mutation and informs any listeners about it.
     */
    public void queueMutation(Mutation mutation) {
        // The private member is created only if mutation is being used.
        if (_pendingMutations == null) {
            _pendingMutations = new LinkedList();
        }
        _pendingMutations.insertLast(mutation);
    }

    /** Remove a mutation listener that does not want to be informed
     *  of any future mutations by this director. This does not do anything
     *  if the listener is not listed with this director.
     *
     *  @param listener The MutationListener to be removed.
     */
    public void removeMutationListener(MutationListener listener) {
        _mutationListeners.removeOneOf(listener);
    }

    /** Recursively terminate all of our actors.   Domains may need to 
     *  override this to properly deal with any threads they've created.
     */   
    public void terminate() {
        CompositeActor container = ((CompositeActor)getContainer());
        if (container!= null) {
            Enumeration allactors = container.deepGetEntities();
            while (allactors.hasMoreElements()) {
                Actor actor = (Actor)allactors.nextElement();
                actor.terminate();
            }
        }
    }
        
    /** Transfer data from an input port of the container to the
     *  ports it is connected to on the inside.  The port argument must
     *  be an opaque input port.  If any channel of the input port
     *  has no data, then that channel is ignored.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   input port.
     */
    public void transferInputs(IOPort port) throws IllegalActionException {
        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "transferInputs: port argument is not an opaque input port.");
        }
        Receiver[][] insiderecs = port.deepGetReceivers();
        for (int i=0; i < port.getWidth(); i++) {
            if (port.hasToken(i)) {
                try {
                    Token t = port.get(i);
                    if (insiderecs != null && insiderecs[i] != null) {
                        for (int j=0; j < insiderecs[i].length; j++) {
                            insiderecs[i][j].put(t);
                        }
                    }
                } catch (NoTokenException ex) {
                    // this shouldn't happen.
                    throw new InternalErrorException(
                            "Director.transferInputs: Internal error: " +
                            ex.getMessage());
                }
            }
        }
    }

    /** Transfer data from an output port of the container to the
     *  ports it is connected to on the outside.  The port argument must
     *  be an opaque output port.  If any channel of the output port
     *  has no data, then that channel is ignored.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   output port.
     */
    public void transferOutputs(IOPort port) throws IllegalActionException {
        if (!port.isOutput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "transferOutputs: port argument is not an opaque output port.");
        }
        Receiver[][] insiderecs = port.getInsideReceivers();
        if (insiderecs != null) {
            for (int i=0; i < insiderecs.length; i++) {
                if (insiderecs[i] != null) {
                    for (int j=0; j < insiderecs[i].length; j++) {
                        if (insiderecs[i][j].hasToken()) {
                            try {
                                Token t = insiderecs[i][j].get();
                                port.send(i,t);
                            } catch (NoTokenException ex) {
                                throw new InternalErrorException(
                                        "Director.transferOutputs: " +
                                        "Internal error: " +
                                        ex.getMessage());
                            }
                        }
                    }
                }
            }
        }
    }

    /** If this is the local director of its container, invoke the wrapup()
     *  methods of all its deeply contained actors.  If this is the executive
     *  director of the container, then invoke the wrapup() method of the
     *  container.
     *  <p>
     *  This method should be invoked once per execution.  None of the other
     *  action methods should be invoked after it in the execution.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If the wrapup() method of the
     *   container or one of the deeply contained actors throws it.
     */
    public void wrapup() throws IllegalActionException {
        CompositeActor container = ((CompositeActor)getContainer());
        if (container!= null) {
            Enumeration allactors = container.deepGetEntities();
            while (allactors.hasMoreElements()) {
                Actor actor = (Actor)allactors.nextElement();
                actor.wrapup();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a description of the object.  The level of detail depends
     *  on the argument, which is an or-ing of the static final constants
     *  defined in the NamedObj class.  Lines are indented according to
     *  to the level argument using the protected method _getIndentPrefix().
     *  Zero, one or two brackets can be specified to surround the returned
     *  description.  If one is specified it is the the leading bracket.
     *  This is used by derived classes that will append to the description.
     *  Those derived classes are responsible for the closing bracket.
     *  An argument other than 0, 1, or 2 is taken to be equivalent to 0.
     *  This method is read-synchronized on the workspace.
     *  @param detail The level of detail.
     *  @param indent The amount of indenting.
     *  @param bracket The number of surrounding brackets (0, 1, or 2).
     *  @return A description of the object.
     */
    protected String _description(int detail, int indent, int bracket) {
        try {
            workspace().getReadAccess();
            String result;
            if (bracket == 1 || bracket == 2) {
                result = super._description(detail, indent, 1);
            } else {
                result = super._description(detail, indent, 0);
            }
            // FIXME: Add director-specific information here, like
            // what is the state of the director.
            // if ((detail & FIXME) != 0 ) {
            //  if (result.trim().length() > 0) {
            //      result += " ";
            //  }
            //  result += "fixme {\n";
            //  result += _getIndentPrefix(indent) + "}";
            // }
            if (bracket == 2) result += "}";
            return result;
        } finally {
            workspace().doneReading();
        }
    }

    /** Make this director the local director of the specified composite
     *  actor.  This method should not be called directly.  Instead, call
     *  setDirector of the CompositeActor class (or a derived class).
     */
    protected void _makeDirectorOf (CompositeActor cast) {
        _container = cast;
        //        _executivedirector = false;
        if (cast != null) {
            workspace().remove(this);
        }
    }

    /** Perform all pending mutations and inform all registered listeners
     *  of the mutations.  Return true if any mutations were performed,
     *  and false otherwise.
     *
     *  @exception IllegalActionException If the mutation throws it.
     *  @exception NameDuplicationException If the mutation throws it.
     */
    protected boolean _performMutations()
            throws IllegalActionException, NameDuplicationException {

        if (_pendingMutations == null) return false;

        // The private member is created only if mutation is being used.
        if (_actorListener == null) {
            _actorListener = new ActorListener();
            addMutationListener(_actorListener);
        }
        boolean result = false;
        Enumeration mutations = _pendingMutations.elements();
        while (mutations.hasMoreElements()) {
            Mutation m = (Mutation)mutations.nextElement();

            // perform the mutation
            m.perform();

            result = true;

            // inform all listeners
            Enumeration listeners = _mutationListeners.elements();
            while (listeners.hasMoreElements()) {
                m.update((MutationListener)listeners.nextElement());
            }
        }
        // Clear the mutations
        _pendingMutations.clear();
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The composite of which this is either a local or an executive director.
    private CompositeActor _container = null;

    // True if this is an executive director of the container.
    //    private boolean _executivedirector;

    // Support for mutations.
    private LinkedList _pendingMutations = null;
    private LinkedList _mutationListeners = null;
    private ActorListener _actorListener = null;
}
