# chatgpt-plus

To English Doc -> [English Doc](README_EN.md)

# 📖 项目简介

**ChatGPT自定义插件的客户端**

#### 此项目是 Open AI ChatGPT 的plus满血版本。在官方chatgpt的基础上提供了一些额外的拓展能力，比如当日新闻天气油价股市查询等等，只有想不到没有做不到。项目基于OPEN AI的函数式调用开发，目前预置了一些插件（后续在不断更新），同时支持开发者加入自己的自定义插件。项目支持流式和非流式调用两种方式。

# 🚩 特性
#### 非流式输出：
<img width="600" alt="image" src="https://github.com/liyf1/chatgpt-plus/assets/49024327/3bc7589c-3324-4dcd-addf-3de53aa9313f">
<img width="433" alt="image" src="https://github.com/liyf1/chatgpt-plus/assets/49024327/1e91215e-8263-4135-a21e-ca6e9dc40c81">


#### 流式输出：
<img width="433" alt="image" src="https://github.com/liyf1/chatgpt-plus/assets/49024327/9e6edb67-92ac-4fbe-8371-66bffb15bcc6">

### 目前已内置插件：
- [✅] 当前时间查询
- [✅] mid-journey图片生成
- [✅] 城市天气查询
- [✅] 新闻查询
- [✅] 邮件发送
- [✅] 微博热搜
- [✅] 百度搜索 
- [✅] 百度百科 
- .
- .
- .
-   持续更新中

# 🚀 快速开始
### 一、本地启动

项目环境要求：jdk17+
##### 环境变量

```
## 引用了mid-journey proxy项目，需要单独启动mid-journey项目，将启动后地址配入进去
mj.service.url = http://ip:port

## chatgpt api key
chatgpt.api.key = sk-xxxxx

## 发送邮件的配置信息
mail.host = smtp.xx.com
mail.port = 465
mail.username = xxxx@xx.com
mail.password = xxxxxxx
mail.subject = AI Chatbot

## 天气查询使用了百度智能云的接口，需要申请百度智能云的账号，然后创建应用，获取accessKey和secretKey，https://apis.baidu.com/store/detail/d031401a-4081-4572-8dd7-aca64223197e
baidu.weather.accessKey = 
baidu.weather.secretKey = 

## 新闻查询使用了聚合数据的接口，需要申请聚合数据的账号，然后创建应用，获取key，https://www.juhe.cn/docs/api/id/235
juhe.news.key =

## 项目中使用了动态IP获取实时数据，如需使用百度微博等功能需要配置 ，我使用的产品https://www.kuaidaili.com/doc/product/dps/#fetchtypeip
proxy.ip.signature = 
proxy.ip.secretId = 

## 接口的权限验证，配置后请求中必须带有token，否则会认证失败
chatgpt.api.token = 123456

server.port = 15600
```
### 二、docker启动

docker pull uswccr.ccs.tencentyun.com/liyf/images:chatgpt-plus-v1.0
或者
docker pull a419820659/liyf007:chatgpt-plus-v1.0

```
version: '3'
services:
  myapp:
    image: chatgpt-plus-v1.0
    ports:
      - 15600:15600
    environment:
      - mj.service.url=http://xxxxx:8080
      - chatgpt.api.key=sk-32131321ky8ph1231B2xxxxxvUqBX9
      - mail.host=smtp.qq.com
      - mail.port=465
      - mail.username=xsds@qq.com
      - mail.password=2312313
      - mail.subject=AI Chatbot
      - baidu.weather.accessKey=sds
      - baidu.weather.secretKey=sds
      - juhe.news.key=ds
      - proxy.ip.signature=dsds
      - proxy.ip.secretId=dsds
      - chatgpt.api.token=123123
```

# 🙏 鸣谢
项目中依赖了大佬的代码，在此表示感谢🌹：
- OpenAi：https://openai.com/
- chatgpt-java： https://github.com/Grt1228/chatgpt-java
- midjourney-proxy: https://github.com/novicezk/midjourney-proxy 项目内非代码直接依赖，如需图片生成自己单独启动该项目
