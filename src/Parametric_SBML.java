import ij.IJ;
import ij.plugin.PlugIn;


public class Parametric_SBML implements PlugIn{

	static {
		try{
			System.loadLibrary("sbmlj");
		}catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	@Override
	public void run(String args) {
		if(check3Dviewer()) 
			new MainParametricSpatial().run(args);	
	}

	public boolean check3Dviewer(){
		String version = ij3d.Install_J3D.getJava3DVersion();
        System.out.println("3D Viewer version = " + version);
        if(version != null && Float.parseFloat(version) >= 1.5)
                return true;
        IJ.error("Please Update 3D Viewer");
        return false;
	}
}
