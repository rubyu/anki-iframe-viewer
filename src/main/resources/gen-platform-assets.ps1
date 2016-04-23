
$7z = "C:\Program Files\7-Zip\7z.exe"

$devices = @("PC", "Nexus7", "SO-03G")

$cssA  = "anki-iframe-viewer.css"
$_cssB = "post.css"
$_cssC = "col-detach.css"

$jsA   = "anki-iframe-viewer-opt.js"
$_jsB  = "post.js"
$_jsC  = "col-detach.js"

foreach($device in $devices) {
  $cssB = Join-Path $device $_cssB
  $cssC = Resolve-Path (Join-Path $device $_cssC)
  $jsB  = Join-Path $device $_jsB
  $jsC  = Resolve-Path (Join-Path $device $_jsC)

  Get-Content -Encoding UTF8 $cssA | Set-Content -Encoding UTF8 $cssC
  Get-Content -Encoding UTF8 $cssB | Add-Content -Encoding UTF8 $cssC
  Get-Content -Encoding UTF8 $jsA  | Set-Content -Encoding UTF8 $jsC
  Get-Content -Encoding UTF8 $jsB  | Add-Content -Encoding UTF8 $jsC

  $dst = "$device.zip"
  if (Test-Path $dst) {
    Remove-Item $dst
  }

  # `sources` for 7z, $cssC and $jsC here, must be an absolute path. Otherwise, 
  # the generated zip file should contain the directory stracture to the source file.
  Start-Process $7z -ArgumentList "a $dst $cssC $jsC"
}
