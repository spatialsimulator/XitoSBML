package jp.ac.keio.bio.fun.xitosbml.xitosbml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.zip.Deflater;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.Unit.Kind;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.ext.SBasePlugin;
import org.sbml.jsbml.ext.spatial.AdjacentDomains;
import org.sbml.jsbml.ext.spatial.Boundary;
import org.sbml.jsbml.ext.spatial.CompartmentMapping;
import org.sbml.jsbml.ext.spatial.CompressionKind;
import org.sbml.jsbml.ext.spatial.CoordinateComponent;
import org.sbml.jsbml.ext.spatial.CoordinateKind;
import org.sbml.jsbml.ext.spatial.DataKind;
import org.sbml.jsbml.ext.spatial.Domain;
import org.sbml.jsbml.ext.spatial.DomainType;
import org.sbml.jsbml.ext.spatial.Geometry;
import org.sbml.jsbml.ext.spatial.GeometryKind;
import org.sbml.jsbml.ext.spatial.InteriorPoint;
import org.sbml.jsbml.ext.spatial.InterpolationKind;
import org.sbml.jsbml.ext.spatial.ParametricGeometry;
import org.sbml.jsbml.ext.spatial.ParametricObject;
import org.sbml.jsbml.ext.spatial.PolygonKind;
import org.sbml.jsbml.ext.spatial.SampledField;
import org.sbml.jsbml.ext.spatial.SampledFieldGeometry;
import org.sbml.jsbml.ext.spatial.SampledVolume;
import org.sbml.jsbml.ext.spatial.SpatialCompartmentPlugin;
import org.sbml.jsbml.ext.spatial.SpatialConstants;
import org.sbml.jsbml.ext.spatial.SpatialModelPlugin;
import org.sbml.jsbml.ext.spatial.SpatialNamedSBase;
import org.sbml.jsbml.ext.spatial.SpatialParameterPlugin;
import org.sbml.jsbml.ext.spatial.SpatialPoints;
import org.sbml.jsbml.ext.spatial.SpatialSymbolReference;
import org.scijava.vecmath.Point3d;

import jp.ac.keio.bio.fun.xitosbml.image.SpatialImage;
import jp.ac.keio.bio.fun.xitosbml.util.PluginConstants;
import jp.ac.keio.bio.fun.xitosbml.util.PluginInfo;

