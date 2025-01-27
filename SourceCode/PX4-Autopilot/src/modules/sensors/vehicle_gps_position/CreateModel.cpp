#include <iostream>
#include "cPMML.h"

//#include <chrono>
//using namespace std::chrono;
using namespace std;

int main(int argc, char** argv)
{
	//auto start = high_resolution_clock::now();
	
	cpmml::Model model("Model1AltA.xml");
	unordered_map<string, string> sample = {
	{"Lat",argv[1]},
	{"Lon",argv[2]},
	{"Alt",argv[3]},
	{"Velocity",argv[4]},
	{"Direction",argv[5]},
	{"Noise",argv[6]},
	{"GyroX",argv[7]},
	{"GyroY",argv[8]},
	{"GyroZ",argv[9]},
	{"AccelerometerX",argv[10]},
	{"AccelerometerY",argv[11]},
	{"AccelerometerZ",argv[12]},
	//{"WPNextLat",argv[13]},
	//{"WPNextLon",argv[14]},
	//{"WPNextAlt",argv[15]},
	{"AirDensity",argv[16]}
	};

	cout << model.predict(sample) << flush;
	
	//printf("%s", model.predict(sample).c_str());
	//auto stop = high_resolution_clock::now();
	//auto duration = duration_cast<microseconds>(stop - start);
	//cout << "Time taken by function: " << duration.count() << " microseconds" << endl;
	
	return 0;
}

