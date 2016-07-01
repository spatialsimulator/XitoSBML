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
package sbmlplugin.sbmlplugin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.zip.Deflater;

import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import org.sbml.libsbml.AdjacentDomains;
import org.sbml.libsbml.Boundary;
import org.sbml.libsbml.ChangedMath;
import org.sbml.libsbml.Compartment;
import org.sbml.libsbml.CompartmentMapping;
import org.sbml.libsbml.CoordinateComponent;
import org.sbml.libsbml.Domain;
import org.sbml.libsbml.DomainType;
import org.sbml.libsbml.Geometry;
import org.sbml.libsbml.InteriorPoint;
import org.sbml.libsbml.ListOf;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Parameter;
import org.sbml.libsbml.ParametricGeometry;
import org.sbml.libsbml.ParametricObject;
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
import org.sbml.libsbml.SpatialPoints;
import org.sbml.libsbml.SpatialSymbolReference;
import org.sbml.libsbml.Unit;
import org.sbml.libsbml.UnitDefinition;
import org.sbml.libsbml.libsbmlConstants;

import sbmlplugin.image.SpatialImage;
import sun.misc.FloatingDecimal;

// TODO: Auto-generated Javadoc
/**
 * The Class SpatialSBMLExporter.
 */
public class SpatialSBMLExporter{

  /** The document. */
  private SBMLDocument document;
  
  /** The model. */
  private Model model;
  
  /** The sbmlns. */
  private SBMLNamespaces sbmlns;
  
  /** The spatialns. */
  private SpatialPkgNamespaces spatialns;
  
  /** The spatialplugin. */
  private SpatialModelPlugin spatialplugin;
  
  /** The spatialcompplugin. */
  //private ReqSBasePlugin reqplugin;
  private SpatialCompartmentPlugin spatialcompplugin;
  
  /** The geometry. */
  private Geometry geometry;
  
  /** The hash domain types. */
  private HashMap<String, Integer> hashDomainTypes;
  
  /** The hash sampled value. */
  private HashMap<String, Integer> hashSampledValue;
  
  /** The hash domain num. */
  private HashMap<String, Integer> hashDomainNum;
  
  /** The hash dom interior pt. */
  private HashMap<String,Point3f> hashDomInteriorPt;
  
  /** The adjacents list. */
  private ArrayList<ArrayList<String>> adjacentsList;
  
  /** The raw. */
  private byte[] raw;
  
  /** The depth. */
  private int width, height, depth;
  
  /** The unit. */
  private String unit;
  
  /** The delta. */
  private Point3d delta;

	/**
	 * Instantiates a new spatial SBML exporter.
	 */
	public SpatialSBMLExporter() {
		sbmlns = new SBMLNamespaces(3, 1);
		sbmlns.addPackageNamespace("req", 1);
		sbmlns.addPackageNamespace("spatial", 1);

		document = new SBMLDocument(sbmlns);
		document.setPackageRequired("req", false);
		document.setPackageRequired("spatial", true);
		model = document.createModel();

		
		spatialns = new SpatialPkgNamespaces(3, 1, 1);
		// reqplugin = (ReqSBasePlugin)model.getPlugin("req"); //get required

		SBasePlugin basePlugin = (model.getPlugin("spatial"));
		spatialplugin = (SpatialModelPlugin) basePlugin;
		if (spatialplugin == null) {
			System.err.println("[Fatal Error] Layout Extension Level "
					+ spatialns.getLevel() + " Version "
					+ spatialns.getVersion() + " package version "
					+ spatialns.getPackageVersion() + " is not registered.");
			System.exit(1);
		}
	}

	/**
	 * Instantiates a new spatial SBML exporter.
	 *
	 * @param spImg the sp img
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
		spatialplugin = (SpatialModelPlugin) model.getPlugin("spatial");
		unit = spImg.getUnit();
	}

	/**
	 * Creates the geometry elements.
	 */
	public void createGeometryElements() {
		geometry = spatialplugin.createGeometry();
		geometry.setCoordinateSystem("cartesian");
		addCoordinates();
		addDomainTypes();
		addDomains();
		addAdjacentDomains();
		addGeometryDefinitions();
		addUnits();
	}

