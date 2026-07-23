#!/usr/bin/env bash
# 停止全部服务
set -euo pipefail
cd "$(dirname "$0")/.."
LOG="$(pwd)/deploy/logs"
for pidf in "$LOG"/*.pid; do
  [ -f "$pidf" ] || continue
  pid=$(cat "$pidf")
  if kill -0 "$pid" 2>/dev/null; then
    echo "停止 $pid ..."
    kill "$pid"
  fi
  rm -f "$pidf"
done
echo "已停止。"
