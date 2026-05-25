/**
 * 个人中心模块
 * @description 展示当前登录用户的基本信息
 */

import api from '@/api'

export default {
  name: 'ProfileView',

  data() {
    return {
      // 当前登录用户信息
      currentUser: {},
      // 编辑对话框显示状态
      dialogVisible: false,
      // 编辑表单数据
      editForm: {
        realName: '',
        phone: '',
        wechat: ''
      },
      // 修改密码对话框显示状态
      passwordDialogVisible: false,
      // 修改密码表单数据
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      // 加载状态
      loading: false,
      // 密码显示状态
      showPassword: false
    }
  },

  mounted() {
    // 从localStorage获取当前用户信息
    const userStr = localStorage.getItem('user')
    if (userStr) {
      try {
        this.currentUser = JSON.parse(userStr)
        // 尝试获取最新的用户信息，但失败时不影响页面显示
        this.fetchUserInfo().catch(err => {
          console.log('获取最新用户信息失败，使用本地数据', err)
        })
      } catch (e) {
        console.error('解析用户信息失败', e)
      }
    }
  },

  methods: {
    // 获取用户信息
    async fetchUserInfo() {
      if (!this.currentUser.id) return
      
      this.loading = true
      try {
        const response = await api.user.info(this.currentUser.id)
        if (response.code === 200) {
          this.currentUser = response.data
          // 更新localStorage中的用户信息
          localStorage.setItem('user', JSON.stringify(this.currentUser))
        }
      } catch (error) {
        console.error('获取用户信息失败', error)
        // 当获取失败时，继续使用localStorage中的数据
      } finally {
        this.loading = false
      }
    },

    // 打开编辑对话框
    openEditDialog() {
      // 初始化编辑表单数据
      this.editForm = {
        realName: this.currentUser.realName || '',
        phone: this.currentUser.phone || '',
        wechat: this.currentUser.wechat || ''
      }
      // 显示对话框
      this.dialogVisible = true
    },

    // 切换密码显示状态
    togglePassword() {
      this.showPassword = !this.showPassword
    },

    // 打开修改密码对话框
    openPasswordDialog() {
      this.passwordForm = {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
      this.passwordDialogVisible = true
    },

    // 保存密码修改
    async savePassword() {
      if (!this.passwordForm.oldPassword) {
        this.$message.error('请输入原密码')
        return
      }
      if (!this.passwordForm.newPassword) {
        this.$message.error('请输入新密码')
        return
      }
      if (!this.passwordForm.confirmPassword) {
        this.$message.error('请确认新密码')
        return
      }
      if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
        this.$message.error('两次输入的密码不一致')
        return
      }

      this.loading = true
      try {
        const response = await api.user.updatePassword(
          this.currentUser.id,
          this.passwordForm.oldPassword,
          this.passwordForm.newPassword
        )
        if (response.code === 200) {
          this.$message.success('密码修改成功，请重新登录')
          this.passwordDialogVisible = false
          setTimeout(() => {
            localStorage.removeItem('token')
            localStorage.removeItem('user')
            this.$router.push('/login')
          }, 1500)
        } else {
          this.$message.error(response.message || '密码修改失败')
        }
      } catch (error) {
        console.error('修改密码失败', error)
        if (error.response && error.response.data && error.response.data.message) {
          this.$message.error(error.response.data.message)
        } else {
          this.$message.error('修改密码失败，请稍后重试')
        }
      } finally {
        this.loading = false
      }
    },

    // 保存修改
    async saveChanges() {
      this.loading = true
      try {
        // 构建更新数据
        const updateData = {
          id: this.currentUser.id,
          realName: this.editForm.realName,
          phone: this.editForm.phone,
          wechat: this.editForm.wechat
        }
        
        // 调用后端API更新用户信息
        const response = await api.user.update(updateData)
        if (response.code === 200) {
          // 检查是否修改了手机号
          const phoneChanged = this.currentUser.phone !== this.editForm.phone
          
          // 更新当前用户信息
          this.currentUser = response.data
          // 更新localStorage中的用户信息
          localStorage.setItem('user', JSON.stringify(this.currentUser))
          // 关闭对话框
          this.dialogVisible = false
          
          if (phoneChanged) {
            // 如果修改了手机号，退出登录并跳转到登录页
            this.$message.success('个人信息更新成功，请使用新手机号重新登录')
            setTimeout(() => {
              localStorage.removeItem('token')
              localStorage.removeItem('user')
              this.$router.push('/login')
            }, 1500)
          } else {
            // 如果没有修改手机号，直接显示成功提示
            this.$message.success('个人信息更新成功')
          }
        } else {
          // 后端返回错误
          this.$message.error(response.message || '更新失败')
        }
      } catch (error) {
        console.error('更新用户信息失败', error)
        // 显示错误提示
        if (error.response && error.response.data && error.response.data.message) {
          this.$message.error(error.response.data.message)
        } else {
          this.$message.error('更新失败，请稍后重试')
        }
      } finally {
        this.loading = false
      }
    }
  }
}