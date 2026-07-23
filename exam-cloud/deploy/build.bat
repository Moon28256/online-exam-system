@echo off
REM 构建全部服务可执行 jar（Windows）
cd /d "%~dp0\.."

echo ==^> 构建所有服务 jar（跳过测试） ...
call gradlew.bat bootJar -x test --no-daemon
if errorlevel 1 ( echo 构建失败 & pause & exit /b 1 )

echo.
echo ==^> 构建前端并拷贝至 gateway 静态资源 ...
if exist "..\..\exam-frontend\package.json" (
  pushd "..\..\exam-frontend"
  call npm install
  call npm run build
  popd
  if not exist "exam-gateway\src\main\resources\static" mkdir "exam-gateway\src\main\resources\static"
  xcopy /e /y /i "..\..\exam-frontend\dist\*" "exam-gateway\src\main\resources\static\"
  echo 前端已拷贝
) else (
  echo 未找到 exam-frontend，跳过前端构建
)

echo.
echo 完成。
pause
