/* A token that contains a 2-D long matrix.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Green (neuendor@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/

package ptolemy.data;
import ptolemy.kernel.util.*;
import ptolemy.graph.CPO;
import ptolemy.math.LongMatrixMath;
import ptolemy.data.type.*;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.ASTPtRootNode;

//////////////////////////////////////////////////////////////////////////
//// LongMatrixToken
/**
A token that contains a 2-D long matrix.

@author Yuhong Xiong, Steve Neuendorffer
@version $Id$
@since Ptolemy II 0.2
*/
public class LongMatrixToken extends MatrixToken {

    /** Construct an LongMatrixToken with a one by one matrix. The
     *  only element in the matrix has value 0
     */
    public LongMatrixToken() {
        _rowCount = 1;
        _columnCount = 1;
        _value = new long[1][1];
        _value[0][0] = 0;
    }

    /** Construct a LongMatrixToken with the specified 2-D matrix.
     *  This method makes a copy of the matrix and stores the copy,
     *  so changes on the specified matrix after this token is
     *  constructed will not affect the content of this token.
     *  @exception IllegalActionException If the specified matrix
     *   is null.
     */
    public LongMatrixToken(long[][] value) throws IllegalActionException {
        this(value, DO_COPY);
    }

    /** Construct a LongMatrixToken with the specified 2-D matrix.
     *  If copy is DO_COPY, make a copy of the matrix and store the copy,
     *  so that changes on the specified matrix after this token is
     *  constructed will not affect the content of this token.
     *  If copy is DO_NOT_COPY, just reference the matrix (do not copy
     *  its contents). This saves some time and memory.
     *  The argument matrix should NOT be modified after this constructor
     *  is called to preserve immutability.
     *  <p>
     *  Since the DO_NOT_COPY option requires some care, this constructor
     *  is protected.
     *  @exception IllegalActionException If the specified matrix
     *   is null.
     */
    protected LongMatrixToken(final long[][] value, final int copy)
            throws IllegalActionException {
        if (value == null) {
            throw new IllegalActionException("LongMatrixToken: The "
                    + "specified matrix is null.");
        }
        _initialize(value, copy);
    }

    /** Construct a LongMatrixToken from the specified string.
     *  @param init A string expression of a 2-D long matrix.
     *  @exception IllegalActionException If the string does
     *   not contain a parsable 2-D long matrix.
     */
    public LongMatrixToken(String init) throws IllegalActionException {
        PtParser parser = new PtParser();
        ASTPtRootNode tree = parser.generateParseTree(init);
        Token token = tree.evaluateParseTree();
        if (token instanceof LongMatrixToken) {
            long[][] value = ((LongMatrixToken)token).longMatrix();
            _initialize(value, DO_COPY);
        } else {
            throw new IllegalActionException("A LongMatrixToken cannot be"
                    + " created from the expression '" + init + "'");
        }
    }

