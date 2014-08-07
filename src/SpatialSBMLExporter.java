
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.Deflater;

import org.sbml.libsbml.AdjacentDomains;
import org.sbml.libsbml.BoundaryMax;
import org.sbml.libsbml.BoundaryMin;
import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.CompartmentMapping;
import org.sbml.libsbml.CoordinateComponent;
import org.sbml.libsbml.Domain;
import org.sbml.libsbml.DomainType;
import org.sbml.libsbml.Geometry;
import org.sbml.libsbml.ImageData;
import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.RequiredElementsSBasePlugin;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLNamespaces;
import org.sbml.libsbml.SBasePlugin;
import org.sbml.libsbml.SampledField;
import org.sbml.libsbml.SampledFieldGeometry;
import org.sbml.libsbml.SampledVolume;
import org.sbml.libsbml.SpatialCompartmentPlugin;
import org.sbml.libsbml.SpatialModelPlugin;
import org.sbml.libsbml.SpatialPkgNamespaces;
import org.sbml.libsbml.libsbml;

/**
 *
 */

/**
 * @author Akira Funahashi
 *
 */
public class SpatialSBMLExporter {
  static {
    System.loadLibrary("sbmlj");                //read system library sbmlj
  }
  SBMLDocument document;
  Model model;
  SBMLNamespaces sbmlns;                       //class to store SBML Level, version, namespace
  SpatialPkgNamespaces spatialns;
  SpatialModelPlugin spatialplugin;
  SpatialCompartmentPlugin spatialcompplugin;
  RequiredElementsSBasePlugin reqplugin;
  Geometry geometry;
  HashMap<String, Integer> hashDomainTypes;     //store domain type with corresponding dimension
  HashMap<String, Integer> hashSampledValue;
  HashMap<Integer,Integer> hashDomainNum;
  byte[] raw;
  int matrix[];
  int width, height, depth;

  /**
   *
   */
  public SpatialSBMLExporter() {                    //builds the framework of SBML document
    sbmlns = new SBMLNamespaces(3,1);           //create SBML name space with level 3 version 1
    sbmlns.addPackageNamespace("req", 1);   //add required element package
    sbmlns.addPackageNamespace("spatial", 1);  //add spatial processes package
    // SBML Document
    document = new SBMLDocument(sbmlns);              //construct document with name space
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
    reqplugin = (RequiredElementsSBasePlugin)model.getPlugin("req");  //get required elements plugin
    reqplugin.setMathOverridden("spatial");                           //req set overridden as spatial
    reqplugin.setCoreHasAlternateMath(true);                          //

    SBasePlugin basePlugin = (model.getPlugin ("spatial"));
    spatialplugin = (SpatialModelPlugin)basePlugin;                  //get spatial plugin
    if (spatialplugin == null) {
      System.err.println("[Fatal Error] Layout Extension Level " + spatialns.getLevel () + " Version " + spatialns.getVersion () + " package version " + spatialns.getPackageVersion () + " is not registered.");
      System.exit(1);
    }
  //  width = 5; height = 5; depth = 1;
  }

  public SpatialSBMLExporter(RawSpatialImage ri) {
    this();
    this.hashDomainTypes = ri.hashDomainTypes;
    this.hashSampledValue = ri.hashSampledValue;
    this.raw = ri.raw;
    this.width = ri.width;
    this.height = ri.height;
    this.depth = ri.depth;
  }

