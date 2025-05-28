import axios from 'axios'

const API_BASE = '/student'

export const studentApi = {
  // 获取学生列表（分页）
  getStudentList(page, size = 10) {
    console.log('API调用参数:', { page, size })
    return axios.get(`${API_BASE}/list`, {
      params: { 
        page: page, 
        size: size 
      }
    }).then(response => {
      console.log('axios原始响应:', response)
      console.log('响应数据:', response.data)
      return response.data // 返回完整的后端Result对象，包含code、msg、data
    }).catch(error => {
      console.error('API请求失败:', error)
      throw error
    })
  },

  // 获取学生详情
  getStudentInfo(id) {
    return axios.get(`${API_BASE}/info/${id}`)
  },

  // 添加学生
  addStudent(studentData) {
    return axios.post(`${API_BASE}/add`, studentData)
  },

  // 更新学生信息
  updateStudent(id, studentData) {
    return axios.put(`${API_BASE}/update/${id}`, studentData)
  },

  // 删除学生
  deleteStudent(id) {
    return axios.delete(`${API_BASE}/delete/${id}`)
  },

  // 按班级查询学生
  getStudentsByClass(clazz) {
    return axios.get(`${API_BASE}/class/${clazz}`)
  },

  // 按教师查询学生
  getStudentsByTeacher(teacherId) {
    return axios.get(`${API_BASE}/teacher/${teacherId}`)
  },  // 添加这个逗号！
  
  // 获取所有教师列表
  getAllTeachers() {
    return axios.get(`${API_BASE}/teachers`).then(response => {
      console.log('获取教师列表响应:', response.data)
      return response.data
    }).catch(error => {
      console.error('获取教师列表失败:', error)
      throw error
    })
  },
  
  // 获取所有班级列表
  getAllClasses() {
    return axios.get(`${API_BASE}/classes`).then(response => {
      console.log('获取班级列表响应:', response.data)
      return response.data
    }).catch(error => {
      console.error('获取班级列表失败:', error)
      throw error
    })
  }
}