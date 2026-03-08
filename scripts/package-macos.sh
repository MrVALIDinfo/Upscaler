#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

APP_NAME="Upscaler"
VERSION="${1:-$(./mvnw -q -DforceStdout help:evaluate -Dexpression=project.version)}"
JAR_NAME="upscaler-${VERSION}.jar"
DIST_DIR="$ROOT_DIR/dist/macos"
INPUT_DIR="$DIST_DIR/input"

rm -rf "$DIST_DIR"
mkdir -p "$INPUT_DIR"

./mvnw -B -DskipTests package
cp "target/$JAR_NAME" "$INPUT_DIR/"

jpackage \
  --type app-image \
  --dest "$DIST_DIR" \
  --input "$INPUT_DIR" \
  --name "$APP_NAME" \
  --main-jar "$JAR_NAME" \
  --main-class ProgramStart.Main \
  --icon "$ROOT_DIR/src/main/resources/assets/logo.icns" \
  --vendor "MrVALIDinfo" \
  --app-version "$VERSION" \
  --description "AI upscaler studio for images and video."

ditto -c -k --sequesterRsrc --keepParent "$DIST_DIR/$APP_NAME.app" "$DIST_DIR/upscaler-$VERSION-macos.zip"

jpackage \
  --type dmg \
  --dest "$DIST_DIR" \
  --input "$INPUT_DIR" \
  --name "$APP_NAME" \
  --main-jar "$JAR_NAME" \
  --main-class ProgramStart.Main \
  --icon "$ROOT_DIR/src/main/resources/assets/logo.icns" \
  --vendor "MrVALIDinfo" \
  --app-version "$VERSION" \
  --description "AI upscaler studio for images and video."
