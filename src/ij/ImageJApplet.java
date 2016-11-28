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
import java.applet.Applet;

// TODO: Auto-generated Javadoc
/**
	Runs ImageJ as an applet and optionally opens up to 
	nine images using URLs passed as a parameters.
	<p>
	Here is an example applet tag that launches ImageJ as an applet
	and passes it the URLs of two images:
	<pre>
	&lt;applet archive="../ij.jar" code="ij.ImageJApplet.class" width=0 height=0&gt;
	&lt;param name=url1 value="http://imagej.nih.gov/ij/images/FluorescentCells.jpg"&gt;
	&lt;param name=url2 value="http://imagej.nih.gov/ij/images/blobs.gif"&gt;
	&lt;/applet&gt;
	</pre>
	To use plugins, add them to ij.jar and add entries to IJ_Props.txt file (in ij.jar) that will  
	create commands for them in the Plugins menu, or a submenu. There are examples 
	of such entries in IJ.Props.txt, in the "Plugins installed in the Plugins menu" section.
	<p>
	Macros contained in a file named "StartupMacros.txt", in the same directory as the HTML file
	containing the applet tag, will be installed on startup.
*/
public class ImageJApplet extends Applet {

	/** Starts ImageJ if it's not already running. */
    public void init() {
    	ImageJ ij = IJ.getInstance();
     	if (ij==null || (ij!=null && !ij.isShowing()))
			new ImageJ(this);
		for (int i=1; i<=9; i++) {
			String url = getParameter("url"+i);
			if (url==null) break;
			ImagePlus imp = new ImagePlus(url);
			if (imp!=null) imp.show();
		}
    }
    
    /* (non-Javadoc)
     * @see java.applet.Applet#destroy()
     */
    public void destroy() {
    	ImageJ ij = IJ.getInstance();
    	if (ij!=null) ij.quit();
    }

}

