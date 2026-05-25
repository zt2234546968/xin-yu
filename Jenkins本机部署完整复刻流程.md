# Jenkins 本机部署完整流程

这份文档用于把当前 `XinYu` 跨境电商管理系统部署到一台 Linux 服务器上。

推荐方案：

```text
Jenkins 安装在业务服务器本机
Jenkins 从 GitHub 拉取代码
Jenkins 在本机打包后端和前端
后端 Jar 部署到 /opt/xinyu/app
前端静态文件部署到 /opt/xinyu/web
Nginx 托管前端并反向代理 /api
systemd 管理后端服务 xinyu-backend
```

当前项目仓库：

```text
https://github.com/zt2234546968/xin-yu.git
```

## 1. 服务器环境

推荐配置：

```text
系统：Alibaba Cloud Linux 4 / CentOS / Rocky / AlmaLinux
CPU：2 核及以上
内存：4G 及以上
磁盘：40G 及以上
```

建议开放端口：

```text
22    SSH
80    网站访问
443   HTTPS
8080  Jenkins 管理页面，正式环境建议限制来源 IP
3306  MySQL，仅内网或本机访问
```

后端默认监听 `8080`，但生产环境建议只让 Nginx 访问后端，不直接暴露后端端口。

## 2. 安装基础工具

Jenkins 新版本建议 Java 21，项目后端使用 Java 17 也可以在 Java 21 上运行。

```bash
sudo yum install -y wget git tar gzip fontconfig maven nginx
sudo yum install -y java-21-openjdk java-21-openjdk-devel
java -version
mvn -v
```

安装 Node.js 22：

```bash
curl -fsSL https://rpm.nodesource.com/setup_22.x | sudo bash -
sudo yum install -y nodejs
node -v
npm -v
```

## 3. 安装 Jenkins

添加 Jenkins LTS 源：

```bash
sudo wget -O /etc/yum.repos.d/jenkins.repo https://pkg.jenkins.io/redhat-stable/jenkins.repo
sudo rpm --import https://pkg.jenkins.io/redhat-stable/jenkins.io-2023.key
```

安装并启动：

```bash
sudo yum install -y jenkins
sudo systemctl daemon-reload
sudo systemctl enable jenkins
sudo systemctl start jenkins
sudo systemctl status jenkins
```

查看初始密码：

```bash
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```

浏览器访问：

```text
http://服务器公网IP:8080
```

## 4. Jenkins 插件

至少确认这些插件可用：

```text
Git
Pipeline
Credentials Binding
```

如果插件下载慢，可以把 Jenkins 更新站点改为清华镜像：

```text
https://mirrors.tuna.tsinghua.edu.cn/jenkins/updates/update-center.json
```

修改后重启 Jenkins：

```bash
sudo systemctl restart jenkins
```

## 5. 准备部署目录

```bash
sudo mkdir -p /opt/xinyu/app /opt/xinyu/web /opt/xinyu/backup /opt/xinyu/config
sudo chown -R jenkins:jenkins /opt/xinyu
```

目录说明：

```text
/opt/xinyu/app      后端 Jar
/opt/xinyu/web      前端 dist 静态文件
/opt/xinyu/backup   后端历史 Jar 备份
/opt/xinyu/config   后端本地配置
```

## 6. 准备数据库

安装 MySQL 8 并创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS xin_yu_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

项目使用 Flyway，后端启动时会自动执行：

```text
backend/src/main/resources/db/migration
```

当前迁移：

```text
V1__init_xinyu_schema.sql
V2__fix_countries_and_placeholder_images.sql
V3__marketplace_task_modules.sql
```

默认超级管理员：

```text
手机号：18530957887
密码：xinyu0508
```

## 7. 后端生产配置

创建配置文件：

```bash
sudo vi /opt/xinyu/config/application-local.yml
```

