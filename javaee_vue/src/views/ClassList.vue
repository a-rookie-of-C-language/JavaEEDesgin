<template>
  <div class="class-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>班级管理</span>
          <el-button type="primary" @click="showAddDialog = true">
            <el-icon>
              <Plus/>
            </el-icon>
            添加班级
          </el-button>
        </div>
      </template>

      <!-- 搜索区域 -->
      <div class="search-area">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
                v-model="searchForm.name"
                placeholder="请输入班级名称"
                clearable
                @clear="handleSearch"
            >
              <template #prefix>
                <el-icon>
                  <Search/>
                </el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="6">
            <el-input
                v-model="searchForm.teacherName"
                placeholder="请输入班主任姓名"
                clearable
                @clear="handleSearch"
            >
              <template #prefix>
                <el-icon>
                  <User/>
                </el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="6">
            <el-button type="primary" @click="handleSearch">
              <el-icon>
                <Search/>
              </el-icon>
              搜索
            </el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-col>
        </el-row>
      </div>

      <!-- 班级表格 -->
      <el-table
          :data="filteredClassList"
          style="width: 100%"
          v-loading="loading"
      >
        <el-table-column prop="id" label="班级编号" width="120"/>
        <el-table-column prop="name" label="班级名称" width="150"/>
        <el-table-column prop="teacherName" label="班主任" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.teacherName" type="success">
              {{ scope.row.teacherName }}
            </el-tag>
            <el-tag v-else type="warning">未分配</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="当前人数" width="100">
          <template #default="scope">
            <el-tag
                :type=" 'primary' "
            >
              {{ getStudentCount(scope.row.name) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="scope">
            <el-button
                type="info"
                size="small"
                @click="viewStudents(scope.row)"
            >
              查看学生
            </el-button>
            <el-button
                type="primary"
                size="small"
                @click="editClass(scope.row)"
            >
              编辑
            </el-button>
            <el-button
                type="danger"
                size="small"
                @click="deleteClass(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑班级对话框 -->
    <el-dialog
        v-model="showAddDialog"
        :title="isEdit ? '编辑班级' : '添加班级'"
        width="600px"
        @close="resetForm"
    >
      <el-form
          ref="classFormRef"
          :model="classForm"
          :rules="rules"
          label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="班级编号" prop="id">
              <el-input v-model="classForm.id" :disabled="isEdit"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="班级名称" prop="name">
              <el-input v-model="classForm.name"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-col :span="12">
          <el-form-item label="班主任" prop="teacherId">
            <el-select
                v-model="classForm.teacherId"
                placeholder="请选择班主任"
                style="width: 100%"
                filterable
            >
              <el-option
                  v-for="teacher in availableTeachers"
                  :key="teacher.id"
                  :label="teacher.name"
                  :value="teacher.id"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAddDialog = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看学生对话框 -->
    <el-dialog
        v-model="showStudentsDialog"
        :title="`${selectedClass?.name} - 学生列表`"
        width="800px"
    >
      <el-table :data="classStudents" style="width: 100%">
        <el-table-column prop="id" label="学号" width="120"/>
        <el-table-column prop="name" label="姓名" width="120"/>
      </el-table>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showStudentsDialog = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, reactive, computed, onMounted, getCurrentInstance} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Plus, Search, User} from '@element-plus/icons-vue'

const {proxy} = getCurrentInstance()

// 响应式数据
const loading = ref(false)
const showAddDialog = ref(false)
const showStudentsDialog = ref(false)
const isEdit = ref(false)
const classList = ref([])
const teacherList = ref([])
const studentCounts = ref({})
const classStudents = ref([])
const selectedClass = ref(null)
const classFormRef = ref()

// 搜索表单
const searchForm = reactive({
  name: '',
  teacherName: ''
})

// 班级表单
const classForm = reactive({
  id: '',
  name: '',
  teacherId: ''
})

// 表单验证规则
const rules = {
  id: [{required: true, message: '请输入班级编号', trigger: 'blur'}],
  name: [{required: true, message: '请输入班级名称', trigger: 'blur'}],
  teacherId: [{required: true, message: '请选择班主任', trigger: 'change'}],
}

