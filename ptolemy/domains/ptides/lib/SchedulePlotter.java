
package ptolemy.domains.ptides.lib;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PlotEffigy;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.domains.ptides.kernel.PtidesDirector;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.plot.Plot;

//////////////////////////////////////////////////////////////////////////
//// SchedulePlotter

/**
 * @author Patricia Derler
 */
public class SchedulePlotter extends Attribute implements ScheduleListener {
    /** Construct a factory with the specified container and name.
     *  @param container The container.
     *  @param name The name of the factory.
     *  @exception IllegalActionException If the factory is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public SchedulePlotter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"130\" height=\"40\" "
                + "style=\"fill:blue\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
                + "Double click to\nplot the schedule.</text></svg>");

        new SchedulePlotterEditorFactory(this, "_editorFactory");

        SingletonParameter hide = new SingletonParameter(this, "_hideName");
        hide.setToken(BooleanToken.TRUE);
        hide.setVisibility(Settable.EXPERT);
        

        // FIXME: This seems wrong.
        if (container instanceof CompositeActor) {
            // We need to check if the container is a CompositeActor
            // because the reference to SchedulePlotter in tmentities.xml
            // is not a CompositeActor
            Director director = ((CompositeActor) container).getDirector();

            if (!(director instanceof PtidesDirector)) {
                throw new IllegalActionException("Director '" + director
                        + "' is not a PtidesDirector, so adding a SchedulePlotter "
                        + "makes no sense");
            }

            ((PtidesDirector) director).addScheduleListener(this);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////        public variables and parameters                    ////

    /** The plotter. */
    public Plot plot;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    public void event(final Actor node, final Actor actor, double time, int scheduleEvent) {
    	if (plot == null)
    		return;
    	double actorY = getYForActor(node, actor);
    	double nodeY = getYForNode(node);
    	double x = time;
    	int actorDataset = nodeActorStrings.indexOf(node.getName() + ": " + actor.getName());
    	int nodeDataSet = nodeActorStrings.indexOf(node.getName());
        if (scheduleEvent == ScheduleListener.START || scheduleEvent == ScheduleListener.STOP) {
        	plot.addPoint(actorDataset, x, actorY, scheduleEvent == ScheduleListener.STOP);
        	plot.addPoint(nodeDataSet, x, nodeY, scheduleEvent == ScheduleListener.STOP);
        	nodeActive[nodes.indexOf(node)] = scheduleEvent == ScheduleListener.START;
        }
        else if (scheduleEvent == ScheduleListener.TRANSFERINPUT) {
        	plot.addPoint(nodeDataSet, x, nodeY, false || nodeActive[nodes.indexOf(node)]);
        	plot.addPoint(nodeDataSet, x - 0.05, nodeY - 0.05, false);
        	plot.addPoint(nodeDataSet, x, nodeY, true);
        }
        else if (scheduleEvent == ScheduleListener.TRANSFEROUTPUT) {
        	plot.addPoint(nodeDataSet, x, nodeY, false || nodeActive[nodes.indexOf(node)]);
        	plot.addPoint(nodeDataSet, x + 0.05, nodeY - 0.05, false);
        	plot.addPoint(nodeDataSet, x, nodeY, true);
        } else if (scheduleEvent == ScheduleListener.MISSEDEXECUTION) {
        	plot.addPoint(actorDataset, x - 0.05, actorY + 0.05, false);
        	plot.addPoint(actorDataset, x + 0.05, actorY - 0.05, true);
        	plot.addPoint(actorDataset, x - 0.05, actorY - 0.05, false);
        	plot.addPoint(actorDataset, x + 0.05, actorY + 0.05, true);
        }
        plot.fillPlot();
        plot.repaint();
    }
    
    private ArrayList nodes = new ArrayList();
    private boolean[] nodeActive;
    private Hashtable nodeActors = new Hashtable();
    private ArrayList nodeActorStrings = new ArrayList();
    
    private double getYForNode(final Actor node) {
//    	if (!nodes.contains(node)) {
//    		nodes.add(node);    	
//    		nodeActors.put(node, new ArrayList());
//    	} 
    	return nodes.indexOf(node)*2;
    }
    
