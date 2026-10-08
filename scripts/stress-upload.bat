@echo off
chcp 65001 >nul
setlocal

set URL=http://localhost:8080/upload
set FILE=sample.xlsx
set COUNT=50

if not exist "%FILE%" (
  echo 未找到 %FILE%，请先放置任意 xlsx 文件到项目根目录，或修改 FILE 变量。
  exit /b 1
)

echo 开始向 %URL% 连续上传 %COUNT% 次...
for /L %%i in (1,1,%COUNT%) do (
  set /a MOD=%%i %% 10
  curl -s -X POST "%URL%/user-00!MOD!" -F "file=@%FILE%" >nul
  echo [%%i/%COUNT%] upload user-00!MOD! done
)

echo.
echo 查看缓存统计:
curl -s http://localhost:8080/cache/stats
echo.
