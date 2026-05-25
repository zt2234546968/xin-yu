/**
 * 邀请码管理模块
 * @description 处理邀请码的列表展示、生成、备注编辑等功能
 */

import api from '../../api'

export default {
  name: 'InvitationCodeView',

  data() {
    return {
      // 邀请码列表数据
      invitationCodeList: [],
      // 表格加载状态
      loading: false
    }
  },

  mounted() {
    // 组件挂载时获取邀请码列表
    this.fetchInvitationCodeList()
  },

  methods: {
    /**
     * 获取邀请码列表
     * 调用接口: GET /api/invitationCode/list - 获取所有邀请码列表
     * 接口参数: 无
     * 返回数据: { code: 状态码, data: [邀请码对象数组] }
     * 邀请码对象结构: {
     *   id: 邀请码ID,
     *   code: 邀请码内容,
     *   used: 使用状态(0-未使用, 1-已使用),
     *   remark: 公司备注,
     *   createTime: 创建时间
     * }
     */
    async fetchInvitationCodeList() {
      this.loading = true
      try {
        const res = await api.invitationCode.list()
        if (res.code === 200) {
          this.invitationCodeList = res.data || []
        } else {
          this.$message.error(res.message || '获取邀请码列表失败')
        }
      } catch (error) {
        console.error('获取邀请码列表错误:', error)
        this.$message.error('获取邀请码列表失败')
      } finally {
        this.loading = false
      }
    },

    /**
     * 生成新的邀请码
     * 调用接口: POST /api/invitationCode/generate - 生成新邀请码
     * 接口参数: 无
     * 返回数据: { code: 状态码, data: 新生成的邀请码对象 }
     */
    async handleGenerate() {
      try {
        const res = await api.invitationCode.generate()
        if (res.code === 200) {
          this.$message.success('生成成功')
          // 生成成功后刷新列表
          this.fetchInvitationCodeList()
        } else {
          this.$message.error(res.message || '生成失败')
        }
      } catch (error) {
        this.$message.error('生成邀请码失败')
      }
    },

    /**
     * 更新邀请码备注
     * 调用接口: POST /api/invitationCode/updateRemark - 更新邀请码备注
     * 接口参数: { id: 邀请码ID, remark: 备注内容 }
     * 返回数据: { code: 状态码, data: 更新后的邀请码对象 }
     * @param {Object} row - 邀请码行数据对象
     */
    async handleRemarkChange(row) {
      try {
        const res = await api.invitationCode.updateRemark(row.id, row.remark)
        if (res.code === 200) {
          // 更新成功，无需额外操作
        } else {
          this.$message.error(res.message || '更新备注失败')
          // 更新失败时刷新列表以恢复原数据
          this.fetchInvitationCodeList()
        }
      } catch (error) {
        this.$message.error('更新备注失败')
        // 更新失败时刷新列表以恢复原数据
        this.fetchInvitationCodeList()
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