	/**
	 * Adds the geometry definitions.
	 */
//TODO determine to use compressed or uncompressed data
	public void addGeometryDefinitions() {
		SampledFieldGeometry sfg = geometry.createSampledFieldGeometry();
		sfg.setId("mySampledField");
		sfg.setIsActive(true);
		sfg.setSampledField("imgtest");
		for (Entry<String, Integer> e : hashDomainTypes.entrySet()) {
			// if ((e.getValue() == 3 && depth > 2) || (e.getValue() == 2 &&
			// depth == 1)) {
			if (e.getValue() == 3) {
				SampledVolume sv = sfg.createSampledVolume();
				sv.setId(e.getKey());
				sv.setDomainType(e.getKey());
				sv.setSampledValue(hashSampledValue.get(e.getKey()));
				// sv.setMinValue(0); sv.setMaxValue(0);
			}
		}
		SampledField sf = geometry.createSampledField();
		sf.setId("imgtest");
		sf.setDataType(libsbmlConstants.SPATIAL_DATAKIND_UINT8);
		// sf.setCompression(libsbmlConstants.SPATIAL_COMPRESSIONKIND_DEFLATED);
		sf.setCompression(libsbmlConstants.SPATIAL_COMPRESSIONKIND_UNCOMPRESSED);
		sf.setNumSamples1(width);
		sf.setNumSamples2(height);
		// if(depth > 1)
		sf.setNumSamples3(depth);
		sf.setInterpolationType(libsbmlConstants.SPATIAL_INTERPOLATIONKIND_NEARESTNEIGHBOR);

		// byte[] compressed = compressRawData(raw);
		// if (compressed != null)
		// sf.setSamples(byteArrayToIntArray(compressed),compressed.length);

		sf.setSamples(byteArrayToIntArray(raw), raw.length);
	}

	/**
	 * Compress raw data.
	 *
	 * @param raw the raw
	 * @return the byte[]
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
	 * Byte array to int array.
	 *
	 * @param compressed the compressed
	 * @return the int[]
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
	 */
	public void addAdjacentDomains() { // adds membrane domains and adjacents
		WeakHashMap<String, Integer> hashMembrane = new WeakHashMap<String, Integer>();
		for (ArrayList<String> e : adjacentsList) {
			String one = e.get(0).substring(0, e.get(0).length());
			one = one.replaceAll("[0-9]", "");
			String two = e.get(1).substring(0, e.get(1).length());
			two = two.replaceAll("[0-9]", "");
			DomainType dt = geometry.getDomainType(one + "_" + two
					+ "_membrane");
			if (hashMembrane.containsKey(dt.getId())) {
				hashMembrane.put(dt.getId(), hashMembrane.get(dt.getId()) + 1);
			} else {
				hashMembrane.put(dt.getId(), 0);
			}
			one = e.get(0).substring(0, e.get(0).length());
			two = e.get(1).substring(0, e.get(1).length());
			for (int i = 0; i < 2; i++) { // add info about adjacent domain
				AdjacentDomains adj = geometry.createAdjacentDomains();
				adj.setId(one + "_" + two + "_membrane_" + e.get(i));
				adj.setDomain1(dt.getId() + hashMembrane.get(dt.getId()));
				adj.setDomain2(e.get(i));
			}
		}
	}

	/**
	 * Adds the domains.
	 */
	public void addDomains() {
     for(Entry<String,Integer> e : hashDomainTypes.entrySet()){    			//add domains to corresponding domaintypes
 		DomainType dt = geometry.getDomainType(e.getKey());
		Domain dom = geometry.createDomain();
		for (int i = 0; i < hashDomainNum.get(e.getKey()); i++) { // add each domain
			String id = dt.getId() + i;
			dom.setId(id);
			dom.setDomainType(dt.getId());
			if (!dt.getId().matches(".*membrane")) {
				  InteriorPoint ip = dom.createInteriorPoint();
				  Point3f p = hashDomInteriorPt.get(id);
				  ip.setId(id + " point");
				  ip.setCoord1(p.x);
				  ip.setCoord2(p.y);
				  if(depth > 1) ip.setCoord3(p.z);  
				}
			}
     	}   
	}	

  	/**
	   * Adds the domain types.
	   */
	public void addDomainTypes() {
		for (Entry<String, Integer> e : hashDomainTypes.entrySet()) {
			// DomainTypes
			DomainType dt = geometry.createDomainType();
			dt.setId(e.getKey());
			dt.setSpatialDimensions(e.getValue());
			//dt.setSpatialDimensions(3);

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
			CompartmentMapping cm = spatialcompplugin.createCompartmentMapping();
			cm.setId(e.getKey() + c.getId());
			cm.setDomainType(e.getKey());
			// TODO 
			//cm.setUnitSize(delta.x * delta.y * delta.z);
			cm.setUnitSize(1);
		}
	}

