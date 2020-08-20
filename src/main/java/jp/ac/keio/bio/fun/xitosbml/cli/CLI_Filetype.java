package jp.ac.keio.bio.fun.xitosbml.cli;

import java.io.File;
import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "Running XitoSBML-CLI", mixinStandardHelpOptions = true, description = "Saves VIA File option", version = "1.0")

public class CLI_Filetype implements Callable<Integer> {

	@Option(names = "-d", required = true, description = "Directory")
	String inputDir;
	@Option(names = "-n", required = true, description = "No.of images to be converted to SBML files")
	int outputValue;
	@Option(names = "-i", required = true, description = "Image file id")
	String imageId;
	@Option(names = "-o", required = true, description = "Output XML file id")
	String outputId;

	@Override
	public Integer call() {
		String[] input = new String[outputValue];
		String[] output = new String[outputValue];
		String dir = inputDir;

		for (int i = 0; i < outputValue; i++) {
			input[i] = dir + File.separator + imageId + (i + 1) + ".tif";
			output[i] = dir + File.separator + outputId + (i + 1) + ".xml";
			// System.out.println(input[i]);
			// System.out.println(output[i]);
			new CliMainImgSpatial().runCli(input[i], output[i]);
		}
		return 0;
	}

	public static void main(String... args) {
		System.exit(new CommandLine(new CLI_Filetype()).execute(args));
	}

}
