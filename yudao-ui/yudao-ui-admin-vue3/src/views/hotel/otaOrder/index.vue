<template>
  <ContentWrap>
    <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="90px" class="-mb-15px">
      <el-form-item label="OTA" prop="otaCode">
        <el-select v-model="queryParams.otaCode" placeholder="全部" clearable class="!w-150px">
          <el-option v-for="o in ['CTRIP', 'MEITUAN', 'FLIGGY', 'QUNAR']" :key="o" :label="o" :value="o" />
        </el-select>
      </el-form-item>
      <el-form-item label="OTA 单号" prop="otaOrderNo">
        <el-input v-model="queryParams.otaOrderNo" placeholder="OTA 订单号" clearable class="!w-220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="同步状态" prop="syncStatus">
        <el-select v-model="queryParams.syncStatus" placeholder="全部" clearable class="!w-140px">
          <el-option label="待处理" :value="10" />
          <el-option label="已建单" :value="20" />
          <el-option label="已确认" :value="30" />
          <el-option label="已取消" :value="40" />
          <el-option label="失败" :value="50" />
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
      <el-table-column label="OTA" prop="otaCode" width="100" />
      <el-table-column label="OTA 单号" prop="otaOrderNo" min-width="180" />
      <el-table-column label="本地订单" prop="orderId" width="110" />
      <el-table-column label="同步状态" width="110">
        <template #default="{ row }">
          <el-tag :type="row.syncStatus === 20 ? 'success' : row.syncStatus === 50 ? 'danger' : 'warning'">
            {{ statusLabel(row.syncStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="同步时间" prop="syncTime" width="170" :formatter="dateFormatter" />
      <el-table-column label="原始报文" prop="rawData" min-width="260" show-overflow-tooltip />
      <el-table-column label="失败原因" prop="failReason" min-width="160" show-overflow-tooltip />
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import * as OtaApi from '@/api/hotel/ota'

defineOptions({ name: 'HotelOtaOrder' })

const loading = ref(false)
const total = ref(0)
const list = ref<OtaApi.OtaOrderVO[]>([])
const queryParams = reactive({
  pageNo: 1, pageSize: 10, otaCode: '', otaOrderNo: '', syncStatus: undefined as number | undefined
})
const queryFormRef = ref()

const statusLabel = (v?: number) =>
  ({ 10: '待处理', 20: '已建单', 30: '已确认', 40: '已取消', 50: '失败' } as any)[v || 0] || '-'

const getList = async () => {
  loading.value = true
  try {
    const data = await OtaApi.getOtaOrderPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => { queryParams.pageNo = 1; getList() }
const resetQuery = () => { queryFormRef.value?.resetFields(); handleQuery() }

onMounted(getList)
</script>
