/**
 * API服务封装
 */
import axios from 'axios'

// 基础API类
class ApiService {
  constructor(baseURL = '') {
    this.baseURL = baseURL
  }

  // GET请求
  async get(url, params = {}) {
    try {
      const response = await axios.get(this.baseURL + url, { params })
      return response.data
    } catch (error) {
      throw error
    }
  }

  // POST请求
  async post(url, data = {}) {
    try {
      const response = await axios.post(this.baseURL + url, data)
      return response.data
    } catch (error) {
      throw error
    }
  }

  // PUT请求
  async put(url, data = {}) {
    try {
      const response = await axios.put(this.baseURL + url, data)
      return response.data
    } catch (error) {
      throw error
    }
  }

  // DELETE请求
  async delete(url) {
    try {
      const response = await axios.delete(this.baseURL + url)
      return response.data
    } catch (error) {
      throw error
    }
  }
}

// 学生API
export const studentApi = {
  // 获取学生列表
  getList: (params) => new ApiService().get('/students', params),
  
  // 根据ID获取学生
  getById: (id) => new ApiService().get(`/students/${id}`),
  
  // 添加学生
  create: (data) => new ApiService().post('/students', data),
  
  // 更新学生
  update: (id, data) => new ApiService().put(`/students/${id}`, data),
  
  // 删除学生
  delete: (id) => new ApiService().delete(`/students/${id}`),
  
  // 按条件搜索学生
  search: (params) => new ApiService().get('/students/search', params)
}

// 教师API
export const teacherApi = {
  // 获取教师列表
  getList: (params) => new ApiService().get('/teachers', params),
  
  // 根据ID获取教师
  getById: (id) => new ApiService().get(`/teachers/${id}`),
  
  // 添加教师
  create: (data) => new ApiService().post('/teachers', data),
  
  // 更新教师
  update: (id, data) => new ApiService().put(`/teachers/${id}`, data),
  
  // 删除教师
  delete: (id) => new ApiService().delete(`/teachers/${id}`),
  
  // 按条件搜索教师
  search: (params) => new ApiService().get('/teachers/search', params)
}

// 班级API
export const classApi = {
  // 获取班级列表
  getList: (params) => new ApiService().get('/classes', params),
  
  // 根据ID获取班级
  getById: (id) => new ApiService().get(`/classes/${id}`),
  
  // 添加班级
  create: (data) => new ApiService().post('/classes', data),
  
  // 更新班级
  update: (id, data) => new ApiService().put(`/classes/${id}`, data),
  
  // 删除班级
  delete: (id) => new ApiService().delete(`/classes/${id}`),
  
  // 按条件搜索班级
  search: (params) => new ApiService().get('/classes/search', params),
  
  // 获取班级下的学生
  getStudents: (id) => new ApiService().get(`/classes/${id}/students`)
}

// 认证API
export const authApi = {
  // 用户登录
  login: (data) => new ApiService().post('/auth/login', data),
  
  // 用户注册
  register: (data) => new ApiService().post('/auth/register', data),
  
  // 刷新token
  refreshToken: (data) => new ApiService().post('/auth/refresh', data),
  
  // 获取当前用户信息
  getCurrentUser: () => new ApiService().get('/auth/me'),
  
  // 用户登出
  logout: () => new ApiService().post('/auth/logout'),
  
  // 修改密码
  changePassword: (data) => new ApiService().post('/auth/change-password', data)
}

// AI聊天API
export const aiApi = {
  // 发送聊天消息
  chat: (data) => new ApiService().post('/ai/chat', data),
  
  // 获取聊天历史
  getChatHistory: (params) => new ApiService().get('/ai/chat/history', params),
  
  // 清除聊天历史
  clearChatHistory: () => new ApiService().delete('/ai/chat/history')
}

// 统计API
export const statsApi = {
  // 获取总体统计信息
  getOverview: () => new ApiService().get('/stats/overview'),
  
  // 获取学生统计
  getStudentStats: () => new ApiService().get('/stats/students'),
  
  // 获取教师统计
  getTeacherStats: () => new ApiService().get('/stats/teachers'),
  
  // 获取班级统计
  getClassStats: () => new ApiService().get('/stats/classes')
}

// 导出默认API服务
export default {
  student: studentApi,
  teacher: teacherApi,
  class: classApi,
  auth: authApi,
  ai: aiApi,
  stats: statsApi
}