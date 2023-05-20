#!/bin/bash

mkdir -p classes

javac -d classes -sourcepath src/main/java src/main/java/me/soliveirajr/piping/**/*.java

