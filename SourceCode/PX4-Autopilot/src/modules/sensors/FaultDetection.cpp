// FaultDetection.cpp
#include <px4_platform_common/log.h>
#include <px4_platform_common/defines.h>
#include <string> 
#include <iostream>
#include <sstream>
#include <unistd.h>
#include <poll.h>
#include <fstream>
#include <cstdlib>
#include <uORB/topics/vehicle_gps_position.h>
#include <uORB/topics/sensor_combined.h>
#include <iomanip>  // For std::fixed
#include <thread>
#include <fcntl.h> // For open
#include <cstring> // For strerror
#include <iomanip>
#include <cmath>


static std::string FaultDetected = "NF";  // The shared Falt detection variable

// Acceleration data (Time steps as rows, axes as columns)
static double Acc[4][3] = {  // 4 time steps (current, p1, p2, p3) and 3 axes (X, Y, Z)
    {0.0, 0.0, 0.0},  // Current values: AccX, AccY, AccZ
    {0.0, 0.0, 0.0},  // p1 values: AccX_p1, AccY_p1, AccZ_p1
    {0.0, 0.0, 0.0},  // p2 values: AccX_p2, AccY_p2, AccZ_p2
    {0.0, 0.0, 0.0}   // p3 values: AccX_p3, AccY_p3, AccZ_p3
};


// Gyroscope data (Time steps as rows, axes as columns)
static double Gyro[4][3] = {  // 4 time steps (current, p1, p2, p3) and 3 axes (X, Y, Z)
    {0.0, 0.0, 0.0},  // Current values: GyroX, GyroY, GyroZ
    {0.0, 0.0, 0.0},  // p1 values: GyroX_p1, GyroY_p1, GyroZ_p1
    {0.0, 0.0, 0.0},  // p2 values: GyroX_p2, GyroY_p2, GyroZ_p2
    {0.0, 0.0, 0.0}   // p3 values: GyroX_p3, GyroY_p3, GyroZ_p3
};


// GPS data (Time steps as rows, data points as columns)
static double GPS[4][6] = {  // 4 time steps (current, p1, p2, p3) and 6 data points (Lat, Lon, Alt, Velocity, Dir, Noise)
    {0.0, 0.0, 0.0, 0.0, 0.0, 0.0},  // Current values: Lat, Lon, Alt, Velocity, Direction, Noise
    {0.0, 0.0, 0.0, 0.0, 0.0, 0.0},  // p1 values: Lat_p1, Lon_p1, Alt_p1, Velocity_p1, Direction_p1, Noise_p1
    {0.0, 0.0, 0.0, 0.0, 0.0, 0.0},  // p2 values: Lat_p2, Lon_p2, Alt_p2, Velocity_p2, Direction_p2, Noise_p2
    {0.0, 0.0, 0.0, 0.0, 0.0, 0.0}   // p3 values: Lat_p3, Lon_p3, Alt_p3, Velocity_p3, Direction_p3, Noise_p3
};

//static bool FirstCheck = false;


std::string format_sensor_data(double GPS_data[4][6], double Gyro_data[4][3], double Acc_data[4][3]) {
    // Use ostringstream to accumulate the data into a string
    std::ostringstream oss;

    // Add current GPS data (Lat, Lon, Alt, Velocity, Dir, Noise)
    oss << GPS_data[0][0] << " " << GPS_data[0][1] << " " << GPS_data[0][2] << " "
        << GPS_data[0][3] << " " << GPS_data[0][4] << " " << GPS_data[0][5] << " ";

    // Add current Gyro data (GyroX, GyroY, GyroZ)
    oss << Gyro_data[0][0] << " " << Gyro_data[0][1] << " " << Gyro_data[0][2] << " ";

    // Add current Acc data (AccX, AccY, AccZ)
    oss << Acc_data[0][0] << " " << Acc_data[0][1] << " " << Acc_data[0][2] << " ";

    // Add previous GPS data (p1, p2, p3)
    for (int i = 1; i <= 3; ++i) {
        oss << GPS_data[i][0] << " " << GPS_data[i][1] << " " << GPS_data[i][2] << " "
            << GPS_data[i][3] << " " << GPS_data[i][4] << " " << GPS_data[i][5] << " ";
    }

    // Add previous Gyro data (p1, p2, p3)
    for (int i = 1; i <= 3; ++i) {
        oss << Gyro_data[i][0] << " " << Gyro_data[i][1] << " " << Gyro_data[i][2] << " ";
    }

    // Add previous Acc data (p1, p2, p3)
    for (int i = 1; i <= 3; ++i) {
        oss << Acc_data[i][0] << " " << Acc_data[i][1] << " " << Acc_data[i][2] << " ";
    }

    // Convert ostringstream to string and return
    return oss.str();
}

