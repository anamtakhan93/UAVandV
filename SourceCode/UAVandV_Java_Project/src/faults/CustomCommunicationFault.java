package faults;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CustomCommunicationFault extends Fault{
	
	public CustomCommunicationFault(int timeStartInjWindow, int timeEndInjWindow, int timeEndInjRun, int numberOfFaults,
			String faultType, String faultSubtype, String target, int bw, int jiter, int loss, int latency) {
		super(timeStartInjWindow, timeEndInjWindow, timeEndInjRun, numberOfFaults, faultType, faultSubtype, target);
		this.bw = bw;
		this.jiter = jiter;
		this.loss = loss;
		this.latency = latency;
	}
	
	public CustomCommunicationFault(int timeStartInjWindow, int timeEndInjWindow, int timeEndInjRun, int numberOfFaults,
			String faultType, String faultSubtype, String target, int bw, int jiter, int loss, int latency, int injectionTime, List<String> uavs) {
		super(timeStartInjWindow, timeEndInjWindow, timeEndInjRun, numberOfFaults, faultType, faultSubtype, target, injectionTime, uavs);
		this.bw = bw;
		this.jiter = jiter;
		this.loss = loss;
		this.latency = latency;
	}
	
	

	private int bw;
	private int jiter;
	private int loss;
	private int latency;

	

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
		return timeEndInjRun;
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
		String [] fault = new String[12];
		fault[0] = getFaultType();
		fault[1] = getTarget();
		fault[2] = getFaultSubtype();
		fault[3] = getTimeStartInjWindow()+"";
		fault[4] = getTimeInjection()+"";
		fault[5] = getTimeEndInjWindow()+"";
		fault[6] = getTimeEndInjRun()+"";
		fault[7] = bw+"";
		fault[8] = jiter+"";
		fault[9] = loss+"";
		fault[10] = latency+"";
		fault[11] = this.getUavModelsToAffectString();
		return fault;
	}


	@Override
	public int getTimeInjection() {
		return timeInjection;
	}

	public int getBw() {
		return bw;
	}

	public int getJiter() {
		return jiter;
	}

	public int getLoss() {
		return loss;
	}

	public int getLatency() {
		return latency;
	}

	@Override
	public List<Integer> getFaultValues() {
		List<Integer> values = new ArrayList<>();
		values.add(bw);
		values.add(jiter);
		values.add(loss);
		values.add(latency);
		return values;
	}
		

}
