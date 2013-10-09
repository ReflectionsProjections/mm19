# CS438 - spring 2013 MP0
#
# This is a simple example of a makefile, use this for reference when you create your own
#
# NOTE: if you decide to write your solution in C++, you will have to change the compiler 
# in this file. 

CC=/usr/bin/gcc
CC_OPTS=-g3
CC_LIBS=
CC_DEFINES=
CC_INCLUDES=
CC_ARGS=${CC_OPTS} ${CC_LIBS} ${CC_DEFINES} ${CC_INCLUDES}

# clean is not a file
.PHONY=clean

all: testClient

testClient: testClient.c clientAI.c AIhelper.c
	@${CC} ${CC_ARGS} -o testClient testClient.c
	
clean:
	@rm -f testClient *.o
