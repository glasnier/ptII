/** An actor able to call a C function
 *
@ProposedRating Red (vincent.arnould@thalesgroup.com)
@AcceptedRating Red (vincent.arnould@thalesgroup.com)
*/
package jni;
import ptolemy.actor.Director;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedList;
import ptolemy.kernel.util.Workspace;

import ptolemy.gui.MessageHandler;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.net.URL;

///// JNIActor
/**
 * This actor must have been configured before to be execute.
 * After have set the Arguments of an existing native method,
 * it is necessary to generate the interface thanks to the JNI Menu in Vergil.
 * Once the interface has been generated, it is necessary to open the
 * Visual Project generated to compile the interface dll. Then the actor is
 * ready to
 * warning : the existing dll, the h file and the lib file must be
 * in the $(PTII)/jni/dll/ directory.
 * @author Vincent Arnould (vincent.arnould@thalesgroup.com)
 * @version $Id$
 */
public class GenericJNIActor extends TypedAtomicActor {
        /** Construct an entity in the default workspace with an empty string
         *  as its name.
         *  The object is added to the workspace directory.
         *  Increment the version number of the workspace.
         */
        public GenericJNIActor() {
                super();
                _argList = new NamedList(this);
        }
        /** Construct an entity in the given workspace with an empty string
         *  as a name.
         *  If the workspace argument is null, use the default workspace.
         *  The object is added to the workspace directory.
         *  Increment the version of the workspace.
         *  @param workspace The workspace for synchronization and version tracking.
         */
        public GenericJNIActor(Workspace workspace) {
                super(workspace);
                _argList = new NamedList(this);
        }
        /** Construct an entity in the given workspace with the given name.
         *  If the workspace argument is null, use the default workspace.
         *  If the name argument
         *  is null, then the name is set to the empty string.
         *  The object is added to the workspace directory.
         *  Increment the version of the workspace.
         *  @param workspace The workspace for synchronization and version tracking.
         *  @param name The name of this object.
         *  @exception IllegalActionException If the name has a period.
         */
        public GenericJNIActor(CompositeEntity container, String name)
                throws NameDuplicationException, IllegalActionException {
                super(container, name);
                _argList = new NamedList(this);
                nativeFunc =
                        new Parameter(
                                this,
                                "Native Function Name",
                                (Token) new StringToken("unknownFunc"));
                lib =
                        new Parameter(
                                this,
                                "Native Library Name",
                                (Token) new StringToken("unknownLib"));

                dllDir =
                        new Parameter(
                                this,
                                "DLLs Directory",
                                (Token) new StringToken("jni\\\\dll"));

                _attachText(
                        "_iconDescription",
                        "<svg>\n"
                                + "<rect x=\"0\" y=\"0\" "
                                + "width=\"32\" height=\"38\" "
                                + "style=\"fill:white\"/>\n"
                                + "<image x=\"1\" y=\"1\" width=\"32\" height=\"38\""
                                + "xlink:href=\"jni/dll.gif\"/>\n"
                                + "</svg>\n");

        }
        ///////////////////////////////////////////////////////////////////
        ////                     ports and parameters                  ////
        /** The lib parameter, which contains the name of the
         *  existing native library.
         */
        public Parameter lib;
        /** The native function parameter, which contains the name of the
         *  existing native function.
         */
        public Parameter nativeFunc;
        /** The dllDir parameter, which contains the directory of the
         *  existing dll, h file and lib file.
         */
        public Parameter dllDir;
        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////
        /** Clone the object into the specified workspace. The new object is
         *  <i>not</i> added to the directory of that workspace (you must do this
         *  yourself if you want it there).
         *  The result is a new entity with clones of the ports of the original
         *  entity.  The ports are set to the ports of the new entity.
         *  @param workspace The workspace for the cloned object.
         *  @exception CloneNotSupportedException If cloned ports cannot have
         *   as their container the cloned entity (this should not occur), or
         *   if one of the attributes cannot be cloned.
         *  @return The new Entity.
         */
        public Object clone(Workspace workspace)
                throws CloneNotSupportedException {
                GenericJNIActor newEntity = (GenericJNIActor) super.clone(workspace);
                newEntity._argList = new NamedList(newEntity);
                // Clone the ports.
                Iterator args = argList().iterator();
                while (args.hasNext()) {
                        Argument arg = (Argument) args.next();
                        Argument newArg = (Argument) arg.clone(workspace);
                        // Assume that since we are dealing with clones,
                        // exceptions won't occur normally (the original was successfully
                        // incorporated, so this one should be too).  If they do, throw an
                        // InvalidStateException.
                        try {
                                newArg.setContainer(newEntity);
                        } catch (KernelException ex) {
                                workspace.remove(newEntity);
                                throw new InvalidStateException(
                                        this,
                                        "Failed to clone an Entity: " + ex.getMessage());
                        }
                }
                Class myClass = getClass();
                Field fields[] = myClass.getFields();
                for (int i = 0; i < fields.length; i++) {
                        try {
                                if (fields[i].get(newEntity) instanceof Argument) {
                                        fields[i].set(
                                                newEntity,
                                                newEntity.getArgument(fields[i].getName()));
                                }
                        } catch (IllegalAccessException e) {
                                throw new CloneNotSupportedException(
                                        e.getMessage() + ": " + fields[i].getName());
                        }
                }
                return newEntity;
        }
        /**  If attributes change.
         *  @param attribute The attribute that has changed.
         *  @exception IllegalActionException If the parameters are out of range.
         */
        public void attributeChanged(Attribute attribute)
                throws IllegalActionException {
                if (attribute == null) {
                }
                Director dir = getDirector();
                if (dir != null) {
                        dir.invalidateResolvedTypes();
                }
        }
        /** Get the arguments belonging to this entity.
        *  The order is the order in which they became contained by this entity.
        *  This method is read-synchronized on the workspace.
        *  @return An unmodifiable list of Port objects.
        */
        public List argList() {
                try {
                        _workspace.getReadAccess();
                        return _argList.elementList();
                } finally {
                        _workspace.doneReading();
                }
        }

