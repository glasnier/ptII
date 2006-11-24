/* A helper class for ptolemy.actor.lib.gui.XYPlotter
 
 Copyright (c) 2006 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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

package ptolemy.codegen.c.actor.lib.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// XYPlotter

/**
 A helper class for ptolemy.actor.lib.gui.XYPlotter.
 
 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Yellow (zgang) 
 @Pt.AcceptedRating Red (zgang)
 */
public class XYPlotter extends CCodeGeneratorHelper {

    /** Constructor method for the XYPlotter helper.
     *  @param actor the associated actor.
     */
    public XYPlotter(ptolemy.actor.lib.gui.XYPlotter actor) {
        super(actor);
    }

    /** Generate fire code.
     *  @return The generated code.
     *  @exception IllegalActionException If the code stream encounters 
     *   errors in processing the specified code blocks.
     */
    public String generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generateFireCode());
        ptolemy.actor.lib.gui.XYPlotter actor 
                = (ptolemy.actor.lib.gui.XYPlotter) getComponent();       
        int width = actor.inputX.getWidth();
        ArrayList args = new ArrayList();
        for (int i = width - 1; i >= 0; i--) {
            args.clear();
            args.add(new Integer(i));
            code.append(_generateBlockCode("plotBlock", args));          
        }

        return code.toString();
    }
    
    /** Generate initialize code.
     *  @return The generated code.
     *  @exception IllegalActionException If the code stream encounters 
     *   errors in processing the specified code blocks.
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        
        String ptIIDir = StringUtilities.getProperty("ptolemy.ptII.dir");
        ArrayList args = new ArrayList();
        args.add(ptIIDir);
        code.append(_generateBlockCode("createJVMBlock", args));
        
        code.append(super.generateInitializeCode());
        ptolemy.actor.lib.gui.XYPlotter actor 
                = (ptolemy.actor.lib.gui.XYPlotter) getComponent();       
        
        // If the plot has not been created, we need to creat the plot
        // to get the configuration.
        if (actor.plot == null) {
            actor.initialize();
        }
        
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        String header = "<!DOCTYPE plot PUBLIC \"-//UC Berkeley//DTD PlotML 1//EN\"" + _eol
            + "\"http://ptolemy.eecs.berkeley.edu/xml/dtd/PlotML_1.dtd\">";
        printWriter.write(header);
        printWriter.write(_eol + "<plot>" + _eol);
        actor.plot.writeFormat(printWriter);
        printWriter.write("</plot>" + _eol);
        
        BufferedReader reader = new BufferedReader
                (new StringReader(stringWriter.toString()));
        String line = null;
        StringBuffer result = new StringBuffer();
        try {
            while((line = reader.readLine()) != null) {
                line = line.trim();
                if (line != "") {
                    line = line.replace("\"", "\\\\\"");
                    result.append("\"" + line + "\\\\n\"" + "\n");
                }
            }
        } catch (IOException ex) {
            
        }
        
        args.clear();
        args.add(result.toString());
        code.append(_generateBlockCode("configureBlock", args));
        
        return code.toString();
    }
    
    /** Generate the wrapup code. 
     *  @return The generated wrapup code.
     *  @exception IllegalActionException 
     */
    public String generateWrapupCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generateWrapupCode());
        // FIXME: this is a dumb way to leave the plot window open 
        // when the program runs to the end. I need to figure out a
        // better way. Or is there any?
        code.append("char $actorSymbol(temp)[80];" + _eol);
        code.append("printf(\"close plot window to exit...\");" + _eol);
        code.append("scanf(\"%s\",$actorSymbol(temp));" + _eol);
        return processCode(code.toString());
    }
    

    /** Get the header files needed by the code generated for the
     *  XYPlotter actor.
     *  @return A set of strings that are names of the header files
     *   needed by the code generated for the XYPlotter actor.
     *  @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        // FIXME: This only works under windows.
        String javaHome = StringUtilities.getProperty("java.home");
        javaHome = javaHome.replace('\\', '/');
        int index = javaHome.lastIndexOf("jre");
        javaHome = javaHome.substring(0, index);
        getCodeGenerator().addInclude("-I\"" + javaHome + "include\"");
        getCodeGenerator().addInclude("-I\"" + javaHome + "include/win32\"");
        
        String ptIIDir = StringUtilities.getProperty("ptolemy.ptII.dir");
        getCodeGenerator().addLibrary("-L\"" + ptIIDir + "/ptolemy/codegen/c\"");
        getCodeGenerator().addLibrary("-ljvm");
        
        Set files = new HashSet();
        files.add("<jni.h>");
        return files;
    }
}
