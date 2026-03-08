# Upscaler

Dark desktop studio for image upscaling, video enhancement and frame interpolation.

Upscaler wraps bundled `Real-ESRGAN NCNN Vulkan` runtimes for image restoration and frame upscaling, then uses `FFmpeg` for video extraction, interpolation and final encode. The app is written in Java 21 and packaged as a cross-platform desktop application.

## Highlights

- dark matte glassmorphism desktop UI
- exact image scales: `2x`, `3x`, `4x`, `6x`, `8x`
- video scales: `1x`, `2x`, `3x`, `4x`, `6x`, `8x`
- safe arbitrary output scale path for bundled x4 models: native `4x` inference plus final exact resize
- target video FPS: `Original`, `60`, `120`, `240`
- GPU / chip selection for the Real-ESRGAN runtime
- video encoder selection from locally available FFmpeg encoders
- drag and drop
- before / after compare slider for images
- live diagnostics log
- real cancel support for image and video jobs
- persistent settings
- bundled Real-ESRGAN runtimes for Windows, Linux and macOS

## Release 1.0

The project is published through GitHub Releases.

Expected release assets for `v1.0.1`:

- `upscaler-1.0.1.jar`
- `Upscaler-1.0.1.exe`
- `upscaler-1.0.1-windows-x64.zip`
- `Upscaler-1.0.1.dmg`
- `upscaler-1.0.1-macos.zip`
- `upscaler-1.0.1-linux-x64.tar.gz`
- `upscaler-1.0.1*.deb`
- `upscaler-1.0.1-linux-arch-x64.tar.gz`
- `PKGBUILD`

Open the repository Releases tab:

- <https://github.com/MrVALIDinfo/Upscaler/releases>

## Installation

### Windows

1. Download the `.exe` installer from Releases.
2. Run the installer.
3. Launch `Upscaler` from Start Menu or Desktop shortcut.

Portable option:

1. Download `upscaler-1.0.1-windows-x64.zip`.
2. Extract it.
3. Run `Upscaler.exe`.

### macOS

1. Download the `.dmg` from Releases.
2. Open it and drag `Upscaler.app` into `Applications`.
3. Start the app from Launchpad or `Applications`.

Portable option:

1. Download `upscaler-1.0.1-macos.zip`.
2. Extract it.
3. Open `Upscaler.app`.

### Debian / Ubuntu

1. Download the `.deb` package from Releases.
2. Install it:

```bash
sudo apt install ./upscaler-1.0.1*.deb
```

### Arch Linux

Portable option:

```bash
tar -xzf upscaler-1.0.1-linux-arch-x64.tar.gz
./Upscaler/bin/Upscaler
```

Packaging option:

1. Download `PKGBUILD` and `upscaler-1.0.1-linux-arch-x64.tar.gz`.
2. Put them into one directory.
3. Build and install:

```bash
makepkg -si
```

### Generic Linux

```bash
tar -xzf upscaler-1.0.1-linux-x64.tar.gz
./Upscaler/bin/Upscaler
```

## Video requirements

Video processing needs local FFmpeg tools:

- `ffmpeg`
- `ffprobe`

Frame interpolation to higher FPS needs the `minterpolate` filter in your FFmpeg build.

Quick check:

```bash
ffmpeg -version
ffprobe -version
ffmpeg -hide_banner -filters | grep minterpolate
```

## How to use

### Images

1. Open the `Image` tab.
2. Drag an image into the window or click `Open Image`.
3. Choose a model and scale.
4. Open `Settings` and select the GPU / chip if you do not want `Auto`.
5. Click `Upscale`.
6. Compare the result with the slider.
7. Click `Save Result`.

### Video

1. Open the `Video` tab.
2. Load a clip.
3. Choose the output path.
4. Select video scale, target FPS and quality.
5. Open `Settings` and choose:
   - compute device for Real-ESRGAN
   - video encoder for final export
6. Click `Render Video`.
7. Use `Open Result` or `Reveal Output` after render completes.

## Models

Bundled models:

- `realesrgan-x4plus`
- `realesrgan-x4plus-anime`

Custom NCNN models can be placed into the user models directory. Each custom model must provide:

- `<name>.bin`
- `<name>.param`

App data folders:

- Linux: `~/.local/share/Upscaler`
- macOS: `~/Library/Application Support/Upscaler`
- Windows: `%APPDATA%\\Upscaler`

## Device detection

Device detection uses this order:

1. `vulkaninfo --summary`
2. probing the bundled Real-ESRGAN runtime by GPU id
3. `Auto` fallback

The selected device is used for image inference and for frame upscaling in video jobs.

## Upscale quality notes

- bundled general and anime models are native `4x` NCNN models
- for `2x` and `3x`, Upscaler now uses a safe `4x` inference pass and then resizes to the exact requested output
- this avoids the broken direct `-s 2` / `-s 3` path that can create tile-like block artifacts on some systems
- for `6x` and `8x`, the app follows the same exact-output strategy instead of forcing unstable NCNN scale modes

## Build from source

### Maven Wrapper

```bash
./mvnw test
./mvnw package
java -jar target/upscaler-1.0.1.jar
```

### Native packaging

Linux:

```bash
bash scripts/package-linux.sh
bash scripts/package-arch.sh
```

macOS:

```bash
bash scripts/package-macos.sh
```

Windows:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\package-windows.ps1
```

## CI and Releases

The repository contains:

- `ci.yml` for test and package validation
- `release.yml` for native release builds on tag push

Publishing a release:

```bash
git tag v1.0.1
git push origin main
git push origin v1.0.1
```

The release workflow uploads native assets into GitHub Releases automatically.

## Project structure

- `src/main/java/upscaler/app`: application bootstrap
- `src/main/java/upscaler/config`: settings and app directories
- `src/main/java/upscaler/model`: immutable domain records
- `src/main/java/upscaler/runtime`: platform detection and runtime extraction
- `src/main/java/upscaler/service`: Real-ESRGAN execution, device detection and video processing
- `src/main/java/upscaler/ui`: desktop UI
- `src/main/resources/runtime/realesrgan`: bundled native runtimes and bundled models
- `scripts`: native packaging scripts
- `.github/workflows`: CI and release automation

## Notes

- Video interpolation currently uses FFmpeg `minterpolate`.
- Hardware video encoders appear only when the local FFmpeg build exposes them.
- Native installers are built in CI on clean GitHub runners. This is more reliable than local cross-building from one machine.

## Third-party notices

See [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
