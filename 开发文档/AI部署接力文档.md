# AI 部署接力文档

本文用于让后续接手的 AI 或开发者快速理解 `XinYu` 项目的当前部署状态、最终端口规划、关键配置文件和容易踩坑的位置。接手时优先阅读本文，再看根目录 `Jenkins本机部署完整复刻流程.md`。

## 项目范围

- 本仓库只关注 `backend` 和 `leading`。
- `backend` 是 Spring Boot 后端，最终由 systemd 服务 `xinyu-backend` 管理。
- `leading` 是 Vue 3 + TypeScript + Element Plus 前端，构建后由 Nginx 托管。
- 原来的自动化工具已移出为独立项目，不参与本项目部署。

## 当前服务器

```text
公网 IP：8.148.7.94
Jenkins：http://8.148.7.94:8080/
前端：http://8.148.7.94:8000/
后端 Swagger：http://8.148.7.94:8081/api/swagger-ui/index.html
```

注意：`http://8.148.7.94/` 是 80 端口，会命中服务器已有的面板默认站点或旧站点，不是本项目。当前项目的前端入口是 `http://8.148.7.94:8000/`。

## 最终端口规划

```text
22     SSH
80     服务器原有面板默认站点或旧站点，当前项目不使用
8000   XinYu 前端 Nginx 站点
8080   Jenkins 管理页面
8081   XinYu 后端 Spring Boot 服务
3306   MySQL，本机或内网访问
```

如果浏览器打不开 `http://8.148.7.94:8000/`，先检查阿里云安全组是否已放行 TCP `8000`。

## 部署链路

```text
GitHub main
  -> Jenkins xinyu-deploy
  -> Maven 构建 backend
  -> npm 构建 leading
  -> Jar 复制到 /opt/xinyu/app
  -> dist 复制到 /opt/xinyu/web
  -> systemctl restart xinyu-backend
  -> Nginx 通过 8000 提供前端
  -> Nginx 将 /api/ 代理到 127.0.0.1:8081/api/
```

## 服务器环境

当前服务器已确认：

```text
Java：Alibaba Dragonwell OpenJDK 21
Maven：Apache Maven 3.9.9，目录 /opt/maven
Node.js：v20.20.2
npm：10.8.2
Nginx：1.26.2，面板安装路径 /www/server/nginx
Git：2.43.7
MySQL：8.0.46 MySQL Community Server
```

Jenkins shell 不一定会加载 `/etc/profile.d/maven.sh`，所以 Jenkinsfile 中固定使用 `/opt/maven/bin/mvn`。

## 关键服务器文件

```text
/opt/xinyu/app/cross-border-ecommerce-backend-1.0.0.jar
/opt/xinyu/web/index.html
/opt/xinyu/web/assets
/opt/xinyu/web/test.jpg
/opt/xinyu/config/application-local.yml
/etc/systemd/system/xinyu-backend.service
/etc/sudoers.d/xinyu-jenkins
/www/server/panel/vhost/nginx/xinyu.conf
```

`/opt/xinyu/config/application-local.yml` 是服务器本地配置，包含数据库账号密码，不要提交到 GitHub。不同服务器应单独维护该文件。

## 后端本地配置

服务器当前使用：

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/xin_yu_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: xinyu
    password: 132321
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    validate-on-migrate: false
```

当前数据库：

```text
库名：xin_yu_db
用户：xinyu
密码：132321
MySQL root 密码：132321
```

这是测试服务器口令，生产环境应改成强密码，并限制 MySQL 只允许本机或内网访问。

## systemd 服务

```ini
[Unit]
Description=XinYu Cross-border E-commerce Backend
After=network.target mysqld.service mysql.service

