import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { useOrganizationStore } from './stores/organizationStore.js'; // 导入 organization store
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
//import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
// 如果您正在使用CDN引入，请删除下面一行。
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
//import './assets/index.scss'
const app = createApp(App)
const pinia =createPinia()

app.use(router)
app.use(pinia)

// 在应用启动时，尝试从localStorage恢复组织登录状态
const organizationStore = useOrganizationStore();
organizationStore.initializeStore();

app.use(ElementPlus,{
    locale:zhCn,
    })

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')
