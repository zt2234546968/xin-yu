/**
 * 主布局组件
 * @description 跨境电商管理系统的整体布局框架，包含顶部导航和侧边栏菜单
 */

export default {
  name: 'MainLayout',

  data() {
    return {
      // 当前登录用户显示名称
      username: '用户',
      // 当前选中的菜单项路径
      activeMenu: '/home'
    }
  },

  mounted() {
    // 从localStorage获取当前用户信息，用于显示用户名
    const userStr = localStorage.getItem('user')
    if (userStr) {
      try {
        const user = JSON.parse(userStr)
        // 优先显示真实姓名，其次显示手机号
        this.username = user.realName || user.phone || '用户'
      } catch (e) {
        console.error('解析用户信息失败', e)
      }
    }
  },

  watch: {
    // 监听路由变化，更新当前选中的菜单项
    $route: {
      handler(to) {
        this.activeMenu = to.path
      },
      // 立即执行一次，确保刷新页面时菜单状态正确
      immediate: true
    }
  },

  methods: {
    /**
     * 处理下拉菜单命令
     * @param {String} command - 命令标识符，当前仅支持 'logout'(退出登录)
     */
    handleCommand(command) {
      if (command === 'logout') {
        this.$confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          // 清除本地存储的token和用户信息
          localStorage.removeItem('token')
          localStorage.removeItem('user')
          this.$message.success('退出登录成功')
          // 跳转到登录页面
          this.$router.push('/login')
        }).catch(() => {
          // 用户取消操作，不做任何处理
        })
      }
    }
  }
}