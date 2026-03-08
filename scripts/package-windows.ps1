$ErrorActionPreference = "Stop"

$RootDir = Split-Path -Parent $PSScriptRoot
Set-Location $RootDir

$Version = if ($args.Count -gt 0) { $args[0] } else { ([xml](Get-Content "$RootDir/pom.xml")).project.version.Trim() }
$JarName = "upscaler-$Version.jar"
$DistDir = Join-Path $RootDir "dist/windows"
$InputDir = Join-Path $DistDir "input"

if (Test-Path $DistDir) {
    Remove-Item -Recurse -Force $DistDir
}
New-Item -ItemType Directory -Force -Path $InputDir | Out-Null

& .\mvnw.cmd -B -DskipTests package
Copy-Item "target/$JarName" $InputDir

$commonArgs = @(
    "--dest", $DistDir,
    "--input", $InputDir,
    "--name", "Upscaler",
    "--main-jar", $JarName,
    "--main-class", "ProgramStart.Main",
    "--icon", "src/main/resources/assets/logo.ico",
    "--vendor", "MrVALIDinfo",
    "--app-version", $Version,
    "--description", "AI upscaler studio for images and video."
)

& jpackage --type app-image @commonArgs
Compress-Archive -Path "$DistDir/Upscaler/*" -DestinationPath "$DistDir/upscaler-$Version-windows-x64.zip"

& jpackage --type exe @commonArgs --win-dir-chooser --win-menu --win-shortcut --win-per-user-install
