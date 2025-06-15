<template>
  <div class="teacher-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>教师管理</span>
          <el-button type="primary" @click="showAddDialog = true">
            <el-icon><Plus /></el-icon>
            添加教师
          </el-button>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-area">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="searchForm.name"
              placeholder="请输入教师姓名"
              clearable
              @clear="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="6">
            <el-input
              v-model="searchForm.clazz"
              placeholder="请输入班级名称"
              clearable
              @clear="handleSearch"
            >
              <template #prefix>
                <el-icon><School /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="6">
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-col>
        </el-row>
      </div>

      <!-- 教师表格 -->
      <el-table
        :data="filteredTeacherList"
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="id" label="教师编号" width="120" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="editTeacher(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="deleteTeacher(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑教师对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="isEdit ? '编辑教师' : '添加教师'"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="teacherFormRef"
        :model="teacherForm"
        :rules="rules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="教师编号" prop="id">
              <el-input v-model="teacherForm.id" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="teacherForm.name" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAddDialog = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, getCurrentInstance } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, School } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()

// 响应式数据
const loading = ref(false)
const showAddDialog = ref(false)
const isEdit = ref(false)
const teacherList = ref([])
const studentCounts = ref({})
const teacherFormRef = ref()

// 搜索表单
const searchForm = reactive({
  name: '',
  clazz: ''
})

// 教师表单
const teacherForm = reactive({
  id: '',
  name: '',
})

// 表单验证规则
const rules = {
  id: [{ required: true, message: '请输入教师编号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
}

// 过滤后的教师列表
const filteredTeacherList = computed(() => {
  return teacherList.value.filter(teacher => {
    const nameMatch = !searchForm.name || teacher.name.includes(searchForm.name)
    const clazzMatch = !searchForm.clazz || teacher.clazz.includes(searchForm.clazz)
    return nameMatch && clazzMatch
  })
})

// 获取教师列表
const getTeacherList = async () => {
  loading.value = true
  try {
    const response = await proxy.$http.get('/teacher/list')
    if (response.data.code === 200) {
      teacherList.value = response.data.data
      // 获取每个教师的学生数量
      await getStudentCounts()
    }
  } catch (error) {
    ElMessage.error('获取教师列表失败')
  } finally {
    loading.value = false
  }
}

// 获取每个教师的学生数量
const getStudentCounts = async () => {
  try {
    for (const teacher of teacherList.value) {
      const response = await proxy.$http.get(`/student/teacher/${teacher.id}`)
      if (response.data.code === 200) {
        studentCounts.value[teacher.id] = response.data.data.length
      }
    }
  } catch (error) {
    console.error('获取学生数量失败:', error)
  }
}

// 获取学生数量
const getStudentCount = (teacherId) => {
  return studentCounts.value[teacherId] || 0
}

// 搜索教师
const handleSearch = () => {
  // 使用计算属性自动过滤，无需额外操作
}

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    name: '',
    clazz: ''
  })
}

// 编辑教师
const editTeacher = (teacher) => {
  isEdit.value = true
  Object.assign(teacherForm, teacher)
  showAddDialog.value = true
}

// 删除教师
const deleteTeacher = async (teacher) => {
  try {
    // 检查是否有学生
    const studentCount = getStudentCount(teacher.id)
    if (studentCount > 0) {
      ElMessage.warning(`该教师还有 ${studentCount} 名学生，无法删除`)
      return
    }

    await ElMessageBox.confirm(
      `确定要删除教师 ${teacher.name} 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await proxy.$http.delete(`/teacher/delete/${teacher.id}`)
    if (response.data.code === 200) {
      ElMessage.success('删除成功')
      await getTeacherList()
    } else {
      ElMessage.error(response.data.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 提交表单
const submitForm = async () => {
  try {
    await teacherFormRef.value.validate()
    
    const url = isEdit.value ? `/teacher/update/${teacherForm.id}` : '/teacher/add'
    const response = await proxy.$http[isEdit.value ? 'put' : 'post'](url, teacherForm)
    
    if (response.data.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
      showAddDialog.value = false
      await getTeacherList()
    } else {
      ElMessage.error(response.data.message || '操作失败')
    }
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

// 重置表单
const resetForm = () => {
  isEdit.value = false
  Object.assign(teacherForm, {
    id: '',
    name: '',
    clazz: '',
  })
  teacherFormRef.value?.resetFields()
}

// 组件挂载时获取数据
onMounted(() => {
  getTeacherList()
})
</script>

<style scoped>
.teacher-list {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-area {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.dialog-footer {
  text-align: right;
}
</style>