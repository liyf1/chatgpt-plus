# ChatGPT-Plus

## 📖 Project Overview

**ChatGPT Custom Plugin Client**

#### This project is the full version of Open AI ChatGPT Plus. It provides additional capabilities on top of the official chatgpt, such as daily news, weather, oil prices, stock market queries, and more. The project is developed based on OPEN AI's function invocation and comes with some pre-built plugins (continuously updated) while also supporting developers to add their own custom plugins. The project supports both streaming and non-streaming calls.

## 🚩 Features
#### Non-streaming output:
![Non-streaming Output](https://github.com/liyf1/chatgpt-plus/assets/49024327/3bc7589c-3324-4dcd-addf-3de53aa9313f)
![Non-streaming Output](https://github.com/liyf1/chatgpt-plus/assets/49024327/1e91215e-8263-4135-a21e-ca6e9dc40c81)

#### Streaming output:
![Streaming Output](https://github.com/liyf1/chatgpt-plus/assets/49024327/9e6edb67-92ac-4fbe-8371-66bffb15bcc6)

### Current built-in plugins:
- [x] Current time query
- [x] Mid-journey image generation
- [x] City weather query
- [x] News query
- [x] Email sending
- .
- .
- .
-   Continuously updated

## 🚀 Quick Start
### I. Local Setup

Project requirements: JDK 17+
##### Environment variables

```
## Refers to the mid-journey proxy project, you need to start the mid-journey project separately and include its startup address here
mj.service.url = http://ip:port

## chatgpt api key
chatgpt.api.key = sk-xxxxx

## Email sending configuration
mail.host = smtp.xx.com
mail.port = 465
mail.username = xxxx@xx.com
mail.password = xxxxxxx
mail.subject = AI Chatbot

## City weather query uses the Baidu Cloud API, you need to apply for a Baidu Cloud account, create an application, and obtain the accessKey and secretKey, https://apis.baidu.com/store/detail/d031401a-4081-4572-8dd7-aca64223197e
baidu.weather.accessKey = 
baidu.weather.secretKey = 

## News query uses the Juhe Data API, you need to apply for a Juhe Data account, create an application, and obtain the key, https://www.juhe.cn/docs/api/id/235
juhe.news.key =
```

### II. Docker Setup
Docker setup is not yet supported but will be available soon.

## 🙏 Acknowledgements
The project depends on the code from amazing contributors, and I would like to express my gratitude 🌹:
- OpenAi: https://openai.com/
- chatgpt-java: https://github.com/Grt1228/chatgpt-java
- midjourney-proxy: https://github.com/novicezk/midjourney-proxy (Indirect dependency in this project, if you want image generation, please start this project separately)

Translated into English, without changing the style.
