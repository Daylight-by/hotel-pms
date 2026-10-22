<template>
  <ContentWrap>
    <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="76px" class="-mb-15px">
      <el-form-item label="账页号" prop="folioNo">
        <el-input
          v-model="queryParams.folioNo"
          placeholder="F20261021..."
          clearable
          class="!w-200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="订单号" prop="orderId">
        <el-input
          v-model="queryParams.orderId"
          placeholder="订单编号"
          clearable
          class="!w-160px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable class="!w-140px">
          <el-option
            v-for="item in STATUS_OPTIONS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list" @row-click="openDetail" row-class-name="cursor-pointer">
      <el-table-column label="账页号" prop="folioNo" width="160" />
      <el-table-column label="订单号" prop="orderNo" min-width="170" show-overflow-tooltip />
      <el-table-column label="住客" prop="guestName" width="110" />
      <el-table-column label="房间" prop="roomNo" width="90" />
      <el-table-column label="类型" width="90">
        <template #default="{ row }">
          <el-tag size="small" effect="plain">{{ row.type === 20 ? '分账页' : '主账页' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="消费" width="110" align="right">
        <template #default="{ row }">¥{{ formatMoney(row.totalCharge) }}</template>
      </el-table-column>
      <el-table-column label="收款" width="110" align="right">
        <template #default="{ row }">¥{{ formatMoney(row.totalPayment) }}</template>
      </el-table-column>
      <el-table-column label="余额" width="110" align="right">
        <template #default="{ row }">
          <span :class="Number(row.balance) > 0 ? 'text-red-500' : 'text-green-600'">
            ¥{{ formatMoney(row.balance) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="statusTag(row.status)">{{ row.statusName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createTime" width="170" :formatter="dateFormatter" />
      <el-table-column label="操作" width="90" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click.stop="openDetail(row)">账页</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <el-drawer v-model="drawerVisible" :size="'78%'" title="账务账页" destroy-on-close>
    <FolioPanel v-if="currentFolio" :folio="currentFolio" @refresh="refreshCurrent" />
  </el-drawer>
</template>

<script lang="ts" setup>
import { formatMoney } from '@/utils/hotel'
import { dateFormatter } from '@/utils/formatTime'
import * as FolioApi from '@/api/hotel/folio'
import FolioPanel from './FolioPanel.vue'

defineOptions({ name: 'HotelFolio' })

const STATUS_OPTIONS = [
  { value: 10, label: '未结' },
  { value: 20, label: '部分结账' },
  { value: 30, label: '已结账' },
  { value: 40, label: '已转出' },
  { value: 50, label: '已取消' }
]

const loading = ref(false)
const list = ref<FolioApi.FolioVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  folioNo: '',
  orderId: '',
  status: undefined as number | undefined
})
const queryFormRef = ref()

const drawerVisible = ref(false)
const currentFolio = ref<FolioApi.FolioVO | null>(null)

const statusTag = (status?: number) => {
  if (status === 10) return 'primary'
  if (status === 20) return 'warning'
  if (status === 30) return 'success'
  return 'info'
}

const getList = async () => {
  loading.value = true
  try {
    const data = await FolioApi.getFolioPage(queryParams as any)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value?.resetFields()
  queryParams.orderId = ''
  handleQuery()
}

const openDetail = async (row: FolioApi.FolioVO) => {
  if (!row?.id) return
  currentFolio.value = await FolioApi.getFolio(row.id)
  drawerVisible.value = true
}

const refreshCurrent = async () => {
  if (!currentFolio.value?.id) return
  currentFolio.value = await FolioApi.getFolio(currentFolio.value.id)
  await getList()
}

onMounted(getList)
</script>
