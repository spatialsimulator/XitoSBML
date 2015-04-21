
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.zip.Deflater;

import org.sbml.libsbml.AdjacentDomains;
import org.sbml.libsbml.Boundary;
import org.sbml.libsbml.ChangedMath;
import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.CompartmentMapping;
import org.sbml.libsbml.CoordinateComponent;
import org.sbml.libsbml.Domain;
import org.sbml.libsbml.DomainType;
import org.sbml.libsbml.Geometry;
import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.ReqSBasePlugin;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLNamespaces;
import org.sbml.libsbml.SBasePlugin;
import org.sbml.libsbml.SampledField;
import org.sbml.libsbml.SampledFieldGeometry;
import org.sbml.libsbml.SampledVolume;
import org.sbml.libsbml.SpatialCompartmentPlugin;
import org.sbml.libsbml.SpatialModelPlugin;
import org.sbml.libsbml.SpatialParameterPlugin;
import org.sbml.libsbml.SpatialPkgNamespaces;
import org.sbml.libsbml.SpatialSymbolReference;
import org.sbml.libsbml.Unit;
import org.sbml.libsbml.UnitDefinition;
import org.sbml.libsbml.libsbmlConstants;

/**
 *
 */

/**
 * @author Akira Funahashi
 *
 */
public class SpatialSBMLExporter implements libsbmlConstants{

  SBMLDocument document;
  Model model;
  SBMLNamespaces sbmlns;                       //class to store SBML Level, version, namespace
  SpatialPkgNamespaces spatialns;
  SpatialModelPlugin spatialplugin;
  ReqSBasePlugin reqplugin;
  SpatialCompartmentPlugin spatialcompplugin;
  Geometry geometry;
  HashMap<String, Integer> hashDomainTypes;     //store domain type with corresponding dimension
  HashMap<String, Integer> hashSampledValue;
  HashMap<String, Integer> hashDomainNum;
  ArrayList<ArrayList<String>> adjacentsList;
  byte[] raw;
  int matrix[];
  int width, height, depth;

  /**
   *
   */
  public SpatialSBMLExporter() {                    //builds the framework of SBML document

	sbmlns = new SBMLNamespaces(3,1);           //create SBML name space with level 3 version 1
    sbmlns.addPackageNamespace("req", 1);
    sbmlns.addPackageNamespace("spatial", 1);
    // SBML Document
    document = new SBMLDocument(sbmlns); 
    document.setPackageRequired("req", true);        //set req package as required
    document.setPackageRequired("spatial", true);    //set spatial package as required
    model = document.createModel();  //create model using the document and return pointer


    // Create Spatial
    //
    // set the SpatialPkgNamespaces for Level 3 Version 1 Spatial Version 1
    //
    spatialns = new SpatialPkgNamespaces(3, 1, 1);    //create spatial package name space
    //
    // Get a SpatialModelPlugin object plugged in the model object.
    //
    // The type of the returned value of SBase::getPlugin() function is SBasePlugin, and
    // thus the value needs to be casted for the corresponding derived class.
    //
    reqplugin = (ReqSBasePlugin)model.getPlugin("req");  //get required elements plugin
    SBasePlugin basePlugin = (model.getPlugin ("spatial"));
    spatialplugin = (SpatialModelPlugin)basePlugin;                  //get spatial plugin
    if (spatialplugin == null) {
      System.err.println("[Fatal Error] Layout Extension Level " + spatialns.getLevel () + " Version " + spatialns.getVersion () + " package version " + spatialns.getPackageVersion () + " is not registered.");
      System.exit(1);
    }

	  spatialns = new SpatialPkgNamespaces(3, 1, 1); 
  }

