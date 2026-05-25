<template>
  <el-card class="management-card">
    <template #header>
      <div class="card-header-row">
        <div class="card-header-main">
          <span class="card-title">邀请码管理</span>
          <div class="card-subtitle">为新客户或协作账号发放注册入口，备注可用于记录公司来源</div>
        </div>
        <el-button type="primary" @click="handleGenerate">
          <el-icon><Plus /></el-icon>
          <span>生成邀请码</span>
        </el-button>
      </div>
    </template>

    <div class="table-shell">
      <el-table
        :data="invitationCodeList"
        style="width: 100%"
        v-loading="loading"
        empty-text="暂无邀请码，点击右上角生成"
      >
        <el-table-column prop="code" label="邀请码" width="220">
          <template #default="scope">
            <span class="code-cell">{{ scope.row.code }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="公司备注" min-width="360">
          <template #default="scope">
            <el-input
              v-model="scope.row.remark"
              placeholder="请输入备注"
              @blur="handleRemarkChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="220">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
      </el-table>
    </div>
  </el-card>
</template>

<script>
import { Plus } from '@element-plus/icons-vue'
import invitationCodeMixin from './index'

export default {
  name: 'InvitationCodeView',
  components: {
    Plus
  },
  mixins: [invitationCodeMixin]
}
</script>

<style scoped>
@import './index.css';
</style>
