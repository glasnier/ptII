/* An object that can read models from a URL.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.actor.gui;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;

//////////////////////////////////////////////////////////////////////////
//// ModelReader
/**
An object that can read models from a URL.
An application contains one instance of this class, and delegates
reading URLs to that instance.  The application is responsible for
registering the model with the ModelDirectory.

@author Edward A. Lee
@version $Id$
@see Application
@see ModelDirectory
*/
public class ModelReader extends CompositeEntity {

    /** Create a new reader in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this reader within the container.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public ModelReader(Application container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read the specified input URL, and return an instance of ModelProxy
     *  for the data at that URL.  The instance of ModelProxy is created
     *  in the same workspace as this model reader, but it is not given
     *  a name.
     *  @param base The base for relative file references, or null if
     *   there are no relative file references.
     *  @param in The input URL.
     *  @param key The key to use to uniquely identify the model.
     *  @exception Exception If the stream cannot be read.
     */
    // FIXME remove key.
    public ModelProxy read(URL base, URL in, String key) throws Exception {
        MoMLParser parser = new MoMLParser(new Workspace(), null);
        NamedObj toplevel = parser.parse(base, in.openStream());
        // Create a proxy for the model.
        PtolemyModelProxy proxy = new PtolemyModelProxy(workspace());
        proxy.setModel(toplevel);
        return proxy;
    }
}
