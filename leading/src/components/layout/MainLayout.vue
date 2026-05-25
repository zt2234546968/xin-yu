<script setup lang="ts">
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Goods,
  HomeFilled,
  Money,
  Operation,
  Tickets,
  User,
  UserFilled
} from "@element-plus/icons-vue";

const route = useRoute();
const router = useRouter();

const username = computed(() => {
  const userText = localStorage.getItem("user");
  if (!userText) return "用户";

  try {
    const user = JSON.parse(userText);
    return user.realName || user.phone || "用户";
  } catch {
    return "用户";
  }
});

const activeMenu = computed(() => route.path);

async function handleCommand(command: string) {
  if (command !== "logout") return;

  await ElMessageBox.confirm("确定要退出登录吗？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning"
  });

  localStorage.removeItem("token");
  localStorage.removeItem("user");
  ElMessage.success("退出登录成功");
  await router.push("/login");
}
</script>

<template>
  <el-container class="full-height">
    <el-header>
      <div class="header-content">
        <h1>跨境电商管理系统</h1>
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            {{ username }}
            <el-icon><Operation /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <el-container>
      <el-aside width="220px">
        <el-menu :default-active="activeMenu" class="el-menu-vertical-demo" router>
          <el-menu-item index="/home">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-sub-menu index="/home/taskCenter">
            <template #title>
              <el-icon><Goods /></el-icon>
              <span>任务中心</span>
            </template>
            <el-menu-item index="/home/taskCenter">直评</el-menu-item>
            <el-menu-item index="/home/taskCenter/ceping">测评</el-menu-item>
            <el-menu-item index="/home/taskCenter/vp-negative">VP 差评</el-menu-item>
            <el-menu-item index="/home/taskCenter/counter-adult">反击成人</el-menu-item>
            <el-menu-item index="/home/taskCenter/counter-split-variant">反击拆变体</el-menu-item>
            <el-menu-item index="/home/taskCenter/counter-copyright-image">反击版权图片</el-menu-item>
            <el-menu-item index="/home/taskCenter/counter-authenticity-complaint">反击真实性投诉</el-menu-item>
            <el-menu-item index="/home/taskCenter/counter-authenticity-vp-negative">反击真实性 VP 差评</el-menu-item>
            <el-menu-item index="/home/taskCenter/counter-product-safety">反击商品安全投诉</el-menu-item>
            <el-menu-item index="/home/taskCenter/counter-dog">反击变狗</el-menu-item>
            <el-menu-item index="/home/taskCenter/buyer-show">买家秀</el-menu-item>
            <el-menu-item index="/home/taskCenter/other-business">其他业务需求</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/home/invitationCode">
            <el-icon><Tickets /></el-icon>
            <span>邀请码</span>
          </el-menu-item>
          <el-menu-item index="/home/userManage">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-sub-menu index="/home/financeCenter">
            <template #title>
              <el-icon><Money /></el-icon>
              <span>财务中心</span>
            </template>
            <el-menu-item index="/home/financeCenter/pendingOrder">待结算订单</el-menu-item>
            <el-menu-item index="/home/financeCenter/settledOrder">已结算订单</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/home/profile">
            <el-icon><UserFilled /></el-icon>
            <span>个人中心</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main class="full-height">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style>
@import "./index.css";
</style>
