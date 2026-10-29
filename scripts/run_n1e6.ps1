[CmdletBinding()]
param()

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

if (-not (Test-Path data/random_1000000.txt)) {
    Invoke-Native -Command java -Arguments @('-Xms2g', '-Xmx2g', '-XX:+UseSerialGC', '-cp', 'build', 'GenData')
}
Invoke-Native -Command java -Arguments @(
    '-Xms2g', '-Xmx2g', '-XX:+UseSerialGC', '-cp', 'build', 'Benchmark',
    '--sizes=1000000', '--reps=25', '--output=results/raw_n1e6.csv'
)
Invoke-Native -Command python -Arguments @('scripts/analyze.py')
