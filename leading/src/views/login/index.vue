<template>
  <div class="login-container">
    <div class="login-shell">
      <section class="login-brand-panel">
        <div class="brand-mark">XY</div>
        <h1>跨境电商管理系统</h1>
        <p>运营工作台</p>
      </section>

      <section class="login-panel">
        <div class="login-header">
          <h2>{{ activeTab === 'login' ? '欢迎回来' : '创建账号' }}</h2>
          <p>{{ activeTab === 'login' ? '使用手机号进入系统' : '填写信息完成注册' }}</p>
        </div>

        <el-tabs v-model="activeTab" class="auth-tabs" stretch>
          <el-tab-pane label="登录" name="login">
            <el-form
              ref="loginForm"
              :model="loginForm"
              :rules="loginRules"
              class="auth-form"
              label-position="top"
              hide-required-asterisk
            >
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="loginForm.phone" placeholder="请输入手机号">
                  <template #prefix>
                    <el-icon><Iphone /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password>
                  <template #prefix>
                    <el-icon><Lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              <el-button type="primary" class="submit-button" @click="handleLogin">登录</el-button>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="注册" name="register">
            <el-form
              ref="registerForm"
              :model="registerForm"
              :rules="registerRules"
              class="auth-form register-form"
              label-position="top"
              hide-required-asterisk
            >
              <el-form-item label="姓名" prop="realName">
                <el-input v-model="registerForm.realName" placeholder="请输入姓名">
                  <template #prefix>
                    <el-icon><User /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="registerForm.phone" placeholder="请输入手机号">
                  <template #prefix>
                    <el-icon><Iphone /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item label="微信号" prop="wechat">
                <el-input v-model="registerForm.wechat" placeholder="请输入微信号">
                  <template #prefix>
                    <el-icon><ChatDotRound /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" show-password>
                  <template #prefix>
                    <el-icon><Lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码" show-password>
                  <template #prefix>
                    <el-icon><Lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item label="邀请码" prop="inviteCode">
                <el-input v-model="registerForm.inviteCode" placeholder="请输入邀请码">
                  <template #prefix>
                    <el-icon><Ticket /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              <el-button type="primary" class="submit-button" @click="handleRegister">注册</el-button>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </section>
    </div>
  </div>
</template>

<script>
import { ChatDotRound, Iphone, Lock, Ticket, User } from '@element-plus/icons-vue'
import loginMixin from './index'

export default {
  name: 'LoginView',
  components: {
    ChatDotRound,
    Iphone,
    Lock,
    Ticket,
    User
  },
  mixins: [loginMixin]
}
</script>

<style scoped>
@import './index.css';
</style>
