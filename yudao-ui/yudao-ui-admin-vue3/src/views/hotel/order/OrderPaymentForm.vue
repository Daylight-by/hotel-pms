<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="560">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="90px"
    >
      <el-form-item label="订单号">
        <span>{{ order?.orderNo }}</span>
      </el-form-item>
      <el-form-item label="金额">
        <span>
          订单金额 ¥ {{ formatMoney(order?.amount) }} ／ 已收 ¥ {{ formatMoney(order?.paidAmount) }}
        </span>
      </el-form-item>

      <template v-if="mode === 'payment'">
        <el-form-item label="支付状态" prop="paymentStatus">
          <el-radio-group v-model="formData.paymentStatus">
            <el-radio :value="10">未支付</el-radio>
            <el-radio :value="20">部分支付</el-radio>
            <el-radio :value="30">已支付</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="支付方式" prop="paymentMethod">
          <el-select v-model="formData.paymentMethod" placeholder="请选择支付方式" class="!w-full">
            <el-option
              v-for="item in PAYMENT_METHOD"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="实收金额" prop="amount">
          <el-input-number
            v-model="formData.amount"
            :min="0"
            :precision="2"
            :step="100"
            class="!w-full"
          />
        </el-form-item>
        <el-form-item label="需要发票" prop="invoiceRequested">
          <el-switch v-model="formData.invoiceRequested" />
        </el-form-item>
        <el-form-item v-if="formData.invoiceRequested" label="发票号" prop="invoiceNo">
          <el-input v-model="formData.invoiceNo" placeholder="请输入发票号" />
        </el-form-item>
      </template>

      <template v-else>
        <el-form-item label="退款金额" prop="amount">
          <el-input-number
            v-model="formData.amount"
            :min="0"
            :max="Number(order?.paidAmount ?? 0)"
            :precision="2"
            :step="100"
            class="!w-full"
          />
        </el-form-item>
        <el-form-item label="退款原因" prop="reason">
          <el-input v-model="formData.reason" type="textarea" :rows="2" placeholder="请输入退款原因" />
        </el-form-item>
        <el-alert
          type="warning"
          :closable="false"
          show-icon
          title="退款后若订单处于「已确认 / 已入住」，订单将自动取消并释放房间"
        />
      </template>
    </el-form>
    <template #footer>
      <el-button :disabled="formLoading" type="primary" @click="submitForm">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script lang="ts" setup>
import { PAYMENT_METHOD, formatMoney } from '@/utils/hotel'
import * as OrderApi from '@/api/hotel/order'

defineOptions({ name: 'HotelOrderPaymentForm' })

const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const mode = ref<'payment' | 'refund'>('payment')
const order = ref<OrderApi.OrderVO>()
const formData = ref<OrderApi.OrderPaymentVO>({
  paymentStatus: 30,
  paymentMethod: 10,
  amount: 0,
  invoiceNo: '',
  invoiceRequested: false,
  reason: ''
})
const formRules = reactive({
  paymentStatus: [{ required: true, message: '请选择支付状态', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'change' }]
})
const formRef = ref()

const open = (row: OrderApi.OrderVO, type: 'payment' | 'refund') => {
  dialogVisible.value = true
  mode.value = type
  order.value = row
  dialogTitle.value = type === 'payment' ? '登记收款' : '办理退款'
  formData.value = {
    paymentStatus: type === 'payment' ? 30 : 40,
    paymentMethod: row.paymentMethod ?? 10,
    amount: type === 'payment' ? Number(row.amount ?? 0) : Number(row.paidAmount ?? 0),
    invoiceNo: row.invoiceNo ?? '',
    invoiceRequested: row.invoiceRequested ?? false,
    reason: ''
  }
}
defineExpose({ open })

const emit = defineEmits(['success'])
const submitForm = async () => {
  if (!formRef) return
  const valid = await formRef.value.validate()
  if (!valid) return
  if (!order.value?.id) return
  formLoading.value = true
  try {
    if (mode.value === 'payment') {
      await OrderApi.payOrder(order.value.id, formData.value)
      message.success('收款信息已登记')
    } else {
      await OrderApi.refundOrder(order.value.id, formData.value)
      message.success('退款已处理')
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}
</script>
