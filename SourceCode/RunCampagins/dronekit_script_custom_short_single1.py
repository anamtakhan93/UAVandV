#!/usr/bin/env python

# Import DroneKit-Python
from dronekit import connect,  VehicleMode, Command, LocationGlobal, LocationGlobalRelative
from pymavlink import mavutil
import time, sys, argparse, math, matplotlib, threading, requests
import matplotlib.pyplot as plt
import shutil, sys
import re
import socket
from datetime import datetime
import glob
import os
from pathlib import Path
import subprocess
import psutil
import signal
from os import system



################################################################################################
# Settings
################################################################################################

connection_string       = '127.0.0.1:14540' # default
MAV_MODE_AUTO   = 4
# https://github.com/PX4/PX4-Autopilot/blob/master/Tools/mavlink_px4.py

# Parse connection argument
parser = argparse.ArgumentParser()
parser.add_argument("-c", "--connect", help="connection string")
parser.add_argument("-m", "--mission", action='store', help="mission file path")
parser.add_argument("-f", "--faults", action='store', help="faults file path")
parser.add_argument("-cid", "--cid", action='store', help="Connection ID")
parser.add_argument("-fn", "--fn", action='store', help="Fault")
#parser.add_argument("-id", "--mavID", action='store', help="Mavlink ID")
parser.add_argument("-type", "--typerun", action='store', help="type of run, 0 for Golden, 1 for Faulty")
args = parser.parse_args()

if args.connect:
    connection_string = args.connect
    
#Plot Data
start_time = time.time()
times = []
distances_east = []
distances_north = []
distances_down = []
has_finished = False
takeoff_time = 0
speed = 5
missionID = "1"
fn = "1"
DronekitDir = "/home/[path]/RunCampagins/"
RestartScriptCommand = "python3 RunMissions.py"
MissionsDir = "/home/[path]/RunCampagins/Shorts/"
px4Dir = "/home/[path]/Documents/PX42/PX4-Autopilot/"
SimPath = "/home/[path]/RunCampagins/SimStart/Sim.txt"
px4Dir = "/home/[path]/Documents/PX42/PX4-Autopilot/"
saveToDir = "/home/[path]/Desktop/Logs/"#+args.fn.split('.')[0]
#mavID = 0



################################################################################################
# Init
################################################################################################
#if args.mavID:
#    mavID = int(args.mavID)
    #print_with_flush(mavID)
    
if args.fn:
    fn = args.fn
    print(fn)
    
def print_with_flush(msg):
    print(msg)
    sys.stdout.flush()


def PX4setMode(mavMode):
    vehicle._master.mav.command_long_send(vehicle._master.target_system, vehicle._master.target_component,
                                               mavutil.mavlink.MAV_CMD_DO_SET_MODE, 0,
                                               mavMode,
                                               0, 0, 0, 0, 0, 0)

def update_values():
    t = threading.Timer(2.0, update_values)
    t.start()
    if(has_finished == True):
        t.cancel()
    if(vehicle.armed == True):
        times.append(time.time() - start_time)
        distances_east.append(vehicle.location.local_frame.east)
        distances_north.append(vehicle.location.local_frame.north)
        distances_down.append(vehicle.location.local_frame.down)
        #print "Updating values"

def get_location_offset_meters(original_location, dNorth, dEast, alt):
    """
    Returns a LocationGlobal object containing the latitude/longitude `dNorth` and `dEast` metres from the
    specified `original_location`. The returned Location adds the entered `alt` value to the altitude of the `original_location`.
    The function is useful when you want to move the vehicle around specifying locations relative to
    the current vehicle position.
    The algorithm is relatively accurate over small distances (10m within 1km) except close to the poles.
    For more information see:
    http://gis.stackexchange.com/questions/2951/algorithm-for-offsetting-a-latitude-longitude-by-some-amount-of-meters
    """
    earth_radius=6378137.0 #Radius of "spherical" earth
    #Coordinate offsets in radians
    dLat = dNorth/earth_radius
    dLon = dEast/(earth_radius*math.cos(math.pi*original_location.lat/180))

    #New position in decimal degrees
    newlat = original_location.lat + (dLat * 180/math.pi)
    newlon = original_location.lon + (dLon * 180/math.pi)
    return LocationGlobal(newlat, newlon,original_location.alt+alt)


