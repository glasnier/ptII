/* A Parameter is an Attribute that contains a token.

 Copyright (c) 1998 The Regents of the University of California.
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

@ProposedRating Yellow (nsmyth@eecs.berkeley.edu)
@AcceptedRating Yellow (yuhong@eecs.berkeley.edu)

*/

package ptolemy.data.expr;

import ptolemy.kernel.util.*;
import ptolemy.data.Token;
import ptolemy.data.TypeLattice;
import collections.LinkedList;
import java.util.Enumeration;
import ptolemy.graph.CPO;

//////////////////////////////////////////////////////////////////////////
//// Parameter
/**
A Parameter is an Attribute that contains a token.
<p>
A parameter can be given a Token or an expression as its value. To 
create a parameter with a Token, either call the appropriate constructor, 
or create the parameter with the appropriate expression and name, and 
then call setToken() to place the token in this parameter. To create a 
parameter from an expression, create the parameter with the appropriate 
container and name, then call setExpression() to set its value. If a 
parameter may be referred to by expressions in other parameters, the 
name of a parameter cannot have any blank spaces in it.
<P>
If it is given an expression, then the token contained by this parameter needs
to be resolved via a call to evaluate(). If the expression string is null,
or if the token placed in it is null, then the token contained will be null.
If the parameter is set from an expression, PtParser is used to generate
a parse tree from the expression which can then be evaluated to a token.
Calling getToken() also results in evaluate() being called if the 
expression in the parameter has not yet been evaluated.
<p>
The type of a Parameter is set by the first non-null Token placed in it.
It is stored as a Class object representing the appropriate class.
The type of the Token returned by getToken() is the same as the type of 
the Parameter. Thus even if a Parameter contains an IntToken, a 
DoubleToken will be returned if the type of the Parameter is DoubleToken.
The actual token contained by the Parameter is obtained by calling 
getContainedToken(). The distinction between the Token contained by the 
Parameter and the Token returned by getToken() is only in the type of 
the Token: the data value will always be the same.
<p>
The type of a Parameter can be changed later via a call to setType(). However 
the new type for the parameter must be able to contain the currently 
contained  token. At any stage a new Token or expression can be 
given to the Parameter. The type of the new/resulting Token is checked 
to see if it can be converted to the type of the Parameter in a 
lossless manner. If it cannot an exception is thrown.
<p>
If another object (e.g. Parameter) wants to be notified of changes in this
Parameter, it must implement the ParameterListener interface and register
itself as a listener with this Parameter. Since Tokens are immutable, the
value contained by this Parameter only changes when a new Token is placed
in it or it is given a new expression which is evaluated.
<p>
A Parameter can also be reset. If the Parameter was originally set from a
Token, then this Token is placed again in the Parameter. If the Parameter
was originally given an expression, then this expression is placed again
in the Parameter and evaluated. Note that the type of the Token resulting
from reset must be compatible with the current Parameter type.
<p>
@author Neil Smyth
@version $Id$
@see ptolemy.kernel.util.Attribute
@see ptolemy.data.expr.PtParser
@see ptolemy.data.Token
*/

public class Parameter extends Attribute implements ParameterListener {

    /** Construct a parameter in the default workspace with an empty string
     *  as its name.
     *  The parameter is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     */
    public Parameter() {
        super();
    }

    /** Construct a parameter in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the parameter.
     */
    public Parameter(Workspace workspace) {
        super(workspace);
    }

    /** Construct a parameter with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This parameter will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  The object is not added to the list of objects in the workspace
     *  unless the container is null.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of the parameter.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a parameter already in the container.
     */
    public Parameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Construct a Parameter with the given container, name, and Token.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This parameter will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  The object is not added to the list of objects in the workspace
     *  unless the container is null.
     *  Increment the version of the workspace.
     *  If the name argument is null, then the name is set to the empty
     *  string.
     *  @param container The container.
     *  @param name The name.
     *  @param token The Token contained by this Parameter.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an parameter already in the container.
     */
    public Parameter(NamedObj container, String name, ptolemy.data.Token token)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setToken(token);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Register a ParameterListener with this Parameter.
     *  @param newListener The ParameterListener that is will be notified
     *   whenever the value of this Parameter changes.
     */
     public void addParameterListener(ParameterListener newListener) {
         if (_listeners == null) {
             _listeners = new LinkedList();
         }
         _listeners.insertLast(newListener);
     }

