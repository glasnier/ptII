/* Code generator adapter class associated with the StaticSchedulingDirector class.

 Copyright (c) 2005-2009 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.sched;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.util.DFUtilities;
import ptolemy.cg.adapter.generic.adapters.ptolemy.actor.Director;
import ptolemy.cg.kernel.generic.ActorCodeGenerator;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapterStrategy;
import ptolemy.cg.kernel.generic.CodeStream;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapterStrategy.Channel;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////
////StaticSchedulingDirector

/**
 Code generator adapter associated with the StaticSchedulingDirector class.
 This class is also associated with a code generator.

 @author Gang Zhou, Contributor: Bert Rodiers
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Yellow (zgang)
 @Pt.AcceptedRating Red (eal)
 */
public class StaticSchedulingDirector extends Director {

    /** Construct the code generator adapter associated with the given
     *  StaticSchedulingDirector.
     *  @param staticSchedulingDirector The associated
     *  ptolemy.actor.sched.StaticSchedulingDirector
     */
    public StaticSchedulingDirector(
            ptolemy.actor.sched.StaticSchedulingDirector staticSchedulingDirector) {
        super(staticSchedulingDirector);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Generate the code for the firing of actors according to the SDF
     *  schedule.
     *  @return The generated fire code.
     *  @exception IllegalActionException If the SDF director does not have an
     *   attribute called "iterations" or a valid schedule, or the actor to be
     *   fired cannot find its associated adapter.
     */
    public String generateFireCode() throws IllegalActionException {

        StringBuffer code = new StringBuffer();
        code.append(CodeStream.indent(getCodeGenerator()
                .comment("The firing of the StaticSchedulingDirector")));
        boolean inline = ((BooleanToken) getCodeGenerator().inline.getToken())
                .booleanValue();

        // Generate code for one iteration.
        ptolemy.actor.sched.StaticSchedulingDirector director = (ptolemy.actor.sched.StaticSchedulingDirector) getComponent();
        Schedule schedule = director.getScheduler().getSchedule();

        boolean isIDefined = false;
        Iterator<?> actorsToFire = schedule.firingIterator();
        while (actorsToFire.hasNext()) {
            Firing firing = (Firing) actorsToFire.next();
            Actor actor = firing.getActor();

            // FIXME: Before looking for a adapter class, we should check to
            // see whether the actor contains a code generator attribute.
            // If it does, we should use that as the adapter.
            CodeGeneratorAdapter adapter = getCodeGenerator().getAdapter((NamedObj) actor);

            if (inline) {
                for (int i = 0; i < firing.getIterationCount(); i++) {

                    // generate fire code for the actor
                    code.append(adapter.generateFireCode());

                    _generateUpdatePortOffsetCode(code, actor);
                }
            } else {

                int count = firing.getIterationCount();
                if (count > 1) {
                    if (!isIDefined) {
                        code.append("int i;" + _eol);
                        isIDefined = true;
                    }
                    code.append("for (i = 0; i < " + count
                            + " ; i++) {" + _eol);
                }

                code.append(CodeGeneratorAdapterStrategy.generateName((NamedObj)
                 actor) + "();" + _eol);

                _generateUpdatePortOffsetCode(code, actor);

                if (count > 1) {
                    code.append("}" + _eol);
                }
            }
        }
        return code.toString();
    }

    /** Generate a main loop for an execution under the control of
     *  this director. If the associated director has a parameter
     *  named <i>iterations</i> with a value greater than zero,
     *  then wrap code generated by generateFireCode() in a
     *  loop that executes the specified number of iterations.
     *  Otherwise, wrap it in a loop that executes forever.
     *  In the loop, first get the code returned by generateFireCode(),
     *  and follow that with the code produced by the container
     *  help for generateModeTransitionCode(). That code will
     *  make state transitions in modal models at the conclusion
     *  of each iteration. Next, this code calls postfire(), and
     *  that returns false, breaks out of the main loop.
     *  Finally, if the director has a parameter named <i>period</i>,
     *  then increment the variable _currentTime after each
     *  pass through the loop.
     *  @return Code for the main loop of an execution.
     *  @exception IllegalActionException If something goes wrong.
     */
    public String generateMainLoop() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        Attribute iterations = _director.getAttribute("iterations");
        if (iterations == null) {
            code.append(_eol + "while (true) {" + _eol);
        } else {
            int iterationCount = ((IntToken) ((Variable) iterations).getToken())
                    .intValue();
            if (iterationCount <= 0) {
                code.append(_eol + "while (true) {" + _eol);
            } else {
                // Declare iteration outside of the loop to avoid
                // mode" with gcc-3.3.3
                code.append(_eol + "int iteration;" + _eol);
                code.append("for (iteration = 0; iteration < "
                        + iterationCount + "; iteration ++) {" + _eol);
            }
        }

        code.append(generateFireCode());

        // The code generated in generateModeTransitionCode() is executed
        // after one global iteration, e.g., in HDF model.
        ActorCodeGenerator modelAdapter = getCodeGenerator().getAdapter(_director
                .getContainer());
        modelAdapter.generateModeTransitionCode(code);

        /*if (callPostfire) {
            code.append(_INDENT2 + "if (!postfire()) {" + _eol + _INDENT3
                    + "break;" + _eol + _INDENT2 + "}" + _eol);
        }
         */
        _generateUpdatePortOffsetCode(code,
                (Actor) _director.getContainer());

        code.append(generatePostfireCode());

        Attribute period = _director.getAttribute("period");
        if (period != null) {
            Double periodValue = ((DoubleToken) ((Variable) period).getToken())
                    .doubleValue();
            if (periodValue != 0.0) {
                code.append("_currentTime += " + periodValue + ";" + _eol);
            }
            code.append("}" + _eol);
        }

        return code.toString();
    }

