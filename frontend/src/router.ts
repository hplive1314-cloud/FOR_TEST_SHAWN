import { createRouter, createWebHistory } from 'vue-router'
import { auth, clearAuth, hasToken, loadUser } from './stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/content' },
    { path: '/login', name: 'login', component: () => import('./views/LoginView.vue') },
    { path: '/content', name: 'content', component: () => import('./views/ContentView.vue'), meta: { requiresAuth: true } },
    { path: '/:pathMatch(.*)*', redirect: '/content' },
  ],
})

router.beforeEach(async (to) => {
  if (hasToken() && !auth.user) {
    try { await loadUser() } catch { clearAuth() }
  }
  if (to.meta.requiresAuth && !auth.user) return { name: 'login' }
  if (to.name === 'login' && auth.user) return { name: 'content' }
  document.title = `${to.name === 'login' ? '登录' : '内容空间'} · 启程工作台`
})

export default router