     /** Clone the parameter.
     *  The state of the cloned parameter will be identical to the original
     *  parameter, but without the ParameterListener dependencies set up.
     *  To achieve this evaluate() should be called after cloning the
     *  parameter.  Evaluate() should only be called after all
     *  the parameters on which this parameter depends have been created.
     *  @param The workspace in which to place the cloned Parameter.
     *  @exception CloneNotSupportedException If the parameter
     *   cannot be cloned.
     *  @see java.lang.Object#clone()
     *  @return An identical Parameter.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        Parameter newobj = (Parameter)super.clone(ws);
        newobj._castToken = null;
        newobj._currentExpression = _currentExpression;
        newobj._dependencyLoop = false;
        newobj._initialExpression = _initialExpression;
        newobj._listeners = null;
        newobj._needsEvaluation = true;
        newobj._noTokenYet = _noTokenYet;
        newobj._origToken = _origToken;
        newobj._paramType = _paramType;
        newobj._parser = null;
        newobj._parseTree = null;
        newobj._scope = null;
        newobj._scopeVersion = -1;
        newobj._token = _token;
        return newobj;
    }

    /** Evaluate the current expression to a Token. If this parameter
     *  was last set directly with a Token do nothing. This method is also
     *  called after a Parameter is cloned.
     *  <p>
     *  This method is defined by The ParameterListener interface which
     *  all Parameters implement. When a Parameter changes, it calls
     *  this method on all ParameterListeners registered with it. This
     *  method also detects dependency loops between Parameters.
     *  <p>
     *  Some of this method is read-synchronized on the workspace.
     *  @exception IllegalArgumentException If the token resulting
     *   from evaluating the expression cannot be stored in this parameter.
     */
    public void evaluate() throws IllegalArgumentException {
        if (_currentExpression == null) {
	    return;
	}
	if (_dependencyLoop) {
            throw new IllegalArgumentException("Found dependency loop in " +
		    getFullName() +  ": " + _currentExpression);
        }
        _dependencyLoop = true;

        try {
	    workspace().getReadAccess();
	    ParameterEvent event = null;
            // if an expression was placed in this parameter but has not
	    // yet been evaluated, do it now
	    if (_needsEvaluation) {
		_needsEvaluation = false;
		if (_parser == null) {
		    _parser = new PtParser(this);
		}
		_parseTree = _parser.generateParseTree(
			_currentExpression, getScope());
                int id = ParameterEvent.SET_FROM_EXPRESSION;
                event = new ParameterEvent(id, this);
            } else {
                event = new ParameterEvent(ParameterEvent.UPDATED, this);
            }
	    if ( _parseTree == null){
		// ERROR: should not get here.
		throw new InternalErrorException("Parameter.evaluate():" +
			"reached a point which should not be reached.");
	    }
	    _token = _parseTree.evaluateParseTree();
            _castToken = null;
	    if (_noTokenYet) {
		// This is the first token stored in this parameter.
		_initialExpression = _currentExpression;
		_noTokenYet = false;
		setType(_token.getClass());
		// don't need to check type as first token in
	    } else {
		_checkType(_token.getClass());
	    }
            _notifyListeners(event);
        } finally {
	    _dependencyLoop = false;
	    workspace().doneReading();
	}
    }

    /** Get the Token contained by this Parameter. It may be null. 
     *  The token is not converted to the type of this Parameter. 
     *  The contained token is either the token placed directly into 
     *  the parameter, or the result of evaluating the current expression.
     *  @return The token contained by this parameter.
     */
    public ptolemy.data.Token getContainedToken() {
        try {
            if (_needsEvaluation) {
                evaluate();
            }
        } catch (IllegalArgumentException ex) {
            _token = null;
        }
        return _token;
    }

