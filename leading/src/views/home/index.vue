<template>
  <div class="home-container">
    <div class="top-section">
      <el-card class="account-card">
        <div class="account-header">
          <span>账户信息</span>
          <span class="phone">{{ phone }}</span>
        </div>
        <div class="account-content">
          <div class="logo-section">
            <div class="logo-placeholder"></div>
          </div>
          <div class="points-section">
            <div class="point-item">
              <span>可用积分</span>
              <span class="point-value">{{ availablePoints }}</span>
            </div>
            <div class="point-item">
              <span>冻结积分</span>
              <span class="point-value">{{ frozenPoints }}</span>
            </div>
          </div>
        </div>
      </el-card>
      
      <el-card class="chart-card">
        <div class="chart-header">最近7天消耗</div>
        <div class="chart-placeholder"></div>
      </el-card>
      
      <el-card class="qr-card">
        <div class="qr-header">专属运营</div>
        <div class="qr-content">
          <div class="qr-placeholder"></div>
          <div class="qr-info">
            <p>联系电话</p>
            <p><i class="el-icon-phone-outline"></i>{{ contactPhone }}</p>
          </div>
        </div>
      </el-card>
    </div>
    
    <div class="bottom-section">
      <div class="task-grid">
        <div v-for="(item, index) in taskList" :key="index" class="task-card" @click="handleTaskClick(item)">
          <div class="task-icon" :style="{ backgroundColor: item.iconBg }">
            <i :class="item.icon" style="font-size: 28px;"></i>
          </div>
          <div class="task-name">{{ item.name }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import homeMixin from './index'

export default {
  name: 'HomeView',
  mixins: [homeMixin]
}
</script>

<style scoped>
@import './index.css';
</style>
