#!/bin/sh
cd ../src/
javac */*.java
java server.SFTPServer
