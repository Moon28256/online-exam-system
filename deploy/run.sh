#!/usr/bin/env bash
# ============================================================
#  在线考试系统 - 运行脚本 (Linux / macOS)
#  作用：启动后端 jar（同时托管前端 + API，单端口 8080）
#  数据库连接可通过环境变量覆盖，见下
# ============================================================
cd "$(dirname "$0")/.."

# --- 数据库连接配置（部署到他人电脑时可在此修改）---
export DB_URL="jdbc:mysql://localhost:3306/exam_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8"
export DB_USER="root"
export DB_PASSWORD="mzls6666mo"

echo "正在启动在线考试系统 (端口 8080)..."
echo "访问地址: http://localhost:8080"

JAR=$(ls exam-backend/exam-backend/build/libs/exam-backend-0.0.1-SNAPSHOT.jar 2>/dev/null | head -1)
if [ -z "$JAR" ]; then
    echo "[错误] 未找到 jar，请先运行 deploy/build.sh"
    exit 1
fi

java -jar "$JAR"
