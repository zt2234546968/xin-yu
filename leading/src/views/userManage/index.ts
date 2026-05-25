/**
 * 用户管理模块
 * @description 处理用户列表的展示、查询等功能
 */

import api from '../../api'

export default {
  name: 'UserManageView',

  data() {
    return {
      // 用户列表数据
      userList: [],
      // 表格加载状态
      userLoading: false
    }
  },

  mounted() {
    // 组件挂载时获取用户列表
    this.fetchUserList()
  },

  methods: {
    /**
     * 获取用户列表
     * 调用接口: GET /api/user/list - 获取所有用户列表
     * 接口参数: 无
     * 返回数据: { code: 状态码, data: [用户对象数组] }
     * 用户对象结构: {
     *   id: 用户ID,
     *   realName: 姓名,
     *   phone: 手机号,
     *   wechat: 微信号,
     *   role: 角色对象 { id, roleName, roleCode },
     *   createTime: 注册时间
     * }
     */
    async fetchUserList() {
      this.userLoading = true
      try {
        const res = await api.user.list()
        if (res.code === 200) {
          this.userList = res.data || []
        } else {
          this.$message.error(res.message || '获取用户列表失败')
        }
      } catch (error) {
        console.error('获取用户列表错误:', error)
        this.$message.error('获取用户列表失败')
      } finally {
        this.userLoading = false
      }
    },

    /**
     * 格式化日期时间
     * @param {String} dateStr - ISO格式的日期字符串
     * @returns {String} 格式化后的日期时间字符串，格式：YYYY/MM/DD HH:mm:ss
     */
    formatDate(dateStr) {
      if (!dateStr) return '-'
      const date = new Date(dateStr)
      return date.toLocaleString()
    }
  }
}