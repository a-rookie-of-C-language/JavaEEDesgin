<template>
  <div class="ai-chat">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>AI助手</span>
          <div class="status-info">
            <el-tag :type="aiStatus.available ? 'success' : 'danger'">
              {{ aiStatus.available ? '在线' : '离线' }}
            </el-tag>
            <el-button size="small" @click="checkStatus">检查状态</el-button>
          </div>
        </div>
      </template>

      <!-- 聊天区域 -->
      <div class="chat-container">
        <div class="chat-messages" ref="messagesContainer">
          <div
            v-for="(message, index) in messages"
            :key="index"
            :class="['message', message.role === 'user' ? 'user-message' : 'ai-message']"
          >
            <div class="message-avatar">
              <el-avatar :size="32">
                <el-icon v-if="message.role === 'user'"><User /></el-icon>
                <el-icon v-else><Robot /></el-icon>
              </el-avatar>
            </div>
            <div class="message-content">
              <div class="message-text">{{ message.content }}</div>
              <div class="message-time">{{ formatTime(message.timestamp) }}</div>
            </div>
          </div>
          <div v-if="loading" class="message ai-message">
            <div class="message-avatar">
              <el-avatar :size="32">
                <el-icon><User /></el-icon>
              </el-avatar>
            </div>
            <div class="message-content">
              <div class="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="chat-input">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="3"
            placeholder="请输入您的问题..."
            @keydown.ctrl.enter="sendMessage"
          />
          <div class="input-actions">
            <el-button @click="clearChat">清空对话</el-button>
            <el-button type="primary" @click="sendMessage" :loading="loading">
              发送 (Ctrl+Enter)
            </el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { User } from '@element-plus/icons-vue'
import axios from 'axios'

// Configure axios default encoding
axios.defaults.headers.post['Content-Type'] = 'application/json;charset=UTF-8'
axios.defaults.headers.get['Accept'] = 'application/json;charset=UTF-8'

// Reactive data declarations - ADD THESE MISSING VARIABLES
const loading = ref(false)
const inputMessage = ref('')
const messages = ref([])
const messagesContainer = ref()
const aiStatus = reactive({
  available: false,
  modelInfo: {}
})

// Send message function
const sendMessage = async () => {
  if (!inputMessage.value.trim()) {
    ElMessage.warning('请输入消息内容')
    return
  }

  const userMessage = {
    role: 'user',
    content: inputMessage.value,
    timestamp: new Date()
  }

  messages.value.push(userMessage)
  const currentInput = inputMessage.value
  inputMessage.value = ''
  loading.value = true

  try {
    await nextTick()
    scrollToBottom()

    let response
    if (messages.value.length > 1) {
      const chatHistory = messages.value
        .filter(msg => msg.role)
        .map(msg => ({
          role: msg.role,
          content: msg.content
        }))
      
      response = await axios.post('/ai/chat-history', {
        messages: chatHistory
      }, {
        headers: {
          'Content-Type': 'application/json;charset=UTF-8'
        }
      })
    } else {
      response = await axios.post('/ai/chat', {
        message: currentInput
      }, {
        headers: {
          'Content-Type': 'application/json;charset=UTF-8'
        }
      })
    }

    if (response.data.code === 200) {
      const aiMessage = {
        role: 'assistant',
        content: response.data.data.response,
        timestamp: new Date()
      }
      messages.value.push(aiMessage)
    } else {
      ElMessage.error(response.data.msg || 'AI服务异常')
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败，请检查网络连接')
  } finally {
    loading.value = false
    await nextTick()
    scrollToBottom()
  }
}

// Check AI status
const checkStatus = async () => {
  try {
    const response = await axios.get('/ai/status')
    console.log('Status response:', response.data)
    if (response.data.code === 200) {
      aiStatus.available = response.data.data.available
      aiStatus.modelInfo = response.data.data.modelInfo
      ElMessage.success('状态检查完成')
    } else {
      aiStatus.available = false
      ElMessage.error(response.data.msg || 'AI服务异常')
    }
  } catch (error) {
    aiStatus.available = false
    ElMessage.error('无法连接到AI服务')
  }
}

// Clear chat
const clearChat = () => {
  messages.value = []
  ElMessage.success('对话已清空')
}

// Scroll to bottom
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// Format time
const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleTimeString()
}

// Check status on component mount
onMounted(() => {
  checkStatus()
})
</script>

<style scoped>
.ai-chat {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.chat-container {
  height: 600px;
  display: flex;
  flex-direction: column;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 20px;
}

.message {
  display: flex;
  margin-bottom: 20px;
  align-items: flex-start;
}

.user-message {
  flex-direction: row-reverse;
}

.user-message .message-content {
  background-color: #409eff;
  color: white;
  margin-right: 10px;
}

.ai-message .message-content {
  background-color: white;
  border: 1px solid #e4e7ed;
  margin-left: 10px;
}

.message-content {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.message-text {
  line-height: 1.5;
  word-wrap: break-word;
}

.message-time {
  font-size: 12px;
  opacity: 0.7;
  margin-top: 5px;
}

.user-message .message-time {
  text-align: right;
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #409eff;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) {
  animation-delay: -0.32s;
}

.typing-indicator span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes typing {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

.chat-input {
  border-top: 1px solid #e4e7ed;
  padding-top: 20px;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
}
</style>