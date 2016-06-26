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
package sbmlplugin;
import sbmlplugin.util.PluginConstants;
import ij.IJ;
import ij.plugin.PlugIn;


// TODO: Auto-generated Javadoc
/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jun 17, 2015
 */
public abstract class Spatial_SBML implements PlugIn {
	
	/** The is running. */
	static boolean isRunning = false;
	
	/** The title. */
	String title = "Export segmented image to Spatial SBML";
	
	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public abstract void run(String arg);

	/**
	 * Check jgraph.
	 *
	 * @return true, if successful
	 */
	public boolean checkJgraph(){
		try {
			Class.forName("org.jgrapht.ListenableGraph");
			return true;
		} catch (ClassNotFoundException e1) {
			IJ.error("Please Install Jgrapht");
			return false;
		}
	}
	
	/**
	 * Check 3 dviewer.
	 *
	 * @return true, if successful
	 */
	public boolean check3Dviewer(){
		String version = ij3d.Install_J3D.getJava3DVersion();
        if(version != null && Float.parseFloat(version) >= PluginConstants.VIEWERVERSION)
                return true;
        IJ.error("Please Update 3D Viewer");
        return false;
	}
}
