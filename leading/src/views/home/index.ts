/**
 * 首页模块
 * @description 跨境电商管理系统首页，展示系统基本信息
 */

export default {
  name: 'HomeView',
  data() {
    return {
      phone: '18530057887',
      availablePoints: 8,
      frozenPoints: 0,
      contactPhone: '13049819178',
      taskList: [
        { name: '添加我Ali亚马逊', icon: 'el-icon-shopping-cart-2', iconBg: '#f0f9ff', route: '' },
        { name: '添加米健康服务商', icon: 'el-icon-trophy', iconBg: '#f0f9ff', route: '' },
        { name: '添加关联联盟任务', icon: 'el-icon-link', iconBg: '#f0f9ff', route: '' },
        { name: '添加下拉联盟任务', icon: 'el-icon-document', iconBg: '#f0f9ff', route: '' },
        { name: '添加IM任务', icon: 'el-icon-chat-dot-round', iconBg: '#f0f9ff', route: '' },
        { name: '添加预算任务', icon: 'el-icon-shield', iconBg: '#f0f9ff', route: '' },
        { name: '添加测图A/Btest任务', icon: 'el-icon-thumb', iconBg: '#f0f9ff', route: '' },
        { name: '添加上评任务', icon: 'el-icon-chat-line-square', iconBg: '#f0f9ff', route: '' },
        { name: '添加无痕贴纸', icon: 'el-icon-video-play', iconBg: '#f9f9f9', route: '' },
        { name: '添加Question任务', icon: 'el-icon-question', iconBg: '#f9f9f9', route: '' },
        { name: '添加Answer任务', icon: 'el-icon-s-help', iconBg: '#f9f9f9', route: '' },
        { name: '添加跟监控任务', icon: 'el-icon-monitor', iconBg: '#f9f9f9', route: '' },
        { name: '添加单量提升任务', icon: 'el-icon-star-on', iconBg: '#f9f9f9', route: '' },
        { name: '添加关键词搜索任务', icon: 'el-icon-search', iconBg: '#f9f9f9', route: '' }
      ]
    }
  },
  methods: {
    handleTaskClick(item) {
      this.$message.info(`点击了${item.name}`)
    }
  }
}