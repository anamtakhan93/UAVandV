/****************************************************************************
 *
 *   Copyright (c) 2020-2021 PX4 Development Team. All rights reserved.
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

#include "VehicleIMU.hpp"

#include <px4_platform_common/log.h>
#include <lib/systemlib/mavlink_log.h>
#include <float.h>
#include <iostream>
#include <fstream>
#include <unistd.h>
#include <limits>
#include <cmath>
#include <string>
#include <vector>
#include <thread>
#include <chrono>
#include <math.h>
#include <stdexcept>
#include <cstdlib>
#include <stdio.h>
#include <stdlib.h>
#include <sstream>
#include <algorithm>

#include <unistd.h> // For sleep
#include <sys/wait.h> // For waitpid

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
#include "FaultDetection.h"  // Include the script for fault detection


///////////////////////////////////////////////////////////////////
std::string filename_IMU = "/home/anamta/Injections/Fault.fc";

std::string FaultType_IMU = "Software";
std::string FaultSubType_IMU = "FreezeValue";
std::string FaultTarget_IMU = "IMU";
double InjectionTimeStart_IMU = std::numeric_limits<double>::quiet_NaN();
double InjectionTimeEnd_IMU = std::numeric_limits<double>::quiet_NaN();
std::string Values_IMU[6] = {"","","","","",""};
double DoubleValues_IMU[6];
std::string InjectionType_IMU = "Time";
std::string InjectionLocationS_IMU[2] = {"",""};
double InjectionLocation_IMU[2];
bool goldrun_IMU = true;
bool injectingFault_IMU = false;
bool Frozenvalue = false;
int UseML_IMU = 0;
double InjectionRadius_IMU = std::numeric_limits<double>::quiet_NaN();
vehicle_imu_s imu_raw;
//int sensors_sub_IMU = 0;
//struct sensor_combined_s sensors_data_IMU;
int gps_sub_IMU = 0;
struct vehicle_local_position_s vehicle_local_position_data_IMU;
int attitude_sub_IMU = 0;
struct vehicle_attitude_s attitude_data_IMU;
int vangvel_sub_IMU = 0;
struct vehicle_angular_velocity_s vehicle_angular_velocity_data_IMU;
int vangacc_sub_IMU = 0;
struct vehicle_angular_acceleration_s vehicle_angular_acceleration_data_IMU;
int vacc_sub_IMU = 0;
struct vehicle_acceleration_s vehicle_acceleration_data_IMU;



using namespace matrix;

using math::constrain;

namespace sensors
{

VehicleIMU::VehicleIMU(int instance, uint8_t accel_index, uint8_t gyro_index, const px4::wq_config_t &config) :
	ModuleParams(nullptr),
	ScheduledWorkItem(MODULE_NAME, config),
	_sensor_accel_sub(ORB_ID(sensor_accel), accel_index),
	_sensor_gyro_sub(this, ORB_ID(sensor_gyro), gyro_index),
	_instance(instance)
{
	_imu_integration_interval_us = 1e6f / _param_imu_integ_rate.get();

	_accel_integrator.set_reset_interval(_imu_integration_interval_us);
	_accel_integrator.set_reset_samples(sensor_accel_s::ORB_QUEUE_LENGTH);

	_gyro_integrator.set_reset_interval(_imu_integration_interval_us);
	_gyro_integrator.set_reset_samples(sensor_gyro_s::ORB_QUEUE_LENGTH);

#if defined(ENABLE_LOCKSTEP_SCHEDULER)
	// currently with lockstep every raw sample needs a corresponding vehicle_imu publication
	_sensor_gyro_sub.set_required_updates(1);
#else
	// schedule conservatively until the actual accel & gyro rates are known
	_sensor_gyro_sub.set_required_updates(sensor_gyro_s::ORB_QUEUE_LENGTH / 2);
#endif

	// advertise immediately to ensure consistent ordering
	_vehicle_imu_pub.advertise();
	_vehicle_imu_status_pub.advertise();
}

VehicleIMU::~VehicleIMU()
{
	Stop();

	perf_free(_accel_generation_gap_perf);
	perf_free(_gyro_generation_gap_perf);

	_vehicle_imu_pub.unadvertise();
	_vehicle_imu_status_pub.unadvertise();
}

void SbscribeToTopics(){
    
}

bool VehicleIMU::Start()
{
	// force initial updates
	ParametersUpdate(true);
	
	/////////////////// added for fault injection
	ReadConfigFile();
	SbscribeToTopics();

	_sensor_gyro_sub.registerCallback();
	ScheduleNow();
	return true;
}

void VehicleIMU::Stop()
{
	// clear all registered callbacks
	_sensor_gyro_sub.unregisterCallback();

	Deinit();
}



//////////////////////////////////////////// added for fault injection

void VehicleIMU::ReadConfigFile(){
	std::ifstream ConfigFile;
	std::string text;
	std::string tempString, tempString2;
	std::string tempValue, tempValue2;
	std::vector<std::string> file_contents;
	
	ConfigFile.open(filename_IMU.c_str());
	
	if (ConfigFile.is_open()){
		goldrun_IMU=false;
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
			FaultType_IMU = tempString.substr(12, tempString.size());
			PX4_INFO("FaultType %s\n", FaultType_IMU.c_str());

		} else if (tempString.find("FaultSubType") != std::string::npos) {
			FaultSubType_IMU = tempString.substr(15, tempString.size());
			PX4_INFO("FaultSubType %s\n", FaultSubType_IMU.c_str());

		} else if (tempString.find("FaultTarget") != std::string::npos) {
			FaultTarget_IMU = tempString.substr(14, tempString.size());
			PX4_INFO("FaultTarget %s\n", FaultTarget_IMU.c_str());

		} else if (tempString.find("InjectionType") != std::string::npos){
			InjectionType_IMU = tempString.substr(16, tempString.size());
			PX4_INFO("InjectionType %s\n", InjectionType_IMU.c_str());

		} else if (tempString.find("UseML") != std::string::npos){
			tempString = tempString.substr(8, tempString.size());
			if(tempString.compare("nan") != 0){
				UseML_IMU = std::stod(tempString);
				PX4_INFO("Prediction Type %d\n", UseML_IMU);
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
				InjectionLocationS_IMU[counter]=token;
				sscanf (InjectionLocationS_IMU[0].c_str(),"%lf",&InjectionLocation_IMU[counter++]);
    				//std::cout << token << std::endl;
				//PX4_INFO("Value Token %s\n", token.c_str());
    				tempString.erase(0, pos + delimiter.length());
				PX4_INFO("Injection Location %s\n", InjectionLocationS_IMU[counter-1].c_str());
				
			}
			delimiter = "]";
			pos = tempString.find(delimiter);
			token = tempString.substr(0, pos);
			InjectionLocationS_IMU[counter]=token;
			sscanf (InjectionLocationS_IMU[0].c_str(),"%lf",&InjectionLocation_IMU[counter]);
			PX4_INFO("Injection Location %s\n", InjectionLocationS_IMU[counter].c_str());
		
		} else if (tempString.find("InjectionRadius") != std::string::npos){
			tempString = tempString.substr(18, tempString.size());
			if(tempString.compare("nan") != 0){
				//end_injection_time = atof(tempValue2.c_str()) * 1000000;
				InjectionRadius_IMU = std::stod(tempString); // * 1000000;
				PX4_INFO("Injection Radius is %f\n", InjectionRadius_IMU);
			}
			else{
			InjectionRadius_IMU = 0;
			}
		
		} else if (tempString.find("InjectionTimeStart") != std::string::npos){
			tempString = tempString.substr(21, tempString.size());
			if(tempString.compare("nan") != 0){
				//end_injection_time = atof(tempValue2.c_str()) * 1000000;
				InjectionTimeStart_IMU = std::stod(tempString) * 1000000;
				PX4_INFO("Injected start time is %f\n", InjectionTimeStart_IMU);
			}
		
		} else if (tempString.find("InjectionTimeEnd") != std::string::npos){
			tempString = tempString.substr(19, tempString.size());
			if(tempString.compare("nan") != 0){
				//end_injection_time = atof(tempValue2.c_str()) * 1000000;
				InjectionTimeEnd_IMU = std::stod(tempString) * 1000000;
				PX4_INFO("Injected end time is %f\n", InjectionTimeEnd_IMU);
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
				Values_IMU[counter]=token;
				sscanf (Values_IMU[0].c_str(),"%lf",&DoubleValues_IMU[counter++]);
    				//std::cout << token << std::endl;
				//PX4_INFO("Value Token %s\n", token.c_str());
    				tempString.erase(0, pos + delimiter.length());
				PX4_INFO("Values %s\n", Values_IMU[counter-1].c_str());
				
			}
			delimiter = "]";
			pos = tempString.find(delimiter);
			token = tempString.substr(0, pos);
			Values_IMU[counter]=token;
			sscanf (Values_IMU[0].c_str(),"%lf",&DoubleValues_IMU[counter]);
			PX4_INFO("Values %s\n", Values_IMU[counter].c_str());


		}
}
}






void VehicleIMU::ParametersUpdate(bool force)
{
	// Check if parameters have changed
	if (_parameter_update_sub.updated() || force) {
		// clear update
		parameter_update_s param_update;
		_parameter_update_sub.copy(&param_update);

		const auto imu_integ_rate_prev = _param_imu_integ_rate.get();

		//updateParams();

		_accel_calibration.ParametersUpdate();
		_gyro_calibration.ParametersUpdate();

		// constrain IMU integration time 1-10 milliseconds (100-1000 Hz)
		int32_t imu_integration_rate_hz = constrain(_param_imu_integ_rate.get(),
						  (int32_t)100, math::max(_param_imu_gyro_ratemax.get(), (int32_t) 1000));

		if (imu_integration_rate_hz != _param_imu_integ_rate.get()) {
			PX4_WARN("IMU_INTEG_RATE updated %" PRId32 " -> %" PRIu32, _param_imu_integ_rate.get(), imu_integration_rate_hz);
			_param_imu_integ_rate.set(imu_integration_rate_hz);
			_param_imu_integ_rate.commit_no_notification();
		}

		_imu_integration_interval_us = 1e6f / imu_integration_rate_hz;

		if (_param_imu_integ_rate.get() != imu_integ_rate_prev) {
			// force update
			_update_integrator_config = true;
		}
	}
	
}


void VehicleIMU::Run()
{
	
	

	const hrt_abstime now_us = hrt_absolute_time();

	// backup schedule
	ScheduleDelayed(_backup_schedule_timeout_us);

	ParametersUpdate();

	if (!_accel_calibration.enabled() || !_gyro_calibration.enabled()) {
		return;
	}

	// reset data gap monitor
	_data_gap = false;
	
	while (_sensor_gyro_sub.updated() || _sensor_accel_sub.updated()) {
		bool updated = false;

		bool consume_all_gyro = !_intervals_configured || _data_gap;

		// monitor scheduling latency and force catch up with latest gyro if falling behind
		if (_sensor_gyro_sub.updated() && (_gyro_update_latency_mean.count() > 100)
		    && (_gyro_update_latency_mean.mean()(1) > _gyro_interval_us * 1e-6f)) {

			PX4_DEBUG("gyro update mean sample latency: %.6f, publish latency %.6f",
				  (double)_gyro_update_latency_mean.mean()(0),
				  (double)_gyro_update_latency_mean.mean()(1));

			consume_all_gyro = true;
		}

		// update gyro until integrator ready and not falling behind
		if (!_gyro_integrator.integral_ready() || consume_all_gyro) {
			if (UpdateGyro()) {
				updated = true;
			}
		}


		// update accel until integrator ready and caught up to gyro
		while (_sensor_accel_sub.updated()
		       && (!_accel_integrator.integral_ready() || !_intervals_configured || _data_gap
			   || (_accel_timestamp_sample_last < (_gyro_timestamp_sample_last - 0.5f * _accel_interval_us)))) {

			if (UpdateAccel()) {
				updated = true;
			}
		}

		// reconfigure integrators if calculated sensor intervals have changed
		if (_update_integrator_config || !_intervals_configured) {
			UpdateIntegratorConfiguration();
		}

		// check for additional updates and that we're fully caught up before publishing
		if ((consume_all_gyro || _data_gap) && _sensor_gyro_sub.updated()) {
			continue;
		}

		// publish if both accel & gyro integrators are ready
		if (_intervals_configured && _accel_integrator.integral_ready() && _gyro_integrator.integral_ready()) {
			if (Publish()) {
				// record gyro publication latency and integrated samples
				if (_gyro_update_latency_mean.count() > 10000) {
					// reset periodically to avoid numerical issues
					_gyro_update_latency_mean.reset();
				}

				const float time_run_s = now_us * 1e-6f;

				_gyro_update_latency_mean.update(Vector2f{time_run_s - _gyro_timestamp_sample_last * 1e-6f, time_run_s - _gyro_timestamp_last * 1e-6f});

				return;
			}
		}
		
		// finish if there are no more updates, but didn't publish
		if (!updated) {
			return;
		}
	}
	

}



double RandomFloat(double a, double b) {
    double random = ((double) rand()) / (double) RAND_MAX;
    double diff = b - a;
    double r = random * diff;
    return a + r;
}

bool VehicleIMU::UpdateAccel()
{
	bool updated = false;

	// integrate queued accel
	sensor_accel_s accel;

	if (_sensor_accel_sub.update(&accel)) {
		l_AccX = accel.x;
		l_AccY = accel.y;
		l_AccZ = accel.z;
		if (_sensor_accel_sub.get_last_generation() != _accel_last_generation + 1) {
			_data_gap = true;
			perf_count(_accel_generation_gap_perf);

			// reset average sample measurement
			_accel_interval_mean.reset();

		} else {
			// collect sample interval average for filters
			if (_accel_timestamp_sample_last != 0) {
				float interval_us = accel.timestamp_sample - _accel_timestamp_sample_last;
				_accel_interval_mean.update(Vector2f{interval_us, interval_us / accel.samples});
			}

			if (_accel_interval_mean.valid() && (_accel_interval_mean.count() > 100 || !PX4_ISFINITE(_accel_interval_best_variance))
			    && ((_accel_interval_mean.variance()(0) < _accel_interval_best_variance) || (_accel_interval_mean.count() > 1000))) {
				// update sample rate if previously invalid or changed
				const float interval_delta_us = fabsf(_accel_interval_mean.mean()(0) - _accel_interval_us);
				const float percent_changed = interval_delta_us / _accel_interval_us;

				if (!PX4_ISFINITE(_accel_interval_us) || (percent_changed > 0.001f)) {
					// update integrator configuration if interval has changed by more than 10%
					if (interval_delta_us > 0.1f * _accel_interval_us) {
						_update_integrator_config = true;
					}

					_accel_interval_us = _accel_interval_mean.mean()(0);
					_accel_interval_best_variance = _accel_interval_mean.variance()(0);

					_status.accel_rate_hz = 1e6f / _accel_interval_mean.mean()(0);
					_status.accel_raw_rate_hz = 1e6f / _accel_interval_mean.mean()(1); // FIFO
					_publish_status = true;
				}

				if (_accel_interval_mean.count() > 10000) {
					// reset periodically to prevent numerical issues
					_accel_interval_mean.reset();
				}
			}
		}

		_accel_last_generation = _sensor_accel_sub.get_last_generation();

		_accel_calibration.set_device_id(accel.device_id);

		if (accel.error_count != _status.accel_error_count) {
			_publish_status = true;
			_status.accel_error_count = accel.error_count;
		}
		
		//Fault injection here
		//if(timer > 60000){
		//accel.x = RandomFloat(0.1,1.0);
		//accel.y = RandomFloat(0.1,1.0);
		//accel.z = RandomFloat(0.1,1.0);
		//}
		
		
		
		if(FaultType_IMU.find("Software") != std::string::npos){
				if (FaultTarget_IMU.find("acc") != std::string::npos){
				
				PX4_INFO("---------------------------------------------------- IMU fault is there");
					
					if (((!std::isnan(InjectionTimeStart_IMU) && !std::isnan(InjectionTimeEnd_IMU) && (accel.timestamp >= InjectionTimeStart_IMU) && (accel.timestamp <= InjectionTimeEnd_IMU)) && InjectionType_IMU.find("Time") != std::string::npos)){
					injectingFault_IMU = true;
					
					if(FaultSubType_IMU.find("FixedValue") != std::string::npos){

							PX4_INFO("----------------------------------------------------injecting IMU fixed value");
							
							accel.x = DoubleValues_IMU[0];
							accel.y = DoubleValues_IMU[1];
							accel.z = DoubleValues_IMU[2];


						}else if(FaultSubType_IMU.find("FreezeValue") != std::string::npos){
						if (!Frozenvalue){
								DoubleValues_IMU[0] = accel.x;
								DoubleValues_IMU[1] = accel.y;
								DoubleValues_IMU[2] = accel.z;
								Frozenvalue = true;
							}
							
							accel.x = DoubleValues_IMU[0];
							accel.y = DoubleValues_IMU[1];
							accel.z = DoubleValues_IMU[2];
							
						}else if(FaultSubType_IMU.find("RandomValue") != std::string::npos){
						accel.x = RandomFloat(-99999,99999);
						accel.y = RandomFloat(-99999,99999);
						accel.z = RandomFloat(-99999,99999);
						}else if(FaultSubType_IMU.find("MinValue") != std::string::npos){
						accel.x = -99999;
						accel.y = -99999;
						accel.z = -99999;
						}else if(FaultSubType_IMU.find("MaxValue") != std::string::npos){
						accel.x = 99999;
						accel.y = 99999;
						accel.z = 99999;
						}else if(FaultSubType_IMU.find("RandomNoise") != std::string::npos){
						accel.x = accel.x + (float)(-0.00999 + ((rand() % (9-1))*0.01));
						accel.y = accel.y + (float)(-0.00999 + ((rand() % (9-1))*0.01));
						accel.z = accel.z + (float)(-0.00999 + ((rand() % (9-1))*0.01));
						}
						else{
						}
						
					    
					}
					}
					}
		
		r_accel = accel;

		const Vector3f accel_raw{accel.x, accel.y, accel.z};
		_accel_sum += accel_raw;
		_accel_temperature += accel.temperature;
		_accel_sum_count++;

		const float dt = (accel.timestamp_sample - _accel_timestamp_sample_last) * 1e-6f;
		_accel_timestamp_sample_last = accel.timestamp_sample;

		_accel_integrator.put(accel_raw, dt);
		updated = true;

		if (accel.clip_counter[0] > 0 || accel.clip_counter[1] > 0 || accel.clip_counter[2] > 0) {
			// rotate sensor clip counts into vehicle body frame
			const Vector3f clipping{_accel_calibration.rotation() *
						Vector3f{(float)accel.clip_counter[0], (float)accel.clip_counter[1], (float)accel.clip_counter[2]}};

			// round to get reasonble clip counts per axis (after board rotation)
			const uint8_t clip_x = roundf(fabsf(clipping(0)));
			const uint8_t clip_y = roundf(fabsf(clipping(1)));
			const uint8_t clip_z = roundf(fabsf(clipping(2)));

			_status.accel_clipping[0] += clip_x;
			_status.accel_clipping[1] += clip_y;
			_status.accel_clipping[2] += clip_z;

			if (clip_x > 0) {
				_delta_velocity_clipping |= vehicle_imu_s::CLIPPING_X;
			}

			if (clip_y > 0) {
				_delta_velocity_clipping |= vehicle_imu_s::CLIPPING_Y;
			}

			if (clip_z > 0) {
				_delta_velocity_clipping |= vehicle_imu_s::CLIPPING_Z;
			}

			_publish_status = true;

			if (_accel_calibration.enabled() && (hrt_elapsed_time(&_last_clipping_notify_time) > 3_s)) {
				// start notifying the user periodically if there's significant continuous clipping
				const uint64_t clipping_total = _status.accel_clipping[0] + _status.accel_clipping[1] + _status.accel_clipping[2];

				if (clipping_total > _last_clipping_notify_total_count + 1000) {
					mavlink_log_critical(&_mavlink_log_pub, "Accel %" PRIu8 " clipping, not safe to fly!", _instance);
					_last_clipping_notify_time = accel.timestamp_sample;
					_last_clipping_notify_total_count = clipping_total;
				}
			}
		}
	}
	
	return updated;
}

//int timer = 0;

bool VehicleIMU::UpdateGyro()
{
	bool updated = false;
	timer = timer+1;

	// integrate queued gyro
	sensor_gyro_s gyro;
	DetectedFault = 0;
	//sensor_gyro_s gyro_base = gyro;

	if (_sensor_gyro_sub.update(&gyro)) {
		l_GyroX = gyro.x;
		l_GyroY = gyro.y;
		l_GyroZ = gyro.z;
		if (_sensor_gyro_sub.get_last_generation() != _gyro_last_generation + 1) {
			_data_gap = true;
			perf_count(_gyro_generation_gap_perf);

			// reset average sample measurement
			_gyro_interval_mean.reset();

		} else {
			// collect sample interval average for filters
			if (_gyro_timestamp_sample_last != 0) {
				float interval_us = gyro.timestamp_sample - _gyro_timestamp_sample_last;
				_gyro_interval_mean.update(Vector2f{interval_us, interval_us / gyro.samples});
			}

			if (_gyro_interval_mean.valid() && (_gyro_interval_mean.count() > 100 || !PX4_ISFINITE(_gyro_interval_best_variance))
			    && ((_gyro_interval_mean.variance()(0) < _gyro_interval_best_variance) || (_gyro_interval_mean.count() > 1000))) {
				// update sample rate if previously invalid or changed
				const float interval_delta_us = fabsf(_gyro_interval_mean.mean()(0) - _gyro_interval_us);
				const float percent_changed = interval_delta_us / _gyro_interval_us;

				if (!PX4_ISFINITE(_gyro_interval_us) || (percent_changed > 0.001f)) {
					// update integrator configuration if interval has changed by more than 10%
					if (interval_delta_us > 0.1f * _gyro_interval_us) {
						_update_integrator_config = true;
					}

					_gyro_interval_us = _gyro_interval_mean.mean()(0);
					_gyro_interval_best_variance = _gyro_interval_mean.variance()(0);

					_status.gyro_rate_hz = 1e6f / _gyro_interval_mean.mean()(0);
					_status.gyro_raw_rate_hz = 1e6f / _gyro_interval_mean.mean()(1); // FIFO
					_publish_status = true;
				}

				if (_gyro_interval_mean.count() > 10000) {
					// reset periodically to prevent numerical issues
					_gyro_interval_mean.reset();
				}
			}
		}

		_gyro_last_generation = _sensor_gyro_sub.get_last_generation();
		_gyro_timestamp_last = gyro.timestamp;

		_gyro_calibration.set_device_id(gyro.device_id);

		if (gyro.error_count != _status.gyro_error_count) {
			_publish_status = true;
			_status.gyro_error_count = gyro.error_count;
		}
		
		
		
		
		if(FaultType_IMU.find("Software") != std::string::npos){
				if (FaultTarget_IMU.find("gyro") != std::string::npos){
				
				PX4_INFO("---------------------------------------------------- IMU fault is there");
					
					if (((!std::isnan(InjectionTimeStart_IMU) && !std::isnan(InjectionTimeEnd_IMU) && (gyro.timestamp >= InjectionTimeStart_IMU) && (gyro.timestamp <= InjectionTimeEnd_IMU)) && InjectionType_IMU.find("Time") != std::string::npos)){
					injectingFault_IMU = true;
					
					if(FaultSubType_IMU.find("FixedValue") != std::string::npos){

							PX4_INFO("----------------------------------------------------injecting IMU fixed value");
							
							gyro.x = DoubleValues_IMU[0];
							gyro.y = DoubleValues_IMU[1];
							gyro.z = DoubleValues_IMU[2];


						}else if(FaultSubType_IMU.find("FreezeValue") != std::string::npos){
						if (!Frozenvalue){
								DoubleValues_IMU[0] = gyro.x;
								DoubleValues_IMU[1] = gyro.y;
								DoubleValues_IMU[2] = gyro.z;
								Frozenvalue = true;
							}
							
							gyro.x = DoubleValues_IMU[0];
							gyro.y = DoubleValues_IMU[1];
							gyro.z = DoubleValues_IMU[2];
						}else if(FaultSubType_IMU.find("RandomValue") != std::string::npos){
						gyro.x = RandomFloat(-99999,99999);
						gyro.y = RandomFloat(-99999,99999);
						gyro.z = RandomFloat(-99999,99999);
						}else if(FaultSubType_IMU.find("MinValue") != std::string::npos){
						gyro.x = -99999;
						gyro.y = -99999;
						gyro.z = -99999;
						}else if(FaultSubType_IMU.find("MaxValue") != std::string::npos){
						gyro.x = 99999;
						gyro.y = 99999;
						gyro.z = 99999;
						}else if(FaultSubType_IMU.find("RandomNoise") != std::string::npos){
						gyro.x = gyro.x + (float)(-0.00999 + ((rand() % (9-1))*0.01));
						gyro.y = gyro.y + (float)(-0.00999 + ((rand() % (9-1))*0.01));
						gyro.z = gyro.z + (float)(-0.00999 + ((rand() % (9-1))*0.01));
						}
						else{
						}
						
						
					    
					}
					}
					}
		
		r_gyro = gyro;

		const Vector3f gyro_raw{gyro.x, gyro.y, gyro.z};
		_gyro_sum += gyro_raw;
		_gyro_temperature += gyro.temperature;
		_gyro_sum_count++;

		const float dt = (gyro.timestamp_sample - _gyro_timestamp_sample_last) * 1e-6f;
		_gyro_timestamp_sample_last = gyro.timestamp_sample;

		_gyro_integrator.put(gyro_raw, dt);
		updated = true;
	}

	return updated;
}

bool VehicleIMU::Publish()
{
	bool updated = false;

	vehicle_imu_s imu;
	Vector3f delta_angle;
	Vector3f delta_velocity;

	if (_accel_integrator.reset(delta_velocity, imu.delta_velocity_dt)
	    && _gyro_integrator.reset(delta_angle, imu.delta_angle_dt)) {

		if (_accel_calibration.enabled() && _gyro_calibration.enabled()) {

			// delta angle: apply offsets, scale, and board rotation
			_gyro_calibration.SensorCorrectionsUpdate();
			const float gyro_dt_inv = 1.e6f / imu.delta_angle_dt;
			const Vector3f angular_velocity{_gyro_calibration.Correct(delta_angle * gyro_dt_inv)};
			UpdateGyroVibrationMetrics(angular_velocity);
			const Vector3f delta_angle_corrected{angular_velocity / gyro_dt_inv};

			// delta velocity: apply offsets, scale, and board rotation
			_accel_calibration.SensorCorrectionsUpdate();
			const float accel_dt_inv = 1.e6f / imu.delta_velocity_dt;
			const Vector3f acceleration{_accel_calibration.Correct(delta_velocity * accel_dt_inv)};
			UpdateAccelVibrationMetrics(acceleration);
			const Vector3f delta_velocity_corrected{acceleration / accel_dt_inv};

			// vehicle_imu_status
			//  publish before vehicle_imu so that error counts are available synchronously if needed
			if ((_accel_sum_count > 0) && (_gyro_sum_count > 0)
			    && (_publish_status || (hrt_elapsed_time(&_status.timestamp) >= 100_ms))) {

				_status.accel_device_id = _accel_calibration.device_id();
				_status.gyro_device_id = _gyro_calibration.device_id();

				// mean accel
				const Vector3f accel_mean{_accel_calibration.Correct(_accel_sum / _accel_sum_count)};
				accel_mean.copyTo(_status.mean_accel);
				_status.temperature_accel = _accel_temperature / _accel_sum_count;
				_accel_sum.zero();
				_accel_temperature = 0;
				_accel_sum_count = 0;

				// mean gyro
				const Vector3f gyro_mean{_gyro_calibration.Correct(_gyro_sum / _gyro_sum_count)};
				gyro_mean.copyTo(_status.mean_gyro);
				_status.temperature_gyro = _gyro_temperature / _gyro_sum_count;
				_gyro_sum.zero();
				_gyro_temperature = 0;
				_gyro_sum_count = 0;

				_status.timestamp = hrt_absolute_time();
				_vehicle_imu_status_pub.publish(_status);

				_publish_status = false;
			}

			// publish vehicle_imu
			imu.timestamp_sample = _gyro_timestamp_sample_last;
			imu.accel_device_id = _accel_calibration.device_id();
			imu.gyro_device_id = _gyro_calibration.device_id();
			delta_angle_corrected.copyTo(imu.delta_angle);
			delta_velocity_corrected.copyTo(imu.delta_velocity);
			imu.delta_velocity_clipping = _delta_velocity_clipping;
			imu.calibration_count = _accel_calibration.calibration_count() + _gyro_calibration.calibration_count();
			imu.timestamp = hrt_absolute_time();
			
			
			//Fault injection here
			//if(timer < 60000){
			//_vehicle_imu_pub.publish(imu);
			//}
			imu_raw = imu;
			imu_raw.calibration_count = DetectedFault;
			imu_raw.delta_angle[0] = r_gyro.x;
			imu_raw.delta_angle[1] = r_gyro.y;
			imu_raw.delta_angle[2] = r_gyro.z;
			imu_raw.delta_velocity[0] = r_accel.x;
			imu_raw.delta_velocity[1] = r_accel.y;
			imu_raw.delta_velocity[2] = r_accel.z;
			
			
			
			_vehicle_imu_pub.publish(imu);
			_vehicle_imu_raw_pub.publish(imu_raw);
			//_vehicle_imu_phy_pub.publish(imu_phy);
			//_vehicle_imu_ml_pub.publish(imu_ml);

			// reset clip counts
			_delta_velocity_clipping = 0;

			updated = true;
		}
	}
	
	return updated;
}

void VehicleIMU::UpdateIntegratorConfiguration()
{
	if (PX4_ISFINITE(_accel_interval_us) && PX4_ISFINITE(_gyro_interval_us)) {

		// determine number of sensor samples that will get closest to the desired integration interval
		uint8_t gyro_integral_samples = math::max(1, (int)roundf(_imu_integration_interval_us / _gyro_interval_us));

		// if gyro samples exceeds queue depth, instead round to nearest even integer to improve scheduling options
		if (gyro_integral_samples > sensor_gyro_s::ORB_QUEUE_LENGTH) {
			gyro_integral_samples = math::max(1, (int)roundf(_imu_integration_interval_us / _gyro_interval_us / 2) * 2);
		}

		uint32_t integration_interval_us = roundf(gyro_integral_samples * _gyro_interval_us);

		// accel follows gyro as closely as possible
		uint8_t accel_integral_samples = math::max(1, (int)roundf(integration_interval_us / _accel_interval_us));

		// let the gyro set the configuration and scheduling
		// relaxed minimum integration time required
		_accel_integrator.set_reset_interval(roundf((accel_integral_samples - 0.5f) * _accel_interval_us));
		_accel_integrator.set_reset_samples(accel_integral_samples);

		_gyro_integrator.set_reset_interval(roundf((gyro_integral_samples - 0.5f) * _gyro_interval_us));
		_gyro_integrator.set_reset_samples(gyro_integral_samples);

		_backup_schedule_timeout_us = math::min(sensor_accel_s::ORB_QUEUE_LENGTH * _accel_interval_us,
							sensor_gyro_s::ORB_QUEUE_LENGTH * _gyro_interval_us);

		// gyro: find largest integer multiple of gyro_integral_samples
		for (int n = sensor_gyro_s::ORB_QUEUE_LENGTH; n > 0; n--) {
			if (gyro_integral_samples > sensor_gyro_s::ORB_QUEUE_LENGTH) {
				gyro_integral_samples /= 2;
			}

			if (gyro_integral_samples % n == 0) {
				_sensor_gyro_sub.set_required_updates(n);

				_intervals_configured = true;
				_update_integrator_config = false;

				PX4_DEBUG("accel (%" PRIu32 "), gyro (%" PRIu32 "), accel samples: %" PRIu8 ", gyro samples: %" PRIu8
					  ", accel interval: %.1f, gyro interval: %.1f sub samples: %d",
					  _accel_calibration.device_id(), _gyro_calibration.device_id(), accel_integral_samples, gyro_integral_samples,
					  (double)_accel_interval_us, (double)_gyro_interval_us, n);

				break;
			}
		}
	}
}

void VehicleIMU::UpdateAccelVibrationMetrics(const Vector3f &acceleration)
{
	// Accel high frequency vibe = filtered length of (acceleration - acceleration_prev)
	_status.accel_vibration_metric = 0.99f * _status.accel_vibration_metric
					 + 0.01f * Vector3f(acceleration - _acceleration_prev).norm();

	_acceleration_prev = acceleration;
}

void VehicleIMU::UpdateGyroVibrationMetrics(const Vector3f &angular_velocity)
{
	// Gyro high frequency vibe = filtered length of (angular_velocity - angular_velocity_prev)
	_status.gyro_vibration_metric = 0.99f * _status.gyro_vibration_metric
					+ 0.01f * Vector3f(angular_velocity - _angular_velocity_prev).norm();

	// Gyro delta angle coning metric = filtered length of (angular_velocity x angular_velocity_prev)
	const Vector3f coning_metric{angular_velocity % _angular_velocity_prev};
	_status.gyro_coning_vibration = 0.99f * _status.gyro_coning_vibration + 0.01f * coning_metric.norm();

	_angular_velocity_prev = angular_velocity;
}

void VehicleIMU::PrintStatus()
{
	PX4_INFO("%" PRIu8 " - Accel ID: %" PRIu32 ", interval: %.1f us (SD %.1f us), Gyro ID: %" PRIu32
		 ", interval: %.1f us (SD %.1f us)",
		 _instance, _accel_calibration.device_id(), (double)_accel_interval_us, (double)sqrtf(_accel_interval_best_variance),
		 _gyro_calibration.device_id(), (double)_gyro_interval_us, (double)sqrtf(_gyro_interval_best_variance));

	PX4_DEBUG("gyro update mean sample latency: %.6f s, publish latency %.6f s, gyro interval %.6f s",
		  (double)_gyro_update_latency_mean.mean()(0), (double)_gyro_update_latency_mean.mean()(1),
		  (double)(_gyro_interval_us * 1e-6f));

	perf_print_counter(_accel_generation_gap_perf);
	perf_print_counter(_gyro_generation_gap_perf);

	_accel_calibration.PrintStatus();
	_gyro_calibration.PrintStatus();
}

} // namespace sensors
