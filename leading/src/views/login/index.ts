/**
 * 登录模块 - 用户登录和注册
 * @description 处理用户登录、注册、表单验证等功能
 */

import api from '../../api'

export default {
  name: 'LoginView',

  data() {
    return {
      // 当前激活的标签页：login(登录) / register(注册)
      activeTab: 'login',

      // 登录表单数据
      loginForm: {
        phone: '',    // 手机号
        password: ''  // 密码
      },

      // 注册表单数据
      registerForm: {
        realName: '',       // 姓名
        phone: '',          // 手机号
        wechat: '',         // 微信号
        password: '',       // 密码
        confirmPassword: '', // 确认密码
        inviteCode: ''      // 邀请码
      },

      // 登录表单验证规则
      loginRules: {
        phone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码长度至少6位', trigger: 'blur' }
        ]
      },

      // 注册表单验证规则
      registerRules: {
        realName: [
          { required: true, message: '请输入姓名', trigger: 'blur' }
        ],
        phone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
        ],
        wechat: [
          { required: true, message: '请输入微信号', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码长度至少6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入密码', trigger: 'blur' },
          { validator: this.validateConfirmPassword, trigger: 'blur' }
        ],
        inviteCode: [
          { required: true, message: '请输入邀请码', trigger: 'blur' }
        ]
      }
    }
  },

  methods: {
    /**
     * 验证确认密码是否与密码一致
     * @param {Object} rule - 验证规则
     * @param {String} value - 输入的值
     * @param {Function} callback - 回调函数
     */
    validateConfirmPassword(rule, value, callback) {
      if (value !== this.registerForm.password) {
        callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    },

    /**
     * 处理用户登录
     * 调用接口: POST /api/user/login - 用户登录接口
     * 接口参数: { phone: 手机号, password: 密码 }
     * 返回数据: { code: 状态码, data: { token: 令牌, user: 用户信息 } }
     */
    async handleLogin() {
      this.$refs.loginForm.validate(async (valid) => {
        if (valid) {
          try {
            // 调用登录接口
            const res = await api.user.login(this.loginForm)
            if (res.code === 200) {
              // 登录成功，保存token和用户信息到localStorage
              localStorage.setItem('token', res.data.token)
              localStorage.setItem('user', JSON.stringify(res.data.user))
              this.$message.success('登录成功')
              // 跳转到首页
              this.$router.push('/home')
            } else {
              this.$message.error(res.message || '登录失败')
            }
          } catch (error) {
            this.$message.error('登录失败')
          }
        }
      })
    },

    /**
     * 处理用户注册
     * 调用接口: POST /api/user/register - 用户注册接口
     * 接口参数: {
     *   realName: 姓名,
     *   phone: 手机号,
     *   wechat: 微信号,
     *   password: 密码,
     *   inviteCode: 邀请码
     * }
     * 返回数据: { code: 状态码, message: 消息 }
     */
    async handleRegister() {
      this.$refs.registerForm.validate(async (valid) => {
        if (valid) {
          try {
            // 调用注册接口
            const res = await api.user.register(this.registerForm)
            if (res.code === 200) {
              this.$message.success('注册成功')
              // 注册成功后切换到登录标签页
              this.activeTab = 'login'
            } else {
              this.$message.error(res.message || '注册失败')
            }
          } catch (error) {
            this.$message.error('注册失败')
          }
        }
      })
    }
  }
}