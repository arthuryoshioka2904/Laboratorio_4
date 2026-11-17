#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "$0")"
mkdir -p out
javac -encoding UTF-8 -Xlint:all -d out src/*.java
java -cp out Main
