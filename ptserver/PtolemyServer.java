/* Ptolemy server which manages the broker, servlet, and simulations.

 Copyright (c) 2011 The Regents of the University of California.
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

package ptserver;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.XMLFormatter;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import ptolemy.kernel.util.IllegalActionException;
import ptserver.control.IServerManager;
import ptserver.control.ServerManager;
import ptserver.control.Ticket;

///////////////////////////////////////////////////////////////////
//// PtolemyServer

/** Launch the message broker, enabling users to start, pause, resume,
 *  and stop simulations through the servlet, and create independently 
 *  executing simulations upon request.
 *   
 *  @author Justin Killian
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (jkillian)
 *  @Pt.AcceptedRating Red (jkillian)
 */
public class PtolemyServer implements IServerManager {

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The ResourceBundle containing configuration parameters. 
     */
    public static final ResourceBundle CONFIG = ResourceBundle
            .getBundle("ptserver.PtolemyServerConfig");

    /** Start the Logger used to write messages to the specified log file. 
     */
    public static final Logger LOGGER;

    static {
        Logger logger = null;
        FileHandler logFile = null;

        try {
            logger = Logger.getLogger(PtolemyServer.class.getSimpleName());
            logFile = new FileHandler(CONFIG.getString("LOG_FILENAME"), true);
            logFile.setFormatter(new XMLFormatter());

            logger.addHandler(logFile);
            logger.setLevel(Level.ALL);
        } catch (SecurityException e) {
            throw new ExceptionInInitializerError(e);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }

        LOGGER = logger;
    }