// 过滤后的班级列表
const filteredClassList = computed(() => {
  return classList.value.filter(clazz => {
    const nameMatch = !searchForm.name || clazz.name.includes(searchForm.name)
    const teacherMatch = !searchForm.teacherName ||
        (clazz.teacherName && clazz.teacherName.includes(searchForm.teacherName))
    return nameMatch && teacherMatch
  })
})

// 可用的教师列表（未分配班级的教师）
const availableTeachers = computed(() => {
  const assignedTeacherIds = classList.value.map(c => c.teacherId).filter(Boolean)
  return teacherList.value.filter(teacher =>
      !assignedTeacherIds.includes(teacher.id) ||
      (isEdit.value && teacher.id === classForm.teacherId)
  )
})

// 获取班级列表
const getClassList = async () => {
  loading.value = true
  try {
    const response = await proxy.$http.get('/class/list')
    if (response.data.code === 200) {
      classList.value = response.data.data
      // 为每个班级添加教师名称
      await enrichClassData()
      // 获取每个班级的学生数量
      await getStudentCounts()
    }
  } catch (error) {
    ElMessage.error('获取班级列表失败')
  } finally {
    loading.value = false
  }
}

// 丰富班级数据（添加教师名称）
const enrichClassData = async () => {
  for (const clazz of classList.value) {
    if (clazz.teacherId) {
      const teacher = teacherList.value.find(t => t.id === clazz.teacherId)
      if (teacher) {
        clazz.teacherName = teacher.name
      }
    }
  }
}

// 获取教师列表
const getTeacherList = async () => {
  try {
    const response = await proxy.$http.get('/teacher/list')
    if (response.data.code === 200) {
      teacherList.value = response.data.data
    }
  } catch (error) {
    ElMessage.error('获取教师列表失败')
  }
}

// 获取每个班级的学生数量
const getStudentCounts = async () => {
  try {
    for (const clazz of classList.value) {
      const response = await proxy.$http.get(`/student/class/${encodeURIComponent(clazz.id)}`)
      if (response.data.code === 200) {
        studentCounts.value[clazz.name] = response.data.data.length
      }
    }
  } catch (error) {
    console.error('获取学生数量失败:', error)
  }
}

// 获取学生数量
const getStudentCount = (className) => {
  return studentCounts.value[className] || 0
}

// 查看班级学生
const viewStudents = async (clazz) => {
  selectedClass.value = clazz
  try {
    const response = await proxy.$http.get(`/student/class/${encodeURIComponent(clazz.id)}`)
    if (response.data.code === 200) {
      classStudents.value = response.data.data
      showStudentsDialog.value = true
    }
  } catch (error) {
    ElMessage.error('获取学生列表失败')
  }
}

// 搜索班级
const handleSearch = () => {
  // 使用计算属性自动过滤，无需额外操作
}

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    name: '',
    teacherName: ''
  })
}

// 编辑班级
const editClass = (clazz) => {
  isEdit.value = true
  Object.assign(classForm, clazz)
  showAddDialog.value = true
}

// 删除班级
const deleteClass = async (clazz) => {
  try {
    // 检查是否有学生
    const studentCount = getStudentCount(clazz.name)
    if (studentCount > 0) {
      ElMessage.warning(`该班级还有 ${studentCount} 名学生，无法删除`)
      return
    }

    await ElMessageBox.confirm(
        `确定要删除班级 ${clazz.name} 吗？`,
        '确认删除',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    const response = await proxy.$http.delete(`/class/delete/${clazz.id}`)
    if (response.data.code === 200) {
      ElMessage.success('删除成功')
      await getClassList()
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
    await classFormRef.value.validate()

    const submitData = {
      id: classForm.id,
      name: classForm.name,
      teacherId: classForm.teacherId
    }

    const url = isEdit.value ? `/class/update/${classForm.id}` : '/class/add'
    console.log(classForm)
    const response = await proxy.$http[isEdit.value ? 'put' : 'post'](url, submitData)

    if (response.data.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '添加成功')
      showAddDialog.value = false
      await getClassList()
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
  Object.assign(classForm, {
    id: '',
    name: '',
    teacherId: '',
  })
  classFormRef.value?.resetFields()
}

// 组件挂载时获取数据
onMounted(async () => {
  await getTeacherList()
  await getClassList()
})
</script>

<style scoped>
.class-list {
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