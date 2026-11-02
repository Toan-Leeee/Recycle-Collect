$ErrorActionPreference = 'Stop'
$dir = Join-Path $PSScriptRoot '..\app\src\main\res\layout'
Get-ChildItem -LiteralPath $dir -Filter 'activity_*.xml' | Where-Object { $_.Name -notin @('activity_login.xml','activity_home.xml') } | ForEach-Object {
    $text = [System.IO.File]::ReadAllText($_.FullName, [System.Text.UTF8Encoding]::new($false))
    if ($text -notmatch 'view_demo_banner') {
        $match = [regex]::Match($text, '<LinearLayout\b[^>]*>')
        if ($match.Success) {
            $insert = '<include layout="@layout/view_demo_banner" />'
            $text = $text.Insert($match.Index + $match.Length, "`r`n    $insert")
            [System.IO.File]::WriteAllText($_.FullName, $text, [System.Text.UTF8Encoding]::new($false))
        }
    }
}
Write-Output 'Shared visual design applied to all activity layouts.'
