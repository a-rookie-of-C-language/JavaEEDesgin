# JavaEE设计项目 - 学生管理系统

## 项目简介

这是一个基于Java EE设计的学生管理系统，采用自定义注解和处理器实现类似Spring框架的功能。项目使用Maven构建，
支持RESTful API风格的学生、教师、班级管理，并集成了AI对话功能和MCP（Model Context Protocol）服务。
该项目展示了从零开始构建一个完整的Web框架的能力，包括依赖注入、事务管理、AOP、数据验证等核心特性。
## 技术栈
- **Java**: 17
- **构建工具**: Maven
- **Web服务器**: 内嵌Tomcat 11.0.6
- **数据库**: MySQL 8.0+ + HikariCP连接池
- **AI集成**: LangChain4j + Ollama
- **MCP集成**: Model Context Protocol SDK 0.10.0
- **核心依赖**:
  - Lombok 1.18.38 (简化代码)
  - Jakarta Servlet API 6.0.0 (Web容器支持)
  - Jackson 2.15.2 (JSON序列化)
  - SLF4J + Logback (日志框架)
  - Auth0 Java JWT 4.3.0 (JWT认证)
  - Reactor Core (响应式编程)
## 项目架构

### 核心模块

```
src/main/java/site/arookieofc/
├── annotation/          # 自定义注解系统
│   ├── config/         # 配置相关注解 (@Application, @Config, @ComponentScan)
│   ├── ioc/            # IOC容器注解 (@Component, @Autowired, @Bean, @Lazy)
│   ├── sql/            # SQL相关注解 (@SQL)
│   ├── transactional/  # 事务注解 (@Transactional, @Isolation, @Propagation)
│   ├── validation/     # 数据验证注解 (@NotNull, @NotEmpty, @Need, @Range, @Size, @Exists)
│   └── web/            # Web相关注解 (@Controller, @RequestMapping, @PathVariable等)
├── controller/         # 控制器层 (StudentController, TeacherController, ClazzController, AiController)
├── dao/               # 数据访问层 (StudentDAO, TeacherDAO, ClazzDAO)
├── exception/         # 异常处理
├── filter/            # 过滤器
├── pojo/              # 数据对象
│   ├── DO/            # 数据库实体 (Student, Teacher, Clazz)
│   ├── dto/           # 数据传输对象 (StudentDTO, PageResult, Result)
│   └── vo/            # 视图对象 (StudentVO, TeacherVO, ClazzVO)
├── processor/         # 注解处理器
│   ├── config/        # 配置处理器 (ConfigProcessor)
│   ├── ioc/           # IOC容器实现 (AnnotationApplicationContext, BeanFactory等)
│   ├── sql/           # SQL处理器 (SQLExecutor)
│   ├── transaction/   # 事务处理器 (TransactionManager, TransactionInterceptor)
│   ├── validation/    # 验证处理器 (ValidationProcessor, ValidationInterceptor)
│   └── web/           # Web处理器 (HttpMappingProcessor, GlobalExceptionHandler)
├── server/            # 内嵌服务器 (EmbeddedTomcatServer, McpServer)
├── service/           # 业务逻辑层
│   └── impl/          # 服务实现 (AiServiceImpl, StudentServiceImpl等)
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

#### 流式对话
- **路径**: `POST /ai/chat-stream`
- **参数**: `{"message": "用户消息"}`
- **响应**: 返回SSE流式AI回复
- **支持**: 自动识别业务操作并调用MCP工具

#### AI服务状态
- **路径**: `GET /ai/status`
- **响应**: 返回AI服务状态

### 5. MCP集成功能

**MCP服务器端口**: 3001

#### 支持的MCP工具
- **学生管理工具**: 查询、添加、更新、删除学生
- **教师管理工具**: 查询、添加、更新、删除教师
- **班级管理工具**: 查询、添加、更新、删除班级

#### MCP端点
- **SSE端点**: `/sse`
- **消息端点**: `/message`

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
- **Web注解**: `@Controller`、`@GetMapping`、`@PostMapping`、`@PutMapping`、`@DeleteMapping`、`@RequestMapping`
- **参数注解**: `@PathVariable`、`@RequestParam`、`@RequestBody`
- **IOC注解**: `@Component`、`@Autowired`、`@Bean`、`@Lazy`
- **配置注解**: `@Config`、`@Application`、`@ComponentScan`
- **事务注解**: `@Transactional`（支持传播级别、隔离级别、超时设置等）
- **验证注解**: `@NotNull`、`@NotEmpty`、`@Need`、`@Range`、`@Size`、`@Exists`
- **SQL注解**: `@SQL`自定义SQL映射注解
- **异常处理注解**: `@ExceptionHandler`、`@ControllerException`

### 2. 自研Web框架
- 类似Spring MVC的注解驱动开发
- 自动路由映射和参数绑定
- 智能类型转换和JSON序列化/反序列化
- 统一异常处理机制
- CORS跨域支持
- 组件自动扫描和注册

### 3. IOC容器系统
- 完整的依赖注入实现
- 支持三级缓存解决循环依赖
- Bean生命周期管理
- 懒加载支持
- 自动装配和类型匹配
- ApplicationContext上下文管理

### 4. 事务管理系统
- 声明式事务管理
- 支持事务传播机制（REQUIRED、REQUIRES_NEW、SUPPORTS、NOT_SUPPORTED、NEVER、MANDATORY）
- 支持事务隔离级别设置
- 自动回滚机制
- 基于AOP的事务拦截器
- 嵌套事务支持

### 5. 数据验证框架
- 声明式数据验证
- 支持多种验证注解
- 自动参数验证
- 自定义验证规则
- 验证失败自动异常处理

### 6. 配置管理
- YAML配置文件支持
- 自动配置注入
- 支持静态字段注入
- 环境变量覆盖
- 默认值支持

### 7. 数据库集成
- HikariCP高性能连接池
- 自定义DAO层实现
- 支持复杂查询和分页
- 事务级别的连接管理
- 自动SQL执行和结果映射

### 8. AI集成
- 集成Ollama本地AI模型
- 支持流式对话响应
- 智能业务操作识别
- MCP工具自动调用
- 可配置AI模型和服务地址
- 异步处理和错误恢复

### 9. MCP集成
- Model Context Protocol服务器实现
- 业务工具自动注册
- SSE传输支持
- 学生、教师、班级管理工具集成
- 与AI模型无缝协作

### 10. 内嵌服务器
- 基于Tomcat 11的内嵌服务器
- 支持热部署和动态配置
- 自动端口配置
- 双服务器架构（Web服务器 + MCP服务器）
- 生产环境就绪

### 11. JWT认证支持
- JWT令牌生成和验证
- 可配置的过滤器
- 路径排除支持
- 安全认证机制

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Ollama (可选，用于AI功能)

### 配置数据库
1. 创建数据库 `student`
2. 修改 `src/main/resources/config.yml` 中的数据库配置：
```yaml
database:
  url: jdbc:mysql://127.0.0.1:3306/student?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
  username: your_username
  password: your_password
