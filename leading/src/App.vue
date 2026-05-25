<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup lang="ts">
</script>

<style>
:root {
  --app-bg: #f3f6fb;
  --app-surface: #ffffff;
  --app-surface-soft: #f8fafc;
  --app-border: #dbe4ef;
  --app-text: #172033;
  --app-muted: #64748b;
  --app-primary: #145c72;
  --app-primary-hover: #0f4f63;
  --app-accent: #1aa6a6;
  --app-accent-soft: #e6f6f5;
  --app-warning: #d97706;
  --app-success: #17803d;
  --app-danger: #dc2626;
  --app-shadow: 0 10px 30px rgba(15, 35, 55, 0.08);

  --el-color-primary: var(--app-primary);
  --el-color-primary-light-3: #2d7f91;
  --el-color-primary-light-5: #6ea9b5;
  --el-color-primary-light-7: #a8cdd5;
  --el-color-primary-light-8: #c6dfe5;
  --el-color-primary-light-9: #e8f3f5;
  --el-color-primary-dark-2: var(--app-primary-hover);
  --el-color-success: var(--app-success);
  --el-color-warning: var(--app-warning);
  --el-color-danger: var(--app-danger);
  --el-text-color-primary: var(--app-text);
  --el-text-color-regular: #334155;
  --el-text-color-secondary: var(--app-muted);
  --el-border-color: var(--app-border);
  --el-border-color-light: #e4ebf3;
  --el-fill-color-light: var(--app-surface-soft);
  --el-border-radius-base: 6px;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: var(--app-text);
  background:
    linear-gradient(180deg, #edf6f8 0, #f7f9fc 280px, var(--app-bg) 100%);
}

#app {
  height: 100vh;
}

.el-main {
  background: var(--app-bg);
}

.el-card,
.el-dialog,
.el-drawer {
  border-radius: 8px;
  border-color: var(--app-border);
  box-shadow: var(--app-shadow);
}

.el-card__header {
  padding: 20px 24px 14px;
  border-bottom-color: var(--app-border);
  color: var(--app-text);
  font-weight: 600;
}

.el-card__body {
  padding: 20px 24px 24px;
}

.el-table {
  border-radius: 8px;
  overflow: hidden;
  color: #334155;
  --el-table-border-color: var(--app-border);
  --el-table-header-bg-color: var(--app-surface-soft);
  --el-table-row-hover-bg-color: #eef7f8;
}

.el-table th.el-table__cell {
  background: var(--app-surface-soft) !important;
  color: #475569;
  font-weight: 600;
}

.el-button {
  border-radius: 6px;
  font-weight: 500;
}

.el-button--primary {
  background: var(--app-primary);
  border-color: var(--app-primary);
}

.el-button--primary:hover,
.el-button--primary:focus {
  background: var(--app-primary-hover);
  border-color: var(--app-primary-hover);
}

.el-input__wrapper,
.el-select__wrapper,
.el-textarea__inner {
  border-radius: 6px;
  box-shadow: 0 0 0 1px var(--app-border) inset;
}

.el-input__wrapper:hover,
.el-select__wrapper:hover,
.el-textarea__inner:hover {
  box-shadow: 0 0 0 1px #aac6d0 inset;
}

.el-tag {
  border-radius: 5px;
}

.header-container,
.table-toolbar,
.search-form {
  background: var(--app-surface);
  border: 1px solid var(--app-border);
  border-radius: 8px;
  box-shadow: var(--app-shadow);
}

.header-container h2,
.page-title,
.section-title {
  color: var(--app-text) !important;
  letter-spacing: 0;
}

.card-title {
  color: var(--app-text);
  font-size: 18px;
  font-weight: 800;
}

.card-subtitle {
  margin-top: 6px;
  color: var(--app-muted);
  font-size: 13px;
  font-weight: 400;
}

.card-header-row,
.clearfix {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.card-header-main {
  min-width: 0;
}

[class^="el-icon-"],
[class*=" el-icon-"] {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 1em;
  height: 1em;
  line-height: 1;
  font-style: normal;
}

.el-icon-star-on::before { content: "★"; }
.el-icon-star-off::before { content: "☆"; }
.el-icon-edit::before { content: "E"; }
.el-icon-document::before { content: "D"; }
.el-icon-link::before { content: ">"; }
.el-icon-picture-outline::before { content: "I"; }
.el-icon-video-play::before { content: ">"; }
.el-icon-phone-outline::before { content: "P"; }
.el-icon-search::before { content: "S"; }
.el-icon-plus::before { content: "+"; }
.el-icon-view::before { content: "V"; }
.el-icon-time::before { content: "T"; }
.el-icon-top-right::before { content: ">"; }
.el-icon-shopping-cart-2::before { content: "C"; }
.el-icon-goods::before { content: "G"; }
.el-icon-monitor::before { content: "M"; }
.el-icon-chat-dot-round::before,
.el-icon-chat-line-square::before { content: "Q"; }
.el-icon-s-claim::before,
.el-icon-s-help::before,
.el-icon-question::before { content: "?"; }
.el-icon-shield::before { content: "#"; }
.el-icon-thumb::before { content: "+"; }
.el-icon-trophy::before { content: "^"; }
</style>