    /** Generate the preinitialize code for this director.
     *  The preinitialize code for the director is generated by appending
     *  the preinitialize code for each actor.
     *  @return The generated preinitialize code.
     *  @exception IllegalActionException If getting the adapter fails,
     *   or if generating the preinitialize code for a adapter fails,
     *   or if there is a problem getting the buffer size of a port.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generatePreinitializeCode());

        ptolemy.actor.sched.StaticSchedulingDirector director = (ptolemy.actor.sched.StaticSchedulingDirector) getComponent();

        // Force schedule (re)calculation before generating code
        // because we need to know buffer capacity. (otherwise
        // sometimes new receivers are created but the schedule
        // is not re-calculated.)
        director.invalidateSchedule();
        director.getScheduler().getSchedule();

        return code.toString();
    }


    // FIXME: documentation
    public String getReference(String name, boolean isWrite, CodeGeneratorAdapter target)
    throws IllegalActionException {

        name = processCode(name);
        String castType = _getCastType(name);
        String refName = _getRefName(name);
        String[] channelAndOffset = _getChannelAndOffset(name);

        // Usually given the name of an input port, getReference(String name)
        // returns variable name representing the input port. Given the name
        // of an output port, getReference(String name) returns variable names
        // representing the input ports connected to the output port.
        // However, if the name of an input port starts with '@',
        // getReference(String name) returns variable names representing the
        // input ports connected to the given input port on the inside.
        // If the name of an output port starts with '@',
        // getReference(String name) returns variable name representing the
        // the given output port which has inside receivers.
        // The special use of '@' is for composite actor when
        // tokens are transferred into or out of the composite actor.
        boolean forComposite = false;
        if (refName.charAt(0) == '@') {
            forComposite = true;
            refName = refName.substring(1);
        }

        TypedIOPort port = target.getStrategy().getPort(refName);
        if (port != null) {

            if (port instanceof ParameterPort && port.numLinks() <= 0) {

                // Then use the parameter (attribute) instead.
            } else {
                String result = getReference(
                        port, channelAndOffset, forComposite, isWrite, target);

                String refType = getStrategy().codeGenType(port.getType());

                return getStrategy()._generateTypeConvertMethod(result, castType, refType);
            }
        }

        // Try if the name is a parameter.
        Attribute attribute = target.getComponent().getAttribute(refName);

        if (attribute != null) {
            String refType = _getRefType(attribute);

            String result = getReference(
                    attribute, channelAndOffset);

            return getStrategy()._generateTypeConvertMethod(result, castType, refType);
        }

        throw new IllegalActionException(target.getComponent(), "Reference not found: "
                + name);
    }

    public String getReference(TypedIOPort port,
            String[] channelAndOffset,
            boolean forComposite,
            boolean isWrite, CodeGeneratorAdapter target) throws IllegalActionException {

        StringBuffer result = new StringBuffer();
        boolean dynamicReferencesAllowed = ((BooleanToken) getCodeGenerator().
                allowDynamicMultiportReference.getToken()).booleanValue();

        int channelNumber = 0;
        boolean isChannelNumberInt = true;
        if (!channelAndOffset[0].equals("")) {
            // If dynamic multiport references are allowed, catch errors
            // when the channel specification is not an integer.
            if (dynamicReferencesAllowed) {
                try {
                    channelNumber =
                        (Integer.valueOf(channelAndOffset[0])).intValue();
                } catch (Exception ex) {
                    isChannelNumberInt = false;
                }
            } else {
                channelNumber =
                    (Integer.valueOf(channelAndOffset[0])).intValue();
            }
        }

        if (!isChannelNumberInt) { // variable channel reference.
            if (port.isOutput()) {
                throw new IllegalActionException(
                        "Variable channel reference not supported"
                        + " for output ports");
            } else {

                return CodeGeneratorAdapterStrategy.generatePortReference(port, channelAndOffset, isWrite);
            }
        }

        // To support modal model, we need to check the following condition
        // first because an output port of a modal controller should be
        // mainly treated as an output port. However, during choice action,
        // an output port of a modal controller will receive the tokens sent
        // from the same port.  During commit action, an output port of a modal
        // controller will NOT receive the tokens sent from the same port.
        if (CodeGeneratorAdapterStrategy.checkRemote(forComposite, port)) {
            Receiver[][] remoteReceivers;

            // For the same reason as above, we cannot do: if (port.isInput())...
            if (port.isOutput()) {
                remoteReceivers = port.getRemoteReceivers();
            } else {
                remoteReceivers = port.deepGetReceivers();
            }

            if (remoteReceivers.length == 0) {
                // The channel of this output port doesn't have any sink.
                result.append(CodeGeneratorAdapterStrategy.generateName(target.getComponent()));
                result.append("_");
                result.append(port.getName());
                return result.toString();
            }

            Channel sourceChannel = new Channel(port, channelNumber);

            List<Channel> typeConvertSinks = getStrategy()._getTypeConvertSinkChannels(sourceChannel);

            List<Channel> sinkChannels = CodeGeneratorAdapterStrategy.getSinkChannels(port, channelNumber);

            boolean hasTypeConvertReference = false;

            for (int i = 0; i < sinkChannels.size(); i++) {
                Channel channel = sinkChannels.get(i);
                IOPort sinkPort = channel.port;
                int sinkChannelNumber = channel.channelNumber;

                // Type convert.
                if (typeConvertSinks.contains(channel) &&
                        getStrategy().isPrimitive(((TypedIOPort) sourceChannel.port).getType())) {

                    if (!hasTypeConvertReference) {
                        if (i != 0) {
                            result.append(" = ");
                        }
                        result.append(CodeGeneratorAdapterStrategy.getTypeConvertReference(sourceChannel));

                        if (dynamicReferencesAllowed && port.isInput()) {
                            if (channelAndOffset[1].trim().length() > 0) {
                                result.append("[" + channelAndOffset[1].trim() + "]");
                            } else {
                                result.append("[" +
                                        CodeGeneratorAdapterStrategy.generateChannelOffset(
                                                port, isWrite, channelAndOffset[0]) + "]");
                            }
                        } else {
                            int rate = Math.max(
                                    DFUtilities.getTokenProductionRate(sourceChannel.port),
                                    DFUtilities.getTokenConsumptionRate(sourceChannel.port));
                            if (rate > 1
                                    && channelAndOffset[1].trim().length() > 0) {
                                result.append("["
                                        + channelAndOffset[1].trim() + "]");
                            }
                        }
                        hasTypeConvertReference = true;
                    } else {
                        // We already generated reference for this sink.
                        continue;
                    }
                } else {
                    if (i != 0) {
                        result.append(" = ");
                    }
                    result.append(CodeGeneratorAdapterStrategy.generateName(sinkPort));

                    if (sinkPort.isMultiport()) {
                        result.append("[" + sinkChannelNumber + "]");
                    }
                    if (channelAndOffset[1].equals("")) {
                        channelAndOffset[1] = "0";
                    }
                    result.append(_ports.generateOffset(sinkPort, channelAndOffset[1],
                            sinkChannelNumber, true));
                }
            }

            return result.toString();
        }

        // Note that if the width is 0, then we have no connection to
        // the port but the port might be a PortParameter, in which
        // case we want the Parameter.
        // codegen/c/actor/lib/string/test/auto/StringCompare3.xml
        // tests this.

        if (CodeGeneratorAdapterStrategy.checkLocal(forComposite, port)) {

            result.append(CodeGeneratorAdapterStrategy.generateName(port));

            //if (!channelAndOffset[0].equals("")) {
            if (port.isMultiport()) {
                // Channel number specified. This must be a multiport.
                result.append("[" + channelAndOffset[0] + "]");
            }

            result.append(_ports.generateOffset(port, channelAndOffset[1],
                    channelNumber, isWrite));

            return result.toString();
        }

        // FIXME: when does this happen?
        return "";
    }


    /** Generate a variable declaration for the <i>period</i> parameter,
     *  if there is one.
     *  @return code The generated code.
     *  @exception IllegalActionException If the adapter class for the model
     *   director cannot be found.
     */
    public String generateVariableDeclaration() throws IllegalActionException {
        StringBuffer variableDeclarations = new StringBuffer(super
                .generateVariableDeclaration());
        Attribute period = _director.getAttribute("period");
        if (period != null) {
            Double periodValue = ((DoubleToken) ((Variable) period).getToken())
                    .doubleValue();
            if (periodValue != 0.0) {
                variableDeclarations.append(_eol
                        + getCodeGenerator().comment(
                                "Director has a period attribute,"
                                        + " so we track current time."));
                variableDeclarations.append("double _currentTime = 0;" + _eol);
            }
        }

        return variableDeclarations.toString();
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Update the offsets of the buffers associated with the ports connected
     *  with the given port in its downstream.
     *
     *  @param port The port whose directly connected downstream actors update
     *   their write offsets.
     *  @param code The string buffer that the generated code is appended to.
     *  @param rate The rate, which must be greater than or equal to 0.
     *  @exception IllegalActionException If thrown while reading or writing
     *   offsets, or getting the buffer size, or if the rate is less than 0.
     */
    final protected void _updateConnectedPortsOffset(IOPort port, StringBuffer code,
            int rate) throws IllegalActionException {
        if (rate == 0) {
            return;
        } else if (rate < 0) {
            throw new IllegalActionException(port, "the rate: " + rate
                    + " is negative.");
        }

        code.append(_ports.updateConnectedPortsOffset(port, rate));
    }

    /** Update the read offsets of the buffer associated with the given port.
    *
    *  @param port The port whose read offset is to be updated.
    *  @param code The string buffer that the generated code is appended to.
    *  @param rate The rate, which must be greater than or equal to 0.
    *  @exception IllegalActionException If thrown while reading or writing
    *   offsets, or getting the buffer size, or if the rate is less than 0.
    */
   protected void _updatePortOffset(IOPort port, StringBuffer code, int rate)
   throws IllegalActionException {
       if (rate == 0) {
           return;
       } else if (rate < 0) {
           throw new IllegalActionException(port, "the rate: " + rate
                   + " is negative.");
       }

       code.append(_ports.updateOffset(port, rate));
   }
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Generate the code that updates the input/output port offset.
     * @param code The given code buffer.
     * @param actor The given actor.
     * @exception IllegalActionException Thrown if
     *  _updatePortOffset(IOPort, StringBuffer, int) or getRate(IOPort)
     *  throw it.
     */
    private void _generateUpdatePortOffsetCode(StringBuffer code, Actor actor)
            throws IllegalActionException {
        // update buffer offset after firing each actor once
        Iterator<?> inputPorts = actor.inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort port = (IOPort) inputPorts.next();
            int rate = DFUtilities.getRate(port);
            _updatePortOffset(port, code, rate);
        }

        Iterator<?> outputPorts = actor.outputPortList().iterator();
        while (outputPorts.hasNext()) {
            IOPort port = (IOPort) outputPorts.next();
            int rate = DFUtilities.getRate(port);
            _updateConnectedPortsOffset(port, code, rate);
        }
    }

