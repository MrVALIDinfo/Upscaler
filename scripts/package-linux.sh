#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

APP_NAME="Upscaler"
VERSION="${1:-$(./mvnw -q -DforceStdout help:evaluate -Dexpression=project.version)}"
JAR_NAME="upscaler-${VERSION}.jar"
DIST_DIR="$ROOT_DIR/dist/linux"
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
  --icon "$ROOT_DIR/src/main/resources/assets/logo.png" \
  --vendor "MrVALIDinfo" \
  --app-version "$VERSION" \
  --copyright "Copyright 2026" \
  --description "AI upscaler studio for images and video." \
  --java-options "-Dsun.java2d.uiScale=1.0"

tar -C "$DIST_DIR" -czf "$DIST_DIR/upscaler-$VERSION-linux-x64.tar.gz" "$APP_NAME"

if command -v fakeroot >/dev/null 2>&1; then
  jpackage \
    --type deb \
    --dest "$DIST_DIR" \
    --input "$INPUT_DIR" \
    --name "$APP_NAME" \
    --main-jar "$JAR_NAME" \
    --main-class ProgramStart.Main \
    --icon "$ROOT_DIR/src/main/resources/assets/logo.png" \
    --vendor "MrVALIDinfo" \
    --app-version "$VERSION" \
    --linux-package-name upscaler \
    --linux-shortcut \
    --linux-menu-group Graphics \
    --copyright "Copyright 2026" \
    --description "AI upscaler studio for images and video." \
    --java-options "-Dsun.java2d.uiScale=1.0"
fi
