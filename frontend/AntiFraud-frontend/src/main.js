import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { zhCn } from 'element-plus/es/locale/index.mjs'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import PersonForm from './components/PersonForm.vue'
import AvatarUpload from './components/AvatarUpload.vue'
import { reactive } from 'vue'

const app = createApp(App)

// 【新增】创建全局消息状态，用于实时同步红点
const messageState = reactive({
  unreadCount: 0,
  // 更新未读数量
  updateUnreadCount(count) {
    this.unreadCount = count
  }
})

// 【新增】提供全局消息状态给所有子组件
app.provide('messageState', messageState)

app.use(router)
app.use(ElementPlus,{
    locale:zhCn,
})

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.component('PersonForm', PersonForm)
app.component('AvatarUpload', AvatarUpload)

app.mount('#app')