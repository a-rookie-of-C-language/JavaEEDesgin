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
          <el-select 
            v-model="studentForm.teacherId" 
            placeholder="请选择班主任"
            :disabled="!!studentForm.clazz">
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

<!-- 保留原有模板结构，添加以下功能 -->
<script setup>
// 导入所需的组合式API
import { ref, reactive, onMounted, watch } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

// 状态管理
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
  teacherId: ''
})

// 表单验证规则
const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  age: [{ required: true, message: '请输入年龄', trigger: 'blur' }],
  clazz: [{ required: true, message: '请选择班级', trigger: 'change' }],
  teacherId: [{ required: true, message: '请选择班主任', trigger: 'change' }]
}

// 生命周期钩子
onMounted(async () => {
  await Promise.all([
    getStudents(),
    getTeachers(),
    getClasses()
  ])
})

// 监听搜索条件变化
watch([searchClass, searchTeacher], () => {
  handleSearch()
})

// 获取学生列表
const getStudents = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    
    if (searchClass.value) {
      params.clazz = searchClass.value
    }
    
    if (searchTeacher.value) {
      params.teacherId = searchTeacher.value
    }
    
    const response = await axios.get('/student/page', { params })
    if (response.data.code === 200) {
      students.value = response.data.data.data
      total.value = response.data.data.total
    } else {
      ElMessage.error(response.data.msg || '获取学生列表失败')
    }
  } catch (error) {
    console.error('获取学生列表失败:', error)
    ElMessage.error('获取学生列表失败')
  } finally {
    loading.value = false
  }
}

// 获取教师列表
const getTeachers = async () => {
  try {
    const response = await axios.get('/teacher/list')
    if (response.data.code === 200) {
      teachers.value = response.data.data
    }
  } catch (error) {
    console.error('获取教师列表失败:', error)
  }
}

// 在script setup部分添加以下代码
// 获取班级列表时同时保存班级和班主任的对应关系
const classTeacherMap = ref({})

// 修改getClasses方法
const getClasses = async () => {
  try {
    const response = await axios.get('/class/list')
    if (response.data.code === 200) {
      classes.value = response.data.data.map(c => c.name)
      
      // 保存班级和班主任的对应关系
      response.data.data.forEach(c => {
        classTeacherMap.value[c.name] = c.teacherId
      })
    }
  } catch (error) {
    console.error('获取班级列表失败:', error)
  }
}

// 监听班级选择变化，自动设置对应的班主任
watch(() => studentForm.clazz, (newClass) => {
  if (newClass && classTeacherMap.value[newClass]) {
    studentForm.teacherId = classTeacherMap.value[newClass]
  }
})

// 修改submitForm方法
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 创建一个新对象，只包含需要的字段
        const formData = {
          id: studentForm.id,
          name: studentForm.name,
          age: studentForm.age,
          clazz: studentForm.clazz,
          teacherId: studentForm.teacherId
        };
        
        let response
        if (isEdit.value) {
          response = await axios.put(`/student/update`, formData)
        } else {
          response = await axios.post('/student/add', formData)
        }
        
        if (response.data.code === 200) {
          ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
          dialogVisible.value = false
          getStudents()
        } else {
          ElMessage.error(response.data.msg || (isEdit.value ? '更新失败' : '添加失败'))
        }
      } catch (error) {
        console.error(isEdit.value ? '更新学生失败:' : '添加学生失败:', error)
        ElMessage.error(isEdit.value ? '更新失败' : '添加失败')
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