    private String _getCastType(String name) throws IllegalActionException {
        StringTokenizer tokenizer = new StringTokenizer(name, "#,", true);

        // Get the referenced name.
        String refName = tokenizer.nextToken().trim();

        // Get the cast type (if any), so we can add the proper convert method.
        StringTokenizer tokenizer2 = new StringTokenizer(refName, "()", false);
        if (tokenizer2.countTokens() != 1 && tokenizer2.countTokens() != 2) {
            throw new IllegalActionException(getComponent(), "Invalid cast type: "
                    + refName);
        }

        if (tokenizer2.countTokens() == 2) {
            String type = tokenizer2.nextToken().trim();
            return (type.length() > 0) ? type : null;
        }
        return null;
    }

    /** Return the channel and offset given in a string.
     *  The result is an string array of length 2. The first element
     *  indicates the channel index, and the second the offset. If either
     *  element is an empty string, it means that channel/offset is not
     *  specified.
     * @param name The given string.
     * @return An string array of length 2, containing expressions of the
     *  channel index and offset.
     * @exception IllegalActionException If the channel index or offset
     *  specified in the given string is illegal.
     *  FIXME rodiers: SDF specific
     */
    private String[] _getChannelAndOffset(String name)
    throws IllegalActionException {

        String[] result = { "", "" };

        // Given expression of forms:
        //     "port"
        //     "port, offset", or
        //     "port#channel, offset".

        int poundIndex = CodeGeneratorAdapterStrategy.indexOf("#", name, 0);
        int commaIndex = CodeGeneratorAdapterStrategy.indexOf(",", name, 0);

        if (commaIndex < 0) {
            commaIndex = name.length();
        }
        if (poundIndex < 0) {
            poundIndex = commaIndex;
        }

        if (poundIndex < commaIndex) {
            result[0] = name.substring(poundIndex + 1, commaIndex);
        }

        if (commaIndex < name.length()) {
            result[1] = name.substring(commaIndex + 1);
        }
        return result;
    }


