#Requires -Version 5.1
param(
    [int]$Count = 20,
    [ValidateSet("mixed", "young", "full")]
    [string]$Target = "full",
    [string]$BaseUrl = "http://localhost:8080"
)

Write-Host "GC 压测: target=$Target, count=$Count"
1..$Count | ForEach-Object {
    curl.exe -s "$BaseUrl/gc/test?target=$Target" | Out-Null
    Write-Host "[$_/ $Count] done"
    Start-Sleep -Milliseconds 200
}

Write-Host "`n持有区统计:"
curl.exe -s "$BaseUrl/gc/stats"