def LineLength(aFileName) -> int:
    file = open(aFileName, "r")
    line_count = 0
    for line in file:
        if line != "\n":
            line_count += 1
    file.close()
    #print(line_count)
    return line_count


def readmission(aFileName):
    """
    Load a mission from a file into a list. 
    This function is used by upload_mission().
    """
    numOfWaypoints = 7;
    print_with_flush("\nReading mission from file: %s" % aFileName)
    cmds = vehicle.commands
    missionlist=[]
    home = vehicle.location.global_relative_frame
    missionread = False
    with open(aFileName) as f:
        cur_wp = 0
        num_lines = LineLength(aFileName)
        numOfWaypoints = num_lines;
        print_with_flush("num_lines is : " + str(num_lines))
        for i, line in enumerate(f):
            linearray=re.split('    |\n',line)
            #print_with_flush(linearray)
            if len(linearray) <= 2:
                if linearray[0]!='':
                    takeoff_time = int(linearray[0])
                    print_with_flush(linearray)
            
            elif len(linearray) <= 5:
                print_with_flush(linearray)
                if linearray[0]!='':
                    takeoff_time = int(linearray[0])
                if linearray[1]!='':
                    global missionID
                    missionID = linearray[1]
                    print_with_flush(missionID)
                if linearray[3]!='':
                    global speed
                    speed = int(linearray[3])
                    print_with_flush(speed)
                    print_with_flush(" Param MPC_XY_CRUISE before: %s" % vehicle.parameters['MPC_XY_CRUISE'])
                    vehicle.parameters['MPC_XY_CRUISE']=speed
                    print_with_flush(" Param MPC_XY_CRUISE after: %s" % vehicle.parameters['MPC_XY_CRUISE'])
                    
                    
            else:
                print_with_flush("i is : " + str(i))
                if i >= num_lines-numOfWaypoints:
                    print_with_flush("Adding line to the mission: " + str(i))
                    print_with_flush(linearray)
                    ln_target_system = int(linearray[0])
                    ln_target_component = int(linearray[1])
                    ln_seq = int(linearray[2])
                    ln_frame = int(linearray[3])
                    if ln_frame == 0:
                        ln_frame = mavutil.mavlink.MAV_FRAME_GLOBAL_RELATIVE_ALT
                        ln_current = int(linearray[5])
                        ln_autocontinue = int(linearray[6])
                        ln_param1 = float(linearray[7])
                        ln_param2 = float(linearray[8])
                        ln_param3 = float(linearray[9])
                        ln_param4 = float(linearray[10])
                        ln_paramx = float(linearray[11])
                        ln_paramy = float(linearray[12])
                        ln_paramz = max(float(linearray[13]),5)
                        ln_command = int(linearray[4])
                        print_with_flush(ln_command)
                        if i == num_lines-numOfWaypoints:
                            ln_command = 0
                            ln_paramz = 5
                        if ln_command == 0:
                            ln_command = mavutil.mavlink.MAV_CMD_NAV_TAKEOFF
                            vehicle.home_location = LocationGlobal(ln_paramx, ln_paramy, 5);
                            #home = vehicle.home_location
                            print_with_flush(" Home after: %s" % vehicle.home_location)
                            #print_with_flush(" Home value: %s" % home)
                            #print_with_flush(" Global Location before: %s" % vehicle.location.global_frame)
                            #vehicle.location.global_frame(vehicle.home_location)
                            #vehicle.location.global_frame.lat = vehicle.home_location.lat
                            #vehicle.location.global_frame.lon = vehicle.home_location.lon
                            #print_with_flush(" Global Location after: %s" % vehicle.location.global_frame)
                            wp = get_location_offset_meters(home, ln_paramx, ln_paramy, ln_paramz);
                            #print_with_flush(" Param SIH_LOC_LAT0 before: %s" % vehicle.parameters['SIH_LOC_LAT0'])
                            #vehicle.parameters['SIH_LOC_LAT0']=ln_paramx
                            #print_with_flush(" Param SIH_LOC_LAT0 after: %s" % vehicle.parameters['SIH_LOC_LAT0'])
                            print_with_flush(" wp: %s" % wp)
                            cur_wp = get_location_offset_meters(home, ln_paramx, ln_paramy, ln_paramz);
                        elif ln_command == 1:
                            ln_command = mavutil.mavlink.MAV_CMD_NAV_WAYPOINT
                            #print_with_flush(cur_wp)
                            wp = get_location_offset_meters(cur_wp, ln_paramx, ln_paramy, ln_paramz);
                            #print_with_flush(wp)
                            cur_wp = wp
                        elif ln_command == 2:
                            ln_command = mavutil.mavlink.MAV_CMD_NAV_LAND
                            wp = get_location_offset_meters(home, ln_paramx, ln_paramy, ln_paramz);
                            missionread = True
                
                    #cmd = Command(ln_target_system, ln_target_component, ln_seq, ln_frame, ln_command, ln_current, ln_autocontinue, ln_param1, ln_param2, ln_param3, ln_param4, wp.lat, wp.lon, wp.alt)
                    cmd = Command(ln_target_system, ln_target_component, ln_seq, ln_frame, ln_command, ln_current, ln_autocontinue, ln_param1, ln_param2, ln_param3, ln_param4, ln_paramx, ln_paramy, ln_paramz)
                    missionlist.append(cmd)
                    if missionread:
                        break
                
    return missionlist


