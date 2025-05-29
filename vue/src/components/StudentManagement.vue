<template>
  <div class="student-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>学生管理</span>
          <el-button type="primary" @click="showAddDialog">添加学生</el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-select v-model="searchClass" placeholder="选择班级" clearable @change="handleSearch">
              <el-option label="全部班级" value="" />
              <el-option
                v-for="cls in classes"
                :key="cls"
                :label="cls"
                :value="cls"
              />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-select v-model="searchTeacher" placeholder="选择教师" clearable @change="handleSearch">
              <el-option label="全部教师" value="" />
              <el-option
                v-for="teacher in teachers"
                :key="teacher.id"
                :label="teacher.name"
                :value="teacher.id"
              />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-col>
        </el-row>
      </div>

      <!-- 学生列表 -->
      <el-table :data="students" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="age" label="年龄" width="80" />
        <el-table-column prop="clazz" label="班级" width="120" />
        <el-table-column prop="teacherName" label="班主任" width="120" />
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button size="small" @click="showEditDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteStudent(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑学生对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑学生' : '添加学生'"
      width="500px"
    >
      <el-form :model="studentForm" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="studentForm.name" />
        </el-form-item>
        <el-form-item label="年龄" prop="age">
          <el-input-number v-model="studentForm.age" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="班级" prop="clazz">
          <el-select v-model="studentForm.clazz" placeholder="请选择班级">
            <el-option
              v-for="cls in classes"
              :key="cls"
              :label="cls"
              :value="cls"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="班主任" prop="teacherId">
          <el-select v-model="studentForm.teacherId" placeholder="请选择班主任">
            <el-option
              v-for="teacher in teachers"
              :key="teacher.id"
              :label="teacher.name"
              :value="teacher.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

// 响应式数据
const loading = ref(false)
const students = ref([])
const teachers = ref([])
const classes = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

// 搜索条件
const searchClass = ref('')
const searchTeacher = ref('')

// 学生表单
const studentForm = reactive({
  id: null,
  name: '',
  age: null,
  clazz: '',
  teacherId: '',
})

// 表单验证规则
const rules = {
  name: [{ required: true, message: '请输入学生姓名', trigger: 'blur' }],
  age: [{ required: true, message: '请输入学生年龄', trigger: 'blur' }],
  clazz: [{ required: true, message: '请选择班级', trigger: 'change' }],
  teacherId: [{ required: true, message: '请选择班主任', trigger: 'change' }]
}

// 获取学生列表
const getStudents = async () => {
  loading.value = true
  try {
    const response = await axios.get('/student/page', {
      params: {
        page: currentPage.value,
        size: pageSize.value
      }
    })
    if (response.data.code === 200) {
      console.log(response.data.data)
      students.value = Array.isArray(response.data.data.data) ? response.data.data.data : []
      total.value = response.data.data.total
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    ElMessage.error('获取学生列表失败')
  } finally {
    loading.value = false
  }
}

const getTeachers = async () => {
  try {
    const response = await axios.get('/student/teachers')
    if (response.data.code === 200) {
      teachers.value = response.data.data
    }
  } catch (error) {
    console.error('获取教师列表失败', error)
  }
}

const getClasses = async () => {
  try {
    const response = await axios.get('/student/classes')
    if (response.data.code === 200) {
      console.log(response.data.data)
      classes.value = response.data.data
    }
  } catch (error) {
    console.error('获取班级列表失败', error)
  }
}

// 修复handleSearch方法中的响应访问
const handleSearch = async () => {
  if (searchClass.value) {
    try {
      const response = await axios.get(`/student/class/${searchClass.value}`)
      if (response.data.code === 200) {  // 修复：使用response.data.code
        students.value = response.data.data
        total.value = response.data.data.length
      }
    } catch (error) {
      ElMessage.error('搜索失败')
    }
  } else if (searchTeacher.value) {
    try {
      const response = await axios.get(`/student/teacher/${searchTeacher.value}`)
      if (response.data.code === 200) {  // 修复：使用response.data.code
        students.value = response.data.data
        total.value = response.data.data.length
      }
    } catch (error) {
      ElMessage.error('搜索失败')
    }
  } else {
    getStudents()
  }
}

// 修复submitForm方法
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        let response
        if (isEdit.value) {
          response = await axios.put(`/student/update/${studentForm.id}`, studentForm)
        } else {
          response = await axios.post('/student/add', studentForm)
        }
        
        if (response.data.code === 200) {  // 修复：使用response.data.code
          ElMessage.success(response.data.message)
          dialogVisible.value = false
          getStudents()
        } else {
          ElMessage.error(response.data.message)
        }
      } catch (error) {
        ElMessage.error(isEdit.value ? '更新学生失败' : '添加学生失败')
      }
    }
  })
}

// 修复deleteStudent方法
const deleteStudent = async (id) => {
  try {
    await ElMessageBox.confirm('此操作将永久删除该学生, 是否继续?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await axios.delete(`/student/delete/${id}`)
    if (response.data.code === 200) {  // 修复：使用response.data.code
      ElMessage.success('删除成功')
      getStudents()
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 重置搜索
const resetSearch = () => {
  searchClass.value = ''
  searchTeacher.value = ''
  currentPage.value = 1
  getStudents()
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
  getStudents()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  getStudents()
}

// 显示添加对话框
const showAddDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (row) => {
  isEdit.value = true
  Object.assign(studentForm, row)
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  Object.assign(studentForm, {
    id: null,
    name: '',
    age: null,
    clazz: '',
    teacherId: '',
  })
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 组件挂载时获取数据
onMounted(() => {
  getStudents()
  getTeachers()
  getClasses()
})
</script>

<style scoped>
.student-management {
  height: 100%;
  width: 100%; /* 添加宽度 */
}

.box-card {
  width: 100%; /* 确保卡片占满宽度 */
}

/* 确保表格占满容器宽度 */
.el-table {
  width: 100% !important;
}

/* 确保表格容器不被压缩 */
.el-card__body {
  width: 100%;
  overflow-x: auto; /* 添加水平滚动以防内容过宽 */
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%; /* 确保头部占满宽度 */
}

.search-bar {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 4px;
  width: 100%; /* 确保搜索栏占满宽度 */
}

.pagination {
  margin-top: 20px;
  text-align: right;
  width: 100%; /* 确保分页占满宽度 */
}

.dialog-footer {
  text-align: right;
}

/* 强制表格列宽度自适应 */
.el-table .el-table__cell {
  padding: 8px;
}

/* 确保主容器不被压缩 */
.el-main {
  width: 100%;
  min-width: 800px; /* 设置最小宽度 */
}
</style>