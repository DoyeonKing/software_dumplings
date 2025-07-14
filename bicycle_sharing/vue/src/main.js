
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import 'leaflet/dist/leaflet.css'

// 导入Element Plus样式
import 'element-plus/dist/index.css'

const app = createApp(App)

app.use(router)

app.mount('#app')