    /** Get the expression currently used by this Parameter. If the 
     *  Parameter was last set directly via a Token being placed in it, 
     *  the Parameter does not have an expression and null is returned.
     *  @return The expression contained by this parameter.
     */
    public String getExpression() {
        return _currentExpression;
    }
  
    /** Obtain a NamedList of the parameters that the value of this
     *  Parameter can depend on. The scope is limited to the parameters in the
     *  same NamedObj and those one level up in the hierarchy.
     *  It catches any exceptions thrown by NamedList as 1) the parameter must
     *  have a container with a NamedList of Attributes, and 2) if there is
     *  a clash in the names of the two scoping levels, the parameter from
     *  the top level is considered not to be visible in the scope of this
     *  Parameter. A parameter also cannot reference itself.
     *  This method is read-synchronized on the workspace.
     *  @return The parameters on which this parameter can depend.
     */
    public NamedList getScope() {
        if (_scopeVersion == workspace().getVersion()) {
            return _scope;
        }
        try {
            workspace().getReadAccess();
            _scope = new NamedList();
            NamedObj container = (NamedObj)getContainer();
            if (container != null) {
                NamedObj containerContainer =
                    ((NamedObj)container.getContainer());
                Enumeration level1 = container.getAttributes();
                Attribute p;
                while (level1.hasMoreElements() ) {
                    // Now copy the namedlist, omitting the current Parameter.
                    if ( (p = (Attribute)level1.nextElement()) != this ) {
                        try {
                            if ((p instanceof Parameter) && (p != this) ){
                                _scope.append(p);
                            }
                        } catch (NameDuplicationException ex) {
                            // since we're basically copying a namedlist,
                            // this exceptions should not occur
                        }
                    }
                }
                if (containerContainer != null) {
                    Enumeration level2 = containerContainer.getAttributes();
                    while (level2.hasMoreElements() ) {
                        p = (Attribute)level2.nextElement();
                        try {
                            if (p instanceof Parameter) {
                                _scope.append(p);
                            }
                        } catch (NameDuplicationException ex) {
                            // Name clash between the two levels of scope.
                            // The top level is hidden.
                        }
                    }
                }
            }
            _scopeVersion = workspace().getVersion();
            return _scope;
        } catch (IllegalActionException ex) {
            // since we're basically copying namedlists,
            // this exception should not occur
            throw new InternalErrorException(ex.getMessage());
        } finally {
            workspace().doneReading();
        }
    }

    /** Get the Token contained by this Parameter converted to the type of 
     *  the Parameter. It may be null. This means that if the parameter 
     *  is of type DoubleToken, and the Token currently contained by the 
     *  Parameter if of type IntToken, then the IntToken is converted to 
     *  a DoubleToken and returned.
     *  @return The token contained by this parameter converted to the 
     *   type of this Parameter.
     */
    public ptolemy.data.Token getToken() {
        try {
            if (_needsEvaluation) {
                evaluate();
            }
        } catch (IllegalArgumentException ex) {
            _token = null;
        }
        if (_token == null) {
            _castToken = null;
        } else if (_castToken == null) {
            // Need to convert the token contained by this parameter to
            // the type of the Parameter.
            if (_paramType == null) {
                _castToken = null;
            } else {
                try {
                    Object[] arg = new Object[1];
                    arg[0] = _token;
                    Object t = _convertMethod.invoke(null, arg);
                    _castToken = (ptolemy.data.Token)t;
                } catch (java.lang.reflect.InvocationTargetException e) {
                    throw new InternalErrorException("Convert method failed:" +
                            e.getTargetException().getMessage() +
                            e.getTargetException().getClass().getName());
                } catch (Exception e) {
                    // Cannot happen: types should have already been checked.
                    throw new InternalErrorException("Parameter.getToken: " +
                            "could not convert Token contained by this " +
                            "Parameter into the type of this Parameter." +
                            e.getMessage() + e.getClass().getName());
                }
            }
        }        
        return _castToken;
    }

    /** Get the type of this Parameter. It may be null.
     *  @return The type of this parameter.
     */
    public Class getType() {
        return _paramType;
    }

