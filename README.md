# HealthAssistant - 智能健康管理助手平台

<div align="center">

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.3-green)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

一个基于 Spring Boot 的智能健康管理平台，集成 AI 智能助手、家庭健康共享、药品管理、智能提醒等核心功能

[功能特性](#-功能特性) • [快速开始](#-快速开始) • [项目架构](#-项目架构) • [技术栈](#-技术栈) • [API文档](#-api文档)

</div>

---

## 📖 项目简介

HealthAssistant 是一款智能健康管理助手平台，旨在为家庭提供全方位的健康管理服务。平台采用前后端分离架构，后端基于 Spring Boot 开发，提供 RESTful API 接口，前端基于微信小程序开发。
核心功能包括：
- **AI 智能助手**：基于 DeepSeek API 的智能健康咨询
- **家庭健康共享**：家庭成员间健康数据共享与协作
- **药品智能管理**：OCR 图片识别药品信息
- **智能提醒系统**：用药提醒、健康检查提醒等
- **健康数据分析**：基于深度学习的健康趋势分析

---

## ✨ 功能特性

### 👤 用户管理
- 微信小程序一键登录
- 用户信息管理
- JWT 令牌认证

### 👨‍👩‍👧‍👦 家庭管理
- 创建/加入家庭
- 家庭成员管理
- 家庭权限控制
- 邀请码机制

### 💊 药品管理
- 药品信息录入
- OCR 图片识别（百度 AI）
- 用户药品库管理
- 药品图片存储（阿里云 OSS）

### 💉 处方管理
- 处方记录管理
- 处方项目管理
- 处方历史查询

### 🔔 智能提醒
- 用药提醒
- 健康检查提醒
- 定时任务调度
- 提醒历史记录

### 📊 健康数据
- 健康数据录入
- 健康报告生成
- 健康趋势分析
- 家庭健康概览

### 🤖 AI 智能助手
- 健康咨询对话
- WebSocket 实时通信
- 对话历史记录
- 智能健康建议

---

## 🚀 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 5.0+

### 数据库配置

1. 创建数据库

运行 healthAssistantDB.sql 脚本创建数据库和表;

2. 配置数据库连接

编辑 `HealthAssistant_server/src/main/resources/application-dev.yml`：

```yaml
sky:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    host: localhost
    port: 3306
    database: health_assistant
    username: root
    password: your_password
```

### Redis 配置

编辑 `application-dev.yml`：

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: 
    database: 0
```

### 第三方服务配置

配置以下服务的 API Key：

- 阿里云 OSS（文件存储）
- 百度 AI OCR（药品识别）
- DeepSeek API（AI 对话）
- 微信小程序（登录）

### 启动项目

```bash
# 编译项目
mvn clean install

# 启动服务
cd HealthAssistant_server
mvn spring-boot:run
```

服务启动后，访问：

- **API 文档**：http://localhost:8080/doc.html
- **服务地址**：http://localhost:8080

---

## 🏗️ 项目架构

### 模块结构

```
HealthAssistant (父模块)
├── HealthAssistant_common    # 公共工具模块
├── HealthAssistant_pojo      # 数据对象模块
└── HealthAssistant_server    # 主服务模块
```

### 模块依赖关系

```
HealthAssistant_server
    ├── 依赖 → HealthAssistant_pojo
    └── 依赖 → HealthAssistant_common

HealthAssistant_common
    └── 依赖 → HealthAssistant_pojo
```

### 分层架构

```
Controller 层 (控制器)
    ↓
Service 层 (业务逻辑)
    ↓
Mapper 层 (数据访问)
    ↓
MySQL 数据库
```

### 完整项目目录树

```
HealthAssistant/
├── pom.xml                                    # 父POM文件，统一管理依赖版本
├── README.md                                  # 项目文档
├── HealthAssistant_common/                    # 公共工具模块
│   ├── pom.xml                                # common模块POM
│   └── src/main/java/com/healthy/
│       ├── constant/
│       │   └── JwtClaimsConstant.java         # JWT声明常量定义
│       ├── context/
│       │   └── BaseContext.java               # 基础上下文，存储当前用户ID
│       ├── enumeration/
│       │   └── OperationType.java             # 操作类型枚举
│       ├── exception/
│       │   ├── BaseException.java             # 基础异常类
│       │   └── UserException.java             # 用户异常类
│       ├── json/
│       │   └── JacksonObjectMapper.java      # Jackson JSON配置
│       ├── properties/                        # 配置属性类
│       │   ├── AliOssProperties.java          # 阿里云OSS配置属性
│       │   ├── JwtProperties.java             # JWT配置属性
│       │   ├── OcrProperties.java             # OCR配置属性
│       │   └── WeChatProperties.java          # 微信配置属性
│       ├── result/                            # 统一响应结果
│       │   ├── Result.java                    # 通用响应结果封装
│       │   └── PageResult.java                # 分页响应结果
│       └── utils/                             # 工具类
│           ├── AliOssUtil.java                 # 阿里云OSS工具类
│           ├── HttpClientUtil.java            # HTTP客户端工具类
│           ├── ImageCompressUtil.java         # 图片压缩工具类
│           └── JwtUtil.java                   # JWT令牌工具类
├── HealthAssistant_pojo/                      # 数据对象模块
│   ├── pom.xml                                # pojo模块POM
│   └── src/main/java/com/healthy/
│       ├── dto/                               # 数据传输对象（Data Transfer Object）
│       │   ├── ChatRequestDTO.java            # AI聊天请求DTO
│       │   ├── CreateFamilyDTO.java           # 创建家庭DTO
│       │   ├── HealthDataDTO.java             # 健康数据DTO
│       │   ├── JoinFamilyDTO.java             # 加入家庭DTO
│       │   ├── LoginDTO.java                 # 登录请求DTO
│       │   ├── ManualSyncDTO.java            # 手动同步DTO
│       │   ├── OCRResultDTO.java             # OCR识别结果DTO
│       │   ├── PrescriptionItemDTO.java       # 处方项目DTO
│       │   ├── PrescriptionRecordDTO.java    # 处方记录DTO
│       │   ├── ReminderSaveRequest.java       # 提醒保存请求DTO
│       │   ├── SetAuthorizationDTO.java      # 设置授权DTO
│       │   ├── SetReminderBatchDTO.java       # 批量设置提醒DTO
│       │   ├── SetReminderDTO.java           # 设置提醒DTO
│       │   ├── SetReminderFromMedicineDTO.java # 从药品设置提醒DTO
│       │   ├── UpdateFamilyDTO.java          # 更新家庭DTO
│       │   ├── UpdateRelationDTO.java        # 更新关系DTO
│       │   ├── UpdateReminderDTO.java        # 更新提醒DTO
│       │   ├── UpdateRoleDTO.java            # 更新角色DTO
│       │   ├── UpdateUserDTO.java            # 更新用户DTO
│       │   └── UserMedicineDTO.java          # 用户药品DTO
│       ├── entity/                            # 数据库实体类
│       │   ├── Conversation.java              # AI对话记录实体
│       │   ├── DataAuthorization.java         # 数据授权实体
│       │   ├── Family.java                    # 家庭信息实体
│       │   ├── FamilyMember.java             # 家庭成员实体
│       │   ├── HealthData.java                # 健康数据实体
│       │   ├── HealthReport.java              # 健康报告实体
│       │   ├── MedicineImageRecord.java      # 药品图片记录实体
│       │   ├── MedicineInfo.java              # 药品信息实体
│       │   ├── PrescriptionItem.java          # 处方项目实体
│       │   ├── PrescriptionRecord.java         # 处方记录实体
│       │   ├── Reminder.java                  # 提醒实体
│       │   ├── UserMedicine.java              # 用户药品实体
│       │   └── Users.java                     # 用户实体
│       ├── properties/                        # 配置属性
│       │   └── DeepSeekProperties.java        # DeepSeek AI配置属性
│       └── vo/                                # 视图对象（View Object）
│           ├── AuthorizationVO.java          # 授权信息VO
│           ├── ChatResponseVO.java            # AI聊天响应VO
│           ├── EmergencyContactVO.java       # 紧急联系人VO
│           ├── FamilyInfoVO.java              # 家庭信息VO
│           ├── FamilyMemberVO.java            # 家庭成员VO
│           ├── FamilyReportVO.java            # 家庭报告VO
│           ├── FamilyVO.java                  # 家庭VO
│           ├── HealthDataVO.java              # 健康数据VO
│           ├── HealthHistoryVO.java           # 健康历史VO
│           ├── HealthOverviewVO.java          # 健康概览VO
│           ├── HealthReportVO.java            # 健康报告VO
│           ├── InviteCodeVO.java              # 邀请码VO
│           ├── LoginResultVO.java             # 登录结果VO
│           ├── MemberDetailVO.java            # 成员详情VO
│           ├── PrescriptionItemVO.java         # 处方项目VO
│           ├── PrescriptionRecordVO.java      # 处方记录VO
│           ├── UserMedicineVO.java            # 用户药品VO
│           └── UserVO.java                    # 用户VO
└── HealthAssistant_server/                    # 主服务模块
    ├── pom.xml                                # server模块POM
    ├── src/main/java/com/healthy/
    │   ├── SmartServerApplication.java       # Spring Boot启动类
    │   ├── config/                            # 配置类
    │   │   ├── AsyncConfig.java              # 异步任务配置
    │   │   ├── MyMetaObjectHandler.java      # MyBatis Plus元数据处理器
    │   │   ├── MybatisPlusConfig.java        # MyBatis Plus配置
    │   │   ├── OssConfiguration.java         # 阿里云OSS配置
    │   │   ├── RedisConfiguration.java       # Redis配置
    │   │   ├── RestTemplateConfig.java       # RestTemplate配置
    │   │   └── WebMvcConfiguration.java      # Web MVC配置
    │   ├── controller/                        # 控制器层（REST API）
    │   │   ├── AiController.java              # AI智能助手控制器
    │   │   ├── FamilyController.java          # 家庭管理控制器
    │   │   ├── HealthController.java         # 健康数据控制器
    │   │   ├── MedicineController.java        # 药品管理控制器
    │   │   ├── PrescriptionController.java    # 处方管理控制器
    │   │   ├── ReminderController.java       # 提醒管理控制器
    │   │   ├── UserController.java           # 用户管理控制器
    │   │   └── UserMedicineController.java   # 用户药品控制器
    │   ├── handler/                           # 异常处理器
    │   │   └── GlobalExceptionHandler.java    # 全局异常处理器
    │   ├── interceptor/                       # 拦截器
    │   │   ├── JwtTokenAdminInterceptor.java # 管理员JWT拦截器
    │   │   └── JwtTokenUserInterceptor.java  # 用户JWT拦截器
    │   ├── mapper/                            # 数据访问层（MyBatis Mapper）
    │   │   ├── ConversationMapper.java       # 对话记录Mapper
    │   │   ├── DataAuthorizationMapper.java  # 数据授权Mapper
    │   │   ├── FamilyMapper.java              # 家庭Mapper
    │   │   ├── FamilyMemberMapper.java       # 家庭成员Mapper
    │   │   ├── HealthDataMapper.java         # 健康数据Mapper
    │   │   ├── HealthReportMapper.java       # 健康报告Mapper
    │   │   ├── MedicineImageRecordMapper.java # 药品图片Mapper
    │   │   ├── MedicineInfoMapper.java       # 药品信息Mapper
    │   │   ├── PrescriptionItemMapper.java   # 处方项目Mapper
    │   │   ├── PrescriptionRecordMapper.java  # 处方记录Mapper
    │   │   ├── ReminderMapper.java           # 提醒Mapper
    │   │   ├── UserMapper.java               # 用户Mapper
    │   │   └── UserMedicineMapper.java       # 用户药品Mapper
    │   ├── security/                          # 安全控制
    │   │   └── FamilySecurity.java           # 家庭安全控制
    │   ├── service/                           # 服务层接口
    │   │   ├── AIGenerationService.java       # AI生成服务接口
    │   │   ├── AiChatService.java             # AI聊天服务接口
    │   │   ├── AsyncReportService.java        # 异步报告服务接口
    │   │   ├── AuthorizationService.java      # 授权服务接口
    │   │   ├── FamilyHealthService.java       # 家庭健康服务接口
    │   │   ├── FamilyMemberService.java       # 家庭成员服务接口
    │   │   ├── FamilyService.java             # 家庭服务接口
    │   │   ├── HealthService.java             # 健康服务接口
    │   │   ├── LoginService.java              # 登录服务接口
    │   │   ├── MedicineService.java           # 药品服务接口
    │   │   ├── OcrService.java               # OCR服务接口
    │   │   ├── PrescriptionService.java      # 处方服务接口
    │   │   ├── ReminderService.java           # 提醒服务接口
    │   │   ├── UserMedicineService.java      # 用户药品服务接口
    │   │   ├── UserService.java               # 用户服务接口
    │   │   └── WechatService.java            # 微信服务接口
    │   ├── service/impl/                      # 服务层实现
    │   │   ├── AiChatServiceImpl.java        # AI聊天服务实现
    │   │   ├── AsyncReportServiceImpl.java   # 异步报告服务实现
    │   │   ├── AuthorizationServiceImpl.java # 授权服务实现
    │   │   ├── DeepSeekServiceImpl.java      # DeepSeek AI服务实现
    │   │   ├── FamilyHealthServiceImpl.java   # 家庭健康服务实现
    │   │   ├── FamilyMemberServiceImpl.java  # 家庭成员服务实现
    │   │   ├── FamilyServiceImpl.java        # 家庭服务实现
    │   │   ├── HealthServiceImpl.java        # 健康服务实现
    │   │   ├── LoginServiceImpl.java         # 登录服务实现
    │   │   ├── MedicineServiceImpl.java      # 药品服务实现
    │   │   ├── OcrServiceImpl.java           # OCR服务实现
    │   │   ├── PrescriptionServiceImpl.java  # 处方服务实现
    │   │   ├── ReminderServiceImpl.java      # 提醒服务实现
    │   │   ├── UserMedicineServiceImpl.java # 用户药品服务实现
    │   │   ├── UserServiceImpl.java          # 用户服务实现
    │   │   └── WechatServiceImpl.java       # 微信服务实现
    │   └── task/                              # 定时任务
    │       └── ReminderScheduler.java         # 提醒定时任务调度器
    ├── src/main/resources/                   # 资源文件
    │   ├── application.yml                   # 主配置文件
    │   ├── application-dev.yml               # 开发环境配置
    │   ├── application-dev.yml.example        # 开发环境配置示例
    │   └── mapper/                            # MyBatis XML映射文件
    │       ├── DataAuthorizationMapper.xml   # 数据授权映射
    │       ├── FamilyMapper.xml               # 家庭映射
    │       ├── FamilyMemberMapper.xml        # 家庭成员映射
    │       ├── HealthDataMapper.xml           # 健康数据映射
    │       ├── HealthReportMapper.xml         # 健康报告映射
    │       ├── MedicineImageRecordMapper.xml # 药品图片映射
    │       ├── MedicineInfoMapper.xml        # 药品信息映射
    │       ├── PrescriptionItemMapper.xml    # 处方项目映射
    │       ├── PrescriptionRecordMapper.xml  # 处方记录映射
    │       ├── ReminderMapper.xml            # 提醒映射
    │       ├── UserMapper.xml                # 用户映射
    │       └── UserMedicineMapper.xml        # 用户药品映射
    └── src/test/java/com/healthy/            # 测试代码
        └── PollutionServerApplicationTests.java # 应用测试类
```

### 目录结构详细说明

#### 📁 HealthAssistant_pojo（数据对象模块）

**模块职责**：定义所有数据传输对象、数据库实体类、视图对象和配置属性类，是整个项目的数据模型层。

**主要包说明**：
- **dto/**：数据传输对象，用于接收前端请求参数和返回响应数据，实现前后端数据交互
- **entity/**：数据库实体类，对应数据库表结构，使用MyBatis Plus注解映射
- **vo/**：视图对象，用于封装返回给前端的业务数据，可包含多个实体数据的组合
- **properties/**：配置属性类，用于绑定配置文件中的配置项

**核心实体类**：
- [Users.java](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_pojo/src/main/java/com/healthy/entity/Users.java)：用户实体，包含微信openid、姓名、手机号、昵称、头像等信息
- [Family.java](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_pojo/src/main/java/com/healthy/entity/Family.java)：家庭实体，包含家庭名称、邀请码、创建时间等信息
- [HealthData.java](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_pojo/src/main/java/com/healthy/entity/HealthData.java)：健康数据实体，包含心率、血压、血氧、血糖、体温等健康指标
- [MedicineInfo.java](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_pojo/src/main/java/com/healthy/entity/MedicineInfo.java)：药品信息实体，包含药品名称、规格、生产厂家、批准文号、用法用量、成份、功能主治等

#### 📁 HealthAssistant_common（公共工具模块）

**模块职责**：提供通用的工具类、常量定义、异常处理、统一响应格式等公共组件，被其他模块依赖使用。

**主要包说明**：
- **constant/**：常量定义，如JWT声明常量
- **context/**：上下文管理，使用ThreadLocal存储当前线程的用户ID等信息
- **enumeration/**：枚举类，定义系统中使用的各种枚举类型
- **exception/**：异常类，定义业务异常和基础异常
- **json/**：JSON配置，定制Jackson序列化行为
- **properties/**：配置属性类，绑定第三方服务的配置
- **result/**：统一响应结果封装，定义标准的API响应格式
- **utils/**：工具类，提供JWT、OSS、HTTP、图片压缩等通用功能

**核心工具类**：
- [JwtUtil.java](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_common/src/main/java/com/healthy/utils/JwtUtil.java)：JWT令牌生成和验证工具
- [AliOssUtil.java](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_common/src/main/java/com/healthy/utils/AliOssUtil.java)：阿里云OSS文件上传下载工具
- [Result.java](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_common/src/main/java/com/healthy/result/Result.java)：统一响应结果封装，code=1表示成功，code=0表示失败

#### 📁 HealthAssistant_server（主服务模块）

**模块职责**：实现核心业务逻辑，提供REST API接口，处理HTTP请求，整合各个功能模块。

**主要包说明**：
- **config/**：配置类，包括异步配置、MyBatis Plus配置、OSS配置、Redis配置、Web MVC配置等
- **controller/**：控制器层，接收HTTP请求，调用Service层处理业务，返回响应结果
- **handler/**：异常处理器，全局捕获和处理异常，返回友好的错误信息
- **interceptor/**：拦截器，实现JWT令牌验证、用户身份认证等功能
- **mapper/**：数据访问层，使用MyBatis Plus进行数据库操作
- **security/**：安全控制，实现家庭数据权限控制
- **service/**：服务层接口，定义业务逻辑接口
- **service/impl/**：服务层实现，实现具体的业务逻辑
- **task/**：定时任务，使用Spring Task实现定时提醒等功能

**核心配置类**：
- [SmartServerApplication.java](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_server/src/main/java/com/healthy/SmartServerApplication.java)：Spring Boot启动类，启用定时任务调度
- [WebMvcConfiguration.java](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_server/src/main/java/com/healthy/config/WebMvcConfiguration.java)：Web MVC配置，注册拦截器
- [MybatisPlusConfig.java](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_server/src/main/java/com/healthy/config/MybatisPlusConfig.java)：MyBatis Plus配置，分页插件等
- [RedisConfiguration.java](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_server/src/main/java/com/healthy/config/RedisConfiguration.java)：Redis配置，序列化配置

**核心控制器**：
- [UserController.java](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_server/src/main/java/com/healthy/controller/UserController.java)：用户管理，登录、获取用户信息、更新用户信息
- [FamilyController.java](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_server/src/main/java/com/healthy/controller/FamilyController.java)：家庭管理，创建家庭、加入家庭、获取家庭信息、管理家庭成员
- [HealthController.java](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_server/src/main/java/com/healthy/controller/HealthController.java)：健康数据管理，添加健康数据、获取健康历史、生成健康报告
- [MedicineController.java](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_server/src/main/java/com/healthy/controller/MedicineController.java)：药品管理，添加药品信息、OCR识别药品
- [PrescriptionController.java](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_server/src/main/java/com/healthy/controller/PrescriptionController.java)：处方管理，添加处方、获取处方列表
- [ReminderController.java](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_server/src/main/java/com/healthy/controller/ReminderController.java)：提醒管理，设置提醒、获取提醒列表、删除提醒
- [AiController.java](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_server/src/main/java/com/healthy/controller/AiController.java)：AI智能助手，健康咨询对话、对话历史

**核心服务实现**：
- [LoginServiceImpl.java](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_server/src/main/java/com/healthy/service/impl/LoginServiceImpl.java)：登录服务实现，微信小程序登录、JWT令牌生成
- [FamilyServiceImpl.java](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_server/src/main/java/com/healthy/service/impl/FamilyServiceImpl.java)：家庭服务实现，创建家庭、生成邀请码、加入家庭
- [HealthServiceImpl.java](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_server/src/main/java/com/healthy/service/impl/HealthServiceImpl.java)：健康服务实现，健康数据录入、健康报告生成、健康趋势分析
- [OcrServiceImpl.java](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_server/src/main/java/com/healthy/service/impl/OcrServiceImpl.java)：OCR服务实现，调用百度AI识别药品图片
- [DeepSeekServiceImpl.java](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_server/src/main/java/com/healthy/service/impl/DeepSeekServiceImpl.java)：DeepSeek AI服务实现，健康咨询对话
- [ReminderScheduler.java](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_server/src/main/java/com/healthy/task/ReminderScheduler.java)：提醒定时任务，定时检查并发送提醒

**资源文件**：
- [application.yml](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_server/src/main/resources/application.yml)：主配置文件，包含服务器端口、数据库配置、JWT配置、MyBatis配置等
- [application-dev.yml](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_server/src/main/resources/application-dev.yml)：开发环境配置，包含数据库连接信息、Redis配置、第三方服务API密钥等
- **mapper/**：MyBatis XML映射文件，定义SQL查询语句和结果映射

---

## 🛠️ 技术栈

| 类别 | 技术选型 | 版本 | 用途 |
|------|---------|------|------|
| **核心框架** | Spring Boot | 2.7.3 | 应用框架 |
| **Java** | JDK | 17 | 开发语言 |
| **ORM框架** | MyBatis Plus | 3.5.3.1 | 数据库操作 |
| **数据库** | MySQL | 8.0 | 数据存储 |
| **缓存** | Redis | 5.0+ | 缓存、会话 |
| **连接池** | Druid | 1.2.12 | 数据库连接池 |
| **认证授权** | JWT | 0.9.1 | 用户认证 |
| **文件存储** | 阿里云 OSS | 3.10.2 | 图片存储 |
| **AI 服务** | DeepSeek API | - | 智能对话 |
| **OCR 识别** | 百度 AI SDK | 4.16.3 | 药品识别 |
| **API 文档** | Knife4j | 3.0.2 | 接口文档 |
| **深度学习** | DeepLearning4J | 1.0.0-M2.1 | 数据分析 |
| **Excel 处理** | Apache POI | 3.14 | 报表导出 |
| **实时通信** | WebSocket | - | AI 聊天 |
| **工具类** | Lombok | 1.18.30 | 代码简化 |
| **JSON 处理** | FastJSON | 1.2.76 | JSON 处理 |

---

## 📚 API 文档

项目使用 Knife4j 生成 API 文档，启动服务后访问：

```
http://localhost:8080/doc.html
```

### 主要 API 接口

#### 用户管理
- `POST /user/login` - 用户登录
- `GET /user/info` - 获取用户信息
- `PUT /user/update` - 更新用户信息

#### 家庭管理
- `POST /family/create` - 创建家庭
- `POST /family/join` - 加入家庭
- `GET /family/info` - 获取家庭信息
- `GET /family/members` - 获取家庭成员

#### 健康数据
- `POST /health/add` - 添加健康数据
- `GET /health/history` - 获取健康历史
- `GET /health/report` - 生成健康报告

#### 药品管理
- `POST /medicine/info` - 添加药品信息
- `POST /medicine/ocr` - OCR 识别药品
- `GET /medicine/list` - 获取药品列表

#### 处方管理
- `POST /prescription/add` - 添加处方
- `GET /prescription/list` - 获取处方列表

#### 提醒管理
- `POST /reminder/set` - 设置提醒
- `GET /reminder/list` - 获取提醒列表
- `DELETE /reminder/delete` - 删除提醒

#### AI 智能助手
- `POST /ai/chat` - AI 对话
- `GET /ai/history` - 对话历史

---

## 🔧 配置说明

### application.yml 主配置

```yaml
server:
  port: 8080

spring:
  profiles:
    active: dev
  datasource:
    druid:
      driver-class-name: com.mysql.cj.jdbc.Driver
      url: jdbc:mysql://localhost:3306/health_assistant
      username: root
      password: your_password

mybatis:
  mapper-locations: classpath:mapper/*.xml
  configuration:
    map-underscore-to-camel-case: true

sky:
  jwt:
    user-secret-key: healthy-assistant-jwt-secret-key-2024
    user-ttl: 7200000  # 2小时
    user-token-name: token
```

### 第三方服务配置

在 `application-dev.yml` 中配置：

```yaml
# 阿里云 OSS
aliyun:
  oss:
    endpoint: your-endpoint
    access-key-id: your-access-key-id
    access-key-secret: your-access-key-secret
    bucket-name: your-bucket-name

# 百度 AI OCR
baidu:
  aip:
    app-id: your-app-id
    api-key: your-api-key
    secret-key: your-secret-key

# DeepSeek AI
deepseek:
  api-key: your-deepseek-api-key
  api-url: https://api.deepseek.com

# 微信小程序
wechat:
  app-id: your-wechat-app-id
  app-secret: your-wechat-app-secret
```

---

## 📦 核心功能模块

### 1️⃣ 用户管理模块

**核心类**：
- [UserController](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_server/src/main/java/com/healthy/controller/UserController.java)
- [LoginServiceImpl](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_server/src/main/java/com/healthy/service/impl/LoginServiceImpl.java)

**功能**：
- 微信小程序登录
- JWT 令牌生成与验证
- 用户信息管理

**依赖**：微信 SDK、JWT

### 2️⃣ 家庭管理模块

**核心类**：
- [FamilyController](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_server/src/main/java/com/healthy/controller/FamilyController.java)
- [FamilyServiceImpl](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_server/src/main/java/com/healthy/service/impl/FamilyServiceImpl.java)

**功能**：
- 创建/加入家庭
- 家庭成员管理
- 权限管理
- 邀请码机制

**依赖**：Redis（缓存邀请码）

### 3️⃣ 健康数据模块

**核心类**：
- [HealthController](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_server/src/main/java/com/healthy/controller/HealthController.java)
- [HealthServiceImpl](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_server/src/main/java/com/healthy/service/impl/HealthServiceImpl.java)

**功能**：
- 健康数据录入
- 健康报告生成
- 健康趋势分析
- 家庭健康概览

**依赖**：DeepLearning4J（数据分析）

### 4️⃣ 药品管理模块

**核心类**：
- [MedicineController](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_server/src/main/java/com/healthy/controller/MedicineController.java)
- [OcrServiceImpl](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_server/src/main/java/com/healthy/service/impl/OcrServiceImpl.java)

**功能**：
- 药品信息管理
- OCR 图片识别
- 用户药品库

**依赖**：百度 AI OCR、阿里云 OSS

### 5️⃣ 处方管理模块

**核心类**：
- [PrescriptionController](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_server/src/main/java/com/healthy/controller/PrescriptionController.java)
- [PrescriptionServiceImpl](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_server/src/main/java/com/healthy/service/impl/PrescriptionServiceImpl.java)

**功能**：
- 处方记录管理
- 处方项目管理
- 处方历史查询

### 6️⃣ 提醒管理模块

**核心类**：
- [ReminderController](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_server/src/main/java/com/healthy/controller/ReminderController.java)
- [ReminderScheduler](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_server/src/main/java/com/healthy/task/ReminderScheduler.java)

**功能**：
- 用药提醒
- 健康检查提醒
- 定时任务调度

**依赖**：Spring Task

### 7️⃣ AI 智能助手模块

**核心类**：
- [AiController](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_server/src/main/java/com/healthy/controller/AiController.java)
- [DeepSeekServiceImpl](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_server/src/main/java/com/healthy/service/impl/DeepSeekServiceImpl.java)

**功能**：
- 健康咨询对话
- WebSocket 实时通信
- 对话历史记录

**依赖**：DeepSeek API、WebSocket

---

## 🎯 项目特点

1. **模块化设计**：清晰的模块划分，职责明确
2. **分层架构**：Controller → Service → Mapper 三层架构
3. **统一响应**：使用 [Result](file:///D:/develop/JavaIDEA/health-assistant/HealthAssistant_common/src/main/java/com/healthy/result/Result.java) 封装统一响应格式
4. **异常处理**：全局异常处理器统一处理异常
5. **拦截器**：JWT 拦截器实现用户认证
6. **定时任务**：Spring Task 实现提醒定时任务
7. **异步处理**：异步配置支持耗时操作异步执行
8. **缓存优化**：Redis 缓存提升性能
9. **AI 集成**：集成 DeepSeek 和百度 AI 提供智能服务
10. **微信集成**：支持微信小程序登录

<div align="center">

**感谢使用 HealthAssistant！**

Made with ❤️ by HealthAssistant Team

</div>