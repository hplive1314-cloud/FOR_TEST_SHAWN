import { createApp } from 'vue'
import 'element-plus/es/components/message/style/css'
import './styles.css'
import App from './App.vue'
import router from './router'
import { clearAuth } from './stores/auth'

window.addEventListener('auth:expired', () => {
  clearAuth()
  if (router.currentRoute.value.name !== 'login') {
    void router.replace({ name: 'login', query: { expired: '1' } })
  }
})

createApp(App).use(router).mount('#app')
