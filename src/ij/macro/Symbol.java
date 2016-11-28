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

// TODO: Auto-generated Javadoc
/** Objects of this class are used as entries in the macro language symbol table. */
public class Symbol implements MacroConstants {
    
    /** The type. */
    public int type;
    
    /** The value. */
    public double value;
    
    /** The str. */
    public String str;

    /**
     * Instantiates a new symbol.
     *
     * @param token the token
     * @param str the str
     */
    Symbol(int token, String str) {
        type = token&0xffff;
        this.str = str;
    }

    /**
     * Instantiates a new symbol.
     *
     * @param value the value
     */
    Symbol(double value) {
        this.value = value;
    }

    /**
     * Gets the function type.
     *
     * @return the function type
     */
    int getFunctionType() {
        int t = 0;
        if (type>=300 && type<1000)
            t = PREDEFINED_FUNCTION;
        else if (type>=1000 && type<2000)
            t = NUMERIC_FUNCTION;
        else if (type>=2000 && type<3000)
            t = STRING_FUNCTION;
        else if (type>=3000 && type<4000)
            t = ARRAY_FUNCTION;
        return t;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return type+" "+value+" "+str;
    }

} // class Symbol
