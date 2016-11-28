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
package ij.plugin;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.io.Opener;
import ij.process.ImageProcessor;

import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

// TODO: Auto-generated Javadoc
/** This plugin implements the File/Batch/Convert command, 
	which converts the images in a folder to a specified format. */
	public class BatchConverter implements PlugIn, ActionListener {
		
		/** The Constant formats. */
		private static final String[] formats = {"TIFF", "8-bit TIFF", "JPEG", "GIF", "PNG", "PGM", "BMP", "FITS", "Text Image", "ZIP", "Raw"};
		
		/** The format. */
		private static String format = formats[0];
		
		/** The scale. */
		//private static int height;
		private static double scale = 1.0;
		
		/** The use bio formats. */
		private static boolean useBioFormats;
		
		/** The interpolation method. */
		private static int interpolationMethod = ImageProcessor.BILINEAR;
		
		/** The methods. */
		private String[] methods = ImageProcessor.getInterpolationMethods();
		
		/** The output. */
		private Button input, output;
		
		/** The output dir. */
		private TextField inputDir, outputDir;
		
		/** The gd. */
		private GenericDialog gd;

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		if (!showDialog())
			return;
		String inputPath = inputDir.getText();
		if (inputPath.equals("")) {
			IJ.error("Batch Converter", "Please choose an input folder");
			return;
		}
		String outputPath = outputDir.getText();
		if (outputPath.equals("")) {
			IJ.error("Batch Converter", "Please choose an output folder");
			return;
		}
		File f1 = new File(inputPath);
		if (!f1.exists() || !f1.isDirectory()) {
			IJ.error("Batch Converter", "Input does not exist or is not a folder\n \n"+inputPath);
			return;
		}
		File f2 = new File(outputPath);
		if (!outputPath.equals("") && (!f2.exists() || !f2.isDirectory())) {
			IJ.error("Batch Converter", "Output does not exist or is not a folder\n \n"+outputPath);
			return;
		}
		String[] list = (new File(inputPath)).list();
		ImageJ ij = IJ.getInstance();
		if (ij!=null) ij.getProgressBar().setBatchMode(true);
		IJ.resetEscape();
		for (int i=0; i<list.length; i++) {
			if (IJ.escapePressed())
				break;
			if (IJ.debugMode) IJ.log(i+"  "+list[i]);
			String path = inputPath + list[i];
			if ((new File(path)).isDirectory())
				continue;
			if (list[i].startsWith(".")||list[i].endsWith(".avi")||list[i].endsWith(".AVI"))
				continue;
			IJ.showProgress(i+1, list.length);
			ImagePlus imp = null;
			IJ.redirectErrorMessages(true);
			if (useBioFormats)
				imp = Opener.openUsingBioFormats(path);
			else
				imp = IJ.openImage(path);
			IJ.redirectErrorMessages(false);
			if (imp==null) {
				String reader = useBioFormats?"Bio-Formats not found or":"IJ.openImage()";
				IJ.log(reader+" returned null: "+path);
				continue;
			}
			if (scale!=1.0) {
				int width = (int)(scale*imp.getWidth());
				int height = (int)(scale*imp.getHeight());
				ImageProcessor ip = imp.getProcessor();
				ip.setInterpolationMethod(interpolationMethod);
				imp.setProcessor(null, ip.resize(width,height,true));
			}
			if (format.equals("8-bit TIFF") || format.equals("GIF")) {
				if (imp.getBitDepth()==24)
					IJ.run(imp, "8-bit Color", "number=256");
				else
					IJ.run(imp, "8-bit", "");
			}
			IJ.saveAs(imp, format, outputPath+list[i]);
			imp.close();
		}
		IJ.showProgress(1,1);
		Prefs.set("batch.input", inputDir.getText());
		Prefs.set("batch.output", outputDir.getText());
	}
			
	/**
	 * Show dialog.
	 *
	 * @return true, if successful
	 */
	private boolean showDialog() {
		gd = new GenericDialog("Batch Convert");
		addPanels(gd);
		gd.setInsets(15, 0, 5);
		gd.addChoice("Output format:", formats, format);
		gd.addChoice("Interpolation:", methods, methods[interpolationMethod]);
		//gd.addStringField("Height (pixels): ", height==0?"\u2014":""+height, 6);
		gd.addNumericField("Scale factor:", scale, 2);
		gd.addCheckbox("Read images using Bio-Formats", useBioFormats);
		gd.setOKLabel("Convert");
		gd.showDialog();
		if (gd.wasCanceled())
			return false;
		format = gd.getNextChoice();
		interpolationMethod = gd.getNextChoiceIndex();
		//height = (int)Tools.parseDouble(gd.getNextString(), 0.0);
		scale = gd.getNextNumber();
		useBioFormats = gd.getNextBoolean();
		return true;
	}

	/**
	 * Adds the panels.
	 *
	 * @param gd the gd
	 */
	void addPanels(GenericDialog gd) {
		Panel p = new Panel();
    	p.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		input = new Button("Input...");
		input.addActionListener(this);
		p.add(input);
		inputDir = new TextField(Prefs.get("batch.input", ""), 45);
		p.add(inputDir);
		gd.addPanel(p);
		p = new Panel();
    	p.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		output = new Button("Output...");
		output.addActionListener(this);
		p.add(output);
		outputDir = new TextField(Prefs.get("batch.output", ""), 45);
		p.add(outputDir);
		gd.addPanel(p);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		String s = source==input?"Input":"Output";
		String path = IJ.getDirectory(s+" Folder");
		if (path==null) return;
		if (source==input)
			inputDir.setText(path);
		else
			outputDir.setText(path);
		if (IJ.isMacOSX())
			{gd.setVisible(false); gd.setVisible(true);}
	}

}
