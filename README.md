# 📝 在线考试与学习分析系统

> Spring Cloud 微服务 + Vue 3 + MySQL + Redis 的在线考试系统，支持学生在线考试、错题本、成绩趋势分析，教师题库/试卷管理与数据分析，管理员用户管理。

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
- 🌗 **明暗双主题**：暖珊瑚色系，主题切换带涟漪动画
- 🔐 **JWT 鉴权**：Gateway 统一校验 + Redis Token 管理
- 👤 **个人中心**：修改昵称/真实姓名、修改密码
- 📱 **单端口部署**：网关 :8080 统一对外，内网穿透即可公网访问

## 🛠 技术栈

| 层 | 技术 |
|----|------|
| 后端框架 | Spring Boot 3.3.5 · Spring Cloud 2023.0.3 · Spring Cloud Alibaba 2023.0.3.2 |
| 注册/配置 | Nacos 2.3.x |
| 网关 | Spring Cloud Gateway |
| 服务调用 | OpenFeign + LoadBalancer |
| 持久层 | MyBatis-Plus 3.5.9 · MySQL |
| 缓存/会话 | Redis（Token、热点缓存、分布式锁、限流） |
| 安全 | JWT (jjwt 0.12.6) · BCrypt |
| 前端 | Vue 3 · Vite · Element Plus · Vue Router · Axios · ECharts |
| 构建 | Gradle 8.10+ 多项目 · JDK 17 |
| 部署 | 单端口 8080 · cpolar 内网穿透 |

## 📁 目录结构

```
online-exam-system/
├── exam-cloud/                   # 后端（Spring Cloud 微服务）
│   ├── exam-common/              #   公共库（实体/Feign/JWT/全局配置）
│   ├── exam-gateway/             #   网关 :8080（路由+鉴权+限流+托管前端）
│   ├── exam-user-service/        #   用户服务 :8081
│   ├── exam-question-service/    #   题目服务 :8082
│   ├── exam-paper-service/       #   试卷服务 :8083
│   ├── exam-exam-service/        #   考试服务 :8084（编排核心）
│   ├── exam-score-service/       #   成绩服务 :8085
│   ├── exam-wrong-service/       #   错题服务 :8086
│   ├── exam-analysis-service/    #   分析服务 :8087
│   ├── nacos-config/             #   Nacos 共享配置参考
│   ├── deploy/                   #   构建/启动/停止/内网穿透脚本
│   └── README.md                 #   详细架构说明
├── exam-frontend/                # 前端（Vue 3 + Vite）
│   └── src/
├── sql/                          # 数据库脚本
│   ├── schema.sql                #   建表（8 张表）
│   └── seed.sql                  #   初始用户 + 题目数据
└── README.md                     # 本文件
```

## 🚀 本地运行

### 前置依赖

| 组件 | 说明 |
|------|------|
| **MySQL** | 9.x，默认 `localhost:3306` |
| **Redis** | 6+，默认 `127.0.0.1:6379` |
| **Nacos** | 2.3.x standalone，默认 `127.0.0.1:8848` |

### 1. 初始化数据库

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS exam_system DEFAULT CHARSET utf8mb4"
mysql -u root -p exam_system < sql/schema.sql
mysql -u root -p exam_system < sql/seed.sql
```

### 2. 构建并启动

```bash
# Windows（双击即可）
exam-cloud\deploy\build.bat      # 构建全部 jar + 前端
exam-cloud\deploy\run-all.bat    # 启动 8 个服务

# Linux / macOS
bash exam-cloud/deploy/build.sh
bash exam-cloud/deploy/run-all.sh
```

访问 **http://localhost:8080**。

停止：`exam-cloud\deploy\stop-all.bat`（或 `.sh`）。

### 3. 前端热更新开发（可选）

```bash
cd exam-frontend
npm install
npm run dev        # 默认 :5173，已代理 API 到 :8080
```

## 🌐 对外部署（发网址给别人用）

使用 cpolar 内网穿透，把本地 8080 映射成公网地址：

1. 从 https://www.cpolar.com/download 下载安装 cpolar
2. 首次使用执行：`cpolar authtoken <你的token>`
3. 双击 `exam-cloud\deploy\online.bat`（或 PowerShell 执行 `online.ps1`）
4. 终端打印 `https://xxx.cpolar.cn` 公网地址，发给别人即可访问

## 📌 默认账号

> 首次使用后请尽快修改密码（个人中心 → 修改密码）。

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 教师 | teacher1 | 123456 |
| 学生 | student1 | 123456 |

更多账号见 `sql/seed.sql`。

## 📄 许可

课程设计项目，仅供学习交流。
