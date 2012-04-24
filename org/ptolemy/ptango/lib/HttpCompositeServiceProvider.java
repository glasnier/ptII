package org.ptolemy.ptango.lib;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.AsyncContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.PtolemyFrame;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.basic.ExportParameters;
import ptolemy.vergil.basic.export.html.WebExportable;
import ptolemy.vergil.basic.export.html.WebExporter;

/* An actor that handles an HttpRequest to the given path.

 Copyright (c) 1997-2011 The Regents of the University of California.
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

/** An actor that handles an HttpRequest to the given path.  This actor creates
 *  a servlet, registers this servlet with the WebServer during preinitialize(),
 *  and displays its content at the specified path when a request is received.
 *  
 *  @author ltrnc
 *  @version $Id$
 *  @since Ptolemy II 9.0
 *  @Pt.ProposedRating Red (ltrnc)
 *  @Pt.AcceptedRating Red (ltrnc)
 *  @see org.ptolemy.ptango.WebServer
 */

public class HttpCompositeServiceProvider extends TypedCompositeActor 
        implements HttpService, WebExporter{
    public HttpCompositeServiceProvider(CompositeEntity container, String name)
    throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        path = new StringParameter(this, "path");
        path.setExpression("/*");
        setRelativePath(path.getExpression().toString());
        
        inputPage = new FileParameter(this, "inputPage");
        inputPage.setExpression("/pages/index.html");
        
        outputPage = new FileParameter(this, "outputPage");
        outputPage.setExpression("/pages/output.html");   
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////
    
    /** WebExportables call this method to add web content to the page
     *  returned by the HttpCompositeServiceProvider.  
     *  Copied from ptolemy.vergil.basic.export.html.ExportHTMLAction 
     *  
     *  Add HTML content at the specified position.
     *  The position is expected to be one of "head", "start", "end",
     *  or anything else. In the latter case, the value
     *  of the position attribute is a filename
     *  into which the content is written.
     *  If <i>onceOnly</i> is true, then if identical content has
     *  already been added to the specified position, then it is not
     *  added again.
     *  @param position The position for the content.
     *  @param onceOnly True to prevent duplicate content.
     *  @param content The content to add.
     *  @see ptolemy.vergil.basic.export.html.ExportHTMLAction
     */
    // FIXME:  Move some web export functionality out of ptolemy.vergil?  
    // Would like this to be able to run without GUI.
    public void addContent(String position, boolean onceOnly, String content) {
        List <StringBuffer> contents = _contents.get(position);

        if (contents == null) {
            contents = new LinkedList<StringBuffer>();
            _contents.put(position, contents);
        }
        if (onceOnly) {
            // Check to see whether contents are already present.
            if (contents.contains(content)) {
                return;
            }
        }
        contents.add(new StringBuffer(content));     
    }
    
    /** Returns the relative path that this HttpService is mapped to. 
     * 
     * @return The relative path that this HttpService is mapped to.
     */
    public URI getRelativePath() {
        return _URIpath;
    }

    /** Creates and returns an HttpServlet which is used to handle requests that
     *  arrive at the given path.
     * 
     * @return An HttpServlet to handle requests. 
     */
    public HttpServlet getServlet() {
        return new HttpServiceServlet();
    }
    
    /** Initialize the data structures into which web content is collected.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        
        // TODO:  Other data structures from unimplemented methods for
        // WebExporter interface
        _contents = new HashMap<String,List<StringBuffer>>();
        _contents.put("head", new LinkedList<StringBuffer>());
        _contents.put("start", new LinkedList<StringBuffer>());
        _contents.put("end", new LinkedList<StringBuffer>());
    }
    
    /** This actor may be fired if there is an HttpResponse (contained in the
     *  AsyncContext _asyncContext) to write to (i.e. a request has been 
     *  received meaning _asyncContext is not null).
     */
   
    public boolean prefire() throws IllegalActionException{
        // First check other prefire() conditions.  Then, check if AsyncContext
        // has been set so we can write a response
        if (!super.prefire() || _asyncContext == null) {
            return false;
        }
       
        return true;
    }
    
    
    /** Set the relative path that this actor will receive requests to.  This is
     *  set here in case the path does not conform to URI syntax rules.  If the
     *  path does not conform, an IllegalActionException is thrown.
     *
     *  @exception IllegalActionException If the path is invalid URI syntax
     */
    
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        
        if (!path.getExpression().isEmpty()) {
            setRelativePath(path.getExpression().toString());
        }         
    }
    
    /** Invoke superclass postfire() and return true.  This actor is always 
     *  ready to be fired again.
     */
    
    // FIXME:  Is it OK to always return true here?  Had problems without this.
    public boolean postfire() throws IllegalActionException {
        super.postfire();
        return true;
    }
    
    /** Fire the actor and create an output page.
     * 
     * @exception IllegalActionException If there is no HttpResponse to write
     *  the output page to.
     */
    public void fire() throws IllegalActionException{
        super.fire();
        if (_asyncContext == null) {
            throw new IllegalActionException(this, "No response page for " +
            		"the HttpService to write to");
        }
        
        try {
            // Write the response
            // Do this after all ports are read, because if there is 
            // problem with any port value, we want to return an error
            // Currently this prints a list of port names and values
            
            // This should always be true since the doPost() method from the 
            // HttpServlet creates the asynchronous context, but just in case... 
            if (_asyncContext.getResponse() instanceof HttpServletResponse) {
                HttpServletResponse response  
                    = (HttpServletResponse)_asyncContext.getResponse();
                
                // TODO:  Right now, use output port names as variables.  Do 
                // we want to change this since WebSink is inside of this actor?
                // Should WebSource and WebSink be responsible for knowing the
                // output file to write to?  Weird for pages with multiple 
                // variables (or, allow multiple input / output ports per source
                // and sink?  One source, sink per page?)
                
                // TODO:  Right now, WebSink is contained by 
                // HttpCompositeService because we read its value in 
                // the fire() of HttpCompositeService which must occur 
                // after the fire() of WebSink, which would not happen in some
                // MoCs if the WebSink is outside of of the HttpCompositeService
                
                // Find all WebSink actors
                // These should be connected to a relation which connects to an
                // output port
                // FIXME:  Possible to obtain token from output port directly?
                // Tried (with no luck):
                 //DoubleToken outputToken = 
                // (DoubleToken) ((TypedIOPort) outputPortList().get(0)).getInside(0);
                HashMap resultsMap = new HashMap<String, String>();
                    
                // Loop through all output ports with connected WebSinks 
                // to collect the names and values
                // The values will be printed as strings to the response page,
                // so the type is not an issue for an HTML output page
                // This might change for other output formats?  
                if (!outputPortList().isEmpty()) {
                    Iterator outputs = outputPortList().iterator();
                    
                    while (outputs.hasNext()) {
                        TypedIOPort output = (TypedIOPort) outputs.next();                       
                        Iterator relations = 
                            output.insideRelationList().iterator();
                        while (relations.hasNext()) {
                            Relation relation = (Relation) relations.next();
                            Iterator sinks = 
                                relation.linkedObjectsList().iterator();
                            while (sinks.hasNext()) {
                                NamedObj sink = (NamedObj) sinks.next();
                                if (sink.getContainer() != null && 
                                        sink.getContainer() instanceof WebSink){
                                    WebSink webSink = 
                                        (WebSink) sink.getContainer();
                                    
                                    resultsMap.put(output.getName()
                                            , webSink.value.getValueAsString());                                 
                                }
                            }
                        }
                    }
                } 
                
                /* This is no longer an error - could have content 
                 * defined by WebExportables
                 */
                /*
                else {
                    _writeError(response, HttpServletResponse.SC_NO_CONTENT, 
                       "No output variables are defined for this web service.");
                }
                */
                
                // Get web content from all contained WebExportables
                _addAllContent(this);
                
                // Write the response page
                response.setContentType("text/javascript");
                response.setStatus(HttpServletResponse.SC_OK);
                PrintWriter writer = response.getWriter();      
                
                String fileName = outputPage.getExpression().toString();
                if (fileName.isEmpty()) {
                    // Assume /output.html
                    fileName = "/output.html";
                }
                
                // TODO:  Move to attributeChanged() method?
                if (fileName.charAt(0) != '/') {
                    fileName = "/" + fileName;
                }
                
                 if (_servletContext != null) {               
   
                    BufferedReader reader = 
                      new BufferedReader(
                      new InputStreamReader(_servletContext
                           .getResourceAsStream(fileName)));                         
                    
                    String line;
                    
                    // TODO:  This finds all the output variables line by line
                    // Would be nice to insert variables as a group and use
                    // e.g. Javascript to print the values in the correct spots
                    // But, couldn't get Javascript to work as part of
                    // an HttpResponse - worked OK loading page from file
                    // FIXME:  All of this assumes nice line spacing in the 
                    // source file...
                    String key;
                    Iterator keys;
                    while((line = reader.readLine()) != null) {
                        // Insert elements from WebExportables into <head>
                        // Assumes file contains <head> </head>
                        if (line.contains("</head>")) {
                            _printHTML(writer, "head");
                        } 
                        
                        // Insert elements from WebExportables into <body>
                        if (line.contains("<body>")) {
                            _printHTML(writer, "start");
                        } 
                        
                        // Insert elements from WebExportables before </body>
                        if (line.contains("</body>")) {
                            _printHTML(writer, "end");
                        } 
                        
                        // Insert Javascript for variables 
                        if (!resultsMap.keySet().isEmpty()) {
                            keys = resultsMap.keySet().iterator();
                            while(keys.hasNext()) {
                                key = (String) keys.next();
                                if (line.contains("<div id=\"" + key + "\">")) {
                                    line = "<div id=\"" + key + "\">" + 
                                        (String) resultsMap.get(key) + "</div>";
                                    resultsMap.remove(key);
                                    break;
                                } 
                            }
                        }
                        
                        writer.println(line);
                        }
                           
                    reader.close();
                    writer.flush();
                    }
                }
                
            // Mark the AsyncContext as complete to show we are done processing
            // this HttpRequest.  Set it to null so prefire() will return false
            // since this actor should not be fired until receiving another
            // request
            _asyncContext.complete();
            _asyncContext = null;
            
        } catch(IOException e){throw new IllegalActionException(this,"Problem" +
        		"writing the response page.");
        }
    }
    

    
    /** Set the relative path that this HttpService is mapped to, and ensure
     * that this path conforms to URI naming conventions.
     * See:  http://docs.oracle.com/javase/1.4.2/docs/api/java/net/URI.html
     * 
     * @param path The relative path that this HtppService is mapped to.
     */
    public void setRelativePath(String path) throws IllegalActionException {
        
        try {
            _URIpath = URI.create(path);
        } catch(NullPointerException e){
            throw new IllegalActionException(this, "Path cannot be null.");
        } catch(IllegalArgumentException e2){
            // TODO:  Even better - transform this path to a legal one.
            throw new IllegalActionException(this, "Path contains illegal " +
                        "characters according to URI definition");
        }
    }
    
    /** Set the relative path that this HttpService is mapped to.
     * 
     * @param path The relative path that this HttpService is mapped to.
     */
    public void setRelativePath(URI path) {
        _URIpath = path;
        
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    
    /** The file containing the HTML input form to display.
     */
    public FileParameter inputPage;
    
    /** The file containing the HTML output page to display.
     */
    public FileParameter outputPage;
    
    /** The relative URL to map this servlet to. 
     */
    public StringParameter path; 
    
    ///////////////////////////////////////////////////////////////////
    ////                     private methods                      ////
   
    /** Finds all contained WebExportables and gets their web content.
     * 
     * @param container The container to search for WebExportables
     * @exception If something is wrong with the specification of the outside 
     * content
     */
    private void _addAllContent(NamedObj container) 
        throws IllegalActionException {
        if (container instanceof WebExportable) {
            ((WebExportable) container).provideOutsideContent(this);
        } else {
            Iterator objects = container.containedObjectsIterator();
            while (objects.hasNext()) {
                _addAllContent((NamedObj) objects.next());
            }
        }  
    }
    
    /** Copied from ptolemy.vergil.basic.export.html.ExportHTMLAction 
     *  Print the HTML in the _contents structure corresponding to the
     *  specified position to the specified writer. Each item in the
     *  _contents structure is written on one line.
     *  @param writer The writer to print to.
     *  @param position The position.
     *  @see ptolemy.vergil.basic.export.html.ExportHTMLAction
     */
    private void _printHTML(PrintWriter writer, String position) {
        if (_contents != null) {
            List<StringBuffer> contents = _contents.get(position);
            if (contents != null) {
                for (StringBuffer content : contents) {
                    writer.println(content);
                }
            }
        }
    }

    /** Write an error message to the given HttpServletResponse.
     *
     * @param response The HttpServletResponse to write the message to
     * @param responseCode  The HTTP response code for the message.  Should be
     * one of HttpServletResponse.X
     * @param message  The error message to write
     */
    private void _writeError(HttpServletResponse response, int responseCode,
            String message)
        throws IOException {
        response.setContentType("text/html");
        response.setStatus(responseCode);

        PrintWriter writer = response.getWriter();                
            
        writer.println("<!DOCTYPE html>");
        writer.println("<html>");
        writer.println("<body>");
        writer.println(message);
        writer.println("</body>");
        writer.println("</html>");                   
        writer.flush();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private variables                     ////
    
    /** A servlet that handles HTTP requests.  It maps any input and output 
     * ports to information from the input and output web pages.
     */
    private class HttpServiceServlet extends HttpServlet
    {
        public HttpServiceServlet(){}

        protected void doGet(HttpServletRequest request, 
                HttpServletResponse response) 
                throws ServletException, IOException
        {

            // Display a page with an input form and the results of any
            // prior computation, if any
            
            // TODO:  Return a "view" like in the Spring framework to support 
            // multiple representations
            
            // TODO:  A get request is ambiguous here - do we want to get the
            // input form or the results of the last computation?
            
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            PrintWriter writer = response.getWriter();                
            
            
            String fileName = inputPage.getExpression().toString();
            if (fileName.isEmpty()) {
                // Assume /index.html
                fileName = "/index.html";
            }
            
            // TODO:  Move to attributeChanged() method?
            if (fileName.charAt(0) != '/') {
                fileName = "/" + fileName;
            }
            
            BufferedReader reader = 
                new BufferedReader(new InputStreamReader(getServletContext()
                    .getResourceAsStream(fileName))); 
            
            String line;
            while((line = reader.readLine()) != null) {
                writer.println(line);
            }

            reader.close();
            writer.flush();
        }
        
        /** Respond to an HTTP POST request.  The actor reads information from 
         *  the web form into its input ports, executes the contained model, 
         *  and produces a web page with the result.
         *  
         *  HTML forms only allow GET and POST:
         *  http://objectmix.com/php/728829-anyone-have-html-snippet-example-http-method-%3D-put.html
         *  
         *  However it seems like PUT might be more RESTful?
         *  See slides 18-22:  
         *  http://chess.eecs.berkeley.edu/ptango/wiki/uploads/Main/ComposingRESTInterfaceJOpera.pdf
         *  
         *  @param HttpServletRequest request  The HTTP request
         *  @param HttpServletResponse response The HTTP response
         */
        protected void doPost(HttpServletRequest request, 
                HttpServletResponse response) 
                throws ServletException, IOException
        {
            // Map request parameters to input ports
            Iterator inputPorts = inputPortList().iterator();
            while (inputPorts.hasNext()) {
                TypedIOPort inputPort = (TypedIOPort) inputPorts.next();
                
                Iterator connectedPorts 
                    = inputPort.connectedPortList().iterator();
                
                while(connectedPorts.hasNext()) {
                    Port sourcePort = (Port) connectedPorts.next();
                    
                    // Check if this port is connected to a WebSource actor
                    // This indicates that the port should retrieve data from the
                    // HttpRequest
                    if (sourcePort.getContainer() instanceof WebSource) {
                        // Note that the argument to request.getParameter() 
                        // is case sensitive!
                        
                        if (request.getParameter(inputPort.getName()) != null) {
                            String data = 
                                request.getParameter(inputPort.getName());
                            
                            // FIXME: Require that ports have declared types?
                            // Otherwise, how to tell what the type is?
                            // For now, assume double
                            // Extract a token from the request and broadcast 
                            // this to the input port's receivers
                            try {
                                // Collect tokens from the request, set the 
                                // values of the WebSource actors and request
                                // the WebSources to fire themselves
                                DoubleToken token = new DoubleToken(data);
                                ((WebSource) sourcePort.getContainer())
                                    .value.setToken(token);
                                
                                WebSource webSource = 
                                    (WebSource) sourcePort.getContainer();
                              
                                if (!webSource.requestFiringNow()) {
                                    _writeError(response, 
                                            HttpServletResponse.SC_BAD_REQUEST,
                                            "Problem with data " +
                                    	"value for " + inputPort.getName()); 
                                }                                                
                                
                            } catch(IllegalActionException e){
                                _writeError(response, 
                                        HttpServletResponse.SC_BAD_REQUEST, 
                                        "Problem with data value for " 
                                        + inputPort.getName());                            
                            }
                
                        } else {
                            _writeError(response, 
                                    HttpServletResponse.SC_BAD_REQUEST, 
                                    "Problem with data value for " 
                                    + inputPort.getName());
                        }
                    }
                    break;
                }
            }
            
            // Store a copy of the response so the parent class can
            // write to it and ask the director to fire the parent
            // Store the servlet context so the fire() method can use it
            try {
                _asyncContext = request.startAsync();   
                _servletContext = getServletContext();
                // Only need this if no input ports?  If no WebSources?
                if (inputPortList().isEmpty()) {
                    getDirector()
                        .fireAtCurrentTime(HttpCompositeServiceProvider.this);
                }
              
            } catch(IllegalActionException e){
                throw new IOException("Can't fire HttpService");
            }

        }       
    }  
    
    
    /** An asynchronous context to store the HttpResponse to write the response
     * to.  The context is started in doPost() when a POST request is received
     * in order to store the HttpResponse so that the fire() method may write
     * something to it later.
     * References:
     * https://blogs.oracle.com/enterprisetechtips/entry/asynchronous_support_in_servlet_3
     * http://code.google.com/p/jquery-stream/wiki/EchoExample
     */
    private AsyncContext _asyncContext;
    
    /** Content added by position. */
    private HashMap<String,List<StringBuffer>> _contents;
    
    /** A copy of the servlet context, which is set in the doPost() method of
     *  HttpServiceServlet and used in the fire() method of the parent to 
     *  read the output page file. 
     */
    private ServletContext _servletContext;
    
    /** The URI for the relative path from the "path" parameter.  
     *  A URI is used here to make sure the "path" parameter conforms to
     *  all of the URI naming conventions. 
     */
    private URI _URIpath;

    @Override
    public boolean defineAreaAttribute(NamedObj object, String attribute,
            String value, boolean overwrite) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public ExportParameters getExportParameters() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PtolemyFrame getFrame() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setTitle(String title, boolean showInHTML) {
        // TODO Auto-generated method stub
        
    }
    
}