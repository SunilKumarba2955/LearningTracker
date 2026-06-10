#!/bin/sh
set -eu

BUILD_DIR="${BUILD_DIR:-out}"
MAIN_CLASS="${MAIN_CLASS:-com.airtribe.learntrack.Main}"
SOURCE_LIST="/tmp/learntrack-sources.txt"

find src -name "*.java" -type f > "$SOURCE_LIST"

if [ ! -s "$SOURCE_LIST" ]; then
  echo "No Java source files found under src/ yet."
  echo "Add com.airtribe.learntrack.Main, then run: docker compose up --build"
  exit 0
fi

mkdir -p "$BUILD_DIR"
javac -d "$BUILD_DIR" @"$SOURCE_LIST"

MAIN_CLASS_PATH="$BUILD_DIR/$(echo "$MAIN_CLASS" | tr . /).class"

if [ ! -f "$MAIN_CLASS_PATH" ]; then
  echo "Java sources compiled successfully."
  echo "Main class $MAIN_CLASS is not available yet, so there is nothing to run."
  exit 0
fi

java -cp "$BUILD_DIR" "$MAIN_CLASS"
