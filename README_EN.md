# chatgpt-plus

To English Doc -> [English Doc](README_EN.md)

# 📖 Project Introduction

**Client for Custom Plugins in ChatGPT**

#### This project is the full version of ChatGPT Plus based on OpenAI's official ChatGPT. It provides additional capabilities such as querying daily news, weather, oil prices, stock markets, and more. The project is developed using OPEN AI's functional invocation and currently includes some built-in plugins (with continuous updates). It also supports developers to add their own custom plugins. The project supports both streaming and non-streaming invocations.

# 🚩 Features
#### Non-streaming output:
<img width="600" alt="image" src="https://github.com/liyf1/chatgpt-plus/assets/49024327/3bc7589c-3324-4dcd-addf-3de53aa9313f">
<img width="433" alt="image" src="https://github.com/liyf1/chatgpt-plus/assets/49024327/1e91215e-8263-4135-a21e-ca6e9dc40c81">


#### Streaming output:
<img width="433" alt="image" src="https://github.com/liyf1/chatgpt-plus/assets/49024327/9e6edb67-92ac-4fbe-8371-66bffb15bcc6">

### Currently Built-in Plugins:
- [✅] Current time query
- [✅] Mid-journey image generation
- [✅] City weather query
- [✅] News query
- [✅] Email sending
- [✅] Weibo hot search
- [✅] Baidu search 
- [✅] Baidu Baike 
- .
- .
- .
-   More plugins are continuously being updated

# 🚀 Quick Start
### 1. Local Launch

Project Environment Requirements: JDK 17+
##### Environment Variables

```
## This project references the mid-journey proxy project and requires starting the mid-journey project separately. Replace the service URL once it is started.

mj.service.url = http://ip:port

## ChatGPT API Key
chatgpt.api.key = sk-xxxxx

## Email sending configuration information
mail.host = smtp.xx.com
mail.port = 465
mail.username = xxxx@xx.com
mail.password = xxxxxxx
mail.subject = AI Chatbot

## The weather query uses Baidu Smart Cloud's API. You need to apply for a Baidu Smart Cloud account, create an application, and obtain the accessKey and secretKey. Refer to https://apis.baidu.com/store/detail/d031401a-4081-4572-8dd7-aca64223197e
baidu.weather.accessKey = 
baidu.weather.secretKey = 

## The news query uses Juhe Data's API. You need to apply for a Juhe Data account, create an application, and obtain the key. Refer to https://www.juhe.cn/docs/api/id/235
juhe.news.key =

## Dynamic IP retrieval is used in the project to fetch real-time data. If you want to use features like Baidu Weibo, configure the following. I used this product: https://www.kuaidaili.com/doc/product/dps/#fetchtypeip
proxy.ip.signature = 
proxy.ip.secretId = 

## Interface authorization. Once configured, the request must include the token; otherwise, authentication will fail.
chatgpt.api.token = 123456

server.port = 15600
```

### 2. Docker Launch
Docker launch is not supported yet, but it will be supported soon.

# 🙏 Acknowledgements
This project relies on the code provided by these great developers. A big thank you to them! 🌹:
- OpenAi: https://openai.com/
- chatgpt-java: https://github.com/Grt1228/chatgpt-java
- midjourney-proxy: https://github.com/novicezk/midjourney-proxy (Used indirectly in this project. If you want to generate images, please start this project separately.)
