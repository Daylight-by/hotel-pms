<template>
  <Dialog v-model="dialogVisible" :title="titleMap[mode]" width="480">
    <el-form :model="form" label-width="90px">
      <template v-if="mode === 'extend'">
        <el-form-item label="当前离店">{{ order.checkOutDate }}</el-form-item>
        <el-form-item label="续住晚数" required>
          <el-input-number v-model="form.nights" :min="1" :max="30" class="!w-160px" />
        </el-form-item>
        <el-form-item label="预计离店">{{ previewCheckOut }}</el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" placeholder="选填" />
        </el-form-item>
      </template>

      <template v-else-if="mode === 'changeRoom'">
        <el-form-item label="当前房间">{{ order.roomNo }}（{{ order.roomTypeName }}）</el-form-item>
        <el-form-item label="目标房间" required>
          <el-select v-model="form.toRoomId" filterable class="w-full" placeholder="选择空闲房间">
            <el-option
              v-for="item in freeRooms"
              :key="item.id"
              :label="`${item.roomNo} · ${item.roomTypeName || ''} · ¥${formatMoney(item.price)}`"
              :value="item.id!"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="换房原因">
          <el-input v-model="form.reason" placeholder="如：客人要求换高楼层" />
        </el-form-item>
        <el-alert
          type="warning"
          :closable="false"
          show-icon
          title="换房后原房间将转为「待清洁」并自动生成清洁任务"
        />
      </template>

      <template v-else-if="mode === 'cancel'">
        <el-form-item label="订单号">{{ order.orderNo }}</el-form-item>
        <el-form-item label="取消原因">
          <el-input v-model="form.reason" type="textarea" :rows="2" placeholder="如：客人行程变更" />
        </el-form-item>
        <el-form-item label="违约金">
          <el-input-number v-model="form.penalty" :min="0" :precision="2" class="!w-160px" />
          <span class="ml-8px text-12px text-gray-400">将入账到该订单账页</span>
        </el-form-item>
      </template>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">确定</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { formatMoney } from '@/utils/hotel'
import * as OrderApi from '@/api/hotel/order'
import * as RoomApi from '@/api/hotel/room'

defineOptions({ name: 'HotelOrderActionForm' })

const emit = defineEmits(['success'])
const message = useMessage()

const titleMap: Record<string, string> = {
  extend: '续住',
  changeRoom: '换房',
  cancel: '取消订单'
}

const dialogVisible = ref(false)
const submitting = ref(false)
const mode = ref<'extend' | 'changeRoom' | 'cancel'>('extend')
const order = ref<OrderApi.OrderVO>({})
const freeRooms = ref<RoomApi.RoomVO[]>([])
const form = reactive({
  nights: 1,
  reason: '',
  penalty: 0,
  toRoomId: undefined as number | undefined
})

const previewCheckOut = computed(() => {
  if (!order.value.checkOutDate) return '-'
  const date = new Date(order.value.checkOutDate)
  date.setDate(date.getDate() + (form.nights || 0))
  return date.toISOString().slice(0, 10)
})

const open = async (action: 'extend' | 'changeRoom' | 'cancel', row: OrderApi.OrderVO) => {
  mode.value = action
  order.value = row
  form.nights = 1
  form.reason = ''
  form.penalty = 0
  form.toRoomId = undefined
  if (action === 'changeRoom') {
    const rooms = await RoomApi.getRoomSimpleList()
    freeRooms.value = rooms.filter((r) => r.status === 10 && r.id !== row.roomId)
  }
  dialogVisible.value = true
}

const submit = async () => {
  if (!order.value.id) return
  submitting.value = true
  try {
    if (mode.value === 'extend') {
      await OrderApi.extendOrder({
        id: order.value.id,
        nights: form.nights,
        remark: form.remark || undefined
      })
      message.success('续住成功')
    } else if (mode.value === 'changeRoom') {
      if (!form.toRoomId) {
        message.warning('请选择目标房间')
        return
      }
      await OrderApi.changeRoom({
        id: order.value.id,
        toRoomId: form.toRoomId,
        reason: form.reason
      })
      message.success('换房成功，已生成清洁任务')
    } else {
      await OrderApi.cancelOrderWithReason(order.value.id, {
        reason: form.reason,
        penalty: form.penalty || undefined
      })
      message.success('订单已取消')
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    submitting.value = false
  }
}

defineExpose({ open })
</script>