def upload_mission(aFileName):
    """
    Upload a mission from a file. 
    """
    #Read mission from file
    missionlist = readmission(aFileName)
    print_with_flush("\nUpload mission from a file: %s" % aFileName)
    #Clear existing mission from vehicle
    print_with_flush(' Clear mission ')
    cmds = vehicle.commands
    cmds.clear()
    vehicle.flush()
    time.sleep(1)
    #Add new mission to vehicle
    for command in missionlist:
        cmds.add(command)
    print_with_flush(" Home Location Before upload: %s" % vehicle.home_location)
    print_with_flush(' Upload mission ')
    #vehicle.wait_ready(True,raifnse_exception=False)
    cmds.upload()
    print_with_flush(' Uploaded mission ')
    print_with_flush(" Home Location: %s" % vehicle.home_location)




def send_data():
    if args.cid:
        print_with_flush(" Location: %s" % vehicle.home_location)
        print_with_flush(" Global Location: %s" % vehicle.location.global_frame)
        print_with_flush(" Local Location: %s" % vehicle.location.local_frame)
        print_with_flush(" Altitude: %s" % vehicle.attitude)
        #print_with_flush(" Globafnl Altitude: %s" % vehicle.location.global_frame.alt)
        #print_with_flush(" Local Altitude: %s" % vehicle.location.local_frame.down)
        print_with_flush(" Velocity: %s" % vehicle.velocity)
        print_with_flush(" GPS raw: %s" % vehicle.gps_0)
        print_with_flush(" Batterylllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllll: %s" % vehicle.battery)
        #print_with_flush(" Time: %s" % vehicle.time)

        data = [str(conID), str(vehicle.home_location.alt), str(vehicle.home_location.lat), str(vehicle.home_location.lon), str(vehicle.attitude.yaw), str(vehicle.attitude.roll), str(vehicle.attitude.pitch), str(vehicle.velocity[0]), str(vehicle.velocity[1]), str(vehicle.velocity[2]), str(0), str(0), str(0), str(120),str(0)]
        message = ",".join(data)
        try:
            sock.send(bytes(message, 'utf-8'))
        except Exception as e: 
            print("something's wrong with in with sending data over TCP")
            sock.close()


# Function to read MissionPath from .fc file
def get_mission_path(fc_file):
    mission_path = None
    with open(fc_file, 'r') as file:
        for line in file:
            if line.startswith("MissionPath"):
                # Extract the path after '=' and strip any whitespace
                mission_path = line.split("=", 1)[1].strip()
                break
    print_with_flush("---------- Mission Path : "+mission_path)
    return mission_path

missionPath = get_mission_path(args.fn)

