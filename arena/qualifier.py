#!/usr/bin/env python
import os
import mm19_runner
import signal
import json

path, filename = os.path.split(os.path.abspath(__file__))
#path = r"/home/more/safe/sam/project/acm/mm19/arena"
path += "/teams"
teams = {}
OUTPUT_NAME = "qualified_teams.txt"
TIMEOUT = 120  # two minute timeout


class Alarm(Exception):
    pass


def alarm_handler(signum, frame):
    raise Alarm

signal.signal(signal.SIGALRM, alarm_handler)


def add_team(team, name_file, run_file):
    with open(name_file) as f:
        name = f.readlines()[0].rstrip()
    if name in teams:
        print "error the team "+team+" could not be added"
        print name+" has aready been used"
        return
    if qualify(name, run_file):
        teams[name] = run_file
    else:
        print name+" failed to qualify"


def qualify(name, run_file):
    signal.alarm(TIMEOUT)  # set timeout
    try:
        result = mm19_runner.testGame(name, run_file)
        signal.alarm(0)  # cancel alarm
        return result
    except Alarm:
        print name + " timed out"
        return False

def main():
    for dir_entry in os.listdir(path):
        dir_entry_path = os.path.join(path, dir_entry)
        files = os.listdir(dir_entry_path)
        if "name.txt" in files:
            if "run.sh" in files:
                add_team(dir_entry,
                         os.path.join(dir_entry_path, "name.txt"),
                         os.path.join(dir_entry_path, "run.sh"))
            else:
                print "error missing run.sh for entry "+dir_entry
        else:
            print "error missing name.text for entry "+dir_entry
            
    print teams
    with open(OUTPUT_NAME, 'w') as f:
        f.write(json.dumps(teams))

if __name__ == "__main__":
    main()
