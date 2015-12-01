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

import ij.IJ;

/**
 * Spatial SBML Plugin for ImageJ
 * 
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp> Date Created: Jun 17, 2015
 */
public class Spatial_SBase_SBML extends Spatial_SBML {

	static {
		String varname;
		String shlibname;

		if (System.getProperty("os.name").startsWith("Mac OS")) {
			varname = "DYLD_LIBRARY_PATH"; // We're on a Mac.
			shlibname = "'libsbmlj.jnilib'";
		} else {
			varname = "LD_LIBRARY_PATH"; // We're not on a Mac.
			shlibname = "'libsbmlj.so' and/or 'libsbml.so'";
		}

		try {
			System.loadLibrary("sbmlj");
			// For extra safety, check that the jar file is in the classpath.
			Class.forName("org.sbml.libsbml.libsbml");
		} catch (UnsatisfiedLinkError e) {
			IJ.error("Error encountered while attempting to load libSBML:");
			IJ.error("Please check the value of your " + varname
					+ " environment variable and/or"
					+ " your 'java.library.path' system property"
					+ " (depending on which one you are using) to"
					+ " make sure it list the directories needed to"
					+ " find the " + shlibname + " library file and the"
					+ " libraries it depends upon (e.g., the XML parser).");
			System.exit(1);
		} catch (ClassNotFoundException e) {
			IJ.error("Error: unable to load the file 'libsbmlj.jar'."
					+ " It is likely that your -classpath command line "
					+ " setting or your CLASSPATH environment variable "
					+ " do not include the file 'libsbmlj.jar'.");
			e.printStackTrace();

			System.exit(1);
		} catch (SecurityException e) {
			IJ.error("Error encountered while attempting to load libSBML:");
			e.printStackTrace();
			IJ.error("Could not load the libSBML library files due to a"
					+ " security exception.\n");
			System.exit(1);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see Spatial_SBML#run(java.lang.String)
	 */
	@Override
	public void run(String args) {
		if (checkJgraph() && check3Dviewer())
			new MainSBaseSpatial().run(args);
	}
}