    /** A Parameter which the expression in this Parameter references
     *  has changed. Here we just call evaluate() to obtain the new
     *  Token to be stored in this Parameter.
     *  @param event The ParameterEvent containing the information
     *    about why the referenced Parameter changed.
     */
    public void parameterChanged(ParameterEvent event) {
         evaluate();
    }

    /** A Parameter which the expression stored in this Parameter
     *  has been removed. Check if the current expression is still
     *  valid by recreating and evaluating the parse tree.
     *  @param event The ParameterEvent containing information
     *    about the removed Parameter.
     */
    public void parameterRemoved(ParameterEvent event) {
        _rebuildDependencies();
        return;
    }

    /** Unregister a ParameterListener of this Parameter.
     *  @param oldListener The ParameterListener that is will no
     *  longer be notified when the value of this Parameter changes.
     */
    public void removeParameterListener(ParameterListener oldListener) {
        if (_listeners == null) {
            return;
        }
        // Search through LinkedList and remove this listener.
        // When we move to JDK1.2 this should be updated to use an arrayList
        LinkedList newList = new LinkedList();
        Enumeration oldList = _listeners.elements();
        while(oldList.hasMoreElements()) {
            ParameterListener nextList =
                     (ParameterListener)oldList.nextElement();
             if (oldListener != nextList) {
                 newList.insertFirst(nextList);
             } else {
                 //System.out.println("Remove dependency on " + getName());
             }
         }
         _listeners = newList;
         return;
     }

    /** Reset the current value of this parameter to the first seen
     *  token or expression. If the Parameter was initially given a
     *  Token, set the current Token to that Token. Otherwise evaluate
     *  the original expression given to the Parameter.
     *  @exception IllegalArgumentException If the Parameter cannot
     *   contain the original token (the parameters type must have been
     *   changed since then) or the token resulting from reevaluating
     *   the original expression.
     */
    public void reset() throws IllegalArgumentException {
        if (_noTokenYet) return;
        if (_origToken != null) {
            setToken(_origToken);
        } else  {
            //must have an _initialExpression
            setExpression(_initialExpression);
            evaluate();
        }
    }

    /** Specify the container NamedObj, adding this Parameter to the
     *  list of attributes in the container.  If the specified container
     *  is null, remove this Parameter from the list of attributes of the
     *  NamedObj and also notify all ParameterListeners which are registered
     *  with this Parameter that this Parameter has been removed.
     *  If the container already
     *  contains an parameter with the same name, then throw an exception
     *  and do not make any changes.  Similarly, if the container is
     *  not in the same workspace as this parameter, throw an exception.
     *  If this parameter is already contained by the NamedObj, do nothing.
     *  If the parameter already has a container, remove
     *  this attribute from its attribute list of the NamedObj first.
     *  Otherwise, remove it from the directory of the workspace, if it is
     *  there. This method is write-synchronized on the
     *  workspace and increments its version number.
     *  @param container The container to attach this attribute to.
     *  @exception IllegalActionException If this attribute is not of the
     *   expected class for the container, or it has no name,
     *   or the attribute and container are not in the same workspace, or
     *   the proposed container would result in recursive containment.
     *  @exception NameDuplicationException If the container already has
     *   an parameter with the name of this attribute.
     */
    public void setContainer(NamedObj container)
            throws IllegalActionException, NameDuplicationException {
        super.setContainer(container);
        if (container == null) {
            if (_listeners == null) {
                // No listeners to notify.
                return;
            }
            ParameterEvent event = new ParameterEvent(this);
            Enumeration list = _listeners.elements();
            while (list.hasMoreElements()) {
                ParameterListener next = (ParameterListener)list.nextElement();
                next.parameterRemoved(event);
            }
        }
    }