    /** Initialize the server, launch the broker and servlet processes, and
     *  wait from simulation requests.  The following optional command line 
     *  switches may be used with their accompanying value: -servlet_path, 
     *  -servlet_port, -broker_path, and -broker_port.  The port numbers must
     *  integers, the broker path must be the path to the MQTT broker executable on
     *  the local machine, and the servlet path is the virtual directory (including
     *  the preceding slash) that the Ptolemy servlet will run at.
     *  
     *  For example:
     *  java -classpath ptserver.PtolemyServer -broker_path /usr/sbin/mosquitto -broker_port 1883
     *  
     *  @param args Optional command line arguments.
     *  @exception Exception If the server was unable to parse the 
     *  command line configuration values.
     */
    public static void main(String[] args) throws Exception {
        // Create the singleton.
        _instance = new PtolemyServer();

        try {
            // Set all provided configuration parameters.
            for (int i = 0; i < args.length; i++) {
                if ((args[i].startsWith("-")) && (i + 1 < args.length)) {
                    if (args[i].toLowerCase() == "-servlet_path") {
                        _instance._setServletPath(args[i + 1]);
                    } else if (args[i].toLowerCase() == "-servlet_port") {
                        _instance
                                ._setServletPort(Integer.parseInt(args[i + 1]));
                    } else if (args[i].toLowerCase() == "-broker_path") {
                        _instance._setBrokerPath(args[i + 1]);
                    } else if (args[i].toLowerCase() == "-broker_port") {
                        _instance._setBrokerPort(Integer.parseInt(args[i + 1]));
                    }
                }
            }

            // Launch the servlet container and broker.
            _instance.startup();
        } catch (NumberFormatException e) {
            PtolemyServer.LOGGER.log(Level.WARNING,
                    "Port must be a numeric value.");
            throw new Exception("Port must be a numeric value.");
        } catch (IllegalStateException e) {
            PtolemyServer.LOGGER.log(Level.SEVERE,
                    "Unable to start the servlet or broker.");
            throw new Exception("Unable to start the servlet or broker.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         constructor                       ////

    /** Create an instance of the Ptolemy server.  This class is a singleton 
     *  so only one instance should ever exist at a time.  Child process are
     *  initialized for the servlet (synchronous command handler) and the 
     *  MQTT message broker (asynchronous simulation data).
     *  @exception Exception If the server was unable to load the default 
     *  configuration from the resource file.
     */
    public PtolemyServer() throws Exception {
        try {
            _servletPath = CONFIG.getString("SERVLET_PATH");
            _servletPort = Integer.parseInt(CONFIG.getString("SERVLET_PORT"));
            _brokerPath = CONFIG.getString("BROKER_PATH");
            _brokerPort = Integer.parseInt(CONFIG.getString("BROKER_PORT"));
            _requests = new ConcurrentHashMap<Ticket, SimulationTask>();
            _executor = Executors.newCachedThreadPool();
        } catch (NumberFormatException e) {
            PtolemyServer.LOGGER.log(Level.WARNING,
                    "Unable to properly load configuration file.");
            throw new Exception("Unable to properly load configuration file.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the singleton instance of the Ptolemy server.  If it does not
     *  already exist, the singleton will be instantiated using the
     *  default configuration.
     *  @return The PtolemyServer singleton.
     * @throws Exception 
     */
    public static PtolemyServer getInstance() throws Exception {
        if (_instance == null) {
            synchronized (PtolemyServer.class) {
                if (_instance == null) {
                    // Create singleton with default configuration.
                    _instance = new PtolemyServer();

                    // Launch the servlet container and broker.
                    _instance.startup();
                }
            }
        }

        return _instance;
    }

    /** Shut down the thread associated with the user's ticket. 
     *  @param ticket Ticket reference to the simulation request.
     *  @exception IllegalActionException If the server was unable to 
     *  destroy the simulation thread.
     */
    public void close(Ticket ticket) throws IllegalActionException {
        try {
            if ((ticket == null) || (!_requests.containsKey(ticket))) {
                throw new Exception("Invalid ticket: " + ticket.getTicketID());
                //TODO: create InvalidTicketException
            }

            _requests.get(ticket).getManager().finish();
            _requests.remove(ticket);
        } catch (Exception e) {
            PtolemyServer.LOGGER.log(
                    Level.SEVERE,
                    String.format("%s: %s", ticket.getTicketID(),
                            e.getMessage()));
            throw new IllegalActionException(e.getMessage());
        }
    }

    /** Get the broker operating port.
     *  @return The port on which the MQTT broker operates
     */
    public int getBrokerPort() {
        return this._brokerPort;
    }

    /** Get a listing of the models available on the server in either the
     *  database or the local file system.
     *  @exception IllegalActionException If there was a problem discovering
     *  available models.
     *  @return An array of URL references to the available model files.
     */
    public URL[] getModelListing() throws IllegalActionException {
        // TODO Add code to query the local database or current directory
        // in the file system to find available model files.
        return new URL[0];
    }

    /** Get the servlet operating port.
     *  @return The port on which to run the servlet container.
     */
    public int getServletPort() {
        return this._servletPort;
    }

    /** Get the number of simulation requests on the server.
     *  @return The size of the hash map of simulation tasks.
     */
    public int numberOfSimulations() {
        return _requests.size();
    }

    /** Open a model with the provided model URL and wait for the
     *  user to request the execution of the simulation.
     *  @param url The path to the model file
     *  @exception IllegalActionException If the model fails to load 
     *  from the provided URL.
     *  @return The user's reference to the simulation task
     */
    public Ticket open(String url) throws IllegalActionException {
        Ticket ticket = null;

        try {
            // Generate a unique ticket for the request.
            ticket = Ticket.generateTicket(url);
            while (_requests.contains(ticket)) {
                ticket = Ticket.generateTicket(url);
            }

            // Save the simulation request.
            _requests.put(ticket, new SimulationTask(ticket));
        } catch (Exception e) {
            PtolemyServer.LOGGER.log(
                    Level.SEVERE,
                    String.format("%s: %s", ticket.getTicketID().toString(),
                            e.getMessage()), e);
            throw new IllegalActionException(null, e, e.getMessage());
        }

        return ticket;
    }

    /** Pause the execution of the selected simulation.
     *  @param ticket The ticket reference to the simulation request.
     *  @exception IllegalActionException If the server was unable to 
     *  pause the running simulation.
     */
    public void pause(Ticket ticket) throws IllegalActionException {
        try {
            if ((ticket == null) || (!_requests.containsKey(ticket))) {
                throw new Exception("Invalid ticket: " + ticket.getTicketID());
                //TODO: create InvalidTicketException
            }

            _requests.get(ticket).getManager().pause();
        } catch (Exception e) {
            PtolemyServer.LOGGER.log(
                    Level.SEVERE,
                    String.format("%s: %s", ticket.getTicketID(),
                            e.getMessage()));
            throw new IllegalActionException(e.getMessage());
        }
    }

    /** Resume the execution of the selected simulation.
     *  @param ticket The ticket reference to the simulation request.
     *  @exception IllegalActionException If the server was unable to 
     *  resume the execution of the simulation.
     */
    public void resume(Ticket ticket) throws IllegalActionException {
        try {
            if ((ticket == null) || (!_requests.containsKey(ticket))) {
                throw new Exception("Invalid ticket: " + ticket.getTicketID());
                //TODO: create InvalidTicketException
            }

            _requests.get(ticket).getManager().resume();
        } catch (Exception e) {
            PtolemyServer.LOGGER.log(
                    Level.SEVERE,
                    String.format("%s: %s", ticket.getTicketID(),
                            e.getMessage()));
            throw new IllegalActionException(e.getMessage());
        }
    }

    /** Shut down supporting processes and destroy active simulation threads.
     *  @exception Exception If the servlet container or broker cannot be stopped.
     */
    public void shutdown() throws Exception {
        // Shut down the MQTT broker.
        if (_broker != null) {
            _broker.destroy();
            _broker = null;
        }

        // Shut down the servlet.
        if (_servletHost != null) {
            _servletHost.stop();
            _servletHost.destroy();
            _servletHost = null;
        }

        // Clear all requests in the hash table.
        if (_requests != null) {
            _requests.clear();
            _requests = null;
        }

        // Shut down the thread pool and destroy the singleton.
        if (_executor != null) {
            _executor.shutdown();
        }

        _instance = null;
    }

    /** Start the execution of the selected simulation.
     *  @param ticket The ticket reference to the simulation request.
     *  @exception IllegalActionException If the server was unable to 
     *  start the simulation.
     */
    public void start(Ticket ticket) throws IllegalActionException {
        try {
            if ((ticket == null) || (!_requests.containsKey(ticket))) {
                throw new Exception("Invalid ticket: " + ticket.getTicketID());
                //TODO: create InvalidTicketException
            }

            _executor.execute(_requests.get(ticket));
        } catch (Exception e) {
            PtolemyServer.LOGGER.log(
                    Level.SEVERE,
                    String.format("%s: %s", ticket.getTicketID(),
                            e.getMessage()));
            throw new IllegalActionException(e.getMessage());
        }
    }

    /** Initialize the servlet and the broker for use in communication
     *  with the Ptolemy server.
     */
    public void startup() {
        // Launch the broker process.
        _broker = null;
        try {
            String[] commands = new String[] { _brokerPath, "-p",
                    Integer.toString(_brokerPort) };

            ProcessBuilder builder = new ProcessBuilder(commands);
            builder.redirectErrorStream(true);

            _broker = builder.start();
        } catch (IOException e) {
            PtolemyServer.LOGGER.log(Level.SEVERE,
                    "Unable to spawn MQTT broker process at '" + _brokerPath
                            + "' on port " + Integer.toString(_brokerPort)
                            + ".");
            throw new IllegalStateException(
                    "Unable to spawn MQTT broker process at '" + _brokerPath
                            + "' on port " + Integer.toString(_brokerPort)
                            + ".", e);
        }

        // Launch the Jetty servlet container.
        _servletHost = null;
        try {
            _servletHost = new Server(_servletPort);
            ServletContextHandler context = new ServletContextHandler(
                    _servletHost, "/", ServletContextHandler.SESSIONS);
            ServletHolder container = new ServletHolder(ServerManager.class);
            context.addServlet(container, _servletPath);

            _servletHost.setHandler(context);
            _servletHost.start();
        } catch (Exception e) {
            PtolemyServer.LOGGER.log(Level.SEVERE,
                    "Unable to spawn servlet container at '" + _servletPath
                            + "' on port " + Integer.toString(_servletPort)
                            + ".");
            throw new IllegalStateException(
                    "Unable to spawn servlet container at '" + _servletPath
                            + "' on port " + Integer.toString(_servletPort)
                            + ".", e);
        }
    }

    /** Stop the execution of the selected simulation.
     *  @param ticket The ticket reference to the simulation request.
     *  @exception IllegalActionException If the server was unable to 
     *  stop the simulation.
     */
    public void stop(Ticket ticket) throws IllegalActionException {
        try {
            if ((ticket == null) || (!_requests.containsKey(ticket))) {
                throw new Exception("Invalid ticket: " + ticket.getTicketID());
                //TODO: create InvalidTicketException
            }

            _requests.get(ticket).getManager().stop();
        } catch (Exception e) {
            PtolemyServer.LOGGER.log(
                    Level.SEVERE,
                    String.format("%s: %s", ticket.getTicketID(),
                            e.getMessage()));
            throw new IllegalActionException(e.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Set the path to the broker executable.
     *  @param brokerPath Path to the broker executable.
     */
    private void _setBrokerPath(String brokerPath) {
        this._brokerPath = brokerPath;
    }

    /** Set the broker operating port.
     *  @param brokerPort Port on which the MQTT broker operates.
     */
    private void _setBrokerPort(int brokerPort) {
        this._brokerPort = brokerPort;
    }

    /** Set the servlet virtual directory.
     *  @param servletPath Virtual path of the servlet.
     */
    private void _setServletPath(String servletPath) {
        this._servletPath = servletPath;
    }

    /** Set the servlet operating port.
     *  @param servletPort Port on which to run the servlet container.
     */
    private void _setServletPort(int servletPort) {
        this._servletPort = servletPort;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private static PtolemyServer _instance;
    private Process _broker;
    private Server _servletHost;
    private ExecutorService _executor;
    private ConcurrentHashMap<Ticket, SimulationTask> _requests;
    private String _brokerPath;
    private int _brokerPort;
    private String _servletPath;
    private int _servletPort;
}
