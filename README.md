
# JavaEE设计项目 - 学生管理系统

## 项目简介

这是一个基于Java EE设计的学生管理系统，采用自定义注解和处理器实现类似Spring框架的功能。项目使用Maven构建，支持RESTful API风格的学生、教师、班级管理，并集成了AI对话功能。该项目展示了从零开始构建一个完整的Web框架的能力，包括依赖注入、事务管理、AOP等核心特性。

## 技术栈

- **Java**: 17
- **构建工具**: Maven
- **Web服务器**: 内嵌Tomcat 11.0.6
- **数据库**: MySQL 8.2.0 + HikariCP连接池
- **AI集成**: LangChain4j + Ollama
- **核心依赖**:
  - Lombok 1.18.38 (简化代码)
  - Jakarta Servlet API 6.0.0 (Web容器支持)
  - Jackson 2.15.2 (JSON序列化)
  - SLF4J + Logback (日志框架)
  - JUnit 5 + Mockito (测试框架)

## 项目架构

### 核心模块

```
src/main/java/site/arookieofc/
├── annotation/          # 自定义注解系统
│   ├── config/         # 配置相关注解
│   ├── sql/            # SQL相关注解
│   ├── transactional/  # 事务注解
│   └── web/            # Web相关注解
├── controller/         # 控制器层
├── dao/               # 数据访问层
├── entity/            # 实体类
├── filter/            # 过滤器
├── pojo/dto/          # 数据传输对象
├── processor/         # 注解处理器
│   ├── config/        # 配置处理器
│   ├── sql/           # SQL处理器
│   ├── transaction/   # 事务处理器
│   └── web/           # Web处理器
├── server/            # 内嵌服务器
├── service/           # 业务逻辑层
└── utils/             # 工具类
```

## 核心功能

### 1. 学生管理API

**基础路径**: `/student`

#### 获取学生列表（分页）
- **路径**: `GET /student/page`
- **参数**:
  - `page`: 页码 (必填)
  - `size`: 每页数量 (可选，默认10)
- **响应**: 返回分页学生列表，包含教师姓名

#### 获取学生详情
- **路径**: `GET /student/info/{id}`
- **参数**:
  - `id`: 学生ID (路径变量)
- **响应**: 返回单个学生信息

#### 添加学生
- **路径**: `POST /student/add`
- **参数**: StudentDTO对象 (请求体)
- **响应**: 返回添加结果

#### 更新学生信息
- **路径**: `PUT /student/update/{id}`
- **参数**:
  - `id`: 学生ID (路径变量)
  - StudentDTO对象 (请求体)
- **响应**: 返回更新结果

#### 删除学生
- **路径**: `DELETE /student/delete/{id}`
- **参数**:
  - `id`: 学生ID (路径变量)
- **响应**: 返回删除结果

### 2. 教师管理API

**基础路径**: `/teacher`

- `GET /teacher/list` - 获取所有教师
- `POST /teacher/add` - 添加教师
- `PUT /teacher/update/{id}` - 更新教师信息
- `DELETE /teacher/delete/{id}` - 删除教师

### 3. 班级管理API

**基础路径**: `/class`

- `GET /class/list` - 获取所有班级
- `POST /class/add` - 添加班级
- `PUT /class/update/{id}` - 更新班级信息
- `DELETE /class/delete/{id}` - 删除班级

### 4. AI对话功能

**基础路径**: `/ai`

#### 单轮对话
- **路径**: `POST /ai/chat`
- **参数**: `{"message": "用户消息"}`
- **响应**: 返回AI回复

#### 多轮对话
- **路径**: `POST /ai/chat-history`
- **参数**: `{"messages": [{"role": "user", "content": "消息内容"}]}`
- **响应**: 返回AI回复

#### AI服务状态
- **路径**: `GET /ai/status`
- **响应**: 返回AI服务状态

### 数据模型

