import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { zhCn } from 'element-plus/es/locale/index.mjs'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import PersonForm from './components/PersonForm.vue'
import AvatarUpload from './components/AvatarUpload.vue'


const app = createApp(App)

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