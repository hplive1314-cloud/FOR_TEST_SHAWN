<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown, ArrowRight, CircleCheck, Clock, Connection, Document, Files, FolderOpened, Grid, Reading, Refresh, Right, Search, SwitchButton, Close, Menu } from '@element-plus/icons-vue'
import BrandLogo from '../components/BrandLogo.vue'
import { ApiError, request } from '../api'
import { auth, logout } from '../stores/auth'

interface ContentItem { id: number; title: string; summary: string; category: string; status: 'PUBLISHED' | 'DRAFT'; updatedAt: string }
interface ContentPage { items: ContentItem[]; total: number; page: number; size: number; stats: { total: number; published: number; draft: number } }

const router = useRouter()
const result = ref<ContentPage | null>(null)
const search = ref('')
const query = ref('')
const status = ref('')
const page = ref(1)
const pageSize = 6
const loading = ref(true)
const error = ref('')
const selected = ref<ContentItem | null>(null)
const dialogOpen = computed({ get: () => selected.value !== null, set: (open: boolean) => { if (!open) selected.value = null } })
const loggingOut = ref(false)
const mobileNav = ref(false)
let timer: ReturnType<typeof setTimeout> | undefined
let controller: AbortController | undefined
const items = computed(() => result.value?.items || [])
const stats = computed(() => result.value?.stats || { total: 0, published: 0, draft: 0 })
const today = new Intl.DateTimeFormat('zh-CN', { month: 'long', day: 'numeric', weekday: 'long' }).format(new Date())
const tabs = [{ label: '全部内容', value: '' }, { label: '已发布', value: 'PUBLISHED' }, { label: '草稿', value: 'DRAFT' }]

async function load() {
  controller?.abort()
  const active = new AbortController()
  controller = active
  loading.value = true
  error.value = ''
  try {
    const params = new URLSearchParams({ q: query.value, status: status.value, page: String(page.value - 1), size: String(pageSize) })
    result.value = await request<ContentPage>(`/contents?${params}`, { signal: active.signal })
  } catch (e) {
    if (active.signal.aborted) return
    error.value = e instanceof Error ? e.message : '内容加载失败'
  } finally { if (!active.signal.aborted) loading.value = false }
}

watch(search, (value) => {
  clearTimeout(timer)
  timer = setTimeout(() => { query.value = value.trim(); page.value = 1 }, 300)
})
watch(status, () => { page.value = 1 })
watch([query, status, page], load, { immediate: true })
onBeforeUnmount(() => { clearTimeout(timer); controller?.abort() })

async function signOut() {
  if (loggingOut.value) return
  loggingOut.value = true
  try {
    await logout()
    await router.replace('/login')
  } catch (e) {
    if (!(e instanceof ApiError && e.status === 401)) ElMessage.error(e instanceof Error ? e.message : '退出失败，请重试')
  } finally { loggingOut.value = false }
}

function resetFilters() { search.value = ''; query.value = ''; status.value = ''; page.value = 1 }
const dateLabel = (value: string) => value.slice(0, 10).replaceAll('-', '.')
</script>

