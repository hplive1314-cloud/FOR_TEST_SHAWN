export const TOKEN_KEY = 'qicheng.access-token'

export class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message)
  }
}

export async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers)
  if (options.body) headers.set('Content-Type', 'application/json')
  const token = sessionStorage.getItem(TOKEN_KEY)
  if (token) headers.set('Authorization', `Bearer ${token}`)
  let response: Response
  try {
    response = await fetch(`/api${path}`, { ...options, headers, credentials: 'omit' })
  } catch (error) {
    if (error instanceof DOMException && error.name === 'AbortError') throw error
    throw new ApiError(0, '暂时无法连接服务，请稍后重试')
  }
  if (!response.ok) {
    const body = await response.json().catch(() => null)
    if (response.status === 401 && path !== '/auth/login') {
      window.dispatchEvent(new Event('auth:expired'))
    }
    throw new ApiError(response.status, body?.message || '服务暂时不可用，请稍后重试')
  }
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}
