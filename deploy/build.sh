#!/usr/bin/env bash
# ============================================================
#  在线考试系统 - 一键构建脚本 (Linux / macOS)
#  作用：构建前端 -> 拷贝 dist 到后端 static -> 构建 jar
# ============================================================
set -e
cd "$(dirname "$0")/.."

echo ""
echo "[1/3] 构建前端 (npm run build)..."
cd exam-frontend
npm run build
cd ..

echo ""
echo "[2/3] 拷贝前端产物到后端 static 目录..."
STATIC_DIR="exam-backend/exam-backend/src/main/resources/static"
mkdir -p "$STATIC_DIR"
rm -rf "$STATIC_DIR"/*
cp -r exam-frontend/dist/* "$STATIC_DIR"/
echo "    已拷贝到 $STATIC_DIR"

echo ""
echo "[3/3] 构建后端 jar (./gradlew build -x test)..."
cd exam-backend/exam-backend
chmod +x ./gradlew
./gradlew build -x test
cd ../..

echo ""
echo "============================================================"
echo " 构建完成！"
echo " jar 位置: exam-backend/exam-backend/build/libs/exam-backend-0.0.1-SNAPSHOT.jar"
echo " 运行请执行: deploy/run.sh"
echo "============================================================"