写入：

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/xin_yu_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: 你的数据库密码
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    validate-on-migrate: false
```

注意：

```text
不要把 application-local.yml 提交到 GitHub
不要把数据库密码写进 Jenkinsfile
生产环境建议使用独立 MySQL 用户，不要长期使用 root
```

## 8. 配置 systemd 服务

创建服务文件：

```bash
sudo vi /etc/systemd/system/xinyu-backend.service
```

写入：

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

启用服务：

```bash
sudo systemctl daemon-reload
sudo systemctl enable xinyu-backend
```

第一次 Jenkins 部署前 Jar 还不存在，服务启动失败是正常的。等 Jenkins 首次部署后再启动或重启。

## 9. 允许 Jenkins 重启服务

确认 `systemctl` 路径：

```bash
which systemctl
```

通常是：

```text
/usr/bin/systemctl
```

创建 sudoers 文件：

```bash
sudo visudo -f /etc/sudoers.d/xinyu-jenkins
```

写入：

```text
jenkins ALL=(ALL) NOPASSWD: /usr/bin/systemctl restart xinyu-backend, /usr/bin/systemctl status xinyu-backend
```

保存后检查：

```bash
sudo cat /etc/sudoers.d/xinyu-jenkins
```

## 10. 配置 Nginx

创建配置：

```bash
sudo vi /etc/nginx/conf.d/xinyu.conf
```

写入：

```nginx
server {
    listen 80;
    server_name _;

    root /opt/xinyu/web;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

检查并重载：

```bash
sudo nginx -t
sudo systemctl enable nginx
sudo systemctl reload nginx
```

## 11. 创建 Jenkins Pipeline

Jenkins 首页点击：

```text
新建任务
```

填写：

```text
任务名称：xinyu-deploy
类型：Pipeline
```

Pipeline 配置：

```text
Definition: Pipeline script from SCM
SCM: Git
Repository URL: https://github.com/zt2234546968/xin-yu.git
Branch Specifier: */main
Script Path: Jenkinsfile
```

如果仓库是私有仓库，需要在 Jenkins Credentials 里添加 GitHub Token，然后在任务里选择该凭据。

## 12. Jenkinsfile 做了什么

根目录 `Jenkinsfile` 会自动执行：

```text
拉取代码
后端执行 mvn clean package -DskipTests
前端执行 npm install
前端执行 npm run build
备份旧 Jar
复制新 Jar 到 /opt/xinyu/app
复制前端 dist 到 /opt/xinyu/web
重启 xinyu-backend
输出服务状态
```

默认参数：

| 参数 | 默认值 | 说明 |
| --- | --- | --- |
| `DEPLOY_DIR` | `/opt/xinyu` | 部署根目录 |
| `SERVICE_NAME` | `xinyu-backend` | systemd 服务名 |

## 13. 首次构建

进入 Jenkins 任务：

```text
Build with Parameters
```

参数保持默认，点击构建。

构建成功后访问：

```text
http://服务器公网IP/
```

后端健康检查可以看服务日志：

```bash
sudo systemctl status xinyu-backend --no-pager
journalctl -u xinyu-backend -n 100 --no-pager
```

## 14. 常见问题

### Jenkins 拉不到 GitHub 代码

检查：

```text
仓库是否私有
Jenkins 是否配置 GitHub Token
Pipeline 是否选择正确 Credentials
分支是否是 main
```

### 找不到 mvn

```bash
sudo yum install -y maven
mvn -v
```

### 找不到 npm 或 node

```bash
curl -fsSL https://rpm.nodesource.com/setup_22.x | sudo bash -
sudo yum install -y nodejs
node -v
npm -v
```

### Jenkins 无法写入 /opt/xinyu

```bash
sudo chown -R jenkins:jenkins /opt/xinyu
```

### sudo systemctl 需要密码

检查：

```bash
sudo cat /etc/sudoers.d/xinyu-jenkins
```

内容必须包含：

```text
jenkins ALL=(ALL) NOPASSWD: /usr/bin/systemctl restart xinyu-backend, /usr/bin/systemctl status xinyu-backend
```

### 后端启动失败

查看：

```bash
journalctl -u xinyu-backend -n 100 --no-pager
```

重点检查：

```text
数据库地址、账号、密码是否正确
xin_yu_db 是否存在
Flyway 是否迁移失败
8080 端口是否被占用
/opt/xinyu/config/application-local.yml 是否存在
```

### 前端访问 404

检查：

```bash
ls -la /opt/xinyu/web
sudo nginx -t
sudo systemctl status nginx --no-pager
```

`/opt/xinyu/web` 中应有 `index.html` 和 `assets` 目录。

## 15. 日常发布流程

以后发布只需要：

```text
本地修改代码
git add .
git commit -m "说明本次修改"
git push
打开 Jenkins
进入 xinyu-deploy
点击 Build with Parameters
等待构建完成
访问网站验证
```
