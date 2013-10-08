#!/user/bin/env python
import sys
import BaseHTTPServer
from SimpleHTTPServer import SimpleHTTPRequestHandler

# makes sure only the visualizer will be found
class mmRequstHandler(SimpleHTTPRequestHandler,object):
    def do_GET(self):
        if self.path == "/get_game.log": #alows me to get current game
            print "getting game"
            return "game"
        else:
            self.path = "/visualizer" + self.path
            print self.path
            super(mmRequstHandler, self).do_GET()

## blow here is basic web server stuff
HandlerClass = mmRequstHandler
ServerClass  = BaseHTTPServer.HTTPServer
Protocol     = "HTTP/1.0"


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