<template>
  <div class="workspace-layout">
    <button v-if="mobileNav" class="sidebar-overlay" aria-label="关闭导航" @click="mobileNav = false" />
    <aside class="sidebar" :class="{ open: mobileNav }">
      <div class="sidebar-brand"><BrandLogo /><button class="mobile-close icon-button" aria-label="关闭导航" @click="mobileNav = false"><el-icon><Close /></el-icon></button></div>
      <div class="workspace-picker"><span class="workspace-avatar">Q</span><div><strong>我的工作空间</strong><small>让灵感在此生长</small></div><span class="workspace-picker-mark">个人</span></div>
      <div class="nav-label">工作台 <span>WORKSPACE</span></div>
      <nav aria-label="主导航"><router-link to="/content" class="nav-item active" @click="mobileNav = false"><el-icon><Grid /></el-icon><span>内容空间</span><span class="nav-count">{{ stats.total }}</span></router-link></nav>
      <div class="sidebar-note"><div class="note-illustration" aria-hidden="true">✳</div><strong>给想法一个落脚点</strong><p>记录、整理、沉淀。<br />每一份内容，都值得被看见。</p><span>保持专注，慢慢生长 <span aria-hidden="true">↗</span></span></div>
      <div class="sidebar-bottom"><span class="connection-dot" /> 工作空间已连接<span>v1.0</span></div>
    </aside>

    <div class="workspace-main">
      <header class="topbar">
        <div class="breadcrumb"><button class="mobile-menu icon-button" aria-label="打开导航" @click="mobileNav = true"><el-icon><Menu /></el-icon></button><el-icon><FolderOpened /></el-icon><span>工作台</span><el-icon class="breadcrumb-chevron"><ArrowRight /></el-icon><strong>内容空间</strong></div>
        <el-dropdown trigger="click" @command="signOut">
          <button class="profile-button" aria-label="账户菜单"><span class="profile-avatar">{{ auth.user?.displayName.slice(0, 1) }}</span><span>{{ auth.user?.displayName }}</span><el-icon><ArrowDown /></el-icon></button>
          <template #dropdown><el-dropdown-menu><el-dropdown-item command="logout" :disabled="loggingOut" :icon="SwitchButton">{{ loggingOut ? '正在退出…' : '退出登录' }}</el-dropdown-item></el-dropdown-menu></template>
        </el-dropdown>
      </header>

      <main class="content-main">
        <div class="page-heading"><div><div class="eyebrow">YOUR CREATIVE CORNER</div><h1>内容空间<span class="heading-dot">.</span></h1><p>把灵感整理成内容，让每一份记录都有价值。</p></div><div class="today"><el-icon><Clock /></el-icon>{{ today }}</div></div>

        <section class="welcome-banner" aria-label="欢迎信息"><div class="banner-copy"><span class="banner-kicker"><span class="small-dot" /> 新的一天，新的可能</span><h2>你好，{{ auth.user?.displayName }} <span class="wave" aria-hidden="true">✦</span></h2><p>欢迎回到你的工作空间。从一份内容开始，积累每一点进步。</p><button @click="resetFilters">浏览全部内容 <el-icon><Right /></el-icon></button></div><div class="banner-art" aria-hidden="true"><div class="art-orbit"/><div class="art-paper back"/><div class="art-paper front"><span>IDEAS IN MOTION</span><i/><i/><i/><b>✳</b></div><span class="art-spark">✦</span><span class="art-small-spark">＋</span></div></section>

        <section class="stats-grid" aria-label="内容统计">
          <div class="stat-card"><div><span>全部内容</span><strong>{{ result ? stats.total.toString().padStart(2, '0') : '—' }}<small>篇</small></strong><p>每一份记录，都在这里</p></div><span class="stat-icon green"><el-icon><Files /></el-icon></span></div>
          <div class="stat-card"><div><span>已发布</span><strong>{{ result ? stats.published.toString().padStart(2, '0') : '—' }}<small>篇</small></strong><p><span class="status-dot published" /> 已准备好与世界见面</p></div><span class="stat-icon blue"><el-icon><CircleCheck /></el-icon></span></div>
          <div class="stat-card"><div><span>草稿箱</span><strong>{{ result ? stats.draft.toString().padStart(2, '0') : '—' }}<small>篇</small></strong><p><span class="status-dot draft" /> 好想法，值得再打磨</p></div><span class="stat-icon amber"><el-icon><Document /></el-icon></span></div>
        </section>

        <section class="content-panel" aria-label="内容列表">
          <div class="panel-heading"><div><h2>我的内容 <span>{{ result?.total ?? '—' }}</span></h2><p>有序收纳，随时找到需要的内容。</p></div><el-button :icon="Refresh" :loading="loading" @click="load">刷新</el-button></div>
          <div class="content-toolbar"><div class="content-tabs" role="group" aria-label="内容状态"><button v-for="tab in tabs" :key="tab.value" :class="{ selected: status === tab.value }" :aria-pressed="status === tab.value" @click="status = tab.value">{{ tab.label }}</button></div><el-input v-model="search" class="content-search" :prefix-icon="Search" placeholder="搜索内容标题…" aria-label="搜索内容标题" clearable maxlength="100" /></div>
          <div v-if="error" class="content-error" role="alert"><el-icon><Connection /></el-icon><h3>暂时未能加载内容</h3><p>{{ error }}</p><el-button type="primary" plain @click="load">重新加载</el-button></div>
          <div v-else v-loading="loading" class="content-table-wrap" :aria-busy="loading">
            <el-table :data="items" row-class-name="content-table-row" empty-text="暂无匹配内容" @row-click="selected = $event">
              <el-table-column label="内容标题" min-width="320"><template #default="{ row }"><div class="content-title-cell"><span class="document-icon" :class="row.status === 'DRAFT' ? 'is-draft' : ''"><el-icon><Reading /></el-icon></span><div><button class="content-title-button" @click.stop="selected = row">{{ row.title }}</button><p>{{ row.summary }}</p></div></div></template></el-table-column>
              <el-table-column label="分类" width="118"><template #default="{ row }"><span class="category-label">{{ row.category }}</span></template></el-table-column>
              <el-table-column label="状态" width="110"><template #default="{ row }"><span class="status-badge" :class="row.status === 'PUBLISHED' ? 'published' : 'draft'"><span class="status-dot" :class="row.status === 'PUBLISHED' ? 'published' : 'draft'" />{{ row.status === 'PUBLISHED' ? '已发布' : '草稿' }}</span></template></el-table-column>
              <el-table-column label="最近更新" width="130"><template #default="{ row }"><span class="date-cell">{{ dateLabel(row.updatedAt) }}</span></template></el-table-column>
              <el-table-column label="操作" width="76" align="center"><template #default="{ row }"><button class="read-button" :aria-label="`阅读：${row.title}`" @click.stop="selected = row"><el-icon><ArrowRight /></el-icon></button></template></el-table-column>
              <template #empty><div class="empty-state"><el-icon><Search /></el-icon><strong>没有找到相关内容</strong><span>试试其他关键词，或切换内容状态。</span><el-button link type="primary" @click="resetFilters">清除筛选</el-button></div></template>
            </el-table>
          </div>
          <div v-if="!error" class="table-footer"><span>共 {{ result?.total ?? 0 }} 篇内容 · 每页 {{ pageSize }} 篇</span><el-pagination v-model:current-page="page" :page-size="pageSize" :total="result?.total ?? 0" layout="prev, pager, next" :disabled="loading" background /></div>
        </section>
        <footer class="content-footer"><span>© 2026 启程 Workspace</span><span>让每一份想法，走得更远 <span aria-hidden="true">↗</span></span></footer>
      </main>
    </div>
    <el-dialog v-model="dialogOpen" :title="selected?.title" class="reading-dialog" width="min(600px, 92vw)" destroy-on-close>
      <template v-if="selected"><div class="reading-meta"><span class="category-label">{{ selected.category }}</span><span>{{ selected.status === 'PUBLISHED' ? '已发布' : '草稿' }}</span><time>{{ dateLabel(selected.updatedAt) }}</time></div><p class="reading-summary">{{ selected.summary }}</p><p class="reading-note">这是脚手架的示例内容摘要，可在此基础上扩展正文与内容编辑功能。</p></template>
    </el-dialog>
  </div>
</template>
