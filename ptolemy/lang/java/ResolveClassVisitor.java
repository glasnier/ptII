/* 
Create declarations for fields, constructors, and methods, and add them to
their enclosing class's environment.

Code and comments adopted from st-class.cc from the Titanium project.

Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)

*/

package ptolemy.lang.java;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.lang.*;
import ptolemy.lang.java.nodetypes.*;

/** 
 *  Create declarations for fields, constructors, and methods, and add them to
 *  their enclosing class's environment.
 * 
 *  Code and comments adopted from st-class.cc from the Titanium project.
 *
 *  @author Jeff Tsay
 */
public class ResolveClassVisitor extends ResolveVisitorBase 
       implements JavaStaticSemanticConstants  {
    public ResolveClassVisitor() {
        super();
    }

    public Object visitCompileUnitNode(CompileUnitNode node, LinkedList args) {
        ApplicationUtility.trace("resolveClass for " +
         node.getProperty(IDENT_KEY));

        _pkgDecl = (PackageDecl) node.getDefinedProperty(PACKAGE_KEY);

        LinkedList childArgs = new LinkedList();
        childArgs.add(NullValue.instance); // enclosing class decl
        childArgs.add(NullValue.instance); // enclosing class environ

        TNLManip.traverseList(this, node, childArgs, node.getDefTypes());
        return null;
    }

    public Object visitClassDeclNode(ClassDeclNode node, LinkedList args) {

        ClassDecl me = (ClassDecl) JavaDecl.getDecl((NamedNode) node);

        TreeNode superClass = node.getSuperClass();

        ClassDecl superDecl =
         (superClass == AbsentTreeNode.instance) ?
         StaticResolution.OBJECT_DECL :
         (ClassDecl) JavaDecl.getDecl((NamedNode) superClass);

        if (superDecl.category != CG_CLASS) {
           ApplicationUtility.error("class " + node.getName().getIdent() +
            " cannot extend interface " + superDecl.getName());
        }

        node.setSuperClass(superDecl.getDefType());
        me.setSuperClass(superDecl);

        // initialize the implements list.
        LinkedList declInterfaceList = new LinkedList();

        Iterator interfaceItr = node.getInterfaces().iterator();

        while (interfaceItr.hasNext()) {
            ClassDecl intf = (ClassDecl) JavaDecl.getDecl(
             (NamedNode) interfaceItr.next());

            if (intf.category != CG_INTERFACE) {
               ApplicationUtility.error("class " + node.getName().getIdent() +
                " cannot implement class " + intf.getName());
            }
            declInterfaceList.addLast(intf);
        }

        me.setInterfaces(declInterfaceList);

        // add this declaration to outer class's environment, if applicable
        _addUserTypeToEnclosingClassEnviron(args.get(1), me);

        Environ myEnviron = me.getEnviron();

        // have members add themselves to this class's environment
        LinkedList childArgs = new LinkedList();
        childArgs.addLast(me);
        childArgs.addLast(myEnviron);

        TNLManip.traverseList(this, node, childArgs, node.getMembers());

        return null;
    }

    public Object visitFieldDeclNode(FieldDeclNode node, LinkedList args) {
        int modifiers = node.getModifiers();

        String nameString = node.getName().getIdent();

        // Leftover from Titanium. Why??
        // dtype()->resolveClass(package, cclass, fileEnv);

        Environ encEnviron = (Environ) args.get(1);

        Decl d = encEnviron.lookupProper(nameString, CG_FIELD);

        if (d != null) {
           ApplicationUtility.error("redeclaration of " + d.getName());
        }

        d = new FieldDecl(nameString, node.getDefType(), modifiers,
            node, (ClassDecl) args.get(0));

        encEnviron.add(d);

        node.getName().setProperty(DECL_KEY, d);

        // now that we have anonymous classes, we should visit the init
        // expression
        LinkedList childArgs = new LinkedList();
        childArgs.addLast(NullTypeNode.instance); // enclosing class decl
        childArgs.addLast(NullTypeNode.instance); // enclosing class environ
        node.getInitExpr().accept(this, childArgs);

        return null;
    }

    public Object visitInterfaceDeclNode(InterfaceDeclNode node, LinkedList args) {
        ClassDecl me = (ClassDecl) JavaDecl.getDecl((NamedNode) node);

        // initialize the implements list.
        // FIXME : what about the interfaces implemented by the interfaces in the list?

        LinkedList declInterfaceList = new LinkedList();

        Iterator interfaceItr = node.getInterfaces().iterator();

        while (interfaceItr.hasNext()) {
            ClassDecl intf = (ClassDecl) JavaDecl.getDecl(
             (NamedNode) interfaceItr.next());

            if (intf.category != CG_INTERFACE) {
               ApplicationUtility.error("class " + node.getName().getIdent() +
                " cannot implement class " + intf.getName());
            }
            declInterfaceList.addLast(intf);
        }

        me.setInterfaces(declInterfaceList);

        // add this declaration to outer class's environment, if applicable
        _addUserTypeToEnclosingClassEnviron(args.get(1), me);

        Environ myEnviron = me.getEnviron();

        // have members add themselves to this class's environment
        LinkedList childArgs = new LinkedList();
        childArgs.addLast(me);
        childArgs.addLast(myEnviron);

        TNLManip.traverseList(this, node, childArgs, node.getMembers());

        // Set implied modifiers of interface members
        Iterator declItr = myEnviron.allProperDecls();

        while (declItr.hasNext()) {
            JavaDecl decl = (JavaDecl) declItr.next();

            int modifiers = decl.getModifiers();

            switch (decl.category) {

            case CG_METHOD:
            modifiers |= (PUBLIC_MOD | ABSTRACT_MOD);
            break;

            case CG_FIELD:
            case CG_CLASS:
            modifiers |=
             (PUBLIC_MOD | FINAL_MOD | STATIC_MOD);
            break;

            case CG_INTERFACE:
            modifiers |= STATIC_MOD;
            break;

            }
            decl.setModifiers(modifiers);
        }

        return null;
    }

    public Object visitParameterNode(ParameterNode node, LinkedList args) {
        return null;
    }

    public Object visitConstructorDeclNode(ConstructorDeclNode node, LinkedList args) {

        ClassDecl classDecl = (ClassDecl) args.get(0);

        int modifiers = node.getModifiers();

        // Check that this constructor is legal

        Environ classEnv = (Environ) args.get(1);

        NameNode name = node.getName();
        String constructorName = name.getIdent();

        Iterator constructorItr = classEnv.lookupFirstProper(constructorName,
         CG_CONSTRUCTOR);

        MethodDecl d = new MethodDecl(constructorName, CG_CONSTRUCTOR,
         NullTypeNode.instance, modifiers, node, classDecl,
         _makeTypeList(node.getParams()), (Collection) node.getThrowsList());

        while (constructorItr.hasNext()) {

           MethodDecl dd = (MethodDecl) constructorItr.next();

           if (dd.conflictsWith(d)) {
              ApplicationUtility.error("illegal overloading of " +
               constructorName);
           }
        }

        classEnv.add(d);

        name.setProperty(DECL_KEY, d);

        // now that we have anonymous classes, we should visit the body
        TreeNode block = node.getBody();

        if (block != AbsentTreeNode.instance) {

           LinkedList childArgs = new LinkedList();
           childArgs.addLast(NullTypeNode.instance); // enclosing class decl
           childArgs.addLast(NullTypeNode.instance); // enclosing class environ
           block.accept(this, childArgs);
        }

        return null;
    }

    public Object visitMethodDeclNode(MethodDeclNode node, LinkedList args) {

        ClassDecl classDecl = (ClassDecl) args.get(0);

        int modifiers = node.getModifiers();

        if (classDecl.category == CG_CLASS) {
        
           // private methods or methods in private or final classes are final
           if ((modifiers & PRIVATE_MOD) != 0) {              
              modifiers |= FINAL_MOD;
           }

           int classMod = classDecl.getModifiers();

           if ((classMod & (PRIVATE_MOD | FINAL_MOD)) != 0) {
              modifiers |= FINAL_MOD;
           }
           
           node.setModifiers(modifiers);
        }
        
        if (node.getBody() == AbsentTreeNode.instance) {
           if ((modifiers & ABSTRACT_MOD) != 0) {
              if ((modifiers & 
                   (PRIVATE_MOD | STATIC_MOD | FINAL_MOD | SYNCHRONIZED_MOD |
                    NATIVE_MOD)) != 0) {
                 ApplicationUtility.error("can't use private, static, final, " +
                  "synchronized, or native with abstract");
              }
           } else if ((modifiers & NATIVE_MOD) == 0) {
              ApplicationUtility.error("abstract or native required on " +
               " methods without a body");
           }
        } else if ((modifiers & (ABSTRACT_MOD | NATIVE_MOD)) != 0) {
           ApplicationUtility.error("an abstract or native method " +
            "cannot have a body");
        }

        // Check that this method is legal

        Environ classEnv = (Environ) args.get(1);

        NameNode name = node.getName();
        String methodName = name.getIdent();

        Iterator methodItr = classEnv.lookupFirstProper(methodName, CG_METHOD);

        MethodDecl d = new MethodDecl(methodName, CG_METHOD,
         node.getReturnType(), modifiers, node, classDecl,
         _makeTypeList(node.getParams()), (Collection) node.getThrowsList());

        while (methodItr.hasNext()) {

           MethodDecl dd = (MethodDecl) methodItr.next();

           if (dd.conflictsWith(d)) {
              ApplicationUtility.error("illegal overloading of " + methodName);
           }
        }

        classEnv.add(d);

        name.setProperty(DECL_KEY, d);

        // now that we have anonymous classes, we should visit the body
        TreeNode block = node.getBody();

        if (block != AbsentTreeNode.instance) {
           LinkedList childArgs = new LinkedList();
           childArgs.addLast(NullTypeNode.instance); // enclosing class decl
           childArgs.addLast(NullTypeNode.instance); // enclosing class environ
           block.accept(this, childArgs);
        }

        return null;
    }

    public Object visitAllocateAnonymousClassNode(AllocateAnonymousClassNode node, LinkedList args) {

        node.getEnclosingInstance().accept(this, args);
         
        ClassDecl me = (ClassDecl) node.getDefinedProperty(DECL_KEY);
               
        TypeNameNode superType = node.getSuperType();
        
        ClassDecl sdecl = (ClassDecl) JavaDecl.getDecl((NamedNode) superType);
        
        ClassDecl superClass = null;
        ClassDecl implIFace = null;
        
        if (sdecl.category == CG_CLASS) {
           superClass = sdecl;
        } else if (sdecl.category == CG_INTERFACE) {
           superClass = StaticResolution.OBJECT_DECL;
           implIFace = sdecl;
        }

        node.setProperty(SUPERCLASS_KEY, superClass);     
        node.setProperty(INTERFACE_KEY, implIFace);
                         
        me.setSuperClass(superClass);
        
        if (implIFace != null) {        
           me.setInterfaces(TNLManip.cons(implIFace));
        }

        Environ myEnviron = me.getEnviron();

        // have members add themselves to this class's environment
        LinkedList childArgs = new LinkedList();
        childArgs.addLast(me);
        childArgs.addLast(myEnviron);

        TNLManip.traverseList(this, node, childArgs, node.getMembers());

        return null;        
    }

    /** The default visit method. Visits all child nodes with no enclosing
     *  class declaration.
     */
    protected Object _defaultVisit(TreeNode node, LinkedList args) {
        LinkedList childArgs = new LinkedList();
        childArgs.add(NullValue.instance); // no enclosing class decl
        childArgs.add(NullValue.instance); // no enclosing class environ
        TNLManip.traverseList(this, node, args, node.children());
        return null;
    }

    protected void _addUserTypeToEnclosingClassEnviron(Object encClassEnvironObject,
     Decl decl) {
        if (encClassEnvironObject != NullValue.instance) {
           // this is an inner class, add to outer class's environment
           Environ encClassEnviron = (Environ) encClassEnvironObject;
           encClassEnviron.add(decl);
        }
    }

    /** Given a list of ParameterNodes, return a new list of TypeNodes
     *  corresponding to the type of each parameter.
     */
    protected static List _makeTypeList(List paramList) {
        LinkedList retval = new LinkedList();

        Iterator paramItr = paramList.iterator();

        while (paramItr.hasNext()) {
           ParameterNode param = (ParameterNode) paramItr.next();

           TypeNode type = param.getDefType();

           retval.addLast(type);
        }

        return retval;
    }

    /** The package this compile unit is in. */
    protected PackageDecl _pkgDecl = null;
}
