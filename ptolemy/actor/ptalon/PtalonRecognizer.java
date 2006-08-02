// $ANTLR : "ptalonForPtolemy.g" -> "PtalonRecognizer.java"$
/* 

 Copyright (c) 2006 The Regents of the University of California.
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
package ptolemy.actor.ptalon;

import antlr.TokenBuffer;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.ANTLRException;
import antlr.LLkParser;
import antlr.Token;
import antlr.TokenStream;
import antlr.RecognitionException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.ParserSharedInputState;
import antlr.collections.impl.BitSet;
import antlr.collections.AST;
import java.util.Hashtable;
import antlr.ASTFactory;
import antlr.ASTPair;
import antlr.collections.impl.ASTArray;

public class PtalonRecognizer extends antlr.LLkParser       implements PtalonTokenTypes
 {

protected PtalonRecognizer(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public PtalonRecognizer(TokenBuffer tokenBuf) {
  this(tokenBuf,2);
}

protected PtalonRecognizer(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public PtalonRecognizer(TokenStream lexer) {
  this(lexer,2);
}

public PtalonRecognizer(ParserSharedInputState state) {
  super(state,2);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

	public final void import_declaration() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST import_declaration_AST = null;
		
		try {      // for error handling
			{
			AST tmp1_AST = null;
			tmp1_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp1_AST);
			match(IMPORT);
			qualified_identifier();
			astFactory.addASTChild(currentAST, returnAST);
			match(SEMI);
			}
			import_declaration_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_0);
			} else {
			  throw ex;
			}
		}
		returnAST = import_declaration_AST;
	}
	
	public final void qualified_identifier() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST qualified_identifier_AST = null;
		Token  a = null;
		AST a_AST = null;
		Token  b = null;
		AST b_AST = null;
		
			String identifier = "";
		
		
		try {      // for error handling
			a = LT(1);
			a_AST = astFactory.create(a);
			match(ID);
			if ( inputState.guessing==0 ) {
				
						identifier = identifier + a.getText();
					
			}
			{
			_loop659:
			do {
				if ((LA(1)==DOT)) {
					AST tmp3_AST = null;
					tmp3_AST = astFactory.create(LT(1));
					match(DOT);
					b = LT(1);
					b_AST = astFactory.create(b);
					match(ID);
					if ( inputState.guessing==0 ) {
						
								identifier = identifier + "." +  b.getText();
							
					}
				}
				else {
					break _loop659;
				}
				
			} while (true);
			}
			if ( inputState.guessing==0 ) {
				qualified_identifier_AST = (AST)currentAST.root;
				
						qualified_identifier_AST = astFactory.create(QUALID,identifier);
					
				currentAST.root = qualified_identifier_AST;
				currentAST.child = qualified_identifier_AST!=null &&qualified_identifier_AST.getFirstChild()!=null ?
					qualified_identifier_AST.getFirstChild() : qualified_identifier_AST;
				currentAST.advanceChildToEnd();
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_1);
			} else {
			  throw ex;
			}
		}
		returnAST = qualified_identifier_AST;
	}
	
	public final void port_declaration() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST port_declaration_AST = null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case PORT:
			{
				AST tmp4_AST = null;
				tmp4_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp4_AST);
				match(PORT);
				break;
			}
			case INPORT:
			{
				AST tmp5_AST = null;
				tmp5_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp5_AST);
				match(INPORT);
				break;
			}
			case OUTPORT:
			{
				AST tmp6_AST = null;
				tmp6_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp6_AST);
				match(OUTPORT);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			AST tmp7_AST = null;
			tmp7_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp7_AST);
			match(ID);
			port_declaration_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_2);
			} else {
			  throw ex;
			}
		}
		returnAST = port_declaration_AST;
	}
	
	public final void parameter_declaration() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST parameter_declaration_AST = null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case PARAMETER:
			{
				AST tmp8_AST = null;
				tmp8_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp8_AST);
				match(PARAMETER);
				break;
			}
			case INTPARAMETER:
			{
				AST tmp9_AST = null;
				tmp9_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp9_AST);
				match(INTPARAMETER);
				break;
			}
			case BOOLPARAMETER:
			{
				AST tmp10_AST = null;
				tmp10_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp10_AST);
				match(BOOLPARAMETER);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			AST tmp11_AST = null;
			tmp11_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp11_AST);
			match(ID);
			parameter_declaration_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_2);
			} else {
			  throw ex;
			}
		}
		returnAST = parameter_declaration_AST;
	}
	
	public final void relation_declaration() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST relation_declaration_AST = null;
		
		try {      // for error handling
			AST tmp12_AST = null;
			tmp12_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp12_AST);
			match(RELATION);
			AST tmp13_AST = null;
			tmp13_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp13_AST);
			match(ID);
			relation_declaration_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_2);
			} else {
			  throw ex;
			}
		}
		returnAST = relation_declaration_AST;
	}
	
	public final void attribute() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST attribute_AST = null;
		Token  a = null;
		AST a_AST = null;
		AST b_AST = null;
		
		try {      // for error handling
			match(ATTRIBUTE_MARKER);
			a = LT(1);
			a_AST = astFactory.create(a);
			match(ID);
			qualified_identifier();
			b_AST = (AST)returnAST;
			match(ATTRIBUTE_MARKER);
			if ( inputState.guessing==0 ) {
				attribute_AST = (AST)currentAST.root;
				
						attribute_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(ATTRIBUTE,a.getText())).add(b_AST));
					
				currentAST.root = attribute_AST;
				currentAST.child = attribute_AST!=null &&attribute_AST.getFirstChild()!=null ?
					attribute_AST.getFirstChild() : attribute_AST;
				currentAST.advanceChildToEnd();
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_3);
			} else {
			  throw ex;
			}
		}
		returnAST = attribute_AST;
	}
	
	public final void assignment() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST assignment_AST = null;
		
		try {      // for error handling
			AST tmp16_AST = null;
			tmp16_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp16_AST);
			match(ID);
			AST tmp17_AST = null;
			tmp17_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp17_AST);
			match(ASSIGN);
			{
			boolean synPredMatched665 = false;
			if (((LA(1)==ID) && (LA(2)==RPAREN||LA(2)==COMMA))) {
				int _m665 = mark();
				synPredMatched665 = true;
				inputState.guessing++;
				try {
					{
					match(ID);
					{
					switch ( LA(1)) {
					case RPAREN:
					{
						match(RPAREN);
						break;
					}
					case COMMA:
					{
						match(COMMA);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					}
				}
				catch (RecognitionException pe) {
					synPredMatched665 = false;
				}
				rewind(_m665);
inputState.guessing--;
			}
			if ( synPredMatched665 ) {
				AST tmp18_AST = null;
				tmp18_AST = astFactory.create(LT(1));
				astFactory.addASTChild(currentAST, tmp18_AST);
				match(ID);
			}
			else if ((_tokenSet_4.member(LA(1))) && (_tokenSet_5.member(LA(2)))) {
				{
				boolean synPredMatched668 = false;
				if (((LA(1)==ID) && (LA(2)==LPAREN))) {
					int _m668 = mark();
					synPredMatched668 = true;
					inputState.guessing++;
					try {
						{
						match(ID);
						match(LPAREN);
						}
					}
					catch (RecognitionException pe) {
						synPredMatched668 = false;
					}
					rewind(_m668);
inputState.guessing--;
				}
				if ( synPredMatched668 ) {
					actor_declaration();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else if ((_tokenSet_4.member(LA(1))) && (_tokenSet_5.member(LA(2)))) {
					{
					boolean synPredMatched671 = false;
					if (((_tokenSet_6.member(LA(1))) && (_tokenSet_7.member(LA(2))))) {
						int _m671 = mark();
						synPredMatched671 = true;
						inputState.guessing++;
						try {
							{
							arithmetic_expression();
							}
						}
						catch (RecognitionException pe) {
							synPredMatched671 = false;
						}
						rewind(_m671);
inputState.guessing--;
					}
					if ( synPredMatched671 ) {
						arithmetic_expression();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else if ((_tokenSet_4.member(LA(1))) && (_tokenSet_5.member(LA(2)))) {
						boolean_expression();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						throw new NoViableAltException(LT(1), getFilename());
					}
					
					}
				}
				else {
					throw new NoViableAltException(LT(1), getFilename());
				}
				
				}
			}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			
			}
			assignment_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_8);
			} else {
			  throw ex;
			}
		}
		returnAST = assignment_AST;
	}
	
	public final void actor_declaration() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST actor_declaration_AST = null;
		Token  a = null;
		AST a_AST = null;
		AST b_AST = null;
		AST c_AST = null;
		
		try {      // for error handling
			a = LT(1);
			a_AST = astFactory.create(a);
			match(ID);
			if ( inputState.guessing==0 ) {
				actor_declaration_AST = (AST)currentAST.root;
				
						a_AST = astFactory.create(ACTOR_DECLARATION,a.getText());
						actor_declaration_AST = (AST)astFactory.make( (new ASTArray(1)).add(a_AST));
					
				currentAST.root = actor_declaration_AST;
				currentAST.child = actor_declaration_AST!=null &&actor_declaration_AST.getFirstChild()!=null ?
					actor_declaration_AST.getFirstChild() : actor_declaration_AST;
				currentAST.advanceChildToEnd();
			}
			match(LPAREN);
			{
			switch ( LA(1)) {
			case ID:
			{
				assignment();
				b_AST = (AST)returnAST;
				if ( inputState.guessing==0 ) {
					actor_declaration_AST = (AST)currentAST.root;
					
								actor_declaration_AST.addChild(b_AST);
							
				}
				{
				_loop675:
				do {
					if ((LA(1)==COMMA)) {
						match(COMMA);
						assignment();
						c_AST = (AST)returnAST;
						if ( inputState.guessing==0 ) {
							actor_declaration_AST = (AST)currentAST.root;
							
										actor_declaration_AST.addChild(c_AST);
									
						}
					}
					else {
						break _loop675;
					}
					
				} while (true);
				}
				break;
			}
			case RPAREN:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(RPAREN);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_9);
			} else {
			  throw ex;
			}
		}
		returnAST = actor_declaration_AST;
	}
	
	public final void arithmetic_expression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST arithmetic_expression_AST = null;
		
		try {      // for error handling
			arithmetic_term();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop687:
			do {
				if ((LA(1)==MINUS||LA(1)==PLUS)) {
					{
					switch ( LA(1)) {
					case PLUS:
					{
						AST tmp22_AST = null;
						tmp22_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp22_AST);
						match(PLUS);
						break;
					}
					case MINUS:
					{
						AST tmp23_AST = null;
						tmp23_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp23_AST);
						match(MINUS);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					arithmetic_term();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop687;
				}
				
			} while (true);
			}
			arithmetic_expression_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_10);
			} else {
			  throw ex;
			}
		}
		returnAST = arithmetic_expression_AST;
	}
	
	public final void boolean_expression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST boolean_expression_AST = null;
		
		try {      // for error handling
			boolean_term();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop702:
			do {
				if ((LA(1)==LOGICAL_OR)) {
					AST tmp24_AST = null;
					tmp24_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp24_AST);
					match(LOGICAL_OR);
					boolean_term();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop702;
				}
				
			} while (true);
			}
			boolean_expression_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_8);
			} else {
			  throw ex;
			}
		}
		returnAST = boolean_expression_AST;
	}
	
	public final void arithmetic_factor() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST arithmetic_factor_AST = null;
		Token  a = null;
		AST a_AST = null;
		Token  b = null;
		AST b_AST = null;
		AST c_AST = null;
		
			int sign = 1;
		
		
		try {      // for error handling
			{
			_loop678:
			do {
				if ((LA(1)==MINUS)) {
					AST tmp25_AST = null;
					tmp25_AST = astFactory.create(LT(1));
					match(MINUS);
					if ( inputState.guessing==0 ) {
						
								sign = -sign;
							
					}
				}
				else {
					break _loop678;
				}
				
			} while (true);
			}
			if ( inputState.guessing==0 ) {
				arithmetic_factor_AST = (AST)currentAST.root;
				
						if (sign == 1) {
							arithmetic_factor_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(ARITHMETIC_FACTOR,"arithmetic_factor")).add(astFactory.create(POSITIVE_SIGN,"positive")));
						} else {
							arithmetic_factor_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(ARITHMETIC_FACTOR,"arithmetic_factor")).add(astFactory.create(NEGATIVE_SIGN,"negative")));
						}
					
				currentAST.root = arithmetic_factor_AST;
				currentAST.child = arithmetic_factor_AST!=null &&arithmetic_factor_AST.getFirstChild()!=null ?
					arithmetic_factor_AST.getFirstChild() : arithmetic_factor_AST;
				currentAST.advanceChildToEnd();
			}
			{
			switch ( LA(1)) {
			case ID:
			{
				a = LT(1);
				a_AST = astFactory.create(a);
				match(ID);
				if ( inputState.guessing==0 ) {
					arithmetic_factor_AST = (AST)currentAST.root;
					
							arithmetic_factor_AST.addChild(a_AST);
						
				}
				break;
			}
			case NUMBER_LITERAL:
			{
				b = LT(1);
				b_AST = astFactory.create(b);
				match(NUMBER_LITERAL);
				if ( inputState.guessing==0 ) {
					arithmetic_factor_AST = (AST)currentAST.root;
					
							arithmetic_factor_AST.addChild(b_AST);
						
				}
				break;
			}
			case LPAREN:
			{
				match(LPAREN);
				arithmetic_expression();
				c_AST = (AST)returnAST;
				match(RPAREN);
				if ( inputState.guessing==0 ) {
					arithmetic_factor_AST = (AST)currentAST.root;
					
							arithmetic_factor_AST.addChild(c_AST);
						
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_11);
			} else {
			  throw ex;
			}
		}
		returnAST = arithmetic_factor_AST;
	}
	
	public final void arithmetic_term() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST arithmetic_term_AST = null;
		
		try {      // for error handling
			arithmetic_factor();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop683:
			do {
				if (((LA(1) >= STAR && LA(1) <= MOD))) {
					{
					switch ( LA(1)) {
					case STAR:
					{
						AST tmp28_AST = null;
						tmp28_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp28_AST);
						match(STAR);
						break;
					}
					case DIVIDE:
					{
						AST tmp29_AST = null;
						tmp29_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp29_AST);
						match(DIVIDE);
						break;
					}
					case MOD:
					{
						AST tmp30_AST = null;
						tmp30_AST = astFactory.create(LT(1));
						astFactory.makeASTRoot(currentAST, tmp30_AST);
						match(MOD);
						break;
					}
					default:
					{
						throw new NoViableAltException(LT(1), getFilename());
					}
					}
					}
					arithmetic_factor();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop683;
				}
				
			} while (true);
			}
			arithmetic_term_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_12);
			} else {
			  throw ex;
			}
		}
		returnAST = arithmetic_term_AST;
	}
	
	public final void relational_expression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST relational_expression_AST = null;
		
		try {      // for error handling
			arithmetic_expression();
			astFactory.addASTChild(currentAST, returnAST);
			{
			switch ( LA(1)) {
			case EQUAL:
			{
				AST tmp31_AST = null;
				tmp31_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp31_AST);
				match(EQUAL);
				break;
			}
			case NOT_EQUAL:
			{
				AST tmp32_AST = null;
				tmp32_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp32_AST);
				match(NOT_EQUAL);
				break;
			}
			case LESS_THAN:
			{
				AST tmp33_AST = null;
				tmp33_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp33_AST);
				match(LESS_THAN);
				break;
			}
			case GREATER_THAN:
			{
				AST tmp34_AST = null;
				tmp34_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp34_AST);
				match(GREATER_THAN);
				break;
			}
			case LESS_EQUAL:
			{
				AST tmp35_AST = null;
				tmp35_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp35_AST);
				match(LESS_EQUAL);
				break;
			}
			case GREATER_EQUAL:
			{
				AST tmp36_AST = null;
				tmp36_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp36_AST);
				match(GREATER_EQUAL);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			arithmetic_expression();
			astFactory.addASTChild(currentAST, returnAST);
			relational_expression_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_13);
			} else {
			  throw ex;
			}
		}
		returnAST = relational_expression_AST;
	}
	
	public final void boolean_factor() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST boolean_factor_AST = null;
		AST a_AST = null;
		AST b_AST = null;
		Token  c = null;
		AST c_AST = null;
		Token  d = null;
		AST d_AST = null;
		Token  e = null;
		AST e_AST = null;
		
			boolean sign = true;
		
		
		try {      // for error handling
			{
			_loop692:
			do {
				if ((LA(1)==LOGICAL_NOT)) {
					AST tmp37_AST = null;
					tmp37_AST = astFactory.create(LT(1));
					match(LOGICAL_NOT);
					if ( inputState.guessing==0 ) {
						
								sign = !sign;
							
					}
				}
				else {
					break _loop692;
				}
				
			} while (true);
			}
			if ( inputState.guessing==0 ) {
				boolean_factor_AST = (AST)currentAST.root;
				
						if (sign) {
							boolean_factor_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(BOOLEAN_FACTOR,"boolean_factor")).add(astFactory.create(LOGICAL_BUFFER,"!!")));
						} else {
							boolean_factor_AST = (AST)astFactory.make( (new ASTArray(2)).add(astFactory.create(BOOLEAN_FACTOR,"boolean_factor")).add(astFactory.create(LOGICAL_NOT,"!")));
						}
					
				currentAST.root = boolean_factor_AST;
				currentAST.child = boolean_factor_AST!=null &&boolean_factor_AST.getFirstChild()!=null ?
					boolean_factor_AST.getFirstChild() : boolean_factor_AST;
				currentAST.advanceChildToEnd();
			}
			{
			switch ( LA(1)) {
			case TRUE:
			{
				c = LT(1);
				c_AST = astFactory.create(c);
				match(TRUE);
				if ( inputState.guessing==0 ) {
					boolean_factor_AST = (AST)currentAST.root;
					
							boolean_factor_AST.addChild(c_AST);	
						
				}
				break;
			}
			case FALSE:
			{
				d = LT(1);
				d_AST = astFactory.create(d);
				match(FALSE);
				break;
			}
			default:
				if ((_tokenSet_6.member(LA(1))) && (_tokenSet_14.member(LA(2)))) {
					{
					boolean synPredMatched696 = false;
					if (((LA(1)==LPAREN) && (_tokenSet_4.member(LA(2))))) {
						int _m696 = mark();
						synPredMatched696 = true;
						inputState.guessing++;
						try {
							{
							match(LPAREN);
							boolean_expression();
							}
						}
						catch (RecognitionException pe) {
							synPredMatched696 = false;
						}
						rewind(_m696);
inputState.guessing--;
					}
					if ( synPredMatched696 ) {
						match(LPAREN);
						boolean_expression();
						a_AST = (AST)returnAST;
						match(RPAREN);
						if ( inputState.guessing==0 ) {
							boolean_factor_AST = (AST)currentAST.root;
							
									boolean_factor_AST.addChild(a_AST);	
								
						}
					}
					else if ((_tokenSet_6.member(LA(1))) && (_tokenSet_15.member(LA(2)))) {
						relational_expression();
						b_AST = (AST)returnAST;
						if ( inputState.guessing==0 ) {
							boolean_factor_AST = (AST)currentAST.root;
							
									boolean_factor_AST.addChild(b_AST);	
							
						}
					}
					else {
						throw new NoViableAltException(LT(1), getFilename());
					}
					
					}
				}
				else if ((LA(1)==ID) && (_tokenSet_13.member(LA(2)))) {
					if ( inputState.guessing==0 ) {
						boolean_factor_AST = (AST)currentAST.root;
						
								boolean_factor_AST.addChild(d_AST);	
							
					}
					e = LT(1);
					e_AST = astFactory.create(e);
					match(ID);
					if ( inputState.guessing==0 ) {
						boolean_factor_AST = (AST)currentAST.root;
						
								boolean_factor_AST.addChild(e_AST);	
							
					}
				}
			else {
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_13);
			} else {
			  throw ex;
			}
		}
		returnAST = boolean_factor_AST;
	}
	
	public final void boolean_term() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST boolean_term_AST = null;
		
		try {      // for error handling
			boolean_factor();
			astFactory.addASTChild(currentAST, returnAST);
			{
			_loop699:
			do {
				if ((LA(1)==LOGICAL_AND)) {
					AST tmp40_AST = null;
					tmp40_AST = astFactory.create(LT(1));
					astFactory.makeASTRoot(currentAST, tmp40_AST);
					match(LOGICAL_AND);
					boolean_factor();
					astFactory.addASTChild(currentAST, returnAST);
				}
				else {
					break _loop699;
				}
				
			} while (true);
			}
			boolean_term_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_16);
			} else {
			  throw ex;
			}
		}
		returnAST = boolean_term_AST;
	}
	
	public final void atomic_statement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST atomic_statement_AST = null;
		
		try {      // for error handling
			{
			switch ( LA(1)) {
			case PORT:
			case INPORT:
			case OUTPORT:
			{
				port_declaration();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case PARAMETER:
			case INTPARAMETER:
			case BOOLPARAMETER:
			{
				parameter_declaration();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case RELATION:
			{
				relation_declaration();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case ID:
			{
				actor_declaration();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(SEMI);
			atomic_statement_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_17);
			} else {
			  throw ex;
			}
		}
		returnAST = atomic_statement_AST;
	}
	
	public final void conditional_statement() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST conditional_statement_AST = null;
		
		try {      // for error handling
			AST tmp42_AST = null;
			tmp42_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp42_AST);
			match(IF);
			match(LPAREN);
			boolean_expression();
			astFactory.addASTChild(currentAST, returnAST);
			match(RPAREN);
			match(LCURLY);
			{
			switch ( LA(1)) {
			case PORT:
			case INPORT:
			case OUTPORT:
			case ID:
			case PARAMETER:
			case INTPARAMETER:
			case BOOLPARAMETER:
			case RELATION:
			{
				atomic_statement();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case IF:
			{
				conditional_statement();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(RCURLY);
			match(ELSE);
			match(LCURLY);
			{
			switch ( LA(1)) {
			case PORT:
			case INPORT:
			case OUTPORT:
			case ID:
			case PARAMETER:
			case INTPARAMETER:
			case BOOLPARAMETER:
			case RELATION:
			{
				atomic_statement();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			case IF:
			{
				conditional_statement();
				astFactory.addASTChild(currentAST, returnAST);
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(RCURLY);
			conditional_statement_AST = (AST)currentAST.root;
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_17);
			} else {
			  throw ex;
			}
		}
		returnAST = conditional_statement_AST;
	}
	
	public final void actor_definition() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST actor_definition_AST = null;
		AST i_AST = null;
		Token  a = null;
		AST a_AST = null;
		AST b_AST = null;
		AST c_AST = null;
		
		try {      // for error handling
			if ( inputState.guessing==0 ) {
				actor_definition_AST = (AST)currentAST.root;
				
						actor_definition_AST = astFactory.create(ACTOR_DEFINITION);
					
				currentAST.root = actor_definition_AST;
				currentAST.child = actor_definition_AST!=null &&actor_definition_AST.getFirstChild()!=null ?
					actor_definition_AST.getFirstChild() : actor_definition_AST;
				currentAST.advanceChildToEnd();
			}
			{
			_loop710:
			do {
				if ((LA(1)==IMPORT)) {
					import_declaration();
					i_AST = (AST)returnAST;
					if ( inputState.guessing==0 ) {
						actor_definition_AST = (AST)currentAST.root;
						
								actor_definition_AST.addChild(i_AST);
							
					}
				}
				else {
					break _loop710;
				}
				
			} while (true);
			}
			a = LT(1);
			a_AST = astFactory.create(a);
			match(ID);
			if ( inputState.guessing==0 ) {
				actor_definition_AST = (AST)currentAST.root;
				
						actor_definition_AST.setText(a.getText());
					
			}
			match(IS);
			match(LCURLY);
			{
			switch ( LA(1)) {
			case PORT:
			case INPORT:
			case OUTPORT:
			case ID:
			case PARAMETER:
			case INTPARAMETER:
			case BOOLPARAMETER:
			case RELATION:
			case IF:
			case RCURLY:
			{
				{
				_loop713:
				do {
					switch ( LA(1)) {
					case PORT:
					case INPORT:
					case OUTPORT:
					case ID:
					case PARAMETER:
					case INTPARAMETER:
					case BOOLPARAMETER:
					case RELATION:
					{
						atomic_statement();
						b_AST = (AST)returnAST;
						if ( inputState.guessing==0 ) {
							actor_definition_AST = (AST)currentAST.root;
							
									actor_definition_AST.addChild(b_AST);
								
						}
						break;
					}
					case IF:
					{
						conditional_statement();
						c_AST = (AST)returnAST;
						if ( inputState.guessing==0 ) {
							actor_definition_AST = (AST)currentAST.root;
							
									actor_definition_AST.addChild(c_AST);
								
						}
						break;
					}
					default:
					{
						break _loop713;
					}
					}
				} while (true);
				}
				break;
			}
			case ATTRIBUTE_MARKER:
			{
				attribute();
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			match(RCURLY);
		}
		catch (RecognitionException ex) {
			if (inputState.guessing==0) {
				reportError(ex);
				recover(ex,_tokenSet_18);
			} else {
			  throw ex;
			}
		}
		returnAST = actor_definition_AST;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"\"import\"",
		"SEMI",
		"\"port\"",
		"\"inport\"",
		"\"outport\"",
		"ID",
		"\"parameter\"",
		"\"intparameter\"",
		"BOOLPARAMETER",
		"\"relation\"",
		"DOT",
		"ATTRIBUTE_MARKER",
		"ASSIGN",
		"RPAREN",
		"COMMA",
		"LPAREN",
		"MINUS",
		"NUMBER_LITERAL",
		"STAR",
		"DIVIDE",
		"MOD",
		"PLUS",
		"EQUAL",
		"NOT_EQUAL",
		"LESS_THAN",
		"GREATER_THAN",
		"LESS_EQUAL",
		"GREATER_EQUAL",
		"LOGICAL_NOT",
		"\"true\"",
		"\"false\"",
		"LOGICAL_AND",
		"LOGICAL_OR",
		"\"if\"",
		"LCURLY",
		"RCURLY",
		"\"else\"",
		"\"is\"",
		"\"boolparameter\"",
		"QUALID",
		"ATTRIBUTE",
		"ACTOR_DECLARATION",
		"ACTOR_DEFINITION",
		"NEGATIVE_SIGN",
		"POSITIVE_SIGN",
		"ARITHMETIC_FACTOR",
		"BOOLEAN_FACTOR",
		"LOGICAL_BUFFER",
		"LBRACKET",
		"RBRACKET",
		"BINARY_NOT",
		"ESC",
		"STRING_LITERAL",
		"WHITE_SPACE"
	};
	
	protected void buildTokenTypeASTClassMap() {
		tokenTypeToASTClassMap=null;
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { 528L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 32800L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	private static final long[] mk_tokenSet_2() {
		long[] data = { 32L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
	private static final long[] mk_tokenSet_3() {
		long[] data = { 549755813888L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
	private static final long[] mk_tokenSet_4() {
		long[] data = { 30068441600L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_4 = new BitSet(mk_tokenSet_4());
	private static final long[] mk_tokenSet_5() {
		long[] data = { 137438822912L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_5 = new BitSet(mk_tokenSet_5());
	private static final long[] mk_tokenSet_6() {
		long[] data = { 3670528L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_6 = new BitSet(mk_tokenSet_6());
	private static final long[] mk_tokenSet_7() {
		long[] data = { 66978304L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_7 = new BitSet(mk_tokenSet_7());
	private static final long[] mk_tokenSet_8() {
		long[] data = { 393216L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_8 = new BitSet(mk_tokenSet_8());
	private static final long[] mk_tokenSet_9() {
		long[] data = { 393248L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_9 = new BitSet(mk_tokenSet_9());
	private static final long[] mk_tokenSet_10() {
		long[] data = { 107307466752L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_10 = new BitSet(mk_tokenSet_10());
	private static final long[] mk_tokenSet_11() {
		long[] data = { 107371429888L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_11 = new BitSet(mk_tokenSet_11());
	private static final long[] mk_tokenSet_12() {
		long[] data = { 107342069760L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_12 = new BitSet(mk_tokenSet_12());
	private static final long[] mk_tokenSet_13() {
		long[] data = { 103079608320L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_13 = new BitSet(mk_tokenSet_13());
	private static final long[] mk_tokenSet_14() {
		long[] data = { 34359214592L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_14 = new BitSet(mk_tokenSet_14());
	private static final long[] mk_tokenSet_15() {
		long[] data = { 4294443520L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_15 = new BitSet(mk_tokenSet_15());
	private static final long[] mk_tokenSet_16() {
		long[] data = { 68719869952L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_16 = new BitSet(mk_tokenSet_16());
	private static final long[] mk_tokenSet_17() {
		long[] data = { 687194783680L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_17 = new BitSet(mk_tokenSet_17());
	private static final long[] mk_tokenSet_18() {
		long[] data = { 2L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_18 = new BitSet(mk_tokenSet_18());
	
	}