	/**
	 * Adds the coordinates.
	 */
	public void addCoordinates() { 
		CoordinateComponent ccx = geometry.createCoordinateComponent();
		ccx.setId("x");
		ccx.setType("cartesianX");
		ccx.setType(libsbmlConstants.SPATIAL_COORDINATEKIND_CARTESIAN_X);
		if(unit != null) ccx.setUnit(unit);
		setCoordinateBoundary(ccx, "X", 0, width, delta.x);
		CoordinateComponent ccy = geometry.createCoordinateComponent();
		ccy.setId("y");
		ccy.setType(libsbmlConstants.SPATIAL_COORDINATEKIND_CARTESIAN_Y);
		if(unit != null)  ccy.setUnit(unit);
		setCoordinateBoundary(ccy, "Y", 0, height, delta.y);
		if (depth > 1) {
			CoordinateComponent ccz = geometry.createCoordinateComponent();
			ccz.setId("z");
			ccz.setType(libsbmlConstants.SPATIAL_COORDINATEKIND_CARTESIAN_Z);
			if(unit != null) ccz.setUnit(unit);
			setCoordinateBoundary(ccz, "Z", 0, depth, delta.z);
		}
	}

	/**
	 * Sets the coordinate boundary.
	 *
	 * @param cc the cc
	 * @param s the s
	 * @param min the min
	 * @param max the max
	 * @param delta the delta
	 */
	//TODO fix bound after fixing the simulator
	public void setCoordinateBoundary(CoordinateComponent cc, String s, double min, double max, double delta) { 
	  Boundary bmin = cc.createBoundaryMin();
	  //bmin.setId(s + "min"); bmin.setValue(min * delta);
	  bmin.setId(s + "min"); bmin.setValue(min);
	  Boundary bmax = cc.createBoundaryMax();
	  //bmax.setId(s + "max"); bmax.setValue(max * delta);
	  bmax.setId(s + "max"); bmax.setValue(max);
	}
  
	/**
	 * Adds the coord parameter.
	 */
	public void addCoordParameter() {
		ListOf lcc = geometry.getListOfCoordinateComponents();
		Parameter p;
		CoordinateComponent cc;
		for (int i = 0; i < lcc.size(); i++) {
			cc = (CoordinateComponent) lcc.get(i);
			p = model.createParameter();
			p.setId(cc.getId());
			p.setConstant(true);
			SpatialParameterPlugin sp = (SpatialParameterPlugin) p
					.getPlugin("spatial");
			SpatialSymbolReference ssr = sp.createSpatialSymbolReference();
			ssr.setId(cc.getId());
			ssr.setSpatialRef(cc.getId());
			ReqSBasePlugin rsb = (ReqSBasePlugin) p.getPlugin("req");
			ChangedMath cm = rsb.createChangedMath();
			cm.setChangedBy("spatial");
			cm.setViableWithoutChange(true);
		}
	}
  
	/**
	 * Creates the parametric.
	 *
	 * @param hashVertices the hash vertices
	 * @param hashBound the hash bound
	 */
	public void createParametric(HashMap<String, List<Point3f>> hashVertices, HashMap<String, Point3d> hashBound) {
	    geometry = spatialplugin.createGeometry();
	    geometry.setCoordinateSystem("Cartesian");
	    addCoordinates(hashBound);                        
	    addDomainTypes();                         
	    addDomains();                           
	    addAdjacentDomains();  
	    addParaGeoDefinitions(hashVertices, hashBound);    
	  }
  
	/**
	 * Adds the para geo definitions.
	 *
	 * @param hashVertices the hash vertices
	 * @param hashBound the hash bound
	 */
	public void addParaGeoDefinitions(HashMap<String, List<Point3f>> hashVertices, HashMap<String, Point3d> hashBound) {
		ParametricGeometry pg = geometry.createParametricGeometry();
		pg.setIsActive(true);
		pg.setId("ParametricGeometry");
		
		for (Entry<String, List<Point3f>> e : hashVertices.entrySet()) {
			List<Point3f> list = e.getValue();
			ArrayList<Point3f> uniquePointSet = new ArrayList<Point3f>(new LinkedHashSet<Point3f>(list));
			SpatialPoints sp = pg.createSpatialPoints();
			sp.setId(e.getKey() + "_vertices");
			sp.setCompression(libsbmlConstants.SPATIAL_COMPRESSIONKIND_UNCOMPRESSED);
			addUniqueVertices(sp, uniquePointSet);
			
			ParametricObject po = pg.createParametricObject();
			po.setCompression(libsbmlConstants.SPATIAL_COMPRESSIONKIND_UNCOMPRESSED);
			po.setDataType(libsbmlConstants.SPATIAL_DATAKIND_FLOAT);
			po.setPolygonType(libsbmlConstants.SPATIAL_POLYGONKIND_TRIANGLE);
			po.setDomainType(e.getKey());
			po.setId(e.getKey() + "_polygon");
			setPointIndex(po, list, uniquePointSet);	
		}	
	}

