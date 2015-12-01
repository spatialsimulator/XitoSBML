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

import org.sbml.libsbml.AdvectionCoefficient;
import org.sbml.libsbml.AnalyticGeometry;
import org.sbml.libsbml.BoundaryCondition;
import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.CoordinateComponent;
import org.sbml.libsbml.DiffusionCoefficient;
import org.sbml.libsbml.Geometry;
import org.sbml.libsbml.GeometryDefinition;
import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.ListOfAdjacentDomains;
import org.sbml.libsbml.ListOfCompartments;
import org.sbml.libsbml.ListOfCoordinateComponents;
import org.sbml.libsbml.ListOfDomainTypes;
import org.sbml.libsbml.ListOfDomains;
import org.sbml.libsbml.ListOfGeometryDefinitions;
import org.sbml.libsbml.ListOfParameters;
import org.sbml.libsbml.ListOfReactions;
import org.sbml.libsbml.ListOfSampledFields;
import org.sbml.libsbml.ListOfSpecies;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.ParametricGeometry;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLReader;
import org.sbml.libsbml.SBase;
import org.sbml.libsbml.SampledFieldGeometry;
import org.sbml.libsbml.SpatialCompartmentPlugin;
import org.sbml.libsbml.SpatialModelPlugin;
import org.sbml.libsbml.SpatialParameterPlugin;
import org.sbml.libsbml.SpatialSpeciesPlugin;
import org.sbml.libsbml.SpatialSymbolReference;
import org.sbml.libsbml.Species;


