#!/bin/sh
cd ../src/
javac */*.java
java test.TestRunner $1
