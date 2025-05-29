<template>
  <div id="app">
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header class="header">
        <div class="header-content">
          <h1 class="title">学生管理系统</h1>
          <el-menu
            :default-active="activeIndex"
            class="nav-menu"
            mode="horizontal"
            @select="handleSelect"
          >
            <el-menu-item index="students">学生管理</el-menu-item>
            <el-menu-item index="teachers">教师管理</el-menu-item>
            <el-menu-item index="clazzes">班级管理</el-menu-item>
            <el-menu-item index="ai-chat">AI助手</el-menu-item>
          </el-menu>
        </div>
      </el-header>

      <!-- 主内容区域 -->
      <el-main class="main-content">
        <component :is="currentComponent" />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import StudentManagement from './components/StudentManagement.vue'
import TeacherManagement from './components/TeacherManagement.vue'
import ClassManagement from './components/ClassManagement.vue'
import AiChat from './components/AiChat.vue'

const activeIndex = ref('students')

const components = {
  students: StudentManagement,
  teachers: TeacherManagement,
  clazzes: ClassManagement,
  'ai-chat': AiChat
}

const currentComponent = computed(() => {
  return components[activeIndex.value] || StudentManagement
})

const handleSelect = (key) => {
  activeIndex.value = key
}
</script>

<style scoped>
.header {
  background-color: #409eff;
  color: white;
  padding: 0;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  padding: 0 20px;
}

.title {
  margin: 0;
  font-size: 24px;
  font-weight: bold;
}

.nav-menu {
  background-color: transparent;
  border-bottom: none;
}

.nav-menu .el-menu-item {
  color: white;
  border-bottom: 2px solid transparent;
}

.nav-menu .el-menu-item:hover,
.nav-menu .el-menu-item.is-active {
  background-color: rgba(255, 255, 255, 0.1);
  border-bottom-color: white;
}

.main-content {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: calc(100vh - 60px);
}
</style>

