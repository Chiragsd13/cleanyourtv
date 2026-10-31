# Restore script to re-enable packages disabled during this session
param(
    [string]$TvHost = "10.0.0.6"
)

$packages = @(
    "com.amazon.spiderpork",
    "com.amazon.ftv.screensaver",
    "me.efesser.flauncher",
    "com.spocky.projengmenu",
    "com.amazon.whisperjoin.middleware.np",
    "com.amazon.device.crashmanager",
    "com.amazon.device.logmanager",
    "com.amazon.tv.easyupgrade",
    "com.amazon.csm.htmlruntime",
    "com.amazon.uxnotification"
)

$py = "tools\venv\Scripts\python.exe"
$env:TV_HOST = $TvHost

Write-Host "Re-enabling packages on TV at $TvHost..."
foreach ($pkg in $packages) {
    Write-Host "Enabling $pkg..."
    & $py tools\tv.py "pm enable $pkg"
}

Write-Host "Done! All packages re-enabled."
