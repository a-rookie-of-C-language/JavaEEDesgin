/**
 * 认证相关工具函数
 */

// Token存储的key
const TOKEN_KEY = 'token'
const USER_INFO_KEY = 'userInfo'

/**
 * 获取token
 * @returns {string|null} token值
 */
export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

/**
 * 设置token
 * @param {string} token - JWT token
 */
export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

/**
 * 移除token
 */
export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
}

/**
 * 检查是否已登录
 * @returns {boolean} 是否已登录
 */
export function isLoggedIn() {
  const token = getToken()
  if (!token) return false
  
  try {
    // 解析JWT token的payload部分
    const payload = JSON.parse(atob(token.split('.')[1]))
    const currentTime = Date.now() / 1000
    
    // 检查token是否过期
    if (payload.exp && payload.exp < currentTime) {
      removeToken()
      removeUserInfo()
      return false
    }
    
    return true
  } catch (error) {
    console.error('Token解析失败:', error)
    removeToken()
    removeUserInfo()
    return false
  }
}

/**
 * 获取用户信息
 * @returns {object|null} 用户信息
 */
export function getUserInfo() {
  const userInfo = localStorage.getItem(USER_INFO_KEY)
  return userInfo ? JSON.parse(userInfo) : null
}

/**
 * 设置用户信息
 * @param {object} userInfo - 用户信息对象
 */
export function setUserInfo(userInfo) {
  localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo))
}

/**
 * 移除用户信息
 */
export function removeUserInfo() {
  localStorage.removeItem(USER_INFO_KEY)
}

/**
 * 清除所有认证信息
 */
export function clearAuth() {
  removeToken()
  removeUserInfo()
}

/**
 * 从token中解析用户信息
 * @returns {object|null} 解析出的用户信息
 */
export function parseTokenInfo() {
  const token = getToken()
  if (!token) return null
  
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return {
      userId: payload.sub || payload.userId,
      username: payload.username,
      roles: payload.roles || [],
      exp: payload.exp,
      iat: payload.iat
    }
  } catch (error) {
    console.error('Token解析失败:', error)
    return null
  }
}

/**
 * 检查用户是否有指定权限
 * @param {string|array} permission - 权限名称或权限数组
 * @returns {boolean} 是否有权限
 */
export function hasPermission(permission) {
  const tokenInfo = parseTokenInfo()
  if (!tokenInfo || !tokenInfo.roles) return false
  
  if (Array.isArray(permission)) {
    return permission.some(p => tokenInfo.roles.includes(p))
  }
  
  return tokenInfo.roles.includes(permission)
}

/**
 * 检查用户是否是管理员
 * @returns {boolean} 是否是管理员
 */
export function isAdmin() {
  return hasPermission(['admin', 'administrator', 'ADMIN'])
}