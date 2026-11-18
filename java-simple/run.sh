#!/bin/sh
# Compiles and runs the demo. -sourcepath lets javac find all classes used by Main in the sub-packages.
set -e
cd "$(dirname "$0")"
mkdir -p build
javac -d build -sourcepath src src/hybrid/Main.java
java -cp build hybrid.Main "$@"
