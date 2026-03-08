#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

APP_NAME="Upscaler"
VERSION="${1:-$(./mvnw -q -DforceStdout help:evaluate -Dexpression=project.version)}"
JAR_NAME="upscaler-${VERSION}.jar"
DIST_DIR="$ROOT_DIR/dist/arch"
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
  --description "AI upscaler studio for images and video." \
  --java-options "-Dsun.java2d.uiScale=1.0"

tar -C "$DIST_DIR" -czf "$DIST_DIR/upscaler-$VERSION-linux-arch-x64.tar.gz" "$APP_NAME"

cat > "$DIST_DIR/PKGBUILD" <<EOF
pkgname=upscaler
pkgver=${VERSION}
pkgrel=1
pkgdesc="AI upscaler studio for images and video"
arch=('x86_64')
url="https://github.com/MrVALIDinfo/Upscaler"
license=('custom')
depends=('java-runtime')
source=("upscaler-${VERSION}-linux-arch-x64.tar.gz")
sha256sums=('SKIP')

package() {
  install -dm755 "\$pkgdir/opt"
  cp -r "${APP_NAME}" "\$pkgdir/opt/upscaler"
  install -dm755 "\$pkgdir/usr/bin"
  cat > "\$pkgdir/usr/bin/upscaler" <<'SH'
#!/usr/bin/env bash
exec /opt/upscaler/bin/Upscaler "\$@"
SH
  chmod +x "\$pkgdir/usr/bin/upscaler"
}
EOF
