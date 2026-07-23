# exam-cloud —— 在线考试系统（Spring Cloud 版）

原 `exam-backend` 单体 Spring Boot 工程的微服务化重构：**功能与界面不变**，后端拆分为
Spring Cloud 多服务，引入 Nacos（注册+配置）、Spring Cloud Gateway、OpenFeign、Redis、MyBatis-Plus、MySQL。

## 技术栈

| 组件 | 版本 / 选型 |
|---|---|
| Spring Boot | 3.3.5 |
| Spring Cloud | 2023.0.3 |
| Spring Cloud Alibaba | 2023.0.3.2 |
| Nacos | 2.3.x（外部服务端） |
| 注册/配置中心 | Nacos discovery + config |
| 网关 | Spring Cloud Gateway（WebFlux） |
| 服务间调用 | OpenFeign + Spring Cloud LoadBalancer |
| 缓存/会话/限流 | Spring Data Redis |
| ORM | MyBatis-Plus 3.5.9 |
| 数据库 | MySQL（共享 `exam_system` 库） |
| 构建 | Gradle 8.10+ 多项目 |
| JDK | 17 |

## 模块结构

```
exam-cloud/
├── exam-common          公共库：Result/实体/DTO/JwtUtil/Feign 客户端/全局配置
├── exam-gateway         网关（8080）：路由 + JWT/Redis 鉴权 + 限流 + 托管前端
├── exam-user-service    用户（8081）：/user/**  注册/登录/个人中心 + 签发并写 Redis Token
├── exam-question-service 题目（8082）：/question/**  题库 + 列表缓存 + 内部端点
├── exam-paper-service   试卷（8083）：/paper/**  组卷 + Feign 取题目 + 缓存 + 内部端点
├── exam-exam-service    考试（8084）：/exam/**  开考/答题/交卷编排 + Redis 进度/锁 + 重试队列
├── exam-score-service   成绩（8085）：/score/**  成绩快照（幂等）+ ZSet 排行榜 + 缓存
├── exam-wrong-service   错题（8086）：/wrong/**  错题本 + 内部批量维护（幂等）
├── exam-analysis-service 分析（8087）：/analysis/**  只读跨表统计 + 缓存
├── nacos-config/        Nacos 共享配置 exam-shared.yml（参考）
└── deploy/              构建/启动/停止脚本
```

## 架构要点

- **唯一外部端口 8080（网关）**：前端零改动，仍同源部署。网关按路径前缀 `lb://` 路由到各服务。
- **鉴权集中**：登录由 user-service 签发 JWT 并写入 Redis（`login:token:<userId>`）；网关全局过滤器
  校验 JWT + Redis 存在性，通过后把 `X-User-Id/X-Username/X-Role` 注入下游请求头。下游经
  `UserHeaderFilter`（common.web）转为 request 属性，使旧版 `@RequestAttribute("userId")` 无需改动。
- **跨服务一致性（尽力而为 + 重试）**：交卷时 exam-service 本地事务写 exam_record/answer_record，
  再经 Feign 调 score-service / wrong-service；失败入 Redis 重试队列，`@Scheduled` 退避重放。
  score / wrong 内部端点按 `examRecordId` 幂等（Redis guard），重试不产生副作用。
- **Redis 四项用途**：Token/会话、业务热点缓存（Spring Cache）、考试进度+交卷锁、排行榜 ZSet + 接口限流。
- **共享库**：保留 `exam_system` 单库与外键/跨表 JOIN（analysis 只读聚合），各服务仅操作自有表。
  物理拆库会破坏外键与分析 JOIN，故不拆。

## 启动依赖

启动服务前需本地就绪：

1. **MySQL** 9.x，建库并执行 `sql/schema.sql` + `sql/seed.sql`（沿用原仓库脚本）。
2. **Redis** 6+，默认 `127.0.0.1:6379`。
3. **Nacos** 2.3.x，standalone 模式：`sh startup.sh -m standalone`（默认 `127.0.0.1:8848`）。
   - 可选：把 `nacos-config/exam-shared.yml` 导入 Nacos（data-id `exam-shared.yml`）集中管理 DB/Redis/JWT。
   - 不导入也能启动：各服务 `application.yml` 已含默认值，可用环境变量覆盖。

## 构建

```bash
cd exam-cloud
deploy/build.sh        # 构建 8 个可执行 jar + 前端 dist 拷入 gateway/static
# 或仅构建后端：./gradlew bootJar -x test
```

## 运行

```bash
deploy/run-all.sh      # 后台启动 7 服务 + 网关（日志在 deploy/logs/*.log）
deploy/stop-all.sh
```
Windows 用 `deploy\build.bat` / `run-all.bat`。

访问：浏览器打开 `http://localhost:8080`（前端 hash 路由，与原版一致）。
健康检查：`GET http://localhost:8080/test`。

## 环境变量（可选覆盖）

| 变量 | 默认 | 说明 |
|---|---|---|
| `NACOS_ADDR` | 127.0.0.1:8848 | Nacos 地址 |
| `NACOS_NS` | public | Nacos 命名空间 |
| `DB_URL` / `DB_USER` / `DB_PASSWORD` | localhost / root / mzls6666mo | MySQL |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` / `REDIS_DB` | 127.0.0.1 / 6379 / / 0 | Redis |
| `JWT_SECRET` | OnlineExamSystem2026SecretKeyForJWT!! | user-service 与 gateway 必须一致 |
| `JWT_EXPIRATION` | 86400000 | Token 有效期（ms） |

## 端口分配

8080 网关 / 8081 用户 / 8082 题目 / 8083 试卷 / 8084 考试 / 8085 成绩 / 8086 错题 / 8087 分析。
