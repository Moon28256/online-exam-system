#!/usr/bin/env bash
# 启动全部服务（需先运行 build.sh，且 Nacos/MySQL/Redis 已就绪）
set -euo pipefail
cd "$(dirname "$0")/.."
ROOT=$(pwd)
VER=0.0.1-SNAPSHOT
LOG="$ROOT/deploy/logs"
mkdir -p "$LOG"

start() {
  local name=$1
  local jar="$ROOT/$name/build/libs/$name-$VER.jar"
  if [ ! -f "$jar" ]; then echo "缺少 $jar，请先运行 deploy/build.sh"; exit 1; fi
  echo "启动 $name ..."
  nohup java -jar "$jar" > "$LOG/$name.log" 2>&1 &
  echo $! > "$LOG/$name.pid"
}

# 业务服务先行（向 Nacos 注册），网关后行
start exam-user-service
start exam-question-service
start exam-paper-service
start exam-exam-service
start exam-score-service
start exam-wrong-service
start exam-analysis-service
start exam-gateway

echo "全部已启动。网关端口 8080。日志: $LOG/*.log"