/**
 * The class SpatialSBMLExporter, which converts a spatial model generated from
 * microscopic images to an SBML model.
 * Date Created: Feb 21, 2017
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
public class SpatialSBMLExporter{

  /** The SBML document. */
  private SBMLDocument document;
  
  /** The SBML model. */
  private Model model;
  
  /** The SBML spatialplugin. */
  private SpatialModelPlugin spatialplugin;
  
  /** The SBML spatialcompplugin. */
  private SpatialCompartmentPlugin spatialcompplugin;
  
  /** The geometry. */
  private Geometry geometry;
  
  /** The hashmap of domain types. */
  private HashMap<String, Integer> hashDomainTypes;
  
  /** The hashmap of sampled value. */
  private HashMap<String, Integer> hashSampledValue;
  
  /** The hashmap of domain num. */
  private HashMap<String, Integer> hashDomainNum;
  
  /** The hashmap of domain InteriorPoint of spatial image. */
  private HashMap<String,Point3d> hashDomInteriorPt;
  
  /** The adjacents list of spatial image. */
  private ArrayList<ArrayList<String>> adjacentsList;
  
  /** The raw data of spatial image in 1D array. */
  private byte[] raw;
  
  /** The size of an image (width, height and depth). */
  private int width, height, depth;
  
  /** The unit of a CoordinateComponent */
  private String unit;
  
  /** The delta. */
  private Point3d delta;

	/**
	 * Instantiates a new spatial SBML exporter.
	 */
	public SpatialSBMLExporter() {
		document = new SBMLDocument(3,1);
		document.setPackageRequired(SpatialConstants.namespaceURI, true);
		document.addDeclaredNamespace(PluginConstants.TAG_CELLDESIGNER_PREFIX, PluginConstants.CDNAMESPACE);
		model = document.createModel();

		SBasePlugin basePlugin = (model.getPlugin(SpatialConstants.namespaceURI));
		spatialplugin = (SpatialModelPlugin) basePlugin;
		if (spatialplugin == null) {
			System.exit(1);
		}
	}

	/**
	 * Instantiates a new spatial SBML exporter with given SpatialImage.
	 *
	 * @param spImg the {@link jp.ac.keio.bio.fun.xitosbml.image.SpatialImage}
	 */
	public SpatialSBMLExporter(SpatialImage spImg) {
		this();
		this.hashDomainTypes = spImg.getHashDomainTypes();
		this.hashSampledValue = spImg.getHashSampledValue();
		this.hashDomainNum = spImg.getHashDomainNum();
		this.hashDomInteriorPt = spImg.getHashDomInteriorPt();
		this.raw = spImg.getRaw();
		this.width = spImg.getWidth();
		this.height = spImg.getHeight();
		this.depth = spImg.getDepth();
		this.adjacentsList = spImg.getAdjacentsList();
		this.delta = spImg.getDelta();
		model = document.getModel();
		spatialplugin = (SpatialModelPlugin) model.getPlugin(SpatialConstants.namespaceURI);
		unit = spImg.getUnit();
	}

	/**
	 * Creates the geometry elements.
	 * This method will create following objects:
     * <ul>
	 *  <li>Geometry</li>
	 *  <li>CoordinateComponent</li>
	 *  <li>DomainType</li>
	 *  <li>Domain</li>
     *  <li>AdjacentDomains</li>
	 *  <li>Compartment</li>
	 *  <li>CompartmentMapping</li>
     *  <li>SampledFieldGeometry</li>
	 *  <li>SampledField</li>
	 *  <li>UnitDefinition</li>
	 *  <li>Unit</li>
     * </ul>
     * CellDesigner annotation will be added in addOutside() method to support
	 * spatial modeling on CellDesigner.
	 */
	public void createGeometryElements() {
		geometry = spatialplugin.createGeometry();
		geometry.setCoordinateSystem(GeometryKind.cartesian);
		addCoordinates();
		addDomainTypes();
		addDomains();
		addAdjacentDomains();
		addGeometryDefinitions();
		addUnits();
		addOutside();
	}

	/**
	 * Adds the geometry definitions.
	 * This method supports creating both 2D and 3D spaces (by looking into DomainType).
     * Each element in SampledField will store unsigned int 8 bit (uint8) value.
     * Data compression is currently disabled.
	 */
	public void addGeometryDefinitions() {
		SampledFieldGeometry sfg = geometry.createSampledFieldGeometry();
		sfg.setSpatialId("mySampledFieldGeometry");
		sfg.setIsActive(true);
		sfg.setSampledField("mySampledField");
		for (Entry<String, Integer> e : hashDomainTypes.entrySet()) {
			 if (e.getValue() == 3 || (e.getValue() == 2 && depth == 1)) {
				SampledVolume sv = sfg.createSampledVolume();
				sv.setSpatialId(e.getKey() + "_volume");
				sv.setDomainType(e.getKey());
				sv.setSampledValue(hashSampledValue.get(e.getKey()));
			}
		}
		
		SampledField sf = geometry.createSampledField();
		sf.setSpatialId("mySampledField");
		sf.setDataType(DataKind.UINT8);
		sf.setNumSamples1(width);
		sf.setNumSamples2(height);
		//if(depth > 1)
		sf.setNumSamples3(depth);
		sf.setInterpolation(InterpolationKind.nearestneighbor);
//		byte[] compressed = compressRawData(raw);
		String s;
//		if (compressed == null){
			sf.setCompression(CompressionKind.uncompressed);
			s = Arrays.toString(byteArrayToIntArray(raw));
//		}else{
//			sf.setCompression(CompressionKind.deflated);
//			s = Arrays.toString(byteArrayToIntArray(compressed));
//		}
		s = s.replace("[", "");
		s = s.replace("]", "");
		s = s.replace(",", "");
		sf.setSamples(s);
		sf.setSamplesLength(raw.length);
	}

	/**
	 * Compress raw data of spatial image in 1D array.
	 *
	 * @param raw the raw data of spatial image in 1D array
	 * @return byte[] the byte array
	 */
	public byte[] compressRawData(byte[] raw) {
		Deflater compresser = new Deflater();
		compresser.setLevel(Deflater.BEST_COMPRESSION);
		compresser.setInput(raw);
		compresser.finish();
		int size;
		byte[] buffer = new byte[1024];
		byte[] compressed = null;
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		while (true) {
			size = compresser.deflate(buffer);
			stream.write(buffer, 0, size);
			if (compresser.finished()) {
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

	/**
	 * Convert byte array to int array.
	 *
	 * @param compressed the compressed 1D byte array
	 * @return int[] the 1D integer array
	 */
	public int[] byteArrayToIntArray(byte[] compressed) {
		int[] intArray = new int[compressed.length];
		for (int i = 0; i < compressed.length; i++) {
			intArray[i] = compressed[i] & 0xff;
		}
		return intArray;
	}

	/**
	 * Adds the adjacent domains.
	 * This method adds membrane domains and adjacents to the model.
     * The membrane domain contains a string "_membrane" in its SpatialId.
	 */
	public void addAdjacentDomains() { // adds membrane domains and adjacents
		WeakHashMap<String, Integer> hashMembrane = new WeakHashMap<String, Integer>();
		for (ArrayList<String> e : adjacentsList) {
			String one = e.get(0).substring(0, e.get(0).length());
			one = one.replaceAll("[0-9]", "");
			String two = e.get(1).substring(0, e.get(1).length());
			two = two.replaceAll("[0-9]", "");
			//DomainType dt = geometry.getListOfDomainTypes().get(one + "_" + two + "_membrane");
			DomainType dt = (DomainType) getFromSpatialList(geometry.getListOfDomainTypes(), one + "_" + two + "_membrane");
			 
			if (hashMembrane.containsKey(dt.getSpatialId())) {
				hashMembrane.put(dt.getSpatialId(), hashMembrane.get(dt.getSpatialId()) + 1);
			} else {
				hashMembrane.put(dt.getSpatialId(), 0);
			}
			one = e.get(0).substring(0, e.get(0).length());
			two = e.get(1).substring(0, e.get(1).length());
			for (int i = 0; i < 2; i++) { // add info about adjacent domain
				AdjacentDomains adj = geometry.createAdjacentDomain();
				adj.setSpatialId(one + "_" + two + "_membrane_" + e.get(i));
				adj.setDomain1(dt.getSpatialId() + hashMembrane.get(dt.getSpatialId()));
				adj.setDomain2(e.get(i));
			}
		}
	}

	/**
	 * Adds domains to corresponding domaintypes.
	 * If SpatialId contains a string "membrane", it will be handled as membrane domain,
	 * thus InteriorPoint will not be added to the domain.
	 */
	public void addDomains() {
     for(Entry<String,Integer> e : hashDomainTypes.entrySet()){    	//add domains to corresponding domaintypes
    	 //DomainType dt = geometry.getListOfDomainTypes().get(e.getKey());
    	 DomainType dt = (DomainType) getFromSpatialList(geometry.getListOfDomainTypes(),e.getKey());
 
 		for (int i = 0; i < hashDomainNum.get(e.getKey()); i++) { // add each domain
			Domain dom = geometry.createDomain();
			String id = dt.getSpatialId() + i;
			dom.setSpatialId(id);
			dom.setDomainType(dt.getSpatialId());
			if (!dt.getSpatialId().matches(".*membrane")) {
				  InteriorPoint ip = dom.createInteriorPoint();
				  Point3d p = hashDomInteriorPt.get(id);
				  ip.setCoord1(p.x);
				  ip.setCoord2(p.y);
				  if(depth > 1) ip.setCoord3(p.z);  
				}
			}
     	}   
	}

	/**
     * Returns SpatialNamedSBase object which has SpatialId "id" from given list of Objects.
     * If there is no object in the list, then returns null.
	 * @param list list of objects (ex. SpatialNamedSbase)
	 * @param id SpatialId
	 * @return SpatialNamedSBase object
	 */
	public SpatialNamedSBase getFromSpatialList(ListOf<?> list, String id){
		for(Object o : list){
			SpatialNamedSBase sbase = (SpatialNamedSBase) o;
			if(sbase.getSpatialId().equals(id))
				return sbase;
		}
		
		return null;
	}
	
  	/**
	 * Adds the domain types to the Geometry object.
     * SpatialId and SpatialDimensions will be set to generated domain type.
	 * Also, SpatialDimensions, SpatialId and its name will be added to the compartment.
	 */
	public void addDomainTypes() {
		for (Entry<String, Integer> e : hashDomainTypes.entrySet()) {
			// DomainTypes
			DomainType dt = geometry.createDomainType();
			dt.setSpatialId(e.getKey());
			dt.setSpatialDimensions(e.getValue());
		
			// Compartment may need changes for name and id
			if(model.getListOfCompartments().get(e.getKey()) != null)
				continue;
			Compartment c = model.createCompartment();
			c.setSpatialDimensions(e.getValue());
			//c.setSpatialDimensions(3);
			c.setConstant(true);
			c.setId(e.getKey());
			c.setName(e.getKey());
			
			spatialcompplugin = (SpatialCompartmentPlugin) c.getPlugin("spatial");
			CompartmentMapping cm = new CompartmentMapping();
			cm.setSpatialId(e.getKey() + c.getId());
			cm.setDomainType(e.getKey());
			// TODO 
			//cm.setUnitSize(delta.x * delta.y * delta.z);
			cm.setUnitSize(1);

			spatialcompplugin.setCompartmentMapping(cm);
		}
	}

	/**
	 * Adds the coordinates to the Geometry object.
	 * It alsow sets the minimum and maximum values of the coordinate axis (boundary) to
	 * a CoordinateComponent object which explicitly defines a coordinate component of
	 * the coordinate axes.
     * If the image is a 3D image, then coordz (Z axis) will be set.
	 */
	public void addCoordinates() {
		CoordinateComponent ccx = geometry.createCoordinateComponent();
		ccx.setSpatialId("coordx");
		ccx.setType(CoordinateKind.cartesianX);
		if(unit != null) ccx.setUnits(unit);
		setCoordinateBoundary(ccx, "X", 0, width, delta.x);
		CoordinateComponent ccy = geometry.createCoordinateComponent();
		ccy.setSpatialId("coordy");
		ccy.setType(CoordinateKind.cartesianY);
		if(unit != null)  ccy.setUnits(unit);
		setCoordinateBoundary(ccy, "Y", 0, height, delta.y);
		if (depth > 1) {
			CoordinateComponent ccz = geometry.createCoordinateComponent();
			ccz.setSpatialId("coordz");
			ccz.setType(CoordinateKind.cartesianZ);
			if(unit != null) ccz.setUnits(unit);
			setCoordinateBoundary(ccz, "Z", 0, depth, delta.z);
		}
	}

	/**
     * Sets the minimum and maximum values of the coordinate axis (boundary) to
	 * a CoordinateComponent object which explicitly defines a coordinate component of
	 * the coordinate axes.
	 *
	 * @param coordinateComponent the CoordinateComponent object
	 * @param spId the spatialId as String
	 * @param min the minimum value of the coordinate axis
	 * @param max the maximum value of the coordinate axis
	 * @param delta the delta
	 */
	//TODO fix bound?
	public void setCoordinateBoundary(CoordinateComponent coordinateComponent, String spId, double min, double max, double delta) {
	  Boundary bmin = new Boundary();
	  //bmin.setId(spId + "min"); bmin.setValue(min * delta);
	  bmin.setSpatialId(spId + "min"); bmin.setValue(min);
	  Boundary bmax = new Boundary();
	  //bmax.setId(spId + "max"); bmax.setValue(max * delta);
	  bmax.setSpatialId(spId + "max"); bmax.setValue(max);
	  coordinateComponent.setBoundaryMaximum(bmax);
	  coordinateComponent.setBoundaryMinimum(bmin);
	}
  
	/**
	 * Adds the global Parameter to each CoordinateComponent.
	 * The id and the value of created will be set as "coordinate"+SpatialId and 0d respectively.
	 */
	public void addCoordParameter() {
		ListOf<CoordinateComponent> lcc = geometry.getListOfCoordinateComponents();
		Parameter p;
		CoordinateComponent cc;
		for (int i = 0; i < lcc.size(); i++) {
			cc = (CoordinateComponent) lcc.get(i);
			p = model.createParameter();
			p.setId("coordinate" + cc.getSpatialId());
			p.setConstant(true);
			p.setValue(0d);
			SpatialParameterPlugin spp = (SpatialParameterPlugin) p.getPlugin(SpatialConstants.namespaceURI);
			SpatialSymbolReference ssr = new SpatialSymbolReference();
			ssr.setSpatialRef(cc.getSpatialId());
			spp.setParamType(ssr);
		}
	}
  
	/**
	 * Gets the SBML model.
	 *
	 * @return the SBML model
	 */
	public Model getModel(){
		return model;
	}
	
	/**
	 * Gets the SBML document.
	 *
	 * @return the SBML document
	 */
	public SBMLDocument getDocument(){
		return document;
	}
	
	/**
	 * Adds the units (length, area and volume) to the model.
	 */
	public void addUnits(){
		if(unit == null) 
			return; 
		UnitDefinition ud = model.createUnitDefinition();
		ud.setId("length");
		Unit u = ud.createUnit();
		u.setKind(Kind.METRE);
		u.setExponent(1d);
		u.setScale(0);
		u.setMultiplier(getUnitMultiplier(unit));
	
		ud = model.createUnitDefinition();
		ud.setId("area");
		u = ud.createUnit();
		u.setKind(Kind.METRE);
		u.setExponent(2d);
		u.setScale(0);
		u.setMultiplier(getUnitMultiplier(unit));
		
		ud = model.createUnitDefinition();
		ud.setId("volume");
		u = ud.createUnit();
		u.setKind(Kind.METRE);
		u.setExponent(3d);
		u.setScale(0);
		u.setMultiplier(getUnitMultiplier(unit));
	}

	/**
	 * Gets the unit multiplier.
	 * Currently, it only supports "um".
	 *
	 * @param unit the unit
	 * @return the unit multiplier
	 */
	private double getUnitMultiplier(String unit) {
		if(unit.equals("um")) return 0.000001;
		return 1;
	}
	
	/**
	 * Creates the ParametricGeometry.
	 * This method will create following objects:
	 * <ul>
	 *  <li>Geometry</li>
	 *  <li>CoordinateComponent</li>
	 *  <li>DomainType</li>
	 *  <li>Domain</li>
	 *  <li>AdjacentDomains</li>
	 *  <li>Compartment</li>
	 *  <li>CompartmentMapping</li>
	 *  <li>SpatialPoints</li>
	 *  <li>ParametricObject</li>
	 * </ul>
	 *
	 * @param hashVertices the hashmap of vertices
	 * @param hathBound the hashmap of boundary
	 */
	public void createParametric(HashMap<String, List<Point3d>> hashVertices, HashMap<String, Point3d> hathBound) {
	    geometry = spatialplugin.createGeometry();
	    geometry.setCoordinateSystem(GeometryKind.cartesian);
	    addCoordinates(hathBound);
	    addDomainTypes();
	    addDomains();
	    addAdjacentDomains();
	    addParaGeoDefinitions(hashVertices, hathBound);
	  }

	/**
	 * Adds the coordinates to the Geometry object.
	 * It alsow sets the minimum and maximum values of the coordinate axis (boundary) to
	 * a CoordinateComponent object which explicitly defines a coordinate component of
	 * the coordinate axes.
	 *
	 * @param hashBound the hashmap of boundary
	 */
	public void addCoordinates(HashMap<String, Point3d> hashBound) { 
		CoordinateComponent ccx = geometry.createCoordinateComponent();
		ccx.setSpatialId("x");
		ccx.setType(CoordinateKind.cartesianX);
		if(unit != null) ccx.setUnits(unit);
		setCoordinateBoundary(ccx, "X", hashBound.get("min").x, hashBound.get("max").x, delta.x);
		CoordinateComponent ccy = geometry.createCoordinateComponent();
		ccy.setSpatialId("y");
		ccy.setType(CoordinateKind.cartesianY);
		if(unit !=null) ccy.setUnits(unit);
		setCoordinateBoundary(ccy, "Y", hashBound.get("min").y, hashBound.get("max").y, delta.x);
		CoordinateComponent ccz = geometry.createCoordinateComponent();
		ccz.setSpatialId("z");
		ccz.setType(CoordinateKind.cartesianZ);
		if(unit !=null) ccz.setUnits(unit);
		setCoordinateBoundary(ccz, "Z", hashBound.get("min").z, hashBound.get("max").z, delta.x);
	}
	
	/**
	 * Adds the parametric geometry definitions.
     * SpatialPoints will be set to ParametricGeometry.
     * ParametricObject will store double value, and the polygon will be triangle.
	 * Data compression is currently disabled.
	 *
	 * @param hashVertices the hashmap of vertices
	 * @param hashBound the hashmap of boundary
	 */
	public void addParaGeoDefinitions(HashMap<String, List<Point3d>> hashVertices, HashMap<String, Point3d> hashBound) {
		ParametricGeometry pg = geometry.createParametricGeometry();
		pg.setIsActive(true);
		pg.setSpatialId("ParametricGeometry");
		
		for (Entry<String, List<Point3d>> e : hashVertices.entrySet()) {
			List<Point3d> list = e.getValue();
			ArrayList<Point3d> uniquePointSet = new ArrayList<Point3d>(new LinkedHashSet<Point3d>(list));
			SpatialPoints sp = new SpatialPoints(PluginInfo.SBMLLEVEL, PluginInfo.SBMLVERSION);
			//sp.setId(e.getKey() + "_vertices");
			sp.setCompression(CompressionKind.uncompressed);
			addUniqueVertices(sp, uniquePointSet);
			pg.setSpatialPoints(sp);
			
			ParametricObject po = pg.createParametricObject();
			po.setCompression(CompressionKind.uncompressed);
			po.setDataType(DataKind.DOUBLE);
			po.setPolygonType(PolygonKind.triangle);
			po.setDomainType(e.getKey());
			po.setSpatialId(e.getKey() + "_polygon");
			setPointIndex(po, list, uniquePointSet);	
		}	
	}

	/**
	 * Adds the unique vertices to the SpatialPoints object.
	 * The set of unique vertices (an ArrayList of Point3d object: point0(x0, y0, z0),
	 * point1(x1, y1, z1) will be stored in an 1D array of String as
	 * ["x0", "y0", "z0", "x1", "y1", "z1", ...].
	 *
	 * @param sp the SpatialPoints of the ParametricGeometry
	 * @param uniquePointSet the ArrayList of Point3d (unique point set)
	 */
	public void addUniqueVertices(SpatialPoints sp, ArrayList<Point3d> uniquePointSet){
		Iterator<Point3d> pIt = uniquePointSet.iterator();
		int count = 0;
		double[] d = new double[uniquePointSet.size() *3];
		
		while(pIt.hasNext()){
			Point3d point = pIt.next();
			d[count++] = point.x;
			d[count++] = point.y;
			d[count++] = point.z;
		}	
		
		String s = Arrays.toString(d);
		s = s.replace("[", "");
		s = s.replace("]", "");
		s = s.replace(",", "");
		sp.setArrayData(s);
	}
	
	/**
	 * Sets the index of unique vertices to the SpatialPoints object.
	 * The set of index of unique vertices (an array of indices) will be stored
	 * in an 1D array of String as ["0", "3", "1", "2", ...].
	 *
	 * @param po the ParametricObject
	 * @param list the list of Point3d
	 * @param uniquePointSet the ArrayList of Point3d (unique point set)
	 */
	public void setPointIndex(ParametricObject po, List<Point3d> list, ArrayList<Point3d> uniquePointSet) {
		int[] points = new int[list.size()];
		
		for(int i = 0 ; i < list.size() ; i++)
			points[i] = uniquePointSet.indexOf(list.get(i));

		String s = Arrays.toString(points);
		s = s.replace("[", "");
		s = s.replace("]", "");
		s = s.replace(",", "");
		po.setPointIndex(s);
	}
	
	/**
	 * Add the outside annotation to Compartment for CellDesigner.
	 */
	private void addOutside() {
		//one = inner domain, two = outer domain
		for (ArrayList<String> e : adjacentsList) {
			String one = e.get(0).substring(0, e.get(0).length());
			one = one.replaceAll("[0-9]", "");
			String two = e.get(1).substring(0, e.get(1).length());
			two = two.replaceAll("[0-9]", "");
			String mem = one + "_" + two + "_membrane";
			Compartment com1 = getMappedCompartment(one);
			Compartment com2 = getMappedCompartment(two);
			Compartment commem = getMappedCompartment(mem);
			if(!com1.isSetAnnotation()){
				String str = createOutsideAnnotationString(commem.getId());
				try {
					com1.setAnnotation(PluginConstants.addCellDesignerAnnotationTag(str));
				} catch (XMLStreamException ex) {
					ex.printStackTrace();
				}
			}
			
			if(!commem.isSetAnnotation()){
				String str = createOutsideAnnotationString(com2.getId());
				try {
					commem.setAnnotation(PluginConstants.addCellDesignerAnnotationTag(str));
				} catch (XMLStreamException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	/**
	 * Gets the mapped compartment for given domain type.
	 *
	 * @param id the id of domain type
	 * @return the mapped compartment
	 */
	private Compartment getMappedCompartment(String id){
		ListOf<Compartment> cList = model.getListOfCompartments();
		
		for(Compartment c : cList){
			CompartmentMapping cm = ((SpatialCompartmentPlugin)c.getPlugin(SpatialConstants.shortLabel)).getCompartmentMapping();
			if(cm.getDomainType().equals(id))
				return c;
		}		
		
		return null;
	}
	
	/**
	 * Creates the outside annotation to Compartment for CellDesigner.
	 *
	 * @param outside the id of outside compartment
	 * @return the XML node as String
	 */
	private String createOutsideAnnotationString(String outside){
		return "<celldesigner:outside>" + outside + "</celldesigner:outside>\n";
	}
	
}