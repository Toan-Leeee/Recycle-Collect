$ErrorActionPreference = 'Stop'
$layoutDir = Join-Path $PSScriptRoot '..\app\src\main\res\layout'
$settings = New-Object System.Xml.XmlWriterSettings
$settings.Indent = $true
$settings.IndentChars = '    '
$settings.NewLineChars = "`r`n"
$settings.NewLineHandling = [System.Xml.NewLineHandling]::Entitize
Get-ChildItem -LiteralPath $layoutDir -Filter '*.xml' | ForEach-Object {
    $raw = Get-Content -LiteralPath $_.FullName -Raw -Encoding UTF8
    [xml]$doc = $raw
    $root = $doc.DocumentElement
    if (-not $root.HasAttribute('background', 'http://schemas.android.com/apk/res/android')) {
        $root.SetAttribute('background', 'http://schemas.android.com/apk/res/android', '@color/surface')
    }
    $writer = New-Object System.IO.StringWriter
    $xmlWriter = [System.Xml.XmlWriter]::Create($writer, $settings)
    $doc.Save($xmlWriter)
    $xmlWriter.Dispose()
    $xmlText = $writer.ToString() -replace 'encoding="utf-16"', 'encoding="utf-8"'
    [System.IO.File]::WriteAllText($_.FullName, $xmlText + "`r`n", [System.Text.UTF8Encoding]::new($false))
}
Write-Output 'Formatted layout XML files.'
