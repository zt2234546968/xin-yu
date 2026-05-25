<template>
  <div class="zhiping-container">
    <div class="header-container">
      <h2>直评任务管理</h2>
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
          <el-button type="primary" size="small" @click="openCreateDialog">创建任务</el-button>
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
                      <el-icon
                        v-for="i in 5"
                        :key="i"
                        class="star"
                        :class="{ active: i <= scope.row.starRating }"
                      >
                        <StarFilled v-if="i <= scope.row.starRating" />
                        <Star v-else />
                      </el-icon>
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
            <el-button v-if="isSuperAdmin" type="danger" size="small" @click="deleteZhiping(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 详情侧边栏 -->
    <el-drawer
      title="任务详情"
      v-model="drawerVisible"
      size="560px"
      :with-header="true"
    >
      <div class="detail-content" v-if="currentZhiping.id">
        <div class="detail-summary-card">
          <div class="summary-head">
            <div>
              <span class="summary-label">任务编号</span>
              <strong class="summary-code">{{ currentZhiping.code }}</strong>
            </div>
            <el-select
              v-if="isSuperAdmin"
              :model-value="currentZhiping.status"
              class="status-pill-select"
              @change="handleStatusChange"
              :disabled="statusUpdating"
            >
              <el-option label="等待提交" value="0" />
              <el-option label="等待反馈" value="1" />
              <el-option label="已完成" value="2" />
              <el-option label="已取消" value="3" />
            </el-select>
            <span v-else class="status-pill" :class="'status-pill-' + currentZhiping.status">
              {{ getStatusText(currentZhiping.status) }}
            </span>
          </div>

          <div class="summary-body">
            <img :src="currentZhiping.taskImage || '/test.jpg'" class="summary-thumb" alt="任务图片">
            <div class="summary-main">
              <span class="summary-label">ASIN</span>
              <strong class="summary-asin">{{ currentZhiping.asin || '-' }}</strong>
              <div class="summary-stars">
                <el-icon
                  v-for="i in 5"
                  :key="i"
                  class="star"
                  :class="{ active: i <= currentZhiping.starRating }"
                >
                  <StarFilled v-if="i <= currentZhiping.starRating" />
                  <Star v-else />
                </el-icon>
              </div>
            </div>
          </div>

          <div class="summary-meta">
            <span class="summary-meta-item" v-if="currentZhiping.country">
              <img v-if="currentZhiping.countryImage" :src="currentZhiping.countryImage" class="country-flag-small" alt="国旗">
              {{ currentZhiping.country }}
            </span>
            <span class="summary-meta-item">{{ getWarrantyTimeText(currentZhiping.warrantyTime) }}</span>
            <span class="summary-meta-item">{{ currentZhiping.channel || '暂无渠道' }}</span>
          </div>
        </div>

        <!-- 任务图片 -->
        <div class="detail-card" v-if="currentZhiping.taskImage">
          <div class="card-header">
            <el-icon><Picture /></el-icon>
            <span>任务图片</span>
          </div>
          <div class="card-body">
            <img :src="currentZhiping.taskImage" class="detail-image" alt="任务图片">
          </div>
        </div>

        <!-- 评论内容卡片 -->
        <div class="detail-card">
          <div class="card-header">
            <el-icon><Document /></el-icon>
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
            <el-icon><Timer /></el-icon>
            <span>质保信息</span>
          </div>
          <div class="card-body">
            <div class="info-row">
              <span class="info-label">渠道</span>
              <div class="info-value-right">
                <span class="info-value">{{ currentZhiping.channel || '--' }}</span>
                <el-button v-if="isSuperAdmin && currentZhiping.status === '1'" type="text" size="small" @click="openEditChannelDialog">修改</el-button>
              </div>
            </div>
            <div class="info-row">
              <span class="info-label">质保时间</span>
              <div class="info-value-right">
                <span class="info-value warranty">{{ getWarrantyTimeText(currentZhiping.warrantyTime) }}</span>
                <el-button v-if="isSuperAdmin" type="text" size="small" @click="openEditWarrantyDialog">修改</el-button>
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
            <el-icon><Link /></el-icon>
            <span>反馈信息</span>
          </div>
          <div class="card-body">
            <div class="info-row">
              <span class="info-label">反馈链接</span>
              <a v-if="currentZhiping.feedbackLink" :href="currentZhiping.feedbackLink" target="_blank" class="feedback-link-btn">
                <el-icon><TopRight /></el-icon> 打开链接
              </a>
              <span v-else class="info-value empty">暂无</span>
            </div>
            <div class="info-row">
              <span class="info-label">反馈图片</span>
              <div class="info-value-right">
                <img v-if="currentZhiping.feedbackImage" :src="currentZhiping.feedbackImage" class="detail-image-small" alt="反馈图片">
                <span v-else class="info-value">--</span>
                <el-button v-if="isSuperAdmin && currentZhiping.status === '2'" type="text" size="small" @click="openEditFeedbackDialog">修改</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>

    <!-- 创建任务对话框 -->
    <el-dialog
      title="创建直评任务"
      v-model="createDialogVisible"
      width="600px"
    >
      <el-form label-width="120px">
        <el-form-item label="国家">
          <el-select v-model="createForm.countryId" placeholder="请选择国家" @change="handleCountryChange">
            <template #prefix>
              <img v-if="createForm.country.flagImage" :src="createForm.country.flagImage" alt="国旗" class="selected-country-flag">
            </template>
            <el-option
              v-for="country in countryList"
              :key="country.id"
              :label="country.countryName"
              :value="country.id"
            >
              <div class="country-option">
                <span class="country-name">{{ country.countryName }}</span>
                <img :src="country.flagImage" alt="国旗" class="country-flag">
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="ASIN">
          <el-input v-model="createForm.asin"></el-input>
        </el-form-item>
        <el-form-item label="任务图片">
          <el-input v-model="createForm.taskImage"></el-input>
        </el-form-item>
        <el-form-item label="评论标题">
          <el-input v-model="createForm.reviewTitle"></el-input>
        </el-form-item>
        <el-form-item label="评论内容">
          <el-input v-model="createForm.reviewContent" type="textarea" rows="4"></el-input>
        </el-form-item>
        <el-form-item label="星评">
          <div class="star-rating">
            <el-icon
              v-for="star in 5"
              :key="star"
              class="star"
              :class="{ active: star <= createForm.starRating, hover: star <= hoverRating }"
              @click="createForm.starRating = star"
              @mouseenter="hoverRating = star"
              @mouseleave="hoverRating = 0"
            >
              <StarFilled v-if="star <= createForm.starRating || star <= hoverRating" />
              <Star v-else />
            </el-icon>
          </div>
        </el-form-item>
        <el-form-item label="质保时间">
          <el-select v-model="createForm.warrantyTime" placeholder="请选择质保时间">
            <el-option label="7天" value="0"></el-option>
            <el-option label="30天" value="1"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="店铺">
          <el-input v-model="createForm.shop" placeholder="请输入店铺" style="width: 240px;"></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-input :value="getStatusText(createForm.status)" disabled class="status-input"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createZhiping">确定</el-button>
      </span>
    </el-dialog>

    <!-- 反馈渠道输入对话框 -->
    <el-dialog
      title="输入反馈渠道"
      v-model="feedbackChannelDialogVisible"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form class="feedback-channel-form">
        <el-form-item label="渠道" label-width="80px">
          <el-input
            v-model="feedbackChannel"
            placeholder="请输入渠道"
            class="feedback-channel-input"
          />
        </el-form-item>
      </el-form>
      <p class="tip-text">提示：切换到"等待反馈"状态前，需要输入渠道以便后续跟进</p>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancelFeedbackChannel">取消</el-button>
        <el-button type="primary" @click="confirmFeedbackChannel">确认</el-button>
      </div>
    </el-dialog>

    <!-- 完成状态输入对话框 -->
    <el-dialog
      title="输入完成反馈"
      v-model="completeDialogVisible"
      width="450px"
      :close-on-click-modal="false"
    >
      <el-form class="complete-feedback-form">
        <el-form-item label="反馈链接" label-width="90px">
          <el-input
            v-model="completeFeedbackLink"
            placeholder="请输入反馈链接"
            class="complete-feedback-input"
          />
        </el-form-item>
        <el-form-item label="反馈截图" label-width="90px">
          <div class="image-upload-area">
            <input
              type="file"
              accept="image/*"
              @change="handleImageSelect"
              class="hidden-file-input"
              ref="imageInput"
            />
            <div v-if="completeFeedbackImage" class="image-preview">
              <img :src="completeFeedbackImage" alt="反馈截图" class="preview-image">
              <div class="image-actions">
                <el-button size="small" @click="completeFeedbackImage = ''">更换</el-button>
              </div>
            </div>
            <div v-else class="image-upload-btn" @click="$refs.imageInput.click()">
              <el-icon><Plus /></el-icon>
              <p>点击上传截图</p>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <p class="tip-text">提示：切换到"已完成"状态前，需要填写反馈链接（必填），截图可选</p>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancelCompleteStatus">取消</el-button>
        <el-button type="primary" @click="confirmCompleteStatus">确认</el-button>
      </div>
    </el-dialog>

    <!-- 编辑反馈信息对话框 -->
    <el-dialog
      title="修改反馈信息"
      v-model="editFeedbackDialogVisible"
      width="450px"
      :close-on-click-modal="false"
    >
      <el-form class="edit-feedback-form">
        <el-form-item label="反馈链接" label-width="90px">
          <el-input
            v-model="editFeedbackLink"
            placeholder="请输入反馈链接"
            class="edit-feedback-input"
          />
        </el-form-item>
        <el-form-item label="反馈截图" label-width="90px">
          <div class="image-upload-area">
            <input
              type="file"
              accept="image/*"
              @change="handleEditImageSelect"
              class="hidden-file-input"
              ref="editImageInput"
            />
            <div v-if="editFeedbackImage" class="image-preview">
              <img :src="editFeedbackImage" alt="反馈截图" class="preview-image">
              <div class="image-actions">
                <el-button size="small" @click="editFeedbackImage = ''">更换</el-button>
              </div>
            </div>
            <div v-else class="image-upload-btn" @click="$refs.editImageInput.click()">
              <el-icon><Plus /></el-icon>
              <p>点击上传截图</p>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancelEditFeedback">取消</el-button>
        <el-button type="primary" @click="confirmEditFeedback">确认</el-button>
      </div>
    </el-dialog>

    <!-- 编辑渠道对话框 -->
    <el-dialog
      title="修改渠道"
      v-model="editChannelDialogVisible"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form class="edit-channel-form">
        <el-form-item label="渠道" label-width="80px">
          <el-input
            v-model="editChannel"
            placeholder="请输入渠道"
            class="edit-channel-input"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancelEditChannel">取消</el-button>
        <el-button type="primary" @click="confirmEditChannel">确认</el-button>
      </div>
    </el-dialog>

    <!-- 编辑质保时间对话框 -->
    <el-dialog
      title="修改质保时间"
      v-model="editWarrantyDialogVisible"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form class="edit-warranty-form">
        <el-form-item label="质保时间" label-width="80px">
          <el-select
            v-model="editWarrantyTime"
            placeholder="请选择质保时间"
            class="edit-warranty-input"
          >
            <el-option label="7天" value="0"></el-option>
            <el-option label="30天" value="1"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancelEditWarranty">取消</el-button>
        <el-button type="primary" @click="confirmEditWarranty">确认</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { Document, Link, Picture, Plus, Star, StarFilled, Timer, TopRight } from '@element-plus/icons-vue'
import zhipingMixin from './index'

export default {
  name: 'ZhipingView',
  components: {
    Document,
    Link,
    Picture,
    Plus,
    Star,
    StarFilled,
    Timer,
    TopRight
  },
  mixins: [zhipingMixin]
}
</script>

<style scoped>
@import './index.css';
</style>
