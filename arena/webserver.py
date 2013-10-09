#!/usr/bin/env python
import sys
import os
import BaseHTTPServer
from SimpleHTTPServer import SimpleHTTPRequestHandler
from mm19_runner import git_pull, runGame
from qualifier import qualifyed_teams
import random
ARENA_VISUALIZER_FILE = "current_match.tmp"

teams = qualifyed_teams()
#def qualifyed_teams(): 
#    return { 'Roger': '/home/more/safe/sam/project/acm/mm19/arena/teams/mm19-test-3', 'oneFish': '/home/more/safe/sam/project/acm/mm19/arena/teams/team1'}

path, filename = os.path.split(os.path.abspath(__file__))
path += "/teams"

# makes sure only the visualizer will be found
class mmRequstHandler(SimpleHTTPRequestHandler,object):
    def do_GET(self):
        if self.path == "/get_game.log": #alows me to get current game
            print "getting game"
            play_game()
            with open(ARENA_VISUALIZER_FILE) as f:
                if f:
                    self.send_response(200)
                    self.end_headers()
                    self.wfile.write(f.read())
                else:
                    self.send_error(500)
        else:
            self.path = "/visualizer" + self.path
            print self.path
            super(mmRequstHandler, self).do_GET()


def update_repos():
    for dir_entry in os.listdir(path):
        dir_entry_path = os.path.join(path, dir_entry)
        files = os.listdir(dir_entry_path)
        if ".git" in files:
            git_pull(dir_entry_path)
        else:
            print dir_entry_path+" does not have a git repo"
        

def play_game():
    bot1,bot2 = random.sample(teams,2)
    runGame(ARENA_VISUALIZER_FILE, bot1, bot2, teams[bot1], teams[bot2])
    
## blow here is basic web server stuff
HandlerClass = mmRequstHandler
ServerClass  = BaseHTTPServer.HTTPServer
Protocol     = "HTTP/1.0"

def main():
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
    
if __name__=="__main__":
    main()
