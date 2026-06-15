# Maven Wrapper PowerShell Script
$projectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$wrapperDir = Join-Path $projectDir ".mvn\wrapper"
$wrapperJar = Join-Path $wrapperDir "maven-wrapper.jar"
$wrapperProps = Join-Path $wrapperDir "maven-wrapper.properties"
$wrapperBase = Join-Path $env:USERPROFILE ".m2\wrapper\dists"

# 读取 distributionUrl
$distributionUrl = "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip"

if (Test-Path $wrapperProps) {
    $content = Get-Content $wrapperProps
    foreach ($line in $content) {
        if ($line -match "^distributionUrl=(.*)$") {
            $distributionUrl = $Matches[1]
            break
        }
    }
}

# 确保 maven-wrapper.jar 存在
if (!(Test-Path $wrapperJar)) {
    $wrapperUrl = "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"
    New-Item -ItemType Directory -Path $wrapperDir -Force | Out-Null
    curl.exe -sL -o $wrapperJar $wrapperUrl
}

# 下载 Maven (如果本地没有)
$mavenVersion = ($distributionUrl -split "/")[-2]
$distName = "apache-maven-${mavenVersion}-bin.zip"
$mavenHome = $null

# 搜索已解压的 Maven
$basePaths = @($wrapperBase)
$basePaths += Join-Path $env:USERPROFILE ".m2\wrapper\dists"
foreach ($bp in $basePaths) {
    if (Test-Path $bp) {
        $found = Get-ChildItem -Path $bp -Recurse -Filter "mvn.cmd" -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($found) {
            $mavenHome = Split-Path (Split-Path $found.FullName)
            break
        }
    }
}

# 如果没找到，下载
if (!$mavenHome) {
    $mavenDistDir = Join-Path $wrapperBase "apache-maven-$mavenVersion"
    $mavenZip = Join-Path $mavenDistDir $distName
    
    if (!(Test-Path $mavenZip)) {
        New-Item -ItemType Directory -Path $mavenDistDir -Force | Out-Null
        Write-Host "Downloading Maven $mavenVersion..."
        curl.exe -sL -o $mavenZip $distributionUrl
    }
    
    $mavenHome = Join-Path $mavenDistDir "apache-maven-$mavenVersion"
    if (!(Test-Path (Join-Path $mavenHome "bin\mvn.cmd"))) {
        Write-Host "Extracting Maven..."
        Add-Type -AssemblyName System.IO.Compression.FileSystem
        [System.IO.Compression.ZipFile]::ExtractToDirectory($mavenZip, $mavenDistDir)
    }
}

# 设置 JAVA_HOME
if (!$env:JAVA_HOME) {
    $javaExe = (Get-Command java -ErrorAction SilentlyContinue).Source
    if ($javaExe) {
        $env:JAVA_HOME = Split-Path (Split-Path $javaExe)
    }
}

# 运行 Maven
$mvnCmd = Join-Path $mavenHome "bin\mvn.cmd"
$args = @("-f", $projectDir) + $args
& $mvnCmd @args
