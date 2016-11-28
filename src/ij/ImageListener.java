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
	 *  Plugins that implement this interface are notified when
	 * 		an image is opened, closed or updated. The 
	 * 		Plugins/Utilities/Monitor Events command uses this interface.
	 *
	 * @see ImageEvent
	 */
	public interface ImageListener {

	/**
	 * Image opened.
	 *
	 * @param imp the imp
	 */
	public void imageOpened(ImagePlus imp);

	/**
	 * Image closed.
	 *
	 * @param imp the imp
	 */
	public void imageClosed(ImagePlus imp);

	/**
	 * Invoked when image update occurs.
	 *
	 * @param imp the imp
	 */
	public void imageUpdated(ImagePlus imp);

}
