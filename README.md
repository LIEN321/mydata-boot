<p align="center">
    <img src="https://www.mydata.work/images/mydata_logo.png" alt="mydata" width="300px"/>
</p>
<p align="center">
    <strong>你的数据融合新助手</strong>
</p>

-------------------------------------------------------------------------------

## 介绍

`MyData`是一个为业务可持续建设而提供服务的数据平台，如其名“我的数据”旨在让用户更好的掌控和使用数据。

`数据资产化`是本系统的核心理念，将数据视为具有价值的资产，并采取适当的措施来管理、保护和使用，实现企业业务的增值和持续发展。

-------------------------------------------------------------------------------

## 诚邀试用

经过一段时间的打磨，MyData升级到了v0.8.0版本，诚挚邀请更多用户试用；

详细介绍见 [MyData 基于 Web API 的数据集成平台](https://mydata.work/docs)

系统的主要功能和优势：

- **集成度高**：通过API集成数据，没有数据库类型、开发技术等限制，有API即可对接；
- **数据安全**：无需开放业务系统数据库；
- **零侵入性**：无SDK，业务系统有API即可集成；
- **可控性高**：统一调度集成任务，对业务系统几乎无感知；
- **复用性高**：业务系统迁移、技术升级等，无需重复集成，API可用即可恢复集成；
- **私有部署**：支持本地部署，防止数据外泄；

特此承诺：

作为试用用户可享受 **免费试用**、**免费升级功能**、**全程技术支持**；

**前10位纳入实际项目**使用的将成为**永久免费用户**；

试用方式：联系微信，开通专属账号，开展数据集成；

截止日期：2024-08-30

-------------------------------------------------------------------------------

## 规划

`v1 数据集成`：针对多应用之间数据集成的场景，为开发人员提供更安全、更方便的对接集成方案；（进行中）

`v2 数据分析`：基于集成的数据，为业务人员提供数据统计和分析服务；

`v3 数据资产`：基于集成和分析，融入数据资产管理模式，让数据成为企业优化和增益的资产；

-------------------------------------------------------------------------------

## v1 数据集成

基于Web API实现数据集成，简化跨系统之间的数据对接；

适用场景

* 数据集成，打通业务之间的关联，形成业务融合；
* 基于 HTTP(S)协议 API 的应用之间对接数据；
* 为项目或组织构建数据中心，降低对接门槛；

不适用

* 定位不是数据湖，不适用于将所有数据、文件都放到同一个系统里；

优点

* **安全**，与业务数据库完全隔离，保留业务系统对数据的控制；
* **简单**，任意业务系统只需与平台进行一对一的对接；
* **灵活**，数据标准和调度策略由用户自定义；
* **方便**，可复用已有API，减少对接工作量，且无任何SDK 对业务系统是零侵入；

技术选型

* 采用前后端分离框架开发；
    * mydata-blade是平台后端，基于 [SpringBlade](https://gitee.com/smallc/SpringBlade) 的boot版开发
    * mydata-sword是平台前端，基于 [Sword](https://gitee.com/smallc/Sword) （React、Ant Design）开发；
* 依赖组件
    * MySQL：存储管理数据，主流数据库方便维护和使用；
    * MongoDB；存储业务数据；
        * 支持大数据存储；
        * 松散数据结构 适合自定义业务数据的扩展、分析和统计查询；
        * 技术门槛不高，可单机可集群；
    * Redis：缓存管理数据和定时任务；

## 系统结构

![系统结构](http://www.mydata.work/images/mydata_frame_0.7.0.jpg)

## 功能模块

![功能模块](https://www.mydata.work/images/mydata_funtion_v0.7.0.jpg)

## 体验

* Demo体验环境：http://www.mydata.work
* 使用手册：https://www.mydata.work/docs#/./manual/

## 快速部署

详见部署文档：https://www.mydata.work/docs#/./docker/

### 安装Docker

MyData已推送镜像到Docker Hub，因此需要先在服务器上安装Docker，若您已经安装则跳过该步骤；

[CentOS 中安装 Docker](https://docs.docker.com/engine/install/centos/)

[Ubuntu 中安装 Docker](https://docs.docker.com/engine/install/ubuntu/)

### 下载并修改文件

下载 [docker-compose.yml](https://www.mydata.work/res/docker-compose.yml) 、 [nginx.conf](https://www.mydata.work/res/nginx.conf)
和 [redis.conf](https://www.mydata.work/res/redis.conf)  3个文件到您的服务器上，存放在同一目录，然后根据实际环境修改该文件；

### 启动服务

以下命令 需在docker-compose.yml和nginx.conf文件所在目录下执行；

```shell
cd {docker-compose.yml所在目录}
```

1. 启动3个依赖服务（若使用自有mongodb 可跳过）

   ```shell
   docker compose up -d mydata-mongodb mydata-mysql mydata-redis

2. 创建mongodb的账号（若使用自有mongodb 可跳过）

   示例创建的账号是 root/root；

   ```shell
   docker exec -it mydata-mongodb mongo admin
   db.createUser({ user:'root',pwd:'root',roles:[ { role:'root', db: 'admin'}]});
   db.auth('root','root')
   ```

3. 初始化mysql数据库

   [获取SQL脚本](https://gitee.com/LIEN321/mydata-blade/blob/boot/doc/sql/)

   脚本中包含 建库、建表和初始数据，执行后可以看到以下表

4. 启动2个mydata服务

   ```shell
   docker compose up -d mydata-boot mydata-sword
   ```

### 访问mydata

在浏览器上访问`http://{服务器ip}`即可访问mydata；

管理租户：000000，初始账密：admin / admin

# 联系我们

* 交流Q群：<a href="https://qm.qq.com/cgi-bin/qm/qr?k=OVVACmjDnrjQo6j9s7_fmv5Mwx1S_-MM&jump_from=webapi">
  <img src="https://img.shields.io/badge/QQ群-430089673-orange"/></a>
* 联系vx：lient321

![联系作者](./doc/image/wechat.jpg)