    private String _getRefName(String name) throws IllegalActionException {
        StringTokenizer tokenizer = new StringTokenizer(name, "#,", true);

        if ((tokenizer.countTokens() != 1) && (tokenizer.countTokens() != 3)
                && (tokenizer.countTokens() != 5)) {
            throw new IllegalActionException(getComponent(),
                    "Reference not found: " + name);
        }

        // Get the referenced name.
        String refName = tokenizer.nextToken().trim();

        // Get the cast type (if any), so we can add the proper convert method.
        StringTokenizer tokenizer2 = new StringTokenizer(refName, "()", false);
        if (tokenizer2.countTokens() != 1 && tokenizer2.countTokens() != 2) {
            throw new IllegalActionException(getComponent(), "Invalid cast type: "
                    + refName);
        }

        if (tokenizer2.countTokens() == 2) {
            // castType
            tokenizer2.nextToken();
        }

        return tokenizer2.nextToken().trim();
    }

    private String _getRefType(Attribute attribute) {
        if (attribute instanceof Parameter) {
            return getStrategy().codeGenType(((Parameter) attribute).getType());
        }
        return null;
    }

    private class PortInfo {

        public PortInfo(IOPort port) {
            _port = port;
        }


        /** Get the buffer size of channel of the port.
         *  @param channelNumber The number of the channel that is being set.
         *  @return return The size of the buffer.
         *  @see #setBufferSize(int, int)
         */
        public int getBufferSize(int channelNumber)
            throws IllegalActionException {
            CodeGeneratorAdapterStrategy.Channel channel = _getChannel(channelNumber);

            if (_bufferSizes.get(channel) == null) {
                // This should be a special case for doing
                // codegen for a sub-part of a model.
                return channel.port.getWidth();
            }

            return _bufferSizes.get(channel);
        }


        /**
         * Return the buffer size of a given port, which is the maximum of
         * the bufferSizes of all channels of the given port.
         * @param port The given port.
         * @return The buffer size of the given port.
         * @exception IllegalActionException If the
         * {@link #getBufferSize(IOPort, int)} method throws it.
         * @see #setBufferSize(IOPort, int, int)
         */
        public int getBufferSize() throws IllegalActionException {
            int bufferSize = 1;

                int length = 0;

                if (_port.isInput()) {
                    length = _port.getWidth();
                } else {
                    length = _port.getWidthInside();
                }

                for (int i = 0; i < length; i++) {
                    int channelBufferSize = getBufferSize(i);

                    if (channelBufferSize > bufferSize) {
                        bufferSize = channelBufferSize;
                    }
                }


            return bufferSize;
        }


        /** Generate the initialize code for this director.
         *  The initialize code for the director is generated by appending the
         *  initialize code for each actor.
         *  @return The generated initialize code.
         *  @exception IllegalActionException If the adapter associated with
         *   an actor throws it while generating initialize code for the actor.
         */
        public String generateInitializeCode() throws IllegalActionException {
            StringBuffer code = new StringBuffer();

            Iterator<?> actors = ((CompositeActor) _director.getContainer())
            .deepEntityList().iterator();
            while (actors.hasNext()) {
                Actor actor = (Actor) actors.next();
                CodeGeneratorAdapter adapterObject = getCodeGenerator().getAdapter((NamedObj) actor);
                // Initialize code for the actor.
                code.append(adapterObject.generateInitializeCode());

                // Update write offset due to initial tokens produced.
                Iterator<?> outputPorts = actor.outputPortList().iterator();
                while (outputPorts.hasNext()) {
                    IOPort port = (IOPort) outputPorts.next();
                    int rate = DFUtilities.getTokenInitProduction(port);
                    _updateConnectedPortsOffset(port, code, rate);
                }

                for (IOPort port : (List<IOPort>) ((Entity) actor).portList()) {
                    if (port.isOutsideConnected()) {
                        CodeGeneratorAdapter portAdapter =
                            getCodeGenerator().getAdapter(port);
                        code.append(portAdapter.generateInitializeCode());
                    }
                }
            }
            return code.toString();
        }


