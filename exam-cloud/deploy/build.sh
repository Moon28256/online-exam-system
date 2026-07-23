#!/usr/bin/env bash
# 构建全部服务可执行 jar
set -euo pipefail
cd "$(dirname "$0")/.."

echo "==> 构建所有服务 jar (跳过测试) ..."
./gradlew bootJar -x test --no-daemon

echo "==> 构建前端并拷贝至 gateway 静态资源 ..."
FE="../../exam-frontend"
if [ -d "$FE" ]; then
  ( cd "$FE" && npm install && npm run build )
  mkdir -p exam-gateway/src/main/resources/static
  cp -r "$FE/dist/." exam-gateway/src/main/resources/static/
  echo "前端已拷贝至 exam-gateway/src/main/resources/static/"
else
  echo "未找到 $FE，跳过前端构建（网关将返回 404 提示）"
fi

echo "完成。"
