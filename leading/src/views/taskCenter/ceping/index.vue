<template>
  <div class="ceping-container">
    <div class="header-container">
      <h2>测评任务管理</h2>
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
      <el-table :data="cepingList" style="width: 100%" stripe>
        <el-table-column label="序号" width="60" fixed="left">
          <template #default="scope">
            {{ scope.$index + 1 }}
          </template>
        </el-table-column>
        <el-table-column prop="code" label="ID" width="80"></el-table-column>
        <el-table-column label="商品信息" min-width="600">
          <template #default="scope">
            <div class="task-info">
              <!-- 左侧：商品图片 -->
              <div class="task-image">
                <a v-if="scope.row.productLink" :href="scope.row.productLink" target="_blank" class="product-image-link">
                  <img :src="scope.row.productImage || '/test.jpg'" alt="商品图片" class="image-thumbnail">
                </a>
                <img v-else :src="scope.row.productImage || '/test.jpg'" alt="商品图片" class="image-thumbnail">
              </div>
              <!-- 右侧：商品信息 -->
              <div class="right-box">
                <!-- 第一行：左是商品名称，右是ASIN+店铺+好评+国旗 -->
                <div class="upper-section">
                  <div class="product-name">
                    <a v-if="scope.row.productLink" :href="scope.row.productLink" target="_blank" class="product-name-link">
                      {{ scope.row.productName || '--' }}
                    </a>
                    <span v-else>{{ scope.row.productName || '--' }}</span>
                  </div>
                  <div class="rating-flag">
                    <span v-if="scope.row.asin" class="info-item">ASIN: {{ scope.row.asin }}</span>
                    <span v-if="scope.row.shop" class="info-item">店铺: {{ scope.row.shop }}</span>
                    <el-tag :type="scope.row.isPositive ? 'success' : 'danger'" size="mini">
                      {{ scope.row.isPositive ? '好评' : '差评' }}
                    </el-tag>
                    <img v-if="scope.row.countryImage" :src="scope.row.countryImage" alt="国旗" class="flag-thumbnail">
                  </div>
                </div>
                <!-- 评论信息 - 好评用flex占位，差评显示内容 -->
                <div class="lower-section">
                  <div v-if="scope.row.isPositive" class="review-empty"></div>
                  <template v-else>
                    <div v-if="scope.row.reviewTitle" class="review-title">{{ scope.row.reviewTitle }}</div>
                    <div v-if="scope.row.reviewContent" class="review-content">{{ scope.row.reviewContent }}</div>
                  </template>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="scope">
            {{ getStatusText(scope.row.status) }}
          </template>
        </el-table-column>
        <el-table-column label="上评要求" width="300">
          <template #default="scope">
            <div class="requirement-list">
              <div v-if="scope.row.freeReview > 0" class="requirement-item">免评: {{ scope.row.freeReview }}</div>
              <div v-if="scope.row.starReview > 0" class="requirement-item">点星: {{ scope.row.starReview }}</div>
              <div v-if="scope.row.textReview > 0" class="requirement-item">文字: {{ scope.row.textReview }}</div>
              <div v-if="scope.row.imageReview > 0" class="requirement-item">图片: {{ scope.row.imageReview }}</div>
              <div v-if="scope.row.videoReview > 0" class="requirement-item">视频: {{ scope.row.videoReview }}</div>
              <div v-if="scope.row.feedbackReview > 0" class="requirement-item">Feedback: {{ scope.row.feedbackReview }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="总量" width="80">
          <template #default="scope">
            {{ scope.row.totalQuantity }}
          </template>
        </el-table-column>
        <el-table-column label="店铺" width="150">
          <template #default="scope">
            {{ scope.row.shop || '--' }}
          </template>
        </el-table-column>
        <el-table-column label="售价" width="100">
          <template #default="scope">
            {{ scope.row.price || '--' }}
          </template>
        </el-table-column>
        <el-table-column label="备注" width="150">
          <template #default="scope">
            {{ scope.row.remark || '--' }}
          </template>
        </el-table-column>
        <el-table-column label="预算" width="200">
          <template #default="scope">
            <div v-if="getBudgetStatus(scope.row) === 'waiting' && !isSuperAdmin" class="budget-negotiation">
              <span class="budget-value">{{ scope.row.adminBudget }}</span>
              <div class="negotiation-buttons">
                <el-button size="mini" type="primary" @click="agreeBudget(scope.row)">同意</el-button>
                <el-button size="mini" type="danger" @click="rejectBudget(scope.row)">拒绝</el-button>
              </div>
            </div>
            <div v-else-if="getBudgetStatus(scope.row) === 'agreed'" class="budget-agreed">
              <span class="budget-value">{{ scope.row.adminBudget }}</span>
              <span class="status-tag success">已同意</span>
            </div>
            <div v-else-if="getBudgetStatus(scope.row) === 'rejected' && isSuperAdmin" class="budget-rejected">
              <span class="budget-value">{{ scope.row.adminBudget }}</span>
              <span class="status-tag danger">已拒绝</span>
              <el-button size="mini" type="warning" @click="openBudgetEditDialog(scope.row)">修改</el-button>
            </div>
            <div v-else-if="getBudgetStatus(scope.row) === 'rejected' && !isSuperAdmin" class="budget-rejected">
              <span class="budget-value">{{ scope.row.adminBudget }}</span>
              <span class="status-tag danger">已拒绝</span>
            </div>
            <div v-else>
              {{ scope.row.budget || '--' }}
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="订单信息" width="120">
          <template #default="scope">
            <span v-if="!scope.row.orderLists || scope.row.orderLists.length === 0" class="no-order">--</span>
            <el-button
              v-else
              type="text"
              class="view-order-btn"
              @click="openOrderDialog(scope.row)"
            >
              查看
            </el-button>
          </template>
        </el-table-column>
        <el-table-column label="评价链接" min-width="180">
          <template #default="scope">
            <a v-if="scope.row.reviewLink" :href="scope.row.reviewLink" target="_blank" class="feedback-link">
              {{ scope.row.reviewLink }}
            </a>
            <span v-else>--</span>
          </template>
        </el-table-column>
        <el-table-column label="评价截图" width="120">
          <template #default="scope">
            <div v-if="scope.row.reviewScreenshot && scope.row.reviewScreenshot.trim()" class="feedback-image">
              <img :src="scope.row.reviewScreenshot" alt="评价截图" class="image-thumbnail">
            </div>
            <span v-else>--</span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="修改时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="openDetailSidebar(scope.row)">详情</el-button>
            <el-button v-if="isSuperAdmin" type="danger" size="small" @click="deleteCeping(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 详情侧边栏 -->
    <el-drawer
      title="任务详情"
      v-model="drawerVisible"
      size="480px"
      :with-header="true"
    >
      <div class="detail-content" v-if="currentCeping.id">
        <!-- 顶部状态大卡片 - 超级管理员版 -->
        <template v-if="isSuperAdmin">
          <div class="detail-header-card">
            <div class="task-status-large" :class="'status-bg-' + currentCeping.status">
              <div class="status-main">
                <el-select 
                  :value="String(currentCeping.status)" 
                  class="status-select"
                  @change="handleStatusChange"
                  :disabled="statusUpdating"
                >
                  <el-option label="放单中" value="0"></el-option>
                  <el-option label="已放单" value="1"></el-option>
                  <el-option label="申请增加预算" value="2"></el-option>
                </el-select>
              </div>
              <div class="task-meta">
                <span class="meta-item">
                  <i class="el-icon-document"></i>
                  {{ currentCeping.code }}
                </span>
                <span class="meta-item" v-if="currentCeping.country">
                  <img v-if="currentCeping.countryImage" :src="currentCeping.countryImage" class="country-flag-small" alt="国旗">
                  {{ currentCeping.country }}
                </span>
              </div>
            </div>
          </div>
        </template>

        <!-- 顶部状态大卡片 - 普通用户版 -->
        <template v-else>
          <div class="detail-header-card">
            <div class="task-status-large" :class="'status-bg-' + currentCeping.status">
              <div class="status-main">
                <span class="status-text">{{ getStatusText(currentCeping.status) }}</span>
              </div>
              <div class="task-meta">
                <span class="meta-item">
                  <i class="el-icon-document"></i>
                  {{ currentCeping.code }}
                </span>
                <span class="meta-item" v-if="currentCeping.country">
                  <img v-if="currentCeping.countryImage" :src="currentCeping.countryImage" class="country-flag-small" alt="国旗">
                  {{ currentCeping.country }}
                </span>
              </div>
            </div>
          </div>
        </template>

        <div class="detail-card">
          <div class="card-header">
            <i class="el-icon-goods"></i>
            <span>任务信息</span>
          </div>
          <div class="card-body">
            <div class="info-row">
              <span class="info-label">评论类型</span>
              <el-tag :type="currentCeping.isPositive ? 'success' : 'danger'" size="small">
                {{ currentCeping.isPositive ? '好评' : '差评' }}
              </el-tag>
            </div>
          </div>
        </div>

        <!-- 商品信息 -->
        <div class="detail-card">
          <div class="card-header">
            <i class="el-icon-goods"></i>
            <span>商品信息</span>
          </div>
          <div class="card-body">
            <div class="info-row" v-if="currentCeping.productName">
              <span class="info-label">商品名称</span>
              <span class="info-value">{{ currentCeping.productName }}</span>
            </div>
            <div class="info-row" v-if="currentCeping.asin">
              <span class="info-label">ASIN</span>
              <span class="info-value asin">{{ currentCeping.asin }}</span>
            </div>
            <div class="info-row" v-if="currentCeping.shop">
              <span class="info-label">店铺</span>
              <span class="info-value">{{ currentCeping.shop }}</span>
            </div>
            <div class="info-row" v-if="currentCeping.price">
              <span class="info-label">售价</span>
              <span class="info-value">{{ currentCeping.price }}</span>
            </div>
          </div>
        </div>

        <!-- 商品图片 -->
        <div class="detail-card" v-if="currentCeping.productImage">
          <div class="card-header">
            <i class="el-icon-picture-outline"></i>
            <span>商品图片</span>
          </div>
          <div class="card-body">
            <img :src="currentCeping.productImage" class="detail-image" alt="商品图片">
          </div>
        </div>

        <!-- 评论信息 -->
        <div class="detail-card" v-if="!currentCeping.isPositive">
          <div class="card-header">
            <i class="el-icon-chat-dot-round"></i>
            <span>评论信息</span>
          </div>
          <div class="card-body">
            <div class="info-row" v-if="currentCeping.reviewTitle">
              <span class="info-label">评论标题</span>
              <span class="info-value">{{ currentCeping.reviewTitle }}</span>
            </div>
            <div class="info-row" v-if="currentCeping.reviewContent">
              <span class="info-label">评论内容</span>
              <span class="info-value">{{ currentCeping.reviewContent }}</span>
            </div>
          </div>
        </div>

        <!-- 上评要求 -->
        <div class="detail-card">
          <div class="card-header">
            <i class="el-icon-s-claim"></i>
            <span>上评要求</span>
          </div>
          <div class="card-body">
            <div class="info-row">
              <span class="info-label">总量</span>
              <span class="info-value">{{ currentCeping.totalQuantity }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">免评</span>
              <span class="info-value">{{ currentCeping.freeReview || 0 }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">点星</span>
              <span class="info-value">{{ currentCeping.starReview || 0 }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">文字</span>
              <span class="info-value">{{ currentCeping.textReview || 0 }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">图片</span>
              <span class="info-value">{{ currentCeping.imageReview || 0 }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">视频</span>
              <span class="info-value">{{ currentCeping.videoReview || 0 }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">Feedback</span>
              <span class="info-value">{{ currentCeping.feedbackReview || 0 }}</span>
            </div>
          </div>
        </div>

        <!-- 备注 -->
        <div class="detail-card" v-if="currentCeping.remark">
          <div class="card-header">
            <i class="el-icon-document"></i>
            <span>备注</span>
          </div>
          <div class="card-body">
            <span class="info-value">{{ currentCeping.remark }}</span>
          </div>
        </div>
      </div>
    </el-drawer>

    <!-- 创建对话框 -->
    <el-dialog title="创建测评任务" v-model="createDialogVisible" width="800px">
      <el-form :model="createForm" ref="createForm" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="国家" required>
              <el-select v-model="createForm.countryId" placeholder="请选择国家" style="width: 100%;" @change="handleCountryChange">
                <template slot="prefix">
                  <img v-if="createForm.country.flagImage" :src="createForm.country.flagImage" alt="国旗" class="selected-country-flag">
                </template>
                <el-option
                  v-for="item in countryList"
                  :key="item.id"
                  :label="item.countryName"
                  :value="item.id"
                >
                  <div class="country-option">
                    <span class="country-name">{{ item.countryName }}</span>
                    <img :src="item.flagImage" alt="国旗" class="country-flag">
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="ASIN">
              <el-input v-model="createForm.asin" placeholder="请输入ASIN"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="关键词" required v-if="createForm.isPositive">
          <el-input v-model="createForm.keyword" placeholder="请输入关键词"></el-input>
        </el-form-item>

        <el-form-item label="商品名称" required v-if="createForm.isPositive">
          <el-input v-model="createForm.productName" placeholder="请输入商品名称"></el-input>
        </el-form-item>

        <el-row :gutter="20" v-if="createForm.isPositive">
          <el-col :span="12">
            <el-form-item label="商品链接" required>
              <el-input v-model="createForm.productLink" placeholder="请输入商品链接"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品图片">
              <el-input v-model="createForm.productImage" placeholder="请输入商品图片URL"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20" v-if="createForm.isPositive">
          <el-col :span="12">
            <el-form-item label="店铺" required>
              <el-input v-model="createForm.shop" placeholder="请输入店铺名称"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="售价(美元)" required>
              <el-input-number v-model="createForm.price" :precision="2" style="width: 100%;" placeholder="请输入售价"></el-input-number>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="评论类型">
          <el-select v-model="createForm.isPositive" placeholder="请选择评论类型" style="width: 100%;">
            <el-option label="好评" :value="true"></el-option>
            <el-option label="差评" :value="false"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="评论标题" required v-if="!createForm.isPositive">
          <el-input v-model="createForm.reviewTitle" placeholder="请输入评论标题"></el-input>
        </el-form-item>

        <el-form-item label="评论内容" required v-if="!createForm.isPositive">
          <el-input type="textarea" v-model="createForm.reviewContent" :rows="3" placeholder="请输入评论内容"></el-input>
        </el-form-item>

        <template v-if="createForm.isPositive">
          <el-divider content-position="left">上评要求</el-divider>

          <el-row :gutter="15">
            <el-col :span="8">
              <el-form-item label="免评">
                <el-input-number v-model="createForm.freeReview" :min="0" style="width: 100%;"></el-input-number>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="点星">
                <el-input-number v-model="createForm.starReview" :min="0" style="width: 100%;"></el-input-number>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="文字">
                <el-input-number v-model="createForm.textReview" :min="0" style="width: 100%;"></el-input-number>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="15">
            <el-col :span="8">
              <el-form-item label="图片">
                <el-input-number v-model="createForm.imageReview" :min="0" style="width: 100%;"></el-input-number>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="视频">
                <el-input-number v-model="createForm.videoReview" :min="0" style="width: 100%;"></el-input-number>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="Feedback">
                <el-input-number v-model="createForm.feedbackReview" :min="0" style="width: 100%;"></el-input-number>
              </el-form-item>
            </el-col>
          </el-row>
        </template>
        
        <el-form-item label="状态">
          <el-input :value="getStatusText(createForm.status)" disabled></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createCeping" :loading="loading">创建</el-button>
      </div>
    </el-dialog>

    <!-- 已放单状态对话框 -->
    <el-dialog title="修改为已放单" v-model="releasedDialogVisible" width="600px">
      <div class="released-form-container">
        <div class="order-input-section">
          <h4>添加订单</h4>
          <el-form label-width="100px">
            <el-form-item label="订单号" required>
              <el-input v-model="releasedOrderNumber" placeholder="请输入订单号"></el-input>
            </el-form-item>
            <el-form-item label="订单截图">
              <input type="file" accept="image/*" style="display: none;" ref="releasedImageInput" @change="handleReleasedImageSelect">
              <el-button @click="$refs.releasedImageInput.click()" type="primary">选择图片</el-button>
              <div v-if="releasedOrderScreenshot" style="margin-top: 10px;">
                <img :src="releasedOrderScreenshot" style="max-width: 100%; max-height: 150px; border-radius: 4px;">
              </div>
            </el-form-item>
            <el-form-item>
              <el-button type="success" @click="addOrderToList" :disabled="!releasedOrderNumber.trim()">添加订单</el-button>
            </el-form-item>
          </el-form>
        </div>
        <div class="order-list-section">
          <h4>订单列表 ({{ releasedOrdersList.length }})</h4>
          <el-table :data="releasedOrdersList" border size="small" max-height="200">
            <el-table-column prop="orderNumber" label="订单号" min-width="150"></el-table-column>
            <el-table-column label="订单截图" width="100">
              <template #default="scope">
                <img v-if="scope.row.orderScreenshot" :src="scope.row.orderScreenshot" style="max-width: 60px; max-height: 60px;">
                <span v-else>--</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" fixed="right">
              <template #default="scope">
                <el-button type="danger" size="small" @click="removeOrderFromList(scope.$index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancelReleasedStatus">取消</el-button>
        <el-button type="primary" @click="confirmReleasedStatus" :loading="statusUpdating" :disabled="releasedOrdersList.length === 0">确定</el-button>
      </div>
    </el-dialog>

    <!-- 已完成状态对话框 -->
    <el-dialog title="修改为已完成" v-model="completedDialogVisible" width="500px">
      <el-form label-width="100px">
        <el-form-item label="评价链接" required>
          <el-input v-model="completedReviewLink" placeholder="请输入评价链接"></el-input>
        </el-form-item>
        <el-form-item label="评价截图">
          <input type="file" accept="image/*" style="display: none;" ref="completedImageInput" @change="handleCompletedImageSelect">
          <el-button @click="$refs.completedImageInput.click()" type="primary">选择图片</el-button>
          <div v-if="completedReviewScreenshot" style="margin-top: 10px;">
            <img :src="completedReviewScreenshot" style="max-width: 100%; max-height: 200px; border-radius: 4px;">
          </div>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancelCompletedStatus">取消</el-button>
        <el-button type="primary" @click="confirmCompletedStatus" :loading="statusUpdating">确定</el-button>
      </div>
    </el-dialog>

    <!-- 申请增加预算对话框 -->
    <el-dialog title="申请增加预算" v-model="budgetRequestDialogVisible" width="500px">
      <el-form label-width="100px">
        <el-form-item label="预算金额" required>
          <el-select v-model="budgetRequestAmount" placeholder="请选择预算金额" style="width: 100%;">
            <el-option label="5" :value="5"></el-option>
            <el-option label="10" :value="10"></el-option>
            <el-option label="15" :value="15"></el-option>
            <el-option label="20" :value="20"></el-option>
            <el-option label="25" :value="25"></el-option>
            <el-option label="30" :value="30"></el-option>
            <el-option label="35" :value="35"></el-option>
            <el-option label="40" :value="40"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input type="textarea" v-model="budgetRequestRemark" :rows="3" placeholder="请输入备注"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancelBudgetRequest">取消</el-button>
        <el-button type="primary" @click="confirmBudgetRequest" :loading="statusUpdating">确定</el-button>
      </div>
    </el-dialog>

    <!-- 订单列表弹窗 -->
    <el-dialog title="订单列表" v-model="orderDialogVisible" width="1100px">
      <el-table :data="currentOrderList" border style="width: 100%">
        <el-table-column label="序号" width="60">
          <template #default="scope">
            {{ scope.$index + 1 }}
          </template>
        </el-table-column>
        <el-table-column label="订单号" min-width="160">
          <template #default="scope">
            <inline-edit
              :value="scope.row.orderNumber"
              :display-value="scope.row.orderNumber || '--'"
              @save="(val) => handleOrderFieldChange(scope.row, 'orderNumber', val)"
            ></inline-edit>
          </template>
        </el-table-column>
        <el-table-column label="订单截图" width="100">
          <template #default="scope">
            <div v-if="scope.row.orderScreenshot && scope.row.orderScreenshot.trim()" class="order-screenshot">
              <img :src="scope.row.orderScreenshot" alt="订单截图" class="screenshot-thumbnail">
            </div>
            <span v-else>--</span>
          </template>
        </el-table-column>
        <el-table-column label="评价链接" min-width="160">
          <template #default="scope">
            <inline-edit
              :value="scope.row.reviewLink"
              :display-value="scope.row.reviewLink || '--'"
              @save="(val) => handleOrderFieldChange(scope.row, 'reviewLink', val)"
            ></inline-edit>
          </template>
        </el-table-column>
        <el-table-column label="评价截图" width="100">
          <template #default="scope">
            <div v-if="scope.row.reviewScreenshot && scope.row.reviewScreenshot.trim()" class="review-screenshot">
              <img :src="scope.row.reviewScreenshot" alt="评价截图" class="screenshot-thumbnail">
            </div>
            <span v-else>--</span>
          </template>
        </el-table-column>
        <el-table-column label="本金" width="120">
          <template #default="scope">
            <inline-edit
              :value="scope.row.principal"
              :display-value="scope.row.principal != null ? scope.row.principal : '--'"
              type="text"
              placeholder="输入本金"
              @save="(val) => handleOrderFieldChange(scope.row, 'principal', val)"
            ></inline-edit>
          </template>
        </el-table-column>
        <el-table-column label="汇率" width="100">
          <template #default="scope">
            <inline-edit
              :value="scope.row.exchangeRate"
              :display-value="formatExchangeRate(scope.row.exchangeRate)"
              type="text"
              placeholder="输入汇率"
              @save="(val) => handleOrderFieldChange(scope.row, 'exchangeRate', val)"
            ></inline-edit>
          </template>
        </el-table-column>
        <el-table-column label="PP后价格" width="140">
          <template #default="scope">
            {{ calculatePpPrice(scope.row) }}
          </template>
        </el-table-column>
        <el-table-column label="佣金" width="100">
          <template #default="scope">
            <inline-edit
              :value="scope.row.commission"
              :display-value="scope.row.commission != null ? scope.row.commission : '--'"
              type="text"
              placeholder="输入佣金"
              @save="(val) => handleOrderFieldChange(scope.row, 'commission', val)"
            ></inline-edit>
          </template>
        </el-table-column>
        <el-table-column label="费用明细" min-width="280">
          <template #default="scope">
            <div class="expense-detail">
              <div v-if="getExpenseDetailParts(scope.row).ppPrice" class="expense-item">
                {{ getExpenseDetailParts(scope.row).ppPrice }}
              </div>
              <div v-if="getExpenseDetailParts(scope.row).sum" class="expense-item">
                {{ getExpenseDetailParts(scope.row).sum }}
              </div>
              <div v-if="!getExpenseDetailParts(scope.row).ppPrice && !getExpenseDetailParts(scope.row).sum" class="expense-empty">
                --
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="140">
          <template #default="scope">
            <el-select 
              :value="String(scope.row.status || '0')" 
              size="mini"
              @change="(val) => handleOrderStatusChange(scope.row, val)"
            >
              <el-option label="已放单" value="0"></el-option>
              <el-option 
                label="已完成"
                value="1" 
                :disabled="!scope.row.reviewLink || !scope.row.reviewLink.trim()"
              ></el-option>
            </el-select>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 订单已完成状态对话框 -->
    <el-dialog title="设置为已完成" v-model="orderCompletedDialogVisible" width="500px">
      <el-form label-width="100px">
        <el-form-item label="评价链接" required>
          <el-input v-model="orderCompletedReviewLink" placeholder="请输入评价链接"></el-input>
        </el-form-item>
        <el-form-item label="评价截图">
          <input type="file" accept="image/*" style="display: none;" ref="orderCompletedImageInput" @change="handleOrderCompletedImageSelect">
          <el-button @click="$refs.orderCompletedImageInput.click()" type="primary">选择图片</el-button>
          <div v-if="orderCompletedReviewScreenshot" style="margin-top: 10px;">
            <img :src="orderCompletedReviewScreenshot" style="max-width: 100%; max-height: 200px; border-radius: 4px;">
          </div>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancelOrderCompletedStatus">取消</el-button>
        <el-button type="primary" @click="confirmOrderCompletedStatus" :loading="statusUpdating">确定</el-button>
      </div>
    </el-dialog>

    <!-- 管理员修改预算对话框 -->
    <el-dialog title="修改预算" v-model="budgetEditDialogVisible" width="500px">
      <el-form label-width="100px">
        <el-form-item label="预算金额" required>
          <el-select v-model="budgetEditAmount" placeholder="请选择预算金额" style="width: 100%;">
            <el-option label="5" :value="5"></el-option>
            <el-option label="10" :value="10"></el-option>
            <el-option label="15" :value="15"></el-option>
            <el-option label="20" :value="20"></el-option>
            <el-option label="25" :value="25"></el-option>
            <el-option label="30" :value="30"></el-option>
            <el-option label="35" :value="35"></el-option>
            <el-option label="40" :value="40"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancelBudgetEdit">取消</el-button>
        <el-button type="primary" @click="confirmBudgetEdit">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import cepingMixin from './index'
import InlineEdit from '@/components/InlineEdit.vue'

export default {
  name: 'CepingView',
  mixins: [cepingMixin],
  components: {
    InlineEdit
  }
}
</script>

<style scoped>
@import './index.css';
</style>



