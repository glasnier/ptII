/* List of cross-references.

 Copyright (c) 1997- The Regents of the University of California.
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

@ProposedRating Green (eal@eecs.berkeley.edu)

*/

package pt.kernel.util;

import collections.CorruptedEnumerationException;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.io.Serializable;

//////////////////////////////////////////////////////////////////////////
//// CrossRefList
/** 

CrossRefList is a list that maintains pointers to other CrossRefLists.
This class is meant to be used to keep track of links between Objects,
like the arcs between some nodes of a graph, for example.
CrossRefList is an implementation class that requires an Object to
contain it.  (This container is immutable in that it cannot be changed
after the instance is constructed.)  CrossRefList enumerators and
query methods do not return references to other CrossRefLists;
instead, references to those CrossRefLists' containers (of type
Object) are returned.
<p> 
For efficiency, CrossRefList maintains a list of pairwise links
between Objects (CrossRefs). That is, each member of a set of objects
has a list of references to other members of the set, and each
reference has a similar list that contains a corresponding back
reference. Each reference list is an instance of this class. The class
is used as if it were a simple list of references to objects, but it
ensures the symmetry of the references, and supports efficient removal
of links.  Removing a reference in one list automatically updates N
back-references in O(N) time, independent of the sizes of the
cross-reference lists.
<p> 
CrossRefList implements the Serializable interface but this has not
been tested.

@author Geroncio Galicia
@contributor Edward A. Lee
@version $Id$
*/
public final class CrossRefList implements Serializable  {

    /** Construct a list with the specified container.
     *  The container is immutable (it cannot be changed).
     *  @param container The container of the object to be constructed.
     *  @exception IllegalActionException If the argument is null.
     */
    public CrossRefList(Object container)
            throws IllegalActionException {
        if (container == null) {
            throw new IllegalActionException(
                    "Attempt to create CrossRefList with a null container.");
        }
        _container = container;
    }

