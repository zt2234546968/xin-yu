# 跨境电商管理系统

本仓库当前聚焦两个项目：

- `backend`: Java 17 + Spring Boot 后端服务，统一接口前缀为 `/api`。
- `leading`: Vue 3 + TypeScript + Element Plus 前端管理系统。

`ixBrowser` 是自动化工具，本轮整理与优化未纳入前后端改造范围。

## 已完成能力

- 用户登录、注册、用户列表、用户资料修改、密码修改。
- 角色初始化、角色查询、角色创建、更新和删除。
- 邀请码生成、校验、查询、列表和备注维护。
- 国家字典列表、详情、按名称查询和固定国家初始化；国家为固定字典，业务上不允许新增、修改和删除。
- 直评任务列表、详情、创建、更新、状态更新、反馈信息维护、删除和编号生成。
- 测评任务列表、详情、创建、更新、删除和编号生成。
- 测评订单创建、列表、详情、按测评任务查询、更新、软删除。
- 管理端菜单包含首页、任务中心、邀请码、用户管理、财务中心和个人中心。

## 技术栈

后端参考 `D:\LanTu\life-cycle-management-system\bossChat\boss-chat-server`：

- Spring Boot 3.3.5
- Java 17
- Spring Web
- Spring Data JPA
- MyBatis Plus 3.5.9
- Flyway
- MySQL
- springdoc-openapi

前端参考 `D:\LanTu\life-cycle-management-system\bossChat\boss-chat-web`：

- Vue 3
- TypeScript
- Vite 6
- Element Plus
- Vue Router 4
- Pinia
- Axios

## 快速启动

后端：

```bash
cd backend
mvn spring-boot:run
```

前端：

```bash
cd leading
npm install
npm run dev
```

默认访问：

- 前端开发地址: `http://localhost:9000`
- 后端接口地址: `http://localhost:8080/api`
- Swagger: `http://localhost:8080/api/swagger-ui.html`

## 当前业务约定

- 国家为固定字典，美国必须排在第一；后端会自动初始化固定国家，并隐藏非固定国家数据。
- 当前未接入图床，所有任务图片、反馈图片和国家图标先统一使用前端静态资源 `/test.jpg`。
- 数据库结构由 Flyway 管理，新增或调整字段应继续放到 `backend/src/main/resources/db/migration`。
- 前端构建产物 `leading/dist` 不纳入源码维护，需要时通过 `npm run build` 重新生成。

更多说明见 [开发文档/文档导航.md](开发文档/文档导航.md)。
