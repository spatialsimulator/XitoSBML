

public class test {
	//original voxel size
	private double voxx = 1.05;			
	private double voxy = 1.05;
	private double voxz = 1.8;
	private int width = 5;
	private int height = 3;
	private int depth = 3;
	private double xaxis = voxx * width;
	private double yaxis = voxx * height;
	private double zaxis = voxz * depth;
	
	int[] mat = {
			0,0,0,0,0,
			0,1,1,1,0,
			0,0,0,0,0,
			
			0,0,1,0,0,
			1,1,1,1,1,
			0,0,1,0,0,
			
			0,0,0,0,0,
			0,1,1,1,0,
			0,0,0,0,0,
	};
	
	
	public void interpolate(){
		int altz = (int) (zaxis / voxx);
		System.out.println("altz " + altz);
		int matrix[][] = new int[altz][width * height];
		double xdis, ydis, zdis;
		double halfx = voxx /2, halfy = voxy /2,halfz = voxx /2;
		
		for(int d = 0 ; d < altz ; d++){
			for(int h = 0 ; h < height ; h++){
				for(int w = 0 ; w < width ; w++){
					// get center
					xdis = w * voxx + halfx;	 
					ydis = h * voxy + halfy;
					zdis = d * voxx + halfz;
					
					//apply to nearest original pixel
					
					xdis = Math.floor(xdis / voxx);
					ydis = Math.floor(ydis / voxy);
					zdis = Math.floor(zdis / voxz);

					matrix[d][h * width + w] = mat[(int) (zdis * width * height + ydis * width + xdis)];
					System.out.println(xdis + " " + ydis + " " + zdis);
				}
			}
		}
	System.out.println();
		for(int d = 0 ; d < altz ; d++){
			for(int h = 0 ; h < height ; h++){
				for(int w = 0 ; w < width ; w++){
					System.out.print(matrix[d][h * width + w] + " ");
				}
				System.out.println();
			}
			System.out.println();
			System.out.println();
		}
		
	}

	public static void main(String args[]){
		test unko = new test();
		unko.interpolate();
	}
	
}