def SaveLog():
    """
    Save drone's log to a directory. 
    """
    instanceNum = connection_string[-1]
    dateTimeObj = datetime.now()
    logDir = "build/px4_sitl_default/instance_"+instanceNum+"/sitl_iris_"+instanceNum+"/log/"+dateTimeObj.strftime("%Y-%m-%d")+"/*.ulg"
    print_with_flush(px4Dir+logDir)
    list_of_files = glob.glob(px4Dir+logDir) # * means all if need specific format then *.csv
    latest_file = max(list_of_files, key=os.path.getctime)
    print_with_flush(latest_file)
    Path(saveToDir).mkdir(parents=True, exist_ok=True)
    #if not os.path.exists(saveToDir):
         #os.makedirs(saveToDir)
    #asFile = "Drone"+instanceNum+".ulg"
    print_with_flush(missionID)
    #asFile = missionID + ".ulg"
    asFile = ""+args.fn + ".ulg"
    
    #saveToDir = "/home/[path]/Logs/Shorts/"
    #asFile = args.fn.split('.')[0] + ".ulg"
    
    shutil.move(latest_file,saveToDir+asFile)
    print_with_flush("Log Saved in "+saveToDir+asFile)
  


def kill(proc_pid):
    process = psutil.Process(proc_pid)
    for proc in process.children(recursive=True):
        proc.kill()
    process.kill()




################################################################################################
# Connect to the vehicle
################################################################################################



print_with_flush("Connecting...%s" % connection_string)
vehicle = connect(connection_string, wait_ready=True, heartbeat_timeout=30)
    
time.sleep(2)
print_with_flush("Connection Completed for ...%s" % connection_string)


#print_with_flush(" Param SENS_IMU_MODE before: %s" % vehicle.parameters['SENS_IMU_MODE'])
#vehicle.parameters['SENS_IMU_MODE']=0
#print_with_flush(" Param SENS_IMU_MODE after: %s" % vehicle.parameters['SENS_IMU_MODE'])

#print_with_flush(" Param SENS_MAG_MODE before: %s" % vehicle.parameters['SENS_MAG_MODE'])
#vehicle.parameters['SENS_MAG_MODE']=0
#print_with_flush(" Param SENS_MAG_MODE after: %s" % vehicle.parameters['SENS_MAG_MODE'])

#print_with_flush(" Param SIM_GPS_NOISE_X before: %s" % vehicle.parameters['SIM_GPS_NOISE_X'])
#vehicle.parameters['SIM_GPS_NOISE_X']=5
#print_with_flush(" Param SIM_GPS_NOISE_X after: %s" % vehicle.parameters['SIM_GPS_NOISE_X'])

conID = 1
if args.cid:
    conID = int(args.cid)
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_address = ('localhost', 8888)
    try:
        sock.connect(server_address)
    except Exception as e: 
        print("something's wrong with in connecting with TCP server")


################################################################################################
# Listeners
################################################################################################

home_position_set = False

#Create a message listener for home position fix
@vehicle.on_message('HOME_POSITION')
def listener(self, name, home_position):
    global home_position_set
    home_position_set = True

#@vehicle.on_attribute('armed')
#def arming_listener(self, name, value):
#    if(value == False):
#        print_with_flush("Vehicle disarmed")

#        has_finished = True
#        time.sleep(1)

        #print_with_flush("Drawing run plot")
        #plt.plot(times, distances_north, 'r', label='X coordinate')
        #plt.plot(times, distances_east, 'g', label='Y coordinate')
        #plt.plot(times, distances_down, 'b', label='Z coordinate')

        #plt.legend(loc=2)
        #plt.xlabel('Time (s)')
        #plt.ylabel('GPS Coordinates')
        #if(args.typerun == '0'):
        #    plt.title("Golden Run")
        #    plt.savefig('drone_golden_run.png')
        #    print_with_flush("Drawn golden run plot")
        #elif(args.typerun == '1'):
        #    plt.title("Faulty Run")
        #    plt.savefig('drone_faulty_run.png')
        #    print_with_flush("Drawn faulty run plot")

       
     

################################################################################################
# Start mission example
################################################################################################
 
update_values()

# wait for a home position lock
while not home_position_set:
    print_with_flush("Waiting for home position...")
    time.sleep(1)

# Display basic vehicle state
print_with_flush(" Type: %s" % vehicle._vehicle_type)
print_with_flush(" Armed: %s" % vehicle.armed)
print_with_flush(" System status: %s" % vehicle.system_status.state)
print_with_flush(" GPS: %s" % vehicle.gps_0)
print_with_flush(" Alt: %s" % vehicle.location.global_relative_frame.alt)

# wait
time.sleep(takeoff_time)


 

