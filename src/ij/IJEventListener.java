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
package ij;

	// TODO: Auto-generated Javadoc
/**
	 *  Plugins that implement this interface are notified when the user
	 * 	     changes the foreground color, changes the background color,
	 * 	     closes the color picker, closes the Log window or switches to
	 * 	     another tool.
	 *
	 * @see IJEventEvent
	 */
	public interface IJEventListener {
		
		/** The Constant FOREGROUND_COLOR_CHANGED. */
		public static final int FOREGROUND_COLOR_CHANGED = 0;
		
		/** The Constant BACKGROUND_COLOR_CHANGED. */
		public static final int BACKGROUND_COLOR_CHANGED = 1;
		
		/** The Constant COLOR_PICKER_CLOSED. */
		public static final int COLOR_PICKER_CLOSED= 2;
		
		/** The Constant LOG_WINDOW_CLOSED. */
		public static final int LOG_WINDOW_CLOSED= 3;
		
		/** The Constant TOOL_CHANGED. */
		public static final int TOOL_CHANGED= 4;

	/**
	 * Event occurred.
	 *
	 * @param eventID the event ID
	 */
	public void eventOccurred(int eventID);

}
