<template>
  <div class="ai-chat">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>AI助手</span>
          <el-button @click="clearChat" type="danger" plain>
            <el-icon><Delete /></el-icon>
            清空对话
          </el-button>
        </div>
      </template>

      <!-- 聊天区域 -->
      <div class="chat-container" ref="chatContainer">
        <div class="chat-messages">
          <div 
            v-for="(message, index) in messages" 
            :key="index" 
            :class="['message', message.type]"
          >
            <div class="message-avatar">
              <el-avatar 
                :icon="message.type === 'user' ? User : Robot" 
                :style="{ backgroundColor: message.type === 'user' ? '#409eff' : '#67c23a' }"
              />
            </div>
            <div class="message-content">
              <div class="message-text" v-html="formatMessage(message.content)"></div>
              <div class="message-time">{{ formatTime(message.timestamp) }}</div>
            </div>
          </div>
          
          <!-- 正在输入指示器 -->
          <div v-if="isTyping" class="message ai typing">
            <div class="message-avatar">
              <el-avatar :icon="Robot" style="background-color: #67c23a" />
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
      </div>

      <!-- 输入区域 -->
      <div class="chat-input">
        <el-row :gutter="10">
          <el-col :span="20">
            <el-input
              v-model="inputMessage"
              type="textarea"
              :rows="3"
              placeholder="请输入您的问题..."
              @keydown.ctrl.enter="sendMessage"
              :disabled="isTyping"
            />
          </el-col>
          <el-col :span="4">
            <el-button 
              type="primary" 
              @click="sendMessage" 
              :loading="isTyping"
              :disabled="!inputMessage.trim()"
              style="height: 100%; width: 100%"
            >
              <el-icon><Promotion /></el-icon>
              发送
            </el-button>
          </el-col>
        </el-row>
        <div class="input-tip">
          提示：按 Ctrl + Enter 快速发送
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted, getCurrentInstance } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Delete, Promotion } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()

// 响应式数据
const inputMessage = ref('')
const isTyping = ref(false)
const messages = ref([])
const chatContainer = ref()

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || isTyping.value) return

  const userMessage = {
    type: 'user',
    content: inputMessage.value.trim(),
    timestamp: new Date()
  }
  
  messages.value.push(userMessage)
  const messageToSend = inputMessage.value.trim()
  inputMessage.value = ''
  
  // 滚动到底部
  await nextTick()
  scrollToBottom()
  
  // 开始AI回复
  isTyping.value = true
  
  const aiMessage = {
    type: 'ai',
    content: '',
    timestamp: new Date()
  }
  messages.value.push(aiMessage)
  
  try {
    const response = await fetch('http://localhost:8080/ai/chat-stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json;charset=UTF-8',
        'Accept': 'text/plain;charset=UTF-8'
      },
      body: JSON.stringify({ message: messageToSend })
    })
    
    if (!response.ok) {
      throw new Error('网络请求失败')
    }
    
    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    
    while (true) {
      const { done, value } = await reader.read()
      
      if (done) break
      
      const chunk = decoder.decode(value, { stream: true })
      aiMessage.content += chunk
      
      // 滚动到底部
      await nextTick()
      scrollToBottom()
    }
  } catch (error) {
    console.error('AI聊天错误:', error)
    aiMessage.content = '抱歉，AI服务暂时不可用，请稍后再试。'
    ElMessage.error('AI服务连接失败')
  } finally {
    isTyping.value = false
    await nextTick()
    scrollToBottom()
  }
}

// 清空对话
const clearChat = () => {
  messages.value = []
  ElMessage.success('对话已清空')
}

// 格式化消息内容（支持简单的markdown）
const formatMessage = (content) => {
  if (!content) return ''
  
  // 简单的markdown支持
  return content
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>') // 粗体
    .replace(/\*(.*?)\*/g, '<em>$1</em>') // 斜体
    .replace(/`(.*?)`/g, '<code>$1</code>') // 行内代码
    .replace(/\n/g, '<br>') // 换行
}

// 格式化时间
const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 滚动到底部
const scrollToBottom = () => {
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight
  }
}

// 组件挂载时添加欢迎消息
onMounted(() => {
  messages.value.push({
    type: 'ai',
    content: '您好！我是AI助手，有什么可以帮助您的吗？',
    timestamp: new Date()
  })
})
</script>

<style scoped>
.ai-chat {
  padding: 20px;
  height: calc(100vh - 140px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-container {
  height: calc(100vh - 300px);
  overflow-y: auto;
  padding: 20px 0;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 20px;
}

.chat-messages {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.message {
  display: flex;
  gap: 12px;
  max-width: 80%;
}

.message.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message.ai {
  align-self: flex-start;
}

.message-avatar {
  flex-shrink: 0;
}

.message-content {
  flex: 1;
}

.message-text {
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.5;
  word-wrap: break-word;
}

.message.user .message-text {
  background-color: #409eff;
  color: white;
  border-bottom-right-radius: 4px;
}

.message.ai .message-text {
  background-color: #f5f7fa;
  color: #303133;
  border-bottom-left-radius: 4px;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  text-align: right;
}

.message.user .message-time {
  text-align: left;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 12px 16px;
  background-color: #f5f7fa;
  border-radius: 12px;
  border-bottom-left-radius: 4px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #909399;
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
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.chat-input {
  position: relative;
}

.input-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
  text-align: right;
}

/* 滚动条样式 */
.chat-container::-webkit-scrollbar {
  width: 6px;
}

.chat-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.chat-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.chat-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 代码样式 */
:deep(code) {
  background-color: #f4f4f5;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  font-size: 0.9em;
}

:deep(strong) {
  font-weight: bold;
}

:deep(em) {
  font-style: italic;
}
</style>