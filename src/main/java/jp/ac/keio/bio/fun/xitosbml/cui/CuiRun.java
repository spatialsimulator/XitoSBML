package jp.ac.keio.bio.fun.xitosbml.cui;

import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "Running XitoSBML-CUI", mixinStandardHelpOptions = true, description = "Saves SBML document and spatial image", version = "1.0")

public class CuiRun implements Callable<Integer> {

	@Option(names = "-i", required = true, description = "The path to input image file")
	String inputValue;
	@Option(names = "-o", required = true, description = "The path to output XML file")
	String outputValue;

	@Override
	public Integer call() {
		// System.out.printf("-i=%s%n", inputValue);
		// System.out.printf("-o=%s%n", outputValue);
		new CuiMainImgSpatial().runCui(inputValue, outputValue);

		return 0;
	}

	public static void main(String... args) {
		System.exit(new CommandLine(new CuiRun()).execute(args));
	}

}

/*
 * @Command(name = "Cui for XitoSBML", mixinStandardHelpOptions = true,
 * description = "Saves SBML document and spatial image", version = "1.0")
 * public class CuiRun implements Runnable {
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
 * new CuiMainImgSpatial().runCui(iValue, oValue); }
 * 
 * @SuppressWarnings("deprecation") public static void main(String... args) {
 * CommandLine.run(new CuiRun(), System.err, args); }
 * 
 */