    private double getYForActor(final Actor node, final Actor actor) {
//    	if (!nodes.contains(node)) {
//    		nodes.add(node);    	
//    		nodeActors.put(node, new ArrayList());
//    	} 
//    	if (!((List)nodeActors.get(node)).contains(actor)) {
//    		((List)nodeActors.get(node)).add(actor);
//    		nodeActorStrings.add(node.getName() + ": " + actor.getName());
////    		Runnable doAddPoint = new Runnable() {
////                public void run() {
////                	System.out.print("node + nodeactor " + nodes.indexOf(node));
////                	System.out.print(((List)nodeActors.get(node)));
////                	System.out.println(((List)nodeActors.get(node)).indexOf(actor));
////                    plot.addLegend(nodeActorStrings.indexOf(node.getName() + ": " + actor.getName()), node.getName() + ": " + actor.getName());
////                }
////            };
////
////            synchronized (plot) {
////                plot.deferIfNecessary(doAddPoint);
////            }
//    	}
    	return nodes.indexOf(node)*2 + ((double)((ArrayList)nodeActors.get(node)).indexOf(actor)) / ((ArrayList)nodeActors.get(node)).size() + 0.1;
    }

	public void initialize(Hashtable nodesActors) {
		nodes.clear();
		plot.clear(false);
		plot.clearLegends();
		nodes.addAll(nodesActors.keySet());
		nodeActive = new boolean[nodes.size()];
		this.nodeActors = nodesActors;
		for (int i = 0; i < nodes.size(); i++) {
			Actor node = (Actor) nodes.get(i);
			nodeActorStrings.add(node.getName());
			if (plot == null)
				return;
			plot.addLegend(nodeActorStrings.indexOf(node.getName()), node.getName());
			List actors = (List) nodeActors.get(node);
			for (int j = 0; j < actors.size(); j++) {
				Actor actor = (Actor) actors.get(j);
				nodeActorStrings.add(node.getName() + ": " + actor.getName());
		        plot.addLegend(nodeActorStrings.indexOf(node.getName() + ": " + actor.getName()), node.getName() + ": " + actor.getName());
			}
		}
		plot.doLayout();
	}
	
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private HashMap _taskMap;

    private ArrayList _taskState;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Factory that creates the schedule plotter. */
    public class SchedulePlotterEditorFactory extends EditorFactory {
        // This class needs to be public for shallow code generation.
        /**
         * Constructs a SchedulePlotter$SchedulePlotterEditorFactory object.
         *
         *  @param container The container.
         *  @param name The name of the factory.
         *  @exception IllegalActionException If the factory is not of an
         *   acceptable attribute for the container.
         *  @exception NameDuplicationException If the name coincides with
         *   an attribute already in the container.
         */
        public SchedulePlotterEditorFactory(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        /** Create an editor for configuring the specified object with the
         *  specified parent window.
         *  @param object The object to configure.
         *  @param parent The parent window, or null if there is none.
         */
        public void createEditor(NamedObj object, Frame parent) {
            try {
                Configuration configuration = ((TableauFrame) parent)
                        .getConfiguration();

                NamedObj container = object.getContainer();

                _taskMap = new HashMap();
                _taskState = new ArrayList();
                plot = new Plot();
                plot.setTitle("Ptides Schedule");
                plot.setButtons(true);
                plot.setMarksStyle("dots");

                // We put the plotter as a sub-effigy of the toplevel effigy,
                // so that it closes when the model is closed.
                Effigy effigy = Configuration.findEffigy(toplevel());
                PlotEffigy schedulePlotterEffigy = new PlotEffigy(effigy, container.uniqueName("schedulePlotterEffigy"));
                schedulePlotterEffigy.setPlot(plot);
                schedulePlotterEffigy.identifier.setExpression("TM Schedule");

                configuration.createPrimaryTableau(schedulePlotterEffigy);

                plot.setVisible(true);
            } catch (Throwable throwable) {
                throw new InternalErrorException(object, throwable,
                        "Cannot create Schedule Plotter");
            }
        }
    }

}