    /** Create a new CrossRefList that is linked to the same
     *  CrossRefLists as the original CrossRefList except that this
     *  new one has a new container.  This method synchronizes on the
     *  original list.  Note that this is not a true copy constructor.
     *  @param container The container of the object to be constructed.
     *  @param originalList The model to copy.  
     *  @exception IllegalActionException If either argument is null.
     */
    public CrossRefList(Object container, CrossRefList originalList)
            throws IllegalActionException {
        this(container);
        if (originalList == null) {
            throw new IllegalActionException(
                    "Attempt to copy a CrossRefList from a null model.");
        }
        synchronized(originalList) {
            if(originalList.size() == 0) return; // List to copy is empty.
            for(CrossRef p = originalList._headNode; p != null; p = p._next) {
                if(p._far._nearList() != null) {
                    link(p._far._nearList());
                }
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Return the first container referenced by this list, or
     *  null if the list is empty.  
     *  Time complexity: O(1).  
     *  @return The first entry, or null if there is none.  
     */
    public synchronized Object first() {
        if (_headNode != null) {
            return _headNode._farContainer();
        } else {
            return null;
        }
    }

    /** Enumerate the containers referenced by this list.  The
     *  enumeration returns the container object itself and not the
     *  CrossRefList instance in this list that the object owns or
     *  contains.  Note that an object may be enumerated more than
     *  once if more than one link to it has been established.
     *  Time complexity: O(1).
     *  @return An enumeration of remote referenced objects.  
     */
    public synchronized Enumeration getContainers() {
        return new CrossRefEnumeration();
    }

    /** Return true if the specified container is referenced on this
     *  list.
     *  Time complexity: O(n).
     *  @param obj An object that might be referenced.
     *  @return A boolean indicating whether the object is referenced.
     */
    public synchronized boolean isLinked(Object obj) {
        if (obj == null || _size == 0) return false;
        for(CrossRef p = _headNode; p != null; p = p._next) {
            if (p._farContainer().equals(obj)) {
                return true;
            }
        }
        return false;
    }

    /** Link this list to a different CrossRefList (farList).
     *  This action additionally creates a back-reference in the far list.
     *  Redundant entries are allowed.
     *  Note that this method does not synchronize on the remote object.
     *  Thus, this method should be called within a write-synchronization of
     *  the common workspace.
     *  Time complexity: O(1).
     *  @param farList The cross reference list contained by the remote object.
     *  @exception IllegalActionException If the remote object is the container
     *   of this cross reference list.
     */
    public synchronized void link(CrossRefList farList)
            throws IllegalActionException {
        if(farList == this) {
            throw new IllegalActionException(
                    "CrossRefLink.link: Illegal self-link.");
        }

        ++_listVersion;
        CrossRef localCrossRef = new CrossRef();
        // NOTE: In jdk 1.2 or higher, the line below could be
        // put in an initializer and _far could be made a "blank final."
        // This would prevent modifications to _far, equivalent
        // to making it a const in C++.
        localCrossRef._far = farList.new CrossRef(localCrossRef);
    }

    /** Return size of this list.
     *  Time complexity: O(1).
     *  @return A non-negative integer.
     */
    public synchronized int size() {
        return _size;
    }

    /** Delete a link to the specified container.  If there is no such
     *  link, ignore.  That is, delete the entry in this list that
     *  points to a different CrossRefList contained by the
     *  specified object.  That object's CrossRefList list is
     *  similarly updated to remove this from its members.
     *  Time complexity: O(n).  
     *  @param obj The object to delete.
     */
    public synchronized void unlink(Object obj) {
        if (obj == null || _size == 0) return;
        ++_listVersion;
        Object v;
        CrossRef p = _headNode;
        while(p != null) {
            CrossRef n = p._next;
            // If associations are always created using link then
            // p._farContainer() will never return null.
            if (p._farContainer().equals(obj)) {
                p._dissociate();
                return;
            }
            p = n;
        }
    }

    /** Delete all cross references.
     *  Time complexity: O(n).
     */
    public synchronized void unlinkAll() {
        if(_size == 0) return;
        ++_listVersion;

        CrossRef deadHead = _headNode; // _deadHead is marked for deletion.

        // As long as there's a next element.
        while( (_headNode = deadHead._next) != null) {
            deadHead._dissociate();     // Delete old head and its partner.
            deadHead = _headNode;
        }
        // Delete the last CrossRef.
        deadHead._dissociate();

        // Mark the list empty.
        _headNode = null;
        _lastNode = null;
        _size = 0;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    // Version number is incremented each time the list is modified.
    // This is used to make sure that elements accessed via an enumeration
    // are valid.  This is inspired by a similar mechanism in Doug Lea's
    // Java Collections.
    private int _listVersion = 0;

    // The code ensures that if this is non-zero, then _headNode is non-null.
    private int _size = 0;

    private CrossRef _headNode;

    private CrossRef _lastNode;

    // NOTE: In jdk 1.2 or higher, this could be made final to prohibit
    // what is called "reference reseating" (not resetting), i.e. to
    // make the variable immutable.
    private Object _container;

    //////////////////////////////////////////////////////////////////////////
    ////                         inner classes                            ////

    // Class CrossRef.
    // Objects of this type form the elements of the list.
    // They occur in pairs, one in each list at each end of a link.
    // A CrossRef is similar to a "link" in a doubly-linked list.
    protected class CrossRef implements Serializable{

        protected CrossRef _far;

        private CrossRef _next;

        private CrossRef _previous;

        public CrossRef() { this(null); }

        public  CrossRef(CrossRef spouse) {
            _far = spouse;
            if(_size > 0) {
                _previous = _lastNode;
                _lastNode._next = this;
                _lastNode = this;
            } else {
                // List is empty.
                _lastNode = this;
                _headNode = this;
            }
            ++_size;
        }

        private synchronized Object _nearContainer() {
            return _container;
        }

        private synchronized Object _farContainer() {
            // Returning null shouldn't happen.
            return _far != null ? _far._nearContainer() : null;
        }

        private synchronized CrossRefList _nearList() {
            return CrossRefList.this;
        }

        private synchronized void _dissociate() {
            _unlink(); // Remove this.
            if(_far != null) _far._unlink(); // Remove far
        }

        private synchronized void _unlink() {
            ++_listVersion;
            // Removes this from enclosing CrossRefList.
            if(_next != null) _next._previous = _previous; // Modify next.
            else _lastNode = _previous;
            if(_previous != null) _previous._next = _next; // Modify previous.
            else _headNode = _next;
            _size--; // Modify list.
        }
    }


    /** Enumerate the objects pointed to by the list.
     *  @see CrossRefList
     */
    private class CrossRefEnumeration implements Enumeration {

        public CrossRefEnumeration() {
            _enumeratorVersion = _listVersion;
            _startAtHead = true;
            _ref = null;
        }

        /** Return true if there are more elements to enumerate. */
        public boolean hasMoreElements() {
            if(_enumeratorVersion != _listVersion) {
                throw new CorruptedEnumerationException();
            }
            CrossRef tmp1 = _ref; // Callee-save.
            boolean tmp2 = _startAtHead; // Callee-save.
            Object tmpObj;
            try {
                tmpObj = nextElement();
            } catch (NoSuchElementException ex) {
                tmpObj = null;
            }
            _ref = tmp1; // Restore state.
            _startAtHead = tmp2; // Restore state.
            return tmpObj != null;
        }

        /** Return the next element in the enumeration.
         *  @exception java.util.NoSuchElementException Exhausted enumeration.
         */
        public Object nextElement()
                throws NoSuchElementException {
            if(_enumeratorVersion != _listVersion) {
                throw new CorruptedEnumerationException();
            }
            if(_startAtHead) {
                // Starting at beginning of list.
                _startAtHead = false;
                if(_headNode != null) {
                    // List not empty.
                    _ref = _headNode;
                    return _ref._farContainer();
                } else {
                    // List is empty, throw exception.
                    throw new NoSuchElementException("exhausted enumeration");
                }
            } else {
                // Not at beginning of list.
                if (_ref != _lastNode) {
                    // Not at end of list.
                    if (_ref != null) {
                        // If pointer to element not NULL, return next.
                        _ref = _ref._next;
                        return _ref._farContainer();
                    } else {
                        // If pointer is NULL, then end of list was
                        // already passed.  Throw exception.
                        throw new NoSuchElementException(
                                "exhausted enumeration");
                    }
                } else {
                    // At end of list.
                    throw new NoSuchElementException("exhausted enumeration");
                }
            }
        }

        private int _enumeratorVersion;

        private CrossRef _ref;

        private boolean _startAtHead;
    }
}

