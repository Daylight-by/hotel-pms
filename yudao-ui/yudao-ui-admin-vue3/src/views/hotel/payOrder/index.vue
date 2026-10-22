<template>
  <ContentWrap>
    <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="82px" class="-mb-15px">
      <el-form-item label="支付单号" prop="payOrderNo">
        <el-input v-model="queryParams.payOrderNo" placeholder="支付单号" clearable class="!w-220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="渠道" prop="channel">
        <el-select v-model="queryParams.channel" placeholder="全部" clearable class="!w-140px">
          <el-option label="微信支付" value="wxpay" />
          <el-option label="支付宝" value="alipay" />
          <el-option label="模拟支付" value="mock" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable class="!w-140px">
          <el-option label="待支付" :value="10" />
          <el-option label="已支付" :value="20" />
          <el-option label="已关闭" :value="30" />
          <el-option label="已退款" :value="40" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-tag type="info" effect="plain" class="ml-10px">演示环境默认模拟支付（Mock）</el-tag>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="支付单号" prop="payOrderNo" width="200" />
      <el-table-column label="订单号" prop="orderNo" width="180" show-overflow-tooltip />
      <el-table-column label="金额" width="110" align="right">
        <template #default="{ row }">￥{{ formatMoney(row.amount) }}</template>
      </el-table-column>
      <el-table-column label="渠道" width="110">
        <template #default="{ row }">
          <el-tag effect="plain">{{ row.channelName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="第三方交易号" prop="channelTradeNo" width="220" show-overflow-tooltip />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)">{{ row.statusName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="已退金额" width="110" align="right">
        <template #default="{ row }">￥{{ formatMoney(row.refundAmount) }}</template>
      </el-table-column>
      <el-table-column label="支付时间" prop="payTime" width="170" />
      <el-table-column label="创建时间" prop="createTime" width="170" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 20"
            link
            type="danger"
            @click="handleRefund(row)"
            v-hasPermi="['hotel:pay:refund']"
          >
            退款
          </el-button>
          <span v-else class="text-12px text-gray-400">-</span>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>
</template>

<script lang="ts" setup>
import { formatMoney } from '@/utils/hotel'
import * as PayApi from '@/api/hotel/pay'

defineOptions({ name: 'HotelPayOrder' })

const message = useMessage()
const loading = ref(false)
const list = ref<PayApi.PayOrderVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  payOrderNo: '',
  channel: undefined as string | undefined,
  status: undefined as number | undefined
})
const queryFormRef = ref()

const statusTag = (status?: number) => {
  if (status === 20) return 'success'
  if (status === 40) return 'warning'
  if (status === 30) return 'info'
  return 'primary'
}

const getList = async () => {
  loading.value = true
  try {
    const data = await PayApi.getPayOrderPage(queryParams as any)
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

const handleRefund = async (row: PayApi.PayOrderVO) => {
  try {
    const { value } = await message.prompt(`请输入退款金额（最多 ￥${formatMoney(row.amount)}）`, '支付退款', {
      inputValue: String(formatMoney(row.amount))
    })
    await PayApi.refundPayOrder({ payOrderId: row.id!, amount: Number(value) })
    message.success('退款成功')
    await getList()
  } catch {}
}

onMounted(getList)
</script>
