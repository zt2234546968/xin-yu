<template>
  <div class="marketplace-task-page">
    <div class="page-toolbar">
      <div>
        <h2>{{ title }}</h2>
        <div class="page-subtitle">{{ taskTypeLabel }} / 运营任务</div>
      </div>
      <div class="toolbar-actions">
        <el-input
          v-model="filters.keyword"
          clearable
          placeholder="搜索编号 / ASIN / 店铺"
          class="search-input"
          @keyup.enter="applyFilters"
          @clear="applyFilters"
        />
        <el-select v-model="filters.status" clearable placeholder="状态" class="status-filter" @change="applyFilters">
          <el-option label="等待提交" value="0" />
          <el-option label="等待反馈" value="1" />
          <el-option label="已完成" value="2" />
          <el-option label="已取消" value="3" />
        </el-select>
        <el-button @click="fetchTaskList">刷新</el-button>
        <el-button type="primary" @click="openCreateDialog">创建任务</el-button>
      </div>
    </div>

    <div class="task-metrics">
      <div v-for="item in metrics" :key="item.label" class="metric-item">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
      </div>
    </div>

    <el-table v-loading="loading" :data="taskList" class="task-table" stripe>
      <el-table-column label="序号" width="70" fixed="left">
        <template #default="scope">{{ scope.$index + 1 }}</template>
      </el-table-column>
      <el-table-column prop="code" label="任务编号" width="110" fixed="left" />
      <el-table-column label="任务信息" min-width="440">
        <template #default="scope">
          <div class="task-cell">
            <img :src="scope.row.taskImage || '/test.jpg'" class="task-thumb" alt="任务图片">
            <div class="task-main">
              <div class="task-title-line">
                <strong>{{ scope.row.productName || scope.row.asin || '-' }}</strong>
                <el-tag size="small" :type="priorityTag(scope.row.priority)">{{ priorityText(scope.row.priority) }}</el-tag>
              </div>
              <div class="task-meta-line">
                <span>{{ scope.row.asin || '-' }}</span>
                <span>{{ scope.row.shop || '未填写店铺' }}</span>
                <span>{{ scope.row.quantity || 1 }} 件</span>
              </div>
              <a v-if="scope.row.productLink" :href="scope.row.productLink" target="_blank" class="task-link">
                {{ scope.row.productLink }}
              </a>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="国家" width="110">
        <template #default="scope">
          <span class="country-cell">
            <img v-if="scope.row.countryImage" :src="scope.row.countryImage" alt="国旗">
            {{ scope.row.countryName || '-' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="类型 / 目标" min-width="220">
        <template #default="scope">
          <div class="compact-stack">
            <strong>{{ scope.row.issueType || '-' }}</strong>
            <span>{{ scope.row.targetAction || '-' }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="scope">
          <el-tag :type="statusTag(scope.row.status)" effect="light">{{ statusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="channel" label="渠道" width="150">
        <template #default="scope">{{ scope.row.channel || '-' }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="170" fixed="right">
        <template #default="scope">
          <el-button type="primary" size="small" @click="openDetail(scope.row)">详情</el-button>
          <el-button v-if="isSuperAdmin" type="danger" size="small" @click="deleteTask(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-drawer v-model="detailVisible" title="任务详情" size="620px">
      <div v-if="currentTask.id" class="task-drawer">
        <div class="drawer-summary">
          <div class="summary-top">
            <div>
              <span class="field-label">任务编号</span>
              <strong>{{ currentTask.code }}</strong>
            </div>
            <el-select
              v-if="isSuperAdmin"
              :model-value="currentTask.status"
              class="drawer-status-select"
              :disabled="statusUpdating"
              @change="handleStatusChange"
            >
              <el-option label="等待提交" value="0" />
              <el-option label="等待反馈" value="1" />
              <el-option label="已完成" value="2" />
              <el-option label="已取消" value="3" />
            </el-select>
            <el-tag v-else :type="statusTag(currentTask.status)" effect="light">{{ statusText(currentTask.status) }}</el-tag>
          </div>
          <div class="summary-product">
            <img :src="currentTask.taskImage || '/test.jpg'" alt="任务图片">
            <div>
              <span class="field-label">商品</span>
              <h3>{{ currentTask.productName || currentTask.asin || '-' }}</h3>
              <p>{{ currentTask.asin || '-' }}</p>
            </div>
          </div>
          <div class="summary-tags">
            <span>{{ currentTask.countryName || '未选择国家' }}</span>
            <span>{{ priorityText(currentTask.priority) }}</span>
            <span>{{ currentTask.quantity || 1 }} 件</span>
            <span>{{ currentTask.channel || '暂无渠道' }}</span>
          </div>
        </div>

        <section class="detail-card">
          <div class="detail-card-title">处理信息</div>
          <div class="detail-grid">
            <div><span>问题类型</span><strong>{{ currentTask.issueType || '-' }}</strong></div>
            <div><span>处理目标</span><strong>{{ currentTask.targetAction || '-' }}</strong></div>
            <div><span>关键词</span><strong>{{ currentTask.keyword || '-' }}</strong></div>
            <div><span>店铺</span><strong>{{ currentTask.shop || '-' }}</strong></div>
          </div>
          <p class="detail-text">{{ currentTask.claimReason || '暂无说明' }}</p>
        </section>

        <section class="detail-card">
          <div class="detail-card-title">证据与链接</div>
          <div class="link-row">
            <span>商品链接</span>
            <a v-if="currentTask.productLink" :href="currentTask.productLink" target="_blank">打开商品</a>
            <strong v-else>-</strong>
          </div>
          <div class="link-row">
            <span>证据链接</span>
            <a v-if="currentTask.evidenceLink" :href="currentTask.evidenceLink" target="_blank">打开证据</a>
            <strong v-else>-</strong>
          </div>
          <img :src="currentTask.evidenceImage || currentTask.taskImage || '/test.jpg'" class="evidence-image" alt="证据图片">
        </section>

        <section class="detail-card">
          <div class="detail-card-title">反馈结果</div>
          <div class="link-row">
            <span>反馈链接</span>
            <a v-if="currentTask.feedbackLink" :href="currentTask.feedbackLink" target="_blank">打开反馈</a>
            <strong v-else>暂无</strong>
          </div>
          <img v-if="currentTask.feedbackImage" :src="currentTask.feedbackImage" class="evidence-image" alt="反馈图片">
          <p v-if="currentTask.remark" class="detail-text">{{ currentTask.remark }}</p>
        </section>
      </div>
    </el-drawer>

    <el-dialog v-model="createVisible" :title="`创建${title.replace('管理', '')}`" width="720px">
      <el-form :model="createForm" label-width="96px" class="create-form">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="国家">
              <el-select v-model="createForm.countryId" placeholder="请选择国家" filterable>
                <el-option v-for="country in countryList" :key="country.id" :label="country.countryName" :value="country.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级">
              <el-select v-model="createForm.priority">
                <el-option label="普通" value="NORMAL" />
                <el-option label="高" value="HIGH" />
                <el-option label="紧急" value="URGENT" />
                <el-option label="低" value="LOW" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="ASIN">
              <el-input v-model="createForm.asin" placeholder="请输入 ASIN" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数量">
              <el-input-number v-model="createForm.quantity" :min="1" :max="999" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="商品名称">
          <el-input v-model="createForm.productName" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品链接">
          <el-input v-model="createForm.productLink" placeholder="请输入商品或 Case 链接" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="店铺">
              <el-input v-model="createForm.shop" placeholder="请输入店铺" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关键词">
              <el-input v-model="createForm.keyword" placeholder="请输入关键词" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="问题类型">
              <el-input v-model="createForm.issueType" :placeholder="issuePlaceholder" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="处理目标">
              <el-input v-model="createForm.targetAction" :placeholder="targetPlaceholder" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="说明">
          <el-input v-model="createForm.claimReason" type="textarea" :rows="4" placeholder="填写任务背景、处理要求、证据说明" />
        </el-form-item>
        <el-form-item label="证据链接">
          <el-input v-model="createForm.evidenceLink" placeholder="请输入证据链接" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="createTask">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="feedbackVisible" title="填写完成反馈" width="520px">
      <el-form label-width="88px">
        <el-form-item label="反馈链接">
          <el-input v-model="feedbackForm.feedbackLink" placeholder="请输入反馈链接" />
        </el-form-item>
        <el-form-item label="反馈图片">
          <el-input v-model="feedbackForm.feedbackImage" placeholder="暂无图床，默认可填写 /test.jpg" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="feedbackVisible = false">取消</el-button>
        <el-button type="primary" :loading="statusUpdating" @click="confirmComplete">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'

interface CountryOption {
  id: string
  countryName: string
  flagImage: string
}

interface MarketplaceTask {
  id?: string
  taskType: string
  code?: string
  countryId?: string
  country?: CountryOption
  countryName?: string
  countryImage?: string
  asin?: string
  productName?: string
  productLink?: string
  taskImage?: string
  shop?: string
  keyword?: string
  quantity?: number
  priority?: string
  issueType?: string
  targetAction?: string
  claimReason?: string
  evidenceLink?: string
  evidenceImage?: string
  channel?: string
  status?: string
  feedbackLink?: string
  feedbackImage?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

const props = withDefaults(defineProps<{
  taskType: string
  title: string
  taskTypeLabel?: string
  issuePlaceholder?: string
  targetPlaceholder?: string
}>(), {
  taskTypeLabel: '跨境业务',
  issuePlaceholder: '例如：版权投诉、变体拆分、VP 差评',
  targetPlaceholder: '例如：申诉恢复、删除评论、合并变体'
})

const loading = ref(false)
const submitting = ref(false)
const detailVisible = ref(false)
const createVisible = ref(false)
const feedbackVisible = ref(false)
const statusUpdating = ref(false)
const pendingStatus = ref('')

const allTasks = ref<MarketplaceTask[]>([])
const taskList = ref<MarketplaceTask[]>([])
const countryList = ref<CountryOption[]>([])
const currentTask = ref<MarketplaceTask>({ taskType: props.taskType })

const filters = reactive({
  keyword: '',
  status: ''
})

const createForm = reactive<MarketplaceTask>(emptyCreateForm())
const feedbackForm = reactive({
  feedbackLink: '',
  feedbackImage: '/test.jpg'
})

const currentUser = computed(() => {
  const userStr = localStorage.getItem('user')
  if (!userStr) return {}
  try {
    return JSON.parse(userStr)
  } catch {
    return {}
  }
})

const isSuperAdmin = computed(() => currentUser.value?.role?.roleName === 'SUPER_ADMIN')

const metrics = computed(() => [
  { label: '全部', value: allTasks.value.length },
  { label: '等待提交', value: allTasks.value.filter(item => item.status === '0').length },
  { label: '等待反馈', value: allTasks.value.filter(item => item.status === '1').length },
  { label: '已完成', value: allTasks.value.filter(item => item.status === '2').length }
])

onMounted(() => {
  fetchCountries()
  fetchTaskList()
})

function emptyCreateForm(): MarketplaceTask {
  return {
    taskType: props.taskType,
    countryId: '10000000-0000-0000-0000-000000000001',
    asin: '',
    productName: '',
    productLink: '',
    taskImage: '/test.jpg',
    shop: '',
    keyword: '',
    quantity: 1,
    priority: 'NORMAL',
    issueType: '',
    targetAction: '',
    claimReason: '',
    evidenceLink: '',
    evidenceImage: '/test.jpg',
    status: '0'
  }
}

function resetCreateForm() {
  Object.assign(createForm, emptyCreateForm())
}

function formatDateTime(dateTime?: string) {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  if (Number.isNaN(date.getTime())) return dateTime
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`
}

function normalizeTask(item: MarketplaceTask): MarketplaceTask {
  return {
    ...item,
    countryName: item.country?.countryName || item.countryName || '',
    countryImage: item.country?.flagImage || item.countryImage || '',
    taskImage: item.taskImage || '/test.jpg',
    evidenceImage: item.evidenceImage || '/test.jpg',
    createTime: formatDateTime(item.createTime),
    updateTime: formatDateTime(item.updateTime)
  }
}

async function fetchCountries() {
  try {
    const response = await api.country.list()
    if (response.code === 200) {
      countryList.value = response.data
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取国家列表失败')
  }
}

async function fetchTaskList() {
  loading.value = true
  try {
    const response = await api.task.list(props.taskType)
    if (response.code === 200) {
      allTasks.value = response.data.map(normalizeTask)
      applyFilters()
    } else {
      ElMessage.error(response.message || '获取任务列表失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('获取任务列表失败')
  } finally {
    loading.value = false
  }
}

function applyFilters() {
  const keyword = filters.keyword.trim().toLowerCase()
  taskList.value = allTasks.value.filter(item => {
    const matchKeyword = !keyword || [item.code, item.asin, item.shop, item.productName]
      .some(value => String(value || '').toLowerCase().includes(keyword))
    const matchStatus = !filters.status || item.status === filters.status
    return matchKeyword && matchStatus
  })
}

function openCreateDialog() {
  resetCreateForm()
  createVisible.value = true
}

async function createTask() {
  if (!createForm.asin && !createForm.productName) {
    ElMessage.warning('请至少填写 ASIN 或商品名称')
    return
  }

  submitting.value = true
  try {
    const response = await api.task.create({ ...createForm, taskType: props.taskType })
    if (response.code === 200) {
      ElMessage.success('创建成功')
      createVisible.value = false
      fetchTaskList()
    } else {
      ElMessage.error(response.message || '创建失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('创建失败')
  } finally {
    submitting.value = false
  }
}

function openDetail(row: MarketplaceTask) {
  currentTask.value = { ...row }
  detailVisible.value = true
}

async function deleteTask(id?: string) {
  if (!id) return
  try {
    await ElMessageBox.confirm('确定删除这个任务吗？删除后不会在列表中显示。', '删除确认', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    const response = await api.task.delete(id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      fetchTaskList()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

async function handleStatusChange(status: string) {
  if (!currentTask.value.id) return
  if (status === '1') {
    const { value } = await ElMessageBox.prompt('请输入处理渠道', '进入等待反馈', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: currentTask.value.channel || ''
    })
    await updateFeedback(value, currentTask.value.feedbackLink, currentTask.value.feedbackImage)
    await updateStatus(status)
    return
  }
  if (status === '2') {
    pendingStatus.value = status
    feedbackForm.feedbackLink = currentTask.value.feedbackLink || ''
    feedbackForm.feedbackImage = currentTask.value.feedbackImage || '/test.jpg'
    feedbackVisible.value = true
    return
  }
  await updateStatus(status)
}

async function confirmComplete() {
  await updateFeedback(currentTask.value.channel, feedbackForm.feedbackLink, feedbackForm.feedbackImage)
  await updateStatus(pendingStatus.value || '2')
  feedbackVisible.value = false
}

async function updateStatus(status: string) {
  if (!currentTask.value.id) return
  statusUpdating.value = true
  try {
    const response = await api.task.updateStatus(currentTask.value.id, status)
    if (response.code === 200) {
      currentTask.value = normalizeTask(response.data)
      ElMessage.success('状态更新成功')
      fetchTaskList()
    } else {
      ElMessage.error(response.message || '状态更新失败')
    }
  } finally {
    statusUpdating.value = false
  }
}

async function updateFeedback(channel?: string, feedbackLink?: string, feedbackImage?: string) {
  if (!currentTask.value.id) return
  const response = await api.task.updateFeedback(currentTask.value.id, feedbackLink, feedbackImage, channel)
  if (response.code === 200) {
    currentTask.value = normalizeTask(response.data)
  } else {
    ElMessage.error(response.message || '反馈更新失败')
  }
}

function statusText(status?: string) {
  const map: Record<string, string> = {
    '0': '等待提交',
    '1': '等待反馈',
    '2': '已完成',
    '3': '已取消'
  }
  return map[status || ''] || status || '-'
}

function statusTag(status?: string) {
  const map: Record<string, 'info' | 'warning' | 'success' | 'danger'> = {
    '0': 'info',
    '1': 'warning',
    '2': 'success',
    '3': 'danger'
  }
  return map[status || ''] || 'info'
}

function priorityText(priority?: string) {
  const map: Record<string, string> = {
    LOW: '低',
    NORMAL: '普通',
    HIGH: '高',
    URGENT: '紧急'
  }
  return map[priority || 'NORMAL'] || priority || '普通'
}

function priorityTag(priority?: string) {
  const map: Record<string, 'info' | 'primary' | 'warning' | 'danger'> = {
    LOW: 'info',
    NORMAL: 'primary',
    HIGH: 'warning',
    URGENT: 'danger'
  }
  return map[priority || 'NORMAL'] || 'primary'
}
</script>

<style scoped>
.marketplace-task-page {
  padding: 20px 24px;
}

.page-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}

.page-toolbar h2 {
  margin: 0;
  color: var(--app-text);
  font-size: 20px;
}

.page-subtitle {
  margin-top: 6px;
  color: var(--app-text-soft);
  font-size: 13px;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.search-input {
  width: 260px;
}

.status-filter {
  width: 132px;
}

.task-metrics {
  display: grid;
  grid-template-columns: repeat(4, minmax(120px, 1fr));
  gap: 12px;
  margin-bottom: 14px;
}

.metric-item {
  padding: 14px 16px;
  background: #ffffff;
  border: 1px solid var(--app-border);
  border-radius: 8px;
  box-shadow: 0 8px 18px rgba(8, 37, 49, 0.04);
}

.metric-item span {
  display: block;
  color: var(--app-text-soft);
  font-size: 12px;
}

.metric-item strong {
  display: block;
  margin-top: 8px;
  color: var(--app-text);
  font-size: 24px;
}

.task-table {
  border: 1px solid var(--app-border);
  border-radius: 8px;
  overflow: hidden;
}

.task-cell {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.task-thumb {
  width: 72px;
  height: 72px;
  object-fit: cover;
  border-radius: 6px;
}

.task-main {
  min-width: 0;
}

.task-title-line {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.task-title-line strong,
.compact-stack strong {
  color: var(--app-text);
  font-size: 14px;
}

.task-meta-line {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 5px;
  color: var(--app-text-soft);
  font-size: 12px;
}

.task-link {
  display: block;
  max-width: 520px;
  overflow: hidden;
  color: var(--app-primary);
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-decoration: none;
}

.country-cell {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.country-cell img {
  width: 24px;
  height: 16px;
  object-fit: cover;
  border-radius: 2px;
}

.compact-stack {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.compact-stack span {
  color: var(--app-text-soft);
  font-size: 12px;
}

.task-drawer {
  padding: 0 18px 24px;
  background: var(--app-bg);
  min-height: 100%;
}

.drawer-summary,
.detail-card {
  background: #ffffff;
  border: 1px solid var(--app-border);
  border-radius: 8px;
  box-shadow: 0 10px 24px rgba(8, 37, 49, 0.05);
}

.drawer-summary {
  padding: 18px;
  margin-bottom: 14px;
}

.summary-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.summary-top strong {
  display: block;
  margin-top: 4px;
  color: var(--app-primary);
  font-size: 22px;
}

.field-label {
  color: var(--app-text-soft);
  font-size: 12px;
  font-weight: 600;
}

.drawer-status-select {
  width: 128px;
}

.summary-product {
  display: grid;
  grid-template-columns: 112px 1fr;
  gap: 14px;
  align-items: center;
  padding: 12px;
  background: rgba(20, 96, 112, 0.05);
  border-radius: 8px;
}

.summary-product img {
  width: 112px;
  height: 84px;
  object-fit: cover;
  border-radius: 6px;
}

.summary-product h3 {
  margin: 4px 0 6px;
  color: var(--app-text);
  font-size: 17px;
}

.summary-product p {
  margin: 0;
  color: var(--app-text-soft);
}

.summary-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.summary-tags span {
  padding: 6px 10px;
  color: var(--app-text-soft);
  background: rgba(20, 96, 112, 0.07);
  border-radius: 14px;
  font-size: 12px;
  font-weight: 600;
}

.detail-card {
  padding: 16px;
  margin-bottom: 14px;
}

.detail-card-title {
  margin-bottom: 12px;
  color: var(--app-text);
  font-weight: 700;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.detail-grid div,
.link-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px dashed var(--app-border);
}

.detail-grid span,
.link-row span {
  color: var(--app-text-soft);
  font-size: 13px;
}

.detail-grid strong,
.link-row strong {
  color: var(--app-text);
  font-size: 13px;
  text-align: right;
}

.link-row a {
  color: var(--app-primary);
  font-size: 13px;
  text-decoration: none;
}

.detail-text {
  margin: 12px 0 0;
  color: var(--app-text);
  line-height: 1.7;
}

.evidence-image {
  width: 100%;
  max-height: 240px;
  margin-top: 12px;
  object-fit: cover;
  border-radius: 8px;
}

.create-form .el-select,
.create-form .el-input,
.create-form .el-input-number {
  width: 100%;
}

@media (max-width: 1200px) {
  .page-toolbar,
  .toolbar-actions {
    align-items: stretch;
    flex-direction: column;
  }

  .search-input,
  .status-filter {
    width: 100%;
  }

  .task-metrics {
    grid-template-columns: repeat(2, minmax(120px, 1fr));
  }
}
</style>
