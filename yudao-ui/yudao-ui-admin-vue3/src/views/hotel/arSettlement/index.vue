<template>
  <ContentWrap>
    <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px" class="-mb-15px">
      <el-form-item label="协议单位" prop="arAccountId">
        <el-select v-model="queryParams.arAccountId" placeholder="全部" clearable filterable class="!w-240px">
          <el-option v-for="a in accounts" :key="a.id" :label="a.name" :value="a.id!" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable class="!w-140px">
          <el-option label="待结算" :value="10" />
          <el-option label="部分收款" :value="20" />
          <el-option label="已结清" :value="30" />
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
      <el-table-column label="结算单号" prop="settlementNo" min-width="190" />
      <el-table-column label="协议单位" prop="arAccountName" min-width="200" show-overflow-tooltip />
      <el-table-column label="账期" width="210">
        <template #default="{ row }">{{ row.beginDate }} ~ {{ row.endDate }}</template>
      </el-table-column>
      <el-table-column label="结算金额" width="130"><template #default="{ row }">{{ money(row.totalAmount) }}</template></el-table-column>
      <el-table-column label="已收" width="120"><template #default="{ row }">{{ money(row.paidAmount) }}</template></el-table-column>
      <el-table-column label="待收" width="120"><template #default="{ row }">{{ money(row.remainAmount) }}</template></el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="row.status === 30 ? 'success' : row.status === 20 ? 'warning' : 'info'">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="结清时间" prop="settleTime" width="170" :formatter="dateFormatter" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" :disabled="row.status === 30" @click="openReceive(row)" v-hasPermi="['hotel:ar:update']">收款</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>

  <el-dialog v-model="receiveVisible" title="结算收款" width="460px">
    <el-form label-width="100px">
      <el-form-item label="结算单号"><el-input :model-value="current?.settlementNo" disabled /></el-form-item>
      <el-form-item label="待收金额"><el-input :model-value="money(current?.remainAmount)" disabled /></el-form-item>
      <el-form-item label="本次收款"><el-input-number v-model="receiveForm.amount" :min="0.01" :max="Number(current?.remainAmount || 0)" :precision="2" controls-position="right" /></el-form-item>
      <el-form-item label="收款方式">
        <el-select v-model="receiveForm.paymentMethod" class="!w-full">
          <el-option label="现金" :value="1" /><el-option label="银行卡" :value="2" />
          <el-option label="微信" :value="3" /><el-option label="支付宝" :value="4" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="receiveVisible = false">取消</el-button>
      <el-button type="primary" @click="handleReceive">确认收款</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import * as ArApi from '@/api/hotel/ar'

defineOptions({ name: 'HotelArSettlement' })

const message = useMessage()
const loading = ref(false)
const total = ref(0)
const list = ref<ArApi.ArSettlementVO[]>([])
const accounts = ref<ArApi.ArAccountVO[]>([])
const queryParams = reactive({ pageNo: 1, pageSize: 10, arAccountId: undefined as number | undefined, status: undefined as number | undefined })
const queryFormRef = ref()

const receiveVisible = ref(false)
const current = ref<ArApi.ArSettlementVO>()
const receiveForm = reactive({ amount: 0, paymentMethod: 2 })

const money = (v?: number) => `¥ ${Number(v || 0).toFixed(2)}`
const statusLabel = (v?: number) => ({ 10: '待结算', 20: '部分收款', 30: '已结清' } as any)[v || 0] || '-'

const getList = async () => {
  loading.value = true
  try {
    const data = await ArApi.getArSettlementPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => { queryParams.pageNo = 1; getList() }
const resetQuery = () => { queryFormRef.value?.resetFields(); handleQuery() }

const openReceive = (row: ArApi.ArSettlementVO) => {
  current.value = row
  receiveForm.amount = Number(row.remainAmount || 0)
  receiveForm.paymentMethod = 2
  receiveVisible.value = true
}

const handleReceive = async () => {
  await ArApi.receiveSettlement({
    settlementId: current.value?.id,
    amount: receiveForm.amount,
    paymentMethod: receiveForm.paymentMethod
  })
  message.success('收款成功')
  receiveVisible.value = false
  await getList()
}

onMounted(async () => {
  accounts.value = (await ArApi.getArAccountList()) || []
  await getList()
})
</script>
