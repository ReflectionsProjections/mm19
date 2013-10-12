#!/usr/bin/env python
import sys
import os
import BaseHTTPServer
from SimpleHTTPServer import SimpleHTTPRequestHandler
from mm19_runner import  runGame
from mm19_git_tools import git_pull
from qualifier import qualifyed_teams
import random
import time
import threading

GameThread = {}
ARENA_VISUALIZER_FILE = "current_match.tmp"
teams = {}

HOW_OFTEN_TO_PULL_IN_MIN = 60 # update every hour

TIME_SINCE_OF_PULL = 0;

#def qualifyed_teams(): 
#    return { 'Roger': '/home/more/safe/sam/project/acm/mm19/arena/teams/mm19-test-3', 'oneFish': '/home/more/safe/sam/project/acm/mm19/arena/teams/team1'}

path, filename = os.path.split(os.path.abspath(__file__))
path += "/teams"

def time_to_update():
    return ((time.time() - TIME_SINCE_OF_PULL) >
            (HOW_OFTEN_TO_PULL_IN_MIN * 60)) 

# makes sure only the visualizer will be found
class mmRequstHandler(SimpleHTTPRequestHandler,object):
    def do_GET(self):
        if self.path == "/get_game.log": #alows me to get current game
            print "getting game"
            global GameThread
            GameThread.join()
            with open(ARENA_VISUALIZER_FILE) as f:
                if f:
                    self.send_response(200)
                    self.send_header("display_text","Exhibition Match")
                    self.end_headers()
                    self.wfile.write(f.read())
                else:
                    self.send_error(500)
                GameThread = threading.Thread(target=play_game)
                GameThread.start()
        else:
            self.path = "/visualizer" + self.path
            print self.path
            super(mmRequstHandler, self).do_GET()


def update_repos():
    print "____________________updating___________________"
    for dir_entry in os.listdir(path):
        dir_entry_path = os.path.join(path, dir_entry)
        files = os.listdir(dir_entry_path)
        if ".git" in files:
            git_pull(dir_entry_path)
        else:
            print dir_entry_path+" does not have a git repo"
    global TIME_SINCE_OF_PULL
    TIME_SINCE_OF_PULL= time.time()
            

def play_game():
    if time_to_update():
        update_repos()
        qualify()
    bot1,bot2 = random.sample(teams,2)
    print teams[bot1], "v", teams[bot2]
    winner = runGame(ARENA_VISUALIZER_FILE, bot1, bot2, 
                     teams[bot1], teams[bot2])
    print winner
    if winner == """#fail#""":
        play_game()
    
## blow here is basic web server stuff
HandlerClass = mmRequstHandler
ServerClass  = BaseHTTPServer.HTTPServer
Protocol     = "HTTP/1.0"

def start_web_server():
    if sys.argv[1:]:
        port = int(sys.argv[1])
    else:
        port = 8000
    server_address = ('127.0.0.1', port)

    HandlerClass.protocol_version = Protocol
    httpd = ServerClass(server_address, HandlerClass)

    sa = httpd.socket.getsockname()
    print "Serving HTTP on", sa[0], "port", sa[1], "..."
    httpd.serve_forever()

def qualify():
    global teams
    teams = qualifyed_teams(False)

def main():
    update_repos()
    qualify()
    global GameThread
    GameThread = threading.Thread(target=play_game)
    GameThread.start()
    start_web_server()
    
if __name__=="__main__":
    main()