  public SpatialSBMLExporter(SpatialImage spImg, SBMLDocument document) {
	    this();
	    this.hashDomainTypes = spImg.hashDomainTypes;
	    this.hashSampledValue = spImg.hashSampledValue;
	    this.hashDomainNum = spImg.hashDomainNum;
	    this.raw = spImg.raw;
	    this.width = spImg.width;
	    this.height = spImg.height;
	    this.depth = spImg.depth;
	    this.adjacentsList = spImg.adjacentsList;
	    this.document = document;
	    model = document.getModel();
	    spatialplugin = (SpatialModelPlugin) model.getPlugin("spatial");
	  }

  
  public void createGeometryElements() {
    // Creates a Geometry object via SpatialModelPlugin object.
    geometry = spatialplugin.createGeometry();     //get geometry of spatial plugin
    geometry.setCoordinateSystem("Cartesian");  //set to Cartesian coordinate
    addCoordinates();                      
    addDomainTypes();                         
    addDomains();                           
    addAdjacentDomains();  
    addGeometryDefinitions();   
    addCoordParameter();
  }

  public void addGeometryDefinitions(){
    SampledFieldGeometry sfg = geometry.createSampledFieldGeometry();   //create new geometry definition and add to ListOfGeometryDefinitions list
    sfg.setId("mySampledField");
 
    for (Entry<String, Integer> e : hashDomainTypes.entrySet()) {
      if (e.getValue() == 3) {                                      //if dimensions is 3
    	SampledVolume sv = sfg.createSampledVolume();
        sv.setId(e.getKey()); sv.setDomainType(e.getKey());
        sv.setSampledValue( hashSampledValue.get(e.getKey())); sv.setMinValue(0); sv.setMaxValue(0);
      }
    }
    SampledField sf = geometry.createSampledField();
    sf.setId("imgtest"); sf.setDataType(SPATIAL_DATAKIND_UINT8);
    sf.setInterpolationType(SPATIAL_INTERPOLATIONKIND_NEARESTNEIGHBOR); sf.setCompression(SPATIAL_COMPRESSIONKIND_DEFLATED);
    sf.setNumSamples1(width); sf.setNumSamples2(height); sf.setNumSamples3(depth);

    byte[] compressed = compressRawData(raw);
    if (compressed != null) {
    	sf.setSamples(byteArrayToIntArray(compressed),compressed.length); // see below byteArrayToIntArray
    }
  }

  public byte[] compressRawData(byte[] raw) {           //compression of image
    Deflater compresser = new Deflater();
    compresser.setLevel(Deflater.BEST_COMPRESSION);
    compresser.setInput(raw);
    compresser.finish();
    int size;
    byte[] buffer = new byte[1024];
    byte[] compressed = null;
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    while(true) {
      size = compresser.deflate(buffer);              //insert compressed data
      stream.write(buffer, 0, size);                  //write compressed date to buffer
      if(compresser.finished()) {
        break;
      }
    }
    compressed = stream.toByteArray();
    try {
      stream.close();
    } catch (IOException e1) {
      e1.printStackTrace();
      return null;
    }
    return compressed;
  }

  public int[] byteArrayToIntArray(byte[] compressed) {
    int[] intArray = new int[compressed.length];
    for (int i = 0; i < compressed.length; i++) {
      intArray[i] = compressed[i] & 0xff;
    }
    return intArray;
  }

  public void addAdjacentDomains() {		//adds membrane domains and adjacents
	  WeakHashMap<String, Integer> hashMembrane = new WeakHashMap<String,Integer>();   
	  for(ArrayList<String> e : adjacentsList){
		 String one = e.get(0).substring(0, e.get(0).length());
		 one = one.replaceAll("[0-9]","");
		 String two = e.get(1).substring(0, e.get(1).length());
		 two = two.replaceAll("[0-9]","");
		 DomainType dt = geometry.getDomainType(one + "_" + two + "_membrane");
		 if(hashMembrane.containsKey(dt.getId())){
			 hashMembrane.put(dt.getId(), hashMembrane.get(dt.getId()) + 1);
		 }else{
			 hashMembrane.put(dt.getId(), 0);
		 }
		 
		  for (int i = 0; i < 2; i++) {                           //add info about adjacent domain
			  AdjacentDomains adj = geometry.createAdjacentDomains();
			  adj.setId(dt.getId() + "_" + e.get(i));
			  adj.setDomain1(dt.getId() + hashMembrane.get(dt.getId()));
			  adj.setDomain2(e.get(i));
		  }
	  }
  }

  public static int unsignedToBytes(byte b) {
	  return b & 0xFF;
  }

