import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
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
    System.loadLibrary("sbmlj");
  }
  SBMLDocument document;
  Model model;
  SBMLNamespaces sbmlns;
  SpatialPkgNamespaces spatialns;
  SpatialModelPlugin spatialplugin;
  SpatialCompartmentPlugin spatialcompplugin;
  RequiredElementsSBasePlugin reqplugin;
  Geometry geometry;
  HashMap<String, Integer> hashDomainTypes;
  byte[] raw;
  int width, height, depth;

  /**
   * 
   */
  public SpatialSBMLExporter() {
    sbmlns = new SBMLNamespaces(3,1);
    sbmlns.addPackageNamespace("req", 1);
    sbmlns.addPackageNamespace("spatial", 1);
    // SBML Document
    document = new SBMLDocument(sbmlns);
    document.setPackageRequired("req", true);
    document.setPackageRequired("spatial", true);
    model = document.createModel("mySpatialModel");
    // Create Spatial
    //
    // set the SpatialPkgNamespaces for Level 3 Version 1 Spatial Version 1
    //
    spatialns = new SpatialPkgNamespaces(3, 1, 1);
    //
    // Get a SpatialModelPlugin object plugged in the model object.
    //
    // The type of the returned value of SBase::getPlugin() function is SBasePlugin, and
    // thus the value needs to be casted for the corresponding derived class.
    //
    reqplugin = (RequiredElementsSBasePlugin)model.getPlugin("req");
    reqplugin.setMathOverridden("spatial");
    reqplugin.setCoreHasAlternateMath(true);

    SBasePlugin basePlugin = (model.getPlugin ("spatial"));
    spatialplugin = (SpatialModelPlugin)basePlugin;
    if (spatialplugin == null) {
      System.err.println("[Fatal Error] Layout Extension Level " + spatialns.getLevel () + " Version " + spatialns.getVersion () + " package version " + spatialns.getPackageVersion () + " is not registered.");
      System.exit(1);
    }
    width = 5; height = 5; depth = 1;
  }

  public SpatialSBMLExporter(HashMap<String, Integer> hash, RawSpatialImage ri) {
    this();
    this.hashDomainTypes = hash; 
    this.raw = ri.raw;
    this.width = ri.width;
    this.height = ri.height;
    this.depth = ri.depth;
  }

  public void createGeometryElements() {
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
    geometry = spatialplugin.getGeometry();
    geometry.setCoordinateSystem("Cartesian");
    addCoordinates();	
    addDomainTypes();
    addDomains();
    addAdjacentDomains();
    addGeometryDefinitions();
  }

  public void addGeometryDefinitions() {
    SampledFieldGeometry sfg = geometry.createSampledFieldGeometry();
    sfg.setSpatialId("mySampledField");
    ListOf losg = sfg.getListOfSampledVolumes();
    int i = 0;
    for (Entry<String, Integer> e : hashDomainTypes.entrySet()) {
      if (e.getValue() == 3) {
        SampledVolume sv = new SampledVolume();
        sv.setSpatialId(e.getKey()); sv.setDomainType(e.getKey());
        sv.setSampledValue(i); sv.setMinValue(0); sv.setMaxValue(0);
        losg.append(sv);
        i++;
      }
    }
    SampledField sf = sfg.createSampledField();
    sf.setSpatialId("imgtest"); sf.setDataType("integer");
    sf.setInterpolationType("linear"); sf.setEncoding("compressed");
    sf.setNumSamples1(width); sf.setNumSamples2(height); sf.setNumSamples3(depth);
    ImageData idata = sf.createImageData();
    byte[] compressed = compressRawData(raw);
    if (compressed != null) {
      idata.setSamples(byteArrayToIntArray(compressed), compressed.length);
      idata.setDataType("compressed");
    }
  }

  public byte[] compressRawData(byte[] raw) {
    Deflater compresser = new Deflater();
    compresser.setLevel(Deflater.BEST_COMPRESSION);
    compresser.setInput(raw);
    compresser.finish();
    int size;
    byte[] buffer = new byte[1024];
    byte[] compressed = null;
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    while(true) {
      size = compresser.deflate(buffer);
      stream.write(buffer, 0, size);
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

  public void addAdjacentDomains() {

  }

  public void addDomains() {

  }

  public void addDomainTypes() {
    ListOf lodt = geometry.getListOfDomainTypes();
    ListOf lodom = geometry.getListOfDomains();
    ListOf loadj = geometry.getListOfAdjacentDomains();
    for (Entry<String, Integer> e : hashDomainTypes.entrySet()) {
      // DomainTypes
      DomainType dt = new DomainType();
      dt.setSpatialId(e.getKey()); dt.setSpatialDimensions(e.getValue());
      lodt.append(dt);
      // Domains
      Domain dom = new Domain();
      dom.setSpatialId(dt.getSpatialId() + "0");
      dom.setDomainType(dt.getSpatialId());
      if (dt.getSpatialId().matches(".*membrane.*")) {
        dom.setImplicit(true);
        String[] domname = dt.getSpatialId().split("_", 0);
        for (int i = 0; i < 2; i++) {
          AdjacentDomains adj = new AdjacentDomains();
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
      c.setConstant(true);
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
      spatialcompplugin = (SpatialCompartmentPlugin)c.getPlugin("spatial");
      CompartmentMapping cm = spatialcompplugin.getCompartmentMapping();
      cm.setSpatialId(e.getKey()+c.getId());
      cm.setCompartment(c.getId());
      cm.setDomainType(e.getKey());
      cm.setUnitSize(1);
    }


  }

  public void addCoordinates() {
    ListOf lcc = geometry.getListOfCoordinateComponents();
    CoordinateComponent ccx = new CoordinateComponent(spatialns);
    CoordinateComponent ccy = new CoordinateComponent(spatialns);
    ccx.setSpatialId("x"); ccx.setComponentType("cartesianX"); ccx.setIndex(0); ccx.setSbmlUnit("um");
    ccy.setSpatialId("y"); ccy.setComponentType("cartesianY"); ccy.setIndex(1); ccy.setSbmlUnit("um");
    setCoordinateBoundary(ccx, "X", 0, 48.54);
    setCoordinateBoundary(ccy, "Y", 0, 71.24);
    lcc.append(ccx);
    lcc.append(ccy);
  }

  public void setCoordinateBoundary(CoordinateComponent cc, String s, double min, double max) {
    if (cc.getBoundaryMin() == null) cc.setBoundaryMin(new BoundaryMin(spatialns));
    if (cc.getBoundaryMax() == null) cc.setBoundaryMax(new BoundaryMax(spatialns));
    cc.getBoundaryMin().setSpatialId(s+"min");
    cc.getBoundaryMin().setValue(min);
    cc.getBoundaryMax().setSpatialId(s+"max");
    cc.getBoundaryMax().setValue(max);
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
    byte[] raw = {
      // z=0
      /*
         0,1,1,1,0,
         1,1,2,1,1,
         1,2,2,2,1,
         1,1,2,1,1,
         0,1,1,1,0
         */
      0,1,1,1,
      1,1,2,1,
      1,2,2,2,
      1,1,2,1
    };
    RawSpatialImage ri = new RawSpatialImage(raw, (int)Math.sqrt((double)raw.length), (int)Math.sqrt((double)raw.length), 1, hashDomainTypes, hashSampledValue);
    SpatialSBMLExporter ts = new SpatialSBMLExporter(hashDomainTypes, ri);
    ts.createGeometryElements();
    System.out.println(ts.document.getModel().getId());
    libsbml.writeSBMLToFile(ts.document, "out.xml");
  }

}
