/* Types of nodes in an AST.

Copyright (c) 2005 The Regents of the University of California.
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

package ptolemy.backtrack.ast;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;

//////////////////////////////////////////////////////////////////////////
//// Type
/**
   During AST analysis, a type is assigned to each expression or
   sub-expression (which can be as simple as reference to a local variable)
   in a Java program. This class represents the type objects to be assigned
   to those expressions.
   <p>
   This class represents primitive Java types (<tt>boolean</tt>, <tt>byte</tt>, 
   <tt>char</tt>, <tt>double</tt>, <tt>float</tt>, <tt>int</tt>, <tt>long</tt>, 
   and <tt>short</tt>) as well as object types (including arrays). It
   treats <tt>null</tt> and </tt>void</tt> (the "return value" of a
   <tt>void</tt> method) as </tt>null</tt> type, which is also considered as
   primitive.
   <p>
   Manipulation can also be done on those types by means of the given
   operations.
   <p>
   This class cannot be directed instantiated with "<tt>new</tt>". Users should
   use {@link #createType(String)} to create a type object with a name. If the
   name refers to a class, it must be a full name (including the package).
   <p>
   Objects of this class can also be associated with AST nodes during AST
   analysis. When the analyzer resolves the type of a node representing an
   expression or sub-expression in the Java program, it creates a type and
   associates it with that node with {@link #setType(ASTNode, Type)}. This
   information can be extracted with {@link #getType(ASTNode)}.
 
   @author Thomas Feng
   @version $Id$
   @since Ptolemy II 5.1
   @Pt.ProposedRating Red (tfeng)
   @Pt.AcceptedRating Red (tfeng)
*/
public class Type {

    ///////////////////////////////////////////////////////////////////
    ////                       public methods                      ////

    /** Add one dimension to this type object, and return the new type object.
     *  If this type object is primitive, the return type is a primitive array
     *  as an object type. If this type object is <tt>null</tt> type, the return
     *  type is also <tt>null</tt> type.
     *
     *  @return The new type with one dimension added.
     *  @see #dimensions()
     *  @see #removeOneDimension()
     */
    public Type addOneDimension() {
        if (isPrimitive())
            if (_primitiveNum == NULL_NUM)
                return this;
            else
                return createType(_fullName + "[]");
        else
            if (isArray())
                return createType("[" + _fullName);
            else
                return createType(_fullName + "[]");
    }

