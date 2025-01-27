#include <iostream>
#include <vector>
#include <string>
#include <cstdlib>
#include <sstream>
#include <chrono>

int main() {
    // Start the timer
    auto start_time = std::chrono::high_resolution_clock::now();

    // Example input data
    std::vector<std::vector<double>> input_data = {{1, 2, 3, 4, 5, 6}, {7, 8, 9, 10, 11, 12}};

    // Convert input data to JSON format
    std::ostringstream oss;
    oss << "{\"data\": [";
    for (int i = 0; i < input_data.size(); ++i) {
        oss << "[";
        for (int j = 0; j < input_data[i].size(); ++j) {
            oss << input_data[i][j];
            if (j < input_data[i].size() - 1) {
                oss << ", ";
            }
        }
        oss << "]";
        if (i < input_data.size() - 1) {
            oss << ", ";
        }
    }
    oss << "], \"columns\": [\"GyroX\", \"GyroY\", \"GyroZ\", \"AccelerometerX\", \"AccelerometerY\", \"AccelerometerZ\"]}";
    std::string json_input = oss.str();

    // Execute Python script to predict next values
    std::string command = "python3 predict_next_values5.py '" + json_input + "' 2>/dev/null";
    FILE* pipe = popen(command.c_str(), "r");
    if (!pipe) {
        std::cerr << "Error: Unable to execute Python script." << std::endl;
        return 1;
    }

    // Read predicted values from Python script's standard output
    std::string predicted_values;
    char buffer[128];
    while (!feof(pipe)) {
        if (fgets(buffer, 128, pipe) != NULL)
            predicted_values += buffer;
    }
    pclose(pipe);

    // Print predicted values received from Python
    //std::cout << "Response received from Python: " << predicted_values << std::endl;
    
    
    // Breaking the output into array

    // Find the position of the opening bracket '[' and closing bracket ']'
    size_t start_pos = predicted_values.find("! [");
    size_t end_pos = predicted_values.find("] !");

    if (start_pos != std::string::npos && end_pos != std::string::npos) {
        // Extract the substring containing the numerical values
        std::string values_str = predicted_values.substr(start_pos + 3, end_pos - start_pos - 3);

        // Parse the numerical values from the substring
        std::vector<double> values;
        std::istringstream iss(values_str);
        double num;
        while (iss >> num) {
            values.push_back(num);
        }

        // Print the extracted values
        //std::cout << "Extracted values:" << std::endl;
        for (double value : values) {
            std::cout << value << " ";
        }
        std::cout << std::endl;
    } else {
        std::cerr << "Error: Unable to find predicted values in the string." << std::endl;
    }
    // Calculate and print the execution time
    //auto end_time = std::chrono::high_resolution_clock::now();
    //std::chrono::duration<double> elapsed_seconds = end_time - start_time;
    //std::cout << "Execution time: " << elapsed_seconds.count() << " seconds" << std::endl;

    return 0;
}

