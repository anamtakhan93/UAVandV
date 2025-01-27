import os
import subprocess
import glob
import re
import time
import psutil
import signal
import shutil
import sys
from os import system
from distutils.dir_util import copy_tree

PX4Dir = "/home/[path]/Documents/PX4/PX4-Autopilot"
DronekitDir = "/home/[path]/RunCampagins/"
MissionsDir = "/home/[path]/RunCampagins/Shorts/"
CompletedFaultsDir = "/home/[path]/RunCampagins/FinishedMissions/"
CompletedMissionDir = "/home/[path]/RunCampagins/CompletedMissions/"
RunningMissionDir = "/home/[path]/RunCampagins/RunningMissions/"
MainFaultDir = "/home/[path]/Injections/"
MainFault = "/home/[path]/Injections/Fault.fc"
SimPath = "/home/[path]/RunCampagins/SimStart/Sim.txt"
Lat = "123"
Lon = "123"
Alt = "5"

def kill(proc_pid):
    if psutil.pid_exists(proc_pid):
        print("a process with pid %d exists" % proc_pid)
        process = psutil.Process(proc_pid)
        for proc in process.children(recursive=True):
            proc.kill()
        process.kill()
    else:
        print("a process with pid %d does not exist" % proc_pid)

def LineLength(aFileName) -> int:
    file = open(aFileName, "r")
    line_count = 0
    for line in file:
        if line != "\n":
            line_count += 1
    file.close()
    return line_count

def readmission(aFileName):
    missionread = False
    num_lines = LineLength(aFileName)
    with open(aFileName) as f:
        for i, line in enumerate(f):
            linearray = re.split('    |\n', line)
            if len(linearray) <= 2:
                missionread = False
            elif len(linearray) <= 5:
                missionread = False
            else:
                if i == num_lines - 7:
                    global Lat
                    Lat = linearray[11]
                    global Lon
                    Lon = linearray[12]
                    missionread = True
                    break

def readmission_full(aFileName):
    missionread = False
    with open(aFileName) as f:
        for i, line in enumerate(f):
            linearray = re.split('    |\n', line)
            if len(linearray) <= 2:
                missionread = False
            elif len(linearray) <= 5:
                missionread = False
            else:
                global Lat
                Lat = linearray[11]
                global Lon
                Lon = linearray[12]
                missionread = True
                break


# Function to read MissionPath from .fc file
def get_mission_path(fc_file):
    mission_path = None
    with open(fc_file, 'r') as file:
        for line in file:
            if line.startswith("MissionPath"):
                # Extract the path after '=' and strip any whitespace
                mission_path = line.split("=", 1)[1].strip()
                break
    return mission_path




if os.path.exists(DronekitDir + "Sim.txt"):
    os.remove(DronekitDir + "Sim.txt")

list_of_faults = glob.glob("./*.fc")
print("--------- Faults ----------")
print(list_of_faults)
for fault in list_of_faults:
    print("------------ Working on Fault : " + fault)
    Path(MainFaultDir).mkdir(parents=True, exist_ok=True)
    shutil.copy(DronekitDir + fault, MainFault)
    missionPath = get_mission_path(fault)
    readmission_full(missionPath)
    time.sleep(2)
    print("--------- Fault Updated ----------")
    commandG = "gnome-terminal -- bash -c 'export PX4_HOME_LAT=" + Lat + " && export PX4_HOME_LON=" + Lon + " && export PX4_HOME_ALT=" + Alt + " && HEADLESS=1 Tools/gazebo_sitl_multiple_run.sh -s \"iris:1\"'"
    print(commandG)
    gazebo = subprocess.run([commandG], cwd=PX4Dir, stdout=subprocess.PIPE, shell=True, preexec_fn=os.setpgrp)
    time.sleep(15)
    commandDK = "python3.8 dronekit_script_custom_short_single1.py -fn " + fault
    print(commandDK)
    dkScript = subprocess.Popen([commandDK], cwd=DronekitDir, shell=True)
    dkScript.wait()
    if os.path.exists(DronekitDir + "Sim.txt"):
        print("Sim file removed")
        os.remove(DronekitDir + "Sim.txt")
        commandDK = "gnome-terminal -- bash -c 'python3.8 RunMissions.py'"
        print(commandDK)
        dkScript = subprocess.Popen([commandDK], cwd=DronekitDir, shell=True)
        time.sleep(2)
        sys.exit()
    bash_pids = str(subprocess.check_output(["pidof", "bash"]), 'utf-8')
    pid_to_kill = bash_pids.split(" ")[0]
    kill(int(pid_to_kill))
    time.sleep(2)
    print("--------------------")
    shutil.move(DronekitDir + fault, CompletedFaultsDir + fault)
    copy_tree(RunningMissionDir, DronekitDir)
    time.sleep(5)