    /** Compute the compatibility rating between this type and another formal
     *  type. If the result is larger than or equal to 0, the two types are
     *  compatible. An object of this type can be assigned to the given formal
     *  type only when the two types are compatible.
     *  <p>
     *  The compatibility rating is an integer number. When it is -1, the two
     *  types are not compatible. When it is 0, the two types are the same.
     *  When it is a natural number, it denotes how compatible the two types
     *  are. The smaller this natural number is, the more compatible this
     *  type is with the given formal type.
     *
     *  @param formalType The formal type to be evaluated.
     *  @param loader The {@link ClassLoader} object to be used when the
     *   classes of types are resolved. This functioon uses this loader to
     *   resolve both types if they are classes, and also their superclasses
     *   and the interfaces that they implement, if necessary.
     *  @return The compatibility rating as an integer larger than or equal to
     *   -1.
     *  @exception ClassNotFoundException If a class cannot be loaded.
     */
    public int compatibility(Type formalType, ClassLoader loader)
            throws ClassNotFoundException {
        if (equals(formalType))
            return 0;

        // Null type is always compatible with object types.
        if (_primitiveNum == NULL_NUM && !formalType.isPrimitive())
            return 0;

        // Check primitive types.
        if (isPrimitive() != formalType.isPrimitive())
            return -1;

        if (isPrimitive())
            if (formalType.isPrimitive())
                if (_primitiveNum == CHAR_NUM)
                    if (formalType._primitiveNum == INT_NUM)
                        return 1;
                    else if (formalType._primitiveNum == LONG_NUM)
                        return 2;
                    else if (formalType._primitiveNum == FLOAT_NUM)
                        return 3;
                    else if (formalType._primitiveNum == DOUBLE_NUM)
                        return 4;
                    else
                        return -1;
                else if (_primitiveNum == INT_NUM)
                    if (formalType._primitiveNum == LONG_NUM)
                        return 1;
                    else if (formalType._primitiveNum == FLOAT_NUM)
                        return 2;
                    else if (formalType._primitiveNum == DOUBLE_NUM)
                        return 3;
                    // We make the type checking less strict then Java to
                    // allow for declarations like "byte b = 1;".
                    else if (formalType._primitiveNum == BYTE_NUM)
                        return 4;   // The same as short.
                    else if (formalType._primitiveNum == SHORT_NUM)
                        return 4;   // The same as byte.
                    else
                        return -1;
                else if (_primitiveNum == LONG_NUM)
                    if (formalType._primitiveNum == DOUBLE_NUM)
                        return 1;
                    else
                        return -1;
                else if (_primitiveNum == SHORT_NUM)
                    if (formalType._primitiveNum == INT_NUM)
                        return 1;
                    else if (formalType._primitiveNum == LONG_NUM)
                        return 2;
                    else if (formalType._primitiveNum == FLOAT_NUM)
                        return 3;
                    else if (formalType._primitiveNum == DOUBLE_NUM)
                        return 4;
                    else
                        return -1;
                else if (_primitiveNum == BYTE_NUM)
                    if (formalType._primitiveNum == SHORT_NUM)
                        return 1;
                    else if (formalType._primitiveNum == INT_NUM)
                        return 2;
                    else if (formalType._primitiveNum == LONG_NUM)
                        return 3;
                    else if (formalType._primitiveNum == FLOAT_NUM)
                        return 4;
                    else if (formalType._primitiveNum == DOUBLE_NUM)
                        return 5;
                    else
                        return -1;
                else if (_fullName.equals("float"))
                    if (formalType._fullName.equals("double"))
                        return 1;
                    else
                        return -1;
                else
                    return -1;
            else
                return -1;
        else {    // Not primitive types.
            // Check number of dimensions.
            Type selfType = this;
            while (selfType.isArray() && formalType.isArray()) {
                selfType = selfType.removeOneDimension();
                formalType = formalType.removeOneDimension();
            }

            Class class1 = selfType.toClass(loader);
            Class class2 = formalType.toClass(loader);
            int i = 0;
            while (class1 != null) {
                List workList = new LinkedList();
                Set handledSet = new HashSet();
                workList.add(class1);
                while (!workList.isEmpty()) {
                    Class c = (Class)workList.remove(0);
                    if (c.getName().equals(class2.getName()))
                        return i;
                    handledSet.add(c);
                    Class[] interfaces = c.getInterfaces();
                    for (int k = 0; k < interfaces.length; k++)
                        if (!handledSet.contains(interfaces[k]))
                            workList.add(interfaces[k]);
                }
                i++;
                class1 = class1.getSuperclass();
            }
            if (class2.getName().equals("java.lang.Object"))
                return i;
            else
                return -1;
        }
    }

    /** Create a type with a full name of a primitive type or a class. Users
     *  cannot directly create a type object with <tt>new</tt>. Instead, they
     *  should always use this function.
     *  <p>
     *  They full name of the type given can be any of the primitive types as
     *  well as <tt>null</tt> or <tt>void</tt>. When it is the name of an array
     *  type, both source representation (as "<tt>char[]</tt>") or run-time
     *  representation (as "<tt>[C<tt>") are accepted.
     *
     *  @param fullName The full name of the type.
     *  @return The type object with the given name. No error is returned when
     *   the name is incorrect.
     */
    public static Type createType(String fullName) {
        fullName = toArrayType(fullName);
        if (_typeObjects.containsKey(fullName))
            return (Type)_typeObjects.get(fullName);
        else {
            Type type;
            if (PRIMITIVE_TYPES.containsKey(fullName))
                type = (Type)PRIMITIVE_TYPES.get(fullName);
            else
                type = new Type(fullName);
            _typeObjects.put(fullName, type);
            return type;
        }
    }

    /** Count the number of dimensions of an array type.
     *
     *  @return The number of dimensions (>1) if the type is an array;
     *   otherwise, return 1 (scalar).
     */
    public int dimensions() {
        int bracketPos = _fullName.indexOf("[");
        int dim = 1;
        while (bracketPos >= 0) {
            dim++;
            bracketPos = _fullName.indexOf("[", bracketPos + 1);
        }
        return dim;
    }

    /** Test if this type is semantically equal to the given type.
     *
     *  @param type The type to be tested.
     *  @return <tt>true</tt> if the two types equal; <tt>false</tt>
     *   otherwise.
     */
    public boolean equals(Type type) {
        return _primitiveNum == type._primitiveNum &&
                _fullName.equals(type._fullName);
    }

