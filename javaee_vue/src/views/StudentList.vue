<template>
  <div class="student-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学生管理</span>
          <el-button type="primary" @click="showAddDialog = true">
            <el-icon><Plus /></el-icon>
            添加学生
          </el-button>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-area">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="searchForm.name"
              placeholder="请输入学生姓名"
              clearable
              @clear="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="6">
            <el-select
              v-model="searchForm.clazz"
              placeholder="请选择班级"
              clearable
              @clear="handleSearch"
            >
              <el-option
                v-for="clazz in availableClasses"
                :key="clazz.id"
                :label="`${clazz.name} - ${clazz.teacherName || '暂无班主任'}`"
                :value="clazz.id"
              />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-select
              v-model="searchForm.teacherId"
              placeholder="请选择班主任"
              clearable
              @clear="handleSearch"
            >
              <el-option
                v-for="teacher in teacherList"
                :key="teacher.id"
                :label="teacher.name"
                :value="teacher.id"
              />
            </el-select>
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

      <!-- 学生表格 -->
      <el-table
        :data="studentList"
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="id" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="age" label="年龄" width="80" />
        <el-table-column prop="clazzName" label="班级" width="120" />
        <el-table-column prop="teacherName" label="班主任" width="120" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="editStudent(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="deleteStudent(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑学生对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="isEdit ? '编辑学生' : '添加学生'"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="studentFormRef"
        :model="studentForm"
        :rules="rules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学号" prop="id">
              <el-input v-model="studentForm.id"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="studentForm.name" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="年龄" prop="age">
              <el-input-number
                v-model="studentForm.age"
                :min="1"
                :max="100"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="班级" prop="clazzId">
              <el-select
                v-model="studentForm.clazzId"
                placeholder="请选择班级（班主任将自动设置）"
                style="width: 100%"
                @change="handleClassChange"
                clearable
              >
                <el-option
                  v-for="clazz in availableClasses"
                  :key="clazz.id"
                  :label="`${clazz.name} - ${clazz.teacherName || '暂无班主任'}`"
                  :value="clazz.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20" v-if="studentForm.clazzId">
          <el-col :span="24">
            <el-form-item label="班主任">
              <el-input
                :value="selectedTeacherName"
                placeholder="选择班级后自动显示班主任"
                disabled
                style="width: 100%"
              />
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
import { ref, reactive, onMounted, getCurrentInstance, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()

// 响应式数据
const loading = ref(false)
const showAddDialog = ref(false)
const isEdit = ref(false)
const studentList = ref([])
const teacherList = ref([])
const classList = ref([])
const availableClasses = ref([])
const studentFormRef = ref()

// 计算属性：选中的班主任名称
const selectedTeacherName = computed(() => {
  if (!studentForm.clazzId) return ''
  const selectedClass = availableClasses.value.find(c => c.id === studentForm.clazzId)
  return selectedClass?.teacherName || '暂无班主任'
})

// 搜索表单
const searchForm = reactive({
  name: '',
  clazz: '',
  teacherId: ''
})

// 分页数据
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 学生表单
const studentForm = reactive({
  id: '',
  name: '',
  age: null,
  clazzId: '',
  teacherId: '',
})

// 表单验证规则
const rules = {
  id: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  age: [{ required: true, message: '请输入年龄', trigger: 'blur' }],
  clazzId: [{ required: true, message: '请选择班级', trigger: 'change' }],
  teacherId: [{ required: true, message: '请选择班主任', trigger: 'change' }],
}

// 获取学生列表
const getStudentList = async () => {
  loading.value = true
  try {
    const response = await proxy.$http.get('/student/page', {
      params: {
        page: pagination.page,
        size: pagination.size
      }
    })
    if (response.data.code === 200) {
      studentList.value = response.data.data.data
      console.log(studentList.value)
      pagination.total = response.data.data.total
    }
  } catch (error) {
    ElMessage.error('获取学生列表失败')
  } finally {
    loading.value = false
  }
}

// 获取教师列表
const getTeacherList = async () => {
  try {
    const response = await proxy.$http.get('/student/teachers')
    if (response.data.code === 200) {
      teacherList.value = response.data.data
    }
  } catch (error) {
    ElMessage.error('获取教师列表失败')
  }
}

// 获取班级列表
const getClassList = async () => {
  try {
    console.log('开始获取班级列表...')
    const response = await proxy.$http.get('/class/list')
    console.log('班级API响应:', response.data)
    if (response.data.code === 200) {
      classList.value = response.data.data
      console.log('班级原始数据:', classList.value)
      await enrichClassData()
      console.log('处理后的班级数据:', availableClasses.value)
    }
  } catch (error) {
    console.error('获取班级列表失败:', error)
    ElMessage.error('获取班级列表失败')
  }
}

// 丰富班级数据（添加教师名称）
const enrichClassData = async () => {
  console.log('开始处理班级数据...')
  console.log('classList:', classList.value)
  console.log('teacherList:', teacherList.value)
  
  availableClasses.value = []
  for (const clazz of classList.value) {
    let teacherName = null
    
    if (clazz.teacherId) {
      const teacher = teacherList.value.find(t => t.id === clazz.teacherId)
      teacherName = teacher ? teacher.name : '教师信息缺失'
    }
    
    // 无论是否找到教师，都添加班级到可选列表
    availableClasses.value.push({
      id: clazz.id,
      name: clazz.name,
      teacherId: clazz.teacherId || null,
      teacherName: teacherName
    })
  }
  
  console.log('处理后的 availableClasses:', availableClasses.value)
}

// 搜索学生
const handleSearch = async () => {
  loading.value = true
  try {
    let url = '/student/page'
    const params = {
      page: pagination.page,
      size: pagination.size
    }
    
    // 根据搜索条件调用不同接口
    if (searchForm.teacherId) {
      url = '/student/teacher/' + searchForm.teacherId
    } else if (searchForm.clazz) {
      url = '/student/class/' + encodeURIComponent(searchForm.clazz)
    }
    
    const response = await proxy.$http.get(url, { params })
    if (response.data.code === 200) {
      let data = response.data.data
      if (Array.isArray(data)) {
        // 如果是数组，说明是按班级或教师搜索的结果
        studentList.value = data
        if (searchForm.name) {
          // 如果还有姓名搜索条件，进行前端过滤
          studentList.value = data.filter(student => 
            student.name.includes(searchForm.name)
          )
        }
        pagination.total = studentList.value.length
      } else {
        // 分页数据
        studentList.value = data.data
        pagination.total = data.total
      }
    }
  } catch (error) {
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    name: '',
    clazz: '',
    teacherId: ''
  })
  pagination.page = 1
  getStudentList()
}

// 分页大小改变
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  getStudentList()
}

