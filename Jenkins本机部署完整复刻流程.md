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

当前阿里云服务器已确认环境：

```text
Java：Alibaba Dragonwell OpenJDK 21
Maven：Apache Maven 3.9.9，安装目录 /opt/maven
Node.js：20.20.2
npm：10.8.2
Nginx：1.26.2
MySQL：8.0.46 MySQL Community Server
Git：2.43.7
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

Jenkins 使用 `8080`。为避免端口冲突，当前服务器后端监听 `8081`，生产访问由 Nginx 对外暴露 `80` 并反向代理 `/api`。

## 2. 安装基础工具

Jenkins 新版本建议 Java 21，项目后端使用 Java 17 也可以在 Java 21 上运行。当前服务器已经安装 Alibaba Dragonwell OpenJDK 21：

```bash
java -version
```

### 2.1 Maven 3.9.9

不要直接依赖系统源里的 Maven 版本，当前服务器使用固定版本 Maven 3.9.9：

```bash
cd /opt
sudo wget https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz
sudo tar -zxvf apache-maven-3.9.9-bin.tar.gz
sudo ln -sfn /opt/apache-maven-3.9.9 /opt/maven
printf '%s\n' 'export MAVEN_HOME=/opt/maven' 'export PATH=$MAVEN_HOME/bin:$PATH' | sudo tee /etc/profile.d/maven.sh > /dev/null
source /etc/profile.d/maven.sh
mvn -v
```

### 2.2 Node.js 20

```bash
curl -fsSL https://rpm.nodesource.com/setup_20.x | sudo bash -
sudo yum install -y nodejs
node -v
npm -v
```

当前服务器已确认：

```text
node v20.20.2
npm 10.8.2
```

### 2.3 Nginx

当前服务器已经存在 Nginx 1.26.2。如果 `sudo yum install -y nginx` 提示被 exclude 过滤，但 `nginx -v` 能看到版本，可以继续使用现有 Nginx。

```bash
nginx -v
sudo systemctl enable nginx
sudo systemctl start nginx
sudo nginx -t
```

### 2.4 Git

```bash
sudo yum install -y git
git --version
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

项目推荐使用 MySQL 8.0。当前服务器最终使用的是 MySQL Community Server 8.0.46。

如果服务器上原来有面板或脚本安装的 MariaDB，可能会出现类似：

```text
/www/server/mysql/bin/mariadbd
```

这说明 yum 包已经卸载，但 `/www/server/mysql` 下还有旧 MariaDB 在运行。确认没有重要数据后，可以直接清理：

```bash
sudo systemctl stop mysqld
sudo systemctl disable mysqld
sudo pkill -f mariadbd || true
sudo pkill -f mysqld_safe || true
ps -ef | grep -E 'mysqld|mariadbd' | grep -v grep

sudo rm -rf /www/server/mysql
sudo rm -rf /www/server/data
sudo rm -f /etc/my.cnf
sudo rm -f /etc/init.d/mysqld
sudo rm -f /etc/rc.d/init.d/mysqld
sudo systemctl daemon-reload
```

安装 MySQL 8.0 官方源和服务端：

```bash
cd /tmp
rm -f mysql80-community-release-el8.rpm
wget https://repo.mysql.com/mysql80-community-release-el8.rpm
sudo rpm -Uvh mysql80-community-release-el8.rpm
sudo rpm --import https://repo.mysql.com/RPM-GPG-KEY-mysql-2023
sudo yum clean all
sudo yum makecache
sudo yum install -y mysql-community-server --disableexcludes=all
```

启动 MySQL：

```bash
sudo systemctl daemon-reload
sudo systemctl enable mysqld
sudo systemctl start mysqld
sudo systemctl status mysqld --no-pager
mysql --version
```

获取临时 root 密码：

```bash
sudo grep 'temporary password' /var/log/mysqld.log
```

第一次登录后必须先设置一个符合策略的强密码，再降低策略，最后才可以改成简单密码：

```sql
ALTER USER 'root'@'localhost' IDENTIFIED BY 'Root@2026_XinYu!';
SET GLOBAL validate_password.policy = LOW;
SET GLOBAL validate_password.length = 6;
ALTER USER 'root'@'localhost' IDENTIFIED BY '132321';
```

创建项目数据库和项目用户：

```sql
CREATE DATABASE IF NOT EXISTS xin_yu_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'xinyu'@'localhost' IDENTIFIED BY '132321';
GRANT ALL PRIVILEGES ON xin_yu_db.* TO 'xinyu'@'localhost';
FLUSH PRIVILEGES;
```

验证：

```bash
mysql -uxinyu -p
```

输入密码 `132321` 后执行：

```sql
SHOW DATABASES;
USE xin_yu_db;
SHOW TABLES;
exit;
```

`SHOW TABLES;` 为空是正常的，Jenkins 首次部署后，后端启动时会通过 Flyway 自动建表。

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

这是服务器上的运行配置文件，不是本地项目代码。它只存在服务器上，不提交到 GitHub。

创建配置目录和文件：

```bash
sudo mkdir -p /opt/xinyu/config
sudo tee /opt/xinyu/config/application-local.yml > /dev/null <<'EOF'
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
EOF
```

检查：

```bash
cat /opt/xinyu/config/application-local.yml
```

注意：