        /** For each Argument, a port of the same name is created,
         * belonging to this argument.
         *  @return void
         */
        public void createPorts() {
                Iterator ite = this.argList().iterator();
                Iterator ito = this.portList().iterator();
                TypedIOPort port;
                boolean exist = false;
                while (ite.hasNext()) {
                        Argument arg = (Argument) ite.next();
                        port = (TypedIOPort) this.getPort(arg.getName());
                        if (port == null) {
                                if (arg.isReturn())
                                        try {
                                                port = (TypedIOPort) this.newPort(arg.getName());
                                                port.setInput(false);
                                                port.setOutput(true);
                                                port.setTypeEquals(BaseType.GENERAL);
                                        } catch (Exception ex) {
                                                MessageHandler.error("Unable to construct port", ex);
                                        } finally {
                                        } else if (arg.isInput() && arg.isOutput()) {
                                        try {
                                                port = (TypedIOPort) this.newPort(arg.getName() + "in");
                                                port.setInput(arg.isInput());
                                                port.setTypeEquals(BaseType.GENERAL);
                                                port =
                                                        (TypedIOPort) this.newPort(arg.getName() + "out");
                                                port.setOutput(arg.isOutput());
                                                port.setTypeEquals(BaseType.GENERAL);
                                        } catch (Exception ex) {
                                                MessageHandler.error("Unable to construct port", ex);
                                        }
                                } else {
                                        try {
                                                port = (TypedIOPort) this.newPort(arg.getName());
                                                port.setInput(arg.isInput());
                                                port.setOutput(arg.isOutput());
                                                port.setTypeEquals(BaseType.GENERAL);
                                        } catch (Exception ex) {
                                                MessageHandler.error("Unable to construct port", ex);
                                        }
                                }
                        }
                        //end if port == nul
                        // synchronized the arguments and the ports
                        else {
                                if (arg.isReturn()) {
                                        port.setInput(false);
                                        port.setOutput(true);
                                        port.setTypeEquals(BaseType.GENERAL);
                                } else {
                                        port.setInput(arg.isInput());
                                        port.setOutput(arg.isOutput());
                                        port.setTypeEquals(BaseType.GENERAL);
                                }
                        }
                }
        }

        /** Return the argument contained by this entity that has the specified name.
          *  If there is no such port, return null.
          *  This method is read-synchronized on the workspace.
          *  @param name The name of the desired argument.
          *  @return A argument with the given name, or null if none exists.
          */
        public Argument getArgument(String name) {
                try {
                        _workspace.getReadAccess();
                        return (Argument) _argList.get(name);
                } finally {
                        _workspace.doneReading();
                }
        }