std::string format_sensor_data2(double GPS_data[4][6], double Gyro_data[4][3], double Acc_data[4][3]) {
    // Use ostringstream to accumulate the data into a string
    std::ostringstream oss;
    
    // Set the format to fixed point and prevent scientific notation
    oss << std::fixed;

    // Add current GPS data (Lat, Lon, Alt, Velocity, Dir, Noise)
    oss << " " << GPS_data[0][0] << " " << GPS_data[0][1] << " " << GPS_data[0][2] << " "
        << GPS_data[0][3] << " " << GPS_data[0][4] << " " << GPS_data[0][5] << " ";

    // Add current Gyro data (GyroX, GyroY, GyroZ)
    oss << " " << Gyro_data[0][0] << " " << Gyro_data[0][1] << " " << Gyro_data[0][2] << " ";

    // Add current Acc data (AccX, AccY, AccZ)
    oss << " " << Acc_data[0][0] << " " << Acc_data[0][1] << " " << Acc_data[0][2] << " ";

    // Add previous GPS data (p1, p2, p3)
    for (int i = 0; i < 3; ++i) {
        oss << " " << GPS_data[i][0] << " " << GPS_data[i][1] << " " << GPS_data[i][2] << " "
            << GPS_data[i][3] << " " << GPS_data[i][4] << " " << GPS_data[i][5] << " ";
    }

    // Add previous Gyro data (p1, p2, p3)
    for (int i = 0; i < 3; ++i) {
        oss << " " << Gyro_data[i][0] << " " << Gyro_data[i][1] << " " << Gyro_data[i][2] << " ";
    }

    // Add previous Acc data (p1, p2, p3)
    for (int i = 0; i < 3; ++i) {
        oss << " " << Acc_data[i][0] << " " << Acc_data[i][1] << " " << Acc_data[i][2] << " ";
    }

    // Convert ostringstream to string and return
    return oss.str();
}

// Helper function to format a single double value safely
std::string format_double(double value) {
    if (std::isnan(value) || std::isinf(value)) {
        value = 0.0; // Replace invalid values with a default
    }
    std::ostringstream stream;
    stream << std::fixed << std::setprecision(6) << value; // Ensure consistent formatting
    return stream.str();
}


// Updated function
std::string format_sensor_data3(double GPS_data[4][6], double Gyro_data[4][3], double Acc_data[4][3]) {
    // Use ostringstream to accumulate the data into a string
    std::ostringstream oss;
    
    // Add current GPS data (Lat, Lon, Alt, Velocity, Dir, Noise)
    for (int i = 0; i < 6; ++i) {
        oss << " " << format_double(GPS_data[0][i]);
    }

    // Add current Gyro data (GyroX, GyroY, GyroZ)
    for (int i = 0; i < 3; ++i) {
        oss << " " << format_double(Gyro_data[0][i]);
    }

    // Add current Acc data (AccX, AccY, AccZ)
    for (int i = 0; i < 3; ++i) {
        oss << " " << format_double(Acc_data[0][i]);
    }

    // Add previous GPS data (p1, p2, p3)
    for (int j = 1; j <= 3; ++j) {
        for (int i = 0; i < 6; ++i) {
            oss << " " << format_double(GPS_data[j][i]);
        }
    }

    // Add previous Gyro data (p1, p2, p3)
    for (int j = 1; j <= 3; ++j) {
        for (int i = 0; i < 3; ++i) {
            oss << " " << format_double(Gyro_data[j][i]);
        }
    }

    // Add previous Acc data (p1, p2, p3)
    for (int j = 1; j <= 3; ++j) {
        for (int i = 0; i < 3; ++i) {
            oss << " " << format_double(Acc_data[j][i]);
        }
    }

    // Convert ostringstream to string and return
    return oss.str();
}

