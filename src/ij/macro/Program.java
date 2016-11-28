/*******************************************************************************
 * Copyright 2015 Kaito Ii
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package ij.macro;
import ij.IJ;

import java.util.Hashtable;

// TODO: Auto-generated Javadoc
/** An object of this type is a tokenized macro file and the associated symbol table. */
public class Program implements MacroConstants {

	/** The max symbols. */
	private int maxSymbols = 800; // will be increased as needed
	
	/** The max program size. */
	private int maxProgramSize = 2000;  // well be increased as needed
	
	/** The pc. */
	private int pc = -1;
	
	/** The st loc. */
	int stLoc = -1;
	
	/** The sym tab loc. */
	int symTabLoc;
	
	/** The table. */
	Symbol[] table = new Symbol[maxSymbols];
    
    /** The system table. */
    static Symbol[] systemTable;
	
	/** The code. */
	int[] code = new int[maxProgramSize];
	
	/** The line numbers. */
	int[] lineNumbers = new int[maxProgramSize];
	
	/** The globals. */
	Variable[] globals;
	
	/** The has functions. */
	boolean hasVars, hasFunctions;
	
	/** The macro count. */
	int macroCount;
    
    /** The menus. */
    Hashtable menus;
    
    /** The queue commands. */
    // run keyboard shortcut macros on event dispatch thread?
	boolean queueCommands; 
	
	/** The extension registry. */
	Hashtable extensionRegistry;
	
	
	/**
	 * Instantiates a new program.
	 */
	public Program() {
		if (systemTable!=null) {
			stLoc = systemTable.length - 1;
			for (int i=0; i<=stLoc; i++)
			table[i] = systemTable[i];
		} else {
			//IJ.log("make table");
			addKeywords();
			addFunctions();
			addNumericFunctions();
			addStringFunctions();
			addArrayFunctions();
			systemTable = new Symbol[stLoc+1];
			for (int i=0; i<=stLoc; i++)
				systemTable[i] = table[i];
			IJ.register(Program.class);
		}
	}
	
	/**
	 * Gets the code.
	 *
	 * @return the code
	 */
	public int[] getCode() {
		return code;
	}
	
	/**
	 * Gets the symbol table.
	 *
	 * @return the symbol table
	 */
	public Symbol[] getSymbolTable() {
		return table;
	}
	
	/**
	 * Adds the keywords.
	 */
	void addKeywords() {
		for (int i=0; i<keywords.length; i++)
			addSymbol(new Symbol(keywordIDs[i], keywords[i]));
	}

	/**
	 * Adds the functions.
	 */
	void addFunctions() {
		for (int i=0; i<functions.length; i++)
			addSymbol(new Symbol(functionIDs[i], functions[i]));
	}

	/**
	 * Adds the numeric functions.
	 */
	void addNumericFunctions() {
		for (int i=0; i<numericFunctions.length; i++)
			addSymbol(new Symbol(numericFunctionIDs[i], numericFunctions[i]));
	}
	
	/**
	 * Adds the string functions.
	 */
	void addStringFunctions() {
		for (int i=0; i<stringFunctions.length; i++)
			addSymbol(new Symbol(stringFunctionIDs[i], stringFunctions[i]));
	}

	/**
	 * Adds the array functions.
	 */
	void addArrayFunctions() {
		for (int i=0; i<arrayFunctions.length; i++)
			addSymbol(new Symbol(arrayFunctionIDs[i], arrayFunctions[i]));
	}

	/**
	 * Adds the symbol.
	 *
	 * @param sym the sym
	 */
	void addSymbol(Symbol sym) {
		stLoc++;
		if (stLoc==table.length) {
			Symbol[] tmp = new Symbol[maxSymbols*2];
			System.arraycopy(table, 0, tmp, 0, maxSymbols);
			table = tmp;
			maxSymbols *= 2;
		}
		table[stLoc] = sym;
	}
	
	/**
	 * Adds the token.
	 *
	 * @param tok the tok
	 * @param lineNumber the line number
	 */
	void addToken(int tok, int lineNumber) {//n__
		pc++;
		if (pc==code.length) {
			int[] tmp = new int[maxProgramSize*2];
			System.arraycopy(code, 0, tmp, 0, maxProgramSize);
			code = tmp;

            tmp = new int[maxProgramSize*2];  //n__
			System.arraycopy(lineNumbers, 0, tmp, 0, maxProgramSize);
			lineNumbers = tmp;

			maxProgramSize *= 2;
        }
		code[pc] = tok;
        lineNumbers[pc] = lineNumber; //n__
	}