        /**Generate the expression that represents the offset in the generated
         * code.
         * @param offset The specified offset from the user.
         * @param channel The referenced port channel.
         * @param isWrite Whether to generate the write or read offset.
         * @return The expression that represents the offset in the generated code.
         * @exception IllegalActionException If there is problems getting the port
         *  buffer size or the offset in the channel and offset map.
         */
        public String generateOffset(String offset, int channel, boolean isWrite) throws IllegalActionException {

            //Receiver receiver = _getReceiver(offset, channel, _port);

         // FIXME rodiers: reintroduce PN! (but somewhere else)
            if (false) {
            /*
            if (_isPthread() && receiver instanceof PNQueueReceiver) {
                String result;
                if (offset.length() == 0 || offset.equals("0")) {
                    result = (isWrite) ?
                            "$getWriteOffset(" : "$getReadOffset(";
                } else {
                    result = (isWrite) ?
                            "$getAdvancedWriteOffset(" : "$getAdvancedReadOffset(";
                    result += offset + ", ";
                }

                // FIXME: why does this depend on PN?
                PNDirector pnDirector = (PNDirector) _getAdapter(director);
                result += "&" + PNDirector.generatePortHeader(port, channel) + ", ";
                result += "&" + pnDirector.generateDirectorHeader() + ")";
                return "[" + result + "]";
                */
                return "";
            // End FIXME rodiers
            } else {
                return _generateOffset(offset, channel, isWrite);
            }
        }


        /** Get the read offset of a channel of the port.
         *  @param channelNumber The number of the channel.
         *  @return The read offset.
         *  @exception IllegalActionException If thrown while getting the channel.
         *  @see #setReadOffset(int, Object)
         */
        public Object getReadOffset(int channelNumber)
            throws IllegalActionException {
            CodeGeneratorAdapterStrategy.Channel channel = _getChannel(channelNumber);
            return _readOffsets.get(channel);

        }



        /** Get the write offset of a channel of the port.
         *  @param channelNumber The number of the channel.
         *  @return The write offset.
         *  @exception IllegalActionException If thrown while getting the channel.
         *  @see #setWriteOffset(int, Object)
         */
        public Object getWriteOffset(int channelNumber)
        throws IllegalActionException {
            CodeGeneratorAdapterStrategy.Channel channel = _getChannel(channelNumber);
            return _writeOffsets.get(channel);

        }

        /** Initialize the offsets.
         *  @return The code to initialize the offsets.
         */
        public String initializeOffsets() throws IllegalActionException {

            /* FIXME: move pthread specific code out-of-here...
            if (_isPthread()) {
                return "";
            }
            */

            StringBuffer code = new StringBuffer();

            for (int i = 0; i < _port.getWidth(); i++) {
                Object readOffset = _ports.getReadOffset(_port, i);
                if (readOffset instanceof Integer) {
                    _ports.setReadOffset(_port, i, Integer.valueOf(0));
                } else {
                    code.append(((String) readOffset)
                            + " = 0;" + _eol);
                }
                Object writeOffset = _ports.getWriteOffset(_port, i);
                if (writeOffset instanceof Integer) {
                    _ports.setWriteOffset(_port, i, Integer.valueOf(0));
                } else {
                    code.append(((String) writeOffset)
                            + " = 0;" + _eol);
                }
            }
            return code.toString();
        }

        /** Set the buffer size of channel of the port.
         *  @param channelNumber The number of the channel that is being set.
         *  @param bufferSize The size of the buffer.
         *  @see #getBufferSize(int)
         */
        public void setBufferSize(int channelNumber, int bufferSize) {
            CodeGeneratorAdapterStrategy.Channel channel = _getChannel(channelNumber);
            _bufferSizes.put(channel, bufferSize);
        }


        /** Set the read offset of a channel of the port.
         *  @param channelNumber The number of the channel that is being set.
         *  @param readOffset The offset.
         *  @see #getReadOffset(int)
         */
        public void setReadOffset(int channelNumber, Object readOffset) {
            CodeGeneratorAdapterStrategy.Channel channel = _getChannel(channelNumber);
            _readOffsets.put(channel, readOffset);
        }

        /** Set the write offset of a channel of the port.
         *  @param channelNumber The number of the channel that is being set.
         *  @param writeOffset The offset.
         *  @see #getWriteOffset(int)
         */
        public void setWriteOffset(int channelNumber, Object writeOffset) {
            CodeGeneratorAdapterStrategy.Channel channel = _getChannel(channelNumber);
            _writeOffsets.put(channel, writeOffset);
        }



