import api from '@/api'

export default {
  name: 'PendingOrderView',
  data() {
    return {
      // 原始直评任务列表
      allZhipingList: [],
      // 显示的直评任务列表
      zhipingList: [],
      // 搜索关键词
      searchKeyword: '',
      // 详情侧边栏显示状态
      drawerVisible: false,
      // 当前查看的直评任务
      currentZhiping: {
        id: '',
        code: '',
        asin: '',
        reviewTitle: '',
        reviewContent: '',
        taskImage: '',
        starRating: '',
        country: '',
        countryImage: '',
        channel: '',
        status: '',
        warrantyTime: '',
        feedbackLink: '',
        feedbackImage: '',
        createTime: '',
        updateTime: ''
      },
      // 临时保存的原始状态（用于取消时恢复）
      originalStatus: '',
      // 状态选择器的临时值
      statusSelectValue: '',
      // 加载状态
      loading: false,
      // 状态更新中
      statusUpdating: false,
      // 当前登录用户信息
      currentUser: {}
    }
  },

  computed: {
    // 是否是超级管理员
    isSuperAdmin() {
      return this.currentUser.role && this.currentUser.role.roleName === 'SUPER_ADMIN'
    }
  },

  mounted() {
    this.fetchZhipingList()
    this.loadCurrentUser()
  },

  methods: {
    // 加载当前登录用户信息
    loadCurrentUser() {
      const userStr = localStorage.getItem('user')
      if (userStr) {
        try {
          this.currentUser = JSON.parse(userStr)
        } catch (e) {
          console.error('解析用户信息失败', e)
        }
      }
    },

    // 格式化时间
    formatDateTime(dateTime) {
      if (!dateTime) return ''
      const date = new Date(dateTime)
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      const seconds = String(date.getSeconds()).padStart(2, '0')
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
    },

    // 获取状态文本
    getStatusText(status) {
      const statusMap = {
        '0': '等待提交',
        '1': '等待反馈',
        '2': '已完成',
        '3': '已取消'
      }
      return statusMap[status] || status
    },

    // 获取质保时间文本
    getWarrantyTimeText(warrantyTime) {
      const warrantyMap = {
        '0': '7天',
        '1': '30天'
      }
      return warrantyMap[warrantyTime] || warrantyTime
    },

    // 刷新列表
    refreshList() {
      this.searchKeyword = ''
      this.fetchZhipingList()
    },

    // 获取订单列表
    async fetchZhipingList() {
      this.loading = true
      try {
        const response = await api.orderList.list()
        if (response.code === 200) {
          // 格式化时间字段，并处理country对象
          this.allZhipingList = response.data.map(item => ({
            ...item,
            country: item.country?.countryName || item.country || '',
            countryImage: item.country?.flagImage || item.countryImage || '',
            channel: item.channel || '',
            createTime: this.formatDateTime(item.createTime),
            updateTime: this.formatDateTime(item.updateTime)
          }))
          // 如果有搜索关键词，先过滤
          this.handleSearch()
        }
      } catch (error) {
        console.error('获取订单列表失败', error)
        this.$message.error('获取订单列表失败')
      } finally {
        this.loading = false
      }
    },

    // 搜索功能
    handleSearch() {
      if (!this.searchKeyword || !this.searchKeyword.trim()) {
        // 没有搜索关键词，显示全部
        this.zhipingList = [...this.allZhipingList]
        return
      }

      const keyword = this.searchKeyword.trim().toLowerCase()
      // 根据任务编号（code字段）模糊搜索
      this.zhipingList = this.allZhipingList.filter(item => {
        return item.code && item.code.toLowerCase().includes(keyword)
      })
    },

    // 打开详情侧边栏
    openDetailSidebar(row) {
      const countryName = typeof row.country === 'object' ? row.country.countryName : row.country
      const flagImage = typeof row.country === 'object' ? row.country.flagImage : row.countryImage
      this.currentZhiping = {
        id: row.id || '',
        code: row.code || '',
        asin: row.asin || '',
        reviewTitle: row.reviewTitle || '',
        reviewContent: row.reviewContent || '',
        taskImage: row.taskImage || '',
        starRating: row.starRating || '',
        country: countryName || '',
        countryImage: flagImage || '',
        channel: row.channel || '',
        status: row.status || '',
        warrantyTime: row.warrantyTime || '',
        feedbackLink: row.feedbackLink || '',
        feedbackImage: row.feedbackImage || '',
        createTime: this.formatDateTime(row.createTime) || '',
        updateTime: this.formatDateTime(row.updateTime) || ''
      }
      // 保存原始状态并初始化选择器
      this.originalStatus = row.status || ''
      this.statusSelectValue = row.status || ''
      this.drawerVisible = true
    },
    // 处理状态变化
    async handleStatusChange(newStatus) {
      if (!this.currentZhiping.id) return

      // 如果切换到等待反馈状态，需要先输入反馈渠道
      if (newStatus === '1') {
        this.pendingStatus = newStatus
        this.feedbackChannel = this.currentZhiping.channel || ''
        this.feedbackChannelDialogVisible = true
        return
      }

      // 如果切换到已完成状态，需要输入反馈信息
      if (newStatus === '2') {
        this.pendingStatus = newStatus
        this.completeFeedbackLink = this.currentZhiping.feedbackLink || ''
        this.completeFeedbackImage = this.currentZhiping.feedbackImage || ''
        this.completeDialogVisible = true
        return
      }

      // 其他状态直接更新
      await this.doUpdateStatus(newStatus)
    },

    // 执行状态更新
    async doUpdateStatus(newStatus) {
      this.statusUpdating = true
      try {
        const response = await api.zhiping.updateStatus(this.currentZhiping.id, newStatus)
        if (response.code === 200) {
          this.$message.success('状态更新成功')
          // 更新本地状态和选择器值
          this.currentZhiping.status = newStatus
          this.originalStatus = newStatus
          // 刷新列表
          this.fetchZhipingList()
        } else {
          this.$message.error(response.message || '状态更新失败')
          // 恢复状态选择器
          this.statusSelectValue = this.originalStatus
        }
      } catch (error) {
        console.error('更新状态失败', error)
        this.$message.error('状态更新失败，请稍后重试')
        // 恢复状态选择器
        this.statusSelectValue = this.originalStatus
      } finally {
        this.statusUpdating = false
      }
    },
  }
}
