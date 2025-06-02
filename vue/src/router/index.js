import { createRouter, createWebHistory } from 'vue-router'
import StudentManagement from '../components/StudentManagement.vue'
import TeacherManagement from '../components/TeacherManagement.vue'
import ClassManagement from '../components/ClassManagement.vue'
import AiChat from '../components/AiChat.vue'

const routes = [
  {
    path: '/',
    redirect: '/students'
  },
  {
    path: '/students',
    name: 'students',
    component: StudentManagement
  },
  {
    path: '/teachers',
    name: 'teachers',
    component: TeacherManagement
  },
  {
    path: '/classes',
    name: 'classes',
    component: ClassManagement
  },
  {
    path: '/ai-chat',
    name: 'ai-chat',
    component: AiChat
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router