	/**
	 *  Looks up a word in the symbol table. Returns null if the word is not found.
	 *
	 * @param str the str
	 * @return the symbol
	 */
	Symbol lookupWord(String str) {
        //IJ.log("lookupWord: "+str);
		Symbol symbol;
		String symStr;
		for (int i=0; i<=stLoc; i++) {
			symbol = table[i];
			if (symbol.type!=STRING_CONSTANT && str.equals(symbol.str)) {
				symTabLoc = i;
				return symbol;
			}
		}
		return null;
	}

	/**
	 * Save globals.
	 *
	 * @param interp the interp
	 */
	void saveGlobals(Interpreter interp) {
		//IJ.log("saveGlobals: "+interp.topOfStack);
		if (interp.topOfStack==-1)
			return;
		int n = interp.topOfStack+1;
		globals = new Variable[n];
		for (int i=0; i<n; i++)
			globals[i] = interp.stack[i];
	}
	
	/**
	 * Dump symbol table.
	 */
	public void dumpSymbolTable() {
		IJ.log("");
		IJ.log("Symbol Table");
		for (int i=0; i<=maxSymbols; i++) {
			Symbol symbol = table[i];
			if (symbol==null)
				break;
			IJ.log(i+" "+symbol);
		}
	}

	/**
	 * Dump program.
	 */
	public void dumpProgram() {
		IJ.log("");
		IJ.log("Tokenized Program");
		String str;
		int token, address;
		for (int i=0; i<=pc; i++) 
			IJ.log(i+"	 "+lineNumbers[i]+"   "+(code[i]&TOK_MASK)+"   "+decodeToken(code[i]));
	}
	
	/**
	 * Gets the globals.
	 *
	 * @return the globals
	 */
	public Variable[] getGlobals() {
		return globals;
	}

	/**
	 * Checks for vars.
	 *
	 * @return true, if successful
	 */
	public boolean hasVars() {
		return hasVars;
	}

	/**
	 * Macro count.
	 *
	 * @return the int
	 */
	public int macroCount() {
		return macroCount;
	}

	/**
	 * Decode token.
	 *
	 * @param token the token
	 * @return the string
	 */
	public String decodeToken(int token) {
		return decodeToken(token&TOK_MASK, token>>TOK_SHIFT);
	}

	/**
	 * Decode token.
	 *
	 * @param token the token
	 * @param address the address
	 * @return the string
	 */
	String decodeToken(int token, int address) {
		String str;
		switch (token) {
			case WORD:
			case PREDEFINED_FUNCTION:
			case NUMERIC_FUNCTION:
			case STRING_FUNCTION:
			case ARRAY_FUNCTION:
			case USER_FUNCTION:
				str = table[address].str;
				break;
			case STRING_CONSTANT:
				str = "\""+table[address].str+"\"";
				break;
			case NUMBER:
				double v = table[address].value;
				if ((int)v==v)
					str = IJ.d2s(v,0);
				else
					str = ""+v;
				break;
			case EOF:
				str = "EOF";
				break;
			default:
				if (token<32) {
					switch (token) {
					case PLUS_PLUS:
						str="++";
						break;
					case MINUS_MINUS:
						str="--";
						break;
					case PLUS_EQUAL:
						str="+=";
						break;
					case MINUS_EQUAL:
						str="-=";
						break;
					case MUL_EQUAL:
						str="*=";
						break;
					case DIV_EQUAL:
						str="/=";
						break;
					case LOGICAL_AND:
						str="&&";
						break;
					case LOGICAL_OR:
						str="||";
						break;
					case EQ:
						str="==";
						break;
					case NEQ:
						str="!=";
						break;
					case GT:
						str=">";
						break;
					case GTE:
						str=">=";
						break;
					case LT:
						str="<";
						break;
					case LTE:
						str="<=";
						break;
					default:
						str="";
						break;
					}
				} else if (token>=200) {
					str = table[address].str;
				} else {
					char s[] = new char[1];
					s[0] = (char)token;
					str = new String(s);
				}
				break;
		}
		return str;
	}
    
    /**
     * Gets the menus.
     *
     * @return the menus
     */
    public Hashtable getMenus() {
        return menus;
    }

	/**
	 * Checks for word.
	 *
	 * @param word the word
	 * @return true, if successful
	 */
	// Returns 'true' if this macro program contains the specified word. */
	public boolean hasWord(String word) {
		int token, tokenAddress;
		for (int i=0; i<code.length; i++) {
			token = code[i];
			if (token<=127) continue;
			if (token==EOF) return false;
			tokenAddress = token>>TOK_SHIFT;
			String str = table[tokenAddress].str;
			if (str!=null && str.equals(word)) return true;
		}
		return false;
	}
	
	/**
	 * Gets the size.
	 *
	 * @return the size
	 */
	public int getSize() {
		return pc;
	}
	
} // Program
