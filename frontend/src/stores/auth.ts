import { reactive } from 'vue'
import { request, TOKEN_KEY } from '../api'

export interface User { id: number; username: string; displayName: string }
interface LoginResult { token: string; expiresAt: string; user: User }

export const auth = reactive<{ user: User | null }>({ user: null })
export const hasToken = () => Boolean(sessionStorage.getItem(TOKEN_KEY))

export async function login(username: string, password: string) {
  const result = await request<LoginResult>('/auth/login', { method: 'POST', body: JSON.stringify({ username, password }) })
  sessionStorage.setItem(TOKEN_KEY, result.token)
  auth.user = result.user
}

export async function loadUser() {
  auth.user = await request<User>('/auth/me')
}

export function clearAuth() {
  sessionStorage.removeItem(TOKEN_KEY)
  auth.user = null
}

export async function logout() {
  await request<void>('/auth/logout', { method: 'POST' })
  clearAuth()
}
