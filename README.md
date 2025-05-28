# JavaEE设计项目 - 学生管理系统

## 项目简介

这是一个基于Java EE设计的学生管理系统，采用自定义注解和处理器实现类似Spring MVC的功能。项目使用Maven构建，支持RESTful API风格的学生信息管理。

## 技术栈

- **Java**: 21
- **构建工具**: Maven
- **核心依赖**:
  - Lombok 1.18.38 (简化代码)
  - Servlet API 2.5 (Web容器支持)
  - Jackson 2.15.2 (JSON序列化)
## 核心功能

### 学生管理API

**基础路径**: `/student`

#### 1. 获取学生列表
- **路径**: `GET /student/list`
- **参数**:
  - `page`: 页码 (必填)
  - `size`: 每页数量 (可选，默认10)
- **响应**: 返回学生列表

#### 2. 获取学生详情
- **路径**: `GET /student/info/{id}`
- **参数**:
  - `id`: 学生ID (路径变量)
- **响应**: 返回单个学生信息

#### 3. 添加学生
- **路径**: `POST /student/add`
- **参数**: StudentDTO对象 (请求体)
- **响应**: 返回添加结果

#### 4. 更新学生信息
- **路径**: `PUT /student/update/{id}`
- **参数**:
  - `id`: 学生ID (路径变量)
  - StudentDTO对象 (请求体)
- **响应**: 返回更新结果

#### 5. 删除学生
- **路径**: `DELETE /student/delete/{id}`
- **参数**:
  - `id`: 学生ID (路径变量)
- **响应**: 返回删除结果

### 数据模型

#### StudentDTO
```json
{
  "id": "学生ID",
  "name": "学生姓名",
  "age": "年龄",
  "teacherId": "教师ID",
  "clazz": "班级"
}
```

统一响应格式
所有API接口都使用统一的Result响应格式：
```
json
{  "code": 状态码,  "msg": "响应消息",  "data": 响应数据}
```
状态码说明:
200: 成功
500: 服务器错误
其他自定义错误码
## 核心特性
### 1. 自定义注解系统
- 实现了类似Spring MVC的注解驱动开发
- 支持 @Controller 、 @GetMapping 、 @PostMapping 等注解
- 自动处理路径变量 @PathVariable 和请求参数 @RequestParam
### 2. 增强的HTTP处理器
- 自动JSON序列化/反序列化
- 智能参数绑定
- 统一异常处理
### 3. 统一响应处理
- 使用Result工具类统一API响应格式
- 支持成功和错误状态的标准化处理