    /** Set the expression in this Parameter.
     *  If the string is null, the token contained by this parameter
     *  is set to null. If it is not null, the expression is stored
     *  to be evaluated at a later stage. To evaluate the expression
     *  now, invoke the method evaluate on this parameter.
     *  If the previous Token in the Parameter was the result of
     *  evaluating an expression, the dependencies registered with
     *  other Parameters for that expression are cleared.
     *  @param str The expression for this parameter.
     */
    public void setExpression(String str) {
        try {
            if (str == null) {
                setToken(null);
                return;
            }
        } catch (IllegalArgumentException ex) {
            // cannot happen.
        }
        if (_parseTree != null) {
            _clearDependencies(_parseTree);
            _parseTree = null;
        }
        _currentExpression = str;
        _needsEvaluation = true;
    }

    /** Put a new Token in this Parameter. This is the way to give the
     *  give the Parameter a new simple value.
     *  If the previous Token in the Parameter was the result of
     *  evaluating an expression, the dependencies registered with
     *  other Parameters for that expression are cleared.
     *  @param token The new Token to be stored in this Parameter.
     *  @exception IllegalArgumentException If the token cannot be placed
     *   in this parameter.
     */
    public void setToken(ptolemy.data.Token token)
            throws IllegalArgumentException {
        // Only change the Token stored in this Parameter if the type 
        // of the new Token is compatible with the type of this Parameter.
        ptolemy.data.Token prev = _token;
        _token = token;
        try {
            _checkType(_token.getClass());
        } catch (IllegalArgumentException e) {
            _token = prev;
            throw e;
        }

        // New Token is compatible.
        if ( (_token != null) && (_noTokenYet) ) {
            _origToken = _token;
            _noTokenYet = false;
            setType(_token.getClass());
        } 
        if (_parseTree != null) {
            _clearDependencies(_parseTree);
            _parseTree = null;
        }
        _castToken = null;
        _currentExpression = null;
        _needsEvaluation = false;
        
        int id = ParameterEvent.SET_FROM_TOKEN;
        _notifyListeners(new ParameterEvent(id, this));
    }

    /** Set the types of Tokens that this parameter can contain.
     *  It must be possible to losslessly convert the currently
     *  contained Token to the new type, or else an exception will
     *  be thrown. If so, the state of the parameter is unchanged.
     *  The types to which the Parameter may be set depend on both the 
     *  current type of the Parameter and the token currently contained
     *  by the Parameter. 
     *  @param newType The class object whose type represents the new type
     *   of this parameter. It must be an instance of type Token or a 
     *   derived class.
     *  @exception IllegalArgumentException If the new type
     *   is too restrictive for the currently contained token.
     */
    public void setType(Class newType) 
            throws IllegalArgumentException {
        Class oldType = _paramType;
        _paramType = newType;
        try {
            Class[] f = new Class[1];
            f[0] = Token.class;
            _convertMethod = _paramType.getClass().getMethod("convert", f);
            if (oldType == null) {
                return;
            } else{
                _checkType(_token.getClass());
            }
        } catch (IllegalArgumentException ex) {
            _paramType = oldType;
            throw new IllegalArgumentException("Cannot set the type of " +
                    "Parameter " + getName() + " to type: " + 
                    newType.getName() + ", when the currently " +
                    "contained Token is of type: " +  
                    _token.getClass().getName());
        } catch (NoSuchMethodException nsme) {
            throw new InternalErrorException("Parameter.setType: "
                    + "NoSuchMethodException: " + nsme.getMessage());
        }        
    }