    /** Get the common type of two types when they appear in an
     *  expression. The following rules are followed:
     *  <ol>
     *    <li>
     *    <li>Two object types cannot be computed in an expression, 
     *      unless one of them is {@link String}, in which case the
     *      result type is {@link String}, or they are the same
     *      type.
     *    </li>
     *    <li>Primitive types can be computed (+) with only one
     *      object type, which is {@link String}. The result is also
     *      {@link String}.
     *    </li>
     *    <li>When two different primitive types are computed, one
     *      of them must be coerced to the other one, which can
     *      store more information.
     *    </li>
     *  </ol>
     *
     *  @param type1 One of the types to be evaluated.
     *  @param type2 The other of the types to be evaluated.
     *  @return The common type of the two if any, or <tt>null</tt>
     *   (not the <tt>null</tt> primitive type) if none.
     */
    public static Type getCommonType(Type type1, Type type2) {
        try {
            if (type1.equals(type2))
                return type1;
            else if (type1.getName().equals("java.lang.String"))
                return type1;
            else if (type2.getName().equals("java.lang.String"))
                return type2;
            else if (type1.isPrimitive() && type2.isPrimitive())
                if (type1.compatibility(type2, 
                        ClassLoader.getSystemClassLoader()) >= 0)
                    return type2;
                else if (type2.compatibility(type1, 
                        ClassLoader.getSystemClassLoader()) >= 0)
                    return type1;
        } catch (ClassNotFoundException e) {
        }
        return null;
    }

    /** Get the name of this type.
     *
     *  @return The name.
     */
    public String getName() {
        return _fullName;
    }

    /** Get the type associated with an AST node.
     *
     *  @param node The node with a type associated with it.
     *  @return The type. <tt>null</tt> if the node has no type
     *   associated with it.
     *  @see #setType(ASTNode, Type)
     */
    public static Type getType(ASTNode node) {
        return (Type)node.getProperty("type");
    }

    /** Test if this type is an array type.
     *
     *  @return <tt>true</tt> if this type is an array type;
     *   <tt>false</tt> otherwise.
     */
    public boolean isArray() {
        return _fullName.indexOf("[") >= 0;
    }

    /** Test if this type is primitive (<tt>boolean</tt>, 
     *  <tt>byte</tt>, <tt>char</tt>, <tt>double</tt>, <tt>float</tt>, 
     *  <tt>int</tt>, <tt>long</tt>, <tt>short</tt>, and
     *  <tt>null</tt>).
     *
     *  @return <tt>true</tt> if this type is primitive; <tt>false</tt>
     *   otherwise.
     */
    public boolean isPrimitive() {
        return _primitiveNum >= 0;
    }

    /** Test if the type given by the name is primitive. Name of
     *  primitive types include ("<tt>boolean</tt>", 
     *  "<tt>byte</tt>", "<tt>char</tt>", "<tt>double</tt>", "<tt>float</tt>", 
     *  "<tt>int</tt>", "<tt>long</tt>", "<tt>short</tt>", "<tt>null</tt>", 
     *  and "<tt>void</tt>").
     *
     *  @param typeName The name of the type.
     *  @return <tt>true</tt> if the type is primitive; <tt>false</tt>
     *   otherwise.
     */
    public static boolean isPrimitive(String typeName) {
        return PRIMITIVE_TYPES.containsKey(typeName);
    }

    /** Copy the type annotation from a type to another.
     *
     *  @param nTo The node whose type is updated.
     *  @param nFrom The node whose type is fetched.
     */
    public static void propagateType(ASTNode nTo, ASTNode nFrom) {
        setType(nTo, getType(nFrom));
    }

    /** Remove one dimension from this type object. The resulting type
     *  may not be an array type.
     *
     *  @return The array type with the one dimension removed. If this
     *   type is not an array type, this function returns this type
     *   itself.
     *  @exception ClassNotFoundException If this type is an
     *   array type, but the type with one dimension less cannot be
     *   resolved as a class.
     *  @see #addOneDimension()
     *  @see #dimensions()
     */
    public Type removeOneDimension() throws ClassNotFoundException {
        if (!isArray())
            return this;

        String newName = _fullName.substring(1);
        int length = newName.length();
        if (length == 1) {
            Enumeration keys = PRIMITIVE_ARRAY_TYPES.keys();
            while (keys.hasMoreElements()) {
                String key = (String)keys.nextElement();
                String value = (String)PRIMITIVE_ARRAY_TYPES.get(key);
                if (value.equals(newName))
                    return createType(key);
            }
        } else if (length > 2 && newName.charAt(0) == 'L' &&
                newName.charAt(length - 1) == ';')
            return createType(newName.substring(1, length - 1));
        else if (length > 1 && newName.charAt(0) == '[')
            return createType(newName);
        throw new ClassNotFoundException(newName);
    }

    /** Remove the type associated with a node.
     *
     *  @param node The node associated with a type.
     */
    public static void removeType(ASTNode node) {
        node.setProperty("type", null);
    }

