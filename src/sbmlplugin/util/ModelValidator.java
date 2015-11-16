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

import ij.IJ;

import org.sbml.libsbml.Model;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLReader;
import org.sbml.libsbml.SBMLValidator;


public class ModelValidator {
	static {
		try {
			System.loadLibrary("sbmlj");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private SBMLDocument document;
	private Model model;

	
	public ModelValidator(SBMLDocument document){
		this.document = document;
		this.model = document.getModel();
		
	}
	
	private boolean checkModelVersion(){
		if(model.getLevel() != PluginConstants.SBMLLEVEL  ||  model.getVersion() != PluginConstants.SBMLVERSION) 
			IJ.log("model is not level 3 version 1");
		
		return model.getLevel() == PluginConstants.SBMLLEVEL  &&  model.getVersion() == PluginConstants.SBMLVERSION;
	}
	
	private boolean checkExtension(){
		
		if(!document.getPackageRequired("spatial"))
			IJ.log("model missing extension spatial");
		
		if(!document.getPackageRequired("req"))
			IJ.log("model missing extension req");
		
		return document.getPackageRequired("spatial") && document.getPackageRequired("req");
	}

	public void validate(){
		boolean hasRequiredAttribute = checkModelVersion() && checkExtension();
		
		if(!hasRequiredAttribute)
			return;
		
		SBMLValidator sv = new SBMLValidator();
		sv.validate(document);
		IJ.log(sv.getErrorLog().toString());
	}
	
	
	public static void main(String[] args) {
		SBMLReader reader = new SBMLReader();
		SBMLDocument d = reader.readSBML("sampledfield_3d.xml");
		ModelValidator mv = new ModelValidator(d);
		mv.validate();
	}

}
