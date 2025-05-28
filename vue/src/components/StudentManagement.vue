<template>
  <div class="student-management">
    <!-- 页面标题和操作栏 -->
    <div class="header">
      <h2>学生管理系统</h2>
      <div class="actions">
        <el-button type="primary" @click="showAddDialog" :icon="Plus">
          添加学生
        </el-button>
        <el-button @click="refreshData" :icon="Refresh">
          刷新
        </el-button>
        <!-- AI功能预留按钮 -->
        <el-button type="success" @click="openAIAssistant" :icon="Robot">
          AI助手
        </el-button>
      </div>
    </div>

    <!-- 搜索和筛选栏 -->
    <div class="search-bar">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索学生姓名或学号"
            :prefix-icon="Search"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.clazz" placeholder="选择班级" clearable>
            <el-option
              v-for="clazz in classList"
              :key="clazz"
              :label="clazz"
              :value="clazz"
            />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.teacherId" placeholder="选择教师" clearable>
            <el-option
              v-for="teacher in teacherList"
              :key="teacher.id"
              :label="teacher.name"
              :value="teacher.id"
            />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch" style="margin-left: 10px;">重置</el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 学生列表表格 -->
    <div class="table-container">
      <el-table
        :data="studentList"
        v-loading="loading"
        stripe
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <!-- 表格部分：移除性别、邮箱、电话列 -->
        <el-table-column prop="id" label="学号" width="100" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="age" label="年龄" width="80" />
        <el-table-column prop="clazz" label="班级" width="120" />
        <el-table-column label="教师" width="120">
          <template #default="{ row }">
            {{ getTeacherName(row.teacherId) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="editStudent(row)" :icon="Edit">
              修改
            </el-button>
            <el-button size="small" type="danger" @click="deleteStudent(row.id)" :icon="Delete">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 添加/编辑学生对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :before-close="handleDialogClose"
    >
      <el-form
        ref="studentFormRef"
        :model="studentForm"
        :rules="formRules"
        label-width="80px"
      >
        <!-- 表单部分：移除性别、邮箱、电话表单项 -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="studentForm.name" placeholder="请输入学生姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="年龄" prop="age">
              <el-input-number v-model="studentForm.age" :min="1" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="班级" prop="clazz">
              <el-select v-model="studentForm.clazz" placeholder="选择班级" style="width: 100%">
                <el-option
                  v-for="clazz in classList"
                  :key="clazz"
                  :label="clazz"
                  :value="clazz"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="教师ID" prop="teacherId">
              <el-select v-model="studentForm.teacherId" placeholder="选择教师" style="width: 100%">
                <el-option
                  v-for="teacher in teacherList"
                  :key="teacher.id"
                  :label="teacher.name"
                  :value="teacher.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 学生详情对话框 -->
    <!-- 详情对话框：移除性别、邮箱、电话显示项 -->
    <el-dialog v-model="detailDialogVisible" title="学生详情" width="500px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="学号">{{ currentStudent.id }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ currentStudent.name }}</el-descriptions-item>
        <el-descriptions-item label="年龄">{{ currentStudent.age }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ currentStudent.clazz }}</el-descriptions-item>
        <el-descriptions-item label="教师ID" :span="2">{{ currentStudent.teacherId }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- AI助手对话框（预留） -->
    <el-dialog v-model="aiDialogVisible" title="AI智能助手" width="800px">
      <div class="ai-assistant">
        <p>AI助手功能正在开发中...</p>
        <p>将集成LangChain4j框架，提供智能学生管理建议</p>
        <!-- 这里将来可以添加AI聊天界面 -->
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Refresh,
  Search,
  Edit,
  Delete,
  View,
  ChatDotRound  // Replace Robot with ChatDotRound or another AI-related icon
} from '@element-plus/icons-vue'
import { studentApi } from '../api/student.js'

// 响应式数据
const loading = ref(false)
const studentList = ref([])
const selectedStudents = ref([])
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const aiDialogVisible = ref(false)
const isEdit = ref(false)
const studentFormRef = ref()

// 搜索表单
const searchForm = reactive({
  keyword: '',
  clazz: '',
  teacherId: ''
})

// 分页数据
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 学生表单
// 数据模型：移除gender、email、phone字段
const studentForm = reactive({
  id: null,
  name: '',
  age: null,
  clazz: '',
  teacherId: ''
})

// 表单验证规则：移除gender、email、phone验证
const formRules = {
  name: [
    { required: true, message: '请输入学生姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在2到20个字符', trigger: 'blur' }
  ],
  age: [
    { required: true, message: '请输入年龄', trigger: 'blur' },
    { type: 'number', min: 1, max: 100, message: '年龄必须在1-100之间', trigger: 'blur' }
  ],
  clazz: [
    { required: true, message: '请选择班级', trigger: 'change' }
  ],
  teacherId: [
    { required: true, message: '请选择教师', trigger: 'change' }
  ]
}

// 班级列表（从后端获取）
const classList = ref([])

// 教师列表（从后端获取）
const teacherList = ref([])

// 计算属性
const dialogTitle = computed(() => {
  return isEdit.value ? '编辑学生' : '添加学生'
})

// 生命周期
onMounted(() => {
  loadStudentList()
  loadTeacherList()
  loadClassList()
})

// 方法
const loadStudentList = async () => {
  try {
    loading.value = true
    const response = await studentApi.getStudentList(
      pagination.currentPage,
      pagination.pageSize
    )
    
    console.log('API响应:', response)
    
    // 由于student.js已经返回了data部分，直接使用
    if (response && typeof response === 'object') {
    // 检查是否有data数组
    if (response.data && Array.isArray(response.data)) {
    studentList.value = response.data
    pagination.total = response.total || 0
    console.log('使用分页结构，学生数量:', studentList.value.length)
    } 
    // 兼容直接数组结构
    else if (Array.isArray(response)) {
    studentList.value = response
    pagination.total = response.length
    console.log('使用数组结构，学生数量:', studentList.value.length)
    }
    else {
    console.warn('未知的数据结构:', response)
    studentList.value = []
    pagination.total = 0
    }
    
    console.log('最终解析结果:', {
    studentList: studentList.value,
    total: pagination.total,
    currentPage: pagination.currentPage
    })
    } else {
    console.warn('响应数据为空或格式错误:', response)
    studentList.value = []
    pagination.total = 0
    }
  } catch (error) {
    console.error('加载学生列表失败:', error)
    ElMessage.error('网络错误，请稍后重试')
    studentList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  if (searchForm.clazz) {
    try {
      loading.value = true
      const response = await studentApi.getStudentsByClass(searchForm.clazz)
      if (response.code === 200) {
        studentList.value = response.data || []
      }
    } catch (error) {
      ElMessage.error('搜索失败')
    } finally {
      loading.value = false
    }
  } else if (searchForm.teacherId) {
    try {
      loading.value = true
      const response = await studentApi.getStudentsByTeacher(searchForm.teacherId)
      if (response.code === 200) {
        studentList.value = response.data || []
      }
    } catch (error) {
      ElMessage.error('搜索失败')
    } finally {
      loading.value = false
    }
  } else {
    loadStudentList()
  }
}

const resetSearch = () => {
  Object.assign(searchForm, {
    keyword: '',
    clazz: '',
    teacherId: ''
  })
  loadStudentList()
}

const refreshData = () => {
  loadStudentList()
  loadTeacherList()
  loadClassList()
  ElMessage.success('数据已刷新')
}

const showAddDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const editStudent = (row) => {
  isEdit.value = true
  Object.assign(studentForm, row)
  dialogVisible.value = true
}

const viewStudent = (row) => {
  currentStudent.value = row
  detailDialogVisible.value = true
}

const deleteStudent = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除学生 "${row.name}" 吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await studentApi.deleteStudent(row.id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadStudentList()
    } else {
      ElMessage.error(response.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const submitForm = async () => {
  try {
    await studentFormRef.value.validate()
    
    const response = isEdit.value
      ? await studentApi.updateStudent(studentForm.id, studentForm)
      : await studentApi.addStudent(studentForm)
    
    if (response.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
      dialogVisible.value = false
      loadStudentList()
    } else {
      ElMessage.error(response.msg || '操作失败')
    }
  } catch (error) {
    console.error('表单提交失败:', error)
  }
}

// 重置表单函数：移除gender、email、phone字段
const resetForm = () => {
  Object.assign(studentForm, {
    id: null,
    name: '',
    age: null,
    clazz: '',
    teacherId: ''
  })
  studentFormRef.value?.clearValidate()
}

const handleDialogClose = () => {
  resetForm()
  dialogVisible.value = false
}

const handleSelectionChange = (selection) => {
  selectedStudents.value = selection
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  loadStudentList()
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
  loadStudentList()
}

const openAIAssistant = () => {
  aiDialogVisible.value = true
  ElMessage.info('AI助手功能即将上线，敬请期待！')
}
// 在其他方法后添加这个辅助方法
const getTeacherName = (teacherId) => {
  const teacher = teacherList.value.find(t => t.id === teacherId || t.id === String(teacherId))
  return teacher ? teacher.name : `教师${teacherId}`
}

// 加载教师列表
const loadTeacherList = async () => {
  try {
    const response = await studentApi.getAllTeachers()
    console.log('教师列表响应:', response)
    
    if (response && Array.isArray(response)) {
      teacherList.value = response
    } else if (response && response.data && Array.isArray(response.data)) {
      teacherList.value = response.data
    } else {
      console.warn('教师列表数据格式异常:', response)
      // 设置默认教师列表
      teacherList.value = [
        { id: '1', name: '张老师' },
        { id: '2', name: '李老师' },
        { id: '3', name: '王老师' }
      ]
    }
    console.log('最终教师列表:', teacherList.value)
  } catch (error) {
    console.error('加载教师列表失败:', error)
    // 设置默认教师列表
    teacherList.value = [
      { id: '1', name: '张老师' },
      { id: '2', name: '李老师' },
      { id: '3', name: '王老师' }
    ]
  }
}

// 加载班级列表
const loadClassList = async () => {
  try {
    const response = await studentApi.getAllClasses()
    console.log('班级列表响应:', response)
    
    if (response && Array.isArray(response)) {
      classList.value = response
    } else if (response && response.data && Array.isArray(response.data)) {
      classList.value = response.data
    } else {
      console.warn('班级列表数据格式异常:', response)
      // 设置默认班级列表
      classList.value = ['计算机1班', '计算机2班', '软件工程1班', '软件工程2班']
    }
    console.log('最终班级列表:', classList.value)
  } catch (error) {
    console.error('加载班级列表失败:', error)
    // 设置默认班级列表
    classList.value = ['计算机1班', '计算机2班', '软件工程1班', '软件工程2班']
  }
}
</script>

<style scoped>
.student-management {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
  color: #303133;
}

.actions {
  display: flex;
  gap: 10px;
}

.search-bar {
  margin-bottom: 20px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.table-container {
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.ai-assistant {
  text-align: center;
  padding: 40px;
  color: #909399;
}
</style>