    /** Get a string representation of the current parameter value.
     *  @return A String representing the class and the current token.
     */
    public String toString() {
        return super.toString() + " " + getToken().toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a description of this Parameter.
     *  @param detail The level of detail.
     *  @param indent The amount of indenting.
     *  @param bracket The number of surrounding brackets (0, 1, or 2).
     *  @return A String describing the Parameter.
     *  FIXME: needs to be finished, how/what is needed to
     *   describe a Parameter.
     */
    protected String _description(int detail, int indent, int bracket) {
        try {
            workspace().getReadAccess();
            String result = _getIndentPrefix(indent);
            if (bracket == 1 || bracket == 2) result += "{";
            result += toString();
            if (bracket == 2) result += "}";
            return result;
        } finally {
            workspace().doneReading();
        }
    }

    /*  Notify any ParameterListeners that have registered an
     *  interest/dependency in this parameter.
     */
    protected void _notifyListeners(ParameterEvent event) {
        if (_listeners == null) {
            // No listeners to notify.
            return;
        }
        Enumeration list = _listeners.elements();
        while (list.hasMoreElements()) {
            ((ParameterListener)list.nextElement()).parameterChanged(event);
        }
    }

    /** Rebuild the dependencies of this parameter on other parameters
     *  by recreating and evaluating the parse tree, and check if the
     *  current expression is still valid.
     */
    protected void _rebuildDependencies() {
        setExpression(_currentExpression);
        evaluate();
        return;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /*  Checks to see if the type of the new token is compatible with 
     *  the type of this Parameter. If the new Class object represents 
     *  a Token class that cannot be converted in a lossless manner 
     *  to the type of this Parameter, an exception is thrown.
     *  @param tryType The Class objet whose type must be compatible with the 
     *   type of this parameter.
     *  @exception IllegalArgumentException If the new type is 
     *   not compatible with the type of this parameter.
     */
    private void _checkType(Class tryType)
            throws IllegalArgumentException {
        if (tryType == null || _paramType == null) {
            return;
        }
        int typeInfo = TypeLattice.compare(_paramType, tryType);
        if ( (typeInfo == CPO.HIGHER) || (typeInfo == CPO.SAME) ) {
            return;
        }
        // Incompatible type!
        throw new IllegalArgumentException( "Cannot store a Token of type " +
                tryType.getName() + " in a Parameter restricted " +
                "to tokens of type " + _paramType.getName() + 
                " or lower");
    }

   /*  Clear the dependencies this Parameter has registered
    *  with other Parameters. If this is not done a phantom web
    *  of dependencies may exist which could lead to false
    *  dependency loops being detected. Normally this method is
    *  called with the root node of the parse tree and recursively
    *  calls itself to visit the whole tree.
    *  @param node The node in the tree below which all dependencies
    *   are cleared.
    */
    private void _clearDependencies(Node node) {
        int children = node.jjtGetNumChildren();
        if (children > 0) {
            for (int i = 0; i < children; i++) {
                _clearDependencies(node.jjtGetChild(i));
            }
            return;
        }
        if ( !(node instanceof ASTPtLeafNode) ) {
            throw new InternalErrorException("If a node has no children, " +
                   "it must be a leaf node.");
        }
        ASTPtLeafNode leaf = (ASTPtLeafNode)node;
        if (leaf._param != null) {
            leaf._param.removeParameterListener(this);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // The parameters this parameter may reference in an expression.
    // It is cached.
    protected NamedList _scope;
    protected long _scopeVersion = -1;


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////


    // Caches the cast value of the current token to the Parameter type.
    private Token _castToken = null;

    // Stores the object representing the convert method for the current 
    // Token that represents the type of this Parameter.
    private java.lang.reflect.Method _convertMethod = null;

    // Stores the string used to set this expression. It is null if
    // the parameter was set using a token.
    private String _currentExpression;

    // Used to check for dependency loops amongst parameters.
    private boolean _dependencyLoop = false;

    // Stores the string used to initialize the parameter. It is null if
    // the first token placed in the parameter was not parsed from a string.
    private String _initialExpression;

    // Each Parameter stores a linkedlist of the ParameterListeners it
    // needs to notify whenever it changes.
    private LinkedList _listeners;

    // Indicates if the parameter was last set with an expression and that
    // the expression has not yet been evaluated.
    private boolean _needsEvaluation = false;

    // Tests if the parameter has not yet contained a Token.
    private boolean _noTokenYet = true;

    // Stores the first Token placed in this parameter. It is null if the
    // first token contained by the parameter was parsed from an expression.
    private ptolemy.data.Token _origToken;

    // Stores the Class object which represents the type of this Parameter.
    private Class _paramType;

    // The parser used by this parameter to parse expressions.
    private PtParser _parser;

    // If the parameter was last set from an expression, this stores
    // the parse tree for that expression.
    private ASTPtRootNode _parseTree;

    // The token contained by this parameter.
    private ptolemy.data.Token _token;
}