```

### 配置AI服务（可选）
1. 安装并启动Ollama
2. 下载AI模型：`ollama pull qwen3:14b`
3. 修改配置文件中的AI相关配置：
```yaml
ai:
  url: http://127.0.0.1:11434
  name: qwen3:14b
  timeout:
    request: 60000
```

### 配置MCP服务（可选）
```yaml
mcp:
  server:
    port: 3001
    name: business-mcp-server
    version: 1.0.0
```

### 启动应用
```bash
# 编译项目
mvn clean compile

# 运行应用
mvn exec:java -Dexec.mainClass="site.arookieofc.Main"
```

或者直接运行Main类

### 访问应用
- **Web应用地址**: http://localhost:8080
- **MCP服务地址**: http://localhost:3001
- **API测试**: 使用提供的Postman集合 `controller_test_collection.json`

## 测试

项目支持完整的测试框架（测试用例待补充）：

```bash
# 运行所有测试
mvn test

# 运行特定测试
mvn test -Dtest=TransactionTest
```

## API测试

项目提供了完整的Postman测试集合 `controller_test_collection.json`，包含：
- 学生管理API测试
- 教师管理API测试
- 班级管理API测试
- AI对话API测试

### 示例API调用

```bash
# 测试AI对话（支持MCP工具调用）
curl -X POST http://localhost:8080/ai/chat-stream \
  -H "Content-Type: application/json" \
  -d '{"message":"查询所有的老师"}'

# 获取学生列表
curl -X GET "http://localhost:8080/student/page?page=1&size=10"

# 添加学生
curl -X POST http://localhost:8080/student/add \
  -H "Content-Type: application/json" \
  -d '{"name":"张三","age":20,"teacherId":"T001","clazz":"计算机1班"}'
```

## 项目特色

1. **从零构建**: 完全自主实现的Web框架，不依赖Spring等重型框架
2. **教学价值**: 展示了现代Java Web框架的核心实现原理
3. **生产就绪**: 包含完整的事务管理、连接池、异常处理等企业级特性
4. **AI集成**: 前瞻性地集成了本地AI能力和MCP协议
5. **架构完整**: IOC容器、AOP、数据验证、配置管理等核心功能齐全
6. **代码质量**: 使用Lombok简化代码，完整的日志记录
7. **易于扩展**: 模块化设计，支持自定义注解和处理器
        