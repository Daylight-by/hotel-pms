<template>
  <ContentWrap>
    <el-row :gutter="12" class="mb-3">
      <el-col :span="6"><el-card shadow="never"><div class="stat-label">今日上传</div><div class="stat-value">{{ stat.total || 0 }}</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="never"><div class="stat-label">成功</div><div class="stat-value success">{{ stat.success || 0 }}</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="never"><div class="stat-label">失败</div><div class="stat-value danger">{{ stat.failed || 0 }}</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="never"><div class="stat-label">成功率</div><div class="stat-value">{{ stat.successRate || 0 }}%</div></el-card></el-col>
    </el-row>
    <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px" class="-mb-15px">
      <el-form-item label="住客姓名" prop="guestName">
        <el-input v-model="queryParams.guestName" placeholder="姓名" clearable class="!w-160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="身份证" prop="idCard">
        <el-input v-model="queryParams.idCard" placeholder="身份证号" clearable class="!w-200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="动作" prop="actionType">
        <el-select v-model="queryParams.actionType" placeholder="全部" clearable class="!w-140px">
          <el-option label="入住登记" :value="10" />
          <el-option label="退房注销" :value="20" />
          <el-option label="换房" :value="30" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable class="!w-140px">
          <el-option label="待上传" :value="10" />
          <el-option label="上传成功" :value="20" />
          <el-option label="上传失败" :value="30" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" />搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" />重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="住客" prop="guestName" width="120" />
      <el-table-column label="身份证" prop="idCard" width="190" />
      <el-table-column label="房号" prop="roomNo" width="90" />
      <el-table-column label="动作" width="110">
        <template #default="{ row }">{{ actionLabel(row.actionType) }}</template>
      </el-table-column>
      <el-table-column label="业务时间" prop="bizTime" width="170" :formatter="dateFormatter" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="row.status === 20 ? 'success' : row.status === 30 ? 'danger' : 'warning'">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="重试次数" prop="retryCount" width="100" />
      <el-table-column label="失败原因" prop="failReason" min-width="180" show-overflow-tooltip />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" :disabled="row.status === 20" @click="handleRetry(row.id)" v-hasPermi="['hotel:psb:update']">重试</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import * as PsbApi from '@/api/hotel/psb'

defineOptions({ name: 'HotelPsb' })

const message = useMessage()
const loading = ref(false)
const total = ref(0)
const list = ref<PsbApi.PsbLogVO[]>([])
const stat = ref<any>({})
const queryParams = reactive({
  pageNo: 1, pageSize: 10, guestName: '', idCard: '',
  actionType: undefined as number | undefined, status: undefined as number | undefined
})
const queryFormRef = ref()

const actionLabel = (v?: number) => ({ 10: '入住登记', 20: '退房注销', 30: '换房', 40: '信息更正' } as any)[v || 0] || '-'
const statusLabel = (v?: number) => ({ 10: '待上传', 20: '上传成功', 30: '上传失败', 40: '无需上传' } as any)[v || 0] || '-'

const getList = async () => {
  loading.value = true
  try {
    const data = await PsbApi.getPsbPage(queryParams)
    list.value = data.list
    total.value = data.total
    stat.value = (await PsbApi.getPsbStat({})) || {}
  } finally {
    loading.value = false
  }
}

const handleQuery = () => { queryParams.pageNo = 1; getList() }
const resetQuery = () => { queryFormRef.value?.resetFields(); handleQuery() }

const handleRetry = async (id: number) => {
  await PsbApi.retryPsb(id)
  message.success('已重新上传')
  await getList()
}

onMounted(getList)
</script>

<style scoped>
.stat-label { color: var(--el-text-color-secondary); font-size: 13px; }
.stat-value { font-size: 26px; font-weight: 600; margin-top: 4px; }
.stat-value.success { color: var(--el-color-success); }
.stat-value.danger { color: var(--el-color-danger); }
</style>
