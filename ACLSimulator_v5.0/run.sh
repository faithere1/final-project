#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

if [ ! -f "ACLSimulator.jar" ]; then
  echo "ACLSimulator.jar not found in this folder."
  exit 1
fi

java -jar ACLSimulator.jar
