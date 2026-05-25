import { createRouter, createWebHistory, type RouteRecordRaw } from "vue-router";

const routes: RouteRecordRaw[] = [
  { path: "/", redirect: "/login" },
  {
    path: "/login",
    name: "login",
    component: () => import("@/views/login/index.vue"),
    meta: { requiresAuth: false }
  },
  {
    path: "/home",
    component: () => import("@/components/layout/MainLayout.vue"),
    meta: { requiresAuth: true },
    children: [
      { path: "", name: "home", component: () => import("@/views/home/index.vue") },
      { path: "invitationCode", name: "invitationCode", component: () => import("@/views/invitationCode/index.vue") },
      { path: "userManage", name: "userManage", component: () => import("@/views/userManage/index.vue") },
      { path: "profile", name: "profile", component: () => import("@/views/profile/index.vue") },
      { path: "taskCenter", name: "taskCenter", component: () => import("@/views/taskCenter/zhiping/index.vue") },
      { path: "taskCenter/ceping", name: "ceping", component: () => import("@/views/taskCenter/ceping/index.vue") },
      {
        path: "taskCenter/vp-negative",
        name: "vp-negative",
        component: () => import("@/views/taskCenter/common/MarketplaceTaskPage.vue"),
        props: { taskType: "vp_negative", title: "VP 差评管理", taskTypeLabel: "评论治理" }
      },
      {
        path: "taskCenter/counter-adult",
        name: "counter-adult",
        component: () => import("@/views/taskCenter/common/MarketplaceTaskPage.vue"),
        props: { taskType: "counter_adult", title: "反击成人管理", taskTypeLabel: "类目误判申诉" }
      },
      {
        path: "taskCenter/counter-split-variant",
        name: "counter-split-variant",
        component: () => import("@/views/taskCenter/common/MarketplaceTaskPage.vue"),
        props: { taskType: "counter_split_variant", title: "反击拆变体管理", taskTypeLabel: "变体关系恢复" }
      },
      {
        path: "taskCenter/counter-copyright-image",
        name: "counter-copyright-image",
        component: () => import("@/views/taskCenter/common/MarketplaceTaskPage.vue"),
        props: { taskType: "counter_copyright_image", title: "反击版权图片管理", taskTypeLabel: "版权投诉申诉" }
      },
      {
        path: "taskCenter/counter-authenticity-complaint",
        name: "counter-authenticity-complaint",
        component: () => import("@/views/taskCenter/common/MarketplaceTaskPage.vue"),
        props: { taskType: "counter_authenticity_complaint", title: "反击真实性投诉管理", taskTypeLabel: "真实性投诉申诉" }
      },
      {
        path: "taskCenter/counter-authenticity-vp-negative",
        name: "counter-authenticity-vp-negative",
        component: () => import("@/views/taskCenter/common/MarketplaceTaskPage.vue"),
        props: { taskType: "counter_authenticity_vp_negative", title: "反击真实性 VP 差评管理", taskTypeLabel: "真实性评论治理" }
      },
      {
        path: "taskCenter/counter-product-safety",
        name: "counter-product-safety",
        component: () => import("@/views/taskCenter/common/MarketplaceTaskPage.vue"),
        props: { taskType: "counter_product_safety", title: "反击商品安全投诉管理", taskTypeLabel: "商品安全申诉" }
      },
      {
        path: "taskCenter/counter-dog",
        name: "counter-dog",
        component: () => import("@/views/taskCenter/common/MarketplaceTaskPage.vue"),
        props: { taskType: "counter_dog", title: "反击变狗管理", taskTypeLabel: "目录劫持治理" }
      },
      {
        path: "taskCenter/buyer-show",
        name: "buyer-show",
        component: () => import("@/views/taskCenter/common/MarketplaceTaskPage.vue"),
        props: { taskType: "buyer_show", title: "买家秀管理", taskTypeLabel: "内容素材运营" }
      },
      {
        path: "taskCenter/other-business",
        name: "other-business",
        component: () => import("@/views/taskCenter/common/MarketplaceTaskPage.vue"),
        props: { taskType: "other_business", title: "其他业务需求管理", taskTypeLabel: "临时业务" }
      },
      { path: "financeCenter/pendingOrder", name: "pendingOrder", component: () => import("@/views/financeCenter/pendingOrder/index.vue") },
      { path: "financeCenter/settledOrder", name: "settledOrder", component: () => import("@/views/financeCenter/settledOrder/index.vue") }
    ]
  }
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
});

router.beforeEach((to) => {
  const needsAuth = to.matched.some((record) => record.meta.requiresAuth);
  const token = localStorage.getItem("token");

  if (needsAuth && !token) {
    return { path: "/login", query: { redirect: to.fullPath } };
  }

  if (to.name === "login" && token) {
    return { path: "/home" };
  }

  return true;
});

export default router;
