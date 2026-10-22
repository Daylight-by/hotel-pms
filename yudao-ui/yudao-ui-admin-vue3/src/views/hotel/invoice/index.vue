<template>
  <ContentWrap>
    <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="76px" class="-mb-15px">
      <el-form-item label="发票号" prop="invoiceNo">
        <el-input
          v-model="queryParams.invoiceNo"
          placeholder="发票号码"
          clearable
          class="!w-200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="抬头" prop="title">
        <el-input
          v-model="queryParams.title"
          placeholder="发票抬头"
          clearable
          class="!w-200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable class="!w-140px">
          <el-option label="已开具" :value="10" />
          <el-option label="已红冲" :value="20" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openIssue" v-hasPermi="['hotel:invoice:create']">
          <Icon icon="ep:document-add" class="mr-5px" /> 开票
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="发票号" prop="invoiceNo" width="180" />
      <el-table-column label="抬头" prop="title" min-width="180" show-overflow-tooltip />
      <el-table-column label="税号" prop="taxNo" width="180" show-overflow-tooltip />
      <el-table-column label="订单号" prop="orderNo" width="180" show-overflow-tooltip />
      <el-table-column label="住客" prop="guestName" width="100" />
      <el-table-column label="类型" prop="typeName" width="130" />
      <el-table-column label="金额" width="120" align="right">
        <template #default="{ row }">¥{{ formatMoney(row.amount) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 10 ? 'success' : 'info'">{{ row.statusName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="开票时间" prop="issueTime" width="180" :formatter="dateFormatter" />
      <el-table-column label="红冲时间" prop="voidTime" width="180" :formatter="dateFormatter" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 10"
            link
            type="danger"
            @click="handleVoid(row)"
            v-hasPermi="['hotel:invoice:update']"
          >
            红冲
          </el-button>
          <span v-else class="text-12px text-gray-400">已红冲</span>
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

  <el-dialog v-model="dialogVisible" title="开具发票" width="520px" append-to-body>
    <el-form :model="form" label-width="90px">
      <el-form-item label="关联订单">
        <el-select
          v-model="form.orderId"
          filterable
          clearable
          class="w-full"
          placeholder="选择订单（可留空开散客票）"
          @change="onOrderChange"
        >
          <el-option
            v-for="item in orderList"
            :key="item.id"
            :label="`${item.orderNo} · ${item.guestName || ''} · ¥${formatMoney(item.amount)}`"
            :value="item.id!"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="发票抬头" required>
        <el-input v-model="form.title" placeholder="个人姓名或单位名称" />
      </el-form-item>
      <el-form-item label="税号">
        <el-input v-model="form.taxNo" placeholder="单位开票必填" />
      </el-form-item>
      <el-form-item label="发票类型">
        <el-select v-model="form.type" class="w-full">
          <el-option label="增值税普通发票" :value="10" />
          <el-option label="增值税专用发票" :value="20" />
          <el-option label="电子发票" :value="30" />
        </el-select>
      </el-form-item>
      <el-form-item label="开票金额" required>
        <el-input-number v-model="form.amount" :min="0.01" :precision="2" class="!w-200px" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">确认开票</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { formatMoney } from '@/utils/hotel'
import { dateFormatter } from '@/utils/formatTime'
import * as InvoiceApi from '@/api/hotel/invoice'
import * as OrderApi from '@/api/hotel/order'

defineOptions({ name: 'HotelInvoice' })

const message = useMessage()
const loading = ref(false)
const submitting = ref(false)
const list = ref<InvoiceApi.InvoiceVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  invoiceNo: '',
  title: '',
  status: undefined as number | undefined
})
const queryFormRef = ref()

const dialogVisible = ref(false)
const orderList = ref<OrderApi.OrderVO[]>([])
const form = reactive({
  orderId: undefined as number | undefined,
  guestId: undefined as number | undefined,
  title: '',
  taxNo: '',
  type: 10,
  amount: 0,
  remark: ''
})

const getList = async () => {
  loading.value = true
  try {
    const data = await InvoiceApi.getInvoicePage(queryParams as any)
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

const openIssue = async () => {
  const page = await OrderApi.getOrderPage({ pageNo: 1, pageSize: 50 } as any)
  orderList.value = page.list || []
  form.orderId = undefined
  form.guestId = undefined
  form.title = ''
  form.taxNo = ''
  form.type = 10
  form.amount = 0
  form.remark = ''
  dialogVisible.value = true
}

const onOrderChange = (orderId?: number) => {
  const order = orderList.value.find((i) => i.id === orderId)
  if (!order) return
  form.guestId = order.guestId
  form.title = form.title || order.guestName || ''
  form.guestId = order.guestId
  form.amount = Number(order.amount || 0)
}

const submit = async () => {
  if (!form.title) {
    message.warning('请填写发票抬头')
    return
  }
  if (!form.amount) {
    message.warning('请填写开票金额')
    return
  }
  submitting.value = true
  try {
    await InvoiceApi.issueInvoice({ ...form })
    message.success('开票成功')
    dialogVisible.value = false
    await getList()
  } finally {
    submitting.value = false
  }
}

const handleVoid = async (row: InvoiceApi.InvoiceVO) => {
  try {
    const { value } = await message.prompt('请输入红冲原因', '发票红冲')
    await InvoiceApi.voidInvoice(row.id!, value || '开票有误')
    message.success('红冲成功')
    await getList()
  } catch {}
}

onMounted(getList)
</script>
