package jp.ac.keio.bio.fun.xitosbml.cli;

import java.io.File;
import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "Running XitoSBML-CLI", mixinStandardHelpOptions = true, description = "Saves SBML document and spatial image", version = "1.0")

public class CliRun implements Callable<Integer> {

	@Option(names = "-i", required = true, description = "The path to input image file or folder containing images")
	String inputValue;
	@Option(names = "-o", required = true, description = "The path to output XML file if input is image file")
	String outputValue;

	// Function to check whether path specified is a folder or not
	public void RunXitosbml(final File folder) {

		// Instantiating CliMainImgSpatial class
		CliMainImgSpatial cliMain = new CliMainImgSpatial();

		if (!folder.isDirectory()) {
			// The input is path to an image file and hence can be processed directly
			cliMain.runCli(inputValue, outputValue);
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
	public Integer call() {
		File folder = new File(inputValue);
		RunXitosbml(folder);

		return 0;
	}

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
