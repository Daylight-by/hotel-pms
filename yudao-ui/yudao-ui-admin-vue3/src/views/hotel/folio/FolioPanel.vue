<template>
  <div v-if="!folio" class="py-30px text-center text-gray-400">请选择账页</div>
  <div v-else>
    <!-- 概览 -->
    <div class="mb-12px flex flex-wrap items-center gap-12px">
      <div class="folio-head">
        <div class="text-16px font-bold">{{ folio.folioNo }}</div>
        <div class="text-12px text-gray-500">
          {{ folio.guestName || '-' }} · 房间 {{ folio.roomNo || '-' }} · 订单
          {{ folio.orderNo || '-' }}
        </div>
      </div>
      <el-tag :type="statusTag" effect="dark">{{ folio.statusName || '-' }}</el-tag>
      <div class="flex-grow" />
      <div class="amount-card">
        <span class="label">消费合计</span>
        <span class="value primary">¥{{ formatMoney(folio.totalCharge) }}</span>
      </div>
      <div class="amount-card">
        <span class="label">收款合计</span>
        <span class="value success">¥{{ formatMoney(folio.totalPayment) }}</span>
      </div>
      <div class="amount-card">
        <span class="label">余额</span>
        <span class="value" :class="balanceClass">{{ formatMoney(folio.balance) }}</span>
      </div>
    </div>

    <!-- 操作 -->
    <div v-if="operable" class="mb-10px flex flex-wrap gap-8px">
      <el-button type="primary" @click="openCharge" v-hasPermi="['hotel:folio:create']">
        <Icon icon="ep:plus" class="mr-4px" /> 入账
      </el-button>
      <el-button type="success" @click="openPayment" v-hasPermi="['hotel:folio:create']">
        <Icon icon="ep:money" class="mr-4px" /> 收款
      </el-button>
      <el-button
        type="warning"
        plain
        :disabled="selectedItems.length !== 1"
        @click="handleVoid"
        v-hasPermi="['hotel:folio:update']"
      >
        冲销
      </el-button>
      <el-button
        type="info"
        plain
        :disabled="selectedItems.length !== 1"
        @click="openTransfer"
        v-hasPermi="['hotel:folio:update']"
      >
        转账
      </el-button>
      <el-button
        type="info"
        plain
        :disabled="!selectedItems.length"
        @click="handleSplit"
        v-hasPermi="['hotel:folio:create']"
      >
        拆账页（{{ selectedItems.length }}）
      </el-button>
      <div class="flex-grow" />
      <el-button type="danger" plain @click="handleSettle" v-hasPermi="['hotel:folio:update']">
        <Icon icon="ep:check" class="mr-4px" /> 结账
      </el-button>
    </div>

    <!-- 明细 -->
    <el-table
      ref="tableRef"
      :data="folio.items || []"
      size="small"
      border
      @selection-change="onSelectionChange"
    >
      <el-table-column type="selection" width="42" :selectable="isSelectable" />
      <el-table-column label="营业日" prop="businessDate" width="110" />
      <el-table-column label="类型" prop="itemTypeName" width="90" />
      <el-table-column label="项目" prop="itemName" min-width="150" show-overflow-tooltip />
      <el-table-column label="方向" width="80">
        <template #default="{ row }">
          <el-tag size="small" :type="row.direction === 1 ? 'danger' : 'success'">
            {{ row.directionName }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="数量" prop="quantity" width="70" align="center" />
      <el-table-column label="单价" width="100" align="right">
        <template #default="{ row }">¥{{ formatMoney(row.unitPrice) }}</template>
      </el-table-column>
      <el-table-column label="金额" width="110" align="right">
        <template #default="{ row }">
          <span :class="row.amount < 0 ? 'text-green-600' : ''">{{ formatMoney(row.amount) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="支付方式" prop="paymentMethodName" width="90" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="row.status === 10 ? 'success' : 'info'">
            {{ row.statusName }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作人" prop="operatorName" width="100" />
      <el-table-column label="备注" prop="remark" min-width="120" show-overflow-tooltip />
    </el-table>

    <!-- 入账 -->
    <el-dialog v-model="chargeVisible" title="入账消费" width="480px" append-to-body>
      <el-form :model="chargeForm" label-width="80px">
        <el-form-item label="项目类型" required>
          <el-select v-model="chargeForm.itemType" class="w-full">
            <el-option
              v-for="item in ITEM_TYPE_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="项目名称">
          <el-input v-model="chargeForm.itemName" placeholder="不填则用类型名称" />
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="chargeForm.quantity" :min="1" class="!w-160px" />
        </el-form-item>
        <el-form-item label="单价">
          <el-input-number v-model="chargeForm.unitPrice" :min="0" :precision="2" class="!w-160px" />
        </el-form-item>
        <el-form-item label="金额">
          <el-input-number v-model="chargeForm.amount" :min="0" :precision="2" class="!w-160px" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="chargeForm.remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="chargeVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitCharge">确认入账</el-button>
      </template>
    </el-dialog>

    <!-- 收款 -->
    <el-dialog v-model="paymentVisible" title="收款" width="460px" append-to-body>
      <el-form :model="paymentForm" label-width="80px">
        <el-form-item label="金额" required>
          <el-input-number v-model="paymentForm.amount" :precision="2" class="!w-200px" />
          <span class="ml-8px text-12px text-gray-400">退款请填负数</span>
        </el-form-item>
        <el-form-item label="支付方式" required>
          <el-select v-model="paymentForm.paymentMethod" class="w-full">
            <el-option
              v-for="item in PAYMENT_METHOD"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="交易号">
          <el-input v-model="paymentForm.tradeNo" placeholder="选填，银行/在线支付凭证号" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="paymentForm.remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="paymentVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitPayment">确认收款</el-button>
      </template>
    </el-dialog>

    <!-- 转账 -->
    <el-dialog v-model="transferVisible" title="转账到其它账页" width="460px" append-to-body>
      <el-form label-width="90px">
        <el-form-item label="目标账页" required>
          <el-select v-model="transferTargetId" filterable class="w-full" placeholder="选择目标账页">
            <el-option
              v-for="item in otherFolios"
              :key="item.id"
              :label="`${item.folioNo}（${item.guestName || '未知'} / ¥${formatMoney(item.balance)}）`"
              :value="item.id!"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="transferVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitTransfer">确认转账</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { formatMoney, PAYMENT_METHOD } from '@/utils/hotel'
import * as FolioApi from '@/api/hotel/folio'

defineOptions({ name: 'HotelFolioPanel' })

const props = defineProps<{ folio: FolioApi.FolioVO | null }>()
const emit = defineEmits(['refresh'])

const message = useMessage()
const submitting = ref(false)
const tableRef = ref()
const selectedItems = ref<FolioApi.FolioItemVO[]>([])

const ITEM_TYPE_OPTIONS = [
  { value: 10, label: '房费' },
  { value: 20, label: '餐饮' },
  { value: 30, label: '迷你吧' },
  { value: 40, label: '洗衣' },
  { value: 50, label: '赔偿' },
  { value: 60, label: '押金' },
  { value: 70, label: '其他' },
  { value: 80, label: '退款' }
]

const operable = computed(() => props.folio && ![30, 50].includes(props.folio.status ?? 0))
const statusTag = computed(() => {
  const status = props.folio?.status
  if (status === 10) return 'primary'
  if (status === 20) return 'warning'
  if (status === 30) return 'success'
  return 'info'
})
const balanceClass = computed(() => {
  const balance = Number(props.folio?.balance ?? 0)
  if (balance > 0) return 'danger'
  if (balance < 0) return 'warning'
  return 'success'
})

const isSelectable = (row: FolioApi.FolioItemVO) => row.status === 10 && operable.value
const onSelectionChange = (rows: FolioApi.FolioItemVO[]) => {
  selectedItems.value = rows
}

/* ---------------- 入账 ---------------- */
const chargeVisible = ref(false)
const chargeForm = reactive({
  itemType: 20,
  itemName: '',
  quantity: 1,
  unitPrice: 0,
  amount: 0,
  remark: ''
})

const openCharge = () => {
  chargeForm.itemType = 20
  chargeForm.itemName = ''
  chargeForm.quantity = 1
  chargeForm.unitPrice = 0
  chargeForm.amount = 0
  chargeForm.remark = ''
  chargeVisible.value = true
}

const submitCharge = async () => {
  if (!props.folio?.id) return
  submitting.value = true
  try {
    await FolioApi.postCharge({
      folioId: props.folio.id,
      itemType: chargeForm.itemType,
      itemName: chargeForm.itemName || undefined,
      quantity: chargeForm.quantity,
      unitPrice: chargeForm.unitPrice,
      amount: chargeForm.amount || chargeForm.unitPrice * chargeForm.quantity,
      remark: chargeForm.remark
    })
    message.success('入账成功')
    chargeVisible.value = false
    emit('refresh')
  } finally {
    submitting.value = false
  }
}

/* ---------------- 收款 ---------------- */
const paymentVisible = ref(false)
const paymentForm = reactive({
  amount: 0,
  paymentMethod: 1,
  tradeNo: '',
  remark: ''
})

const openPayment = () => {
  paymentForm.amount = Math.abs(Number(props.folio?.balance ?? 0))
  paymentForm.paymentMethod = 1
  paymentForm.tradeNo = ''
  paymentForm.remark = ''
  paymentVisible.value = true
}

const submitPayment = async () => {
  if (!props.folio?.id) return
  if (!paymentForm.amount) {
    message.warning('请输入收款金额')
    return
  }
  submitting.value = true
  try {
    await FolioApi.postPayment({
      folioId: props.folio.id,
      amount: paymentForm.amount,
      paymentMethod: paymentForm.paymentMethod,
      tradeNo: paymentForm.tradeNo || undefined,
      remark: paymentForm.remark
    })
    message.success('收款成功')
    paymentVisible.value = false
    emit('refresh')
  } finally {
    submitting.value = false
  }
}

/* ---------------- 冲销 ---------------- */
const handleVoid = async () => {
  const item = selectedItems.value[0]
  if (!item?.id) return
  const { value } = await message.prompt('请输入冲销原因', '冲销确认')
  try {
    submitting.value = true
    await FolioApi.voidFolioItem(item.id, value || '前台冲销')
    message.success('冲销成功')
    emit('refresh')
  } finally {
    submitting.value = false
  }
}

/* ---------------- 转账 ---------------- */
const transferVisible = ref(false)
const transferTargetId = ref<number>()
const otherFolios = ref<FolioApi.FolioVO[]>([])

const openTransfer = async () => {
  const page = await FolioApi.getFolioPage({ pageNo: 1, pageSize: 100 } as any)
  otherFolios.value = (page.list || []).filter(
    (i: FolioApi.FolioVO) => i.id !== props.folio?.id && i.status !== 50
  )
  transferTargetId.value = undefined
  transferVisible.value = true
}

const submitTransfer = async () => {
  const item = selectedItems.value[0]
  if (!item?.id || !transferTargetId.value) {
    message.warning('请选择目标账页')
    return
  }
  submitting.value = true
  try {
    await FolioApi.transferFolioItem(item.id, transferTargetId.value)
    message.success('转账成功')
    transferVisible.value = false
    emit('refresh')
  } finally {
    submitting.value = false
  }
}

/* ---------------- 拆账页 ---------------- */
const handleSplit = async () => {
  if (!props.folio?.id || !selectedItems.value.length) return
  try {
    await message.confirm(`确认把选中的 ${selectedItems.value.length} 条明细拆到新账页？`)
    submitting.value = true
    await FolioApi.splitFolio(
      props.folio.id,
      selectedItems.value.map((i) => i.id!),
      '前台拆账'
    )
    message.success('拆账成功')
    emit('refresh')
  } catch {
  } finally {
    submitting.value = false
  }
}

/* ---------------- 结账 ---------------- */
const handleSettle = async () => {
  if (!props.folio?.id) return
  try {
    await message.confirm('结账前请确认余额已为 0，是否继续？')
  } catch {
    return
  }
  try {
    submitting.value = true
    await FolioApi.settleFolio(props.folio.id)
    message.success('结账成功')
    emit('refresh')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.folio-head {
  line-height: 1.4;
}
.amount-card {
  display: flex;
  flex-direction: column;
  padding: 6px 14px;
  border-radius: 6px;
  background: var(--el-fill-color-light);
  min-width: 110px;
}
.amount-card .label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.amount-card .value {
  font-size: 18px;
  font-weight: 700;
}
.value.primary {
  color: var(--el-color-primary);
}
.value.success {
  color: var(--el-color-success);
}
.value.danger {
  color: var(--el-color-danger);
}
.value.warning {
  color: var(--el-color-warning);
}
</style>