  public void createGeometryElements() {          //creates the components and geometry layer of SBML
    // Create compartments
    /*
       Compartment c1 = model.createCompartment();
       reqplugin = (RequiredElementsSBasePlugin)c1.getPlugin("req");
       reqplugin.setMathOverridden("spatial");
       reqplugin.setCoreHasAlternateMath(true);
       Compartment c2 = model.createCompartment();
       reqplugin = (RequiredElementsSBasePlugin)c2.getPlugin("req");
       reqplugin.setMathOverridden("spatial");
       reqplugin.setCoreHasAlternateMath(true);
       Compartment c3 = model.createCompartment();
       reqplugin = (RequiredElementsSBasePlugin)c3.getPlugin("req");
       reqplugin.setMathOverridden("spatial");
       reqplugin.setCoreHasAlternateMath(true);
       c1.setId("cytosol");
       c1.setConstant(true);
       c2.setId("extracellular");
       c2.setConstant(true);
       c3.setId("nucleus");
       c3.setConstant(true);
       */
    //
    // Creates a Geometry object via SpatialModelPlugin object.
    //
    geometry = spatialplugin.getGeometry();     //get geometry of spatial plugin
    geometry.setCoordinateSystem("Cartesian");  //set to Cartesian coordinate
    addCoordinates();                           //see below
    addDomainTypes();                           //see below
    addDomains();                               //see below
    addAdjacentDomains();                       //see below
    addGeometryDefinitions();                   //see below
  }

  public void addGeometryDefinitions(){
    SampledFieldGeometry sfg = geometry.createSampledFieldGeometry();   //create new geometry definition and add to ListOfGeometryDefinitions list
    sfg.setSpatialId("mySampledField");                       //inherit from AbstractSpatialNamedSBase
    ListOf losg = sfg.getListOfSampledVolumes();              //get ListOfSampledVolumes

    for (Entry<String, Integer> e : hashDomainTypes.entrySet()) {
      if (e.getValue() == 3) {                                      //if dimensions is 3
        SampledVolume sv = new SampledVolume();
        sv.setSpatialId(e.getKey()); sv.setDomainType(e.getKey());
        sv.setSampledValue( hashSampledValue.get(e.getKey())); sv.setMinValue(0); sv.setMaxValue(0);
        losg.append(sv);
      }
    }

    SampledField sf = sfg.createSampledField();     //create SampleField represent number of coordinates in each
    sf.setSpatialId("imgtest"); sf.setDataType("integer");
    sf.setInterpolationType("linear"); sf.setEncoding("compressed");
    sf.setNumSamples1(width); sf.setNumSamples2(height); sf.setNumSamples3(depth);

    ImageData idata = sf.createImageData();          //create ImageData
    byte[] compressed = compressRawData(raw);
    if (compressed != null) {
      idata.setSamples(byteArrayToIntArray(compressed), compressed.length);     //see below byteArrayToIntArray
      idata.setDataType("compressed");
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
      intArray[i] = compressed[i] & 0xff;       //cast byte to int (0xff = 255)
    }
    return intArray;
  }

  public void addAdjacentDomains() {

  }

	public static int unsignedToBytes(byte b) {
	    return b & 0xFF;
	  }

  public void addDomains() {
        //count number of objects with certain pixel value
        matrix = new int[height*width];   //identical size of matrix with image
        matrix[0] = 0;
        List<Integer> pixel= new ArrayList<Integer>(hashSampledValue.values());
        HashMap<Integer,Integer> num = new HashMap<Integer,Integer>();  //labels the object in a different number
        int label = 0;
        for(int i = 0 ; i < hashSampledValue.size() ; i++){
          num.put(hashSampledValue.get(i), label);
          label += 10;
        }
    for(int i = 0 ; i < height ; i++){
      for(int j = 0 ; j < width ; j++){
        if(matrix[i * height + j] == 0 && raw[i * height + j] != 0){
          int var = num.get(unsignedToBytes(raw[i * height + j]));
          matrix[i * height + j] = var;
          recurs(i,j,width,height);
          var++;
          num.remove(unsignedToBytes(raw[i * height + j]));
          num.put( unsignedToBytes(raw[i * height + j]),var);
        }
      }
    }

    //count number of domains in each domaintype
    hashDomainNum = new HashMap<Integer,Integer>();
    System.out.println("domain");
    for(int i = 0 ; i < hashSampledValue.size() ; i++){
      hashDomainNum.put(pixel.get(i), num.get(pixel.get(i)) % 10);
      Integer temp = num.get(pixel.get(i)) % 10;
      System.out.println(pixel.get(i).toString() + " " + temp.toString());
    }

    

     ListOf lodom = geometry.getListOfDomains();
     
     for(Entry<String,Integer> e : hashDomainTypes.entrySet()){    			//add domains to corresponding domaintypes
		DomainType dt =geometry.getDomainType(e.getKey());
    	 for(int i = 0 ; i < hashDomainNum.get(e); i++){     //add each domain
			Domain dom = new Domain();
			dom.setSpatialId(dt.getSpatialId() + i);
			dom.setImplicit(false);
			lodom.append(dom);
    	}
     }
     
  }

