import csv
import os
import argparse
import glob

# Initialize dictionaries and lists for UAVs and faults
uavs = {}
faults = []

parser = argparse.ArgumentParser()
parser.add_argument("-fn", "--fn", action='store', help="FileName")
args = parser.parse_args()

# Path to save the .fc files
output_folder = "./"
os.makedirs(output_folder, exist_ok=True)


# Find all .fc files in the folder
fc_files = glob.glob(os.path.join(output_folder, '*.fc'))

# Delete each .fc file found
for file_path in fc_files:
    try:
        os.remove(file_path)
        print(f"Deleted: {file_path}")
    except Exception as e:
        print(f"Error deleting {file_path}: {e}")


# Open the t1.csv file and read dynamically
with open(args.fn, mode='r') as file:
    reader = csv.reader(file, delimiter=',', quotechar='"')
    for row in reader:
        print("Row:", row)  # Debugging: Print each row

        if row[0] == "Number of UAV Models":
            num_uavs = int(row[1])
        
        # Identify UAV and path information
        elif row[0] in ['a', 'b']:  # Assume UAV names are 'a' and 'b'
            uav_name = row[0]
            uav_path = row[3]
            uavs[uav_name] = uav_path

        # Identify faults and dynamically locate `Affected UAVs` column
        elif row[0] == "Faulty Run":
            try:
                # Locate the index for 'Affected UAVs' by searching for keywords
                affected_uavs_idx = next(i for i, val in enumerate(row) if '[' in val and ']' in val)
                print("Identified Affected UAVs Column:", row[affected_uavs_idx])  # Debugging print

                fault_info = {
                    "FaultMode": row[1],
                    "FaultSubType": row[2],
                    "InjectionTimeStart": row[3],
                    "InjectionTimeEnd": row[4],
                    "Values": row[6:13],  # Collect values from index 6 to 12
                    "Affected UAVs": [uav.strip() for uav in row[affected_uavs_idx].strip("[]").split(";") if uav.strip()]
                }
                faults.append(fault_info)
                
            except StopIteration:
                print("Error: Could not locate 'Affected UAVs' column.")

# Loop through each fault and create .fc files
for fault in faults:
    for uav in fault["Affected UAVs"]:
        if uav in uavs:
            # Set up filename and mission path
            filename = f"{fault['FaultMode']}_{fault['FaultSubType']}_{uav}.fc"
            filename = filename.replace(" ", "_")
            filepath = os.path.join(output_folder, filename)
            mission_path = uavs[uav]  # UAV-specific mission path
            
            # Write to .fc file
            with open(filepath, 'w') as fc_file:
                fc_file.write(f"DroneIP = 127.0.0.1:14540\n")
                fc_file.write(f"MissionPath = {mission_path}\n")
                fc_file.write(f"InjectionType = Time\n")
                fc_file.write(f"InjectionTimeStart = {fault['InjectionTimeStart']}\n")
                fc_file.write(f"InjectionTimeEnd = {fault['InjectionTimeEnd']}\n")
                fc_file.write(f"InjectionLocation = [0,1123344]\n")
                fc_file.write(f"InjectionRadius = 5\n")
                fc_file.write(f"FaultType = Software\n")
                fc_file.write(f"FaultTarget = {fault['FaultMode']}\n")
                FST = fault['FaultSubType'].replace(" ", "")
                fc_file.write(f"FaultSubType = {FST}\n")
                fc_file.write(f"UseML = 0\n")  # Based on "Faulty Run"
                values_str = ', '.join(fault["Values"])
                fc_file.write(f"Values = [{values_str}]\n")
            
            print(f"Created {filename} with path {filepath}")

print("All .fc files generated successfully.")

