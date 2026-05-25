<template>
  <el-card class="management-card">
    <template #header>
      <div class="card-header-row">
        <div class="card-header-main">
          <span class="card-title">用户管理</span>
          <div class="card-subtitle">查看当前系统账号、角色和注册时间</div>
        </div>
      </div>
    </template>

    <div class="table-shell">
      <el-table :data="userList" style="width: 100%" v-loading="userLoading" empty-text="暂无用户数据">
        <el-table-column prop="realName" label="姓名" width="140" />
        <el-table-column prop="phone" label="手机号" width="170" />
        <el-table-column prop="wechat" label="微信号" width="170" />
        <el-table-column prop="role" label="角色" width="150">
          <template #default="scope">
            <el-tag size="small" effect="light">
              {{ scope.row.role ? scope.row.role.roleName : '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
      </el-table>
    </div>
  </el-card>
</template>

<script>
import userManageMixin from './index'

export default {
  name: 'UserManageView',
  mixins: [userManageMixin]
}
</script>

<style scoped>
@import './index.css';
</style>
