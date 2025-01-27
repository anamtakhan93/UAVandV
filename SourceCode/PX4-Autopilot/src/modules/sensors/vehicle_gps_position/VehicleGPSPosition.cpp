/****************************************************************************
 *
 *   Copyright (c) 2020 PX4 Development Team. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 * 3. Neither the name PX4 nor the names of its contributors may be
 *    used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 ****************************************************************************/

#include "VehicleGPSPosition.hpp"

#include <px4_platform_common/log.h>
#include <lib/geo/geo.h>
#include <lib/mathlib/mathlib.h>
#include <stdio.h>
#include <stdlib.h>
#include <uORB/uORB.h>
#include <uORB/topics/debug_key_value.h>
#include <uORB/topics/takeoff_status.h>
#include <uORB/topics/vehicle_air_data.h>
#include <uORB/topics/sensor_combined.h>
#include <uORB/topics/vehicle_acceleration.h>
#include <uORB/topics/vehicle_local_position.h>
#include <uORB/topics/vehicle_attitude.h>
#include <uORB/topics/vehicle_angular_velocity.h>
#include <uORB/topics/vehicle_angular_acceleration.h>
#include <uORB/topics/vehicle_global_position.h>
#include <unistd.h>
#include <poll.h>
#include <iostream>
#include <fstream>
#include <limits>
#include <cmath>
#include <string>
#include <vector>
#include <thread>
#include <chrono>
#include <math.h>
#include <stdexcept>
#include <cstdlib>
//#include <mavlink/mavlink_main.h>
#include "FaultDetection.cpp"  // Include the script for fault detection
#include <thread>


//////////////////////////////////////// added for fault injection
double injected_gps_lat = std::numeric_limits<double>::quiet_NaN();
double injected_gps_lon = std::numeric_limits<double>::quiet_NaN();
double injected_gps_alt = std::numeric_limits<double>::quiet_NaN();
double mission_deviation = std::numeric_limits<double>::quiet_NaN();
double start_injection_time = std::numeric_limits<double>::quiet_NaN();
double end_injection_time = std::numeric_limits<double>::quiet_NaN();
double delay_value = std::numeric_limits<double>::quiet_NaN();
//1 = Fixed, 2 = Freeze, 3 = Delay
int fault_mode = 0;
//Varibles for takeoff detection
bool takeoff_detected = false;
bool valueFreezed = false;
uint64_t takeoff_start_timestamp;
int takeoff_value = 0, aux_takeoff_value = 0;
double air_density = 0, gyroX = 0, gyroY = 0, gyroZ = 0;
int dev1 = 10000000, dev2 = 100000;
bool goldrun = true;
vehicle_gps_position_s gps_previous{};
vehicle_gps_position_s gps_pos{};
int usedPredictedValues = 0;

///////////////////////////////////////////////////////////////////
std::string filename = "/home/anamta/Injections/Fault.fc";

std::string FaultType = "a";
std::string FaultSubType = "a";
std::string FaultTarget = "a";
double InjectionTimeStart = std::numeric_limits<double>::quiet_NaN();
double InjectionTimeEnd = std::numeric_limits<double>::quiet_NaN();
std::string Values[6]= {"","","","","",""};
double DoubleValues[6];
std::string InjectionType = "Time";
std::string InjectionLocationS[2]= {"",""};
double InjectionLocation[2];
double InjectionRadius = std::numeric_limits<double>::quiet_NaN();
struct vehicle_air_data_s air_data;
int airData_sub = 0, sensros_sub = 0;
int predictedLat = 0, predictedLon = 0, predictedAlt = 0, calculatedLat = 0, calculatedLon = 0, calculatedAlt = 0, hybridLat = 0, hybridLon = 0, hybridAlt = 0;
int ZigZag=1;
int UseML = 0;
bool injectingFault = false;

