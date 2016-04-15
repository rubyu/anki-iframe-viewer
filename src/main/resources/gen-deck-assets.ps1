
$7z = "C:\Program Files\7-Zip\7z.exe"

$time = Get-Date -Format "yyMMdd"

$tsv     = "C:\wok-scripts\SVL.complete.tsv"
$tsv_zip = "SVL.$time.tsv.zip"
$media     = "C:\wok-scripts\SVL.media\*"
$media_zip = "SVL.$time.media.zip"


Start-Process $7z -ArgumentList "a $tsv_zip $tsv"
Start-Process $7z -ArgumentList "a $media_zip $media"
