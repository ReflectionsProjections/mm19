#!/usr/bin/env python
from subprocess import Popen
import sys
import os

FNULL = open(os.devnull, 'w')
PLAYERONESTDOUT = FNULL
path, filename = os.path.split(os.path.abspath(__file__))
dummy_player_path = \
    path+'/dummy'


def git_clone(git_path, mydir):
    clone = Popen(["git", "clone", git_path], cwd=mydir)
    clone.wait()

def git(mydir,*args):
    git_proc = Popen((["git"]+list(args)), cwd=mydir)
    git_proc.wait()

def cp(source, dest, *args):
    cp_proc = Popen((["cp"]+list(args) + [source, dest]))
    cp_proc.wait()

def git_pull(git_path): 
     fetch = Popen(["git", "fetch", "--all"], cwd=git_path)
     fetch.wait()
     reset = Popen(["git", "reset", "--hard", "origin/master"], cwd=git_path)
     reset.wait()

def clone_all():
    repo =  'ssh://git@bitbucket.org/mm19-{}/mm19-{}.git'
    for x in xrange(1, 61):
        git_clone(repo.format(x, x), os.path.join(path, "arena"))
        #print os.path.join(path,"teams")
        #print repo.format(x,x)
def make_repos():
    template = os.path.join(path,"mm_start")
    #repo =  'ssh://git@bitbucket.org/mm19-{}/mm19-{}.git'
    repo =  'ssh://git@mm19/mm19-{}/mm19-{}.git'
    for x in xrange(51, 61):
        new_team = os.path.join(path,"teams", "mm19-{}".format(x))
        cp(template, new_team, "-r")
        git(new_team, "init")
        git(new_team, "remote", "add", "origin", repo.format(x,x))
        git(new_team, "add", ".")
        git(new_team, "commit", "-m", "good luck" )
        git(new_team, "push",  "--set-upstream", "origin", "master")
make_repos()
