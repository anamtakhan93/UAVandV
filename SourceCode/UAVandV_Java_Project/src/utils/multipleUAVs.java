package utils;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class multipleUAVs implements Runnable{
	
	
	static int numberofdrones = 3;
	static int initialportnumber = 14540; 
	int uavport; 
	
	public multipleUAVs(int port) {
		this.uavport = port;
	}
	
	
	public static void CreateDrones(int numOfDrones) {
		
		
		//for each fault in the list of faults 
			// start px4 and Gazebo 
			// copy the necessary files
			
		numberofdrones = numOfDrones;
		
		    // start the threads. 
			ExecutorService executorService = Executors.newFixedThreadPool(numberofdrones);
			for(int i=0;i<numberofdrones;i++) {
				multipleUAVs uav = new multipleUAVs(initialportnumber+i);
				executorService.submit(uav);
			}
			
		
	}

	@Override
	public void run() {
		
		// TODO Auto-generated method stub
		// run the dronkit script with the port number of the uav
		System.out.println("Hello"+ uavport);
		
	}
	
	


}
