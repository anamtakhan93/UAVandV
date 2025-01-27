package CSVModule;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import dataModel.CampaignData;
import dataModel.UAVModel;
import faults.CustomCommunicationFault;
import faults.ForceUAVLanding;
import faults.GpsModuleDelayedValues;
import faults.GpsModuleFixedValues;
import faults.GpsModuleFreezeValues;
import faults.GpsModuleMinMaxValues;
import faults.GpsModuleRandomValues;
import faults.RandomLatitude;
import faults.RandomLongitude;

public class CSVReader {

	
	public static List<UAVModel> readUAVSFromCSV(String filePath) throws Exception{
		List<UAVModel> uavs = new ArrayList<>();
		Path pathToFile = Paths.get(filePath);
		
		 try (BufferedReader br = Files.newBufferedReader(pathToFile, StandardCharsets.US_ASCII)) {
			 			 
			 //Loop reading the UAV Models until the end of the file
			 String line = br.readLine();
			 while(line != null) {
				line = line.replace("\"", "");
				String[] attributes = line.split(";");
				UAVModel newUAV = new UAVModel(attributes);
				uavs.add(newUAV);
				line = br.readLine();
			 }
			 
		 } catch (IOException e) {
			 throw new Exception("Invalid format of CSV file. Please, try again with a valid file.");
		 }
		
		return uavs;
	}
	
	
	public static CampaignData readFaultsFromCSV(String filePath){
		ArrayList<String> header = new ArrayList<>();
		Path pathToFile = Paths.get(filePath);
		String line = null;
		CampaignData data = new CampaignData();
		
		try (BufferedReader br = Files.newBufferedReader(pathToFile, StandardCharsets.US_ASCII)) {
			//Read Flight Control Information
			 line = br.readLine();
			 String [] aux = line.replace("\"", "").split(",");
			 data.setFlightController(Integer.parseInt(aux[1]));
			 //Read Gezebo World File Path
			 line = br.readLine();
			 aux = line.replace("\"", "").split(",");
			 data.setGezeboWorldPath(aux[1]);
			 //Read UAV Models Information
			 line = br.readLine();
			 aux = line.replace("\"", "").split(",");
			 int numberOfUAV = Integer.parseInt(aux[1]);
			 for(int i=0; i<numberOfUAV; i++) {
				 line = br.readLine();
				 aux = line.replace("\"", "").split(",");
				 UAVModel newUav = new UAVModel(aux[0], aux[2], aux[1], aux[3], i);
				 try {
					data.addUAVModel(newUav);
				} catch (Exception e) {
					// TODO 
				}
			 }
			 //Read Faults
			 line = br.readLine();
			 while(line != null) {
				 aux = line.replace("\"", "").split(",");
				 switch(aux[0]) {
				 	case "Software Failures":
				 		switch(aux[2]) {
				 			case "Random Values":
				 				GpsModuleRandomValues rvfault = new GpsModuleRandomValues(Integer.parseInt(aux[3]),
				 						Integer.parseInt(aux[5]),
				 						Integer.parseInt(aux[6]),
				 						1,
				 						aux[0],
				 						aux[2],
				 						aux[1],
				 						Integer.parseInt(aux[7]),
				 						Integer.parseInt(aux[8]),
				 						Integer.parseInt(aux[9]),
				 						Integer.parseInt(aux[4]),
				 						getUavsModels(aux[10]));
				 				data.addFault(rvfault);
				 				break;
				 			case "Min/Max Values":
				 				GpsModuleMinMaxValues minmaxfault = new GpsModuleMinMaxValues(Integer.parseInt(aux[3]),
				 						Integer.parseInt(aux[5]),
				 						Integer.parseInt(aux[6]),
				 						1,
				 						aux[0],
				 						aux[2],
				 						aux[1],
				 						Integer.parseInt(aux[7]),
				 						Integer.parseInt(aux[8]),
				 						Integer.parseInt(aux[9]),
				 						Integer.parseInt(aux[4]),
				 						getUavsModels(aux[10]));
				 				data.addFault(minmaxfault);
				 				break;
				 			case "Fixed Values":
				 				GpsModuleFixedValues fixedfault = new GpsModuleFixedValues(Integer.parseInt(aux[3]),
				 						Integer.parseInt(aux[5]),
				 						Integer.parseInt(aux[6]),
				 						1,
				 						aux[0],
				 						aux[2],
				 						aux[1],
				 						Integer.parseInt(aux[7]),
				 						Integer.parseInt(aux[8]),
				 						Integer.parseInt(aux[9]),
				 						Integer.parseInt(aux[4]),
				 						getUavsModels(aux[10]));
				 				data.addFault(fixedfault);
				 				break;
				 			case "Freeze Values":
				 				GpsModuleFreezeValues freezefault = new GpsModuleFreezeValues(Integer.parseInt(aux[3]),
				 						Integer.parseInt(aux[5]),
				 						Integer.parseInt(aux[6]),
				 						1,
				 						aux[0],
				 						aux[2],
				 						aux[1],
				 						Integer.parseInt(aux[4]),
				 						getUavsModels(aux[7]));
				 				data.addFault(freezefault);
				 				break;
				 			case "Delayed Values":
				 				GpsModuleDelayedValues delayedfault = new GpsModuleDelayedValues(Integer.parseInt(aux[3]),
				 						Integer.parseInt(aux[5]),
				 						Integer.parseInt(aux[6]),
				 						1,
				 						aux[0],
				 						aux[2],
				 						aux[1],
				 						Integer.parseInt(aux[4]),
				 						getUavsModels(aux[7]));
				 				data.addFault(delayedfault);
				 				break;
				 		}
				 		break;
				 	case "Communication Failures":
				 		switch(aux[2]) {
				 			case "Custom Type":
				 				
				 				CustomCommunicationFault ccfault = new CustomCommunicationFault(Integer.parseInt(aux[3]),
				 						Integer.parseInt(aux[5]),
				 						Integer.parseInt(aux[6]),
				 						1,
				 						aux[0],
				 						aux[2],
				 						aux[1],
				 						Integer.parseInt(aux[7]),
				 						Integer.parseInt(aux[8]),
				 						Integer.parseInt(aux[9]),
				 						Integer.parseInt(aux[10]),
				 						Integer.parseInt(aux[4]),
				 						getUavsModels(aux[11]));
				 				data.addFault(ccfault);
				 				break;
				 			default:
				 				break;
				 		}
				 		break;
				 	case "Security Atacks":
				 		switch(aux[2]) {
				 			case "Random Longitude":
				 				RandomLongitude longitudefault = new RandomLongitude(Integer.parseInt(aux[3]),
				 						Integer.parseInt(aux[5]),
				 						Integer.parseInt(aux[6]),
				 						1,
				 						aux[0],
				 						aux[2],
				 						aux[1],
				 						Integer.parseInt(aux[7]),
				 						Integer.parseInt(aux[4]),
				 						getUavsModels(aux[8]));
				 				data.addFault(longitudefault);
				 				break;
				 			case "Random Latitude":
				 				RandomLatitude latitudefault = new RandomLatitude(Integer.parseInt(aux[3]),
				 						Integer.parseInt(aux[5]),
				 						Integer.parseInt(aux[6]),
				 						1,
				 						aux[0],
				 						aux[2],
				 						aux[1],
				 						Integer.parseInt(aux[7]),
				 						Integer.parseInt(aux[4]),
				 						getUavsModels(aux[8]));
				 				data.addFault(latitudefault);
				 				break;
				 			case "Random Position":
				 				GpsModuleRandomValues rpfault = new GpsModuleRandomValues(Integer.parseInt(aux[3]),
				 						Integer.parseInt(aux[5]),
				 						Integer.parseInt(aux[6]),
				 						1,
				 						aux[0],
				 						aux[2],
				 						aux[1],
				 						Integer.parseInt(aux[7]),
				 						Integer.parseInt(aux[8]),
				 						Integer.parseInt(aux[9]),
				 						Integer.parseInt(aux[4]),
				 						getUavsModels(aux[10]));
				 				data.addFault(rpfault);
				 				break;
				 			case "GPS Delay":
				 				GpsModuleDelayedValues delayedfaultSA = new GpsModuleDelayedValues(Integer.parseInt(aux[3]),
				 						Integer.parseInt(aux[5]),
				 						Integer.parseInt(aux[6]),
				 						1,
				 						aux[0],
				 						aux[2],
				 						aux[1],
				 						Integer.parseInt(aux[4]),
				 						getUavsModels(aux[7]));
				 				data.addFault(delayedfaultSA);
				 				break;
				 			case "Force UAV Landing":
				 				ForceUAVLanding forceuavfault = new ForceUAVLanding(Integer.parseInt(aux[3]),
				 						Integer.parseInt(aux[5]),
				 						Integer.parseInt(aux[6]),
				 						1,
				 						aux[0],
				 						aux[2],
				 						aux[1],
				 						Integer.parseInt(aux[7]),
				 						Integer.parseInt(aux[4]),
				 						getUavsModels(aux[8]));
				 				data.addFault(forceuavfault);
				 				break;
				 			case "Hijack with a second UAV":
				 				GpsModuleFixedValues seconduavfault = new GpsModuleFixedValues(Integer.parseInt(aux[3]),
				 						Integer.parseInt(aux[5]),
				 						Integer.parseInt(aux[6]),
				 						1,
				 						aux[0],
				 						aux[2],
				 						aux[1],
				 						Integer.parseInt(aux[7]),
				 						Integer.parseInt(aux[8]),
				 						Integer.parseInt(aux[9]),
				 						Integer.parseInt(aux[4]),
				 						getUavsModels(aux[10]));
				 				data.addFault(seconduavfault);
				 				break;
				 			case "Hijack with attacker�s specified position":
				 				GpsModuleFixedValues attackerfault = new GpsModuleFixedValues(Integer.parseInt(aux[3]),
				 						Integer.parseInt(aux[5]),
				 						Integer.parseInt(aux[6]),
				 						1,
				 						aux[0],
				 						aux[2],
				 						aux[1],
				 						Integer.parseInt(aux[7]),
				 						Integer.parseInt(aux[8]),
				 						Integer.parseInt(aux[9]),
				 						Integer.parseInt(aux[4]),
				 						getUavsModels(aux[10]));
				 				data.addFault(attackerfault);
				 				break;
				 		}
				 		break;
				 	default:
				 		break;
				 }
				 
				 line = br.readLine();
			 }
			 
		 } catch (IOException e) { }
		
		return data;
	}
	
	
	private final static List<String> getUavsModels(String record){
		List<String> uavs = new ArrayList<>();
		record.replace("\"", "");
		record.replace("[", "");
		record.replace("]", "");
		String [] aux = record.split(";");
		for (String uav : aux) {
			uavs.add(uav);
		}
		return uavs;
	}
	
	
	
	
}
