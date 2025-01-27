package faults;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GpsModuleMinMaxValues extends Fault{
	
	private int latitudeMin;
	private int latitudeMax;
	private int longitudeMin;
	private int longitudeMax;
	private int altitudeMin;
	private int altitudeMax;
	
	private int latitude;
	private int longitude;
	private int altitude;
	
	


	public GpsModuleMinMaxValues(int timeStartInjWindow, int timeEndInjWindow, int timeEndInjRun, int numberOfFaults,
			String faultType, String faultSubtype, String target, int latitudeMin, int latitudeMax, int longitudeMin,
			int longitudeMax, int altitudeMin, int altitudeMax) {
		super(timeStartInjWindow, timeEndInjWindow, timeEndInjRun, numberOfFaults, faultType, faultSubtype, target);
		this.latitudeMin = latitudeMin;
		this.latitudeMax = latitudeMax;
		this.longitudeMin = longitudeMin;
		this.longitudeMax = longitudeMax;
		this.altitudeMin = altitudeMin;
		this.altitudeMax = altitudeMax;
	}
	
	public GpsModuleMinMaxValues(int timeStartInjWindow, int timeEndInjWindow, int timeEndInjRun, int numberOfFaults,
			String faultType, String faultSubtype, String target, int latitude,
			int longitude, int altitude, int timeInjection, List<String> uavs) {
		super(timeStartInjWindow, timeEndInjWindow, timeEndInjRun, numberOfFaults, faultType, faultSubtype, target, timeInjection, uavs);
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
	}

	public int getLatitudeMin() {
		return latitudeMin;
	}

	public int getLatitudeMax() {
		return latitudeMax;
	}

	public int getLongitudeMin() {
		return longitudeMin;
	}

	public int getLongitudeMax() {
		return longitudeMax;
	}

	public int getAltitudeMin() {
		return altitudeMin;
	}

	public int getAltitudeMax() {
		return altitudeMax;
	}
	
	@Override
	public int getTimeStartInjWindow() {
		return timeStartInjWindow;
	}

	@Override
	public int getTimeEndInjWindow() {
		return timeEndInjWindow;
	}

	@Override
	public int getTimeEndInjRun() {
		return timeEndInjWindow;
	}

	@Override
	public int getNumberOfFaults() {
		return numberOfFaults;
	}

	@Override
	public String getFaultType() {
		return faultType;
	}

	@Override
	public String getFaultSubtype() {
		return faultSubtype;
	}

	@Override
	public String getTarget() {
		return target;
	}

	@Override
	public String[] getFaultRecord(Random rnd) {
		String [] fault = new String[11];
		fault[0] = getFaultType();
		fault[1] = getTarget();
		fault[2] = getFaultSubtype();
		fault[3] = getTimeStartInjWindow()+"";
		fault[4] = getTimeInjection()+"";
		fault[5] = getTimeEndInjWindow()+"";
		fault[6] = getTimeEndInjRun()+"";
		int aux = getRandomNumber(rnd, 1, 8);
		if( (aux % 2) == 0) {
			latitude = latitudeMin;
			longitude = longitudeMin;
			altitude = altitudeMin;
		}else {
			latitude = latitudeMax;
			longitude = longitudeMax;
			altitude = altitudeMax;
		}
		fault[7] = latitude+"";
		fault[8] = longitude+"";
		fault[9] = altitude+"";
		fault[10] = this.getUavModelsToAffectString();
		return fault;
	}

	@Override
	public int getTimeInjection() {
		return timeInjection;
	}

	@Override
	public List<Integer> getFaultValues() {
		List<Integer> values = new ArrayList<>();
		values.add(latitude);
		values.add(longitude);
		values.add(altitude);
		return values;
	}
	
	

}