std::string format_sensor_data4(double GPS_data[4][6], double Gyro_data[4][3], double Acc_data[4][3]) {
    // Use ostringstream to accumulate the data into a string
    std::ostringstream oss;

    // Add current GPS data (Lat, Lon, Alt, Velocity, Direction, Noise)
    for (int i = 0; i < 6; ++i) {
        oss << format_double(GPS_data[0][i]) << " ";
    }

    // Add current Gyro data (GyroX, GyroY, GyroZ)
    for (int i = 0; i < 3; ++i) {
        oss << format_double(Gyro_data[0][i]) << " ";
    }

    // Add current Acc data (AccX, AccY, AccZ)
    for (int i = 0; i < 3; ++i) {
        oss << format_double(Acc_data[0][i]) << " ";
    }

    // Add previous GPS data for each feature (Lat, Lon, Alt, Velocity, Direction, Noise)
    for (int i = 0; i < 6; ++i) {
        for (int j = 1; j <= 3; ++j) {
            oss << format_double(GPS_data[j][i]) << " ";
        }
    }

    // Add previous Gyro data for each feature (GyroX, GyroY, GyroZ)
    for (int i = 0; i < 3; ++i) {
        for (int j = 1; j <= 3; ++j) {
            oss << format_double(Gyro_data[j][i]) << " ";
        }
    }

    // Add previous Acc data for each feature (AccX, AccY, AccZ)
    for (int i = 0; i < 3; ++i) {
        for (int j = 1; j <= 3; ++j) {
            oss << format_double(Acc_data[j][i]) << " ";
        }
    }

    return oss.str();
}



void CheckFault(const char* cmd) {
    char buffer[128];
    std::string result = "";
    FILE* pipe = popen(cmd, "r");
    //if (!pipe) throw std::runtime_error("popen() failed!");
        while (fgets(buffer, sizeof buffer, pipe) != NULL) {
            result += buffer;
        }
    pclose(pipe);
    FaultDetected = result;
    PX4_INFO("Fault Detected : %s", FaultDetected.c_str());
}

//void startServer(const char* cmd) {
    //char buffer[128];
    //std::string result = "";
//    FILE* pipe = popen(cmd, "r");
//    if (!pipe) {
    //throw std::runtime_error("popen() failed!");
//    pclose(pipe);
//    }
        //while (fgets(buffer, sizeof buffer, pipe) != NULL) {
        //    result += buffer;
        //}
    //pclose(pipe);
    //return result;
//}


void SaveValuesFile(){
 // Save all historical and current values to values.txt
    int fd_values = open("/tmp/values.txt", O_WRONLY | O_CREAT | O_APPEND, 0666);
    if (fd_values >= 0) {
        std::string values_str;

        // Save GPS values
        values_str += "GPS:\n";
        for (int t = 3; t >= 0; --t) { // Iterate through historical and current values
            values_str += "  T-" + std::to_string(3 - t) + ": ";
            for (int i = 0; i < 6; ++i) {
                values_str += std::to_string(GPS[t][i]) + " ";
            }
            values_str += "\n";
        }

        // Save Gyro values
        values_str += "Gyro:\n";
        for (int t = 3; t >= 0; --t) { // Iterate through historical and current values
            values_str += "  T-" + std::to_string(3 - t) + ": ";
            for (int i = 0; i < 3; ++i) {
                values_str += std::to_string(Gyro[t][i]) + " ";
            }
            values_str += "\n";
        }

        // Save ACC values
        values_str += "ACC:\n";
        for (int t = 3; t >= 0; --t) { // Iterate through historical and current values
            values_str += "  T-" + std::to_string(3 - t) + ": ";
            for (int i = 0; i < 3; ++i) {
                values_str += std::to_string(Acc[t][i]) + " ";
            }
            values_str += "\n";
        }

        // Write to file
        ssize_t bytes_written = write(fd_values, values_str.c_str(), values_str.size());
        if (bytes_written < 0) {
            PX4_ERR("Failed to write sensor values to file: %s", strerror(errno));
        } else if (static_cast<size_t>(bytes_written) != values_str.size()) {
            PX4_ERR("Partial write of sensor values to file");
        }
        close(fd_values); // Close file descriptor
    } else {
        PX4_ERR("Failed to open values file for writing: %s", strerror(errno));
    }
}
    
    