[Service]
Type=simple
WorkingDirectory=/opt/xinyu
ExecStart=/usr/bin/java -jar /opt/xinyu/app/cross-border-ecommerce-backend-1.0.0.jar --spring.config.additional-location=file:/opt/xinyu/config/application-local.yml
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```

后端确认命令：

```bash
sudo systemctl status xinyu-backend --no-pager
sudo journalctl -u xinyu-backend -n 200 --no-pager
sudo ss -lntp | grep ':8081'
```

## Nginx 配置

当前服务器不是标准 `/etc/nginx/conf.d` 结构，而是面板 Nginx：

```text
主配置：/www/server/nginx/conf/nginx.conf
站点目录：/www/server/panel/vhost/nginx
项目配置：/www/server/panel/vhost/nginx/xinyu.conf
```

项目配置应为：

```nginx
server {
    listen 8000;
    server_name 8.148.7.94;

    root /opt/xinyu/web;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8081/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

修改后执行：

```bash
sudo nginx -t
sudo systemctl restart nginx
sudo systemctl status nginx --no-pager
sudo ss -lntp | grep ':8000'
curl -I http://127.0.0.1:8000/
```

如果访问 `http://8.148.7.94/` 出现 WordPress 数据库错误或“站点创建成功”，说明命中的是 80 端口默认站点，不代表本项目前端部署失败。

## Jenkins 配置

Jenkins 任务：

```text
任务名称：xinyu-deploy
类型：Pipeline
SCM：Git
仓库：https://github.com/zt2234546968/xin-yu.git
分支：*/main
Script Path：Jenkinsfile
```

Jenkinsfile 已做过这些修正：

- 使用 `/opt/maven/bin/mvn`，避免 Jenkins 找不到 `mvn`。
- `Deploy` 阶段复制后端 Jar 和前端 dist。
- 重启 `xinyu-backend` 后等待 12 秒再检查状态，避免服务刚启动就误判成功。

Jenkins 需要免密执行：

```text
jenkins ALL=(ALL) NOPASSWD: /usr/bin/systemctl restart xinyu-backend, /usr/bin/systemctl status xinyu-backend --no-pager
```

验证 sudoers：

```bash
sudo visudo -cf /etc/sudoers.d/xinyu-jenkins
sudo cat /etc/sudoers.d/xinyu-jenkins
```

## 常见问题和处理

### Jenkins 构建提示 mvn not found

原因：Jenkins shell 没加载登录用户环境变量。

处理：确认 Jenkinsfile 使用 `/opt/maven/bin/mvn clean package -DskipTests`。

### Jenkins sudo 需要密码

原因：`/etc/sudoers.d/xinyu-jenkins` 中允许的命令和 Jenkinsfile 实际命令不完全一致。

处理：sudoers 必须包含 `status xinyu-backend --no-pager`，参数也要一致。

### 后端提示 8080 被占用

原因：Jenkins 正在使用 8080。

处理：后端固定使用 8081，并确认 `/opt/xinyu/config/application-local.yml` 中是：

```yaml
server:
  port: 8081
```

同时 Nginx `/api/` 代理到：

```nginx
proxy_pass http://127.0.0.1:8081/api/;
```

### 访问公网 IP 根路径不是前端

原因：80 端口是服务器已有站点，本项目前端改为 8000。

处理：访问 `http://8.148.7.94:8000/`，并确认安全组放行 8000。

### Jenkins 第一次构建很慢

原因：首次下载 Maven 依赖和 npm 依赖。

处理：只要日志还在下载依赖且没有 ERROR，可以等待。后续构建会明显变快。

### Flyway 校验失败

不要修改已经上线执行过的 `V1`、`V2`、`V3` 迁移文件。需要改表时新增 `V4__xxx.sql`。

如果只是测试库并确认无重要数据，可以清空库重跑迁移。生产环境不要随意删除 `flyway_schema_history`。

## 发布后验证

```bash
sudo systemctl status xinyu-backend --no-pager
sudo ss -lntp | grep -E ':8000|:8080|:8081'
curl -I http://127.0.0.1:8000/
curl -I http://127.0.0.1:8081/api/swagger-ui/index.html
ls -la /opt/xinyu/web
ls -la /opt/xinyu/app
```

浏览器访问：

```text
前端：http://8.148.7.94:8000/
Swagger：http://8.148.7.94:8081/api/swagger-ui/index.html
Jenkins：http://8.148.7.94:8080/
```

默认超级管理员：

```text
手机号：18530957887
密码：xinyu0508
```

## 后续建议

- 前端确认可访问后，再决定是否让 80 端口也指向本项目。
- 如果保留 Jenkins 公网访问，建议限制来源 IP 或加 HTTPS。
- Swagger 的 8081 公网访问适合调试，正式环境建议只允许内网或通过 Nginx 做权限控制。
- 数据库密码 `132321` 只适合当前测试环境，正式环境必须更换强密码。
