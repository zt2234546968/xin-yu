/**
 * 直评任务管理模块
 * @description 管理直评任务的增删改查
 */

import api from '@/api'

export default {
  name: 'ZhipingView',

  data() {
    return {
      // 原始直评任务列表
      allZhipingList: [],
      // 显示的直评任务列表
      zhipingList: [],
      // 国家列表
      countryList: [],
      // 搜索关键词
      searchKeyword: '',
      // 详情侧边栏显示状态
      drawerVisible: false,
      // 创建任务对话框显示状态
      createDialogVisible: false,
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
      // 创建任务表单
      createForm: {
        countryId: '',
        country: {
          id: '',
          countryName: '',
          flagImage: ''
        },
        asin: '',
        taskImage: '/test.jpg',
        reviewTitle: '',
        reviewContent: '',
        starRating: '',
        warrantyTime: '0',
        feedbackLink: '',
        feedbackImage: '',
        status: '0'
      },
      // 加载状态
      loading: false,
      // 悬停星星数量
      hoverRating: 0,
      // 状态更新中
      statusUpdating: false,
      // 当前登录用户信息
      currentUser: {},
      // 反馈渠道对话框
      feedbackChannelDialogVisible: false,
      feedbackChannel: '',
      pendingStatus: '',
      // 完成状态对话框
      completeDialogVisible: false,
      completeFeedbackLink: '',
      completeFeedbackImage: '',
      // 编辑反馈信息对话框
      editFeedbackDialogVisible: false,
      editFeedbackLink: '',
      editFeedbackImage: '',
      // 编辑渠道对话框
      editChannelDialogVisible: false,
      editChannel: '',
      // 编辑质保时间对话框
      editWarrantyDialogVisible: false,
      editWarrantyTime: '',
      // 临时存储选择的图片路径
      selectedImageFile: null
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
    this.fetchCountryList()
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

    // 获取直评任务列表
    async fetchZhipingList() {
      this.loading = true
      try {
        const response = await api.zhiping.list()
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
        console.error('获取直评任务列表失败', error)
        this.$message.error('获取直评任务列表失败')
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

    // 获取国家列表
    async fetchCountryList() {
      try {
        const response = await api.country.list()
        if (response.code === 200) {
          this.countryList = response.data
        }
      } catch (error) {
        console.error('获取国家列表失败', error)
        this.$message.error('获取国家列表失败')
      }
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

    // 删除直评任务
    async deleteZhiping(id) {
      this.$confirm('确定要删除这个直评任务吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        this.loading = true
        try {
          const response = await api.zhiping.delete(id)
          if (response.code === 200) {
            this.$message.success('删除成功')
            this.fetchZhipingList()
          } else {
            this.$message.error(response.message || '删除失败')
          }
        } catch (error) {
          console.error('删除失败', error)
          this.$message.error('删除失败，请稍后重试')
        } finally {
          this.loading = false
        }
      }).catch(() => {
        // 取消删除
      })
    },

    // 打开创建对话框
    openCreateDialog() {
      this.createForm = {
        countryId: '',
        country: {
          id: '',
          countryName: '',
          flagImage: ''
        },
        asin: '',
        taskImage: '/test.jpg',
        reviewTitle: '',
        reviewContent: '',
        starRating: 5,
        warrantyTime: '0',
        feedbackLink: '',
        feedbackImage: '',
        status: '0'
      }
      this.createDialogVisible = true
    },

    // 创建直评任务
    async createZhiping() {
      this.loading = true
      try {
        const response = await api.zhiping.create(this.createForm)
        if (response.code === 200) {
          this.createDialogVisible = false
          this.$message.success('创建成功')
          this.fetchZhipingList()
        } else {
          this.$message.error(response.message || '创建失败')
        }
      } catch (error) {
        console.error('创建直评任务失败', error)
        this.$message.error('创建失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },

    // 根据国家名获取国旗图片URL
    getCountryFlag(countryName) {
      const country = this.countryList.find(item => item.countryName === countryName)
      return country ? country.flagImage : ''
    },

    // 处理国家选择变化
    handleCountryChange(countryId) {
      const country = this.countryList.find(item => item.id === countryId)
      if (country) {
        this.createForm.country = country
      }
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

    // 确认反馈渠道并更新状态
    async confirmFeedbackChannel() {
      console.log('========== confirmFeedbackChannel ==========')
      console.log('feedbackChannel:', this.feedbackChannel)
      console.log('==========================================')
      
      if (!this.feedbackChannel || !this.feedbackChannel.trim()) {
        this.$message.error('请输入渠道')
        return
      }

      this.feedbackChannelDialogVisible = false
      this.statusUpdating = true
      try {
        // 更新渠道信息，保留已有的反馈信息
        const feedbackResponse = await api.zhiping.updateFeedback(
          this.currentZhiping.id, 
          this.currentZhiping.feedbackLink, 
          this.currentZhiping.feedbackImage, 
          this.feedbackChannel.trim()
        )

        if (feedbackResponse.code === 200) {
          // 更新状态
          const statusResponse = await api.zhiping.updateStatus(this.currentZhiping.id, this.pendingStatus)
          if (statusResponse.code === 200) {
            this.$message.success('状态更新成功')
            // 更新渠道显示
            this.currentZhiping.channel = this.feedbackChannel.trim()
            this.currentZhiping.status = this.pendingStatus
            this.originalStatus = this.pendingStatus
            this.fetchZhipingList()
          } else {
            this.$message.error(statusResponse.message || '状态更新失败')
            // 恢复状态选择器
            this.statusSelectValue = this.originalStatus
          }
        } else {
          this.$message.error(feedbackResponse.message || '渠道更新失败')
          // 恢复状态选择器
          this.statusSelectValue = this.originalStatus
        }
      } catch (error) {
        console.error('更新失败', error)
        this.$message.error('更新失败，请稍后重试')
        // 恢复状态选择器
        this.statusSelectValue = this.originalStatus
      } finally {
        this.statusUpdating = false
      }
    },

    // 取消反馈渠道输入
    cancelFeedbackChannel() {
      this.feedbackChannelDialogVisible = false
      this.feedbackChannel = ''
      this.pendingStatus = ''
      // 恢复状态选择器
      this.statusSelectValue = this.originalStatus
    },

    // 确认完成状态并更新信息
    async confirmCompleteStatus() {
      if (!this.completeFeedbackLink || !this.completeFeedbackLink.trim()) {
        this.$message.error('请输入反馈链接')
        return
      }

      this.completeDialogVisible = false
      this.statusUpdating = true
      try {
        // 先更新反馈信息
        const feedbackResponse = await api.zhiping.updateFeedback(this.currentZhiping.id, this.completeFeedbackLink.trim(), this.completeFeedbackImage || '')

        if (feedbackResponse.code === 200) {
          // 再更新状态
          const statusResponse = await api.zhiping.updateStatus(this.currentZhiping.id, this.pendingStatus)
          if (statusResponse.code === 200) {
            this.$message.success('状态更新成功')
            // 更新本地数据
            this.currentZhiping.feedbackLink = this.completeFeedbackLink.trim()
            this.currentZhiping.feedbackImage = this.completeFeedbackImage || ''
            this.currentZhiping.status = this.pendingStatus
            this.originalStatus = this.pendingStatus
            // 刷新列表
            this.fetchZhipingList()
          } else {
            this.$message.error(statusResponse.message || '状态更新失败')
            // 恢复状态选择器
            this.statusSelectValue = this.originalStatus
          }
        } else {
          this.$message.error(feedbackResponse.message || '反馈信息更新失败')
          // 恢复状态选择器
          this.statusSelectValue = this.originalStatus
        }
      } catch (error) {
        console.error('更新失败', error)
        this.$message.error('更新失败，请稍后重试')
        // 恢复状态选择器
        this.statusSelectValue = this.originalStatus
      } finally {
        this.statusUpdating = false
      }
    },

    // 取消完成状态输入
    cancelCompleteStatus() {
      this.completeDialogVisible = false
      this.completeFeedbackLink = ''
      this.completeFeedbackImage = ''
      this.pendingStatus = ''
      // 恢复状态选择器
      this.statusSelectValue = this.originalStatus
    },

    // 处理图片选择
    handleImageSelect(event) {
      const file = event.target.files[0]
      if (file) {
        // 创建本地文件URL
        const imageUrl = '/test.jpg'
        this.completeFeedbackImage = imageUrl
      }
    },

    // 打开编辑反馈信息对话框
    openEditFeedbackDialog() {
      this.editFeedbackLink = this.currentZhiping.feedbackLink || ''
      this.editFeedbackImage = this.currentZhiping.feedbackImage || ''
      this.editFeedbackDialogVisible = true
    },

    // 处理编辑图片选择
    handleEditImageSelect(event) {
      const file = event.target.files[0]
      if (file) {
        // 创建本地文件URL
        const imageUrl = '/test.jpg'
        this.editFeedbackImage = imageUrl
      }
    },

    // 确认编辑反馈信息
    async confirmEditFeedback() {
      this.editFeedbackDialogVisible = false
      this.loading = true
      try {
        const response = await api.zhiping.updateFeedback(
          this.currentZhiping.id,
          this.editFeedbackLink.trim(),
          this.editFeedbackImage || ''
        )
        if (response.code === 200) {
          this.$message.success('修改成功')
          // 更新本地数据
          this.currentZhiping.feedbackLink = this.editFeedbackLink.trim()
          this.currentZhiping.feedbackImage = this.editFeedbackImage || ''
          this.fetchZhipingList()
        } else {
          this.$message.error(response.message || '修改失败')
        }
      } catch (error) {
        console.error('修改失败', error)
        this.$message.error('修改失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },

    // 取消编辑反馈信息
    cancelEditFeedback() {
      this.editFeedbackDialogVisible = false
      this.editFeedbackLink = ''
      this.editFeedbackImage = ''
    },

    // 打开编辑渠道对话框
    openEditChannelDialog() {
      this.editChannel = this.currentZhiping.channel || ''
      this.editChannelDialogVisible = true
    },

    // 确认编辑渠道
    async confirmEditChannel() {
      if (!this.editChannel || !this.editChannel.trim()) {
        this.$message.error('请输入渠道')
        return
      }

      this.editChannelDialogVisible = false
      this.loading = true
      try {
        // 更新渠道信息，保留已有的反馈信息
        const response = await api.zhiping.updateFeedback(
          this.currentZhiping.id, 
          this.currentZhiping.feedbackLink, 
          this.currentZhiping.feedbackImage, 
          this.editChannel.trim()
        )
        if (response.code === 200) {
          this.$message.success('修改成功')
          // 更新本地数据
          this.currentZhiping.channel = this.editChannel.trim()
          this.fetchZhipingList()
        } else {
          this.$message.error(response.message || '修改失败')
        }
      } catch (error) {
        console.error('修改失败', error)
        this.$message.error('修改失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },

    // 取消编辑渠道
    cancelEditChannel() {
      this.editChannelDialogVisible = false
      this.editChannel = ''
    },

    // 打开编辑质保时间对话框
    openEditWarrantyDialog() {
      this.editWarrantyTime = this.currentZhiping.warrantyTime || ''
      this.editWarrantyDialogVisible = true
    },

    // 取消编辑质保时间
    cancelEditWarranty() {
      this.editWarrantyDialogVisible = false
      this.editWarrantyTime = ''
    },

    // 确认编辑质保时间
    async confirmEditWarranty() {
      if (!this.editWarrantyTime && this.editWarrantyTime !== '0') {
        this.$message.error('请选择质保时间')
        return
      }

      try {
        // 调用通用更新接口，只更新质保时间
        const response = await api.zhiping.update(this.currentZhiping.id, {
          warrantyTime: this.editWarrantyTime
        })

        if (response.code === 200) {
          this.$message.success('质保时间更新成功')
          this.currentZhiping.warrantyTime = this.editWarrantyTime
          this.editWarrantyDialogVisible = false
          // 刷新列表
          this.fetchZhipingList()
        } else {
          this.$message.error(response.message || '更新失败')
        }
      } catch (error) {
        console.error('更新质保时间失败', error)
        this.$message.error('更新失败，请稍后重试')
      }
    }
  }
}

