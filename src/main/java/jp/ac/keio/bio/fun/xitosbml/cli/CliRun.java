package jp.ac.keio.bio.fun.xitosbml.cli;

import java.io.File;
import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * 
 * The class CliRun.
 * 
 * This class implements the CLI version of XitoSBML. It allows the user to
 * obtain spatial SBML models by specifying the directory of the image dataset
 * or path of a single image via the command line. Date Created: August 9, 2020
 * 
 * @author Medha Bhattacharya
 * @author Akira Funahashi
 * @author Kaito Ii
 * @author Yuta Tokuoka
 *
 */

@Command(name = "Running XitoSBML-CLI", mixinStandardHelpOptions = true, description = "Saves SBML document and spatial image", version = "1.0")

public class CliRun implements Callable<Integer> {

	@Option(names = "-i", required = true, description = "The path to input image file or folder containing images")
	String inputValue;
	@Option(names = "-o", required = true, description = "The path to output XML file if input is image file")
	String outputValue;

	/**
	 * Checks whether the "inputValue" is the path to an image or a directory
	 * containing image(s) and calls the "run XitoSBML_CLI version"
	 * 
	 * @param folder Can be either a single image File or an entire dataset of
	 *               images
	 */
	public void RunXitosbml(final File folder) {

		// Instantiating CliMainImgSpatial class
		CliMainImgSpatial cliMain = new CliMainImgSpatial();

		if (!folder.isDirectory()) {
			// The input is path to an image file and hence can be processed directly
			if (folder.exists()) {
				// System.out.println(folder);
				String ext = inputValue.substring(inputValue.lastIndexOf('.') + 1);
				System.out.println(ext);
				if (ext.equals("tif") || ext.equals("tiff") || ext.equals("bmp") || ext.equals("dcm")
						|| ext.equals("fits") || ext.equals("pdm") || ext.equals("gif") || ext.equals("jpeg")) {
					cliMain.runCli(inputValue, outputValue);
				}
			} else {
				System.out.println("File: " + folder + " does not exist");
			}
		} else {
			// The input is path to a folder containing image files
			for (final File fileEntry : folder.listFiles()) {
				if (fileEntry.isDirectory()) {
					RunXitosbml(fileEntry);
				} else {
					String temp = folder.getAbsolutePath() + File.separator + fileEntry.getName();
					String ext = temp.substring(temp.lastIndexOf('.') + 1);

					if (ext.equals("tif") || ext.equals("tiff") || ext.equals("bmp") || ext.equals("dcm")
							|| ext.equals("fits") || ext.equals("pdm") || ext.equals("gif") || ext.equals("jpeg")) {

						// Default naming convention for the output SBML models
						String tempOut = temp.substring(0, temp.indexOf('.')) + "_output" + ".xml";
						cliMain.runCli(temp, tempOut);

					}
				}

			}
		}

	}

	@Override
	/**
	 * Creates a file from the path specified through inputValue and calls
	 * RunXitoSBML method for this file.
	 */
	public Integer call() {
		File folder = new File(inputValue);
		RunXitosbml(folder);

		return 0;
	}

	/**
	 * The main method which implements the execute(args) method for this class
	 * 
	 * @param args String array argument
	 */
	public static void main(String... args) {
		System.exit(new CommandLine(new CliRun()).execute(args));
	}

}

/*
 * @Command(name = "Cli for XitoSBML", mixinStandardHelpOptions = true,
 * description = "Saves SBML document and spatial image", version = "1.0")
 * public class CliRun implements Runnable {
 * 
 * @Option(names = "-i", description = "The input option") String iValue;
 * 
 * @Option(names = "-o", description = "The output option") String oValue;
 * 
 * @Override public void run() { /* System.out.printf("-r=%s%n", rValue); new
 * ij.ImageJ(); ImagePlus img = new ImagePlus(rValue); img.show(); String name =
 * img.getTitle(); System.out.println(name);
 * 
 * System.out.printf("-i=%s%n", iValue); System.out.printf("-o=%s%n", oValue);
 * new CliMainImgSpatial().runCui(iValue, oValue); }
 * 
 * @SuppressWarnings("deprecation") public static void main(String... args) {
 * CommandLine.run(new CliRun(), System.err, args); }
 * 
 */