int sensors_sub = 0;
struct sensor_combined_s sensors_data;
int vacc_sub = 0;
struct vehicle_acceleration_s vehicle_acceleration_data;
int gps_sub = 0;
struct vehicle_local_position_s vehicle_local_position_data;
int vatt_sub = 0;
struct vehicle_attitude_s vehicle_attitude_data;
int vangvel_sub = 0;
struct vehicle_angular_velocity_s vehicle_angular_velocity_data;
int vangacc_sub = 0;
struct vehicle_angular_acceleration_s vehicle_angular_acceleration_data;
int vgp_sub = 0;
struct vehicle_global_position_s vehicle_global_position_data;
double PreviousTimestamp;
double accX = 0, accY = 0, accZ = 0;
double velX = 0, velY = 0, velZ = 0;
double RollSpeed = 0, PitchSpeed = 0, YawSpeed = 0;
double RollAcc = 0, PitchAcc = 0, YawAcc = 0;
double lat, lon, alt;
double latcurrent, loncurrent;
double lat3, lon3;
int formulaUsed = 0;
int FaultCounter = 0; //Added for fault checking delay



///////////////////////////////////////////////////////////////////



namespace sensors
{

using namespace matrix;  /////////////////// added for fault injection
using math::constrain;   /////////////////// added for fault injection

VehicleGPSPosition::VehicleGPSPosition() :
	ModuleParams(nullptr),
	ScheduledWorkItem(MODULE_NAME, px4::wq_configurations::nav_and_controllers)
{
}

VehicleGPSPosition::~VehicleGPSPosition()
{
	Stop();
	perf_free(_cycle_perf);
}


void SubscribeToTopics(){
}

void UpdateSensorsValues(){
}




bool VehicleGPSPosition::Start()
{
	// force initial updates
	ParametersUpdate(true);
	ReadConfigFile();  /////////////////// added for fault injection
	ScheduleNow();
	SubscribeToTopics();
	PreviousTimestamp = clock();

	return true;
}

void VehicleGPSPosition::Stop()
{
	Deinit();
	// clear all registered callbacks
	for (auto &sub : _sensor_gps_sub) {
		sub.unregisterCallback();
	}
	int ret_val = system("pkill -f 'python3.8 anomaly_api.py --serve'");
	    // You can check if the system command executed successfully (optional)
    if (ret_val != 0) {
        // Handle the error, e.g., print a message
        std::cerr << "Failed to start the Flask server" << std::endl;
    }
}


//////////////////////////////////////////// added for fault injection

void VehicleGPSPosition::ReadConfigFile(){
	std::ifstream ConfigFile;
	std::string text;
	std::string tempString, tempString2;
	std::string tempValue, tempValue2;
	std::vector<std::string> file_contents;
	
	ConfigFile.open(filename.c_str());
	
	if (ConfigFile.is_open()){
		goldrun=false;
		PX4_INFO("not a goldrun");
	}
		
	//PX4_INFO("---------------------- UAV port number = %zu\n", get_remote_port());


	while (getline (ConfigFile, text)){
		PX4_INFO("Line is %s", text.c_str());
		file_contents.push_back(text);
	}

	ConfigFile.close();

	for(std::size_t i = 0; i < file_contents.size(); i++){
		tempString = file_contents[i];
		PX4_INFO("parsing ... %s", tempString.c_str());

		//////////////////////////////////////////////////////////////////////////
		if(tempString.find("FaultType") != std::string::npos){
			FaultType = tempString.substr(12, tempString.size());
			PX4_INFO("FaultType %s\n", FaultType.c_str());

		} else if (tempString.find("FaultSubType") != std::string::npos) {
			FaultSubType = tempString.substr(15, tempString.size());
			PX4_INFO("FaultSubType %s\n", FaultSubType.c_str());

		} else if (tempString.find("FaultTarget") != std::string::npos) {
			FaultTarget = tempString.substr(14, tempString.size());
			PX4_INFO("FaultTarget %s\n", FaultTarget.c_str());

		} else if (tempString.find("InjectionType") != std::string::npos){
			InjectionType = tempString.substr(16, tempString.size());
			PX4_INFO("InjectionType %s\n", InjectionType.c_str());

		} else if (tempString.find("UseML") != std::string::npos){
			tempString = tempString.substr(8, tempString.size());
			if(tempString.compare("nan") != 0){
				UseML = std::stod(tempString);
				PX4_INFO("Prediction Type %d\n", UseML);
			}

		} else if (tempString.find("InjectionLocation") != std::string::npos){
			
			int s = tempString.size() -1;
			tempString = tempString.substr(10, s);
			PX4_INFO("Injection Location %s\n", tempString.c_str());

			std::string delimiter = ", ";

			size_t pos = 0;
			std::string token;
			int counter=0;
			while ((pos = tempString.find(delimiter)) != std::string::npos) {
    				token = tempString.substr(0, pos);
				InjectionLocationS[counter]=token;
				sscanf (InjectionLocationS[0].c_str(),"%lf",&InjectionLocation[counter++]);
    				//std::cout << token << std::endl;
				//PX4_INFO("Value Token %s\n", token.c_str());
    				tempString.erase(0, pos + delimiter.length());
				PX4_INFO("Injection Location %s\n", InjectionLocationS[counter-1].c_str());
				
			}
			delimiter = "]";
			pos = tempString.find(delimiter);
			token = tempString.substr(0, pos);
			InjectionLocationS[counter]=token;
			sscanf (InjectionLocationS[0].c_str(),"%lf",&InjectionLocation[counter]);
			PX4_INFO("Injection Location %s\n", InjectionLocationS[counter].c_str());
		
		} else if (tempString.find("InjectionRadius") != std::string::npos){
			tempString = tempString.substr(18, tempString.size());
			if(tempString.compare("nan") != 0){
				//end_injection_time = atof(tempValue2.c_str()) * 1000000;
				InjectionRadius = std::stod(tempString); // * 1000000;
				PX4_INFO("Injection Radius is %f\n", InjectionRadius);
			}
			else{
			InjectionRadius = 0;
			}
		
		} else if (tempString.find("InjectionTimeStart") != std::string::npos){
			tempString = tempString.substr(21, tempString.size());
			if(tempString.compare("nan") != 0){
				//end_injection_time = atof(tempValue2.c_str()) * 1000000;
				InjectionTimeStart = std::stod(tempString) * 1000000;
				PX4_INFO("Injected start time is %f\n", InjectionTimeStart);
			}
		
		} else if (tempString.find("InjectionTimeEnd") != std::string::npos){
			tempString = tempString.substr(19, tempString.size());
			if(tempString.compare("nan") != 0){
				//end_injection_time = atof(tempValue2.c_str()) * 1000000;
				InjectionTimeEnd = std::stod(tempString) * 1000000;
				PX4_INFO("Injected end time is %f\n", InjectionTimeEnd);
			}

		} else if (tempString.find("Values") != std::string::npos){
			int s = tempString.size() -1;
			tempString = tempString.substr(10, s);
			PX4_INFO("Values %s\n", tempString.c_str());

			std::string delimiter = ", ";

			size_t pos = 0;
			std::string token;
			int counter=0;
			while ((pos = tempString.find(delimiter)) != std::string::npos) {
    				token = tempString.substr(0, pos);
				Values[counter]=token;
				sscanf (Values[0].c_str(),"%lf",&DoubleValues[counter++]);
    				//std::cout << token << std::endl;
				//PX4_INFO("Value Token %s\n", token.c_str());
    				tempString.erase(0, pos + delimiter.length());
				PX4_INFO("Values %s\n", Values[counter-1].c_str());
				
			}
			delimiter = "]";
			pos = tempString.find(delimiter);
			token = tempString.substr(0, pos);
			Values[counter]=token;
			sscanf (Values[0].c_str(),"%lf",&DoubleValues[counter]);
			PX4_INFO("Values %s\n", Values[counter].c_str());


		}
		

}

///////////////////////////////////////////////////////////


void VehicleGPSPosition::ParametersUpdate(bool force)
{
	// Check if parameters have changed
	if (_parameter_update_sub.updated() || force) {
		// clear update
		parameter_update_s param_update;
		_parameter_update_sub.copy(&param_update);

		updateParams();

		if (_param_sens_gps_mask.get() == 0) {
			_sensor_gps_sub[0].registerCallback();

		} else {
			for (auto &sub : _sensor_gps_sub) {
				sub.registerCallback();
			}
		}

		_gps_blending.setBlendingUseSpeedAccuracy(_param_sens_gps_mask.get() & BLEND_MASK_USE_SPD_ACC);
		_gps_blending.setBlendingUseHPosAccuracy(_param_sens_gps_mask.get() & BLEND_MASK_USE_HPOS_ACC);
		_gps_blending.setBlendingUseVPosAccuracy(_param_sens_gps_mask.get() & BLEND_MASK_USE_VPOS_ACC);
		_gps_blending.setBlendingTimeConstant(_param_sens_gps_tau.get());
		_gps_blending.setPrimaryInstance(_param_sens_gps_prime.get());
	}
}

void VehicleGPSPosition::Run()
{
	perf_begin(_cycle_perf);
	ParametersUpdate();

	// GPS blending
	//ScheduleDelayed(500_ms); // backup schedule

	// Check all GPS instance
	bool any_gps_updated = false;
	bool gps_updated = false;

	for (uint8_t i = 0; i < GPS_MAX_RECEIVERS; i++) {
		gps_updated = _sensor_gps_sub[i].updated();

		sensor_gps_s gps_data;

		if (gps_updated) {
			any_gps_updated = true;

			_sensor_gps_sub[i].copy(&gps_data);
			_gps_blending.setGpsData(gps_data, i);

			if (!_sensor_gps_sub[i].registered()) {
				_sensor_gps_sub[i].registerCallback();
			}
		}
	}

	if (any_gps_updated) {
		_gps_blending.update(hrt_absolute_time());

		if (_gps_blending.isNewOutputDataAvailable()) {
			Publish(_gps_blending.getOutputGpsData(), _gps_blending.getSelectedGps());
		}
	}

	perf_end(_cycle_perf);
}





std::string exeCmd(const char* cmd) {
    char buffer[128];
    std::string result = "";
    FILE* pipe = popen(cmd, "r");
    //if (!pipe) throw std::runtime_error("popen() failed!");
        while (fgets(buffer, sizeof buffer, pipe) != NULL) {
            result += buffer;
        }
    pclose(pipe);
    return result;
}


void VehicleGPSPosition::Publish(const sensor_gps_s &gps, uint8_t selected)
{
	vehicle_gps_position_s gps_output{};
	vehicle_gps_position_s gps_raw{};
		
	

	///////////////////////////////////////////////////////////////////// added for fault injection
	gps_output.timestamp = gps.timestamp;
	gps_output.time_utc_usec = gps.time_utc_usec;
	//PX4_INFO("----------------------------------------------------Timestamp = %zu\n",gps.timestamp);
	injectingFault = false;


	if (!goldrun){

		int takeoff_sub = orb_subscribe(ORB_ID(takeoff_status));
		if(!takeoff_detected){
			PX4_INFO("get takeoff value ........ : %d", takeoff_value);
			orb_set_interval(takeoff_sub, 20);
		
			struct takeoff_status_s takeoff;
			orb_copy(ORB_ID(takeoff_status), takeoff_sub, &takeoff);

			takeoff_value = takeoff.takeoff_state;

			PX4_INFO("tookoff value is : %d", takeoff_value);
			PX4_INFO("takeoff detected is: %d", takeoff_detected);
		}	
		
		//PX4_INFO("----------------------------------------------------aux_takeoff_value = %d\n",aux_takeoff_value);
		if((takeoff_value >= 5) || takeoff_detected == true){

			if(takeoff_detected == false){
				PX4_INFO("takeoff detected");
				//start counting
				takeoff_start_timestamp = gps.timestamp;
				takeoff_detected = true;
				PX4_INFO("----------------------------------------------------Takeoff time = %zu\n",takeoff_start_timestamp);
			}
			
			PX4_INFO("----------------------------------------------------Timestamp after takeoff = %zu\n",gps.timestamp - takeoff_start_timestamp);
			////////////////////////////////////////////////////////////////// GPS Software failures
			if(FaultType.find("Software") != std::string::npos){
				if (FaultTarget.find("GPS") != std::string::npos){
				//PX4_INFO("----------------------------------------------------Software and GPS issue");
				PX4_INFO("Current Distance Value: %f", GetDistance(gps.lat, gps.lon, InjectionLocation[0],InjectionLocation[1]));
					
					if (((!std::isnan(InjectionTimeStart) && !std::isnan(InjectionTimeEnd) && (gps.timestamp - takeoff_start_timestamp >= InjectionTimeStart) && (gps.timestamp - takeoff_start_timestamp <= InjectionTimeEnd)) && InjectionType.find("Time") != std::string::npos) ||
					((GetDistance(gps.lat, gps.lon, InjectionLocation[0],InjectionLocation[1]) <= InjectionRadius) && InjectionType.find("Location") != std::string::npos) ||
					((GetDistance(gps.lat, gps.lon, InjectionLocation[0],InjectionLocation[1]) <= InjectionRadius) && (!std::isnan(InjectionTimeStart) && !std::isnan(InjectionTimeEnd) && (gps.timestamp - takeoff_start_timestamp >= InjectionTimeStart) && (gps.timestamp - takeoff_start_timestamp <= InjectionTimeEnd)) && InjectionType.find("TandL") != std::string::npos)){
					injectingFault = true;
			
			

						if(FaultSubType.find("FixedValue") != std::string::npos){

							PX4_INFO("----------------------------------------------------injecting gps fixed value");
							gps_output.lat = DoubleValues[0]; 
							gps_output.lon = DoubleValues[1]; 
							gps_output.alt = DoubleValues[2]; 


						}else if(FaultSubType.find("FreezeValue") != std::string::npos){
							//Freeze Value
							PX4_INFO("----------------------------------------------------injecting gps freeze value");
							if (!valueFreezed){
								injected_gps_lat = gps.lat;
								injected_gps_lon = gps.lon;
								injected_gps_alt = gps.alt;
								valueFreezed = true;
							}
							
							gps_output.lat = injected_gps_lat;
							gps_output.lon = injected_gps_lon;
							gps_output.alt = injected_gps_alt;


						}else if(FaultSubType.find("DelayValue") != std::string::npos){
							//Delay Value
							PX4_INFO("----------------------------------------------------injecting gps delay value");
							std::this_thread::sleep_for(std::chrono::seconds((int)DoubleValues[0]));
							gps_output.lat = gps.lat;
							gps_output.lon = gps.lon;
							gps_output.alt = gps.alt;

						}else if (FaultSubType.find("RandomValue") != std::string::npos){
							PX4_INFO("----------------------------------------------------injecting gps random value");
							// (rand() % (max-min) ) + min
							gps_output.lat = 111111111 + rand() % (999999999-111111111);
							gps_output.lon = (1111111 + rand() % (9999999-1111111)) * -1;
							gps_output.alt = 11111 + rand() % (99999-11111);

						
						}else if (FaultSubType.find("MinLatitude") != std::string::npos){
							PX4_INFO("----------------------------------------------------injecting gps MinLatitude value");
							gps_output.lat = 111111111;
							gps_output.lon = gps.lon;
							gps_output.alt = gps.alt;
						
						}else if (FaultSubType.find("MinLongtitude") != std::string::npos){
							PX4_INFO("----------------------------------------------------injecting gps MinLongtitude value");
							gps_output.lat = gps.lat;
							gps_output.lon = -9999999;
							gps_output.alt = gps.alt;
						
						}else if (FaultSubType.find("MinAltitude") != std::string::npos){
							PX4_INFO("----------------------------------------------------injecting gps MinAltitude value");
							gps_output.lat = gps.lat;
							gps_output.lon = gps.lon;
							gps_output.alt = 11111;
						
						}else if (FaultSubType.find("MaxLatitude") != std::string::npos){
							PX4_INFO("----------------------------------------------------injecting gps MaxLatitude value");
							gps_output.lat = 999999999;
							gps_output.lon = gps.lon;
							gps_output.alt = gps.alt;
						
						}else if (FaultSubType.find("MaxLongtitude") != std::string::npos){
							PX4_INFO("----------------------------------------------------injecting gps MaxLongtitude value");
							gps_output.lat = gps.lat;
							gps_output.lon = -1111111;
							gps_output.alt = gps.alt;
						
						}else if (FaultSubType.find("MaxAltitude") != std::string::npos){
							PX4_INFO("----------------------------------------------------injecting gps MaxAltitude value");
							gps_output.lat = gps.lat;
							gps_output.lon = gps.lon;
							gps_output.alt = 99999;
						
						}else if (FaultSubType.find("FixedNoise") != std::string::npos){
							PX4_INFO("----------------------------------------------------injecting gps Fixed Noise");
							gps_output.lat = gps.lat + DoubleValues[0]; 
							gps_output.lon = gps.lon + DoubleValues[1]; 
							gps_output.alt = gps.alt + DoubleValues[2];
						
						}else if (FaultSubType.find("RandomNoise") != std::string::npos){
							PX4_INFO("----------------------------------------------------injecting gps Random Noise");
							gps_output.lat = gps.lat + (int)(1111 + (rand() % (99999-1111)));
							gps_output.lon = gps.lon + ((int)(11111 + (rand() % (999999-11111))) * -1);
							gps_output.alt = gps.alt + (int)(111 + (rand() % (9999-111)));
						
						}else if(FaultSubType.find("ZigZag") != std::string::npos){
							//Zig Zag
							PX4_INFO("----------------------------------------------------injecting gps Zig Zag value");
							if (ZigZag == 1){
								ZigZag = -1;
							}
							else{
								ZigZag = 1;
							}
							
							gps_output.lat = gps.lat + (DoubleValues[0] * ZigZag);
							gps_output.lon = gps.lon + (DoubleValues[1] * ZigZag);
							gps_output.alt = gps.alt + (DoubleValues[2] * ZigZag);


						}else if(FaultSubType.find("Custom") != std::string::npos){
							//Custom Fault
							PX4_INFO("----------------------------------------------------injecting gps Custom fault");
							
							std::string command = "cd /home/[path]/CustomFaults && ./predictLatLonAlt " 
                     							 + std::to_string(gps_previous.lat) + " " 
                   							   + std::to_string(gps_previous.lon) + " " 
                   							   + std::to_string(gps_previous.alt);

							// Execute the command and capture the output
							std::string output = exeCmd(command.c_str());

							double customLat = 0.0, customLon = 0.0, customAlt = 0.0;
							sscanf(output.c_str(), "%lf %lf %lf", &gps_output.lat, &gps_output.lon, &gps_output.akt);



						}else{
							gps_output.lat = gps.lat;
							gps_output.lon = gps.lon;
							gps_output.alt = gps.alt;
						}

                    }else {
                        gps_output.lat = gps.lat;
                        gps_output.lon = gps.lon;
                        gps_output.alt = gps.alt;
                    }
                    
                    PX4_INFO("Software Failure Injected");

				PX4_INFO("Fault Sub Type %s", FaultSubType.c_str());
				PX4_INFO("Fault Target %s", FaultTarget.c_str());

				PX4_INFO("Injected start time is %f", InjectionTimeStart);
				PX4_INFO("Injected end time is %f", InjectionTimeEnd);

				PX4_INFO("Latitude Before Injection: %d", gps.lat);
				PX4_INFO("Longitude Before Injection: %d", gps.lon);
				PX4_INFO("Altitude Before Injection: %d", gps.alt);

				PX4_INFO("Latitude After Injection: %d", gps_output.lat);
				PX4_INFO("Longitude After Injection: %d", gps_output.lon);
				PX4_INFO("Altitude After Injection: %d", gps_output.alt);

			////////////////////////////////////////////////////////////////// GPS Security Issues: GPS Spoofing
			}else if (FaultType.find("Security") != std::string::npos){
				if (FaultTarget.find("GPS") != std::string::npos){

					if (((!std::isnan(InjectionTimeStart) && !std::isnan(InjectionTimeEnd) && (gps.timestamp - takeoff_start_timestamp >= InjectionTimeStart) && (gps.timestamp - takeoff_start_timestamp <= InjectionTimeEnd)) && InjectionType.find("Time") != std::string::npos) ||
					((GetDistance(gps.lat, gps.lon, InjectionLocation[0],InjectionLocation[1]) <= InjectionRadius) && InjectionType.find("Location") != std::string::npos) ||
					((GetDistance(gps.lat, gps.lon, InjectionLocation[0],InjectionLocation[1]) <= InjectionRadius) && (!std::isnan(InjectionTimeStart) && !std::isnan(InjectionTimeEnd) && (gps.timestamp - takeoff_start_timestamp >= InjectionTimeStart) && (gps.timestamp - takeoff_start_timestamp <= InjectionTimeEnd)) && InjectionType.find("TandL") != std::string::npos)){
					injectingFault = true;

						if(FaultSubType.find("HijackByFixedPosition") != std::string::npos){

							//Fixed position of attacker is used to hijack a UAV
							PX4_INFO("---------------------------------------------------- injecting gps Hijack By Fixed Position");
							gps_output.lat = DoubleValues[0]; 
							gps_output.lon = DoubleValues[1]; 
							gps_output.alt = DoubleValues[2]; 


						}else if(FaultSubType.find("HijackByUAV") != std::string::npos){
							// assuming that the other UAV is close to the UAV
							PX4_INFO("---------------------------------------------------- injecting gps Hijack By UAV");
							gps_output.lat = gps.lat + DoubleValues[0];
							gps_output.lon = gps.lon + DoubleValues[1];
							gps_output.alt = gps.alt + DoubleValues[2];
							

						}else if(FaultSubType.find("DelayValue") != std::string::npos){
							//Delay Value
							//Delay Value
							PX4_INFO("----------------------------------------------------injecting gps delay value");
							std::this_thread::sleep_for(std::chrono::seconds((int)DoubleValues[0]));
							gps_output.lat = gps.lat;
							gps_output.lon = gps.lon;
							gps_output.alt = gps.alt;

						}else if (FaultSubType.find("RandomValue") != std::string::npos){
							PX4_INFO("----------------------------------------------------injecting gps random value");
							// (rand() % (max-min) ) + min
							gps_output.lat = DoubleValues[0] + rand() % ((int)DoubleValues[1]-(int)DoubleValues[0]);
							gps_output.lon = DoubleValues[2] + rand() % ((int)DoubleValues[3]-(int)DoubleValues[2]);
							gps_output.alt = DoubleValues[4] + rand() % ((int)DoubleValues[5]-(int)DoubleValues[4]);
						
						}else if (FaultSubType.find("ForceLanding") != std::string::npos){
							// tampers th altitude values with slightly higher values than real one, trying to force an unplanned landing
							PX4_INFO("----------------------------------------------------injecting gps force landing");
							gps_output.lat = gps.lat;
							gps_output.lon = gps.lon;
							gps_output.alt = gps.alt + 100000; // 1 meter for instance

						
						}else if (FaultSubType.find("RandomLongtitude") != std::string::npos){ // -180   ... 180
							PX4_INFO("----------------------------------------------------injecting gps random longitude");
							gps_output.lat = gps.lat;
							gps_output.lon = rand() % 360 -180 ;
							gps_output.alt = gps.alt;
						
						}else if (FaultSubType.find("RandomLatitude") != std::string::npos){ // -90     90
							PX4_INFO("----------------------------------------------------injecting gps random longitude");
							gps_output.lat =  rand() % 180 -90 ;
							gps_output.lon = gps.lon;
							gps_output.alt = gps.alt;

						
						}else{
							gps_output.lat = gps.lat;
							gps_output.lon = gps.lon;
							gps_output.alt = gps.alt;
						}


                    }else {
                        gps_output.lat = gps.lat;
                        gps_output.lon = gps.lon;
                        gps_output.alt = gps.alt;
                    }
				
				}else {
					gps_output.lat = gps.lat;
					gps_output.lon = gps.lon;
					gps_output.alt = gps.alt;
				}


				PX4_INFO("Security Issues Injected");

				PX4_INFO("Fault Sub Type %s", FaultSubType.c_str());
				PX4_INFO("Fault Target %s", FaultTarget.c_str());

				PX4_INFO("Injected start time is %f", InjectionTimeStart);
				PX4_INFO("Injected end time is %f", InjectionTimeEnd);

				PX4_INFO("Latitude Before Injection: %d", gps.lat);
				PX4_INFO("Longitude Before Injection: %d", gps.lon);
				PX4_INFO("Altitude Before Injection: %d", gps.alt);

				PX4_INFO("Latitude After Injection: %d", gps_output.lat);
				PX4_INFO("Longitude After Injection: %d", gps_output.lon);
				PX4_INFO("Altitude After Injection: %d", gps_output.alt);




			} else {
				gps_output.lat = gps.lat;
				gps_output.lon = gps.lon;
				gps_output.alt = gps.alt;
			}

			if (FaultTarget.find("GPS") != std::string::npos){

		}else{
			gps_output.lat = gps.lat;
			gps_output.lon = gps.lon;
			gps_output.alt = gps.alt;
		}
		
		aux_takeoff_value = takeoff_value;
		//PX4_INFO("timestamp: %" PRIu64 "\n", gps.timestamp);
		//PX4_INFO("time utc usec: %" PRIu64 "\n", gps.time_utc_usec);
		/////////////////////////////////////////////////////////////////////////////////
	}else {

		gps_output.lat = gps.lat;
		gps_output.lon = gps.lon;
		gps_output.alt = gps.alt;
	}

	//////////////////////////////////////////////////////removed for fault injection
	//gps_output.timestamp = gps.timestamp;
	//gps_output.time_utc_usec = gps.time_utc_usec;
	//gps_output.lat = gps.lat;
	//gps_output.lon = gps.lon;
	//gps_output.alt = gps.alt;
        ///////////////////////////////////////////////////////////////////
        

	}
	else{//Inject fault
	}
	
	
	

					
				}else {
					gps_output.lat = gps.lat;
					gps_output.lon = gps.lon;
					gps_output.alt = gps.alt;
				}
				}

				

	gps_output.alt_ellipsoid = gps.alt_ellipsoid;
	gps_output.s_variance_m_s = gps.s_variance_m_s;
	gps_output.c_variance_rad = gps.c_variance_rad;
	gps_output.eph = gps.eph;
	gps_output.epv = gps.epv;
	gps_output.hdop = gps.hdop;
	gps_output.vdop = gps.vdop;
	gps_output.noise_per_ms = gps.noise_per_ms;
	gps_output.jamming_indicator = gps.jamming_indicator;
	gps_output.jamming_state = gps.jamming_state;
	gps_output.vel_m_s = gps.vel_m_s;
	gps_output.vel_n_m_s = gps.vel_n_m_s;
	gps_output.vel_e_m_s = gps.vel_e_m_s;
	gps_output.vel_d_m_s = gps.vel_d_m_s;
	gps_output.cog_rad = gps.cog_rad;
	gps_output.timestamp_time_relative = gps.timestamp_time_relative;
	gps_output.heading = gps.heading;
	gps_output.heading_offset = gps.heading_offset;
	gps_output.fix_type = gps.fix_type;
	gps_output.vel_ned_valid = gps.vel_ned_valid;
	gps_output.satellites_used = gps.satellites_used;
	gps_output.selected = selected;
	
    gps_raw = gps_output;
    gps_raw.lat = gps.lat;
    gps_raw.lon = gps.lon;
    gps_raw.alt = gps.alt;
    
    gps_previous = gps_raw;
    
    _vehicle_gps_position_pub.publish(gps_output);
    		
	//std::thread t1(CheckForFault);
	//t1.detach();
		
    _vehicle_gps_position_raw_pub.publish(gps_raw);
}

void VehicleGPSPosition::PrintStatus()
{
	//PX4_INFO("selected GPS: %d", _gps_select_index);
	


}

}; // namespace sensors