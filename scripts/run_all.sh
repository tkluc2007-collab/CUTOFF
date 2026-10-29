#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."
mkdir -p build results
javac -d build src/*.java
java -cp build Verify

{
  java -cp build MachineInfo
  echo "JVM flags: -Xms2g -Xmx2g -XX:+UseSerialGC"
} > results/machine.txt

java -Xms2g -Xmx2g -XX:+UseSerialGC -cp build GenData
java -Xms2g -Xmx2g -XX:+UseSerialGC -cp build Benchmark "$@"
if python3 --version >/dev/null 2>&1; then
  python3 scripts/analyze.py
else
  python scripts/analyze.py
fi
