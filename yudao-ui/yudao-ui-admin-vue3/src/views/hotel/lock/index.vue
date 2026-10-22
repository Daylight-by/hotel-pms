<template>
  <ContentWrap>
    <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px" class="-mb-15px">
      <el-form-item label="订单号" prop="orderId">
        <el-input v-model="queryParams.orderId" placeholder="订单编号" clearable class="!w-160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="房号" prop="roomNo">
        <el-input v-model="queryParams.roomNo" placeholder="房号" clearable class="!w-140px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="动作" prop="actionType">
        <el-select v-model="queryParams.actionType" placeholder="全部" clearable class="!w-140px">
          <el-option label="发卡" :value="10" />
          <el-option label="补卡" :value="20" />
          <el-option label="销卡" :value="30" />
          <el-option label="续卡" :value="40" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable class="!w-140px">
          <el-option label="待处理" :value="10" />
          <el-option label="成功" :value="20" />
          <el-option label="失败" :value="30" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" />搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" />重置</el-button>
        <el-button type="primary" plain @click="issueVisible = true" v-hasPermi="['hotel:lock:create']">
          <Icon icon="ep:key" class="mr-5px" />发卡
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="订单编号" prop="orderId" width="100" />
      <el-table-column label="房号" prop="roomNo" width="90" />
      <el-table-column label="房卡号" prop="cardNo" min-width="190" show-overflow-tooltip />
      <el-table-column label="动作" width="90">
        <template #default="{ row }">{{ actionLabel(row.actionType) }}</template>
      </el-table-column>
      <el-table-column label="有效期至" prop="validTo" width="170" :formatter="dateFormatter" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 20 ? 'success' : row.status === 30 ? 'danger' : 'warning'">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="处理时间" prop="handleTime" width="170" :formatter="dateFormatter" />
      <el-table-column label="失败原因" prop="failReason" min-width="160" show-overflow-tooltip />
      <el-table-column label="操作" width="190" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleReissue(row.id)" v-hasPermi="['hotel:lock:create']">补卡</el-button>
          <el-button link type="warning" @click="handleCancel(row.orderId)" v-hasPermi="['hotel:lock:update']">销卡</el-button>
          <el-button link type="success" @click="handleExtend(row.orderId)" v-hasPermi="['hotel:lock:update']">续卡</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>

  <el-dialog v-model="issueVisible" title="发卡" width="420px">
    <el-form label-width="90px">
      <el-form-item label="订单编号"><el-input-number v-model="issueOrderId" :min="1" controls-position="right" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="issueVisible = false">取消</el-button>
      <el-button type="primary" @click="handleIssue">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import * as LockApi from '@/api/hotel/lock'

defineOptions({ name: 'HotelLock' })

const message = useMessage()
const loading = ref(false)
const total = ref(0)
const list = ref<LockApi.LockLogVO[]>([])
const queryParams = reactive({
  pageNo: 1, pageSize: 10, orderId: undefined as any, roomNo: '',
  actionType: undefined as number | undefined, status: undefined as number | undefined
})
const queryFormRef = ref()
const issueVisible = ref(false)
const issueOrderId = ref<number>()

const actionLabel = (v?: number) => ({ 10: '发卡', 20: '补卡', 30: '销卡', 40: '续卡' } as any)[v || 0] || '-'
const statusLabel = (v?: number) => ({ 10: '待处理', 20: '成功', 30: '失败' } as any)[v || 0] || '-'

const getList = async () => {
  loading.value = true
  try {
    const data = await LockApi.getLockPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => { queryParams.pageNo = 1; getList() }
const resetQuery = () => { queryFormRef.value?.resetFields(); handleQuery() }

const handleIssue = async () => {
  if (!issueOrderId.value) return
  await LockApi.issueCard(issueOrderId.value)
  message.success('发卡成功')
  issueVisible.value = false
  await getList()
}

const handleReissue = async (id: number) => {
  await LockApi.reissueCard(id)
  message.success('补卡成功')
  await getList()
}

const handleCancel = async (orderId: number) => {
  await LockApi.cancelCard(orderId)
  message.success('销卡成功')
  await getList()
}

const handleExtend = async (orderId: number) => {
  await LockApi.extendCard(orderId)
  message.success('续卡成功')
  await getList()
}

onMounted(getList)
</script>