        // Update the write offset of the [multiple] connected ports.
        public String updateConnectedPortsOffset(int rate) throws IllegalActionException {
            boolean padBuffers = ((BooleanToken) getCodeGenerator().padBuffers
                    .getToken()).booleanValue();

            StringBuffer code = new StringBuffer();
            code.append(getCodeGenerator().comment(_eol + "....Begin updateConnectedPortsOffset...."
                                                   + CodeGeneratorAdapterStrategy.generateName(_port)));

            if (rate == 0) {
                return "";
            } else if (rate < 0) {
                throw new IllegalActionException(_port, "the rate: " + rate
                        + " is negative.");
            }

            int length = 0;
            if (_port.isInput()) {
                length = _port.getWidthInside();
            } else {
                length = _port.getWidth();
            }

            for (int j = 0; j < length; j++) {
                List<Channel> sinkChannels = CodeGeneratorAdapterStrategy.getSinkChannels(_port, j);

                for (int k = 0; k < sinkChannels.size(); k++) {
                    Channel channel = (Channel) sinkChannels.get(k);
                    ptolemy.actor.IOPort sinkPort = channel.port;
                    int sinkChannelNumber = channel.channelNumber;

                    Object offsetObject = _ports.getWriteOffset(sinkPort,
                            sinkChannelNumber);

                    //Receiver receiver = _getReceiver(
                    //        offsetObject.toString(), sinkChannelNumber, sinkPort);

                    // FIXME rodiers: reintroduce PN specifics (but somewhere else)
                    if (false) {
                    /*if (_isPthread() && MpiPNDirector.isMpiReceiveBuffer(sinkPort, sinkChannelNumber)) {
                        code.append(_generateMPISendCode(j, rate, sinkPort, sinkChannelNumber, director));

                    } else if (_isPthread() && receiver instanceof PNQueueReceiver) {

                        // PNReceiver.
                        code.append(_updatePNOffset(rate, sinkPort, sinkChannelNumber, director, true));
                        */
                     // End FIXME rodiers
                    } else {

                        if (offsetObject instanceof Integer) {
                            int offset = ((Integer) offsetObject).intValue();
                            int bufferSize = _ports.getBufferSize(sinkPort,
                                    sinkChannelNumber);
                            if (bufferSize != 0) {
                                offset = (offset + rate) % bufferSize;
                            }
                            _ports.setWriteOffset(sinkPort, sinkChannelNumber, Integer
                                    .valueOf(offset));
                        } else { // If offset is a variable.
                            String offsetVariable = (String) _ports.getWriteOffset(
                                    sinkPort, sinkChannelNumber);

                            if (padBuffers) {
                                int modulo = _ports.getBufferSize(sinkPort,
                                        sinkChannelNumber) - 1;
                                code.append(offsetVariable + " = ("
                                        + offsetVariable + " + " + rate + ")&" + modulo
                                        + ";" + _eol);
                            } else {
                                code.append(offsetVariable + " = ("
                                        + offsetVariable + " + " + rate
                                        + ") % "
                                        + _ports.getBufferSize(sinkPort,
                                                sinkChannelNumber) + ";" + _eol);
                            }
                        }
                    }
                }
            }
            code.append(getCodeGenerator().comment(_eol + "....End updateConnectedPortsOffset...."
                                                   + CodeGeneratorAdapterStrategy.generateName(_port)));
            return code.toString();
        }

        /** Update the read offset.
         *  @param rate  The rate of the channels.
         *  @param directorHelper The Director helper
         *  @return The offset.
         */
        public String updateOffset(int rate)
        throws IllegalActionException {

            //Receiver receiver = _getReceiver(null, 0, _port);

            String code = getCodeGenerator().comment(_eol + "....Begin updateOffset...."
                                                     + CodeGeneratorAdapterStrategy.generateName(_port));

            //        int width = 0;
            //        if (port.isInput()) {
            //            width = port.getWidth();
            //        } else {
            //            width = port.getWidthInside();
            //        }

            for (int i = 0; i < _port.getWidth(); i++) {
                // FIXME rodiers: reintroduce PN specifics (but somewhere else)
                if (false) {
                /*
                if (MpiPNDirector.isMpiReceiveBuffer(port, i)) {
                    // do nothing.
                } else if (_isPthread() && receiver instanceof PNQueueReceiver) {

                    // FIXME: this is kind of hacky.
                    //PNDirector pnDirector = (PNDirector)//directorAdapter;
                    _getAdapter(((Actor) port.getContainer()).getExecutiveDirector());

                    List<Channel> channels = PNDirector.getReferenceChannels(port, i);

                    for (Channel channel : channels) {
                        code += _updatePNOffset(rate, channel.port,
                                channel.channelNumber, directorAdapter, false);
                    }
                    code += getCodeGenerator().comment(_eol + "....End updateOffset (PN)...."
                                                       + CodeGeneratorAdapterStrategy.generateName(port));
                */
                 // End FIXME rodiers
                } else {
                    code += _updateOffset(i, rate);
                    code += getCodeGenerator().comment(_eol + "\n....End updateOffset...."
                                                       + CodeGeneratorAdapterStrategy.generateName(_port));
                }
            }
            return code;
        }



        private CodeGeneratorAdapterStrategy.Channel _getChannel(int channelNumber) {
            return new CodeGeneratorAdapterStrategy.Channel((ptolemy.actor.IOPort)
                    _port, channelNumber);
        }

