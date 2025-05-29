<template>
  <div class="class-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>班级管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="showAddDialog">添加班级</el-button>
            <el-button type="danger" :disabled="selectedClasses.length === 0" @click="batchDelete">批量删除</el-button>
          </div>
        </div>
      </template>

      <!-- 搜索和筛选区域 -->
      <div class="search-section">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="searchForm.name"
              placeholder="搜索班级名称"
              clearable
              @input="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="6">
            <el-select
              v-model="searchForm.grade"
              placeholder="筛选年级"
              clearable
              @change="handleSearch"
            >
              <el-option label="全部年级" value="" />
              <el-option label="高一" value="高一" />
              <el-option label="高二" value="高二" />
              <el-option label="高三" value="高三" />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-select
              v-model="searchForm.teacherId"
              placeholder="筛选班主任"
              clearable
              @change="handleSearch"
            >
              <el-option label="全部班主任" value="" />
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

      <!-- 班级列表 -->
      <el-table 
        :data="paginatedClasses" 
        style="width: 100%" 
        v-loading="loading"
        @selection-change="handleSelectionChange"
        stripe
        border
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="班级ID" width="120" sortable />
        <el-table-column prop="name" label="班级名称" width="150" sortable />
        <el-table-column prop="description" label="班级描述" min-width="100" show-overflow-tooltip />
        <el-table-column prop="teacherId" label="班主任ID" width="120" />
        <el-table-column label="班主任" width="120">
          <template #default="scope">
            <span>{{ getTeacherName(scope.row.teacherId) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="学生人数" width="100">
          <template #default="scope">
            <el-tag type="info">{{ scope.row.studentCount || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="viewStudents(scope.row)">查看学生</el-button>
            <el-button size="small" @click="showEditDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteClass(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="filteredClasses.length"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑班级对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑班级' : '添加班级'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="classForm" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="班级ID" prop="id" v-if="!isEdit">
              <el-input v-model="classForm.id" placeholder="请输入班级ID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="班级名称" prop="name">
              <el-input v-model="classForm.name" placeholder="请输入班级名称" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="班级描述" prop="description">
          <el-input 
            v-model="classForm.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入班级描述"
          />
        </el-form-item>
        
        <el-form-item label="班主任" prop="teacherId">
          <el-select v-model="classForm.teacherId" placeholder="请选择班主任" style="width: 100%">
            <el-option
              v-for="teacher in teachers"
              :key="teacher.id"
              :label="`${teacher.name} (${teacher.department})`"
              :value="teacher.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 学生列表对话框 -->
    <el-dialog
      v-model="studentDialogVisible"
      :title="`${currentClass?.name} - 学生列表`"
      width="800px"
    >
      <el-table :data="classStudents" v-loading="loadingStudents">
        <el-table-column prop="id" label="学号" width="100" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="age" label="年龄" width="80" />
        <el-table-column prop="teacherName" label="任课教师" width="120" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import axios from 'axios'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const loadingStudents = ref(false)
const classes = ref([])
const teachers = ref([])
const classStudents = ref([])
const selectedClasses = ref([])
const dialogVisible = ref(false)
const studentDialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const currentClass = ref(null)

// 搜索表单
const searchForm = reactive({
  name: '',
  grade: '',
  teacherId: ''
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10
})

// 班级表单
const classForm = reactive({
  id: '',
  name: '',
  description: '',
  teacherId: ''
})

// 表单验证规则
const rules = {
  id: [{ required: true, message: '请输入班级ID', trigger: 'blur' }],
  name: [{ required: true, message: '请输入班级名称', trigger: 'blur' }],
  teacherId: [{ required: true, message: '请选择班主任', trigger: 'change' }],
  description: [{ required: true, message: '请输入班级描述', trigger: 'blur' }]
}

// 计算属性 - 筛选后的班级列表
const filteredClasses = computed(() => {
  let result = classes.value
  
  if (searchForm.name) {
    result = result.filter(cls => cls.name.includes(searchForm.name))
  }
  
  if (searchForm.grade) {
    result = result.filter(cls => cls.name.includes(searchForm.grade))
  }
  
  if (searchForm.teacherId) {
    result = result.filter(cls => cls.teacherId === searchForm.teacherId)
  }
  
  return result
})

// 计算属性 - 分页后的班级列表
const paginatedClasses = computed(() => {
  const start = (pagination.currentPage - 1) * pagination.pageSize
  const end = start + pagination.pageSize
  return filteredClasses.value.slice(start, end)
})

// 获取班级列表
const getClasses = async () => {
  loading.value = true
  try {
    const response = await axios.get('/class/list')
    if (response.data.code === 200) {
      classes.value = response.data.data
    } else {
      ElMessage.error(response.data.message || '获取班级列表失败')
    }
  } catch (error) {
    console.error('获取班级列表失败:', error)
    ElMessage.error('获取班级列表失败')
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
    } else {
      ElMessage.error(response.data.message || '获取教师列表失败')
    }
  } catch (error) {
    console.error('获取教师列表失败:', error)
  }
}

// 获取班级学生列表
const getClassStudents = async (clazz) => {
  loadingStudents.value = true
  try {
    const response = await axios.get(`/student/class/${encodeURIComponent(clazz)}`)
    if (response.data.code === 200) {
      classStudents.value = response.data.data
    } else {
      ElMessage.error(response.data.message || '获取学生列表失败')
    }
  } catch (error) {
    console.error('获取学生列表失败:', error)
    ElMessage.error('获取学生列表失败')
  } finally {
    loadingStudents.value = false
  }
}

// 根据教师ID获取教师姓名
const getTeacherName = (teacherId) => {
  const teacher = teachers.value.find(t => t.id === teacherId)
  return teacher ? teacher.name : '未分配'
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
  Object.assign(classForm, row)
  dialogVisible.value = true
}

// 查看学生
const viewStudents = (row) => {
  currentClass.value = row
  getClassStudents(row.name)
  studentDialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  Object.assign(classForm, {
    id: '',
    name: '',
    description: '',
    teacherId: ''
  })
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        let response
        if (isEdit.value) {
          response = await axios.put(`/class/update/${classForm.id}`, classForm)
        } else {
          response = await axios.post('/class/add', classForm)
        }
        
        if (response.data.code === 200) {
          ElMessage.success(response.data.message || (isEdit.value ? '更新成功' : '添加成功'))
          dialogVisible.value = false
          getClasses()
        } else {
          ElMessage.error(response.data.message || (isEdit.value ? '更新失败' : '添加失败'))
        }
      } catch (error) {
        console.error('操作失败:', error)
        ElMessage.error(isEdit.value ? '更新班级失败' : '添加班级失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 删除班级
const deleteClass = async (id) => {
  try {
    await ElMessageBox.confirm('此操作将永久删除该班级, 是否继续?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await axios.delete(`/class/delete/${id}`)
    if (response.data.code === 200) {
      ElMessage.success('删除成功')
      getClasses()
    } else {
      ElMessage.error(response.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 批量删除
const batchDelete = async () => {
  if (selectedClasses.value.length === 0) {
    ElMessage.warning('请选择要删除的班级')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedClasses.value.length} 个班级吗？`, '批量删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const deletePromises = selectedClasses.value.map(cls => 
      axios.delete(`/class/delete/${cls.id}`)
    )
    
    await Promise.all(deletePromises)
    ElMessage.success('批量删除成功')
    getClasses()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

// 处理搜索
const handleSearch = () => {
  pagination.currentPage = 1
}

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    name: '',
    grade: '',
    teacherId: ''
  })
  pagination.currentPage = 1
}

// 处理选择变化
const handleSelectionChange = (selection) => {
  selectedClasses.value = selection
}

// 处理页面大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.currentPage = 1
}

// 处理当前页变化
const handleCurrentChange = (page) => {
  pagination.currentPage = page
}

// 组件挂载时获取数据
onMounted(() => {
  getClasses()
  getTeachers()
})
</script>

<style scoped>
.class-management {
  height: 100%;
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.search-section {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.dialog-footer {
  text-align: right;
}

.el-table {
  margin-top: 20px;
}

.el-tag {
  margin-left: 5px;
}
</style>