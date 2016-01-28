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
package sbmlplugin.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import ij.IJ;
import ij.io.SaveDialog;

import org.sbml.libsbml.Model;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.libsbml;

/**
 * Spatial SBML Plugin for ImageJ
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Oct 3, 2015
 */
public class ModelSaver {
	private SBMLDocument document;
	private Model model;
	private String path;
	private String name;
	
	public ModelSaver(SBMLDocument document){
		this.document = document;
		this.model = document.getModel();
	}
	
	public void save(){
		SaveDialog sd = new SaveDialog("Save SBML Document", model.getId(), ".xml");
	
		path = sd.getDirectory();
		name = sd.getFileName();
			
		IJ.log(name);
		
		setAnnotation();
		
		try {		
			if(name.contains(".xml"))	
				name = name.substring(0, name.indexOf('.'));
			document.getModel().setId(name);	
			libsbml.writeSBMLToFile(document, path + "/" + name + ".xml"); 			
		} catch(NullPointerException e) {
			System.out.println("SBML document was not saved");
		}	
		
        IJ.log(document.toSBML());
	}

	private void setAnnotation(){
		String id = "";
		try {
			id = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			System.out.println("Unknown host");
		}
		
		String annot = "This " + model.getId() + " model is created";
		
		if(!id.equals(""))
			annot = annot.concat(" by " + id.substring(0, id.indexOf(".")));
			
		Calendar date = new GregorianCalendar();
		annot = annot.concat(" in " + date.getTime());
		
		document.setAnnotation(annot);
		
		model.setAnnotation("This model has been built using Spatial SBML Plugin created by Kaito Ii and Akira Funahashi "
				+ "from Funahashi Lab. Keio University, Japan with substantial contributions from Kota Mashimo, Mitunori Ozeki, and Noriko Hiroi");
	}

	public String getPath() {
		return path;
	}

	public String getName() {
		return name;
	}

	
}
