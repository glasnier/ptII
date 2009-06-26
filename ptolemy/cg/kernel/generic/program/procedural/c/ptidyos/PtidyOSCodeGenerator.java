/* Code generator for PtidyOS, which is implemented on C.

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
package ptolemy.cg.kernel.generic.program.procedural.c.ptidyos;

import ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.ptides.kernel.PtidesBasicDirector;
import ptolemy.cg.kernel.generic.program.procedural.c.CCodeGenerator;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
////PtidyOSCodeGenerator

/** Base class for PtidyOS code generator.
 *
 *  @author Jia Zou, Jeff C. Jensen
 *  @version $Id$
 *  @since Ptolemy II 7.1
 *  @Pt.ProposedRating red (jiazou)
 *  @Pt.AcceptedRating red (jiazou)
 */
public class PtidyOSCodeGenerator extends CCodeGenerator {
    /** Create a new instance of the PtidyOS code generator.
     *  @param container The container.
     *  @param name The name of the PtidyOS code generator.
     *  @exception IllegalActionException If the super class throws the
     *   exception or error occurs when setting the file path.
     *  @exception NameDuplicationException If the super class throws the
     *   exception or an error occurs when setting the file path.
     */
    public PtidyOSCodeGenerator(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    private final static int SOURCE_FILE = 0;

    private final static int ASSEMBLY_FILE = 1;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate code. This is the main entry point. The generateCode()
     *  of the super method is called. After which code is generated
     *  for the assembly file.
     *  @param code The code buffer into which to generate the code.
     *  @return The return value of the last subprocess that was executed.
     *  or -1 if no commands were executed.
     *  @exception KernelException If a type conflict occurs or the model
     *  is running.
     */
    public int generateCode(StringBuffer code) throws KernelException {
        _generateFile = SOURCE_FILE;
        int result = super.generateCode(code);
        _generateFile = ASSEMBLY_FILE;
        _generateAssemblyFile();
        return result;
    }

    /**
     * Return the name of the output file.
     * @return The output file name.
     * @exception IllegalActionException If there is problem resolving
     *  the string value of the generatorPackage parameter.
     */
    public String getOutputFilename() throws IllegalActionException {
        String extension = null;
        if (_generateFile == SOURCE_FILE) {
            extension = ".c";
        } else if (_generateFile == ASSEMBLY_FILE) {
            extension = ".S";
        }

        return _sanitizedModelName + extension;
    }

    /** Generate code for the assembly file.
     *  @throws IllegalActionException if getAdaptor throws it.
     * 
     */
    protected void _generateAssemblyFile() throws IllegalActionException {
        PtidesBasicDirector director = (PtidesBasicDirector) getAdapter(getContainer());
        _writeCode(director.generateAsseblyFile());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /**
     * The index of the list of code files to generate code for. 0 refers
     * to the c file, and 1 refers to the startup .S code file.
     */
    protected int _generateFile;

}