        /** Return the argument contained by this entity that is return.
     *  If there is no such argument, return null.
         *  This method is read-synchronized on the workspace.
         *  @return the argument return, or null if none exists.
         */
        public Argument getArgumentReturn() {
                try {
                        _workspace.getReadAccess();
                        List Args = this.argList();
                        Iterator ite = Args.iterator();
                        Argument ret = (Argument) null;
                        while (ite.hasNext()) {
                                Argument arg = (Argument) ite.next();
                                if (arg != null && arg.isReturn()) {
                                        ret = arg;
                                }
                        }
                        return (Argument) ret;
                } finally {
                        _workspace.doneReading();
                }
        }

        /** Add an argument to this entity
         *  @return void
         */
        protected void _addArgument(Argument arg)
                throws IllegalActionException, NameDuplicationException {
                _argList.append((Nameable) arg);
        }

        /** Add a return argument to this entity
         *  @return void
         */
        public void addArgumentReturn()
                throws IllegalActionException, NameDuplicationException {
                try {
                        _workspace.getReadAccess();
                        Argument ret = new Argument(this, "return");
                        ret.setReturn(true);
                        ret.setCType("void");
                } finally {
                        _workspace.doneReading();
                }
        }

        /** remove an argumentto this entity
         * @exception IllegalActionException If an error occurs
         */
        protected void _removeArgument(Argument arg)
                throws IllegalActionException {
                _argList.remove(((Nameable) arg));
        }

        /** Load the generated class and search for its fire method
         */
        public void initialize() {
                String libName = "";
                String dllDir = "";
                try {
                        libName =
                                ((StringToken) ((Parameter) this
                                        .getAttribute("Native Library Name"))
                                        .getToken())
                                        .toString();
                        dllDir =
                                ((StringToken) ((Parameter) this
                                        .getAttribute("DLLs Directory"))
                                        .getToken())
                                        .toString();
                } catch (Exception ex) {
                        MessageHandler.error("no dllDir or libName ! : ", ex);
                }

                String interlibName =
                        "jni" + libName.substring(1, libName.length() - 1);
                //searching the class generated
                try {
                        URL[] tab = new URL[1];
                        tab[0] = new URL("File://" + System.getProperty("users.dir"));
                        ClassLoader cl = new URLClassLoader(tab);
                        _clas =
                                cl.loadClass("jni." + interlibName + ".Jni" + this.getName());
                } catch (Exception ex) {
                        MessageHandler.error("Interface C class not found : ", ex);
                }
                System.setProperty(
                        "java.library.path",
                        System.getProperty("user.dir")
                                + "/"
                                + dllDir
                                + ";"
                                + System.getProperty("java.library.path"));
                meth = null;
                try {
                        meth = _clas.getMethods();
                } catch (Exception ex) {
                        MessageHandler.error("Interface C method not found : ", ex);
                }
                //getting the fire method
                ind = -1;
                for (int i = 0; i < meth.length; i++) {
                        if (meth[i].getName().equals("fire"))
                                ind = i;
                        break;
                }
        }

