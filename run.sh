#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

MVN="./mvnw"

TARGET="${1:-}"

echo "Building (layout demo)..."
"$MVN" -DskipTests clean compile
echo "Running RelativeResizeDemo..."
exec "$MVN" -DskipTests \
  -Dexec.mainClass=org.example.editor.demo.RelativeResizeDemo \
  -Dexec.classpathScope=runtime \
  -Dexec.jvmArgs="--add-modules=javafx.controls,javafx.fxml,javafx.graphics,javafx.web" \
  org.codehaus.mojo:exec-maven-plugin:3.1.0:java


