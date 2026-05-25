<template>
  <div class="zhiping-container">
    <div class="header-container">
      <h2>已结算订单</h2>
      <div class="header-right">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索任务编号"
          size="small"
          clearable
          style="width: 200px; margin-right: 10px;"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        ></el-input>
        <div class="header-buttons">
          <el-button type="default" size="small" @click="handleSearch">搜索</el-button>
          <el-button type="default" size="small" @click="refreshList">刷新</el-button>
        </div>
      </div>
    </div>

    <div style="overflow-x: auto;">
      <el-table :data="zhipingList" style="width: 100%" stripe>
        <el-table-column label="序号" width="60" fixed="left">
          <template #default="scope">
            {{ scope.$index + 1 }}
          </template>
        </el-table-column>
        <el-table-column prop="code" label="ID" width="80"></el-table-column>
        <el-table-column label="任务信息" min-width="500">
          <template #default="scope">
            <div class="task-info">
              <div class="task-image">
                <img src="/test.jpg" alt="任务图片" class="image-thumbnail">
              </div>
              <div class="right-box">
                <div class="upper-section">
                  <div class="asin">{{ scope.row.asin }}</div>
                  <div class="rating-flag">
                    <div class="stars">
                      <i
                        v-for="i in 5"
                        :key="i"
                        :class="i <= scope.row.starRating ? 'el-icon-star-on' : 'el-icon-star-off'"
                        class="star"
                      ></i>
                    </div>
                    <div class="flag">
                      <img src="/test.jpg" alt="国家图片" class="flag-thumbnail">
                    </div>
                  </div>
                </div>
                <div class="lower-section">
                  <div class="review-title">{{ scope.row.reviewTitle }}</div>
                  <div class="review-content">{{ scope.row.reviewContent }}</div>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            {{ getStatusText(scope.row.status) }}
          </template>
        </el-table-column>
        <el-table-column prop="orderAmount" label="订单金额" width="180"></el-table-column>
        <el-table-column label="质保时间" width="100">
          <template #default="scope">
            {{ getWarrantyTimeText(scope.row.warrantyTime) }}
          </template>
        </el-table-column>
        <el-table-column label="渠道" width="120">
          <template #default="scope">
            {{ scope.row.channel || '--' }}
          </template>
        </el-table-column>
        <el-table-column label="反馈链接" min-width="200">
          <template #default="scope">
            <a v-if="scope.row.feedbackLink" :href="scope.row.feedbackLink" target="_blank" class="feedback-link">
              {{ scope.row.feedbackLink }}
            </a>
            <span v-else>--</span>
          </template>
        </el-table-column>
        <el-table-column label="反馈图片" width="140">
          <template #default="scope">
            <div v-if="scope.row.feedbackImage && scope.row.feedbackImage.trim()" class="feedback-image">
              <img :src="scope.row.feedbackImage" alt="反馈图片" class="image-thumbnail">
            </div>
            <span v-else>--</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
        <el-table-column prop="updateTime" label="修改时间" width="180"></el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="openDetailSidebar(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-drawer
      title="任务详情"
      v-model="drawerVisible"
      size="480px"
      :with-header="true"
    >
      <div class="detail-content" v-if="currentZhiping.id">
        <div class="detail-header-card">
          <div class="task-status-large" :class="'status-bg-' + currentZhiping.status">
            <div class="status-main">
              <span class="status-text">{{ getStatusText(currentZhiping.status) }}</span>
            </div>
            <div class="task-meta">
              <span class="meta-item">
                <i class="el-icon-document"></i>
                {{ currentZhiping.code }}
              </span>
              <span class="meta-item" v-if="currentZhiping.country">
                <img v-if="currentZhiping.countryImage" :src="currentZhiping.countryImage" class="country-flag-small" alt="国旗">
                {{ currentZhiping.country }}
              </span>
            </div>
          </div>
        </div>

        <div class="detail-card">
          <div class="card-header">
            <i class="el-icon-goods"></i>
            <span>商品信息</span>
          </div>
          <div class="card-body">
            <div class="info-row">
              <span class="info-label">ASIN</span>
              <span class="info-value asin">{{ currentZhiping.asin }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">星评</span>
              <div class="star-display">
                <i v-for="i in 5" :key="i" :class="i <= currentZhiping.starRating ? 'el-icon-star-on' : 'el-icon-star-off'" class="star"></i>
              </div>
            </div>
          </div>
        </div>

        <div class="detail-card" v-if="currentZhiping.taskImage">
          <div class="card-header">
            <i class="el-icon-picture-outline"></i>
            <span>任务图片</span>
          </div>
          <div class="card-body">
            <img :src="currentZhiping.taskImage" class="detail-image" alt="任务图片">
          </div>
        </div>

        <div class="detail-card">
          <div class="card-header">
            <i class="el-icon-document"></i>
            <span>评论内容</span>
          </div>
          <div class="card-body">
            <div class="review-section">
              <h4 class="review-title">{{ currentZhiping.reviewTitle }}</h4>
              <p class="review-text">{{ currentZhiping.reviewContent }}</p>
            </div>
          </div>
        </div>

        <div class="detail-card">
          <div class="card-header">
            <i class="el-icon-time"></i>
            <span>质保信息</span>
          </div>
          <div class="card-body">
            <div class="info-row">
              <span class="info-label">渠道</span>
              <span class="info-value">{{ currentZhiping.channel || '--' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">质保时间</span>
              <span class="info-value warranty">{{ getWarrantyTimeText(currentZhiping.warrantyTime) }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">创建时间</span>
              <span class="info-value">{{ currentZhiping.createTime }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">更新时间</span>
              <span class="info-value">{{ currentZhiping.updateTime }}</span>
            </div>
          </div>
        </div>

        <div class="detail-card">
          <div class="card-header">
            <i class="el-icon-link"></i>
            <span>反馈信息</span>
          </div>
          <div class="card-body">
            <div class="info-row">
              <span class="info-label">反馈链接</span>
              <a v-if="currentZhiping.feedbackLink" :href="currentZhiping.feedbackLink" target="_blank" class="feedback-link-btn">
                <i class="el-icon-top-right"></i> 打开链接
              </a>
              <span v-else class="info-value empty">暂无</span>
            </div>
            <div class="info-row">
              <span class="info-label">反馈图片</span>
              <img v-if="currentZhiping.feedbackImage" :src="currentZhiping.feedbackImage" class="detail-image-small" alt="反馈图片">
              <span v-else class="info-value">--</span>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import api from '@/api'

export default {
  name: 'SettledOrderView',
  data() {
    return {
      allZhipingList: [],
      zhipingList: [],
      searchKeyword: '',
      drawerVisible: false,
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
      loading: false
    }
  },
  mounted() {
    this.fetchZhipingList()
  },
  methods: {
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
        '0': '等待提交',
        '1': '等待反馈',
        '2': '已完成',
        '3': '已取消'
      }
      return statusMap[status] || status
    },
    getWarrantyTimeText(warrantyTime) {
      const warrantyMap = {
        '0': '7天',
        '1': '30天'
      }
      return warrantyMap[warrantyTime] || warrantyTime
    },
    refreshList() {
      this.searchKeyword = ''
      this.fetchZhipingList()
    },
    async fetchZhipingList() {
      this.loading = true
      try {
        const response = await api.zhiping.list()
        if (response.code === 200) {
          this.allZhipingList = response.data.map(item => ({
            ...item,
            country: item.country?.countryName || item.country || '',
            countryImage: item.country?.flagImage || item.countryImage || '',
            channel: item.channel || '',
            createTime: this.formatDateTime(item.createTime),
            updateTime: this.formatDateTime(item.updateTime)
          }))
          this.handleSearch()
        }
      } catch (error) {
        console.error('获取直评任务列表失败', error)
        this.$message.error('获取直评任务列表失败')
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      if (!this.searchKeyword || !this.searchKeyword.trim()) {
        this.zhipingList = [...this.allZhipingList]
        return
      }
      const keyword = this.searchKeyword.trim().toLowerCase()
      this.zhipingList = this.allZhipingList.filter(item => {
        return item.code && item.code.toLowerCase().includes(keyword)
      })
    },
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
      this.drawerVisible = true
    }
  }
}
</script>

<style scoped>
.zhiping-container {
  padding: 20px;
}

.cell {
  display: flex;
  align-items: center;
}

.header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0;
}

.header-right {
  display: flex;
  align-items: center;
}

.header-buttons {
  display: flex;
  gap: 10px;
  align-items: center;
}

.header-container h2 {
  margin: 0;
  color: #333;
  font-size: 16px;
  font-weight: bold;
}

.el-button {
  margin-bottom: 0;
}

.el-table {
  margin-top: 20px;
}

.el-dialog__body {
  padding: 15px 20px;
}

.el-form-item {
  margin-bottom: 15px;
}

.el-form-item__label {
  font-weight: 500;
  padding: 0 0 0 0;
}

.el-input__inner {
  border-radius: 4px;
  height: 32px;
}

.el-textarea__inner {
  border-radius: 4px;
  resize: vertical;
  padding: 8px 12px;
}

.dialog-footer {
  text-align: right;
  padding-top: 10px;
}

.dialog-footer .el-button {
  margin-left: 10px;
  margin-bottom: 0;
  padding: 8px 15px;
}

.task-info {
  display: flex;
  align-items: center;
  padding: 10px;
  width: 100%;
}

.task-image {
  display: flex;
  align-items: center;
  margin-right: 15px;
  flex-shrink: 0;
}

.image-thumbnail {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
}

.right-box {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
}

.upper-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.asin {
  font-weight: bold;
  color: var(--app-primary);
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rating-flag {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.stars {
  color: #fadb14;
  font-size: 16px;
}

.star {
  margin-right: 2px;
}

.flag {
  display: flex;
  align-items: center;
}

.flag-thumbnail {
  width: 50px;
  height: 30px;
  object-fit: cover;
  border-radius: 2px;
}

.lower-section {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.review-title {
  font-size: 14px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  width: 100%;
}

.review-content {
  font-size: 13px;
  color: #666;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  width: 100%;
}

.feedback-link {
  color: var(--app-primary);
  text-decoration: none;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: block;
  max-width: 100%;
}

.feedback-link:hover {
  text-decoration: none;
}

.feedback-image {
  display: flex;
  align-items: center;
  justify-content: center;
}

.feedback-image img {
  width: 120px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

.detail-content {
  padding: 0 20px 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.detail-header-card {
  margin-bottom: 16px;
}

.task-status-large {
  border-radius: 16px;
  padding: 24px;
  color: #fff;
  position: relative;
  overflow: hidden;
}

.task-status-large::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -20%;
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.task-status-large::after {
  content: '';
  position: absolute;
  bottom: -30%;
  left: -10%;
  width: 150px;
  height: 150px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
}

.status-bg-0 {
  background: linear-gradient(135deg, var(--app-primary) 0%, var(--app-accent) 100%);
}

.status-bg-1 {
  background: linear-gradient(135deg, var(--app-warning) 0%, var(--app-warning) 100%);
}

.status-bg-2 {
  background: linear-gradient(135deg, var(--app-success) 0%, var(--app-success) 100%);
}

.status-bg-3 {
  background: linear-gradient(135deg, var(--app-danger) 0%, var(--app-danger) 100%);
}

.status-main {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.status-text {
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 2px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.task-meta {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  opacity: 0.9;
  background: rgba(255, 255, 255, 0.15);
  padding: 6px 12px;
  border-radius: 20px;
}

.country-flag-small {
  width: 20px;
  height: 14px;
  border-radius: 2px;
  object-fit: cover;
}

.detail-card {
  background: #fff;
  border-radius: 12px;
  margin-bottom: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 18px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  font-weight: 600;
  font-size: 14px;
  color: #333;
}

.card-header i {
  font-size: 16px;
  color: var(--app-primary);
}

.card-body {
  padding: 16px 18px;
}

.info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px dashed #f0f0f0;
}

.info-row:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 13px;
  color: #909399;
  flex-shrink: 0;
}

.info-value {
  font-size: 14px;
  color: #333;
  text-align: right;
}

.info-value.asin {
  color: var(--app-primary);
  font-weight: 600;
  font-family: 'Courier New', monospace;
}

.info-value.warranty {
  color: var(--app-warning);
  font-weight: 500;
}

.info-value.empty {
  color: #c0c4cc;
}

.star-display {
  display: flex;
  gap: 2px;
}

.star-display .star {
  font-size: 16px;
}

.star-display .el-icon-star-on {
  color: var(--app-warning);
}

.star-display .el-icon-star-off {
  color: #e0e0e0;
}

.review-section {
  padding: 4px 0;
}

.review-section .review-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin: 0 0 10px 0;
  line-height: 1.4;
}

.review-section .review-text {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin: 0;
  word-break: break-all;
}

.detail-image {
  width: 100%;
  max-height: 200px;
  object-fit: cover;
  border-radius: 8px;
  display: block;
}

.detail-image-small {
  width: 100px;
  height: 60px;
  object-fit: cover;
  border-radius: 6px;
}

.feedback-link-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: linear-gradient(135deg, var(--app-primary) 0%, var(--app-accent) 100%);
  color: #fff;
  text-decoration: none;
  border-radius: 6px;
  font-size: 13px;
  transition: transform 0.2s, box-shadow 0.2s;
}

.feedback-link-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  text-decoration: none;
  color: #fff;
}

:deep(.el-drawer__header) {
  margin-bottom: 0;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-drawer__header span) {
  font-weight: 600;
  font-size: 16px;
  color: #333;
}
</style>