  public void addDomainTypes() {                        //create domain types, domain, compartment info
    ListOf lodt = geometry.getListOfDomainTypes();
    ListOf lodom = geometry.getListOfDomains();
    ListOf loadj = geometry.getListOfAdjacentDomains();

    for (Entry<String, Integer> e : hashDomainTypes.entrySet()) {       //for each domain types
    	// DomainTypes
      DomainType dt = new DomainType();
      dt.setSpatialId(e.getKey()); dt.setSpatialDimensions(e.getValue());
      lodt.append(dt);

      // Domains    should be deleted
      Domain dom = new Domain();
      dom.setSpatialId(dt.getSpatialId() + "0");
      dom.setDomainType(dt.getSpatialId());
      if (dt.getSpatialId().matches(".*membrane.*")) {    //membrane related domains' implicit are set to true  to adjacent domain
        dom.setImplicit(true);
        String[] domname = dt.getSpatialId().split("_", 0);
        for (int i = 0; i < 2; i++) {                           //add info about adjacent domain
          AdjacentDomains adj = new AdjacentDomains();                    //adjacent domain only account for membrane cytosol+ extracelluar matrix and cytosol + nucleus
          adj.setSpatialId(dt.getSpatialId() + "_" + domname[i] + "0");
          adj.setDomain1(dt.getSpatialId() + "0");
          adj.setDomain2(domname[i] + "0");
          loadj.append(adj);
        }
      } else {
        dom.setImplicit(false);
      }
      lodom.append(dom);

      // Compartment
      Compartment c = model.createCompartment();
      c.setSpatialDimensions(e.getValue());
      c.setConstant(true);                      //set compartment as a constant
      if (e.getKey().equals("Cyt")) {
        c.setId("cytosol"); c.setName("cytosol");
      } else if (e.getKey().equals("EC")) {
        c.setId("extracellular"); c.setName("extracellular");
      } else if (e.getKey().equals("Nuc")) {
        c.setId("nucleus"); c.setName("nucleus");
      } else if (e.getKey().equals("Cyt_EC_membrane")) {
        c.setId("PM"); c.setName("PM");
      } else if (e.getKey().equals("Cyt_Nuc_membrane")) {
        c.setId("NM"); c.setName("NM");
      }
      spatialcompplugin = (SpatialCompartmentPlugin)c.getPlugin("spatial");   //create compartment mapping which relates compartment and domain type
      CompartmentMapping cm = spatialcompplugin.getCompartmentMapping();
      cm.setSpatialId(e.getKey()+c.getId());
      cm.setCompartment(c.getId());
      cm.setDomainType(e.getKey());
      cm.setUnitSize(1);
    }


  }

