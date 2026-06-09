#!/bin/sh
set -eu
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
GRADLE_VERSION=9.4.1
DIST_DIR="$APP_HOME/.gradle-bootstrap/gradle-$GRADLE_VERSION"
ZIP="$APP_HOME/.gradle-bootstrap/gradle-$GRADLE_VERSION-bin.zip"
if [ ! -x "$DIST_DIR/bin/gradle" ]; then
  mkdir -p "$APP_HOME/.gradle-bootstrap"
  URL="https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
  if command -v curl >/dev/null 2>&1; then
    curl -fL "$URL" -o "$ZIP"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$ZIP" "$URL"
  else
    echo "Neither curl nor wget is available; install one or install Gradle $GRADLE_VERSION." >&2
    exit 1
  fi
  command -v unzip >/dev/null 2>&1 || { echo "unzip is required." >&2; exit 1; }
  unzip -q -o "$ZIP" -d "$APP_HOME/.gradle-bootstrap"
fi
exec "$DIST_DIR/bin/gradle" "$@"
