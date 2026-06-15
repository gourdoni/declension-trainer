#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

# Ensure dependencies are present.
if ! command -v java >/dev/null 2>&1; then
    echo "Error: 'java' not found on PATH. Please install JDK 21+" >&2
    exit 1
fi
if ! command -v mvn >/dev/null 2>&1; then
    echo "Error: 'mvn' (Maven) not found on PATH. Please install Maven 3.9+" >&2
    exit 1
fi

echo "Building..."
mvn -q clean package
exec java -jar target/declension-trainer.jar
