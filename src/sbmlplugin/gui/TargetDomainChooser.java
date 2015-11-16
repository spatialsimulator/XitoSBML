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
package sbmlplugin.gui;

import ij.IJ;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.sbml.libsbml.Geometry;
import org.sbml.libsbml.ListOfDomainTypes;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBMLReader;
import org.sbml.libsbml.SpatialModelPlugin;


/**
 * Spatial SBML Plugin for ImageJ
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Aug 30, 2015
 */
@SuppressWarnings("serial")
public class TargetDomainChooser extends JFrame implements ActionListener{
	static {
		try {
			System.loadLibrary("sbmlj");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	private String targetDomain = null;
	private List<String> compartmentList = new ArrayList<String>();
	private final String[] excludeDom = {"Extracellular","Cytosol"};
	
	TargetDomainChooser() {
		super("Target Domain Chooser");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);	
		setSize(250, 70);
		setResizable(false);
		setLocationByPlatform(true);
		setLocationRelativeTo(null);
	}
	
	public TargetDomainChooser(Model model){
		this();
		createCompartmentList(model);
		initComponent();
	}
	
	private void createCompartmentList(Model model){
		SpatialModelPlugin spatialplugin = (SpatialModelPlugin) model.getPlugin("spatial");
		Geometry geometry = spatialplugin.getGeometry();
		ListOfDomainTypes lodt = geometry.getListOfDomainTypes();
		for(int i = 0 ; i < lodt.size() ; i++){
			String dom = lodt.get(i).getId();
			if(!Arrays.asList(excludeDom).contains(dom) && dom.contains("membrane"))
				compartmentList.add(lodt.get(i).getId());
		}			
	
		if(compartmentList.isEmpty()){
			IJ.error("No target domain found");
			setTargetDomain("");
		}
	}
	
	private JComboBox compartmentBox;
	private JButton okButton;
	private void initComponent(){
		JLabel label = new JLabel(" Select one domain :");
		JPanel panel = new JPanel();
		
		compartmentBox = new JComboBox(compartmentList.toArray());
		String title = "DomainType List";
		compartmentBox.setName(title);
		compartmentBox.setRenderer(new ComboBoxRenderer(title));
		compartmentBox.setSelectedIndex(-1);
	
		okButton = new JButton("OK");
		okButton.setName("ok");
		okButton.addActionListener(this);
		
		
		panel.add(compartmentBox, BorderLayout.LINE_START);
		panel.add(okButton, BorderLayout.PAGE_END);
		getContentPane().add(label, BorderLayout.NORTH);
		getContentPane().add(panel, BorderLayout.CENTER);
		setVisible(true);
	}

	public String getTargetDomain() {
		return targetDomain;
	}

	private void setTargetDomain(String targetDomain) {
		this.targetDomain = targetDomain;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent ae) {
		if(ae.getSource() == okButton && compartmentBox.getSelectedIndex() >= 0){ 
			setTargetDomain((String) compartmentBox.getSelectedItem());
			dispose();
		} else{
			IJ.error("Must select one domain type.");
		}
	}
	
	public static void main(String[] args){
		SBMLReader reader = new SBMLReader();
		SBMLDocument d = reader.readSBML("mem_diff.xml");
		TargetDomainChooser tdc = new TargetDomainChooser(d.getModel());	
	
		while(tdc.getTargetDomain() == null){
			synchronized (d){
				
			}
		}
	}
}
