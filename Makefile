ifndef OS
 OS := $(shell uname)
 export OS  
endif

ifndef HOST
 ifeq ($(OS),Linux)
  HOST = unix
 endif
 ifeq ($(OS),Darwin)
  HOST = unix
 endif
 ifeq ($(OS),OSX)
  HOST = unix
 endif
 ifeq ($(OS),MacOS)
  HOST = unix
 endif
 ifndef HOST
  HOST = win # assuming windows here
 endif
endif

CC=gcc
CFLAGS=-Wall
#LDFLAGS=
OBJECTS=bin/pipe.o
INCLUDES=-I"/usr/lib/jvm/java-7-openjdk/include" -I"/usr/lib/jvm/java-7-openjdk/include/linux"

ifeq ($(HOST),unix)
 OUTPUT="libpipe.so"
 RM=rm
else
 OUTPUT="pipe.dll"
 RM=del
endif

all: pipe.h $(OBJECTS) $(OUTPUT)

$(OUTPUT): $(OBJECTS)
	$(CC) -shared $(LDFLAGS) $(OBJECTS) -o $@

bin/%.o: jni/%.c
	$(CC) $(INCLUDES) $(CFLAGS) -c -o $@ $<
	
pipe.h:
	javah -jni -classpath bin/ -o jni/pipe.h jsingleinstance.PipeCommunication
	
clean:
	$(RM) jni/pipe.h
	$(RM) $(OBJECTS)
	$(RM) $(OUTPUT)
