$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

$targetExtensions = @(
    ".java",
    ".kt",
    ".groovy",
    ".ts",
    ".tsx",
    ".js",
    ".jsx",
    ".css",
    ".scss",
    ".html",
    ".sql",
    ".xml",
    ".json",
    ".yml",
    ".yaml",
    ".properties",
    ".gradle",
    ".md",
    ".ps1",
    ".sh"
)

$excludedDirs = @(
    ".git",
    ".github",
    ".gradle",
    ".idea",
    ".vscode",
    "node_modules",
    "build",
    "dist",
    "out",
    "target",
    "bin",
    "obj",
    ".react-router",
    ".intlayer"
)

$stats = @{}
$processedFiles = 0

Write-Host ""
Write-Host "Starting code line scan..." -ForegroundColor Cyan
Write-Host "Root: $root"
Write-Host ("Extensions: {0}" -f ($targetExtensions -join ", "))
Write-Host ("Excluded directories: {0}" -f ($excludedDirs -join ", "))
Write-Host ""

$files = Get-ChildItem -Path $root -Recurse -File | Where-Object {
    $extension = $_.Extension.ToLowerInvariant()
    if ($extension -notin $targetExtensions) {
        return $false
    }

    foreach ($dir in $excludedDirs) {
        $pattern = [IO.Path]::DirectorySeparatorChar + $dir + [IO.Path]::DirectorySeparatorChar
        if ($_.FullName -like "*$pattern*") {
            return $false
        }
    }

    return $true
}

Write-Host ("Matched {0} files. Counting lines..." -f $files.Count) -ForegroundColor DarkCyan

$files | ForEach-Object {
    $processedFiles += 1

    if ($processedFiles -le 5 -or $processedFiles % 50 -eq 0) {
        Write-Host ("[{0}/{1}] {2}" -f $processedFiles, $files.Count, $_.FullName)
    }

    $extension = $_.Extension.ToLowerInvariant()
    $lineCount = (Get-Content -LiteralPath $_.FullName | Measure-Object -Line).Lines

    if (-not $stats.ContainsKey($extension)) {
        $stats[$extension] = [PSCustomObject]@{
            Extension = $extension
            Files     = 0
            Lines     = 0
        }
    }

    $stats[$extension].Files += 1
    $stats[$extension].Lines += $lineCount
}

Write-Host ""
Write-Host "Line counting finished. Preparing summary..." -ForegroundColor DarkCyan

$rows = $stats.Values | Sort-Object Lines -Descending
$totalFiles = ($rows | Measure-Object -Property Files -Sum).Sum
$totalLines = ($rows | Measure-Object -Property Lines -Sum).Sum

if (-not $rows) {
    Write-Host "No matching source files were found."
    exit 0
}

Write-Host ""
Write-Host "Code Line Statistics" -ForegroundColor Cyan
Write-Host "Root: $root"
Write-Host ""

$rows | Format-Table -AutoSize `
    @{ Label = "Type"; Expression = { $_.Extension } }, `
    @{ Label = "Files"; Expression = { $_.Files } }, `
    @{ Label = "Lines"; Expression = { $_.Lines } }

Write-Host ""
Write-Host ("Total files: {0}" -f $totalFiles) -ForegroundColor Yellow
Write-Host ("Total lines: {0}" -f $totalLines) -ForegroundColor Yellow
