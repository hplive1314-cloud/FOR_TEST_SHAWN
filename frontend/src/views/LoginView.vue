<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowRight, Lock, User, CircleCheck, Connection } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import BrandLogo from '../components/BrandLogo.vue'
import { login } from '../stores/auth'

const router = useRouter()
const route = useRoute()
const formRef = ref<FormInstance>()
const form = reactive({ username: '', password: '' })
const loading = ref(false)
const error = ref('')
const isDev = import.meta.env.DEV
const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function submit() {
  if (loading.value || !await formRef.value?.validate().catch(() => false)) return
  loading.value = true
  error.value = ''
  try {
    await login(form.username.trim(), form.password)
    await router.replace('/content')
  } catch (e) {
    error.value = e instanceof Error ? e.message : '登录失败，请重试'
  } finally { loading.value = false }
}

function fillDemo() {
  form.username = 'admin'
  form.password = 'Admin@123456'
  error.value = ''
}
</script>

<template>
  <main class="login-layout">
    <section class="login-story" aria-label="启程工作台介绍">
      <BrandLogo />
      <div class="story-copy">
        <span class="eyebrow light"><span class="small-dot" /> A SPACE FOR YOUR NEXT IDEA</span>
        <h1>让工作有序，<br />让想法<span class="serif-accent">启程。</span></h1>
        <p>一个简洁、专注的工作空间。<br />连接你的内容与灵感，从容开始每一天。</p>
      </div>
      <div class="workspace-illustration" aria-hidden="true">
        <div class="orbit orbit-one" /><div class="orbit orbit-two" />
        <div class="floating-label"><span class="floating-dot" /> 少一点繁杂，多一点专注</div>
        <div class="mini-workspace">
          <div class="mini-top"><i /><i /><i /><span>我的工作空间</span></div>
          <div class="mini-body">
            <div class="mini-nav"><b /><i /><i /><i /></div>
            <div class="mini-main"><small>让美好的想法，在这里发生</small><strong>每一步，都井然有序。</strong><div class="mini-stats"><i /><i /><i /></div><div class="mini-line" /><div class="mini-line short" /><div class="mini-line" /></div>
          </div>
        </div>
        <div class="floating-check"><el-icon><CircleCheck /></el-icon><div><strong>准备就绪</strong><span>下一个好想法，从这里开始</span></div></div>
      </div>
      <div class="story-footer"><span>专注当下，自有方向。</span><span>EST. 2026 <span class="footer-star">✳</span></span></div>
    </section>

    <section class="login-panel">
      <div class="mobile-brand"><BrandLogo /></div>
      <div class="login-topline"><span>你的下一程，从这里开始</span><span class="outline-badge">工作台 v1.0</span></div>
      <div class="login-form-wrap">
        <div class="welcome-symbol" aria-hidden="true"><el-icon><Connection /></el-icon></div>
        <span class="eyebrow">WELCOME BACK</span>
        <h2>欢迎回来</h2>
        <p class="login-description">登录你的账号，继续今天的精彩。</p>
        <el-alert v-if="route.query.expired && !error" title="登录已过期，请重新登录" type="warning" :closable="false" show-icon class="login-alert" />
        <el-alert v-if="error" :title="error" type="error" :closable="false" show-icon class="login-alert" role="alert" />
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large" @submit.prevent="submit">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" :prefix-icon="User" placeholder="请输入用户名" autocomplete="username" maxlength="64" :disabled="loading" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" :prefix-icon="Lock" type="password" show-password placeholder="请输入密码" autocomplete="current-password" maxlength="72" :disabled="loading" />
          </el-form-item>
          <div class="login-hint"><el-icon><Lock /></el-icon><span>安全登录，专注你的每一次创造</span></div>
          <el-button type="primary" class="login-submit" native-type="submit" :loading="loading">登录工作台<el-icon v-if="!loading"><ArrowRight /></el-icon></el-button>
        </el-form>
        <div v-if="isDev" class="demo-note"><span>本地体验账号已准备好</span><el-button link type="primary" @click="fillDemo">填入体验账号 <span aria-hidden="true">↗</span></el-button><small>admin / Admin@123456</small></div>
        <div class="login-divider"><span />简单一点，高效一点<span /></div>
      </div>
      <footer class="login-footer"><span>© 2026 启程 Workspace</span><span><span class="small-dot" /> 为专注而设计</span></footer>
    </section>
  </main>
</template>