    /** Get the type object associated with a node.
     *
     *  @param node The node associated with a type.
     *  @param type The type associated with the node. If no type
     *   is associated with the node, <tt>null</tt> is returned.
     *  @see #getType(ASTNode)
     */
    public static void setType(ASTNode node, Type type) {
        node.setProperty("type", type);
    }

    /** Convert the name of an array type to the Java run-time
     *  representation. In this representation, "["s are always placed
     *  before the name of the element type. Arrays of primitive types
     *  are denoted with "[" followed by one character representation of
     *  the primitive element type. Arrays of objects are denoted with
     *  "[L" followed by the full class name of the element type and
     *  then ";".
     *  <p>
     *  This function does nothing if the input type is already Java
     *  run-time form.
     *
     *  @param type The type name to be converted.
     *  @return The transformed type name.
     */
    public static String toArrayType(String type) {
        StringBuffer buffer = new StringBuffer(type);
        int length = buffer.length();
        if (buffer.charAt(length - 1) != ']')
            return type;
        else {
            // Delete the last "[]"
            buffer.delete(length - 2, length);
            buffer.insert(0, '[');
            length -= 1;
            int dims = 1;
            while (length >= 0 && buffer.charAt(length - 1) == ']') {
                length -= 1;
                dims++;
                buffer.insert(0, '[');
            }
            buffer.setLength(length);

            String elementType = buffer.substring(dims, length);
            if (PRIMITIVE_TYPES.containsKey(elementType)) {
                if (elementType.equals("null"))
                    return "null";
                else
                    buffer.replace(dims, length, 
                            (String)PRIMITIVE_ARRAY_TYPES.get(elementType));
            } else {
                buffer.insert(dims, 'L');
                buffer.append(';');
            }
            return buffer.toString();
       }
    }

    /** Convert this type to a {@link Class} object.
     *
     *  @param loader The {@link ClassLoader} object to be used to resolve
     *   the type.
     *  @return The class object.
     *  @exception ClassNotFoundException If the class with
     *   the same name as this type cannot be loaded with the given
     *   class loader.
     */
    public Class toClass(ClassLoader loader) throws ClassNotFoundException {
        if (isPrimitive())
            if (equals(NULL))
                // Impossible to load "null" type, though primitive.
                throw new ClassNotFoundException("null");
            else
                return (Class)PRIMITIVE_CLASSES.get(_fullName);
        else {
            if (_classObject == null)
                _classObject = loader.loadClass(_fullName);
            return _classObject;
        }
    }

    /** Convert this type to {@link String}. This function is the same
     *  as {@link #getName()}.
     *
     *  @return The string as the name of this type.
     *  @see #getName()
     */
    public String toString() {
        return _fullName;
    }

    ///////////////////////////////////////////////////////////////////
    ////                      public fields                        ////

    ///////////////////////////////////////////////////////////////////
    ////              identifiers of primitive types               ////

    /** The integer identifier of <tt>null</tt> type.
     */
    public static final int NULL_NUM    = 0;

    /** The integer identifier of <tt>boolean</tt> type.
     */
    public static final int BOOLEAN_NUM = 1;

    /** The integer identifier of <tt>byte</tt> type.
     */
    public static final int BYTE_NUM    = 2;

    /** The integer identifier of <tt>char</tt> type.
     */
    public static final int CHAR_NUM    = 3;

    /** The integer identifier of <tt>double</tt> type.
     */
    public static final int DOUBLE_NUM  = 4;

    /** The integer identifier of <tt>float</tt> type.
     */
    public static final int FLOAT_NUM   = 5;

    /** The integer identifier of <tt>int</tt> type.
     */
    public static final int INT_NUM     = 6;

    /** The integer identifier of <tt>long</tt> type.
     */
    public static final int LONG_NUM    = 7;

    /** The integer identifier of <tt>short</tt> type.
     */
    public static final int SHORT_NUM   = 8;

    ///////////////////////////////////////////////////////////////////
    ////             type objects of primitive types               ////

    /** The type object of <tt>null</tt> type.
     */
    public static final Type NULL    = new Type(NULL_NUM, "null");

    /** The type object of <tt>boolean</tt> type.
     */
    public static final Type BOOLEAN = new Type(BOOLEAN_NUM, "boolean");

    /** The type object of <tt>byte</tt> type.
     */
    public static final Type BYTE    = new Type(BYTE_NUM, "byte");

    /** The type object of <tt>char</tt> type.
     */
    public static final Type CHAR    = new Type(CHAR_NUM, "char");

    /** The type object of <tt>double</tt> type.
     */
    public static final Type DOUBLE  = new Type(DOUBLE_NUM, "double");