// 当前页改变
const handleCurrentChange = (page) => {
  pagination.page = page
  getStudentList()
}

// 班级改变时自动设置对应的班主任
const handleClassChange = (clazzId) => {
  if (!clazzId) {
    studentForm.teacherId = ''
    return
  }
  
  const selectedClass = availableClasses.value.find(c => c.id === clazzId)
  if (selectedClass && selectedClass.teacherId) {
    studentForm.teacherId = selectedClass.teacherId
  } else {
    studentForm.teacherId = ''
  }
}

// 编辑学生
const editStudent = (student) => {
  isEdit.value = true
  // 根据学生的班级名称找到对应的班级ID
  const clazz = availableClasses.value.find(c => c.name === student.clazzName)
  Object.assign(studentForm, {
    id: student.id,
    name: student.name,
    age: student.age,
    clazzId: clazz ? clazz.id : '',
    teacherId: student.teacherId || ''
  })
  showAddDialog.value = true
}

// 删除学生
const deleteStudent = async (student) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除学生 ${student.name} 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await proxy.$http.delete(`/student/delete/${student.id}`)
    if (response.data.code === 200) {
      ElMessage.success('删除成功')
      await getStudentList()
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
    await studentFormRef.value.validate()
    
    // 准备提交的数据
    const submitData = {
      id: studentForm.id,
      name: studentForm.name,
      age: studentForm.age,
      clazzId: studentForm.clazzId,
      teacherId: studentForm.teacherId
    }
    
    const url = isEdit.value ? '/student/update' : '/student/add'
    const response = await proxy.$http[isEdit.value ? 'put' : 'post'](url, submitData)
    
    if (response.data.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
      showAddDialog.value = false
      await getStudentList()
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
  Object.assign(studentForm, {
    id: '',
    name: '',
    age: null,
    clazzId: '',
    teacherId: '',
  })
  studentFormRef.value?.resetFields()
}

// 组件挂载时获取数据
onMounted(async () => {
  await getTeacherList()
  await getClassList()
  getStudentList()
})
</script>

<style scoped>
.student-list {
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

.pagination {
  margin-top: 20px;
  text-align: right;
}

.dialog-footer {
  text-align: right;
}
</style>