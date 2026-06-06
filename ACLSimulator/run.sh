#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

if [ ! -f "target/ACLSimulator.jar" ]; then
  echo "ACLSimulator.jar not found. Building it now..."
  mvn clean package
fi

java -jar target/ACLSimulator.jar
