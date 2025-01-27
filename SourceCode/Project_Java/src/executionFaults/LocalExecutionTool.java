package executionFaults;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import CSVModule.CSVReader;
import dataModel.CampaignData;
import dataModel.InjectorData;
import dataModel.Machine;
import emuNet.MyMqttClient;
import emuNet.SftpClient;
import faults.Fault;
import faults.GpsModuleFreezeValues;

public class LocalExecutionTool {


	private String faultsFilePath;
	private MyMqttClient mqttClient;
	private List<Fault> faults;
	private CampaignData campaign = null;
	private List<InjectorData> injectionFiles;
	private SftpClient sftpc;

	private String localInjectionFilesDir = "/home/anamta/Documents/InjectionFiles/";

	private Boolean faultyRun = true;
	private int initialPort = 14540;
	private String Lat = "39.61768724";
	private String Lon = "-0.319550121";
	private String Alt = "5";


	private List<Machine> Machines;
	private int numOfMachines=2; // this number is overriden later according to the machines array size


	public LocalExecutionTool(String faultsFilePath) {
		this.faultsFilePath = faultsFilePath;
		campaign = CSVReader.readFaultsFromCSV(faultsFilePath);
		faults = campaign.getFaults();
		injectionFiles = new ArrayList<InjectorData>();
		Machines = new ArrayList<Machine>();
		sftpc = new SftpClient();
		
		Lat = "0";
		Lon = "0";
		Alt = "0";
	}
	
