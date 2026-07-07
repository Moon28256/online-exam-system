# 📝 在线考试与学习分析系统

> Spring Boot 3 + Vue 3 + MySQL 的在线考试系统，支持学生在线考试、错题本、成绩趋势分析，教师题库/试卷管理与数据分析，管理员用户管理。

## ✨ 功能特性

### 学生端
- **在线考试**：单选/多选/判断/填空四种题型，答题卡导航、整卷预览确认交卷、倒计时
- **我的成绩**：成绩列表，支持平均分升降序排序
- **错题本**：主界面精简展示，点开查看题目详情（含全部选项与正确答案）
- **成绩趋势**：折线图展示历次考试分数走势

### 教师端
- **题库管理**：增删改查题目，按「我的 / 他人 / 全部」筛选；非创建者仅可查看详情
- **试卷管理**：组卷、发布/取消发布，同样支持归属筛选与只读查看
- **成绩排名**：班级/科目成绩排行
- **数据分析**：试卷难度、得分分布等可视化

### 管理员端
- **用户管理**：按角色筛选 + 搜索 + 统计卡片
- 拥有教师全部权限（可绕过归属限制管理所有题目/试卷）

### 通用
- 🌗 **明暗双主题**：暖珊瑚色系，主题切换带涟漪动画，夜间模式保证考试文字清晰
- 🔐 **JWT 鉴权**：登录态基于 token，雪花 ID 经 Jackson 转 String 防止精度丢失
- 👤 **个人中心**：修改昵称/真实姓名、修改密码
- 📱 **单端口部署**：后端同时托管前端静态资源与 API，内网穿透即可对外访问

## 🛠 技术栈

| 层 | 技术 |
|----|------|
| 后端 | Spring Boot 3.4.5 · JDK 17 · Gradle (Groovy DSL) |
| 持久层 | MyBatis-Plus 3.5.9 · MySQL 9 |
| 安全 | JWT (jjwt 0.12.6) · BCrypt |
| 前端 | Vue 3 · Vite · Element Plus · Vue Router · Axios · ECharts |
| 部署 | 单 jar 同端口 · cpolar 内网穿透 |

## 📁 目录结构

```
online-exam-system/
├── exam-backend/                # 后端
│   └── exam-backend/
│       ├── build.gradle
│       └── src/main/
│           ├── java/com/exam/backend/
│           └── resources/
│               ├── application.properties   # 数据库连接支持环境变量
│               └── static/                  # 前端构建产物（部署时填入）
├── exam-frontend/               # 前端
│   ├── .env.development         # VITE_API_BASE=http://localhost:8080
│   ├── .env.production          # VITE_API_BASE=（同源）
│   └── src/
├── sql/                         # 数据库脚本
│   ├── schema.sql               # 建表
│   └── seed.sql                 # 初始用户 + 题目数据
└── deploy/                      # 部署脚本
    ├── build.bat / build.sh     # 构建前端+拷贝+打 jar
    ├── run.bat / run.sh         # 运行 jar
    └── 部署说明.md              # 完整部署与内网穿透教程
```

## 🚀 本地运行

### 1. 准备数据库
```bash
# 登录 MySQL 后执行
mysql> CREATE DATABASE exam_system DEFAULT CHARACTER SET utf8mb4;
mysql> USE exam_system;
mysql> source sql/schema.sql;
mysql> source sql/seed.sql;
```
> 默认连接：`localhost:3306/exam_system`，用户 `root`，密码见 `application.properties` 中的 `${DB_PASSWORD:...}` 默认值。如需修改，设置环境变量 `DB_URL` / `DB_USER` / `DB_PASSWORD` 即可。

### 2. 一键构建并运行
```bash
# Windows
deploy\build.bat
deploy\run.bat

# Linux / macOS
bash deploy/build.sh
bash deploy/run.sh
```
访问 **http://localhost:8080** 即可。

### 3. 分别开发（可选）
- 前端热更新：`cd exam-frontend && npm install && npm run dev`（默认 5173，已配置代理到 8080）
- 后端：`cd exam-backend/exam-backend && ./gradlew bootRun`

## 🌐 对外部署（发网址给别人用）

见 [deploy/部署说明.md](deploy/部署说明.md) —— 用 cpolar 内网穿透把本地 8080 端口映射成公网网址，免费、无需服务器。

## 📌 默认账号

> 首次初始化后请尽快修改密码（个人中心 → 修改密码）。

| 角色 | 用户名 | 说明 |
|------|--------|------|
| 管理员 | 见 sql/seed.sql | 拥有全部权限 |
| 教师 | teacher1 等 | 题库/试卷创建者 |
| 学生 | 见 seed 数据 | 参加考试 |

## 📄 许可

课程设计项目，仅供学习交流。
