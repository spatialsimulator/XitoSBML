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
	 * The Interface Debugger.
	 */
	public interface Debugger {

		/** The Constant RUN_TO_CARET. */
		public static final int NOT_DEBUGGING=0, STEP=1, TRACE=2, FAST_TRACE=3,
			RUN_TO_COMPLETION=4, RUN_TO_CARET=5;
		
		/**
		 * Debug.
		 *
		 * @param interp the interp
		 * @param mode the mode
		 * @return the int
		 */
		public int debug(Interpreter interp, int mode);
			
}
