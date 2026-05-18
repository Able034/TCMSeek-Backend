# TCMSeek Backend

TCMSeek Backend 是中医药数据平台的后端工程，提供业务数据管理、用户侧数据检索、知识图谱查询、AI 问答、会话管理和统一网关能力。后端由基于 RuoYi 开源框架开发的业务服务、AI 服务和 Gateway 服务组成，各服务通过 Nacos 注册发现，并通过 Gateway 对前端提供统一访问入口。

## 项目结构

```text
TCMSeek-Backend-main/
├─ tcmseek-web-server/    基于 RuoYi 的业务后端服务工程
├─ tcmseek-ai-service/    AI 问答微服务
├─ tcmseek-gateway/       统一网关服务

```

三个服务是平级关系：


| 服务                 | 默认端口 | 技术栈                                   | 职责                                                           |
| -------------------- | -------- | ---------------------------------------- | -------------------------------------------------------------- |
| `tcmseek-web-server` | `8080`   | RuoYi，Spring Boot 2.5.15，Java 8 target | 用户体系、登录鉴权、业务接口、后台管理、业务数据访问           |
| `tcmseek-ai-service` | `8088`   | Spring Boot 3.5.7，Java 17               | DeepSeek 问答、Neo4j 工具查询、AI 会话历史、摘要记忆、CSV 导出 |
| `tcmseek-gateway`    | `8090`   | Spring Cloud Gateway，Java 17            | 统一入口、Token 校验、用户信息注入、服务发现路由               |

`tcmseek-web-server` 是基于 RuoYi 开源项目开发的业务后端聚合工程，内部包含以下模块：


| 模块                | 说明                                                 |
| ------------------- | ---------------------------------------------------- |
| `tcmseek-admin`     | 业务后端启动入口，提供登录、鉴权、系统接口和业务聚合 |
| `tcmseek-web`       | 用户侧中医药业务接口                                 |
| `tcmseek-webmanage` | 管理侧业务接口                                       |
| `tcmseek-system`    | 用户、角色、菜单、字典、日志等系统模块               |
| `tcmseek-framework` | 安全、配置、多数据源、拦截器、异常处理               |
| `tcmseek-common`    | 通用工具、注解、实体和公共返回                       |
| `tcmseek-quartz`    | 定时任务                                             |
| `tcmseek-generator` | 代码生成                                             |

## 核心能力

- 中医药基础数据查询：中药、化合物、疾病、症状、证候、方剂等。
- 知识图谱查询：基于 Neo4j 查询中药、化合物、靶点、疾病、方剂等关系。
- AI 增强问答：DeepSeek 对话、工具调用、图谱结果摘要、长结果压缩。
- AI 会话历史：按用户隔离保存历史对话、消息、工具调用、摘要和记忆。
- CSV 导出：大结果返回摘要，完整明细通过 Gateway 下载。
- 统一网关：Frontend 通过 `/api/web/**` 与 `/api/ai/**` 访问后端能力。

## 环境要求


| 组件             | 建议版本  | 用途                           |
| ---------------- | --------- | ------------------------------ |
| JDK              | 17        | 本地统一使用 JDK 17；          |
| Maven            | 3.8+      | 构建与启动                     |
| Nacos            | 2.x       | 服务注册发现                   |
| MySQL            | 8.x       | 业务数据                       |
| Redis            | 5.x+      | 缓存、活跃上下文、临时导出结果 |
| Neo4j            | 4.x / 5.x | 中医药知识图谱                 |
| PostgreSQL       | 13+       | AI 会话、消息、摘要和记忆      |
| DeepSeek API Key | 自行申请  | AI 问答                        |

## 数据库资料

数据库初始化脚本、图谱数据、导入文件和示例数据不随仓库公开发布。

数据库初始化资料获取方式：

```text
23yyxiao@stu.edu.cn
```

各数据库用途如下：


| 数据库     | 用途                                                |
| ---------- | --------------------------------------------------- |
| MySQL      | 业务数据、用户体系、后台管理相关数据                |
| PostgreSQL | AI 会话历史、消息记录、工具调用记录、摘要和长期记忆 |
| Redis      | 登录/业务缓存、AI 活跃上下文、CSV 临时结果          |
| Neo4j      | 中医药知识图谱节点与关系数据                        |

配置文件

仓库提供示例配置文件。运行环境使用的配置文件由示例配置复制生成，并根据本地数据库、中间件和密钥信息进行调整。

```text
tcmseek-web-server/tcmseek-admin/src/main/resources/application-example.yml
  -> tcmseek-web-server/tcmseek-admin/src/main/resources/application.yml

tcmseek-web-server/tcmseek-admin/src/main/resources/application-druid-example.yml
  -> tcmseek-web-server/tcmseek-admin/src/main/resources/application-druid.yml

tcmseek-ai-service/src/main/resources/application-example.yml
  -> tcmseek-ai-service/src/main/resources/application.yml

tcmseek-gateway/src/main/resources/application-example.yml
  -> tcmseek-gateway/src/main/resources/application.yml
```

重点配置项：