    /** The type object of <tt>float</tt> type.
     */
    public static final Type FLOAT   = new Type(FLOAT_NUM, "float");

    /** The type object of <tt>int</tt> type.
     */
    public static final Type INT     = new Type(INT_NUM, "int");

    /** The type object of <tt>long</tt> type.
     */
    public static final Type LONG    = new Type(LONG_NUM, "long");

    /** The type object of <tt>short</tt> type.
     */
    public static final Type SHORT   = new Type(SHORT_NUM, "short");

    ///////////////////////////////////////////////////////////////////
    ////                       constructors                        ////

    /** Construct a {@link Type} object. Users should not directly
     *  <tt>new</tt> an object but use {@link #createType(String)} to
     *  create it.
     *
     *  @param primitiveNum The integer identifier of primitive types;
     *   -1 if the type to be obtained is not primitive.
     *  @param fullName The full name of the type. If it is an array, 
     *   it must be converted to the run-time representation (with
     *   leading "[").
     */
    private Type(int primitiveNum, String fullName) {
        _primitiveNum = primitiveNum;
        _fullName = fullName;
    }

    /** Construct a {@link Type} object. Users should not directly
     *  <tt>new</tt> an object but use {@link #createType(String)} to
     *  create it. The type constructed by this constructor is never
     *  primitive, even if the full name is a primitive name by
     *  mistake.
     *
     *  @param fullName The full name of the type. If it is an array, 
     *   it must be converted to the run-time representation (with
     *   leading "[").
     */
    private Type(String fullName) {
        _primitiveNum = -1;
        _fullName = fullName;
    }

    ///////////////////////////////////////////////////////////////////
    ////                      private fields                       ////

    /** The integer identifier of primitive type. -1 if this type object
     *  does not represent a primitive type.
     */
    private int _primitiveNum;

    /** The full name of this type, using run-time representation.
     */
    private String _fullName;

    /** The {@link Class} object corresponding to this type.
     */
    private Class _classObject;

    /** The table of created {@link Type} objects, indexed by
     *  their full name. When a user creates a type object
     *  with the same name again, the first one created and
     *  stored in this table is returned.
     */
    private static Hashtable _typeObjects = new Hashtable();

    /** Table of primitive types. Keys are names of primitive
     *  types; values are primitive {@link Type} objects.
     */
    private static final Hashtable PRIMITIVE_TYPES = new Hashtable();

    /** Table of primitive {@link Class} objects. Each primitive
     *  type has a {@link Class} object to represent it.
     */
    private static final Hashtable PRIMITIVE_CLASSES = new Hashtable();

    /** Array nicknames for primitive element types. Keys are
     *  names of primitive types; keys are compact run-time
     *  representations.
     */
    private static final Hashtable PRIMITIVE_ARRAY_TYPES = new Hashtable();

    // Initialize the constant tables.
    static {
        PRIMITIVE_TYPES.put("void", NULL);
        PRIMITIVE_TYPES.put("null", NULL);
        PRIMITIVE_TYPES.put("boolean", BOOLEAN);
        PRIMITIVE_TYPES.put("byte", BYTE);
        PRIMITIVE_TYPES.put("char", CHAR);
        PRIMITIVE_TYPES.put("double", DOUBLE);
        PRIMITIVE_TYPES.put("float", FLOAT);
        PRIMITIVE_TYPES.put("int", INT);
        PRIMITIVE_TYPES.put("long", LONG);
        PRIMITIVE_TYPES.put("short", SHORT);

        PRIMITIVE_CLASSES.put("boolean", boolean.class);
        PRIMITIVE_CLASSES.put("byte", byte.class);
        PRIMITIVE_CLASSES.put("char", char.class);
        PRIMITIVE_CLASSES.put("double", double.class);
        PRIMITIVE_CLASSES.put("float", float.class);
        PRIMITIVE_CLASSES.put("int", int.class);
        PRIMITIVE_CLASSES.put("long", long.class);
        PRIMITIVE_CLASSES.put("short", short.class);

        PRIMITIVE_ARRAY_TYPES.put("boolean", "Z");
        PRIMITIVE_ARRAY_TYPES.put("byte", "B");
        PRIMITIVE_ARRAY_TYPES.put("char", "C");
        PRIMITIVE_ARRAY_TYPES.put("double", "D");
        PRIMITIVE_ARRAY_TYPES.put("float", "F");
        PRIMITIVE_ARRAY_TYPES.put("int", "I");
        PRIMITIVE_ARRAY_TYPES.put("long", "J");
        PRIMITIVE_ARRAY_TYPES.put("short", "S");
    }

}
