# ids
[![GPL-3.0](https://img.shields.io/badge/license-GPL--3.0-%250778B9.svg)](https://www.gnu.org/licenses/gpl-3.0.html)

ICe Distribution Service Core

快速开发分布式服务器框架 

# 特点
保持微服务理念前提下 完全不依赖中间件 一键启动 最大化节省服务器维护成本 让使用者精力集中到业务本身

麻雀虽小五脏俱全 只需要绑定一个端口 就可以实现 http websocket socket 等通讯手段 原生支持json protobuf等协议

支持静态资源访问 与vue天然结合

集成最简单数据库访问方式 用户可以无需关心数据库状态 原生支持mysql redis mongodb

低资源占用 体积小 启动快速 集成docker 一键部署
# 适用场景
中小型网站快速开发

网络游戏服务器

手机app,小程序,公众号服务器

admin后台开发

# 版本说明
ids1.0系列最高版本号v1.6.21 目前已经停止维护

ids2.0系列是船新重构版本 目前处于开发阶段....
  
![image](https://github.com/icesovereign/ids/blob/main/mind.png)

# 新项目生成方法
1,本地更新并安装idsArchetypes
```shell
mvn clean install -f idsArchetypes/pom.xml
```
2,执行下面脚本 (执行前先修改对应的变量)
```shell
mvn archetype:generate -DarchetypeGroupId=com.sencorsta -DarchetypeArtifactId=idsArchetypes -DinteractiveMode=false -Dversion=2.0-SNAPSHOT -Dpackage=com.sencorsta.ids -DgroupId=com.sencorsta -DartifactId=SuperMarioGame
```
