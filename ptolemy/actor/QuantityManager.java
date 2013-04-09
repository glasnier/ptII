/* An interface for objects that can intervene in communication between actors.

@Copyright (c) 2010-2013 The Regents of the University of California.
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
package ptolemy.actor;

import java.util.List;

import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;

/** An interface for objects that can intervene in communication between actors.
 *  A quantity manager creates receivers that wrap the receivers created by a
 *  {@link Director}, delegating (or not) to those receivers as it sees fit.
 *  If an {@link IOPort} references a quantity manager, then calls to any
 *  receiver in that port will be handled instead by a receiver created by
 *  the quantity manager.
 *  <p>
 *  For example, a quantity manager could intervene in communications to take
 *  into account shared resources. For example, it could delay delivery of any
 *  tokens to the original receiver (that created by the director) until the
 *  resources become available for the transport to occur.
 *
 *  @author Patricia Derler, Edward A. Lee
 *  @version $Id$
 *  @since Ptolemy II 9.0
 *  @Pt.ProposedRating Red (cxh)
 *  @Pt.AcceptedRating Red (cxh)
 */
public interface QuantityManager {

    /** Create a receiver to mediate a communication via the specified receiver.
     *  @param receiver Receiver whose communication is to be mediated.
     *  @return A new receiver.
     *  @exception IllegalActionException If the receiver cannot be created.
     */
    public Receiver createIntermediateReceiver(Receiver receiver)
            throws IllegalActionException;
    
    /** Return the list of Attributes that can be specified per port with default
     *  values for the specified port.
     *  @param container The container parameter.
     *  @param port The port.
     *  @return List of attributes.
     *  @exception IllegalActionException Thrown if attributeList could not be created.
     */
    public List<Attribute> getPortAttributeList(Parameter container, Port port) throws IllegalActionException;

    /** Reset the QuantityManager.
     */
    public void reset();

    /** Take the specified token and mediate communication to the specified
     *  receiver. An implementer could, for example, delay the communication
     *  to account for resource contention. Or, it could make a record of the
     *  energy consumed by the communication.
     *  @param source Receiver that sent the token.
     *  @param receiver The receiver for which this quantity manager is mediating
     *   communication.
     *  @param token The token for the communication to mediate.
     *  @exception IllegalActionException If the token cannot be sent.
     */
    public void sendToken(Receiver source, Receiver receiver, Token token)
            throws IllegalActionException; 

    /** Set an attribute for a given port.
     *  @param port The port. 
     *  @param attribute The new attribute or the attribute containing a new value.
     *  @exception IllegalActionException Thrown if attribute could not be updated.
     */
    public void setPortAttribute(Port port, Attribute attribute) throws IllegalActionException;

}
