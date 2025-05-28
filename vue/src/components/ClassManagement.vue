<template>
  <div class="class-management">
    <div class="header">
      <h2>班级管理</h2>
      <el-button type="primary" @click="showAddDialog" :icon="Plus">
        添加班级
      </el-button>
    </div>
    
    <el-table :data="classList" stripe border>
      <el-table-column prop="id" label="班级编号" width="120" />
      <el-table-column prop="name" label="班级名称" width="150" />
      <el-table-column label="班主任" width="120">
        <template #default="{ row }">
          {{ getTeacherName(row.teacherId) }}
        </template>
      </el-table-column>
      <el-table-column prop="studentCount" label="学生人数" width="100" />
      <el-table-column prop="description" label="班级描述" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="editClass(row)">
            编辑
          </el-button>
          <el-button size="small" type="danger" @click="deleteClass(row.id)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 添加/编辑班级对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="classFormRef" :model="classForm" :rules="formRules" label-width="80px">
        <el-form-item label="班级编号" prop="id">
          <el-input v-model="classForm.id" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="班级名称" prop="name">
          <el-input v-model="classForm.name" />
        </el-form-item>
        <el-form-item label="班主任" prop="teacherId">
          <el-select v-model="classForm.teacherId" placeholder="选择班主任" style="width: 100%">
            <el-option
              v-for="teacher in teacherList"
              :key="teacher.id"
              :label="teacher.name"
              :value="teacher.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="班级描述" prop="description">
          <el-input v-model="classForm.description" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

// 响应式数据
const classList = ref([])
const teacherList = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const classFormRef = ref()

// 班级表单
const classForm = reactive({
  id: '',
  name: '',
  teacherId: '',
  description: ''
})

// 表单验证规则
const formRules = {
  id: [{ required: true, message: '请输入班级编号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入班级名称', trigger: 'blur' }],
  teacherId: [{ required: true, message: '请选择班主任', trigger: 'change' }]
}

// 计算属性
const dialogTitle = computed(() => isEdit.value ? '编辑班级' : '添加班级')

// 方法
const getTeacherName = (teacherId) => {
  const teacher = teacherList.value.find(t => t.id === teacherId)
  return teacher ? teacher.name : '未分配'
}

const loadClassList = async () => {
  // 实现班级列表加载逻辑
}

const loadTeacherList = async () => {
  // 实现教师列表加载逻辑
}

const showAddDialog = () => {
  isEdit.value = false
  Object.assign(classForm, { id: '', name: '', teacherId: '', description: '' })
  dialogVisible.value = true
}

const editClass = (row) => {
  isEdit.value = true
  Object.assign(classForm, row)
  dialogVisible.value = true
}

const deleteClass = async (id) => {
  // 实现删除逻辑
}

const submitForm = async () => {
  // 实现提交逻辑
}

onMounted(() => {
  loadClassList()
  loadTeacherList()
})
</script>