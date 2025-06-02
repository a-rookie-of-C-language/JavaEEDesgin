<!-- 保留原有模板结构，添加以下功能 -->
<script setup>
import {ref, reactive, onMounted, nextTick} from 'vue'
import axios from 'axios'
import {ElMessage} from 'element-plus'

// 状态管理
const loading = ref(false)
const inputMessage = ref('')
const messages = ref([])
const messagesContainer = ref(null)
// AI状态
const aiStatus = reactive({
  available: false,
  modelInfo: {}
})

// 生命周期钩子
onMounted(async () => {

})

// 发送消息函数支持流式输出
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

  // 创建AI消息占位符
  const aiMessageIndex = messages.value.length
  const aiMessage = {
    role: 'assistant',
    content: '',
    timestamp: new Date(),
    streaming: true
  }
  messages.value.push(aiMessage)

  try {
    await nextTick()
    scrollToBottom()

    // 使用流式API
    await streamChatResponse(currentInput, aiMessageIndex)

  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败，请检查网络连接')
    // 移除失败的AI消息
    messages.value.splice(aiMessageIndex, 1)
  } finally {
    loading.value = false
  }
}

// 流式聊天响应处理
const streamChatResponse = async (message, messageIndex) => {
  try {
    const response = await fetch('http://localhost:8080/ai/chat-stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json; charset=UTF-8',
      },
      body: JSON.stringify({
        message: message
      })
    })

    if (!response.ok) {
      throw new Error('网络请求失败')
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')

    while (true) {
      const {done, value} = await reader.read()

      if (done) {
        // 流结束，标记为非流式状态
        messages.value[messageIndex].streaming = false
        break
      }

      // 解码接收到的数据
      const chunk = decoder.decode(value, {stream: true})
      
      // 检查是否是错误信息
      if (chunk.includes('[错误:')) {
        throw new Error(chunk)
      }
      
      // 直接追加token到消息内容
      messages.value[messageIndex].content += chunk
      
      await nextTick()
      scrollToBottom()
    }

  } catch (error) {
    console.error('流式响应处理失败:', error)
    throw error
  }
}

// 清空聊天
const clearChat = () => {
  messages.value = []
  ElMessage.success('对话已清空')
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
</script>

<template>
  <div class="ai-chat">
    <!-- 聊天容器 -->
    <el-card class="chat-card">
      <template #header>
        <div class="card-header">
          <span>AI对话 (无会话保存)</span>
          <div>
            <el-button size="small" type="danger" @click="clearChat">清空对话</el-button>
          </div>
        </div>
      </template>

      <div class="chat-container">
        <!-- 消息显示区域 -->
        <div ref="messagesContainer" class="chat-messages">
          <div v-for="(msg, index) in messages" :key="index" 
               class="message" 
               :class="{ 'user-message': msg.role === 'user', 'ai-message': msg.role === 'assistant' }">
            
            <!-- 消息正文 -->
            <div class="message-content">
              <div class="content-text">{{ msg.content }}</div>
              <div v-if="msg.streaming" class="typing-cursor">|</div>
            </div>

            <div class="message-time">{{ formatTime(msg.timestamp) }}</div>
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
            <span class="input-tip">Ctrl + Enter 发送</span>
            <el-button 
              type="primary" 
              @click="sendMessage" 
              :loading="loading"
              :disabled="!aiStatus.available"
            >
              {{ loading ? '发送中...' : '发送' }}
            </el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.typing-cursor {
  display: inline-block;
  animation: blink 1s infinite;
  color: #409eff;
  font-weight: bold;
}

@keyframes blink {
  0%, 50% {
    opacity: 1;
  }
  51%, 100% {
    opacity: 0;
  }
}

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

.content-text {
  line-height: 1.5;
  word-wrap: break-word;
  white-space: pre-wrap;
}

.message-time {
  font-size: 12px;
  opacity: 0.7;
  margin-top: 5px;
}

.user-message .message-time {
  text-align: right;
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