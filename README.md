# Cloud Takeout

基于 Spring Cloud 的外卖微服务实战项目，覆盖从传统业务链路到 AI Agent 点餐助手的完整实现。

- 微服务拆分与网关路由
- JWT 鉴权与服务间调用
- Redis 缓存与缓存一致性
- RabbitMQ 异步解耦
- Sentinel 熔断降级
- Vue3 多页面前端
- Spring AI + Tool 调用的订单对话机器人

---

## 技术栈

### 后端
- Java 17
- Spring Boot 2.7.x（核心业务服务）
- Spring Cloud 2021.x + Spring Cloud Alibaba 2021.x
- Spring Cloud Gateway
- OpenFeign + LoadBalancer
- MyBatis-Plus
- Nacos（服务注册发现）
- Sentinel（限流/熔断）
- Redis（商品缓存）
- RabbitMQ（订单异步事件）
- MySQL 8（业务持久化）

### AI Agent 服务
- Spring Boot 3.4.x（独立模块）
- Spring AI 1.0.0-M6
- OpenAI 兼容接口
- Tool Calling + ChatMemory + Prompt 工作流编排

### 前端
- Vue 3 + Vite
- Vue Router
- Axios

---

## 项目结构

```text
Cloud
├─ cloud-common              # 通用响应体 ApiResponse
├─ cloud-gateway             # API 网关 (8080)
├─ cloud-service-auth        # 登录/注册 + JWT 生成 (9000)
├─ cloud-service-user        # 用户资料/口味偏好 + MQ 消费 (9001)
├─ cloud-service-product     # 菜品查询 + Redis 缓存 (9002)
├─ cloud-service-order       # 下单服务 + Feign + MQ 生产者 (9003)
├─ cloud-service-agent       # AI 点餐助手 Agent (9010)
├─ frontend-vue3             # 前端应用 (5173)
└─ infra
   ├─ docker-compose.yml     # Nacos/MySQL/Redis/RabbitMQ/Sentinel/Nginx
   ├─ nginx/default.conf     # 统一入口反代配置
   └─ sql/01-init-db.sql     # 初始化数据库与样例数据
```

---

## 核心功能

### 1) 账号体系与用户中心
- 登录、注册
- JWT 鉴权
- 用户资料修改（昵称、手机号、头像、简介）
- 用户口味偏好维护（支持 Agent 推荐）

### 2) 菜品与点餐
- 菜品列表/详情展示
- 下单创建订单
- 订单列表查询
- 下单后同步更新销量与库存

### 3) 缓存优化（商品服务）
- 缓存完整商品对象（不是仅缓存名称）
- TTL 随机抖动防雪崩
- 空值缓存防穿透
- 下单后主动失效商品缓存
- 缓存逻辑统一封装在 `ProductCacheService`

### 4) 异步消息
- 订单服务下单后发送 `takeout.order.created` 事件
- 用户服务监听队列消费，演示解耦

### 5) AI 点餐助手（Order Agent）
- 多轮对话收集用户需求
- 先确认口味（可查询用户偏好）
- 查询菜单并推荐 3 个可能喜欢的菜品
- 收集菜品与数量并二次确认
- 通过工具调用逐条创建订单
- 完成后询问是否继续点餐

---

## 业务流程

### 传统点餐链路
1. 前端调用 `/api/auth/login` 获取 token
2. 网关 `JwtAuthGlobalFilter` 校验 Authorization
3. 前端调用 `/api/products`、`/api/orders`
4. 订单服务通过 Feign 校验用户/商品并创建订单
5. 订单服务同步更新商品销量与库存
6. 订单服务发送 MQ 事件，用户服务消费

### AI Agent 点餐链路
1. 前端 Agent 页面调用 `/api/agent/chat`
2. `cloud-service-agent` 使用 Prompt + Memory + Tools 执行流程
3. Tool 调用用户、商品、订单服务接口
4. 完成推荐、确认、下单的完整对话闭环

---

## 实现原理与技术亮点

### 1) 网关统一鉴权
- 所有业务请求先过网关
- 登录接口白名单放行，其他接口要求 Bearer Token
- 降低下游服务重复鉴权成本

### 2) 服务调用稳定性
- Feign + LoadBalancer 做服务间调用
- Sentinel 提供熔断降级兜底
- 出现下游故障时前端可收到可读错误提示

### 3) 缓存一致性策略
- 读路径：先查 Redis，未命中回源 DB 并写缓存
- 写路径：下单后删商品缓存 key，保证后续读取新数据
- Redis 异常时不阻断主流程，自动降级回源 DB

### 4) AI Agent 模块化设计
- `AgentConfiguration` 统一组装 `ChatClient`
- `SystemConstants` 承载流程型 Prompt
- `OrderAgentTools` 聚合跨服务工具调用
- `ChatHistoryRepository + ChatMemory` 支持多会话记忆

### 5) 前端多页面架构
- 登录/注册、点餐、订单、个人中心、点餐助手拆分为独立页面
- 路由守卫实现登录态控制
- Agent 页面采用固定大小聊天窗口 + 会话列表，交互稳定

---

## 运行环境

- JDK 17
- Maven 3.8+
- Node.js 18+
- Docker Desktop

---

## 启动步骤

### 1) 启动基础中间件
```bash
cd infra
docker compose up -d
```

关键地址：
- Nacos: [http://localhost:8848/nacos](http://localhost:8848/nacos) (`nacos/nacos`)
- RabbitMQ: [http://localhost:15672](http://localhost:15672) (`guest/guest`)
- Sentinel: [http://localhost:8858](http://localhost:8858)
- Nginx 入口: [http://localhost](http://localhost)

### 2) 初始化数据库
执行 `infra/sql/01-init-db.sql`

> 注意：该脚本包含重建表逻辑，建议在开发环境使用。

### 3) 启动后端服务（建议顺序）
```bash
mvn -pl cloud-service-user spring-boot:run
mvn -pl cloud-service-product spring-boot:run
mvn -pl cloud-service-order spring-boot:run
mvn -pl cloud-service-auth spring-boot:run
mvn -pl cloud-service-agent spring-boot:run
mvn -pl cloud-gateway spring-boot:run
```

### 4) 启动前端
```bash
cd frontend-vue3
npm install
npm run dev
```

访问：
- 前端开发地址：`http://localhost:5173`
- Nginx 统一入口：`http://localhost`

---

## AI Agent 配置说明

`cloud-service-agent` 使用 OpenAI 兼容接口，当前示例配置为：
- `base-url: https://dashscope.aliyuncs.com/compatible-mode`
- `model: qwen3-max`
- `api-key: ${OPENAI_API_KEY}`

请在启动前设置环境变量：
```bash
# PowerShell
$env:OPENAI_API_KEY="your_api_key"
```

---

## 常用接口

### 认证与基础业务
- `POST /api/auth/login`
- `POST /api/auth/register`
- `GET /api/products`
- `POST /api/orders`
- `GET /api/orders/user/{userId}`
- `GET /api/users/{id}`
- `PUT /api/users/profile/{id}`

### Agent
- `GET /api/agent/chat?prompt=xxx&conversationId=xxx`
- `GET /api/agent/history/order-agent`
- `GET /api/agent/history/order-agent/{conversationId}`

---




