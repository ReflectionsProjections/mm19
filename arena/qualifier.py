import os
import mm19_runner
#path, filename = os.path.split(os.path.abspath(__file__))
path = r"/home/more/safe/sam/project/acm/mm19/arena"
path += "/teams"
teams = {}


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
    return mm19_runner.testGame(name, run_file)

for dir_entry in os.listdir(path):
    dir_entry_path = os.path.join(path, dir_entry)
    files = os.listdir(dir_entry_path)
    if "name.txt" in files:
        if "run.sh" in files:
            add_team(dir_entry,
                     os.path.join(dir_entry_path,"name.txt"),
                     os.path.join(dir_entry_path, "run.sh"))
        else: 
            print "error missing run.sh for entry "+dir_entry
    else:
        print "error missing name.text for entry "+dir_entry

print teams
