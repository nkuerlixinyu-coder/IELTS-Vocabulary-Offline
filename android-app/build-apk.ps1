$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$configuredJava = $env:JAVA_HOME
if (-not $configuredJava) {
    $configuredJava = [Environment]::GetEnvironmentVariable("JAVA_HOME", "User")
}
if (-not $configuredJava) {
    $configuredJava = [Environment]::GetEnvironmentVariable("JAVA_HOME", "Machine")
}

$configuredSdk = $env:ANDROID_HOME
if (-not $configuredSdk) {
    $configuredSdk = $env:ANDROID_SDK_ROOT
}
if (-not $configuredSdk) {
    $configuredSdk = [Environment]::GetEnvironmentVariable("ANDROID_HOME", "User")
}
if (-not $configuredSdk) {
    $configuredSdk = [Environment]::GetEnvironmentVariable("ANDROID_HOME", "Machine")
}

if (-not $configuredJava -or -not (Test-Path -LiteralPath $configuredJava)) {
    throw "JDK 17 not found. Set JAVA_HOME before building."
}
if (-not $configuredSdk -or -not (Test-Path -LiteralPath $configuredSdk)) {
    throw "Android SDK not found. Set ANDROID_HOME or ANDROID_SDK_ROOT before building."
}

$env:JAVA_HOME = $configuredJava
$env:ANDROID_HOME = $configuredSdk
$env:ANDROID_SDK_ROOT = $configuredSdk

Push-Location $projectRoot
try {
    $gradleCommand = Join-Path $projectRoot "gradlew.bat"
    if ($env:GRADLE_HOME) {
        $configuredGradle = Join-Path $env:GRADLE_HOME "bin\gradle.bat"
        if (Test-Path -LiteralPath $configuredGradle) {
            $gradleCommand = $configuredGradle
        }
    }

    & $gradleCommand ":app:assembleDebug" --stacktrace
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle build failed with exit code $LASTEXITCODE."
    }

    $sourceApk = Join-Path $projectRoot "app\build\outputs\apk\debug\app-debug.apk"
    $distDir = Join-Path $projectRoot "dist"
    $distApk = Join-Path $distDir "IELTS-Vocabulary-Offline-Android-1.0.0.apk"
    New-Item -ItemType Directory -Force -Path $distDir | Out-Null
    Copy-Item -LiteralPath $sourceApk -Destination $distApk -Force

    $distFile = Get-Item -LiteralPath $distApk
    $distHash = Get-FileHash -Algorithm SHA256 -LiteralPath $distApk
    $checksumFile = Join-Path $distDir "SHA256SUMS.txt"
    $checksumLine = "{0}  {1}" -f $distHash.Hash.ToLowerInvariant(), $distFile.Name
    Set-Content -LiteralPath $checksumFile -Value $checksumLine -Encoding utf8NoBOM

    $distFile | Select-Object FullName, Length, LastWriteTime
    $distHash
    Get-Item -LiteralPath $checksumFile | Select-Object FullName, Length, LastWriteTime
} finally {
    Pop-Location
}