| 服务                               | 配置内容                                          |
| ---------------------------------- | ------------------------------------------------- |
| `tcmseek-web-server/tcmseek-admin` | MySQL、Redis、Neo4j、Nacos、Token secret          |
| `tcmseek-ai-service`               | DeepSeek API Key、Neo4j、PostgreSQL、Redis、Nacos |
| `tcmseek-gateway`                  | Nacos、路由规则、鉴权白名单、Token 校验目标服务   |

## 启动顺序

服务启动顺序：

1. 启动 Nacos。
2. 启动 MySQL、Redis、Neo4j、PostgreSQL。
3. 启动 `tcmseek-web-server/tcmseek-admin`。
4. 启动 `tcmseek-ai-service`。
5. 启动 `tcmseek-gateway`。
6. 启动 Frontend，前端请求统一走 Gateway。

### 启动业务后端

```powershell
cd D:\TCMseek\Backend\TCMSeek-Backend-main\tcmseek-web-server
mvn clean install -DskipTests

cd D:\TCMseek\Backend\TCMSeek-Backend-main\tcmseek-web-server\tcmseek-admin
mvn spring-boot:run
```

默认地址：

```text
http://localhost:8080
```

### 启动 AI 微服务

```powershell
cd D:\TCMseek\Backend\TCMSeek-Backend-main\tcmseek-ai-service
mvn spring-boot:run
```

默认地址：

```text
http://localhost:8088
```

### 启动 Gateway

```powershell
cd D:\TCMseek\Backend\TCMSeek-Backend-main\tcmseek-gateway
mvn spring-boot:run
```

默认地址：

```text
http://localhost:8090
```

## Gateway 接口约定

Frontend 应通过 Gateway 访问后端服务。


| 前端路径                                          | 转发目标                                             | 鉴权                                 |
| ------------------------------------------------- | ---------------------------------------------------- | ------------------------------------ |
| `POST /api/web/tcmseek/login`                     | `tcmseek-admin /tcmseek/login`                       | 不需要 Gateway Token                 |
| `ANY /api/web/tcmseek/**`                         | `tcmseek-admin /tcmseek/**`                          | 普通数据查询默认不需要 Gateway Token |
| `ANY /api/web/tcmseek/tools/target-prediction/**` | `tcmseek-admin /tcmseek/tools/target-prediction/**`  | 需要用户 Token                       |
| `POST /api/ai/chat`                               | `tcmseek-ai-service /ai/chat`                        | 需要用户 Token                       |
| `POST /api/ai/aichat`                             | `tcmseek-ai-service /ai/aichat`                      | 需要用户 Token                       |
| `GET /api/ai/conversations`                       | `tcmseek-ai-service /ai/conversations`               | 需要用户 Token                       |
| `GET /api/ai/conversations/{id}/messages`         | `tcmseek-ai-service /ai/conversations/{id}/messages` | 需要用户 Token                       |
| `PATCH /api/ai/conversations/{id}`                | `tcmseek-ai-service /ai/conversations/{id}`          | 需要用户 Token                       |
| `DELETE /api/ai/conversations/{id}`               | `tcmseek-ai-service /ai/conversations/{id}`          | 需要用户 Token                       |
| `GET /api/ai/exports/{id}`                        | `tcmseek-ai-service /ai/exports/{id}`                | 需要用户 Token                       |

Gateway 校验 Token 后会向下游服务注入可信用户头：

```http
X-User-Id
X-User-Name
X-User-Account
X-User-Email
X-Request-Id
```

## 本地验收

Gateway 健康检查：

```http
GET http://localhost:8090/actuator/health
```

登录获取 Token：

```http
POST http://localhost:8090/api/web/tcmseek/login
Content-Type: application/json
```

调用 AI 问答：

```http
POST http://localhost:8090/api/ai/aichat
Authorization: <token>
Content-Type: application/json
```

查看历史对话：

```http
GET http://localhost:8090/api/ai/conversations?page=1&pageSize=20
Authorization: <token>
```

下载 CSV：

```http
GET http://localhost:8090/api/ai/exports/{exportId}
Authorization: <token>
```

## 构建命令

业务后端：

```powershell
cd D:\TCMseek\Backend\TCMSeek-Backend-main\tcmseek-web-server
mvn clean package -DskipTests
```

AI 微服务：

```powershell
cd D:\TCMseek\Backend\TCMSeek-Backend-main\tcmseek-ai-service
mvn clean package -DskipTests
```

Gateway：

```powershell
cd D:\TCMseek\Backend\TCMSeek-Backend-main\tcmseek-gateway
mvn clean package -DskipTests
```

## 开源致谢

`tcmseek-web-server` 的后台管理、用户权限、系统管理和基础工程结构基于 RuoYi 开源项目开发。TCMSeek 在此基础上扩展了中医药业务接口、知识图谱相关能力、AI 服务接入和 Gateway 统一入口。

感谢 RuoYi 项目及其社区提供的开源基础。本项目的使用、分发和二次开发应同时遵守 RuoYi 原项目的开源协议与版权声明。
