<template>
  <div id="app">
    <el-container>
      <el-header>
        <div class="header-content">
          <h1>学生管理系统</h1>
          <el-menu
            :default-active="$route.path"
            class="el-menu-demo"
            mode="horizontal"
            router
          >
            <el-menu-item index="/students">
              <el-icon><User /></el-icon>
              <span>学生管理</span>
            </el-menu-item>
            <el-menu-item index="/teachers">
              <el-icon><Avatar /></el-icon>
              <span>教师管理</span>
            </el-menu-item>
            <el-menu-item index="/classes">
              <el-icon><School /></el-icon>
              <span>班级管理</span>
            </el-menu-item>
            <el-menu-item index="/ai-chat">
              <el-icon><ChatDotRound /></el-icon>
              <span>AI助手</span>
            </el-menu-item>
          </el-menu>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { User, Avatar, School, ChatDotRound } from '@element-plus/icons-vue'
</script>

<style scoped>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
}

.el-header {
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

.header-content h1 {
  margin: 0;
  font-size: 24px;
}

.el-menu-demo {
  background-color: transparent;
  border-bottom: none;
}

.el-menu--horizontal > .el-menu-item {
  color: white;
  border-bottom: 2px solid transparent;
}

.el-menu--horizontal > .el-menu-item:hover,
.el-menu--horizontal > .el-menu-item.is-active {
  background-color: rgba(255, 255, 255, 0.1);
  border-bottom-color: white;
  color: white;
}

.el-main {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: calc(100vh - 60px);
}
</style>
