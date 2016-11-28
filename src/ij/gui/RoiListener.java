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
package ij.gui;
import ij.ImagePlus;
	
	// TODO: Auto-generated Javadoc
/**
	 *  Plugins that implement this interface are notified when
	 * 		an ROI is created, modified or deleted. The 
	 * 		Plugins/Utilities/Monitor Events command uses this interface.
	 *
	 * @see RoiEvent
	 */
	public interface RoiListener {
		
		/** The Constant CREATED. */
		public static final int CREATED = 1;
		
		/** The Constant MOVED. */
		public static final int MOVED = 2;
		
		/** The Constant MODIFIED. */
		public static final int MODIFIED = 3;
		
		/** The Constant EXTENDED. */
		public static final int EXTENDED = 4;
		
		/** The Constant COMPLETED. */
		public static final int COMPLETED = 5;
		
		/** The Constant DELETED. */
		public static final int DELETED = 6;

	/**
	 * Roi modified.
	 *
	 * @param imp the imp
	 * @param id the id
	 */
	public void roiModified(ImagePlus imp, int id);

}