        /**
         * Generate the expression that represents the offset in the generated
         * code.
         * @param offsetString The specified offset from the user.
         * @param channel The referenced port channel.
         * @param isWrite Whether to generate the write or read offset.
         * @return The expression that represents the offset in the generated code.
         * @exception IllegalActionException If there is problems getting the port
         *  buffer size or the offset in the channel and offset map.
         */
        private String _generateOffset(String offsetString, int channel, boolean isWrite)
        throws IllegalActionException {

            boolean dynamicReferencesAllowed = ((BooleanToken) getCodeGenerator().allowDynamicMultiportReference
                    .getToken()).booleanValue();
            boolean padBuffers = ((BooleanToken) getCodeGenerator().padBuffers
                    .getToken()).booleanValue();


            //if (MpiPNDirector.isLocalBuffer(port, channel)) {
            //    int i = 1;
            //}


            // When dynamic references are allowed, any input ports require
            // offsets.
            if (dynamicReferencesAllowed && _port.isInput()) {
                if (!(_port.isMultiport() || getBufferSize() > 1)) {
                    return "";
                }
            } else {
                if (!(getBufferSize() > 1)) {
                    return "";
                }
            }

            String result = null;
            Object offsetObject;

            // Get the offset index.
            if (isWrite) {
                offsetObject = getWriteOffset(channel);
            } else {
                offsetObject = getReadOffset(channel);
            }

            if (!offsetString.equals("")) {
                // Specified offset.

                String temp = "";
                if (offsetObject instanceof Integer && _isInteger(offsetString)) {

                    int offset = ((Integer) offsetObject).intValue()
                    + (Integer.valueOf(offsetString)).intValue();

                    offset %= getBufferSize(channel);
                    temp = Integer.toString(offset);
                    /*
                     int divisor = getBufferSize(sinkPort,
                     sinkChannelNumber);
                     temp = "("
                     + getWriteOffset(sinkPort,
                     sinkChannelNumber) + " + "
                     + channelAndOffset[1] + ")%" + divisor;
                     */

                } else {
                    // FIXME: We haven't check if modulo is 0. But this
                    // should never happen. For offsets that need to be
                    // represented by string expression,
                    // getBufferSize(_port, channelNumber) will always
                    // return a value at least 2.

                    //              if (MpiPNDirector.isLocalBuffer(_port, channel)) {
                    //              temp = offsetObject.toString();
                    //              temp = MpiPNDirector.generateFreeSlots(_port, channel) +
                    //              "[" + MpiPNDirector.generatePortHeader(_port, channel) + ".current]";
                    //              } else
                    if (padBuffers) {
                        int modulo = getBufferSize(channel) - 1;
                        temp = "(" + offsetObject.toString() + " + " + offsetString
                        + ")&" + modulo;
                    } else {
                        int modulo = getBufferSize(channel);
                        temp = "(" + offsetObject.toString() + " + " + offsetString
                        + ")%" + modulo;
                    }
                }

                result = "[" + temp + "]";

            } else {
                // Did not specify offset, so the receiver buffer
                // size is 1. This is multiple firing.

                if (offsetObject instanceof Integer) {
                    int offset = ((Integer) offsetObject).intValue();

                    offset %= getBufferSize(channel);

                    result = "[" + offset + "]";
                } else {

                    //              if (MpiPNDirector.isLocalBuffer(_port, channel)) {
                    //              result = offsetObject.toString();
                    //              result = MpiPNDirector.generateFreeSlots(_port, channel) +
                    //              "[" + MpiPNDirector.generatePortHeader(_port, channel) + ".current]";
                    //              } else
                    if (padBuffers) {
                        int modulo = getBufferSize(channel) - 1;
                        result = "[" + offsetObject + "&" + modulo + "]";
                    } else {
                        result = "[" + offsetObject + "%"
                        + getBufferSize(channel) + "]";
                    }
                }
            }
            return result;
        }


//        private Receiver _getReceiver(String offset, int channel, ptolemy.actor.IOPort port) {
//            Receiver[][] receivers = port.getReceivers();
//
//            // For output ports getReceivers always returns an empty table.
//            if (receivers.length == 0) {
//                return null;
//            }
//
//            int staticOffset = -1;
//            Receiver receiver = null;
//            if (offset != null) {
//                try {
//                    staticOffset = Integer.parseInt(offset);
//                    receiver = receivers[channel][staticOffset];
//                } catch (Exception ex) {
//                    staticOffset = -1;
//                }
//            }
//
//            if (staticOffset == -1) {
//                // FIXME: Assume all receivers are the same type for the channel.
//                // However, this may not be true.
//                assert (receivers.length > 0);
//                receiver = receivers[channel][0];
//            }
//            return receiver;
//        }

        /**
         * Return true if the given string can be parse as an integer; otherwise,
         * return false.
         * @param numberString The given number string.
         * @return True if the given string can be parse as an integer; otherwise,
         *  return false.
         */
        private boolean _isInteger(String numberString) {
            try {
                Integer.parseInt(numberString);
                return true;
            } catch (NumberFormatException ex) {
                return false;
            }
        }

        /** Update the offset of the channel.
         *  @param channel The channel number of the channel to be offset.
         *  @param rate The firing rate of the port.
         *  @return The code that represents the offset to the channel,
         *  @exception IllegalActionException If thrown while getting a token,
         *  adapter, read offset or buffer size.
         */
        private String _updateOffset(int channel, int rate) throws IllegalActionException {
            StringBuffer code = new StringBuffer();
            boolean padBuffers = ((BooleanToken) getCodeGenerator().padBuffers
                    .getToken()).booleanValue();

            // Update the offset for each channel.
            if (getReadOffset(channel) instanceof Integer) {
                int offset = ((Integer) getReadOffset(channel))
                .intValue();
                if (getBufferSize(channel) != 0) {
                    offset = (offset + rate) % getBufferSize(channel);
                }
                setReadOffset(channel, Integer.valueOf(offset));
            } else { // If offset is a variable.
                String offsetVariable = (String) getReadOffset(channel);
                if (padBuffers) {
                    int modulo = getBufferSize(channel) - 1;
                    code.append(offsetVariable + " = (" + offsetVariable +
                            " + " + rate + ")&" + modulo + ";" + _eol);
                } else {
                    code.append(offsetVariable + " = (" + offsetVariable +
                            " + " + rate + ") % " +
                            getBufferSize(channel) + ";" + _eol);
                }
            }
            return code.toString();

        }

        /** A HashMap that keeps track of the bufferSizes of each channel
         *  of the actor.
         */
        private HashMap<CodeGeneratorAdapterStrategy.Channel, Integer> _bufferSizes =
            new HashMap<CodeGeneratorAdapterStrategy.Channel, Integer>();

        //TODO rodiers
        private IOPort _port;

        /** A HashMap that keeps track of the read offsets of each input channel of
         *  the actor.
         */
        private HashMap<CodeGeneratorAdapterStrategy.Channel, Object> _readOffsets =
            new HashMap<CodeGeneratorAdapterStrategy.Channel, Object>();

