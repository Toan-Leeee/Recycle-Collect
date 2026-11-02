$ErrorActionPreference = 'Stop'
$utf8 = New-Object System.Text.UTF8Encoding($false)
$cp1252 = [System.Text.Encoding]::GetEncoding(1252)
$dir = Join-Path $PSScriptRoot '..\app\src\main\res\layout'
Get-ChildItem -LiteralPath $dir -Filter '*.xml' | ForEach-Object {
    $text = [System.IO.File]::ReadAllText($_.FullName, $utf8)
    try { $fixed = [System.Text.Encoding]::UTF8.GetString($cp1252.GetBytes($text)) } catch { $fixed = $text }
    $fixed = $fixed -replace 'encoding="utf-16"', 'encoding="utf-8"'
    [System.IO.File]::WriteAllText($_.FullName, $fixed, $utf8)
}
Write-Output 'UTF-8 layout text repaired.'
