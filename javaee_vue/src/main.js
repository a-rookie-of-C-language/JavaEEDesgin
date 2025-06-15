import './assets/main.css'
import { createApp } from 'vue'
import App from './App.vue'
import router from '../router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, clearAuth } from './utils/auth'

// 配置axios基础URL
axios.defaults.baseURL = 'http://localhost:8080'
axios.defaults.timeout = 10000

// 设置请求编码为UTF-8
axios.defaults.headers.common['Content-Type'] = 'application/json;charset=UTF-8'
axios.defaults.headers.post['Content-Type'] = 'application/json;charset=UTF-8'
axios.defaults.headers.put['Content-Type'] = 'application/json;charset=UTF-8'

// 请求拦截器
axios.interceptors.request.use(
  config => {
    // 从localStorage获取token
    const token = getToken()
    if (token) {
      // 在请求头中添加Authorization字段
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // 确保请求头包含UTF-8编码
    if (!config.headers['Content-Type']) {
      config.headers['Content-Type'] = 'application/json;charset=UTF-8'
    }
    
    // 如果是POST、PUT、PATCH请求且数据是对象，确保正确序列化
    if (['post', 'put', 'patch'].includes(config.method?.toLowerCase()) && 
        typeof config.data === 'object' && config.data !== null) {
      config.data = JSON.stringify(config.data)
    }
    
    return config
  },
  error => {
    console.error('请求拦截器错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
axios.interceptors.response.use(
  response => {
    // 请求成功，直接返回响应数据
    return response
  },
  error => {
    console.error('响应拦截器错误:', error)
    
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          // 未授权，清除所有认证信息
          clearAuth()
          ElMessage.error('登录已过期，请重新登录')
          // 如果有登录页面，可以跳转
          // router.push('/login')
          break
        case 403:
          ElMessage.error('没有权限访问该资源')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(data?.message || '请求失败')
      }
    } else if (error.request) {
      // 网络错误
      ElMessage.error('网络连接失败，请检查网络')
    } else {
      ElMessage.error('请求配置错误')
    }
    
    return Promise.reject(error)
  }
)

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(router)
app.use(ElementPlus)
app.config.globalProperties.$http = axios
app.mount('#app')
