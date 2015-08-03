package sbmlplugin.util;

import java.io.File;
import java.io.IOException;

import org.w3c.dom.Document;

import xmlwise.Plist;
import xmlwise.XmlElement;
import xmlwise.XmlParseException;
import xmlwise.Xmlwise;

/**
 * Spatial SBML Plugin for ImageJ
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jul 7, 2015
 */
public class PlistEditor {
	static final String fileName = "info.plist";
	static final String option = "-Djava.library.path";
	private XmlElement docNode;
	static String path;
	
	static {
		if (System.getProperty("os.name").startsWith("Mac OS")) {
			path = "/Applications/Fiji.app";
		} else {
			path = "";
		}
	}
	
	public PlistEditor() throws XmlParseException, IOException{
		String filePath = path + "/Contents/" + fileName;
		docNode = Xmlwise.loadXml(filePath);
	}
	
	public void modify() throws XmlParseException{
		XmlElement topNode = docNode.get(0);
		XmlElement element = topNode.getUnique("dict");
		for(int i = 0 ; i < element.size() ; i++){
			XmlElement el = element.get(i);
			if(el.getValue().equals("JVMOptions")){
				XmlElement newElement = resolveOption(element.get(i+1));
				element.set(i+1, newElement);
				break;
			}
		}
	}
	
	private XmlElement resolveOption(XmlElement element){
		String s = element.getValue();
		if(s.equals("")) return new XmlElement("string", option + "=" + path);
		else if(s.contains(path)) return element;
		else if(s.contains(option)) return new XmlElement("string", s + " " + path);
		else return new XmlElement("string", s + " " + option + "=" + path);
	}
	
	public void save() throws IOException, XmlParseException{
		File file  = new File(path + "/Contents/" + fileName);
		//Plist.storeObject(Plist.fromXml(docNode.toXml()), file);
		Plist.storeObject(Plist.objectFromXmlElement(docNode), file);
		
	}
	
	public static void main(String[] args){
		try {
			PlistEditor edit = new PlistEditor();
			edit.modify();
			edit.save();
		} catch (Exception e) {
			System.err.println("Error while editing info.plist in Fiji");
			e.printStackTrace();
		}		
		
	}
}
