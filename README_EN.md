# chatgpt-plus

To English Doc -> [中文文档](README.md)

# 📖 Project Introduction

**A client for custom ChatGPT plugins**

#### This project is the full-blooded plus version of Open AI's ChatGPT. It provides some additional capabilities on top of the official ChatGPT, such as querying for daily news, weather, gas prices, stock market, etc. There's nothing you can't imagine that it can't do. The project is developed based on OPEN AI's functional invocation and currently has some pre-installed plugins (which will continue to be updated). It also supports developers adding their own custom plugins. The project supports both streaming and non-streaming calling methods.

# 🚩 Features
#### Non-streaming output:
<img width="600" alt="image" src="https://github.com/liyf1/chatgpt-plus/assets/49024327/3bc7589c-3324-4dcd-addf-3de53aa9313f">
<img width="433" alt="image" src="https://github.com/liyf1/chatgpt-plus/assets/49024327/1e91215e-8263-4135-a21e-ca6e9dc40c81">


#### Streaming output:
<img width="433" alt="image" src="https://github.com/liyf1/chatgpt-plus/assets/49024327/9e6edb67-92ac-4fbe-8371-66bffb15bcc6">

### Automatic Decision-making
According to the request content, the built-in decision module automatically identifies the plugins that need to be used, without the need to specify the name of the corresponding plugin, and supports the use of multiple plugins simultaneously, for example: sending Beijing's weather to 4214142@gmail.com

### Currently Built-in Plugins:
- [✅] Current time query
- [✅] Mid-journey image generation
- [✅] City weather query
- [✅] News query
- [✅] Email sending
- [✅] Weibo hot search
- [✅] Baidu search
- [✅] Baidu encyclopedia
- [✅] Google search
- [✅] Web link reading
- [✅] AI daily tech news
- [✅] Dall-E 3 image generation
- [✅] Daily morning news
- [✅] Slack off daily
- [todo] Bilibili video summary
-   Continuously updating

# 🚀 Quick Start
### I. Local Launch

Project environment requirements: jdk17
##### Environment Variables

```
## The project referenced the mid-journey proxy project, which must be launched separately. Enter the address after startup
mj.service.url = http://ip:port

## chatgpt api key
chatgpt.api.key = sk-xxxxx

## Configuration information for sending emails
mail.host = smtp.xx.com
mail.port = 465
mail.username = xxxx@xx.com
mail.password = xxxxxxx
mail.subject = AI Chatbot

## The weather query used the interface of Baidu Intelligent Cloud. You need to apply for a Baidu Intelligent Cloud account, then create an application to get accessKey and secretKey. https://apis.baidu.com/store/detail/d031401a-4081-4572-8dd7-aca64223197e
baidu.weather.accessKey = 
baidu.weather.secretKey = 

## The news query used the interface of Juhe Data. You need to apply for an account with Juhe Data, then create an application to get a key, https://www.juhe.cn/docs/api/id/235
juhe.news.key =

## The project used dynamic IP to get real-time data. To use functions like Baidu and Weibo, configure the following. I used the product https://www.kuaidaili.com/doc/product/dps/#fetchtypeip
proxy.ip.signature = 
proxy.ip.secretId = 

## Interface authentication, you must carry a token in the request after configuration, otherwise authentication will fail
chatgpt.api.token = 123456

server.port = 15600
```
### II. Docker Launch

docker pull uswccr.ccs.tencentyun.com/liyf/images:chatgpt-plus-v1.0
or
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

# 🙏 Acknowledgments
The project depends on the code of great developers, I would like to express my thanks 🌹:
- OpenAi: https://openai.com/
- chatgpt-java: https://github.com/Grt1228/chatgpt-java
- midjourney-proxy: https://github.com/novicezk/midjourney-proxy The project does not have a direct code dependency, if you need image generation, you need to start the project separately