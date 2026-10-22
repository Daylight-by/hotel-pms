<template>
  <ContentWrap>
    <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="82px" class="-mb-15px">
      <el-form-item label="申请单号" prop="applyNo">
        <el-input v-model="queryParams.applyNo" placeholder="申请单号" clearable class="!w-200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="订单编号" prop="orderId">
        <el-input v-model="queryParams.orderId" placeholder="订单编号" clearable class="!w-160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable class="!w-140px">
          <el-option label="待开具" :value="10" />
          <el-option label="已开具" :value="20" />
          <el-option label="已驳回" :value="30" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="申请单号" prop="applyNo" width="190" />
      <el-table-column label="订单编号" prop="orderId" width="100" />
      <el-table-column label="抬头" prop="title" min-width="180" show-overflow-tooltip />
      <el-table-column label="税号" prop="taxNo" width="190" show-overflow-tooltip />
      <el-table-column label="邮箱" prop="email" width="180" show-overflow-tooltip />
      <el-table-column label="类型" prop="typeName" width="130" />
      <el-table-column label="金额" width="110" align="right">
        <template #default="{ row }">￥{{ formatMoney(row.amount) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)">{{ row.statusName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="提交时间" prop="createTime" width="170" />
      <el-table-column label="处理时间" prop="handleTime" width="170" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 10">
            <el-button link type="primary" @click="handle(row, 20)" v-hasPermi="['hotel:invoice-apply:update']">
              开具
            </el-button>
            <el-button link type="danger" @click="handle(row, 30)" v-hasPermi="['hotel:invoice-apply:update']">
              驳回
            </el-button>
          </template>
          <span v-else class="text-12px text-gray-400">{{ row.rejectReason || '已处理' }}</span>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>
</template>

<script lang="ts" setup>
import { formatMoney } from '@/utils/hotel'
import * as InvoiceApplyApi from '@/api/hotel/invoiceApply'

defineOptions({ name: 'HotelInvoiceApply' })

const message = useMessage()
const loading = ref(false)
const list = ref<InvoiceApplyApi.InvoiceApplyVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  applyNo: '',
  orderId: undefined as number | undefined,
  status: undefined as number | undefined
})
const queryFormRef = ref()

const statusTag = (status?: number) => {
  if (status === 20) return 'success'
  if (status === 30) return 'danger'
  return 'warning'
}

const getList = async () => {
  loading.value = true
  try {
    const data = await InvoiceApplyApi.getInvoiceApplyPage(queryParams as any)
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
  handleQuery()
}

const handle = async (row: InvoiceApplyApi.InvoiceApplyVO, status: number) => {
  try {
    if (status === 20) {
      await message.confirm(`确认按申请开具电子发票？金额 ￥${formatMoney(row.amount)}`, '开具发票')
      await InvoiceApplyApi.handleInvoiceApply({ id: row.id!, status })
      message.success('已开具，已生成发票记录')
    } else {
      const { value } = await message.prompt('请输入驳回原因', '驳回申请')
      await InvoiceApplyApi.handleInvoiceApply({ id: row.id!, status, rejectReason: value || '信息不完整' })
      message.success('已驳回')
    }
    await getList()
  } catch {}
}

onMounted(getList)
</script>