// Function to update shared variable during sensor execution
void CheckForFault() {
    // Check for fault
    
    struct sensor_combined_s sensors_data_IMU2;
    int sensros_sub_IMU2 = orb_subscribe(ORB_ID(sensor_combined));
    orb_set_interval(sensros_sub_IMU2, 20);
    orb_copy(ORB_ID(sensor_combined), sensros_sub_IMU2, &sensors_data_IMU2);
    // Shift Acc values
    for (int i = 0; i < 3; ++i) {
    Acc[3][i] = Acc[2][i];  // p3 = p2
    Acc[2][i] = Acc[1][i];  // p2 = p1
    Acc[1][i] = Acc[0][i];  // p1 = current
    }
    Acc[0][0] = sensors_data_IMU2.accelerometer_m_s2[0];
    Acc[0][1] = sensors_data_IMU2.accelerometer_m_s2[1];
    Acc[0][2] = sensors_data_IMU2.accelerometer_m_s2[2];
    // Shift Gyro values
    for (int i = 0; i < 3; ++i) {
    Gyro[3][i] = Gyro[2][i];  // p3 = p2
    Gyro[2][i] = Gyro[1][i];  // p2 = p1
    Gyro[1][i] = Gyro[0][i];  // p1 = current
    }
    Gyro[0][0] = sensors_data_IMU2.gyro_rad[0];
    Gyro[0][1] = sensors_data_IMU2.gyro_rad[1];
    Gyro[0][2] = sensors_data_IMU2.gyro_rad[2];
    
    
    struct vehicle_gps_position_s gps_data;
    int gps_sub = orb_subscribe(ORB_ID(vehicle_gps_position));
    orb_set_interval(gps_sub, 20);
    orb_copy(ORB_ID(vehicle_gps_position), gps_sub, &gps_data);
    // Shift GPS values
    for (int i = 0; i < 6; ++i) {
    GPS[3][i] = GPS[2][i];  // p3 = p2
    GPS[2][i] = GPS[1][i];  // p2 = p1
    GPS[1][i] = GPS[0][i];  // p1 = current
    }
    GPS[0][0] = gps_data.lat;
    GPS[0][1] = gps_data.lon;
    GPS[0][2] = gps_data.alt;
    GPS[0][3] = gps_data.vel_m_s;
    GPS[0][4] = gps_data.cog_rad;
    GPS[0][5] = gps_data.noise_per_ms;
    
    SaveValuesFile();
    
    
    //Check for fault
    std::string command = std::string("cd /home/anamta/Anomaly_ML && python3.8 CheckAnomaly.py --input ") 
                      + format_sensor_data4(GPS, Gyro, Acc) + std::string(" 2>/dev/null");
    //FaultDetected = CheckFault(command.c_str());
    
    //PX4_INFO("Command: %s", command.c_str());
    CheckFault(command.c_str());
    
    // Append the command to a text file efficiently
    int fd = open("/tmp/command.txt", O_WRONLY | O_CREAT | O_APPEND, 0666); // Open or create file in append mode
    if (fd >= 0) {
        // Write the command string
        ssize_t bytes_written = write(fd, command.c_str(), command.size());
        if (bytes_written < 0) {
            PX4_ERR("Failed to write command to file: %s", strerror(errno));
        } else if (static_cast<size_t>(bytes_written) != command.size()) {
            PX4_ERR("Partial write of command to file");
        }

        // Write a newline
        ssize_t newline_written = write(fd, "\n", 1);
        if (newline_written < 0) {
            PX4_ERR("Failed to write newline to file: %s", strerror(errno));
        } else if (newline_written != 1) {
            PX4_ERR("Partial write of newline to file");
        }

        close(fd); // Close the file descriptor
    } else {
        PX4_ERR("Failed to open file for writing: %s", strerror(errno)); // Log error
    }

    PX4_INFO("Command written to file: %s", command.c_str());
    
}


























