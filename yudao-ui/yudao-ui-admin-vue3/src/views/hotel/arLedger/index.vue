<template>
  <ContentWrap>
    <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px" class="-mb-15px">
      <el-form-item label="协议单位" prop="arAccountId">
        <el-select v-model="queryParams.arAccountId" placeholder="全部" clearable filterable class="!w-240px">
          <el-option v-for="a in accounts" :key="a.id" :label="a.name" :value="a.id!" />
        </el-select>
      </el-form-item>
      <el-form-item label="类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="全部" clearable class="!w-140px">
          <el-option label="挂账" :value="10" /><el-option label="冲销" :value="20" /><el-option label="结算" :value="30" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable class="!w-140px">
          <el-option label="未结" :value="10" /><el-option label="已结" :value="20" />
        </el-select>
      </el-form-item>
      <el-form-item label="营业日">
        <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始" end-placeholder="结束" class="!w-260px" />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" />搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" />重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="协议单位" prop="arAccountName" min-width="200" show-overflow-tooltip />
      <el-table-column label="订单" prop="orderId" width="100" />
      <el-table-column label="金额" width="130"><template #default="{ row }">{{ money(row.amount) }}</template></el-table-column>
      <el-table-column label="类型" width="90"><template #default="{ row }">{{ typeLabel(row.type) }}</template></el-table-column>
      <el-table-column label="营业日" prop="bizDate" width="120" />
      <el-table-column label="状态" width="90"><template #default="{ row }"><el-tag :type="row.status === 20 ? 'success' : 'warning'">{{ row.status === 20 ? '已结' : '未结' }}</el-tag></template></el-table-column>
      <el-table-column label="结算单" prop="settlementId" width="100" />
      <el-table-column label="备注" prop="remark" min-width="180" show-overflow-tooltip />
      <el-table-column label="创建时间" prop="createTime" width="170" :formatter="dateFormatter" />
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import * as ArApi from '@/api/hotel/ar'

defineOptions({ name: 'HotelArLedger' })

const loading = ref(false)
const total = ref(0)
const list = ref<ArApi.ArLedgerVO[]>([])
const accounts = ref<ArApi.ArAccountVO[]>([])
const dateRange = ref<string[]>([])
const queryParams = reactive({
  pageNo: 1, pageSize: 10, arAccountId: undefined as number | undefined,
  type: undefined as number | undefined, status: undefined as number | undefined,
  beginDate: undefined as any, endDate: undefined as any
})
const queryFormRef = ref()

const money = (v?: number) => `¥ ${Number(v || 0).toFixed(2)}`
const typeLabel = (v?: number) => ({ 10: '挂账', 20: '冲销', 30: '结算' } as any)[v || 0] || '-'

const getList = async () => {
  loading.value = true
  try {
    queryParams.beginDate = dateRange.value?.[0]
    queryParams.endDate = dateRange.value?.[1]
    const data = await ArApi.getArLedgerPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => { queryParams.pageNo = 1; getList() }
const resetQuery = () => { queryFormRef.value?.resetFields(); dateRange.value = []; handleQuery() }

onMounted(async () => {
  accounts.value = (await ArApi.getArAccountList()) || []
  await getList()
})
</script>
