package jp.ac.keio.bio.fun.xitosbml.cli;

public class TrialRunner {

	public static void run(String path1, String path2) {
		// imager.show();
		new CliMainImgSpatial().runCli(path1, path2);
	}

	public static void main(String[] args) {
		String path1 = "C:\\Users\\Subroto\\Desktop\\Inference(300x100)\\inf_18.tif";
		String path2 = "C:\\Users\\Subroto\\Desktop\\Inference(300x100)\\inf_18.xml";
		// "C:\\Users\\Subroto\\Desktop\\Inference(300x100)\\gt_5_yo.xml";
		/*
		 * if (path.contains(".")) path2 = path.substring(0, path.indexOf('.')); // new
		 * ij.ImageJ(); ImagePlus imager = new ImagePlus(path); // img.show(); String
		 * title = imager.getTitle(); System.out.println(title);
		 * System.out.println(path2);
		 */
		run(path1, path2);
	}
}