public class ModelValidator {
	static {
		try {
			System.loadLibrary("sbmlj");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	Model model;
	Boolean errorFlag = false;
	SpatialModelPlugin spatialplugin;
<<<<<<< 09dcdff56289c11b53b8d5e69bad50ddb993da8d:util/ModelValidator.java
<<<<<<< 20744e32d077613303082db60ff5337295f2661b:util/ModelValidator.java

=======
>>>>>>> adds extension package if necessary, checks model validity:src/util/ModelValidator.java
=======
	long level;
>>>>>>> Created pluginInfo class to store plugin informations:src/util/ModelValidator.java
	public ModelValidator(Model model){
		this.model = model;
		level =  model.getLevel();
		checkModelVersion();
		checkExtension();
		checkValidation();
	}
<<<<<<< 09dcdff56289c11b53b8d5e69bad50ddb993da8d:util/ModelValidator.java

	private void checkModelVersion(){
		if(model.getVersion() != PluginConstants.SBMLLEVEL  ||  model.getLevel() != PluginConstants.SBMLVERSION)
=======
	
	void checkModelVersion(){
		if(model.getVersion() != PluginInfo.SBMLLEVEL  ||  model.getLevel() != PluginInfo.SBMLVERSION) 
>>>>>>> Created pluginInfo class to store plugin informations:src/util/ModelValidator.java
			System.err.println("model is not level 3 version 1");
	}
<<<<<<< 20744e32d077613303082db60ff5337295f2661b:util/ModelValidator.java

	private void checkExtension(){
		SBMLDocument document = model.getSBMLDocument();

		if(!document.getPackageRequired("spatial")){
			System.err.println("model missing extension spatial");
		}

		if(!document.getPackageRequired("req")){
			System.err.println("model missing extension req");
		}

	}

	private void checkValidation(){
=======
	
	public void checkValidation(){
<<<<<<< 09dcdff56289c11b53b8d5e69bad50ddb993da8d:util/ModelValidator.java
>>>>>>> adds extension package if necessary, checks model validity:src/util/ModelValidator.java
=======
		if(level < PluginInfo.SBMLLEVEL) return;
>>>>>>> Created pluginInfo class to store plugin informations:src/util/ModelValidator.java
		checkModel(model);
		checkSpecies(model.getListOfSpecies());
		checkParameter(model.getListOfParameters());
		checkCompartment(model.getListOfCompartments());
		checkReaction(model.getListOfReactions());
		checkGeometry();

		if(!errorFlag)
			System.out.println( model.getId() + " Model is valid");
	}

	private void checkModel(Model model){
		System.out.println("Checking model");
		checkRequired(model);
	}

	private void checkSpecies(ListOfSpecies los){
		System.out.println("Checking species");
		for(int i = 0 ; i < los.size() ; i++){
			Species s = los.get(i);
			checkRequired(s);
			SpatialSpeciesPlugin ssp = (SpatialSpeciesPlugin) s.getPlugin("spatial");
			ssp.hasRequiredElements();
		}
	}


	private void checkParameter(ListOfParameters lop){
		System.out.println("Checking parameter");
		for(int i = 0 ; i < lop.size() ; i++){
			Parameter p = lop.get(i);
			SpatialParameterPlugin sp = (SpatialParameterPlugin) p.getPlugin("spatial");
			checkRequired(p);
			if(sp.isSpatialParameter())
				checkSpatialParameter(sp);
		}
	}

	private void checkSpatialParameter(SpatialParameterPlugin sp){
		if(sp.isSetAdvectionCoefficient()){
			AdvectionCoefficient ac = sp.getAdvectionCoefficient();
			checkRequired(ac);
		} else if(sp.isSetBoundaryCondition()){
			BoundaryCondition bc = sp.getBoundaryCondition();
			checkRequired(bc);
		} else if(sp.isSetDiffusionCoefficient()){
			DiffusionCoefficient dc = sp.getDiffusionCoefficient();
			checkRequired(dc);
		} else if(sp.isSetSpatialSymbolReference()){
			SpatialSymbolReference ssr = sp.getSpatialSymbolReference();
			checkRequired(ssr);
		} else{
			System.err.println("missing spatial in " + sp.getParentSBMLObject().getId() + " at line:" + sp.getLine());
		}
	}

	private void checkCompartment(ListOfCompartments loc){
		System.out.println("Checking compartment");
		for(int i = 0 ; i < loc.size() ; i++){
			Compartment c = loc.get(i);
			checkRequired(c);
			SpatialCompartmentPlugin scp = (SpatialCompartmentPlugin) c.getPlugin("spatial");
			checkSpatialCompartment(scp);
		}
	}

	private void checkSpatialCompartment(SpatialCompartmentPlugin scp){
		if(scp.isSetCompartmentMapping()) checkRequired(scp.getCompartmentMapping());
		else System.err.println("missing compartment mapping in " + scp.getParentSBMLObject().getId() + " at line:" + scp.getLine());
	}

	private void checkReaction(ListOfReactions lor){
		System.out.println("Checking reaction");
		for(int i = 0; i < lor.size() ; i++){
			Reaction r = lor.get(i);
			checkRequired(r);
			checkList(r.getListOfReactants());
			checkList(r.getListOfProducts());
			checkList(r.getListOfModifiers());
			checkRequired(r.getKineticLaw());
			checkList(r.getKineticLaw().getListOfLocalParameters());
		}
	}

	private void checkGeometry(){
		System.out.println("Checking geometry");
		Geometry geometry = spatialplugin.getGeometry();
		checkRequired(geometry);
		checkCoordinateComponents(geometry.getListOfCoordinateComponents());
		checkDomainType(geometry.getListOfDomainTypes());
		checkDomains(geometry.getListOfDomains());
		checkAdjacentDomains(geometry.getListOfAdjacentDomains());
		checkGeometryDefinitions(geometry.getListOfGeometryDefinitions());
		checkSampledField(geometry.getListOfSampledFields());
	}

	private void checkSampledField(ListOfSampledFields losf){
		checkList(losf);
	}

	private void checkGeometryDefinitions(ListOfGeometryDefinitions logd){
		for(int i = 0 ; i < logd.size() ; i++){
			GeometryDefinition gd = logd.get(i);
			checkRequired(gd);
			if(gd instanceof AnalyticGeometry)
				checkList(((AnalyticGeometry) gd).getListOfAnalyticVolumes());

			if(gd instanceof SampledFieldGeometry)
				checkList(((SampledFieldGeometry) gd).getListOfSampledVolumes());

			if(gd instanceof ParametricGeometry){
				checkRequired(((ParametricGeometry) gd).getSpatialPoints());
				checkList(((ParametricGeometry) gd).getListOfParametricObjects());
			}

		}
	}

	private void checkAdjacentDomains(ListOfAdjacentDomains load){
		checkList(load);
	}

	private void checkDomains(ListOfDomains lod){
		checkList(lod);
	}

	private void checkDomainType(ListOfDomainTypes lodt){
		checkList(lodt);
	}

	private void checkCoordinateComponents(ListOfCoordinateComponents locc){
		checkRequired(locc);
		for(int i = 0 ; i < locc.size() ; i++){
			CoordinateComponent cc = locc.get(i);
			checkRequired(cc);
			checkRequired(cc.getBoundaryMax());
			checkRequired(cc.getBoundaryMin());
		}
	}

	private void checkRequired(SBase s){
		if(!s.hasRequiredAttributes()){
			printError(s, "attribute ");
		}
		if(!s.hasRequiredElements()){
			printError(s, "element ");
		}
	}

	private void checkList(ListOf lo){
		for(int i = 0 ; i < lo.size() ; i++)
			checkRequired(lo.get(i));
	}

	private void printError(SBase s, String part){
		String id = s.getId();
		if(id.equals("")) id = s.getParentSBMLObject().getId();
		System.err.println("missing required " +  part + "in " +  s.getClass() + " " + id + " at line: " + s.getLine());
		errorFlag = true;
	}

	public static void main(String[] args) {
		SBMLReader reader = new SBMLReader();
		SBMLDocument d = reader.readSBML("spatial_example1.xml");
		ModelValidator mv = new ModelValidator(d.getModel());
	}

}
