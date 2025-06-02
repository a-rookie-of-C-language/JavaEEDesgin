<template>
  <div class="teacher-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>教师管理</span>
          <el-button type="primary" @click="showAddDialog">添加教师</el-button>
        </div>
      </template>

      <!-- 教师列表 -->
      <el-table :data="teachers" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="120" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="department" label="部门" width="120" />
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button size="small" @click="showEditDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteTeacher(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑教师对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑教师' : '添加教师'"
      width="500px"
    >
      <el-form :model="teacherForm" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="teacherForm.name" />
        </el-form-item>
        <el-form-item label="部门" prop="department">
          <el-input v-model="teacherForm.department" />
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
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

// 状态管理
const loading = ref(false)
const teachers = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

// 教师表单
const teacherForm = reactive({
  id: '',
  name: '',
  department: ''
})

// 表单验证规则
const rules = {
  id: [{ required: true, message: '请输入教师ID', trigger: 'blur' }],
  name: [{ required: true, message: '请输入教师姓名', trigger: 'blur' }],
  department: [{ required: true, message: '请输入所属部门', trigger: 'blur' }]
}

// 生命周期钩子
onMounted(() => {
  getTeachers()
})

// 获取教师列表
const getTeachers = async () => {
  loading.value = true
  try {
    const response = await axios.get('/teacher/list')
    if (response.data.code === 200) {
      teachers.value = response.data.data
    } else {
      ElMessage.error(response.data.msg || '获取教师列表失败')
    }
  } catch (error) {
    console.error('获取教师列表失败:', error)
    ElMessage.error('获取教师列表失败')
  } finally {
    loading.value = false
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        let response
        if (isEdit.value) {
          response = await axios.put(`/teacher/update/${teacherForm.id}`, teacherForm)
        } else {
          response = await axios.post('/teacher/add', teacherForm)
        }
        
        if (response.data.code === 200) {
          ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
          dialogVisible.value = false
          getTeachers()
        } else {
          ElMessage.error(response.data.msg || (isEdit.value ? '更新失败' : '添加失败'))
        }
      } catch (error) {
        console.error(isEdit.value ? '更新教师失败:' : '添加教师失败:', error)
        ElMessage.error(isEdit.value ? '更新失败' : '添加失败')
      }
    }
  })
}

// 添加关联班级查询功能
const teacherClasses = ref([])
const classDialogVisible = ref(false)
const currentTeacher = ref(null)

const viewClasses = async (teacher) => {
  currentTeacher.value = teacher
  try {
    const response = await axios.get(`/class/teacher/${teacher.id}`)
    if (response.data.code === 200) {
      teacherClasses.value = response.data.data
      classDialogVisible.value = true
    } else {
      ElMessage.error(response.data.msg || '获取班级列表失败')
    }
  } catch (error) {
    console.error('获取班级列表失败:', error)
    ElMessage.error('获取班级列表失败')
  }
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
  Object.assign(teacherForm, row)
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  Object.assign(teacherForm, {
    id: '',
    name: '',
    department: ''
  })
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 删除教师
const deleteTeacher = async (id) => {
  try {
    await ElMessageBox.confirm('此操作将永久删除该教师, 是否继续?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await axios.delete(`/teacher/delete/${id}`)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      getTeachers()
    } else {
      ElMessage.error(response.message)
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}
</script>

<style scoped>
.teacher-management {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dialog-footer {
  text-align: right;
}
</style>