  public void addDomains() {
     ListOf lodom = geometry.getListOfDomains();
     for(Entry<String,Integer> e : hashDomainTypes.entrySet()){    			//add domains to corresponding domaintypes
 		DomainType dt = geometry.getDomainType(e.getKey());
		Domain dom = new Domain();
			if (dt.getId().matches(".*membrane")) {
				for (int i = 0; i < hashDomainNum.get(e.getKey()); i++) {
					dom.setId(dt.getId() + i);
					dom.setDomainType(dt.getId());
					//dom.setImplicit(true);
					lodom.append(dom);
				}
			} else {
				for (int i = 0; i < hashDomainNum.get(e.getKey()); i++) { // add each domain
					dom.setId(dt.getId() + i);
					dom.setDomainType(dt.getId());
					//dom.setImplicit(false);
					lodom.append(dom);
				}
			}
     }
     
  }

  public void addDomainTypes() {                        //create domain types, domain, compartment info
    for (Entry<String, Integer> e : hashDomainTypes.entrySet()) { 
    	// DomainTypes
    	DomainType dt = geometry.createDomainType();
      dt.setId(e.getKey()); dt.setSpatialDimensions(e.getValue());
      // Compartment								may need changes for name and id
      Compartment c = model.createCompartment();
      c.setSpatialDimensions(e.getValue());
      c.setConstant(true);                      //set compartment as a constant
      c.setId(e.getKey()); c.setName(e.getKey());

      spatialcompplugin = (SpatialCompartmentPlugin)c.getPlugin("spatial");   //create compartment mapping which relates compartment and domain type
      CompartmentMapping cm = spatialcompplugin.createCompartmentMapping();
      cm.setId(e.getKey() + c.getId());
      cm.setDomainType(e.getKey());
      cm.setUnitSize(1);
    }
  }

  public void addCoordinates() {                                //add coordinates x and y
    CoordinateComponent ccx = geometry.createCoordinateComponent();
    ccx.setId("x"); ccx.setType("cartesianX"); ccx.setUnit("um");
    setCoordinateBoundary(ccx, "X", 0, width);
    CoordinateComponent ccy = geometry.createCoordinateComponent();
    ccy.setId("y"); ccy.setType("cartesianY"); ccy.setUnit("um");
    setCoordinateBoundary(ccy, "Y", 0, height);
		if (depth != 1) {
			CoordinateComponent ccz = geometry.createCoordinateComponent();
			ccz.setId("z");
			ccz.setType("cartesianZ");
			ccz.setUnit("um");
			setCoordinateBoundary(ccz, "Z", 0, depth);
		} 
  }

  public void setCoordinateBoundary(CoordinateComponent cc, String s, double min, double max) { 
	  Boundary bmin = cc.createBoundaryMin();
	  bmin.setId(s + "min"); bmin.setValue(min);
	  Boundary bmax = cc.createBoundaryMax();
	  bmax.setId(s + "max"); bmax.setValue(max);
	}
  
  public void addCoordParameter(){
	 ListOf lcc = geometry.getListOfCoordinateComponents();
	 Parameter p ;
	 CoordinateComponent cc;	 
	for (int i = 0; i < lcc.size(); i++) {
		cc = (CoordinateComponent) lcc.get(i);
		p = model.createParameter();
		p.setId(cc.getId());
		p.setValue(0);
		SpatialParameterPlugin sp = (SpatialParameterPlugin) p.getPlugin("spatial");
		
		SpatialSymbolReference ssr = sp.createSpatialSymbolReference();
		ssr.setId(cc.getId());
		ssr.setSpatialRef("spatial");
		ReqSBasePlugin rsb = (ReqSBasePlugin) p.getPlugin("req");
		ChangedMath cm = rsb.createChangedMath(); 
		cm.setChangedBy("spatial");
		cm.setViableWithoutChange(true);
	}
  }	
  
  public void addUnitDefinition(){
	Unit u = model.createUnit();
	u.setKind(UNIT_KIND_ITEM);u.setExponent(1);u.setScale(0);u.setMultiplier(1);
	UnitDefinition ud = model.createUnitDefinition(); ud.setId("substance");
	ud.addUnit(u);
  }
}
