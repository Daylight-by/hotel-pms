<template>
  <Dialog v-model="dialogVisible" title="钟点房开单" width="520">
    <el-form ref="formRef" :model="form" label-width="90px">
      <el-form-item label="住客" required>
        <el-select v-model="form.guestId" filterable class="w-full" placeholder="选择住客">
          <el-option
            v-for="item in guestList"
            :key="item.id"
            :label="item.name + (item.mobile ? ' · ' + item.mobile : '')"
            :value="item.id!"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="房间" required>
        <el-select v-model="form.roomId" filterable class="w-full" placeholder="选择房间">
          <el-option
            v-for="item in roomList"
            :key="item.id"
            :label="`${item.roomNo} · ${item.roomTypeName || ''} · ¥${formatMoney(item.price)}`"
            :value="item.id!"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="入住时间" required>
        <el-date-picker
          v-model="form.checkInTime"
          type="datetime"
          value-format="YYYY-MM-DD HH:mm:ss"
          class="!w-full"
        />
      </el-form-item>
      <el-form-item label="离店时间" required>
        <el-date-picker
          v-model="form.checkOutTime"
          type="datetime"
          value-format="YYYY-MM-DD HH:mm:ss"
          class="!w-full"
        />
      </el-form-item>
      <el-form-item label="押金">
        <el-input-number v-model="form.deposit" :min="0" :precision="2" class="!w-160px" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" />
      </el-form-item>
      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="钟点房按房型「钟点房价」计价，不占用房型日历库存"
      />
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">确认开单</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { formatMoney } from '@/utils/hotel'
import * as OrderApi from '@/api/hotel/order'
import * as GuestApi from '@/api/hotel/guest'
import * as RoomApi from '@/api/hotel/room'

defineOptions({ name: 'HotelOrderHourlyForm' })

const emit = defineEmits(['success'])
const message = useMessage()

const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()
const guestList = ref<GuestApi.GuestVO[]>([])
const roomList = ref<RoomApi.RoomVO[]>([])

const now = new Date()
const pad = (n: number) => String(n).padStart(2, '0')
const fmt = (date: Date) =>
  `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:00`

const form = reactive({
  guestId: undefined as number | undefined,
  roomId: undefined as number | undefined,
  checkInTime: '',
  checkOutTime: '',
  deposit: 0,
  remark: ''
})

const open = async (presetGuestId?: number, presetRoomId?: number) => {
  const [guests, rooms] = await Promise.all([GuestApi.getGuestSimpleList(), RoomApi.getRoomSimpleList()])
  guestList.value = guests
  roomList.value = rooms
  const start = new Date(now)
  const end = new Date(now.getTime() + 3 * 60 * 60 * 1000)
  form.guestId = presetGuestId
  form.roomId = presetRoomId
  form.checkInTime = fmt(start)
  form.checkOutTime = fmt(end)
  form.deposit = 0
  form.remark = ''
  dialogVisible.value = true
}

const submit = async () => {
  if (!form.guestId || !form.roomId) {
    message.warning('请选择住客与房间')
    return
  }
  if (!form.checkInTime || !form.checkOutTime) {
    message.warning('请选择入住与离店时间')
    return
  }
  submitting.value = true
  try {
    await OrderApi.createHourlyOrder({
      guestId: form.guestId,
      roomId: form.roomId,
      checkInTime: form.checkInTime,
      checkOutTime: form.checkOutTime,
      deposit: form.deposit || undefined,
      remark: form.remark
    })
    message.success('钟点房开单成功')
    dialogVisible.value = false
    emit('success')
  } finally {
    submitting.value = false
  }
}

defineExpose({ open })
</script>
