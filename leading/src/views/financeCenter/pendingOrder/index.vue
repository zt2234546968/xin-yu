<template>
  <div class="zhiping-container">
    <div class="header-container">
      <h2>待结算订单</h2>
      <div class="header-right">
        <el-input v-model="searchKeyword" placeholder="搜索任务编号" size="small" clearable
          style="width: 200px; margin-right: 10px;" @keyup.enter="handleSearch" @clear="handleSearch"></el-input>
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
              <!-- 左侧：任务图片 -->
              <div class="task-image">
                <img src="/test.jpg" alt="任务图片" class="image-thumbnail">
              </div>
              <!-- 右侧：任务信息 -->
              <div class="right-box">
                <!-- 上部分：ASIN、星评和国旗 -->
                <div class="upper-section">
                  <div class="asin">{{ scope.row.asin }}</div>
                  <div class="rating-flag">
                    <!-- 星级 -->
                    <div class="stars">
                      <i v-for="i in 5" :key="i"
                        :class="i <= scope.row.starRating ? 'el-icon-star-on' : 'el-icon-star-off'" class="star"></i>
                    </div>
                    <!-- 国旗图片 -->
                    <div class="flag">
                      <img src="/test.jpg" alt="国家图片" class="flag-thumbnail">
                    </div>
                  </div>
                </div>
                <!-- 下部分：评论内容 -->
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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="openDetailSidebar(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 详情侧边栏 -->
    <el-drawer title="任务详情" v-model="drawerVisible" size="480px" :with-header="true">
      <div class="detail-content" v-if="currentZhiping.id">
        <!-- 顶部状态大卡片 - 超级管理员版 -->
        <template v-if="isSuperAdmin">
          <div class="detail-header-card">
            <div class="task-status-large" :class="'status-bg-' + currentZhiping.status">
              <div class="status-main">
                <el-select :value="currentZhiping.status" class="status-select" @change="handleStatusChange"
                  :disabled="statusUpdating">
                  <el-option label="等待提交" value="0"></el-option>
                  <el-option label="等待反馈" value="1"></el-option>
                  <el-option label="已完成" value="2"></el-option>
                  <el-option label="已取消" value="3"></el-option>
                </el-select>
              </div>
              <div class="task-meta">
                <span class="meta-item">
                  <i class="el-icon-document"></i>
                  {{ currentZhiping.code }}
                </span>
                <span class="meta-item" v-if="currentZhiping.country">
                  <img v-if="currentZhiping.countryImage" :src="currentZhiping.countryImage" class="country-flag-small"
                    alt="国旗">
                  {{ currentZhiping.country }}
                </span>
              </div>
            </div>
          </div>
        </template>

        <!-- 顶部状态大卡片 - 普通用户版 -->
        <template v-else>
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
                  <img v-if="currentZhiping.countryImage" :src="currentZhiping.countryImage" class="country-flag-small"
                    alt="国旗">
                  {{ currentZhiping.country }}
                </span>
              </div>
            </div>
          </div>
        </template>

        <!-- 商品信息卡片 -->
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
                <i v-for="i in 5" :key="i"
                  :class="i <= currentZhiping.starRating ? 'el-icon-star-on' : 'el-icon-star-off'" class="star"></i>
              </div>
            </div>
          </div>
        </div>

        <!-- 任务图片 -->
        <div class="detail-card" v-if="currentZhiping.taskImage">
          <div class="card-header">
            <i class="el-icon-picture-outline"></i>
            <span>任务图片</span>
          </div>
          <div class="card-body">
            <img :src="currentZhiping.taskImage" class="detail-image" alt="任务图片">
          </div>
        </div>

        <!-- 评论内容卡片 -->
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

        <!-- 质保信息卡片 -->
        <div class="detail-card">
          <div class="card-header">
            <i class="el-icon-time"></i>
            <span>质保信息</span>
          </div>
          <div class="card-body">
            <div class="info-row">
              <span class="info-label">渠道</span>
              <div class="info-value-right">
                <span class="info-value">{{ currentZhiping.channel || '--' }}</span>
              </div>
            </div>
            <div class="info-row">
              <span class="info-label">质保时间</span>
              <div class="info-value-right">
                <span class="info-value warranty">{{ getWarrantyTimeText(currentZhiping.warrantyTime) }}</span>
              </div>
            </div>
            <div class="info-row">
              <span class="info-label">创建时间</span>
              <div class="info-value-right">
                <span class="info-value">{{ currentZhiping.createTime }}</span>
              </div>
            </div>
            <div class="info-row">
              <span class="info-label">更新时间</span>
              <div class="info-value-right">
                <span class="info-value">{{ currentZhiping.updateTime }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 反馈信息卡片 -->
        <div class="detail-card">
          <div class="card-header">
            <i class="el-icon-link"></i>
            <span>反馈信息</span>
          </div>
          <div class="card-body">
            <div class="info-row">
              <span class="info-label">反馈链接</span>
              <a v-if="currentZhiping.feedbackLink" :href="currentZhiping.feedbackLink" target="_blank"
                class="feedback-link-btn">
                <i class="el-icon-top-right"></i> 打开链接
              </a>
              <span v-else class="info-value empty">暂无</span>
            </div>
            <div class="info-row">
              <span class="info-label">反馈图片</span>
              <div class="info-value-right">
                <img v-if="currentZhiping.feedbackImage" :src="currentZhiping.feedbackImage" class="detail-image-small"
                  alt="反馈图片">
                <span v-else class="info-value">--</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import pendingOrder from './pendingOrder'

export default {
  name: 'pendingOrder',
  mixins: [pendingOrder]
}
</script>

<style scoped>
@import './pendingOrder.css';
</style>



