/* A singleton factory class that creates websocket clients.

 Copyright (c) 1997-2014 The Regents of the University of California.
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

package org.ptolemy.ptango.lib.websocket;

import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;

///////////////////////////////////////////////////////////////////
//// PtolemyWebSocketClientFactory

/** A singleton factory class that creates websocket clients.    
 *
 *  @author Elizabeth Latronico
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (ltrnc)
 *  @Pt.AcceptedRating Red (ltrnc)
*/

public class PtolemyWebSocketClientFactory {
        
        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    //// 
 
        /** Get the factory instance, creating a new one if none created yet. 
         * @return The websocket client factory instance.
         */
        public static PtolemyWebSocketClientFactory getInstance() {
                return PtolemyWebSocketClientFactoryHolder.INSTANCE;
        }
        
        /** Create a new WebSocket client.
         * @return A new WebSocketClient.
         * @throws Exception If the WebSocketClientFactory cannot be started.
         */
        public WebSocketClient newWebSocketClient() throws Exception {
            if (_webSocketClientFactory.isStopped()) {
                _webSocketClientFactory.start();
            }
            
            return _webSocketClientFactory.newWebSocketClient();
        }
        
        ///////////////////////////////////////////////////////////////////
        ////                         private methods                   //// 
        
        /** Create a new WebSocketClientFactory and start it.  The private 
         *  constructor prevents instantiation from other classes.
         */
        private PtolemyWebSocketClientFactory() { 
            _webSocketClientFactory = new WebSocketClientFactory();
            // _clients = new Hashtable();
        }
 
        /** The Holder is loaded on the first execution of getInstance() 
        * or the first access to INSTANCE, allowing on-demand loading since
        * not all models use websockets.
        */
        private static class PtolemyWebSocketClientFactoryHolder { 
                private static final PtolemyWebSocketClientFactory INSTANCE = 
                        new PtolemyWebSocketClientFactory();
        }
        
        ///////////////////////////////////////////////////////////////////
        ////                         private variables                 //// 
        
        /** A set of paths and clients.  One client is allocated per path.
         *  This allows multiple Ptolemy actors to see data from a single
         *  socket connection.
         */
        // TODO:  Implement client sharing for remote connections.  
        // Otherwise, each actor will have a separate connection to the
        // external site, meaning a reader won't receive a response message
        // from a message a writer wrote
        // (we could alternatively use a single actor that boths sends  
        // messages and receives responses)
        // private Hashtable<URI, WebSocketClient> _clients;
        
        /** A factory for creating WebSocketClients. */
        private static WebSocketClientFactory _webSocketClientFactory;
}