from subprocess import call
from subprocess import Popen
import sys

import getopt
import time
import random
import signal

dummy_player_path = \
    r"/home/more/safe/sam/project/acm/mm19/arena/teams/team1/run.sh"


class Alarm(Exception):
    pass


def alarm_handler(signum, frame):
    raise Alarm

signal.signal(signal.SIGALRM, alarm_handler)


def testGame(name, run_script):
    print "running test ..."
    runGame("qualifier", name, "dummy", run_script, dummy_player_path)
    return True
"""
runs this years game
args: info for the logname, the players names, the scripts used to run them
"""


def runGame(info, name1, name2, run_script1, run_script2):
    match_name = info + name1 + "_vs_" + name2 + ".out"
    server = Popen(["java", "-jar", "server.jar", match_name])
    bot1 = Popen(run_script1)
    bot2 = Popen(run_script2)
    server.wait()  # wait for the sever to exit
    if not bot1.poll():
        bot1.terminate()  # kill client if it fails to exit
    if not bot2.poll():
        bot2.terminate()
    if not (bot1.poll() and bot2.poll()):  # makes sure they die
        time.sleep(100)
        if not bot1.poll():
            bot1.kill()
        if not bot2.poll():
            bot2.kill()