	/**
	 * Adds the unique vertices.
	 *
	 * @param sp the sp
	 * @param uniquePointSet the unique point set
	 */
	public void addUniqueVertices(SpatialPoints sp, ArrayList<Point3f> uniquePointSet){
		Iterator<Point3f> pIt = uniquePointSet.iterator();
		int count = 0;
		double[] d = new double[uniquePointSet.size() *3];
		
		while(pIt.hasNext()){
			Point3f point = pIt.next();
			d[count++] = new FloatingDecimal(point.x).doubleValue();
			d[count++] = new FloatingDecimal(point.y).doubleValue();
			d[count++] = new FloatingDecimal(point.z).doubleValue();
		}	
		sp.setArrayData(d, uniquePointSet.size());
	}
	
	/**
	 * Sets the point index.
	 *
	 * @param po the po
	 * @param list the list
	 * @param uniquePointSet the unique point set
	 */
	public void setPointIndex(ParametricObject po, List<Point3f> list, ArrayList<Point3f> uniquePointSet) {
		int size = list.size();
		int[] points = new int[list.size()];
		
		for(int i = 0 ; i < list.size() ; i++)
			points[i] = uniquePointSet.indexOf(list.get(i));
		
		po.setPointIndex(points, size);
	}
	
	/**
	 * Adds the coordinates.
	 *
	 * @param hashBound the hash bound
	 */
	public void addCoordinates(HashMap<String, Point3d> hashBound) { 
		CoordinateComponent ccx = geometry.createCoordinateComponent();
		ccx.setId("x");
		ccx.setType("cartesianX");
		if(unit !=null) ccx.setUnit(unit);
		setCoordinateBoundary(ccx, "X", hashBound.get("min").x, hashBound.get("max").x, delta.x);
		CoordinateComponent ccy = geometry.createCoordinateComponent();
		ccy.setId("y");
		ccy.setType("cartesianY");
		if(unit !=null) ccy.setUnit(unit);
		setCoordinateBoundary(ccy, "Y", hashBound.get("min").y, hashBound.get("max").y, delta.x);
		CoordinateComponent ccz = geometry.createCoordinateComponent();
		ccz.setId("z");
		ccz.setType("cartesianZ");
		if(unit !=null) ccz.setUnit(unit);
		setCoordinateBoundary(ccz, "Z", hashBound.get("min").z, hashBound.get("max").z, delta.x);
	}
	
	/**
	 * Gets the model.
	 *
	 * @return the model
	 */
	public Model getModel(){
		return model;
	}
	
	/**
	 * Gets the document.
	 *
	 * @return the document
	 */
	public SBMLDocument getDocument(){
		return document;
	}
	
	/**
	 * Adds the units.
	 */
	public void addUnits(){
		if(unit == null) return; 
		UnitDefinition ud = model.createUnitDefinition();
		ud.setId(unit);
		Unit u = ud.createUnit();
		u.setKind(libsbmlConstants.UNIT_KIND_METRE);
		u.setExponent(1);
		u.setScale(0);
		u.setMultiplier(getUnitMultiplier(unit));
	
		ud = model.createUnitDefinition();
		ud.setId(unit+"2");
		u = ud.createUnit();
		u.setKind(libsbmlConstants.UNIT_KIND_METRE);
		u.setExponent(2);
		u.setScale(0);
		u.setMultiplier(getUnitMultiplier(unit));
		
		ud = model.createUnitDefinition();
		ud.setId(unit+"3");
		u = ud.createUnit();
		u.setKind(libsbmlConstants.UNIT_KIND_METRE);
		u.setExponent(3);
		u.setScale(0);
		u.setMultiplier(getUnitMultiplier(unit));
	}
	
	/**
	 * Gets the unit multiplier.
	 *
	 * @param unit the unit
	 * @return the unit multiplier
	 */
	private Double getUnitMultiplier(String unit){
		if(unit.equals("um")) return 0.000001;
		return 1.0;
	}
}