# Change to AUTO mode
print_with_flush("Change to MAV_MODE_AUTO mode...")
PX4setMode(MAV_MODE_AUTO)
time.sleep(2)

#vehicle.home_location = vehicle.location.global_frame


# send and save the file including faults to the UAV that is target of fault injection



# Upload mission
print_with_flush("Uploading mission")
upload_mission(missionPath)

# Arm vehicle
time.sleep(5)
vehicle.armed = True
print_with_flush("Armed")
time.sleep(2)

# monitor mission execution
nextwaypoint = vehicle.commands.next
print_with_flush("Starting to send commands")
print_with_flush("Moving to waypoint 1")


# Wait until the vehicle reaches a safe height before processing the goto (otherwise the command
#  after Vehicle.simple_takeoff will execute immediately).
timer=0
Px4Dir = "/home/[path]/Documents/PX42/PX4-Autopilot/"
while True:
	
    print_with_flush(" Relative Altitude: %s" % vehicle.location.global_relative_frame.alt)
    #Break and return from function just below target altitude.
    if vehicle.location.global_relative_frame.alt>=3*0.95:
        print_with_flush("Reached target altitude")
        break
    elif timer <= 25:
        timer = timer + 1
        print_with_flush("Timer %s" % timer)
    else:
        print_with_flush("Testing - Simulator did not start")
        shutil.copy(SimPath,DronekitDir)
        print("Sim file copied")
        print_with_flush("Disarming the vehicle")
        vehicle.armed = False
        time.sleep(5)
        print_with_flush("Vehicle disarmed")
        has_finished = True
        time.sleep(1)
        print_with_flush("Close vehicle object")
        vehicle.close()
        time.sleep(2)
        bash_pids = str(subprocess.check_output(["pidof", "bash"]),'utf-8')
        pid_to_kill = bash_pids.split(" ")[0]
        kill(int(pid_to_kill))
        print_with_flush("Exit")
        sys.exit()
        stop()
        
        #commandDK1=' '.join(sys.argv)
        #commandDK='python3'+ " "+commandDK1
        #dkScript = subprocess.Popen([commandDK], cwd=DronekitDir, shell=True)
        #break
        #dkScript.wait()
        #os.execv(sys.executable, ['python3'] + sys.argv)
  	
        #vehicle.mode = VehicleMode("RTL")
        #time.sleep(5)
        #vehicle.armed = False
        #time.sleep(2)
        #vehicle.close()
        #time.sleep(1)
        #bash_pids = str(subprocess.check_output(["pidof", "bash"]),'utf-8')
        #pid_to_kill = bash_pids.split(" ")[0]
        #kill(int(pid_to_kill))
        #subprocess.Popen([RestartScriptCommand], cwd=DronekitDir, shell=True)
        #sys.exit()
        
    time.sleep(1)

while nextwaypoint < len(vehicle.commands):
    #print_with_flush("Sending commands ...")
    if args.cid:
        send_data()
    if vehicle.commands.next > nextwaypoint:
        display_seq = vehicle.commands.next+1
        print_with_flush("Moving to waypoint %s" % display_seq)
    if vehicle.location.global_relative_frame.alt<=0.5:
    	if nextwaypoint < len(vehicle.commands):
        	print_with_flush("------Drone Crashed------")
        	time.sleep(5)
        	print_with_flush("Disarming the vehicle")
        	vehicle.armed = False
        	time.sleep(5)
        	print_with_flush("Vehicle disarmed")
        	has_finished = True
        	time.sleep(1)
        	print_with_flush("Close vehicle object")
        	vehicle.close()
        	time.sleep(2)
        	SaveLog()
        	time.sleep(25)
        	print_with_flush("Exit")
        	sys.exit()
        	stop()
        	break
    time.sleep(1)


# wait for the vehicle to land
while vehicle.commands.next > 0:
    time.sleep(1)

#print("Setting LAND mode...")
#vehicle.mode = VehicleMode("LAND")

# Disarm vehicle
print_with_flush("Disarming the vehicle")
vehicle.armed = False
time.sleep(5)

print_with_flush("Vehicle disarmed")
has_finished = True
time.sleep(1)


# Close vehicle object before exiting script
print_with_flush("Close vehicle object")
vehicle.close()
time.sleep(2)

SaveLog()
time.sleep(25)

print_with_flush("Exit")
sys.exit()
stop()