    /** Construct an LongMatrixToken from the specified array of
     *  tokens.  The tokens in the array must be scalar tokens
     *  convertible into integers.
     *  @param tokens The array of tokens, which must contains
     *  rows*columns ScalarTokens.
     *  @param rows The number of rows in the matrix to be created.
     *  @param columns The number of columns in the matrix to be
     *  created.
     *  @exception IllegalActionException If the array of tokens is
     *  null, or the length of the array is not correct, or if one of
     *  the elements of the array is null, or if one of the elements
     *  of the array cannot be losslessly converted to a long.
     */
    public LongMatrixToken(Token[] tokens, int rows, int columns)
            throws IllegalActionException {
        if (tokens == null) {
            throw new IllegalActionException("LongMatrixToken: The specified"
                    + " array is null.");
        }
        if (tokens.length != rows * columns) {
            throw new IllegalActionException("LongMatrixToken: The specified"
                    + " array is not of the correct length");
        }
        _rowCount = rows;
        _columnCount = columns;
        _value = new long[rows][columns];
        for (int i = 0; i < tokens.length; i++) {
            Token token = tokens[i];
            if (token instanceof ScalarToken) {
                _value[i / columns][i % columns] =
                    ((ScalarToken)token).longValue();
            } else {
                throw new IllegalActionException("LongMatrixToken: Element "
                        + i + " in the array with value " + token +
                        " is not a ScalarToken");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Convert the specified token into an instance of LongMatrixToken.
     *  This method does lossless conversion.
     *  If the argument is already an instance of LongMatrixToken,
     *  it is returned without any change. Otherwise, if the argument
     *  is below LongMatrixToken in the type hierarchy, it is converted to
     *  an instance of LongMatrixToken or one of the subclasses of
     *  LongMatrixToken and returned. If none of the above condition is
     *  met, an exception is thrown.
     *  @param token The token to be converted to a LongMatrixToken.
     *  @return A LongMatrixToken
     *  @exception IllegalActionException If the conversion cannot
     *   be carried out.
     */
    public static LongMatrixToken convert(Token token)
            throws IllegalActionException {
        if (token instanceof LongMatrixToken) {
            return (LongMatrixToken)token;
        }

        int compare = TypeLattice.compare(BaseType.LONG_MATRIX, token);
        if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
            throw new IllegalActionException(
                    notSupportedIncomparableConversionMessage(
                            token, "[long]"));
        }

        // try long
        //         compare = TypeLattice.compare(BaseType.LONG, token);
        //         if (compare == CPO.SAME || compare == CPO.HIGHER) {
        //             LongToken tem = LongToken.convert(token);
        //             long[][] result = new long[1][1];
        //             result[0][0] = tem.longValue();
        //             return new LongMatrixToken(result);
        //         }

        // try IntMatrix
        compare = TypeLattice.compare(BaseType.INT_MATRIX, token);
        if (compare == CPO.SAME || compare == CPO.HIGHER) {
            IntMatrixToken tem = IntMatrixToken.convert(token);
            long[][] result = tem.longMatrix();
            return new LongMatrixToken(result);
        }

        // The argument is below LongMatrixToken in the type hierarchy,
        // but I don't recognize it.
        throw new IllegalActionException(
                notSupportedConversionMessage(token, "[long]"));
    }

    /** Return true if the argument is an instance of LongMatrixToken
     *  of the same dimensions and the corresponding elements of the matrices
     *  are equal.
     *  @param object An instance of Object.
     *  @return True if the argument is an instance of LongMatrixToken
     *   of the same dimensions and the corresponding elements of the
     *   matrices are equal.
     */
    public boolean equals(Object object) {
        // This test rules out instances of a subclass.
        if (object.getClass() != LongMatrixToken.class) {
            return false;
        }

        LongMatrixToken matrixArgument = (LongMatrixToken)object;
        if (_rowCount != matrixArgument.getRowCount()) {
            return false;
        }
        if (_columnCount != matrixArgument.getColumnCount()) {
            return false;
        }

        long[][] matrix = matrixArgument.longMatrix();
        for (int i = 0; i < _rowCount; i++) {
            for (int j = 0; j < _columnCount; j++) {
                if (_value[i][j] != matrix[i][j]) {
                    return false;
                }
            }
        }

        return true;
    }

    /** Return the number of columns in the matrix.
     *  @return The number of columns in the matrix.
     */
    public int getColumnCount() {
        return _columnCount;
    }

    /** Return the element of the matrix at the specified
     *  row and column in a LongToken.
     *  @param row The row index of the desired element.
     *  @param column The column index of the desired element.
     *  @return A LongToken containing the matrix element.
     *  @exception ArrayIndexOutOfBoundsException If the specified
     *   row or column number is outside the range of the matrix.
     */
    public Token getElementAsToken(int row, int column)
            throws ArrayIndexOutOfBoundsException {
        return new LongToken(_value[row][column]);
    }

    /** Return the element of the contained matrix at the specified
     *  row and column.
     *  @param row The row index of the desired element.
     *  @param column The column index of the desired element.
     *  @return The long at the specified matrix entry.
     *  @exception ArrayIndexOutOfBoundsException If the specified
     *   row or column number is outside the range of the matrix.
     */
    public long getElementAt(int row, int column) {
        return _value[row][column];
    }

    /** Return the Type of the tokens contained in this matrix token.
     *  This must be a type representing a scalar token.
     *  @return BaseType.LONG.
     */
    public Type getElementType() {
        return BaseType.LONG;
    }

    /** Return the number of rows in the matrix.
     *  @return The number of rows in the matrix.
     */
    public int getRowCount() {
        return _rowCount;
    }

    /** Return the type of this token.
     *  @return BaseType.LONG_MATRIX
     */
    public Type getType() {
        return BaseType.LONG_MATRIX;
    }

    /** Return a hash code value for this token. This method returns the
     *  sum of the elements, casted to integer.
     *  @return A hash code value for this token.
     */
    public int hashCode() {
        long code = 0;
        for (int i = 0; i < _rowCount; i++) {
            for (int j = 0; j < _columnCount; j++) {
                code += _value[i][j];
            }
        }

        return (int)code;
    }

    /** Return the content in the token as a 2-D long matrix.
     *  The returned matrix is a copy so the caller is free to
     *  modify it.
     *  @return A 2-D long matrix.
     */
    public long[][] longMatrix() {
        long[][] matrix = new long[_rowCount][_columnCount];
        for (int i = 0; i < _rowCount; i++) {
            for (int j = 0; j < _columnCount; j++) {
                matrix[i][j] = _value[i][j];
            }
        }
        return matrix;
    }

    /** Return a new Token representing the left multiplicative
     *  identity. The returned token contains an identity matrix
     *  whose dimensions are the same as the number of rows of
     *  the matrix contained in this token.
     *  @return A new LongMatrixToken containing the left multiplicative
     *   identity.
     */
    public Token one() {
        long[][] result = new long[_rowCount][_rowCount];
        for (int i = 0; i < _rowCount; i++) {
            for (int j = 0; j < _rowCount; j++) {
                result[i][j] = 0;
            }
            result[i][i] = 1;
        }
        try {
            return new LongMatrixToken(result);
        } catch (IllegalActionException illegalAction) {
            // should not happen
            throw new InternalErrorException("LongMatrixToken.one: "
                    + "Cannot create identity matrix.");
        }
    }

    /** Return a new Token representing the right multiplicative
     *  identity. The returned token contains an identity matrix
     *  whose dimensions are the same as the number of columns of
     *  the matrix contained in this token.
     *  @return A new LongMatrixToken containing the right multiplicative
     *   identity.
     */
    public Token oneRight() {
        long[][] result = new long[_columnCount][_columnCount];
        for (int i = 0; i < _columnCount; i++) {
            for (int j = 0; j < _columnCount; j++) {
                result[i][j] = 0;
            }
            result[i][i] = 1;
        }

        try {
            return new LongMatrixToken(result);
        } catch (IllegalActionException illegalAction) {
            // should not happen
            throw new InternalErrorException("LongMatrixToken.oneRight: "
                    + "Cannot create identity matrix.");
        }
    }

    /** Return a new Token representing the additive identity.
     *  The returned token contains a matrix whose elements are
     *  all zero, and the size of the matrix is the same as the
     *  matrix contained in this token.
     *  @return A new LongMatrixToken containing the additive identity.
     */
    public Token zero() {
        long[][] result = new long[_rowCount][_columnCount];
        for (int i = 0; i < _rowCount; i++) {
            for (int j = 0; j < _columnCount; j++) {
                result[i][j] = 0;
            }
        }

        try {
            return new LongMatrixToken(result);
        } catch (IllegalActionException illegalAction) {
            // should not happen
            throw new InternalErrorException("LongMatrixToken.zero: "
                    + "Cannot create zero matrix.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a new token whose value is the value of the argument
     *  Token added to the value of this Token.  It is assumed that
     *  the type of the argument is LongMatrixToken.
     *  @param rightArgument The token to add to this token.
     *  @exception IllegalActionException If the units are not
     *  compatible, or this operation is not supported by the derived
     *  class.
     *  @return A new LongMatrixToken containing the result.
     */
    protected MatrixToken _add(MatrixToken rightArgument)
            throws IllegalActionException {
        LongMatrixToken convertedArgument = (LongMatrixToken)rightArgument;
        long[][] result = LongMatrixMath.add(
                convertedArgument._getInternalLongMatrix(), _value);
        return new LongMatrixToken(result);
    }

    /** Return a new token whose value is the value of the argument
     *  Token added to the value of each element of this Token. It is
     *  assumed that the type of the argument is the same as the type
     *  of each element of this class.
     *  @param rightArgument The token to add to this token.
     *  @exception IllegalActionException If this operation is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    protected MatrixToken _addElement(Token rightArgument)
            throws IllegalActionException {
        long scalar = ((LongToken)rightArgument).longValue();
        long[][] result = LongMatrixMath.add(_value, scalar);
        return new LongMatrixToken(result);
    }

    /** Return a new token whose elements are the result of dividing
     *  the elements of this token by the argument. It is
     *  assumed that the type of the argument is the same as the type
     *  of each element of this class.
     *  @param rightArgument The token that divides this token.
     *  @exception IllegalActionException If this operation is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    protected MatrixToken _divideElement(Token rightArgument)
            throws IllegalActionException {
        long scalar = ((LongToken)rightArgument).longValue();
        long[][] result = LongMatrixMath.divide(_value, scalar);
        return new LongMatrixToken(result);
    }

    /** Return a reference to the internal 2-D matrix of longs that
     *  represents this Token. Because no copying is done, the contents
     *  must NOT be modified to preserve the immutability of Token.
     *  @return A 2-D long matrix.
     */
    protected long[][] _getInternalLongMatrix() {
        return _value;
    }

	/** Test whether the value of this token is close to the first argument,
	 *  where "close" means that the distance between their elements is less than
	 *  or equal to the second argument. It is assumed that the type of
	 *  the first argument is LongMatrixToken.
	 *  @param token The token to compare to this token.
	 *  @return A token containing true if every element of the first
	 *   argument matrix is close to the corresponding element of this
	 * 	 matrix.
	 */
    protected BooleanToken _isCloseTo(
            MatrixToken token, double epsilon) {
       LongMatrixToken convertedArgument = (LongMatrixToken)token;
       return BooleanToken.getInstance(LongMatrixMath.within(
               _value,
               convertedArgument._getInternalLongMatrix(),
               (long)Math.floor(epsilon)));
    }

    /** Return a new token whose elements are the remainders of
     *  the elements of this token when divided by the argument.
     *  It is guaranteed by the caller that the type of the argument
     *  is the same as the type of each element of this class.
     *  @param rightArgument The token that performs modulo on this token.
     *  @exception IllegalActionException If this operation is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */

    protected MatrixToken _moduloElement(Token rightArgument)
            throws IllegalActionException {
        long scalar = ((LongToken)rightArgument).longValue();
        long[][] result = LongMatrixMath.modulo(_value, scalar);
        return new LongMatrixToken(result);
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument token.  It is assumed
     *  that the type of the argument is LongMatrixToken.
     *  @param rightArgument The token to multiply this token by.
     *  @exception IllegalActionException If the units are not
     *  compatible, or this operation is not supported by the derived
     *  class.
     *  @return A new LongMatrixToken containing the result.
     */
    protected MatrixToken _multiply(MatrixToken rightArgument)
            throws IllegalActionException {
        LongMatrixToken convertedArgument = (LongMatrixToken)rightArgument;
        long[][] result = LongMatrixMath.multiply(
                _value, convertedArgument._getInternalLongMatrix());
        return new LongMatrixToken(result);
    }

    /** Return a new token whose value is the value of this token
     *  multiplied by the value of the argument scalar token.
     *  This method should be overridden in derived
     *  classes to provide type specific actions for multiply.
     *  @param rightArgument The token to multiply this token by.
     *  @exception IllegalActionException If this method is not
     *   supported by the derived class.
     *  @return A new LongMatrixToken containing the result.
     */
    protected MatrixToken _multiplyElement(Token rightArgument)
            throws IllegalActionException {
        long scalar = ((LongToken)rightArgument).longValue();
        long[][] result = LongMatrixMath.multiply(_value, scalar);
        return new LongMatrixToken(result);
    }

    /** Return a new token whose value is the value of the argument token
     *  subtracted from the value of this token.  It is assumed that the
     *  type of the argument is LongMatrixToken.
     *  @param rightArgument The token to subtract from this token.
     *  @exception IllegalActionException If the units are not
     *  compatible, or this operation is not supported by the derived
     *  class.
     *  @return A new LongMatrixToken containing the result.
     */
    protected MatrixToken _subtract(MatrixToken rightArgument)
            throws IllegalActionException {
        LongMatrixToken convertedArgument = (LongMatrixToken)rightArgument;
        long[][] result = LongMatrixMath.subtract(_value,
                convertedArgument._getInternalLongMatrix());
        return new LongMatrixToken(result);
    }

    /** Return a new token whose value is the value of the argument
     *  Token subtracted from the value of each element of this Token. It is
     *  assumed that the type of the argument is the same as the type
     *  of each element of this class.
     *  @param rightArgument The token to subtract from this token.
     *  @exception IllegalActionException If this operation is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    protected MatrixToken _subtractElement(Token rightArgument)
            throws IllegalActionException {
        long scalar = ((LongToken)rightArgument).longValue();
        long[][] result = LongMatrixMath.add(_value, -scalar);
        return new LongMatrixToken(result);
    }

    /** Return a new token whose value is the value of the argument
     *  Token subtracted from the value of each element of this Token. It is
     *  assumed that the type of the argument is the same as the type
     *  of each element of this class.
     *  @param rightArgument The token to subtract from this token.
     *  @exception IllegalActionException If this operation is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    protected MatrixToken _subtractElementReverse(Token rightArgument)
            throws IllegalActionException {
        long scalar = ((LongToken)rightArgument).longValue();
        long[][] result = LongMatrixMath.negative(
                LongMatrixMath.add(_value, -scalar));
        return new LongMatrixToken(result);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Initialize the row and column count and copy the specified
    // matrix. This method is used by the constructors.
    private void _initialize(long[][] value, int copy) {
        _rowCount = value.length;
        _columnCount = value[0].length;
        if (copy == DO_NOT_COPY) {
            _value = value;
        } else {
            _value = LongMatrixMath.allocCopy(value);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private long[][] _value = null;
    private int _rowCount = 0;
    private int _columnCount = 0;
}
