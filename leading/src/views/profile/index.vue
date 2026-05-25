<template>
  <el-card class="profile-card" v-loading="loading">
    <template #header>
      <div class="card-header-row">
        <div class="card-header-main">
          <span class="card-title">个人中心</span>
          <div class="card-subtitle">维护登录账号、联系方式和密码信息</div>
        </div>
        <el-button type="primary" @click="openEditDialog">
          <el-icon><EditPen /></el-icon>
          <span>编辑资料</span>
        </el-button>
      </div>
    </template>

    <div class="profile-layout">
      <section class="profile-identity-card">
        <div class="avatar-block">
          {{ (currentUser.realName || currentUser.phone || '用').slice(0, 1) }}
        </div>
        <h3>{{ currentUser.realName || '-' }}</h3>
        <p>{{ currentUser.phone || '-' }}</p>
        <el-tag effect="light" class="role-tag">
          {{ currentUser.role && currentUser.role.roleName ? currentUser.role.roleName : 'SUPER_ADMIN' }}
        </el-tag>
      </section>

      <section class="profile-content">
        <div class="info-grid">
          <div class="info-tile">
            <el-icon><User /></el-icon>
            <div>
              <span>姓名</span>
              <strong>{{ currentUser.realName || '-' }}</strong>
            </div>
          </div>
          <div class="info-tile">
            <el-icon><Iphone /></el-icon>
            <div>
              <span>手机号</span>
              <strong>{{ currentUser.phone || '-' }}</strong>
            </div>
          </div>
          <div class="info-tile">
            <el-icon><ChatDotRound /></el-icon>
            <div>
              <span>微信号</span>
              <strong>{{ currentUser.wechat || '-' }}</strong>
            </div>
          </div>
          <div class="info-tile">
            <el-icon><Ticket /></el-icon>
            <div>
              <span>邀请码</span>
              <strong>{{ currentUser.inviteCode || '-' }}</strong>
            </div>
          </div>
        </div>

        <div class="security-card">
          <div class="security-main">
            <div class="security-icon">
              <el-icon><Lock /></el-icon>
            </div>
            <div class="security-text">
              <span>登录密码</span>
              <div class="password-value">
                <strong>{{ showPassword ? currentUser.password || '无' : '******' }}</strong>
                <el-button text class="icon-action" @click="togglePassword">
                  <el-icon>
                    <Hide v-if="showPassword" />
                    <View v-else />
                  </el-icon>
                </el-button>
              </div>
            </div>
          </div>
          <el-button type="primary" plain class="password-button" @click="openPasswordDialog">
            <el-icon><Key /></el-icon>
            <span>修改密码</span>
          </el-button>
        </div>
      </section>
    </div>

    <el-dialog title="编辑个人信息" v-model="dialogVisible" width="500px">
      <el-form :model="editForm" label-width="90px" class="profile-dialog-form">
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="editForm.realName" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" />
        </el-form-item>
        <el-form-item label="微信号" prop="wechat">
          <el-input v-model="editForm.wechat" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveChanges">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog title="修改密码" v-model="passwordDialogVisible" width="500px">
      <el-form :model="passwordForm" label-width="90px" class="profile-dialog-form">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="passwordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="savePassword">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </el-card>
</template>

<script>
import {
  ChatDotRound,
  EditPen,
  Hide,
  Iphone,
  Key,
  Lock,
  Ticket,
  User,
  View
} from '@element-plus/icons-vue'
import profileMixin from './index'

export default {
  name: 'ProfileView',
  components: {
    ChatDotRound,
    EditPen,
    Hide,
    Iphone,
    Key,
    Lock,
    Ticket,
    User,
    View
  },
  mixins: [profileMixin]
}
</script>

<style scoped>
@import './index.css';
</style>
