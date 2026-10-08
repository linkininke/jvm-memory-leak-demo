#Requires -Version 5.1
param(
    [int]$Count = 50,
    [string]$BaseUrl = "http://localhost:8080",
    [string]$File = "sample.xlsx"
)

if (-not (Test-Path $File)) {
    Write-Error "未找到 $File，请在项目根目录放置任意 xlsx 文件"
    exit 1
}

Write-Host "连续上传 $Count 次..."
1..$Count | ForEach-Object {
    $userId = "user-{0:D3}" -f ($_ % 10)
    curl.exe -s -X POST "$BaseUrl/upload/$userId" -F "file=@$File" | Out-Null
    Write-Host "[$_/ $Count] $userId done"
}

Write-Host "`n缓存统计:"
curl.exe -s "$BaseUrl/cache/stats"
