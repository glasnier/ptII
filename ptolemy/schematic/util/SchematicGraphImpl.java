
package ptolemy.schematic.util;

import java.util.*;
import diva.util.*;
import diva.graph.model.*;

/**
 * A graph implementation which creates and edits elements of the type
 * SchematicEntity, SchematicTerminal, Schematic, and SchematicLink
 * respectively.
 *
 * @author Steve Neuendorffer
 * @version $Id$
 * @rating Red
 */
public class SchematicGraphImpl implements GraphImpl {
    /**
     * Add a node to the given graph.
     */
    public final void addNode(Node n, Graph parent) {
        try {
            if(parent instanceof SchematicEntity) {
                SchematicTerminal terminal = (SchematicTerminal)n;
                ((SchematicEntity)parent).addTerminal(terminal);
            }
            //        else if(parent instanceof SchematicRelation) {
            //SchematicTerminal terminal = (SchematicTerminal)n;
            //    ((SchematicEntity)parent).addTerminal(terminal);
            //}
            else if(parent instanceof Schematic) {
                if(n instanceof SchematicEntity) {
                    SchematicEntity entity = (SchematicEntity)n;
                    ((Schematic)parent).addEntity(entity);
                } else { 
                    SchematicTerminal terminal = (SchematicTerminal)n;
                    ((Schematic)parent).addTerminal(terminal);
                }                    
            } 
            /*else {
              throw new InternalErrorException("parent cannot contain node");
              }*/
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new GraphException(e.getMessage());
        }
    }

    /**
     * Return a new instance of a SchematicEntity with the given
     * semantic object.
     */
    public CompositeNode createCompositeNode(Object semanticObject) {
	CompositeNode n = new SchematicEntity();
        n.setSemanticObject(semanticObject);
        return n;
    }
    
    /**
     * Return a new instance of a SchematicTerminal with the given semantic
     * object.
     */
    public final Node createNode(Object semanticObject) {
	Node n = new SchematicTerminal();
        n.setSemanticObject(semanticObject);
        return n;
    }
    
    /**
     * Return a new instance of a Schematic with the given semantic
     * object.
     */
    public final Graph createGraph(Object semanticObject) {
	Graph g = new Schematic();
        g.setSemanticObject(semanticObject);
        return g;
    }
    
    /**
     * Return a new instance of a SchematicLink with the given semantic
     * object.
     */
    public final Edge createEdge(Object semanticObject) {
	Edge e = new SchematicLink();
        e.setSemanticObject(semanticObject);
        return e;
    }

    /**
     * Remove a node from the graph that contains it. 
     */
    public final void removeNode(Node n) {
        try {
            Graph parent = n.getParent();
            
            if(parent == null) {
                String err = "Trying to remove node with no parent";
                throw new GraphException(err);
            }
            
            if(parent instanceof SchematicEntity) {
                SchematicTerminal bn = (SchematicTerminal)n;
                ((SchematicEntity)parent).
                    removeTerminal((SchematicTerminal) bn);
            }
            else {
                SchematicEntity bn = (SchematicEntity)n;
                ((Schematic)parent).removeEntity((SchematicEntity) bn);
            }
        }
        catch (Exception e) {
            throw new GraphException(e.getMessage());
        }
    }

    /**
     * Set the edge's head to the given node.
     */
    public final void setEdgeHead(Edge e, Node head) {
        SchematicLink be = (SchematicLink)e;
        SchematicTerminal bh = (SchematicTerminal)head;
        be.setTo(bh);
    }
    
    /**
     * Set the edge's tail to the given node.
     */
    public final void setEdgeTail(Edge e, Node tail) {
        SchematicLink be = (SchematicLink)e;
        SchematicTerminal bt = (SchematicTerminal)tail;
        be.setFrom(bt);
    }
}
