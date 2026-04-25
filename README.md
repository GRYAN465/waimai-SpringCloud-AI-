# Cloud Takeout Demo


- Spring Boot + Spring MVC 构建服务
- MyBatis-Plus 统一数据库读写
- Spring Cloud Alibaba (Nacos) 做服务注册与发现
- Spring Cloud Gateway 做统一网关
- Nginx 做反向代理统一入口
- JWT + Gateway GlobalFilter 做统一鉴权
- OpenFeign 做服务间调用
- Sentinel 做限流与熔断
- RabbitMQ 做异步消息
- Redis 做缓存
- MySQL 做持久化
- Vue3 + Vite 做前端调用网关

## 1. 项目结构

```text
Cloud
├─ cloud-common             # 通用响应对象
├─ cloud-gateway            # API网关(8080)
├─ cloud-service-auth       # 认证服务(9000)
├─ cloud-service-user       # 用户服务(9001)
├─ cloud-service-product    # 商品服务(9002)
├─ cloud-service-order      # 订单服务(9003)
├─ infra
│  ├─ docker-compose.yml    # Nacos/MySQL/Redis/RabbitMQ/Sentinel/Nginx
│  ├─ nginx/default.conf    # Nginx 反向代理配置
│  └─ sql/01-init-db.sql    # 初始化库表和测试数据
└─ frontend-vue3            # Vue3前端(5173)
```

## 2. 业务流程（下单）

1. 前端先调用 `/api/auth/login` 获取 JWT。
2. 前端携带 `Authorization: Bearer <token>` 访问业务接口。
3. 网关统一校验 JWT，校验通过后路由到目标微服务。
4. 订单服务用 OpenFeign 调用用户服务和商品服务校验数据。
5. 订单服务通过 Sentinel 执行限流和熔断保护。
6. 订单服务写入 MySQL，并发布 `takeout.order.created` 到 RabbitMQ。
7. 用户服务监听队列并消费消息（演示异步解耦）。

## 3. 启动基础中间件

先进入 `infra` 目录，再执行：

```bash
docker compose up -d
```

启动后关键地址：

- Nacos: [http://localhost:8848/nacos](http://localhost:8848/nacos) (默认 nacos / nacos)
- RabbitMQ 管理台: [http://localhost:15672](http://localhost:15672) (guest / guest)
- Sentinel Dashboard: [http://localhost:8858](http://localhost:8858) (默认 sentinel / sentinel)
- Nginx 统一入口: [http://localhost](http://localhost)

## 4. 启动后端服务

在项目根目录执行（按顺序更易观察）：

```bash
mvn -pl cloud-service-user spring-boot:run
mvn -pl cloud-service-product spring-boot:run
mvn -pl cloud-service-order spring-boot:run
mvn -pl cloud-service-auth spring-boot:run
mvn -pl cloud-gateway spring-boot:run
```

> 如果你习惯 IDE，可以分别导入每个模块后直接运行各自的 `*Application` 主类。

## 5. 启动前端

```bash
cd frontend-vue3
npm install
npm run dev
```

开发模式直接访问 [http://localhost:5173](http://localhost:5173)。

也可以走 Nginx 统一入口 [http://localhost](http://localhost)：

- `/` 转发到前端 dev server `5173`
- `/api/**` 转发到网关 `8080`

## 6. 快速验证接口

通过网关访问（先登录再带 token 调业务）：

- 登录获取 token：`POST http://localhost:8080/api/auth/login`
- 查询用户：`GET http://localhost:8080/api/users/1`
- 查询商品：`GET http://localhost:8080/api/products/1`
- 创建订单：`POST http://localhost:8080/api/orders`

登录请求体示例：

```json
{
  "username": "demo",
  "password": "123456"
}
```

请求体示例：

```json
{
  "userId": 1,
  "productId": 1,
  "count": 2
}
```

业务接口请求头示例：

```text
Authorization: Bearer <上一步返回的token>
```

如果走 Nginx，也可以用以下地址：

- `POST http://localhost/api/auth/login`
- `GET http://localhost/api/users/1`
- `GET http://localhost/api/products/1`
- `POST http://localhost/api/orders`

