<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="720">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="90px"
    >
      <el-form-item label="住客" prop="guestId">
        <el-select
          v-model="formData.guestId"
          placeholder="请选择住客"
          filterable
          class="!w-full"
        >
          <el-option
            v-for="item in props.guestList"
            :key="item.id"
            :label="item.name + (item.mobile ? ' · ' + item.mobile : '')"
            :value="item.id!"
          />
        </el-select>
        <div class="text-12px text-gray-500 mt-1">
          找不到住客？先到「住客管理」建档，再回来下单。
        </div>
      </el-form-item>
      <el-form-item label="房间" prop="roomId">
        <el-select v-model="formData.roomId" placeholder="请选择房间" filterable class="!w-full">
          <el-option
            v-for="item in selectableRooms"
            :key="item.id"
            :label="`${item.roomNo} · ${item.roomTypeName || ''} · ¥${item.price}/晚`"
            :value="item.id!"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="入住日期" prop="checkInDate">
        <el-date-picker
          v-model="formData.checkInDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择入住日期"
          class="!w-full"
          :disabled-date="disabledCheckInDate"
        />
      </el-form-item>
      <el-form-item label="离店日期" prop="checkOutDate">
        <el-date-picker
          v-model="formData.checkOutDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择离店日期"
          class="!w-full"
          :disabled-date="disabledCheckOutDate"
        />
      </el-form-item>

      <!-- 金额预估 -->
      <el-form-item label="费用预估">
        <div class="w-full rounded bg-[var(--el-fill-color-light)] p-10px text-13px leading-6">
          <div>
            单价：¥ {{ formatMoney(selectedRoom?.price) }} ×
            <span class="font-bold">{{ nights }}</span> 晚
          </div>
          <div class="text-14px font-bold text-[var(--el-color-danger)]">
            预计金额：¥ {{ formatMoney(estimatedAmount) }}
          </div>
          <div class="text-12px text-gray-500">最终金额以后端按房间实时售价计算为准</div>
        </div>
      </el-form-item>

      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="特殊要求、渠道等" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :disabled="formLoading" type="primary" @click="submitForm">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script lang="ts" setup>
import { formatMoney, nightsBetween } from '@/utils/hotel'
import * as OrderApi from '@/api/hotel/order'
import * as GuestApi from '@/api/hotel/guest'
import * as RoomApi from '@/api/hotel/room'

defineOptions({ name: 'HotelOrderForm' })

const props = defineProps<{
  guestList: GuestApi.GuestVO[]
  roomList: RoomApi.RoomVO[]
}>()

const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref<OrderApi.OrderVO>({
  id: undefined,
  guestId: undefined,
  roomId: undefined,
  checkInDate: '',
  checkOutDate: '',
  remark: ''
})
const formRules = reactive({
  guestId: [{ required: true, message: '请选择住客', trigger: 'change' }],
  roomId: [{ required: true, message: '请选择房间', trigger: 'change' }],
  checkInDate: [{ required: true, message: '请选择入住日期', trigger: 'change' }],
  checkOutDate: [{ required: true, message: '请选择离店日期', trigger: 'change' }]
})
const formRef = ref()

/** 维修中的房间不可下单 */
const selectableRooms = computed(() => props.roomList.filter((room) => room.status !== 40))

const selectedRoom = computed(() =>
  props.roomList.find((room) => room.id === formData.value.roomId)
)

const nights = computed(() =>
  nightsBetween(formData.value.checkInDate, formData.value.checkOutDate)
)

const estimatedAmount = computed(() => {
  const price = Number(selectedRoom.value?.price ?? 0)
  return price * nights.value
})

/** 入住日期：今天及以后 */
const disabledCheckInDate = (date: Date) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return date.getTime() < today.getTime()
}

/** 离店日期：必须晚于入住日期 */
const disabledCheckOutDate = (date: Date) => {
  if (!formData.value.checkInDate) return false
  return date.getTime() <= new Date(formData.value.checkInDate).getTime()
}

const open = async (
  type: string,
  id?: number,
  presetGuest?: GuestApi.GuestVO,
  presetRoom?: RoomApi.RoomVO
) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新建预订' : '编辑订单'
  formType.value = type
  resetForm()
  if (id) {
    formLoading.value = true
    try {
      const order = await OrderApi.getOrder(id)
      formData.value = {
        id: order.id,
        guestId: order.guestId,
        roomId: order.roomId,
        checkInDate: order.checkInDate,
        checkOutDate: order.checkOutDate,
        remark: order.remark
      }
    } finally {
      formLoading.value = false
    }
    return
  }
  if (presetGuest) {
    formData.value.guestId = presetGuest.id
  }
  if (presetRoom) {
    formData.value.roomId = presetRoom.id
  }
  // 默认今晚入住、明天离店
  const today = new Date()
  const tomorrow = new Date(today.getTime() + 24 * 60 * 60 * 1000)
  const fmt = (d: Date) => d.toISOString().slice(0, 10)
  formData.value.checkInDate = fmt(today)
  formData.value.checkOutDate = fmt(tomorrow)
}
defineExpose({ open })

const emit = defineEmits(['success'])
const submitForm = async () => {
  if (!formRef) return
  const valid = await formRef.value.validate()
  if (!valid) return
  if (nights.value <= 0) {
    message.error('离店日期必须晚于入住日期')
    return
  }
  formLoading.value = true
  try {
    if (formType.value === 'create') {
      await OrderApi.createOrder(formData.value)
      message.success('预订创建成功')
    } else {
      await OrderApi.updateOrder(formData.value)
      message.success('订单已更新')
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

const resetForm = () => {
  formData.value = {
    id: undefined,
    guestId: undefined,
    roomId: undefined,
    checkInDate: '',
    checkOutDate: '',
    remark: ''
  }
  formRef.value?.resetFields()
}

/** 房间或日期变化时，若已选房间处于维修状态给出提示 */
watch(
  () => formData.value.roomId,
  (roomId) => {
    const room = props.roomList.find((item) => item.id === roomId)
    if (room?.status === 40) {
      message.warning('该房间正在维修，无法预订')
    }
  }
)
</script>
