import { createRouter, createWebHistory } from 'vue-router'
import StudentList from '../src/views/StudentList.vue'
import TeacherList from '../src/views/TeacherList.vue'
import ClassList from '../src/views/ClassList.vue'
import AiChat from '../src/views/AiChat.vue'

const routes = [
  {
    path: '/',
    redirect: '/students'
  },
  {
    path: '/students',
    name: 'StudentList',
    component: StudentList
  },
  {
    path: '/teachers',
    name: 'TeacherList',
    component: TeacherList
  },
  {
    path: '/classes',
    name: 'ClassList',
    component: ClassList
  },
  {
    path: '/ai-chat',
    name: 'AiChat',
    component: AiChat
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router