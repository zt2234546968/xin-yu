/**
 * 测评任务管理模块
 * @description 管理测评任务的增删改查
 */

import api from '@/api'

export default {
  name: 'CepingView',

  data() {
    return {
      // 原始测评任务列表
      allCepingList: [],
      cepingList: [],
      // 国家列表
      countryList: [],
      searchKeyword: '',
      drawerVisible: false,
      createDialogVisible: false,
      currentCeping: {
        id: '',
        code: '',
        productName: '',
        productImage: '',
        productLink: '',
        asin: '',
        country: '',
        reviewTitle: '',
        reviewContent: '',
        shop: '',
        freeReview: 0,
        starReview: 0,
        textReview: 0,
        imageReview: 0,
        videoReview: 0,
        feedbackReview: 0,
        totalQuantity: 0,
        isPositive: true,
        price: null,
        status: '',
        orderNumber: '',
        orderScreenshot: '',
        reviewLink: '',
        reviewScreenshot: '',
        budget: null,
        adminBudget: null,
        userBudget: null,
        adminMessage: false,
        userMessage: false,
        remark: '',
        createTime: '',
        updateTime: ''
      },
      // 临时保存的原始状态（用于取消时恢复）
      originalStatus: '',
      statusSelectValue: '',
      // 订单弹窗相关
      orderDialogVisible: false,
      currentOrderList: [],
      pendingOrderStatus: null,
      orderCompletedDialogVisible: false,
      orderCompletedReviewLink: '',
      orderCompletedReviewScreenshot: '',
      // 状态更新中
      statusUpdating: false,
      pendingStatus: '',
      // 已放单状态对话框
      releasedDialogVisible: false,
      releasedOrderNumber: '',
      releasedOrderScreenshot: '',
      releasedOrdersList: [],
      // 已完成状态对话框
      completedDialogVisible: false,
      completedReviewLink: '',
      completedReviewScreenshot: '',
      budgetRequestDialogVisible: false,
      budgetRequestAmount: null,
      budgetRequestRemark: '',
      budgetEditDialogVisible: false,
      budgetEditRow: null,
      budgetEditAmount: null,
      // 创建任务表单
      createForm: {
        countryId: '',
        country: {
          id: '',
          countryName: '',
          flagImage: ''
        },
        productName: '',
        productImage: '/test.jpg',
        productLink: '',
        asin: '',
        keyword: '',
        reviewTitle: '',
        reviewContent: '',
        isPositive: true,
        shop: '',
        freeReview: 0,
        starReview: 0,
        textReview: 0,
        imageReview: 0,
        videoReview: 0,
        feedbackReview: 0,
        price: null,
        status: 0,
        remark: ''
      },
      loading: false,
      // 当前登录用户信息
      currentUser: {}
    }
  },
  
  computed: {
    // 是否是超级管理员
    isSuperAdmin() {
      return this.currentUser.role && this.currentUser.role.roleName === 'SUPER_ADMIN'
    },
    // 计算总量
    calculateTotalQuantity() {
      const free = this.createForm.freeReview || 0;
      const star = this.createForm.starReview || 0;
      const text = this.createForm.textReview || 0;
      const image = this.createForm.imageReview || 0;
      const video = this.createForm.videoReview || 0;
      const feedback = this.createForm.feedbackReview || 0;
      return free + star + text + image + video + feedback;
    }
  },

  mounted() {
    this.fetchCepingList()
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

    getStatusText(status) {
      const statusMap = {
        '0': '放单中',
        '1': '已放单'
      }
      return statusMap[String(status)] || status || '--'
    },

    getBudgetStatus(row) {
      const adminBudget = parseFloat(row.adminBudget)
      const userBudget = parseFloat(row.userBudget)

      if (isNaN(adminBudget)) {
        return 'initial'
      }

      if (userBudget === 0) {
        return 'waiting'
      }

      if (userBudget === 1) {
        return 'rejected'
      }

      if (userBudget === adminBudget) {
        return 'agreed'
      }

      return 'initial'
    },

    // 打开订单列表弹窗
    openOrderDialog(row) {
      api.orderList.getByCepingId(row.id).then(response => {
        if (response.code === 200) {
          this.currentOrderList = (response.data || []).map(order => ({
            ...order,
            exchangeRate: order.ppMultiplier
          }))
          this.orderDialogVisible = true
        } else {
          this.$message.error(response.message || '获取订单列表失败')
        }
      }).catch(error => {
        console.error('获取订单列表失败', error)
        this.$message.error('获取订单列表失败，请稍后重试')
      })
    },

    calculatePpPrice(row) {
      const exchangeRate = row.ppMultiplier != null ? row.ppMultiplier : row.exchangeRate
      if (row.principal != null && exchangeRate != null) {
        const ppPrice = row.principal * exchangeRate + row.principal
        return ppPrice.toFixed(2)
      }
      return '--'
    },

    // 格式化汇率显示（数字 -> 百分比）
    formatExchangeRate(value) {
      if (value == null) return '--'
      const percent = parseFloat(value) * 100
      return percent.toFixed(2) + '%'
    },

    parseExchangeRate(input) {
      if (!input) return null
      const cleaned = input.replace('%', '').trim()
      const num = parseFloat(cleaned)
      if (isNaN(num)) return null
      return num / 100
    },

    // 获取费用明细文本
    getExpenseDetailText(row) {
      const parts = []
      const ppMultiplier = row.ppMultiplier != null ? row.ppMultiplier : row.exchangeRate
      const exchangeRate = row.exchangeRate != null ? row.exchangeRate : row.ppMultiplier
      
      if (row.principal != null && ppMultiplier != null) {
        const ppPrice = row.principal * ppMultiplier + row.principal
        const exchangeRatePercent = (ppMultiplier * 100).toFixed(2) + '%'
        parts.push(`PP后价格? ${row.principal}*${exchangeRatePercent}+${row.principal}=${ppPrice.toFixed(2)}`)
      }
      if (row.ppPrice != null && exchangeRate != null && row.commission != null) {
        const sum = row.ppPrice * exchangeRate + row.commission
        const exchangeRatePercent = (exchangeRate * 100).toFixed(2) + '%'
        parts.push(`总金额? ${row.ppPrice}*${exchangeRatePercent}+${row.commission}=${sum.toFixed(2)}`)
      } else if (row.ppPrice != null && exchangeRate != null) {
        const sumPart1 = row.ppPrice * exchangeRate
        const exchangeRatePercent = (exchangeRate * 100).toFixed(2) + '%'
        parts.push(`总金额? ${row.ppPrice}*${exchangeRatePercent}+佣金=? (佣金为空)`)
      }
      return parts.length > 0 ? parts.join(', ') : '--'
    },

    getExpenseDetailParts(row) {
      const parts: Record<string, string> = {}
      const ppMultiplier = row.ppMultiplier != null ? row.ppMultiplier : row.exchangeRate
      const exchangeRate = row.exchangeRate != null ? row.exchangeRate : row.ppMultiplier
      
      if (row.principal != null && ppMultiplier != null) {
        const ppPrice = row.principal * ppMultiplier + row.principal
        const exchangeRatePercent = (ppMultiplier * 100).toFixed(2) + '%'
        parts.ppPrice = `PP后价格? ${row.principal}*${exchangeRatePercent}+${row.principal}=${ppPrice.toFixed(2)}`
      }
      if (row.ppPrice != null && exchangeRate != null && row.commission != null) {
        const sum = row.ppPrice * exchangeRate + row.commission
        const exchangeRatePercent = (exchangeRate * 100).toFixed(2) + '%'
        parts.sum = `总金额? ${row.ppPrice}*${exchangeRatePercent}+${row.commission}=${sum.toFixed(2)}`
      } else if (row.ppPrice != null && exchangeRate != null) {
        const exchangeRatePercent = (exchangeRate * 100).toFixed(2) + '%'
        parts.sum = `总金额? ${row.ppPrice}*${exchangeRatePercent}+佣金=? (佣金为空)`
      }
      return parts
    },

    // 处理订单字段修改
    handleOrderFieldChange(row, field, value) {
      console.log('原始值?', value, '字段:', field)

      let processedValue = value
      let backendField = field

      if (field === 'exchangeRate') {
        processedValue = this.parseExchangeRate(value)
        backendField = 'ppMultiplier'
      } else if (field === 'principal' || field === 'commission') {
        processedValue = parseFloat(value) || null
      }

      console.log('处理值?', processedValue, '后端字段:', backendField)

      api.orderList.getById(row.id).then(response => {
        if (response.code !== 200 || !response.data) {
          this.$message.error('获取订单详情失败')
          return
        }

        const fullOrder = response.data
        
        if (backendField === 'ppMultiplier') {
          fullOrder.ppMultiplier = processedValue
        } else {
          fullOrder[backendField] = processedValue
        }

        if (field === 'principal' || field === 'exchangeRate') {
          const principal = field === 'principal' ? processedValue : fullOrder.principal
          const ppMultiplier = field === 'exchangeRate' ? processedValue : fullOrder.ppMultiplier
          if (principal != null && ppMultiplier != null) {
            const ppPrice = principal * ppMultiplier + principal
            fullOrder.ppPrice = ppPrice
            row.ppPrice = ppPrice
          }
        }

        console.log('发送完整数据?', fullOrder)

        return api.orderList.update(row.id, fullOrder)
      }).then(response => {
        if (response && response.code === 200) {
          row[field] = processedValue
          if (response.data && response.data.ppPrice != null) {
            row.ppPrice = response.data.ppPrice
          }
          if (response.data && response.data.sum != null) {
            row.sum = response.data.sum
          }
          if (response.data && response.data.ppMultiplier != null) {
            row.ppMultiplier = response.data.ppMultiplier
            row.exchangeRate = response.data.ppMultiplier
          }
          this.$message.success('更新成功')
        } else if (response) {
          this.$message.error(response.message || '更新失败')
        }
      }).catch(error => {
        console.error('更新订单字段失败', error)
        this.$message.error('更新失败，请稍后重试')
      })
    },

    // 获取测评任务列表
    async fetchCepingList() {
      try {
        const response = await api.ceping.list()
        if (response.code === 200) {
          // 格式化时间字段，并处理country对象
          this.allCepingList = response.data.map(item => ({
            ...item,
            country: item.country?.countryName || item.country || '',
            countryImage: item.country?.flagImage || item.countryImage || '',
            createTime: this.formatDateTime(item.createTime),
            updateTime: this.formatDateTime(item.updateTime),
            status: parseInt(item.status) || 0
          }))
          this.cepingList = this.allCepingList
        }
      } catch (error) {
        console.error('获取测评任务列表失败', error)
        this.$message.error('获取测评任务列表失败')
      }
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
      }
    },

    // 搜索处理
    handleSearch() {
      if (!this.searchKeyword || !this.searchKeyword.trim()) {
        this.cepingList = [...this.allCepingList]
        return
      }
      const keyword = this.searchKeyword.trim().toLowerCase()
      this.cepingList = this.allCepingList.filter(item => 
        item.code && item.code.toLowerCase().includes(keyword)
      )
    },

    // 刷新列表
    refreshList() {
      this.fetchCepingList()
      this.searchKeyword = ''
    },

    openDetailSidebar(item) {
      this.currentCeping = { ...item }
      this.originalStatus = item.status != null ? String(item.status) : ''
      this.statusSelectValue = item.status != null ? String(item.status) : ''
      this.drawerVisible = true
    },

    openCreateDialog() {
      this.createForm = {
        countryId: '',
        country: {
          id: '',
          countryName: '',
          flagImage: ''
        },
        productName: '',
        productImage: '/test.jpg',
        productLink: '',
        asin: '',
        keyword: '',
        reviewTitle: '',
        reviewContent: '',
        isPositive: true,
        shop: '',
        freeReview: 0,
        starReview: 0,
        textReview: 0,
        imageReview: 0,
        videoReview: 0,
        feedbackReview: 0,
        price: null,
        status: 0,
        remark: ''
      }
      this.createDialogVisible = true
    },
    // 处理国家选择变化
    handleCountryChange(countryId) {
      const country = this.countryList.find(item => item.id === countryId)
      if (country) {
        this.createForm.country = country
      }
    },

    // 创建测评任务
    async createCeping() {
      if (!this.createForm.productName || !this.createForm.productName.trim()) {
        this.$message.warning('请输入商品名称')
        return
      }
      if (!this.createForm.countryId) {
        this.$message.warning('请选择国家')
        return
      }
      
      if (this.createForm.isPositive) {
        const totalQuantity = this.calculateTotalQuantity
        if (totalQuantity <= 0) {
          this.$message.warning('上评要求6个字段总量必须大于0')
          return
        }
      }

      this.loading = true
      try {
        // 计算总量
        const totalQuantity = this.calculateTotalQuantity;
        
        const submitData = {
          ...this.createForm,
          totalQuantity: totalQuantity
        }
        
        const response = await api.ceping.create(submitData)
        if (response.code === 200) {
          this.$message.success('创建成功')
          this.createDialogVisible = false
          this.fetchCepingList()
        } else {
          this.$message.error(response.message || '创建失败')
        }
      } catch (error) {
        console.error('创建测评任务失败', error)
        this.$message.error('创建失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },

    // 删除测评任务
    async deleteCeping(id) {
      this.$confirm('确定要删除这个任务吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await api.ceping.delete(id)
          if (response.code === 200) {
            this.$message.success('删除成功')
            this.fetchCepingList()
          } else {
            this.$message.error(response.message || '删除失败')
          }
        } catch (error) {
          console.error('删除测评任务失败', error)
          this.$message.error('删除失败，请稍后重试')
        }
      }).catch(() => {})
    },

    async handleStatusChange(newStatus) {
      if (!this.currentCeping.id) return

      if (newStatus === '1') {
        console.log('========== handleStatusChange 打开已放单表单?==========')
        console.log('this.currentCeping:', this.currentCeping)
        console.log('this.currentCeping.id:', this.currentCeping.id)
        console.log('===================================================')
        
        this.pendingStatus = newStatus
        this.releasedOrderNumber = ''
        this.releasedOrderScreenshot = ''
        try {
            const response = await api.orderList.getByCepingId(this.currentCeping.id)
            if (response.code === 200) {
              this.releasedOrdersList = (response.data || []).map(order => ({
                ...order,
                exchangeRate: order.ppMultiplier
              }))
            }
          } catch (error) {
            console.error('获取订单列表失败', error)
            this.releasedOrdersList = []
          }
        this.releasedDialogVisible = true
        return
      }

      // 申请增加预算：可以自定义输入
      if (newStatus === '2') {
        this.pendingStatus = newStatus
        this.budgetRequestAmount = null
        this.budgetRequestRemark = ''
        this.budgetRequestDialogVisible = true
        return
      }

      // 其他状态（放单中）直接更新
      await this.doUpdateStatus(newStatus)
    },

    async doUpdateStatus(newStatus) {
      this.statusUpdating = true
      try {
        const response = await api.ceping.update(this.currentCeping.id, {
          status: newStatus
        })
        if (response.code === 200) {
          this.$message.success('状态更新成功')
          this.currentCeping.status = newStatus
          this.originalStatus = newStatus
          // 刷新列表
          this.fetchCepingList()
        } else {
          this.$message.error(response.message || '状态更新失败')
          this.statusSelectValue = this.originalStatus
        }
      } catch (error) {
        console.error('更新状态失败', error)
        this.$message.error('更新失败，请稍后重试')
        this.statusSelectValue = this.originalStatus
      } finally {
        this.statusUpdating = false
      }
    },

    async confirmReleasedStatus() {
      if (this.releasedOrdersList.length === 0) {
        this.$message.error('请先添加至少一个订单')
        return
      }

      this.releasedDialogVisible = false
      this.statusUpdating = true
      try {
        const updateResponse = await api.ceping.update(this.currentCeping.id, {
          status: this.pendingStatus
        })

        if (updateResponse.code === 200) {
          this.$message.success('状态更新成功')
          this.currentCeping.status = this.pendingStatus
          this.originalStatus = this.pendingStatus
          this.releasedOrdersList = []
          this.fetchCepingList()
        } else {
          this.$message.error(updateResponse.message || '状态更新失败')
          this.statusSelectValue = this.originalStatus
        }
      } catch (error) {
        console.error('更新失败', error)
        this.$message.error('更新失败，请稍后重试')
        this.statusSelectValue = this.originalStatus
      } finally {
        this.statusUpdating = false
      }
    },

    cancelReleasedStatus() {
      this.releasedDialogVisible = false
      this.releasedOrderNumber = ''
      this.releasedOrderScreenshot = ''
      this.releasedOrdersList = []
      this.pendingStatus = ''
      this.statusSelectValue = this.originalStatus
    },

    // 处理已放单图片选择
    handleReleasedImageSelect(event) {
      const file = event.target.files[0]
      if (file) {
        this.releasedOrderScreenshot = '/test.jpg'
      }
    },

    async addOrderToList() {
      if (!this.releasedOrderNumber || !this.releasedOrderNumber.trim()) {
        this.$message.warning('请输入订单号')
        return
      }
      console.log('========== addOrderToList 调试信息 ==========')
      console.log('this.currentCeping:', this.currentCeping)
      console.log('this.currentCeping.id:', this.currentCeping.id)
      console.log('订单号?', this.releasedOrderNumber.trim())
      console.log('订单截图:', this.releasedOrderScreenshot)
      
      const requestData = {
        cepingId: this.currentCeping.id,
        orderNumber: this.releasedOrderNumber.trim(),
        orderScreenshot: this.releasedOrderScreenshot
      }
      
      console.log('准备发送的请求数据:', requestData)
      console.log('==============================================')
      
      try {
        const response = await api.orderList.create(requestData)
        if (response.code === 200) {
          this.$message.success('订单创建成功')
          this.releasedOrdersList.push({
            id: response.data.id,
            orderNumber: this.releasedOrderNumber.trim(),
            orderScreenshot: this.releasedOrderScreenshot
          })
          this.releasedOrderNumber = ''
          this.releasedOrderScreenshot = ''
          if (this.$refs.releasedImageInput) {
            this.$refs.releasedImageInput.value = ''
          }
        } else {
          this.$message.error(response.message || '订单创建失败')
        }
      } catch (error) {
        console.error('创建订单失败', error)
        this.$message.error('创建订单失败，请稍后重试')
      }
    },

    // 从列表中移除订单
    removeOrderFromList(index) {
      this.releasedOrdersList.splice(index, 1)
    },

    async confirmCompletedStatus() {
      if (!this.completedReviewLink || !this.completedReviewLink.trim()) {
        this.$message.error('请输入评价链接')
        return
      }

      this.completedDialogVisible = false
      this.statusUpdating = true
      try {
        const updateResponse = await api.ceping.update(this.currentCeping.id, {
          reviewLink: this.completedReviewLink,
          reviewScreenshot: this.completedReviewScreenshot
        })

        if (updateResponse.code === 200) {
          const statusResponse = await api.ceping.update(this.currentCeping.id, {
            status: this.pendingStatus
          })
          if (statusResponse.code === 200) {
            this.$message.success('状态更新成功')
            // 更新本地数据
            this.currentCeping.reviewLink = this.completedReviewLink
            this.currentCeping.reviewScreenshot = this.completedReviewScreenshot
            this.currentCeping.status = this.pendingStatus
            this.originalStatus = this.pendingStatus
            // 刷新列表
            this.fetchCepingList()
          } else {
            this.$message.error(statusResponse.message || '状态更新失败')
            this.statusSelectValue = this.originalStatus
          }
        } else {
          this.$message.error(updateResponse.message || '评价信息更新失败')
          this.statusSelectValue = this.originalStatus
        }
      } catch (error) {
        console.error('更新失败', error)
        this.$message.error('更新失败，请稍后重试')
        this.statusSelectValue = this.originalStatus
      } finally {
        this.statusUpdating = false
      }
    },

    cancelCompletedStatus() {
      this.completedDialogVisible = false
      this.completedReviewLink = ''
      this.completedReviewScreenshot = ''
      this.pendingStatus = ''
      this.statusSelectValue = this.originalStatus
    },

    // 处理已完成图片选择
    handleCompletedImageSelect(event) {
      const file = event.target.files[0]
      if (file) {
        // 创建本地文件URL
        const imageUrl = '/test.jpg'
        this.completedReviewScreenshot = imageUrl
      }
    },

    // 确认申请增加预算
    async confirmBudgetRequest() {
      if (!this.budgetRequestAmount) {
        this.$message.error('请选择预算金额')
        return
      }

      this.budgetRequestDialogVisible = false
      this.statusUpdating = true
      try {
        // 更新管理员预算，同步基础预算，并重置用户预算。?
        const updateResponse = await api.ceping.update(this.currentCeping.id, {
          adminBudget: this.budgetRequestAmount,
          budget: this.budgetRequestAmount,
          userBudget: 0,
          remark: this.budgetRequestRemark || this.currentCeping.remark,
          status: this.pendingStatus
        })

        if (updateResponse.code === 200) {
          this.$message.success('状态更新成功')
          // 更新本地数据
          this.currentCeping.adminBudget = this.budgetRequestAmount
          this.currentCeping.budget = this.budgetRequestAmount
          this.currentCeping.budgetStatus = '1'
          if (this.budgetRequestRemark) {
            this.currentCeping.remark = this.budgetRequestRemark
          }
          this.currentCeping.status = this.pendingStatus
          this.originalStatus = this.pendingStatus
          // 刷新列表
          this.fetchCepingList()
        } else {
          this.$message.error(updateResponse.message || '更新失败')
          this.statusSelectValue = this.originalStatus
        }
      } catch (error) {
        console.error('更新失败', error)
        this.$message.error('更新失败，请稍后重试')
        this.statusSelectValue = this.originalStatus
      } finally {
        this.statusUpdating = false
      }
    },

    // 取消申请增加预算
    cancelBudgetRequest() {
      this.budgetRequestDialogVisible = false
      this.budgetRequestAmount = null
      this.budgetRequestRemark = ''
      this.pendingStatus = ''
      this.statusSelectValue = this.originalStatus
    },

    async handleOrderStatusChange(row, newStatus) {
      console.log('========== handleOrderStatusChange ==========')
      console.log('订单ID:', row.id)
      console.log('新状态?', newStatus)
      console.log('============================================')
      
      // 如果切换到已完成状态，需要先输入评价链接
      if (newStatus === '1') {
        this.pendingOrderStatus = {
          row: row,
          status: newStatus
        }
        this.orderCompletedReviewLink = row.reviewLink || ''
        this.orderCompletedReviewScreenshot = row.reviewScreenshot || ''
        this.orderCompletedDialogVisible = true
        return
      }
      
      try {
        const response = await api.orderList.getById(row.id)
        if (response.code !== 200 || !response.data) {
          this.$message.error('获取订单详情失败')
          return
        }

        const fullOrder = response.data
        fullOrder.status = newStatus

        const updateResponse = await api.orderList.update(row.id, fullOrder)
        
        if (updateResponse.code === 200) {
          this.$message.success('订单状态更新成功')
          row.status = newStatus
          this.refreshCurrentOrderList()
        } else {
          this.$message.error(updateResponse.message || '状态更新失败')
        }
      } catch (error) {
        console.error('更新订单状态失败', error)
        this.$message.error('更新失败，请稍后重试')
      }
    },

    // 刷新当前订单列表
    refreshCurrentOrderList() {
      if (this.currentCeping && this.currentCeping.id) {
        api.orderList.getByCepingId(this.currentCeping.id).then(response => {
          if (response.code === 200) {
            this.currentOrderList = (response.data || []).map(order => ({
              ...order,
              exchangeRate: order.ppMultiplier
            }))
          }
        })
      }
    },

    async confirmOrderCompletedStatus() {
      if (!this.orderCompletedReviewLink || !this.orderCompletedReviewLink.trim()) {
        this.$message.error('请输入评价链接')
        return
      }

      if (!this.pendingOrderStatus) {
        this.$message.error('状态更新信息丢失')
        return
      }

      const { row, status } = this.pendingOrderStatus

      this.orderCompletedDialogVisible = false
      this.statusUpdating = true

      try {
        const response = await api.orderList.getById(row.id)
        if (response.code !== 200 || !response.data) {
          this.$message.error('获取订单详情失败')
          this.statusUpdating = false
          return
        }

        const fullOrder = response.data
        fullOrder.status = status
        fullOrder.reviewLink = this.orderCompletedReviewLink.trim()
        fullOrder.reviewScreenshot = this.orderCompletedReviewScreenshot

        // 调用更新接口
        const updateResponse = await api.orderList.update(row.id, fullOrder)
        
        if (updateResponse.code === 200) {
          this.$message.success('订单已完成，状态更新成功')
          // 更新本地数据
          row.status = status
          row.reviewLink = this.orderCompletedReviewLink.trim()
          row.reviewScreenshot = this.orderCompletedReviewScreenshot
          // 刷新当前订单列表
          this.refreshCurrentOrderList()
        } else {
          this.$message.error(updateResponse.message || '状态更新失败')
        }
      } catch (error) {
        console.error('更新订单状态失败', error)
        this.$message.error('更新失败，请稍后重试')
      } finally {
        this.statusUpdating = false
        // 清空临时数据
        this.pendingOrderStatus = null
        this.orderCompletedReviewLink = ''
        this.orderCompletedReviewScreenshot = ''
      }
    },

    cancelOrderCompletedStatus() {
      this.orderCompletedDialogVisible = false
      this.pendingOrderStatus = null
      this.orderCompletedReviewLink = ''
      this.orderCompletedReviewScreenshot = ''
    },

    // 处理订单已完成状态下的图片选择
    handleOrderCompletedImageSelect(event) {
      const file = event.target.files[0]
      if (file) {
        this.orderCompletedReviewScreenshot = '/test.jpg'
      }
    },

    // 用户同意预算
    async agreeBudget(row) {
      try {
        const response = await api.ceping.update(row.id, {
          userBudget: row.adminBudget
        })
        
        if (response.code === 200) {
          this.$message.success('已同意预算')
          row.userBudget = row.adminBudget
          this.fetchCepingList()
        } else {
          this.$message.error(response.message || '操作失败')
        }
      } catch (error) {
        console.error('同意预算失败', error)
        this.$message.error('操作失败，请稍后重试')
      }
    },

    // 用户拒绝预算
    async rejectBudget(row) {
      try {
        const response = await api.ceping.update(row.id, {
          userBudget: 1
        })
        
        if (response.code === 200) {
          this.$message.success('已拒绝预算')
          row.userBudget = 1
          this.fetchCepingList()
        } else {
          this.$message.error(response.message || '操作失败')
        }
      } catch (error) {
        console.error('拒绝预算失败', error)
        this.$message.error('操作失败，请稍后重试')
      }
    },

    openBudgetEditDialog(row) {
      this.budgetEditRow = row
      this.budgetEditAmount = row.adminBudget
      this.budgetEditDialogVisible = true
    },

    // 确认修改预算
    async confirmBudgetEdit() {
      if (!this.budgetEditAmount || this.budgetEditAmount <= 0) {
        this.$message.error('请选择有效的预算金额')
        return
      }

      this.budgetEditDialogVisible = false
      try {
        const response = await api.ceping.update(this.budgetEditRow.id, {
          adminBudget: this.budgetEditAmount,
          budget: this.budgetEditAmount,
          userBudget: 0
        })

        if (response.code === 200) {
          this.$message.success('预算修改成功')
          this.fetchCepingList()
        } else {
          this.$message.error(response.message || '修改失败')
        }
      } catch (error) {
        console.error('修改预算失败', error)
        this.$message.error('修改失败，请稍后重试')
      } finally {
        this.budgetEditRow = null
        this.budgetEditAmount = null
      }
    },

    // 取消修改预算
    cancelBudgetEdit() {
      this.budgetEditDialogVisible = false
      this.budgetEditRow = null
      this.budgetEditAmount = null
    }
  }
}