#### StudentDTO
```json
{
  "id": "学生ID",
  "name": "学生姓名",
  "age": "年龄",
  "teacherId": "教师ID",
  "clazz": "班级",
  "teacherName": "教师姓名（仅用于显示）"
}
```

#### Teacher
```json
{
  "id": "教师ID",
  "name": "教师姓名",
  "department": "所属部门"
}
```

#### Clazz
```json
{
  "id": "班级ID",
  "name": "班级名称",
  "teacherId": "班主任ID",
  "studentCount": "学生数量",
  "description": "班级描述"
}
```

### 统一响应格式
所有API接口都使用统一的Result响应格式：
```json
{
  "code": "状态码",
  "msg": "响应消息",
  "data": "响应数据"
}
```

**状态码说明**:
- 200: 成功
- 500: 服务器错误
- 其他自定义错误码

## 核心特性

### 1. 自定义注解系统
- **Web注解**: `@Controller`、`@GetMapping`、`@PostMapping`、`@PutMapping`、`@DeleteMapping`
- **参数注解**: `@PathVariable`、`@RequestParam`、`@RequestBody`
- **配置注解**: `@Config`、`@Application`
- **事务注解**: `@Transactional`（支持传播级别、隔离级别、超时设置等）
- **SQL注解**: 自定义SQL映射注解

### 2. 自研Web框架
- 类似Spring MVC的注解驱动开发
- 自动路由映射和参数绑定
- 智能类型转换和JSON序列化/反序列化
- 统一异常处理机制
- CORS跨域支持

### 3. 事务管理系统
- 声明式事务管理
- 支持事务传播机制（REQUIRED、REQUIRES_NEW等）
- 支持事务隔离级别设置
- 自动回滚机制
- 基于AOP的事务拦截器

### 4. 配置管理
- YAML配置文件支持
- 自动配置注入
- 支持静态字段注入
- 环境变量覆盖

### 5. 数据库集成
- HikariCP高性能连接池
- 自定义DAO层实现
- 支持复杂查询和分页
- 事务级别的连接管理

### 6. AI集成
- 集成Ollama本地AI模型
- 支持单轮和多轮对话
- 可配置AI模型和服务地址
- 异步处理和错误恢复

### 7. 内嵌服务器
- 基于Tomcat 11的内嵌服务器
- 支持热部署和动态配置
- 自动端口配置
- 生产环境就绪

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Ollama (可选，用于AI功能)

### 配置数据库
1. 创建数据库 `student`
2. 修改 `src/main/resources/config.yml` 中的数据库配置

### 配置AI服务（可选）
1. 安装并启动Ollama
2. 下载AI模型：`ollama pull qwen3:14b`
3. 修改配置文件中的AI相关配置

### 启动应用
```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="site.arookieofc.Main"
```

或者直接运行Main类

### 访问应用
- 应用地址：http://localhost:8080
- API文档：所有接口都支持RESTful风格访问

## 测试

项目包含完整的单元测试和集成测试：

```bash
# 运行所有测试
mvn test

# 运行特定测试
mvn test -Dtest=TransactionTest
```

## 项目特色

1. **从零构建**: 完全自主实现的Web框架，不依赖Spring等重型框架
2. **教学价值**: 展示了现代Java Web框架的核心实现原理
3. **生产就绪**: 包含完整的事务管理、连接池、异常处理等企业级特性
4. **AI集成**: 前瞻性地集成了本地AI能力
5. **测试完备**: 包含单元测试、集成测试和事务测试

## 开发计划

- [ ] 添加缓存支持（Redis集成）
- [ ] 实现更多数据库操作注解
- [ ] 添加安全认证和授权
- [ ] 支持微服务架构
- [ ] 添加监控和指标收集
- [ ] 完善前端Vue.js界面

## 贡献

欢迎提交Issue和Pull Request来改进这个项目。

## 许可证

本项目采用MIT许可证。

```bash
curl -X POST http://localhost:8080/ai/chat-stream -H "Content-Type: application/json" -d '{"message":"查询所有的老师"}'
```
        