package jp.ac.keio.bio.fun.xitosbml.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLError;
import org.sbml.jsbml.SBMLErrorLog;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.spatial.BoundaryCondition;
import org.sbml.jsbml.ext.spatial.CoordinateComponent;
import org.sbml.jsbml.ext.spatial.Geometry;
import org.sbml.jsbml.ext.spatial.SpatialConstants;
import org.sbml.jsbml.ext.spatial.SpatialModelPlugin;
import org.sbml.jsbml.ext.spatial.SpatialParameterPlugin;
import org.sbml.jsbml.ext.spatial.SpatialSpeciesPlugin;
import org.sbml.jsbml.validator.SBMLValidator.CHECK_CATEGORY;

import ij.IJ;


// TODO: Auto-generated Javadoc
/**
 * The Class ModelValidator.
 */
public class ModelValidator {

	/** The document. */
	private SBMLDocument document;
	
	/** The model. */
	private Model model;

	
	/**
	 * Instantiates a new model validator.
	 *
	 * @param document the document
	 */
	public ModelValidator(SBMLDocument document){
		this.document = document;
		this.model = document.getModel();	
	}
	
	/**
	 * Check model version.
	 *
	 * @return true, if successful
	 */
	private boolean checkModelVersion(){
		if(model.getLevel() != PluginConstants.SBMLLEVEL  ||  model.getVersion() != PluginConstants.SBMLVERSION) 
			IJ.log("model is not level 3 version 1");
		
		return model.getLevel() == PluginConstants.SBMLLEVEL  &&  model.getVersion() == PluginConstants.SBMLVERSION;
	}
	
	/**
	 * Check extension.
	 *
	 * @return true, if successful
	 */
	private boolean checkExtension(){
		if(!document.getPackageRequired("spatial"))
			IJ.log("model missing extension spatial");
		
		return document.getPackageRequired("spatial");
	}
	
	private void checkMissingBourdariesConditions() {
		SpatialModelPlugin spatialPlugin = (SpatialModelPlugin) model.getPlugin(SpatialConstants.namespaceURI);
		Geometry geometry = spatialPlugin.getGeometry();
		int dimensions = geometry.getListOfCoordinateComponents().size();
		int maxBoundaryCondition = dimensions*2 + 1;
		
		ListOf<Parameter> lop = model.getListOfParameters();
		HashMap<String,Set<String>> speciesBoundSet = new HashMap<String,Set<String>>(); 
		for(Parameter p: lop) {
			SpatialParameterPlugin spp = (SpatialParameterPlugin) p.getPlugin(SpatialConstants.namespaceURI);
			if(spp.getParamType() instanceof BoundaryCondition) {
				BoundaryCondition bc =  (BoundaryCondition) spp.getParamType();
				
				String speciesID = bc.getVariable();
				String bound;
				if(bc.isSetCoordinateBoundary())
					bound = bc.getCoordinateBoundary();
				else
					bound = bc.getBoundaryDomainType();
				
				if(!speciesBoundSet.containsKey(speciesID)) {
					Set<String> hash = new HashSet<String>();
					hash.add(bound);
					speciesBoundSet.put(speciesID, hash);
				} else {
					speciesBoundSet.get(speciesID).add(bound);
				}
			}
		}
		
		ListOf<Species> los = model.getListOfSpecies();
		ListOf<CoordinateComponent> locc = geometry.getListOfCoordinateComponents();
		Set<String> coordSet = new HashSet<String>();
		for(CoordinateComponent cc: locc) {
			coordSet.add(cc.getBoundaryMaximum().getSpatialId());
			coordSet.add(cc.getBoundaryMinimum().getSpatialId());
		}
		
		for(Species s: los) {
			SpatialSpeciesPlugin ssp = (SpatialSpeciesPlugin) s.getPlugin(SpatialConstants.namespaceURI);
			if(ssp == null || !ssp.isSetSpatial())
				continue;
			
			if( speciesBoundSet.containsKey(s.getId()) && speciesBoundSet.get(s.getId()).size() == maxBoundaryCondition)
				continue;
			
			StringBuilder errorStr = new StringBuilder();
			errorStr.append("Warning missing boundary condition for Species " + s.getId() + " on boundaries:");

			if(!speciesBoundSet.containsKey(s.getId())) {
				for(String str : coordSet)
						errorStr.append(" " + str);

				errorStr.append(" " + s.getCompartment());

			} else {

			Set<String> speciesBound = speciesBoundSet.get(s.getId());
			
			for(String str : coordSet) {
				if(!speciesBound.contains(str))
					errorStr.append(" " + str);
			}

			if(!speciesBound.contains(s.getCompartment()))
				errorStr.append(" " + s.getCompartment());
			}
			
			IJ.log(errorStr.toString());
		}
		
	}
	
	/**
	 * Validate.
	 */
	public void validate(){
		boolean hasRequiredAttribute = checkModelVersion() && checkExtension();
		
		if(!hasRequiredAttribute)
			return;
		
		document.setConsistencyChecks(CHECK_CATEGORY.GENERAL_CONSISTENCY, true);
		document.setConsistencyChecks(CHECK_CATEGORY.IDENTIFIER_CONSISTENCY, true);
		document.setConsistencyChecks(CHECK_CATEGORY.MODELING_PRACTICE, true);
		document.checkConsistency();
		SBMLErrorLog errorLog = document.getListOfErrors();
		List<SBMLError> errorList = errorLog.getValidationErrors();
		for (SBMLError e : errorList) {
			IJ.log("Line " + e.getLine() + ": " + e.getMessage());
		}
		
		checkMissingBourdariesConditions();
	}

	public static void main(String[] args) {
		SBMLDocument d;
			try {
				d = SBMLReader.read(new File("/Users/ii/Desktop/model/test.xml"));
				new ModelValidator(d).validate();

			} catch (XMLStreamException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	
	}
}