  public void addCoordinates() {                                //add coordinates x and y
    ListOf lcc = geometry.getListOfCoordinateComponents();
    CoordinateComponent ccx = new CoordinateComponent(spatialns);
    CoordinateComponent ccy = new CoordinateComponent(spatialns);
    CoordinateComponent ccz = new CoordinateComponent(spatialns);
    ccx.setSpatialId("x"); ccx.setComponentType("cartesianX"); ccx.setIndex(0); ccx.setSbmlUnit("um");    //setIndex, micrometer
    ccy.setSpatialId("y"); ccy.setComponentType("cartesianY"); ccy.setIndex(1); ccy.setSbmlUnit("um");
    ccz.setSpatialId("z"); ccz.setComponentType("cartesianZ"); ccz.setIndex(2); ccz.setSbmlUnit("um");
    setCoordinateBoundary(ccx, "X", 0, width);
    setCoordinateBoundary(ccy, "Y", 0, height);
    setCoordinateBoundary(ccz, "Z", 0, depth);
    lcc.append(ccx);                                //add coordinate x to listOfCoordinateComponents
    lcc.append(ccy);                               //add coordinate y to listOfCoordinateComponents
    lcc.append(ccz);                               //add coordinate z to listOfCoordinateComponents
  }

  public void setCoordinateBoundary(CoordinateComponent cc, String s, double min, double max) {         //set coordinate boundaries
    if (cc.getBoundaryMin() == null) cc.setBoundaryMin(new BoundaryMin(spatialns));
    if (cc.getBoundaryMax() == null) cc.setBoundaryMax(new BoundaryMax(spatialns));
    cc.getBoundaryMin().setSpatialId(s+"min");
    cc.getBoundaryMin().setValue(min);
    cc.getBoundaryMax().setSpatialId(s+"max");
    cc.getBoundaryMax().setValue(max);
  }

public void recurs(int i, int j, int width, int height){
    //check right
    if(j != width - 1 && raw[i * height + j + 1]== raw[i * height + j] && matrix[i * height + j + 1] == 0){
      matrix[i * height + j + 1] = matrix[i * height + j];
      recurs(i,j+1,width,height);
    }

    //check left
    if(j != 0 && raw[i * height + j - 1] == raw[i * height + j] && matrix[i * height + j - 1] == 0){
      matrix[i * height + j - 1] = matrix[i * height + j];
      recurs(i,j-1,width,height);
    }

    //check down
    if(i != height - 1 && raw[(i+1) * height + j] == raw[i * height + j] && matrix[(i+1) * height + j] == 0){
      matrix[(i + 1) * height + j] = matrix[i * height + j];
      recurs(i+1,j,width,height);
    }

    //check up
    if(i != 0 && raw[(i-1) * height + j ] == raw[i * height + j] && matrix[(i-1) * height + j] == 0){
      matrix[(i - 1) * height + j] = matrix[i * height + j];
      recurs(i-1,j,width,height);
    }

  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    HashMap<String, Integer> hashDomainTypes = new HashMap<String, Integer>();
    hashDomainTypes.put("EC", 3);
    hashDomainTypes.put("Nuc", 3);
    hashDomainTypes.put("Cyt", 3);
    hashDomainTypes.put("Cyt_EC_membrane", 2);
    hashDomainTypes.put("Cyt_Nuc_membrane", 2);
    HashMap<String, Integer> hashSampledValue = new HashMap<String, Integer>();
    hashSampledValue.put("EC", 0);
    hashSampledValue.put("Nuc", 1);
    hashSampledValue.put("Cyt", 2);
    byte[] raw = {                  //need to refer to spatial_SBML to acquire data
      // z=0

         0,1,1,1,0,
         1,1,2,1,1,
         1,2,2,2,1,
         1,1,2,1,1,
         0,1,1,1,0
      /*
      0,1,1,1,
      1,1,2,1,
      1,2,2,2,
      1,1,2,1
*/
    };
    RawSpatialImage ri = new RawSpatialImage(raw, (int)Math.sqrt((double)raw.length), (int)Math.sqrt((double)raw.length), 1, hashDomainTypes, hashSampledValue);  //why does the length need to be squarerooted?
    SpatialSBMLExporter ts = new SpatialSBMLExporter(ri);
    ts.createGeometryElements();
    System.out.println(ts.document.getModel().getId());
    libsbml.writeSBMLToFile(ts.document, "out2.xml");
  }

}
