// FaultDetection.h

#ifndef FAULT_DETECTION_H
#define FAULT_DETECTION_H

#include <string>

// Declare the shared fault detection variable
static std::string FaultDetected;

// Declare sensor data arrays
//extern float Acc[4][3];  // Acceleration data: 4 time steps, 3 axes (X, Y, Z)
//extern float Gyro[4][3];  // Gyroscope data: 4 time steps, 3 axes (X, Y, Z)
//extern float GPS[4][6];  // GPS data: 4 time steps, 6 data points (Lat, Lon, Alt, Velocity, Dir, Noise)

// Declare the FirstCheck flag
//extern bool FirstCheck;

// Function to format sensor data into a string
//std::string format_sensor_data(float GPS_data[4][6], float Gyro_data[4][3], float Acc_data[4][3]);

// Function to check for fault by running a command and returning the result
//std::string CheckFault(const char* cmd);

// Function to start a server by running a command
//void startServer(const char* cmd);

// Function to check for fault and update the shared variable
void CheckForFault();

#endif // FAULT_DETECTION_H