```text
不要把 application-local.yml 提交到 GitHub
不要把数据库密码写进 Jenkinsfile
当前测试服务器使用 xinyu / 132321，正式环境建议改成更强密码
```

## 8. 配置 systemd 服务

创建服务文件，使用 `tee` 可以避免手动编辑器输入错误：

```bash
sudo tee /etc/systemd/system/xinyu-backend.service > /dev/null <<'EOF'
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
EOF
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
sudo tee /etc/sudoers.d/xinyu-jenkins > /dev/null <<'EOF'
jenkins ALL=(ALL) NOPASSWD: /usr/bin/systemctl restart xinyu-backend, /usr/bin/systemctl status xinyu-backend --no-pager
EOF
```

检查语法和内容：

```bash
sudo visudo -cf /etc/sudoers.d/xinyu-jenkins
sudo cat /etc/sudoers.d/xinyu-jenkins
```

## 10. 配置 Nginx

当前服务器的 Nginx 不是标准 yum 原生目录，而是面板安装目录：

```text
主配置：/www/server/nginx/conf/nginx.conf
站点配置目录：/www/server/panel/vhost/nginx/*.conf
启动程序：/www/server/nginx/sbin/nginx
```

如果不确定当前服务器 Nginx 配置目录，先执行：

```bash
sudo nginx -t
sudo grep -n "include" /www/server/nginx/conf/nginx.conf
```

当前服务器已经确认主配置里有：

```text
include /www/server/panel/vhost/nginx/*.conf;
```

所以项目站点配置写到：

```text
/www/server/panel/vhost/nginx/xinyu.conf
```

写入配置：

```bash
sudo tee /www/server/panel/vhost/nginx/xinyu.conf > /dev/null <<'EOF'
server {
    listen 80;
    server_name _;

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
EOF
```

检查并重启：

```bash
sudo nginx -t
sudo systemctl restart nginx
sudo systemctl status nginx --no-pager
```

如果看到 `Active: active (running)`，说明 Nginx 已经正常运行。

如果看到类似：

```text
nginx.service is not a native service, redirecting to systemd-sysv-install
```

这是面板 Nginx 使用 `/etc/init.d/nginx` 脚本被 systemd 托管，不是错误，只要服务状态是 `active (running)` 即可。

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
后端执行 /opt/maven/bin/mvn clean package -DskipTests
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
Build Backend：执行 /opt/maven/bin/mvn clean package -DskipTests
Build Frontend：执行 npm install 和 npm run build
Deploy：复制 Jar、复制前端 dist、重启 xinyu-backend，等待 12 秒后检查服务状态
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

如果服务器终端执行 `mvn -v` 正常，但 Jenkins Console Output 里出现：

```text
mvn: command not found
```

原因是 Jenkins 执行构建 shell 时不会自动加载登录用户的 `/etc/profile.d/maven.sh`。当前项目的 `Jenkinsfile` 已显式使用 Maven 绝对路径：

```groovy
MAVEN_HOME = '/opt/maven'
sh '/opt/maven/bin/mvn clean package -DskipTests'
```

所以正常情况下 Jenkins 能直接找到 Maven。若服务器 Maven 尚未安装，按下面命令安装：

```bash
cd /opt
sudo wget https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz
sudo tar -zxvf apache-maven-3.9.9-bin.tar.gz
sudo ln -sfn /opt/apache-maven-3.9.9 /opt/maven
printf '%s\n' 'export MAVEN_HOME=/opt/maven' 'export PATH=$MAVEN_HOME/bin:$PATH' | sudo tee /etc/profile.d/maven.sh > /dev/null
source /etc/profile.d/maven.sh
mvn -v
```

### 找不到 npm 或 node

```bash
curl -fsSL https://rpm.nodesource.com/setup_20.x | sudo bash -
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
jenkins ALL=(ALL) NOPASSWD: /usr/bin/systemctl restart xinyu-backend, /usr/bin/systemctl status xinyu-backend --no-pager
```

如果 Jenkins Console Output 里出现：

```text
sudo: a terminal is required to read the password
sudo: a password is required
```

通常是 sudoers 里的命令参数没有和 Jenkinsfile 完全一致。当前 Jenkinsfile 执行的是：

```bash
sudo /usr/bin/systemctl restart xinyu-backend
sleep 12
sudo /usr/bin/systemctl status xinyu-backend --no-pager
```

所以 sudoers 必须同时包含上面两条命令，其中 `status` 后面的 `--no-pager` 不能漏。

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
8081 端口是否被占用
/opt/xinyu/config/application-local.yml 是否存在
```

当前服务器 Jenkins 已占用 `8080`，所以后端必须使用 `8081`。如果日志出现：

```text
Web server failed to start. Port 8080 was already in use.
```

说明后端端口仍然配置成了 `8080`。检查并修正：

```bash
cat /opt/xinyu/config/application-local.yml
sudo sed -i 's/port: 8080/port: 8081/' /opt/xinyu/config/application-local.yml
sudo sed -i 's#http://127.0.0.1:8080/api/#http://127.0.0.1:8081/api/#' /www/server/panel/vhost/nginx/xinyu.conf
sudo nginx -t
sudo systemctl restart nginx
sudo systemctl restart xinyu-backend
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
curl http://127.0.0.1:8081/api
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
    proxy_pass http://127.0.0.1:8081/api/;
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
