/* Make all references to ports point to port fields

 Copyright (c) 2001-2002 The Regents of the University of California.
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

import soot.*;
import soot.jimple.*;
import soot.jimple.toolkits.invoke.SiteInliner;
import soot.jimple.toolkits.invoke.StaticInliner;
import soot.jimple.toolkits.invoke.InvokeGraphBuilder;
import soot.jimple.toolkits.invoke.ClassHierarchyAnalysis;
import soot.jimple.toolkits.invoke.InvokeGraph;
import soot.jimple.toolkits.invoke.VariableTypeAnalysis;
import soot.jimple.toolkits.scalar.ConditionalBranchFolder;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.jimple.toolkits.scalar.CopyPropagator;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.jimple.toolkits.scalar.Evaluator;
import soot.toolkits.graph.*;
import soot.toolkits.scalar.*;
import soot.dava.*;
import soot.util.*;
import java.io.*;
import java.util.*;

import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.moml.*;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.data.*;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.Typeable;

import ptolemy.copernicus.kernel.SootUtilities;


//////////////////////////////////////////////////////////////////////////
//// FieldsForPortsTransformer
/**
A transformer that is responsible for replacing references to ports
Any calls to the getPort() method are replaced with a field reference to
the field of the appropriate class that points to the correct port.

@author Stephen Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class FieldsForPortsTransformer extends SceneTransformer {
    /** Construct a new transformer
     */
    private FieldsForPortsTransformer(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on
     *  the given model.  The model is assumed to already have been
     *  properly initialized so that resolved types and other static
     *  properties of the model can be inspected.
     */
    public static FieldsForPortsTransformer v(CompositeActor model) {
        return new FieldsForPortsTransformer(model);
    }

    public String getDefaultOptions() {
        return "";
    }

    public String getDeclaredOptions() {
        return super.getDeclaredOptions() + " targetPackage";
    }

    protected void internalTransform(String phaseName, Map options) {
        int localCount = 0;
        System.out.println("FieldsForPortsTransformer.internalTransform("
                + phaseName + ", " + options + ")");

        Map portToFieldMap = new HashMap();
        Map classToObjectMap = new HashMap();

        // This won't actually create any fields, but will pick up
        // the fields that already exist.
        _getPortFields(ModelTransformer.getModelClass(), _model,
                _model, portToFieldMap);
        classToObjectMap.put(ModelTransformer.getModelClass(), _model);

        // Loop over all the actor instance classes and get
        // fields for ports.
        for (Iterator i = _model.deepEntityList().iterator();
             i.hasNext();) {
            Entity entity = (Entity)i.next();
            String className =
                ActorTransformer.getInstanceClassName(entity, options);
            SootClass entityClass = Scene.v().loadClassAndSupport(className);
            _getPortFields(entityClass, entity, entity,
                    portToFieldMap);
            classToObjectMap.put(entityClass, entity);
        }

        // Loop over all the classes and replace getPort calls.
        for (Iterator i = _model.deepEntityList().iterator();
             i.hasNext();) {
            Entity entity = (Entity)i.next();
            String className =
                ActorTransformer.getInstanceClassName(entity, options);
            SootClass theClass = Scene.v().loadClassAndSupport(className);

            // Loop through all the methods in the class.
            for (Iterator methods = theClass.getMethods().iterator();
                 methods.hasNext();) {
                SootMethod method = (SootMethod)methods.next();

                JimpleBody body = (JimpleBody)method.retrieveActiveBody();

                CompleteUnitGraph unitGraph =
                    new CompleteUnitGraph(body);
                // This will help us figure out where locals are defined.
                SimpleLocalDefs localDefs =
                    new SimpleLocalDefs(unitGraph);

                for (Iterator units = body.getUnits().snapshotIterator();
                     units.hasNext();) {
                    Stmt unit = (Stmt)units.next();
                    if (!unit.containsInvokeExpr()) {
                        continue;
                    }
                    ValueBox box = (ValueBox)unit.getInvokeExprBox();
                    Value value = box.getValue();
                    if (value instanceof InstanceInvokeExpr) {
                        InstanceInvokeExpr r = (InstanceInvokeExpr)value;
                        // FIXME: string matching is probably not good enough.
                        if (r.getMethod().getName().equals("getPort")) {
                                // Inline calls to getPort(arg) when
                                // arg is a string that can be
                                // statically evaluated.
                            Value nameValue = r.getArg(0);
                            if (Evaluator.isValueConstantValued(nameValue)) {
                                StringConstant nameConstant =
                                    (StringConstant)
                                    Evaluator.getConstantValueOf(nameValue);
                                String name = nameConstant.value;
                                // perform type analysis to determine what the
                                // type of the base is.

                                Local baseLocal = (Local)r.getBase();
                                Value newFieldRef = _createPortField(
                                        baseLocal, name, unit, localDefs,
                                        classToObjectMap, portToFieldMap);
                                box.setValue(newFieldRef);
                            } else {
                                String string = "Port cannot be " +
                                    "statically determined";
                                throw new RuntimeException(string);
                            }
                        }
                    }
                }
            }
        }
    }

    // Given a local variable that refers to an entity, and the name
    // of an port in that object, return a new field ref that
    // refers to that port.  If no reference is found, then return null.
    private static Value _createPortField(Local baseLocal,
            String name, Unit unit, LocalDefs localDefs,
            Map classToObjectMap, Map portToFieldMap) {
        // FIXME: This is not enough.
        RefType type = (RefType)baseLocal.getType();
        Entity entity = (Entity)classToObjectMap.get(type.getSootClass());
        if (entity != null) {
            // Then we are dealing with a getPort call on one of the
            // classes we are generating.
            Port port = entity.getPort(name);
            SootField portField = (SootField)
                portToFieldMap.get(port);
            if (portField != null) {
                return Jimple.v().newInstanceFieldRef(
                        baseLocal, portField);
            } else {
                return NullConstant.v();
            }
        } else {
            // Walk back and get the definition of the field.
            DefinitionStmt definition =
                _getFieldDef(baseLocal, unit, localDefs);
            InstanceFieldRef fieldRef = (InstanceFieldRef)
                definition.getRightOp();
            SootField baseField = fieldRef.getField();
            System.out.println("baseField = " + baseField);
            SootField portField =
                baseField.getDeclaringClass().getFieldByName(
                        baseField.getName() + "_" + name);
            return Jimple.v().newInstanceFieldRef(
                    baseLocal, portField);
        }
    }


    /** Attempt to determine the constant value of the given local,
     *  which is assumed to have a variable type.  Walk backwards
     *  through all the possible places that the local may have been
     *  defined and try to symbolically evaluate the value of the
     *  variable. If the value can be determined, then return it,
     *  otherwise return null.
     */
    private static DefinitionStmt _getFieldDef(Local local,
            Unit location, LocalDefs localDefs) {
        List definitionList = localDefs.getDefsOfAt(local, location);
        if (definitionList.size() == 1) {
            DefinitionStmt stmt = (DefinitionStmt)definitionList.get(0);
            Value value = (Value)stmt.getRightOp();
            if (value instanceof CastExpr) {
                return _getFieldDef((Local)((CastExpr)value).getOp(),
                        stmt, localDefs);
            } else if (value instanceof FieldRef) {
                return stmt;
            } else {
                throw new RuntimeException("unknown value = " + value);
            }
        } else {
            System.out.println("more than one definition of = " + local);
            for (Iterator i = definitionList.iterator();
                 i.hasNext();) {
                System.out.println(i.next().toString());
            }
        }
        return null;
    }

    // Populate the given map according to the fields representing the
    // ports of the given object that are expected
    // to exist in the given class
    private void _getPortFields(SootClass theClass, Entity container,
            Entity object, Map portToFieldMap) {

        for (Iterator ports = object.portList().iterator();
             ports.hasNext();) {
            Port port = (Port)ports.next();

            String fieldName =
                StringUtilities.sanitizeName(port.getName(container));
            SootField field;
            if (!theClass.declaresFieldByName(fieldName)) {
                throw new RuntimeException("Class " + theClass
                        + " does not declare field "
                        + fieldName + " for port "
                        + port.getFullName());
            } else {
                // retrieve the existing field.
                field = theClass.getFieldByName(fieldName);
                // Make the field final and private.
                field.setModifiers((field.getModifiers() & Modifier.STATIC) |
                        Modifier.FINAL | Modifier.PRIVATE);
            }
            field.addTag(new ValueTag(port));
            portToFieldMap.put(port, field);
            // FIXME: call recursively
            // _getAttributeFields(theClass, container,
            //        attribute, attributeToFieldMap);
        }
    }

    private CompositeActor _model;
}














