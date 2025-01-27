#include <iostream>
#include <vector>
#include <string>
#include <sstream>
#include <cstdlib>
#include <chrono>

int main(int argc, char* argv[]) {
    if (argc != 2) {
        std::cerr << "Usage: " << argv[0] << " <json_input>" << std::endl;
        return 1;
    }

    // Extract JSON input from command line arguments
    std::string json_input = argv[1];

    // Print received JSON input
    //std::cout << "Received JSON input: " << json_input << std::endl;

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


    return 0;
}

