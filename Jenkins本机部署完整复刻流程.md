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

## 0. 你现在应该从哪里继续

你现在已经完成了这些前置步骤：

```text
Jenkins 已安装
Jenkins 已能登录访问
Jenkins 插件已安装
当前页面在 Manage Jenkins / 系统管理
```

所以接下来不需要重新安装 Jenkins，直接按下面顺序做：

```text
1. 在服务器上准备 Java、Maven、Node、Nginx、MySQL
2. 准备 /opt/xinyu 部署目录
3. 准备数据库 xin_yu_db
4. 准备后端 application-local.yml
5. 准备 systemd 后端服务
6. 给 Jenkins 配置免密重启后端服务权限
7. 配置 Nginx
8. 在 Jenkins 新建 Pipeline 任务
9. 运行 Build with Parameters
10. 浏览器访问系统
```

如果你已经装过某一步，执行对应的版本检查命令即可，不需要重复安装。

快速检查命令：

```bash
java -version
mvn -v
node -v
npm -v
nginx -v
mysql --version
git --version
```

只要这些命令都能正常输出版本，Jenkins 构建环境就基本齐了。

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

你已经安装插件的话，可以在 Jenkins 页面确认：

```text
系统管理
插件管理
Installed plugins / 已安装
搜索 Git、Pipeline、Credentials Binding
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

从你截图所在的 `系统管理 / Manage Jenkins` 页面开始：

```text
1. 点击左上角 Jenkins 标识，回到 Jenkins 首页
2. 点击左侧菜单 新建任务 / New Item
3. 输入任务名称：xinyu-deploy
4. 选择 Pipeline
5. 点击 OK
```

进入任务配置页后，先配置基础信息：

```text
General
- Description：XinYu 跨境电商管理系统自动部署
- This project is parameterized：不用手动勾选
```

说明：当前参数已经写在根目录 `Jenkinsfile` 里，Jenkins 第一次读取 Jenkinsfile 后会自动出现参数。

继续往下找到 `Pipeline` 区域：

```text
Definition：Pipeline script from SCM
SCM：Git
Repository URL：https://github.com/zt2234546968/xin-yu.git
Branch Specifier：*/main
Script Path：Jenkinsfile
```

如果你的 GitHub 仓库是公开仓库：

```text
Credentials 可以先选择 - none -
```

如果你的 GitHub 仓库是私有仓库，需要先添加凭据：

```text
1. 系统管理
2. 凭据管理 / Credentials
3. System
4. Global credentials
5. Add Credentials
```

凭据填写：

```text
Kind：Username with password
Username：zt2234546968
Password：GitHub Personal Access Token，不是 GitHub 登录密码
ID：github-xinyu-token
Description：GitHub xin-yu deploy token
```

然后回到 `xinyu-deploy` 任务配置页：

```text
Credentials：选择 github-xinyu-token
```

保存任务：

```text
点击 Save / 保存
```

保存后第一次进入任务页面，可能还看不到 `Build with Parameters`，这是正常的。先点一次：

```text
Build Now
```

这次构建的主要作用是让 Jenkins 拉取仓库并读取 Jenkinsfile。第一次如果因为服务器目录或 systemd 服务还没配好而失败，不代表任务配置错了。读取 Jenkinsfile 成功后，左侧会出现：

```text
Build with Parameters
```

之后正式发布就点 `Build with Parameters`。

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

默认参数含义：

```text
DEPLOY_DIR=/opt/xinyu
SERVICE_NAME=xinyu-backend
```

构建时进入本次构建编号，点击：

```text
Console Output
```

正常情况下你会依次看到这些阶段：

```text
Checkout
Build Backend
Build Frontend
Deploy
```

其中：

```text
Build Backend：执行 mvn clean package -DskipTests
Build Frontend：执行 npm install 和 npm run build
Deploy：复制 Jar、复制前端 dist、重启 xinyu-backend
```

看到下面类似日志代表 Jenkinsfile 执行完成：

```text
XinYu 部署完成
Finished: SUCCESS
```

构建成功后访问：

```text
http://服务器公网IP/
```

登录账号：

```text
手机号：18530957887
密码：xinyu0508
```

后端健康检查可以看服务日志：

```bash
sudo systemctl status xinyu-backend --no-pager
journalctl -u xinyu-backend -n 100 --no-pager
```

前端静态文件检查：

```bash
ls -la /opt/xinyu/web
```

正常应该能看到：

```text
index.html
assets
```

后端 Jar 检查：

```bash
ls -la /opt/xinyu/app
```

正常应该能看到：

```text
cross-border-ecommerce-backend-1.0.0.jar
```

## 14. 可选：配置 GitHub 自动触发 Jenkins

手动发布跑通后，再考虑自动触发。先不要一上来就配自动触发，避免不好排查问题。

Jenkins 任务配置中勾选：

```text
Build Triggers
GitHub hook trigger for GITScm polling
```

GitHub 仓库页面配置：

```text
Settings
Webhooks
Add webhook
```

填写：

```text
Payload URL：http://你的服务器IP:8080/github-webhook/
Content type：application/json
Which events：Just the push event
Active：勾选
```

保存后，本地每次：

```bash
git push
```

GitHub 就会通知 Jenkins 自动构建。

注意：

```text
如果 Jenkins 8080 没有开放公网访问，GitHub Webhook 访问不到 Jenkins。
正式环境建议给 Jenkins 配域名和 HTTPS，或只使用手动构建。
```

## 15. 常见问题

### Jenkins 拉不到 GitHub 代码

检查：

```text
仓库是否私有
Jenkins 是否配置 GitHub Token
Pipeline 是否选择正确 Credentials
分支是否是 main
```

如果 Console Output 里出现：

```text
Authentication failed
```

优先检查 GitHub Token。GitHub 现在不能用账号密码拉私有仓库，必须用 Personal Access Token。

如果出现：

```text
Couldn't find any revision to build
```

检查分支配置是否为：

```text
*/main
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

如果目录不存在，先创建：

```bash
sudo mkdir -p /opt/xinyu/app /opt/xinyu/web /opt/xinyu/backup /opt/xinyu/config
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

如果报数据库连接失败，先在服务器上测试：

```bash
mysql -uroot -p
```

然后执行：

```sql
SHOW DATABASES;
USE xin_yu_db;
SHOW TABLES;
```

如果报 Flyway 校验失败，通常是修改过已经执行过的 `V1`、`V2`、`V3` 迁移文件。生产环境不要直接改历史迁移，新增结构请创建新的 `V4__xxx.sql`。

### 前端访问 404

检查：

```bash
ls -la /opt/xinyu/web
sudo nginx -t
sudo systemctl status nginx --no-pager
```

`/opt/xinyu/web` 中应有 `index.html` 和 `assets` 目录。

### 前端能打开，但是接口 404 或 502

检查 Nginx 代理：

```bash
sudo nginx -t
sudo systemctl status nginx --no-pager
```

检查后端是否监听：

```bash
curl http://127.0.0.1:8080/api
sudo systemctl status xinyu-backend --no-pager
```

502 通常代表后端没启动，404 通常代表 Nginx `/api/` 代理路径或后端 `server.servlet.context-path` 不一致。

当前项目后端上下文路径是：

```text
/api
```

所以 Nginx 里必须保留：

```nginx
location /api/ {
    proxy_pass http://127.0.0.1:8080/api/;
}
```

## 16. 日常发布流程

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

推荐发布前本地先跑：

```bash
cd backend
mvn test

cd ../leading
npm run build
```

本地能通过，再推到 GitHub，Jenkins 成功率会更高。