        /** Read the argument of the function from the ports,
         * call the native method throw the generated interface,
         * and put the results on the corresponding ports
         @exception IllegalActionException If a exception occured
         */
        public void fire() throws IllegalActionException {

                //getting the in/inout parameters
                Iterator ite = this.portList().iterator();
                Vector args = new Vector();
                while (ite.hasNext()) {
                        TypedIOPort port = (TypedIOPort) ite.next();
                        if (port.isInput() && port.hasToken(0) &&
                         !(port.isOutput()&&!port.isInput())) {
                                Token tok = (Token) port.get(0);

                                String typ =
                                        (String) meth[ind].getParameterTypes()[args.size()].toString();
                                if (typ.equals("boolean"))
                                        args.add(new Boolean((boolean)
                                        ((BooleanToken) tok).booleanValue()));
                                else if (typ.equals("int"))
                                        args.add(new Integer((int)((IntToken) tok).intValue()));
                                else if (typ.equals("double"))
                                        args.add(new Double((double)((DoubleToken) tok).doubleValue()));
                                else if (typ.equals("class [I")) {
                                        int siz = ((ArrayToken) tok).arrayValue().length;
                                        int[] tab = new int[siz];
                                        for (int j = 0; j < siz; j++)
                                                tab[j] = (int) ((IntToken) (((ArrayToken) tok)
                                                                .arrayValue()[j]))
                                                                .intValue();
                                        //(int[])((ArrayToken)tok).arrayValue();
                                        args.add((Object)tab);
                                } else
                                        System.out.println(
                                                "The intype is not convertible with Ptolemy II types.");

                        }
                }
                //tBFixed : the out parameter is not in by definition
                //...so no port in can initialize the param

                //call the native function
                Object obj = null;
                Object ret = null;
                try {
                        obj = _clas.newInstance();
                } catch (Exception ex) {
                        MessageHandler.error("Class cannot be instantiated: ", ex);
                }

                try {
                        ret = meth[ind].invoke(obj, args.toArray());
                } catch (Exception ex) {
                        MessageHandler.error("Native operation call failed : ", ex);
                }

                ite = this.portList().iterator();
                while (ite.hasNext()) {
                        TypedIOPort port = (TypedIOPort) ite.next();
                        //if the argument is return
                        if (port.getName().equals(this.getArgumentReturn().getName())) {
                                String typ = "";
                                Field field = null;

                                try {
                                        field = _clas.getDeclaredField("_" + port.getName());
                                        typ = (String) field.getType().toString();
                                } catch (NoSuchFieldException e) {
                                        try {
                                                MessageHandler.warning("No return typ!");
                                        } catch (Exception ex2) {
                                                getDirector().stop();
                                        }
                                }
                                if (typ.equals("boolean"))
                                        port.send(
                                                0,
                                                (Token) new BooleanToken(((Boolean) ret).booleanValue()));
                                else if (typ.equals("double"))
                                        port.send(
                                                0,
                                                (Token) new DoubleToken(((Double) ret).doubleValue()));
                                else if (typ.equals("int"))
                                        port.send(
                                                0,
                                                (Token) new IntToken(((Integer) ret).intValue()));
                                else if (typ.equals("char"))
                                        port.send(
                                                0,
                                                (Token) new UnsignedByteToken(((Byte) ret).byteValue()));
                        else
                                        System.out.println(
                                                "The return type is not convertible with Ptolemy II types.");
                        }
                        //if the argument is output
                        else if (
                                port.isOutput()
                                        && !(port
                                                .getName()
                                                .equals(this.getArgumentReturn().getName()))) {

                                String typ = "";
                                Field field = null;

                                try {
                                        field = _clas.getDeclaredField("_" + port.getName());
                                        typ = (String) field.getType().toString();
                                } catch (NoSuchFieldException ex) {
                                        try {
                                                field =
                                                        _clas.getDeclaredField(
                                                                "_"
                                                                        + port.getName().substring(
                                                                                0,
                                                                                port.getName().length() - 3));
                                                typ = (String) field.getType().toString();
                                        } catch (Exception e) {
                                                try {
                                                        MessageHandler.warning(
                                                                "No " + port.getName() + " field !");
                                                } catch (Exception ex2) {
                                                        getDirector().stop();
                                                }
                                        }
                                }
                                if (typ.equals("boolean"))
                                        try {
                                                port.send(
                                                0,
                                                (Token) new BooleanToken(field.getBoolean(obj)));
                                                } catch (IllegalAccessException ex) {
                                                MessageHandler.error("Not castable!", ex);
                                        }
                                else if (typ.equals("double"))
                                        try {
                                                port.send(
                                                0,
                                                (Token) new DoubleToken(field.getDouble(obj)));
                                                } catch (IllegalAccessException ex) {
                                                MessageHandler.error("Not castable!", ex);
                                        }
                                else if (typ.equals("int"))
                                        try {
                                                port.send(
                                                0,
                                                (Token) new IntToken( field.getInt(obj)));
                                                } catch (IllegalAccessException ex) {
                                                MessageHandler.error("Not castable!", ex);
                                        }
                                else if (typ.equals("char"))
                                        try {
                                                port.send(
                                                0,
                                                (Token) new UnsignedByteToken((char)field.getChar(obj)));
                                                } catch (IllegalAccessException ex) {
                                                MessageHandler.error("Not castable!", ex);
                                        }
                           else if (typ.equals("class [I")) {
                                        try {
                                                int[] tab = (int[])field.get(obj);
                                                Token[] toks =  new Token[((int[])field.get(obj)).length];
                                                for(int j = 0; j<((int[])field.get(obj)).length ; j++)
                                                toks[j] = new IntToken(((int[])field.get(obj))[j]);
                                                port.send(0, new ArrayToken(toks));
                                            } catch (IllegalAccessException ex) {
                                                  MessageHandler.error("Not castable!", ex); }
                                    } else
                                        System.out.println(
                                                "The outtype is not convertible with Ptolemy II types.");
                        }
                }
        }
        /** A list of Ports owned by this Entity.
        */
        private NamedList _argList;

        /** A private class dynamicly loaded for the JNI interface.
        */
        private Class _clas;

        /** A indicator
        */
        private int ind;

        /** A private array that contains the methods
         *  of the generated interface class.
         */
        private Method[] meth;
}
