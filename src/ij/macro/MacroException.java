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
/** This runtime exception is thrown by break and continue statements. */
class MacroException extends RuntimeException {
	
	/** The type. */
	private int type;
	
	/**
	 * Instantiates a new macro exception.
	 *
	 * @param type the type
	 */
	MacroException(int type) {
		this.type = type;
	}
	
	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	int getType() {
		return type;
	}
	
}
