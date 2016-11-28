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
package ij.plugin.filter;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.process.ImageProcessor;


// TODO: Auto-generated Javadoc
/** This plugin saves an image in tiff, gif, jpeg, bmp, png, text or raw format. */
public class Writer implements PlugInFilter {
	
	/** The arg. */
	private String arg;
    
    /** The imp. */
    private ImagePlus imp;
    
	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#setup(java.lang.String, ij.ImagePlus)
	 */
	public int setup(String arg, ImagePlus imp) {
		this.arg = arg;
		this.imp = imp;
		return DOES_ALL+NO_CHANGES;
	}

	/* (non-Javadoc)
	 * @see ij.plugin.filter.PlugInFilter#run(ij.process.ImageProcessor)
	 */
	public void run(ImageProcessor ip) {
		if (arg.equals("tiff"))
			new FileSaver(imp).saveAsTiff();
		else if (arg.equals("gif"))
			new FileSaver(imp).saveAsGif();
		else if (arg.equals("jpeg"))
			new FileSaver(imp).saveAsJpeg();
		else if (arg.equals("text"))
			new FileSaver(imp).saveAsText();
		else if (arg.equals("lut"))
			new FileSaver(imp).saveAsLut();
		else if (arg.equals("raw"))
			new FileSaver(imp).saveAsRaw();
		else if (arg.equals("zip"))
			new FileSaver(imp).saveAsZip();
		else if (arg.equals("bmp"))
			new FileSaver(imp).saveAsBmp();
		else if (arg.equals("png"))
			new FileSaver(imp).saveAsPng();
		else if (arg.equals("pgm"))
			new FileSaver(imp).saveAsPgm();
		else if (arg.equals("fits"))
			new FileSaver(imp).saveAsFits();
	}
	
}


