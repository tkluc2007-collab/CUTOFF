[CmdletBinding()]
param(
    [string]$Sizes,
    [ValidateRange(1, [int]::MaxValue)]
    [int]$Reps = 0
)

$ErrorActionPreference = 'Stop'
Set-Location (Split-Path -Parent $PSScriptRoot)

function Invoke-Native {
    param([string]$Command, [string[]]$Arguments)
    & $Command @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "$Command failed with exit code $LASTEXITCODE."
    }
}

New-Item -ItemType Directory -Force -Path build, results | Out-Null
$sources = Get-ChildItem -Path src -Filter '*.java' | ForEach-Object FullName
Invoke-Native -Command javac -Arguments (@('-d', 'build') + $sources)
Invoke-Native -Command java -Arguments @('-cp', 'build', 'Verify')

$machine = & java -cp build MachineInfo
if ($LASTEXITCODE -ne 0) { throw "java MachineInfo failed with exit code $LASTEXITCODE." }
$machine + 'JVM flags: -Xms2g -Xmx2g -XX:+UseSerialGC' | Set-Content -Path results/machine.txt

Invoke-Native -Command java -Arguments @('-Xms2g', '-Xmx2g', '-XX:+UseSerialGC', '-cp', 'build', 'GenData')
$benchmarkArgs = @('-Xms2g', '-Xmx2g', '-XX:+UseSerialGC', '-cp', 'build', 'Benchmark')
if ($Sizes) { $benchmarkArgs += "--sizes=$Sizes" }
if ($Reps -gt 0) { $benchmarkArgs += "--reps=$Reps" }
Invoke-Native -Command java -Arguments $benchmarkArgs
Invoke-Native -Command python -Arguments @('scripts/analyze.py')