        /** A HashMap that keeps track of the write offsets of each input channel of
         *  the actor.
         */
        private HashMap<CodeGeneratorAdapterStrategy.Channel, Object> _writeOffsets =
            new HashMap<CodeGeneratorAdapterStrategy.Channel, Object>();

    }

    protected class Ports
    {

        /**Generate the expression that represents the offset in the generated
         * code.
         *  @param port The given port.
         * @param offset The specified offset from the user.
         * @param channel The referenced port channel.
         * @param isWrite Whether to generate the write or read offset.
         * @return The expression that represents the offset in the generated code.
         * @exception IllegalActionException If there is problems getting the port
         *  buffer size or the offset in the channel and offset map.
         */
        public String generateOffset(IOPort port, String offset, int channel, boolean isWrite) throws IllegalActionException {
            return _getPortInfo(port).generateOffset(offset, channel, isWrite);

        }
        /** Get the buffer size of channel of the port.
         *  @param channelNumber The number of the channel that is being set.
         *  @param port The given port.
         *  @return return The size of the buffer.
         * @exception IllegalActionException
         *  @see #setBufferSize(IOPort, int, int)
         */
        public int getBufferSize(IOPort port, int channelNumber) throws IllegalActionException {
            return _getPortInfo(port).getBufferSize(channelNumber);
        }

        /**
         * Return the buffer size of a given port, which is the maximum of
         * the bufferSizes of all channels of the given port.
         * @param port The given port.
         * @return The buffer size of the given port.
         * @exception IllegalActionException If the
         * {@link #getBufferSize(IOPort, int)} method throws it.
         * @see #setBufferSize(IOPort, int, int)
         */
        public int getBufferSize(IOPort port) throws IllegalActionException {
            return _getPortInfo(port).getBufferSize();
        }

        /** Get the read offset in the buffer of a given channel from which a token
         *  should be read. The channel is given by its containing port and
         *  the channel number in that port.
         *  @param inputPort The given port.
         *  @param channelNumber The given channel number.
         *  @return The offset in the buffer of a given channel from which a token
         *   should be read.
         *  @exception IllegalActionException Thrown if the adapter class cannot
         *   be found.
         *  @see #setReadOffset(IOPort, int, Object)
         */
        public Object getReadOffset(IOPort inputPort, int channelNumber) throws IllegalActionException {
            return _getPortInfo(inputPort).getReadOffset(channelNumber);
        }

        /** Get the write offset in the buffer of a given channel to which a token
         *  should be put. The channel is given by its containing port and
         *  the channel number in that port.
         *  @param port The given port.
         *  @param channelNumber The given channel number.
         *  @return The offset in the buffer of a given channel to which a token
         *   should be put.
         *  @exception IllegalActionException Thrown if the adapter class cannot
         *   be found.
         *  @see #setWriteOffset(IOPort, int, Object)
         */
        public Object getWriteOffset(IOPort port, int channelNumber) throws IllegalActionException {
            return _getPortInfo(port).getWriteOffset(channelNumber);
        }

        /** Initialize the offsets.
         *  @param port The given port.
         *  @return The code to initialize the offsets.
         */
        public String initializeOffsets(IOPort port) throws IllegalActionException {
            return _getPortInfo(port).initializeOffsets();
        }

        /** Set the buffer size of channel of the port.
         *  @param channelNumber The number of the channel that is being set.
         *  @param bufferSize The size of the buffer.
         *  @see #getBufferSize(IOPort, int)
         */
        public void setBufferSize(IOPort port, int channelNumber, int bufferSize) {
            _getPortInfo(port).setBufferSize(channelNumber, bufferSize);
        }


        /** Set the read offset in a buffer of a given channel from which a token
         *  should be read.
         *  @param port The given port.
         *  @param channelNumber The given channel.
         *  @param readOffset The offset to be set to the buffer of that channel.
         *  @exception IllegalActionException Thrown if the adapter class cannot
         *   be found.
         *  @see #getReadOffset(IOPort, int)
         */
        public void setReadOffset(IOPort port, int channelNumber,
                Object readOffset) throws IllegalActionException {
            _getPortInfo(port).setReadOffset(channelNumber, readOffset);
        }

        /** Set the write offset in a buffer of a given channel to which a token
         *  should be put.
         *  @param port The given port.
         *  @param channelNumber The given channel.
         *  @param writeOffset The offset to be set to the buffer of that channel.
         *  @exception IllegalActionException If
         *   {@link #setWriteOffset(IOPort, int, Object)} method throws it.
         *  @see #getWriteOffset(IOPort, int)
         */
        public void setWriteOffset(IOPort port, int channelNumber,
                Object writeOffset) throws IllegalActionException {
            _getPortInfo(port).setWriteOffset(channelNumber, writeOffset);
        }

        // Update the write offset of the [multiple] connected ports.
        public String updateConnectedPortsOffset(IOPort port, int rate) throws IllegalActionException {
            return _getPortInfo(port).updateConnectedPortsOffset(rate);
        }


        /** Update the read offset.
         *  @param port The given port.
         *  @param rate  The rate of the channels.
         *  @return The offset.
         */
        public String updateOffset(IOPort port, int rate)
                throws IllegalActionException {
            return _getPortInfo(port).updateOffset(rate);
        }

        /**
         * @param port
         */
        private PortInfo _getPortInfo(IOPort port) {
            PortInfo info = null;
            if (!_portInfo.containsKey(port)) {
                info = new PortInfo(port);
                _portInfo.put(port, info);
            } else {
                info = _portInfo.get(port);
            }
            return info;
        }

        // TODO rodiers: use a HashSet i.s.o. HashMap
        private Map<IOPort, PortInfo> _portInfo = new HashMap<IOPort, PortInfo>();
    }

    protected Ports _ports = new Ports();
}
