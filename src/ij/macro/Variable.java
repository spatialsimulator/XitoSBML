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
/**
 * The Class Variable.
 */
class Variable implements MacroConstants, Cloneable {
	
	/** The Constant STRING. */
	static final int VALUE=0, ARRAY=1, STRING=2;
    
    /** The sym tab index. */
    int symTabIndex;
    
    /** The value. */
    private double value;
    
    /** The str. */
    private String str;
    
    /** The array. */
    private Variable[] array;
    
    /** The array size. */
    private int arraySize;

    /**
     * Instantiates a new variable.
     */
    Variable() {
    }

    /**
     * Instantiates a new variable.
     *
     * @param value the value
     */
    Variable(double value) {
        this.value = value;
    }

    /**
     * Instantiates a new variable.
     *
     * @param symTabIndex the sym tab index
     * @param value the value
     * @param str the str
     */
    Variable(int symTabIndex, double value, String str) {
        this.symTabIndex = symTabIndex;
        this.value = value;
        this.str = str;
    }

    /**
     * Instantiates a new variable.
     *
     * @param symTabIndex the sym tab index
     * @param value the value
     * @param str the str
     * @param array the array
     */
    Variable(int symTabIndex, double value, String str, Variable[] array) {
        this.symTabIndex = symTabIndex;
        this.value = value;
        this.str = str;
        this.array = array;
    }

    /**
     * Instantiates a new variable.
     *
     * @param array the array
     */
    Variable(byte[] array) {
    	this.array = new Variable[array.length];
    	for (int i=0; i<array.length; i++)
    		this.array[i] = new Variable(array[i]&255);
    }

    /**
     * Instantiates a new variable.
     *
     * @param array the array
     */
    Variable(int[] array) {
    	this.array = new Variable[array.length];
    	for (int i=0; i<array.length; i++)
    		this.array[i] = new Variable(array[i]);
    }

    /**
     * Instantiates a new variable.
     *
     * @param array the array
     */
    Variable(double[] array) {
    	this.array = new Variable[array.length];
    	for (int i=0; i<array.length; i++)
    		this.array[i] = new Variable(array[i]);
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    double getValue() {
    	if (str!=null)
    			return convertToDouble();  // string to number conversions
    	else
        	return value;
    }

	/**
	 * Convert to double.
	 *
	 * @return the double
	 */
	double convertToDouble() {
		try {
			Double d = new Double(str);
			return d.doubleValue();
		} catch (NumberFormatException e){
			return Double.NaN;
		}
	}

    /**
     * Sets the value.
     *
     * @param value the new value
     */
    void setValue(double value) {
        this.value = value;
        str = null;
        array = null;
    }

    /**
     * Gets the string.
     *
     * @return the string
     */
    String getString() {
        return str;
    }

    /**
     * Sets the string.
     *
     * @param str the new string
     */
    void setString(String str) {
        this.str = str;
        value = 0.0;
        array = null;
    }

    /**
     * Gets the array.
     *
     * @return the array
     */
    Variable[] getArray() {
        return array;
    }

    /**
     * Sets the array.
     *
     * @param array the new array
     */
    void setArray(Variable[] array) {
        this.array = array;
        value = 0.0;
        str = null;
        arraySize = 0;
    }
    
    /**
     * Sets the array size.
     *
     * @param size the new array size
     */
    void setArraySize(int size) {
    	if (array==null)
    		size = 0;
    	else if (size>array.length)
    		size = array.length;
    	arraySize = size;
    }
    
    /**
     * Gets the array size.
     *
     * @return the array size
     */
    int getArraySize() {
    	int size = array!=null?array.length:0;
    	if (arraySize>0) size = arraySize;
    	return size;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    int getType() {
    	if (array!=null)
    		return ARRAY;
    	else if (str!=null)
    		return STRING;
    	else
    		return VALUE;
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String s = "";
		if (array!=null)
			s += "array["+array.length+"]";
		else if (str!=null) {
			s = str;
			if (s.length()>80)
				s = s.substring(0, 80)+"...";
			s = s.replaceAll("\n", " | ");
			s = "\""+s+"\"";
		} else {
			if (value==(int)value)
				s += (int)value;
			else
				s += ij.IJ.d2s(value,4);
		}
		return s;
	}
    
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public synchronized Object clone() {
		try {return super.clone();}
		catch (CloneNotSupportedException e) {return null;}
	}

} // class Variable
