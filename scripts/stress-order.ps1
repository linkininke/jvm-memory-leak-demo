#Requires -Version 5.1
param(
    [int]$Count = 1000,
    [string]$BaseUrl = "http://localhost:8080"
)

Write-Host "重置库存..."
curl.exe -s -X POST "$BaseUrl/order/reset" | Out-Null

Write-Host "并发购买 $Count 次..."
$jobs = 1..$Count | ForEach-Object {
    Start-Job -ScriptBlock {
        param($url)
        curl.exe -s -X POST $url | Out-Null
    } -ArgumentList "$BaseUrl/order/buy"
}
$jobs | Wait-Job | Out-Null
$jobs | Remove-Job

Write-Host "`n库存状态:"
curl.exe -s "$BaseUrl/order/stock"