	public void RunLocalTest(String filename) {
        // Command to open a new terminal and run the Python script
        System.out.println(filename);
	    String[] command = {
	            "/bin/bash",
	            "-c",
	            "gnome-terminal -- bash -c 'cd /home/anamta/RunCampagins && python3.8 RunCampagin.py --fn " + filename +  "; exec bash'"
	        };
        System.out.println(Arrays.toString(command));
        try {        
        	// Execute the command
            Process process = new ProcessBuilder(command).start();
            
            // Optional: Print the output of the process
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            process.waitFor();
            System.out.println("Python script running in a new terminal...");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        
	    String[] command2 = {
	            "/bin/bash",
	            "-c",
	            "gnome-terminal -- bash -c 'cd /home/anamta/RunCampagins && python3.8 RunMissions.py; exec bash'"
	        };
        System.out.println(Arrays.toString(command2));
        try {        
        	// Execute the command
            Process process = new ProcessBuilder(command2).start();
            
            // Optional: Print the output of the process
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            process.waitFor();
            System.out.println("Python script running in a new terminal...");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        

	}

	public void injectionLoop() {

		AddMachines();

		StartCampagin();

		UploadMissions();
		Injections();

		int sleepTime = 5000 + ((campaign.getUAVModels().size()+1) * 1000);
		System.out.println("-------------------------------- Wait time for Gazeebo : " + sleepTime/1000 + " secs");
		try {
			Thread.sleep(sleepTime);

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		ExecuteCampagin();
	}

	private void AddMachines() {
		Machine vm2 = new Machine("10.254.0.118", "bubbles", "Bubbles%21", "/home/bubbles/Injections/",
				"/home/bubbles/Missions/", "/home/bubbles/PX4/PX4-Autopilot", "/home/bubbles/DronekitScript/dronekit_script_custom.py",
				campaign.getUAVModels().size()/numOfMachines);

		Machine vm1 = new Machine("10.254.0.121", "bubbles", "Bubbles%21", "/home/bubbles/Injections/",
				"/home/bubbles/Missions/", "/home/bubbles/PX4/PX4-Autopilot", "/home/bubbles/DronekitScript/dronekit_script_custom.py",
				campaign.getUAVModels().size()/numOfMachines);
		Machines.add(vm1);
		Machines.add(vm2);
		numOfMachines = Machines.size();

		int startingIndex = 0;
		for(int i = 1; i<=numOfMachines; i++) {
			int extra = (i <= campaign.getUAVModels().size()%numOfMachines) ? 1:0;
			int num = (campaign.getUAVModels().size()/numOfMachines) + extra;
			Machines.get(i-1).setNumOfDrones(num);
			System.out.println("---------------------- Number of drones in machine "+ i +" = " + num);
			Machines.get(i-1).setUAVstartIndex(startingIndex);
			startingIndex+= Machines.get(i-1).getNumOfDrones();
			Machines.get(i-1).setUAVendIndex(startingIndex);
		}
	}

	private void UploadMissions() {
		for (Machine machine : Machines) {
			int port=initialPort;
			for(int i=machine.getUAVstartIndex(); i<machine.getUAVendIndex(); i++) {
				UploadMission(campaign.getUAVModels().get(i).getMissionPath(), campaign.getUAVModels().get(i).getIp()+":"+port,machine);
				port++;
			}
		}
	}

	private void Injections() {
		for (Fault fault : faults) {
			for (Machine machine : Machines) {
				GenerateInjectionDataForFault(fault,machine);
			}
		}
	}

	private void ExecuteCampagin() {

		for (Machine machine : Machines) {
			int port=initialPort;
			for(int i=machine.getUAVstartIndex(); i<machine.getUAVendIndex(); i++) {
				RunMissionOnRemote(campaign.getUAVModels().get(i).getIp()+":"+port, GetRemoteMissionFilePath(campaign.getUAVModels().get(i).getIp()+":"+port
						,machine),machine);
				port++;
			}
		}
	}

	private void StartCampagin() {
		for (Machine machine : Machines) {
			Thread newThread = new Thread(() -> {
				StartGazeebo(machine);
			});
			newThread.start();
		}
	}

	private String GetRemoteMissionFilePath(String droneIP, Machine machine) {
		String address = machine.getMissionFilesDir()+"mission_"+ droneIP + ".plan"; //+"_"+System.currentTimeMillis()

		return address;
	}

	private void GenerateInjectionDataForFault(Fault fault, Machine machine) {
		for(int i=0; i<fault.getUavModelsToAffect().size(); i++) {
			int mIndex = Machines.indexOf(machine);
			int droneOrder = campaign.GetUAVData().get(fault.getUavModelsToAffect().get(i)).getOrder();
			
			if(droneOrder >= machine.getUAVstartIndex() && droneOrder < machine.getUAVendIndex()) {
				int j = i;
				Thread newThread = new Thread(() -> {
					
					int localPort = initialPort + droneOrder/(mIndex+1);

					InjectorData ijd = GenerateInjectionData(campaign.GetUAVData().get(fault.getUavModelsToAffect().get(j)).getIp()+":"+ localPort, 
							GetRemoteMissionFilePath(campaign.GetUAVData().get(fault.getUavModelsToAffect().get(j)).getIp()+":"+localPort,machine),
							fault, fault.getFaultValues(),mIndex);
					injectionFiles.add(ijd);

					CreateInjectionDataFile(ijd,localInjectionFilesDir,mIndex, faultyRun);
				});
				newThread.start();
			}
		}
	}

	private void RunMissionOnRemote(String ip, String missionPath, Machine machine) {
		Thread newThread = new Thread(() -> {
			RunDronekitScript(ip, missionPath, machine);
		});
		newThread.start();
	}

	private void StartGazeebo(Machine machine) {
		try {

			System.out.println("Starting Gazebo on Machine : " + machine.getIP());
				
//			Process p = Runtime.getRuntime().exec("sshpass -p "+machine.getPassword()+" ssh -tt "+machine.getUser()
//			+"@"+machine.getIP()+ " export PX4_HOME_LAT="+Lat+"; export PX4_HOME_LON="+Lon+"; export PX4_HOME_ALT="+Alt+";" +
//			" DISPLAY=:0 " + machine.getAutopilotPath()
//			+ "/Tools/gazebo_sitl_multiple_run.sh -s \""+GenerateScript(machine.getUAVstartIndex(),machine.getUAVendIndex())+"\"");

			Process p = Runtime.getRuntime().exec("sshpass -p "+machine.getPassword()+" ssh -tt "+machine.getUser()
			+"@"+machine.getIP()+ " DISPLAY=:0 " + machine.getAutopilotPath()
			+ "/Tools/gazebo_sitl_multiple_run.sh -s \""+GenerateScript(machine.getUAVstartIndex(),machine.getUAVendIndex())+"\"");

			
			BufferedReader stdInput = new BufferedReader(new 
					InputStreamReader(p.getInputStream()));

			BufferedReader stdError = new BufferedReader(new 
					InputStreamReader(p.getErrorStream()));

			String s = null;
			System.out.println("Gazebo outputs:\n");
			while ((s = stdInput.readLine()) != null) {
				System.out.println(s);
			}

			System.out.println("Gazebo exit / errors (if any):\n");
			while ((s = stdError.readLine()) != null) {
				System.out.println(s);
			}

			//System.exit(0);

		} catch (IOException e) {
			System.out.println("Error in starting Gazebo");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private String GenerateScript(int start, int end) {
		int iris = 0;
		int plane = 0;
		int s_vtol = 0;

		for(int i=start; i<end; i++) {

			if(campaign.getUAVModels().get(i).getModel().contains("iris")) {
				iris++;
			}
			else if (campaign.getUAVModels().get(i).getModel().contains("plane")) {
				plane++;
			}
			else if (campaign.getUAVModels().get(i).getModel().contains("standard_vtol")) {
				s_vtol++;
			}
			else {
				iris++;
			}
		}

		String script = "iris:"+iris+",plane:"+plane+",standard_vtol:"+s_vtol;
		return script;
	}

	private void RunDronekitScript(String ip, String mission, Machine machine) {

		try {
			//Process p = Runtime.getRuntime().exec("python3 dronekit_script.py -c " + ip + " -m " + mission);
			Process p = Runtime.getRuntime().exec("sshpass -p "+machine.getPassword()+" ssh "+machine.getUser()+"@"+machine.getIP()+" python3 " + 
					machine.getDroneKitScriptPath()+ " -c " + ip + " -m " + mission);

			BufferedReader stdInput = new BufferedReader(new 
					InputStreamReader(p.getInputStream()));

			BufferedReader stdError = new BufferedReader(new 
					InputStreamReader(p.getErrorStream()));

			String s = null;
			System.out.println("Dronekit script outputs:\n");
			while ((s = stdInput.readLine()) != null) {
				System.out.println(s);
			}

			System.out.println("Dronekit script exit / errors (if any):\n");
			while ((s = stdError.readLine()) != null) {
				System.out.println(s);
			}

			//System.exit(0);

		} catch (IOException e) {
			System.out.println("Error in python script");
			e.printStackTrace();
			System.exit(-1);
		}

	}

	public String GetFaultsFilePath() {
		return faultsFilePath;
	}

	public List<Fault> GetFaults(){
		return faults;
	}

	private InjectorData GenerateInjectionData(String ip, String mission, Fault fault, List<Integer> values, int machineID) {

		InjectorData mID = new InjectorData(ip, mission, fault.getTimeStartInjWindow(), fault.getTimeEndInjWindow(), fault.getTimeEndInjRun(),
				fault.getFaultType().replace(" ", ""),fault.getFaultSubtype().replace(" ", ""),fault.getTarget(),fault.getTimeInjection(), values, machineID); 

		return mID;
	}


	private void CreateInjectionDataFile(InjectorData data, String dirPath, int machineID, Boolean copyOnRemote) {
		String DirPath = dirPath;
		String FileName = "Fault_Campagin_"+data.getFaultType()+"_"+data.getFaultSubtype()+"_"+data.getDroneIP()+".fc"; //+"_"+System.currentTimeMillis()

		try {
			FileWriter mWriter = new FileWriter(DirPath+machineID+"_"+FileName, false);
			mWriter.write("DroneIP = "+data.getDroneIP()+"\n");
			mWriter.write("MissionPath = "+data.getMissionPath()+"\n");
			mWriter.write("InjectionTimeStart = "+data.getTimeStartInjWindow()+"\n");
			mWriter.write("InjectionTimeEnd = "+data.getTimeEndInjWindow()+"\n");
			mWriter.write("FaultType = "+data.getFaultType()+"\n");
			mWriter.write("FaultTarget = "+data.getTarget()+"\n");
			mWriter.write("FaultSubType = "+data.getFaultSubtype()+"\n");

			mWriter.write("Values = "+data.getValues().toString());
			mWriter.close();
			System.out.println("Successfully wrote to the file.");

		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
		if(copyOnRemote) {

			sftpc.uploadSftpFromPath(DirPath+machineID+"_"+FileName,
					Machines.get(machineID).getInjectionFilesDir()+FileName, Machines.get(machineID).getUser(), Machines.get(machineID).getPassword(),
					Machines.get(machineID).getIP());
			System.out.println("Created injection file on remote");
		}
	}


	private void UploadMission(String missionPath, String droneIP, Machine machine) {
		String address = machine.getMissionFilesDir()+"mission_"+ droneIP + ".plan"; //+"_"+System.currentTimeMillis()

		sftpc.uploadSftpFromPath(missionPath,
				address, machine.getUser(), machine.getPassword(), machine.getIP());
	}


}



//private void RunLocalMission(String ip, Fault fault, String missionPath) { //this is just for testing
//
//	InjectorData ijd = GenerateInjectionData(ip, missionPath, fault, new ArrayList<Integer>(){{
//		add(1);
//		add(2);
//		add(3);
//	}});
//	injectionFiles.add(ijd); //here we will need to add according to the fault
//
//	CreateInjectionDataFile(ijd,localInjectionFilesDir, false);
//
//	Thread newThread = new Thread(() -> {
//		RunDronekitScript(ip, missionPath); //For local call
//	});
//	newThread.start();
//}
