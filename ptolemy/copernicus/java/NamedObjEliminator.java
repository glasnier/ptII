/* Eliminate all references to named objects

 Copyright (c) 2001-2003 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.copernicus.java;

import ptolemy.actor.CompositeActor;
import ptolemy.copernicus.kernel.PtolemyUtilities;
import ptolemy.copernicus.kernel.SootUtilities;

import soot.FastHierarchy;
import soot.Hierarchy;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.NullConstant;
import soot.jimple.ParameterRef;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


//////////////////////////////////////////////////////////////////////////
//// NamedObjEliminator
/**

@author Stephen Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class NamedObjEliminator extends SceneTransformer {
    /** Construct a new transformer
     */
    private NamedObjEliminator(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on
     *  the given model.  The model is assumed to already have been
     *  properly initialized so that resolved types and other static
     *  properties of the model can be inspected.
     */
    public static NamedObjEliminator v(CompositeActor model) {
        return new NamedObjEliminator(model);
    }

    public String getDefaultOptions() {
        return "";
    }

    public String getDeclaredOptions() {
        return super.getDeclaredOptions() + " targetPackage";
    }

    protected void internalTransform(String phaseName, Map options) {
        int localCount = 0;
        System.out.println("NamedObjEliminator.internalTransform("
                + phaseName + ", " + options + ")");

        // First remove most method invocations that are not
        // specialInvokes.
        for (Iterator i = Scene.v().getApplicationClasses().iterator();
             i.hasNext();) {

            SootClass theClass = (SootClass) i.next();
            // Loop through all the methods in the class.
            for (Iterator methods = theClass.getMethods().iterator();
                 methods.hasNext();) {
                SootMethod method = (SootMethod)methods.next();

                // System.out.println("method = " + method);
                JimpleBody body = (JimpleBody)method.retrieveActiveBody();

                for (Iterator units = body.getUnits().snapshotIterator();
                     units.hasNext();) {
                    Stmt unit = (Stmt)units.next();
                    if (unit.containsFieldRef()) {
                        ValueBox box = unit.getFieldRefBox();
                        Value value = box.getValue();
                        if (value instanceof InstanceFieldRef) {
                            InstanceFieldRef fieldRef = (InstanceFieldRef)value;
                            SootField field = fieldRef.getField();
                            // Turn off debugging..
                            if (field.getSubSignature().equals(
                                    PtolemyUtilities.debuggingField.getSubSignature())) {
                                if (unit instanceof AssignStmt) {
                                    body.getUnits().insertBefore(
                                            Jimple.v().newAssignStmt(
                                                    ((AssignStmt)unit).getLeftOp(),
                                                    IntConstant.v(0)),
                                            unit);
                                }
                                body.getUnits().remove(unit);
                            }
                        }
                    } else if (unit.containsInvokeExpr()) {
                        ValueBox box = unit.getInvokeExprBox();
                        Value value = box.getValue();

                        if ((value instanceof InvokeExpr) &&
                                !(value instanceof SpecialInvokeExpr)) {
                            // remove attachText
                            InvokeExpr expr = (InvokeExpr)value;
                            if (expr.getMethod().getSubSignature().equals(
                                    PtolemyUtilities.attachTextMethod.getSubSignature())) {
                                body.getUnits().remove(unit);
                            } else if (expr.getMethod().getSubSignature().equals(
                                    PtolemyUtilities.setNameMethod.getSubSignature())) {
                                body.getUnits().remove(unit);
                            } else if (expr.getMethod().getName().equals("_debug")) {
                                body.getUnits().remove(unit);
                            }
                            // Inline namedObj methods on the
                            // attribute.
                            // FIXME: This should do the
                            // whole traceback business to ensure that
                            // we are calling the methods on the
                            // toplevel object.  This assumes we've
                            // already removed other namedobj methods
                            // on the object objects already.  See
                            // InlineParameterTransformer and
                            // InlinePortTransformer
                            if (expr.getMethod().getSubSignature().equals(
                                    PtolemyUtilities.getFullNameMethod.getSubSignature())) {
                                if (unit instanceof AssignStmt) {
                                    body.getUnits().insertBefore(
                                            Jimple.v().newAssignStmt(
                                                    ((AssignStmt)unit).getLeftOp(),
                                                    StringConstant.v(
                                                            _model.getFullName())),
                                            unit);
                                }
                                body.getUnits().remove(unit);
                            } else if (expr.getMethod().getSubSignature().equals(
                                    PtolemyUtilities.getNameMethod.getSubSignature())) {
                                if (unit instanceof AssignStmt) {
                                    body.getUnits().insertBefore(
                                            Jimple.v().newAssignStmt(
                                                    ((AssignStmt)unit).getLeftOp(),
                                                    StringConstant.v(
                                                            _model.getName())),
                                            unit);
                                }
                                body.getUnits().remove(unit);
                            } else if (expr.getMethod().getSubSignature().equals(
                                    PtolemyUtilities.findEffigyMethod.getSubSignature())) {
                                if (unit instanceof AssignStmt) {
                                    body.getUnits().insertBefore(
                                            Jimple.v().newAssignStmt(
                                                    ((AssignStmt)unit).getLeftOp(),
                                                    NullConstant.v()),
                                            unit);
                                }
                                body.getUnits().remove(unit);
                            }
                        }
                    }
                }
            }
        }


        List modifiedConstructorClassList = new LinkedList();

        // Loop over all the classes
        for (Iterator i = Scene.v().getApplicationClasses().iterator();
             i.hasNext();) {

            SootClass theClass = (SootClass) i.next();
            if (SootUtilities.derivesFrom(theClass,
                    PtolemyUtilities.actorClass) ||
                    SootUtilities.derivesFrom(theClass,
                            PtolemyUtilities.compositeActorClass)) {
                System.out.println("changing superclass for " + theClass);
                theClass.setSuperclass(PtolemyUtilities.objectClass);
                // Fix the constructor for the actor to take no
                // arguments.

                // FIXME: Here we assume that there is just one
                // constructor.. This will throw an exception if there
                // is more than one, in which case we need to improve
                // this code.
                SootMethod method = null;
                try {
                    method = theClass.getMethodByName("<init>");
                } catch (RuntimeException ex) {
                    System.out.println("Could not get method <init> by name "
                            + "from class " + theClass );
                    throw ex;
                }
                method.setParameterTypes(Collections.EMPTY_LIST);
                modifiedConstructorClassList.add(theClass);

                // Dance so that indexes in the Scene are properly updated.
                theClass.removeMethod(method);
                theClass.addMethod(method);

                // System.out.println("method = " + method);
                JimpleBody body = (JimpleBody)method.retrieveActiveBody();

                for (Iterator units = body.getUnits().snapshotIterator();
                     units.hasNext();) {
                    Stmt unit = (Stmt)units.next();

                    if (unit.containsInvokeExpr()) {
                        ValueBox box = unit.getInvokeExprBox();
                        Value value = box.getValue();
                        if (value instanceof SpecialInvokeExpr) {
                            // Fix super.<init> calls. constructor...
                            SpecialInvokeExpr expr = (SpecialInvokeExpr)value;
                            if (expr.getBase().equals(body.getThisLocal()) &&
                                    expr.getMethod().getName().equals("<init>")) {
                                System.out.println("replacing constructor = "
                                        + unit + " in method " + method);

                                // Replace with zero arg object constructor.
                                box.setValue(Jimple.v().newSpecialInvokeExpr(
                                        (Local)expr.getBase(),
                                        PtolemyUtilities.objectConstructor,
                                        Collections.EMPTY_LIST));
                                //          body.getUnits().remove(unit);
                            }
                        }
                    } else if (unit instanceof IdentityStmt) {
                        IdentityStmt identityStmt = (IdentityStmt)unit;
                        Value value = identityStmt.getRightOp();
                        if (value instanceof ParameterRef) {
                            System.out.println("value = " + value);
                            // Parameter values are null.  Note that
                            // we need to make sure that the
                            // assignment to null happens after all
                            // the identity statements, otherwise the
                            // super constructor will be implicitly
                            // called.
                            body.getUnits().remove(identityStmt);
                            body.getUnits().insertBefore(
                                    Jimple.v().newAssignStmt(
                                            identityStmt.getLeftOp(),
                                            NullConstant.v()),
                                    body.getFirstNonIdentityStmt());
                        }//  else if (value instanceof ThisRef) {
                        //                             // Fix the type of thisRefs.
                        //                             ValueBox box = identityStmt.getRightOpBox();
                        //                             box.setValue(
                        //                                     Jimple.v().newThisRef(
                        //                                             RefType.v(PtolemyUtilities.objectClass)));
                        //                         }
                    }
                }

                // Remove all the interfaces that it implements??
                //                theClass.getInterfaces().clear();
            }
        }

        // Reset the hierarchy, since we've changed superclasses and such.
        Scene.v().setActiveHierarchy(new Hierarchy());
        Scene.v().setActiveFastHierarchy(new FastHierarchy());

        // Fix the specialInvokes.
        for (Iterator i = Scene.v().getApplicationClasses().iterator();
             i.hasNext();) {

            SootClass theClass = (SootClass) i.next();
            // Loop through all the methods in the class.
            for (Iterator methods = theClass.getMethods().iterator();
                 methods.hasNext();) {
                SootMethod method = (SootMethod)methods.next();

                // System.out.println("method = " + method);
                JimpleBody body = (JimpleBody)method.retrieveActiveBody();

                for (Iterator units = body.getUnits().snapshotIterator();
                     units.hasNext();) {
                    Stmt unit = (Stmt)units.next();
                    if (unit.containsInvokeExpr()) {
                        ValueBox box = unit.getInvokeExprBox();
                        Value value = box.getValue();

                        if (value instanceof SpecialInvokeExpr) {
                            // If we're constructing one of our actor classes,
                            // then switch to the object constructor.
                            SpecialInvokeExpr expr = (SpecialInvokeExpr)value;
                            SootClass declaringClass =
                                expr.getMethod().getDeclaringClass();
                            if (expr.getMethod().getName().equals("<init>") &&
                                    modifiedConstructorClassList.contains(
                                            declaringClass)) {
                                System.out.println(
                                        "replacing constructor invocation = "
                                        + unit + " in method " + method);

                                // Replace with zero arg object constructor.
                                box.setValue(Jimple.v().newSpecialInvokeExpr(
                                        (Local)expr.getBase(),
                                        declaringClass.getMethod("void <init>()"),
                                        Collections.EMPTY_LIST));
                            }
                        }
                    }
                }
            }
        }

        for (Iterator i = Scene.v().getApplicationClasses().iterator();
             i.hasNext();) {

            SootClass theClass = (SootClass) i.next();
            // Loop through all the methods in the class.
            for (Iterator methods = theClass.getMethods().iterator();
                 methods.hasNext();) {
                SootMethod method = (SootMethod)methods.next();

                // System.out.println("method = " + method);
                JimpleBody body = (JimpleBody)method.retrieveActiveBody();

                for (Iterator units = body.getUnits().snapshotIterator();
                     units.hasNext();) {
                    Stmt unit = (Stmt)units.next();

                    // If any box is removable, then remove the statement.
                    for (Iterator boxes = unit.getUseAndDefBoxes().iterator();
                         boxes.hasNext();) {
                        ValueBox box = (ValueBox)boxes.next();

                        Value value = box.getValue();
                        Type type = value.getType();

                        if (_isRemovableType(type)) {
                            //System.out.println("Unit with removable type: " + unit);
                            body.getUnits().remove(unit);
                            break;
                        }
                    }
                    // If any locals are removable, then remove them.
                    for (Iterator locals = body.getLocals().snapshotIterator();
                         locals.hasNext();) {
                        Local local = (Local)locals.next();
                        Type type = local.getType();
                        if (_isRemovableType(type)) {
                            body.getLocals().remove(local);
                        }
                    }
                }
                // If any fields are removable, then remove them.
                for (Iterator fields = theClass.getFields().snapshotIterator();
                     fields.hasNext();) {
                    SootField field = (SootField)fields.next();
                    Type type = field.getType();
                    if (_isRemovableType(type)) {
                        theClass.getFields().remove(field);
                    }
                }
            }
        }/*
          */
    }

    // Return true if the type is one that should not appear in generated
    // code.  This includes Attribute, Settable, Relation, Port, and their
    // subclasses.
    private static boolean _isRemovableType(Type type) {
        if (type instanceof RefType) {
            RefType refType = (RefType)type;
            SootClass refClass = refType.getSootClass();
            if (SootUtilities.derivesFrom(refClass,
                    PtolemyUtilities.attributeClass) ||
                    SootUtilities.derivesFrom(refClass,
                            PtolemyUtilities.managerClass) ||
                    SootUtilities.derivesFrom(refClass,
                            PtolemyUtilities.settableClass) ||
                    SootUtilities.derivesFrom(refClass,
                            PtolemyUtilities.relationClass) ||
                    SootUtilities.derivesFrom(refClass,
                            PtolemyUtilities.portClass) ||
                    SootUtilities.derivesFrom(refClass,
                            PtolemyUtilities.entityClass)) {
                return true;
            }
        }
        return false;
    }

    private CompositeActor _model;
}














