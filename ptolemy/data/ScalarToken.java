/* A base class for tokens that contain a scalar.

 Copyright (c) 1997- The Regents of the University of California.
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

package ptolemy.data;

import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// ScalarToken
/**
A base class for tokens that contain a scalar.
It provides interface for type conversion among different scalar types.
This class is not abstract to allow an instance to be created. This
is required by the actor package where the type of an IOPort is
represented by an instance of a specific token class.

@author Yuhong Xiong, Mudit Goel
$Id$
*/
public class ScalarToken extends Token {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the value in the token as a byte.
     *  In this base class, we just throw an exception.
     *  @exception IllegalActionException alway thrown.
     */
    public byte byteValue()
	    throws IllegalActionException {
	throw new IllegalActionException("ScalarToken.byteValue: This base "
		+ "class does not contain a value.");
    }

    // Return the value in the token as a Complex.
    // In this base class, we just throw an exception.
    // @exception IllegalActionException always thrown.
    //
    // FIXME: restore this method after the Complex class is available.
    //    public Complex complexValue()
    //	      throws IllegalActionException {
    //	throw new IllegalActionException("ScalarToken.byteValue: This base "
    //      	+ "class does not contain a value.");
    // }

    /** Return the value in the token as a double.
     * In this base class, we just throw an exception.
     * @exception IllegalActionException always thrown.
     */
    public double doubleValue()
	    throws IllegalActionException {
	throw new IllegalActionException("ScalarToken.doubleValue: This base "
		+ "class does not contain a value.");
    }

    // Return the value in the token as a Fix.
    // In this base class, we just throw an exception.
    // @exception IllegalActionException always thrown.
    //
    // FIXME: restore this method after the Fix class is available.
    //    public Fix fixValue()
    //        throws IllegalActionException {
    //	throw new IllegalActionException("ScalarToken.fixValue: This base "
    //		+ "class does not contain a value.");
    // }

    /** Return the value in the token as an int.
     * In this base class, we just throw an exception.
     * @exception IllegalActionException always thrown.
     */
    public int intValue()
	    throws IllegalActionException {
	throw new IllegalActionException("ScalarToken.intValue: This base "
		+ "class does not contain a value.");
    }

    /** Return the value in the token as a long integer.
     * In this base class, we just throw an exception.
     * @exception IllegalActionException always thrown.
     */
    public long longValue()
	    throws IllegalActionException {
	throw new IllegalActionException("ScalarToken.intValue: This base "
		+ "class does not contain a value.");
    }
}

