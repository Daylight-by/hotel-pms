<template>
  <div v-loading="loading">
    <div class="mb-10px flex items-center gap-10px">
      <el-tag type="primary" effect="dark">
        <Icon icon="ep:calendar" class="mr-4px" /> 营业日 {{ businessDate.bizDate || '-' }}
      </el-tag>
      <span class="text-12px text-gray-400">
        自然日 {{ businessDate.today || '-' }} · 房态看板按当前营业日展示
      </span>
    </div>
    <!-- 顶部汇总 -->
    <el-row :gutter="12" class="mb-12px">
      <el-col v-for="card in summaryCards" :key="card.key" :span="4">
        <el-card shadow="hover" :body-style="{ padding: '16px' }">
          <div class="text-12px text-gray-500">{{ card.label }}</div>
          <div class="mt-6px text-26px font-bold" :style="{ color: card.color }">
            {{ card.value }}
          </div>
          <div class="text-12px text-gray-400">{{ card.hint }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" :body-style="{ padding: '16px' }">
          <div class="text-12px text-gray-500">出租率</div>
          <div class="mt-6px text-26px font-bold text-[var(--el-color-primary)]">
            {{ board.counts ? occupancyRate + '%' : '-' }}
          </div>
          <el-progress
            :percentage="Number(occupancyRate)"
            :show-text="false"
            :stroke-width="4"
            class="mt-6px"
          />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12">
      <!-- 房态网格 -->
      <el-col :span="17">
        <ContentWrap title="房态一览">
          <template #header>
            <div class="flex flex-grow items-center justify-end gap-8px">
              <el-radio-group v-model="floorFilter" size="small">
                <el-radio-button :value="0">全部</el-radio-button>
                <el-radio-button v-for="floor in floors" :key="floor" :value="floor">
                  {{ floor }}F
                </el-radio-button>
              </el-radio-group>
              <el-button size="small" @click="loadBoard">
                <Icon icon="ep:refresh" class="mr-4px" />刷新
              </el-button>
            </div>
          </template>

          <div class="mb-12px flex gap-16px text-12px">
            <div v-for="item in ROOM_STATUS" :key="item.value" class="flex items-center gap-6px">
              <span class="inline-block h-10px w-10px rounded-2px" :style="{ background: item.color }" />
              <span class="text-gray-600">{{ item.label }} {{ statusCount(item.value) }}</span>
            </div>
          </div>

          <div v-for="floor in visibleFloors" :key="floor" class="mb-16px">
            <div class="mb-8px text-13px font-bold text-gray-600">{{ floor }} 层</div>
            <div class="flex flex-wrap gap-10px">
              <div
                v-for="room in roomsOfFloor(floor)"
                :key="room.id"
                class="room-card"
                :style="{ borderColor: colorOf(ROOM_STATUS, room.status) }"
                @click="openRoom(room)"
              >
                <div class="flex items-center justify-between">
                  <span class="text-16px font-bold">{{ room.roomNo }}</span>
                  <span class="flex items-center gap-4px">
                    <el-tooltip
                      v-if="room.lockStatus"
                      :content="room.lockStatusName || '已锁定'"
                      placement="top"
                    >
                      <el-tag size="small" type="warning" effect="dark">
                        {{ room.lockStatus === 1 ? '维修锁' : '预留锁' }}
                      </el-tag>
                    </el-tooltip>
                    <span
                      class="h-8px w-8px rounded-full"
                      :style="{ background: colorOf(ROOM_STATUS, room.status) }"
                    />
                  </span>
                </div>
                <div class="text-12px text-gray-500">{{ room.roomTypeName }}</div>
                <div class="mt-4px text-12px" :style="{ color: colorOf(ROOM_STATUS, room.status) }">
                  {{ room.statusName || labelOf(ROOM_STATUS, room.status) }}
                </div>
                <div class="text-12px text-gray-400 truncate">
                  {{ room.currentGuest || '—' }}
                </div>
              </div>
            </div>
          </div>
          <el-empty v-if="!visibleRooms.length" description="暂无房间数据" />
        </ContentWrap>
      </el-col>

      <!-- 今日动态 -->
      <el-col :span="7">
        <ContentWrap title="今日预抵">
          <div v-if="!todayCheckIns.length" class="py-16px text-center text-13px text-gray-400">
            暂无预抵订单
          </div>
          <div
            v-for="order in todayCheckIns"
            :key="order.id"
            class="mb-8px rounded bg-[var(--el-fill-color-light)] p-10px"
          >
            <div class="flex items-center justify-between">
              <span class="font-bold">{{ order.roomNo }} · {{ order.guestName }}</span>
              <el-button
                link
                type="success"
                size="small"
                @click="handleCheckIn(order)"
                v-hasPermi="['hotel:order:update']"
              >
                入住
              </el-button>
            </div>
            <div class="text-12px text-gray-500">
              {{ order.checkInDate }} ~ {{ order.checkOutDate }} ／ ¥{{ formatMoney(order.amount) }}
            </div>
          </div>
        </ContentWrap>

        <ContentWrap title="今日待退房">
          <div v-if="!dueCheckOuts.length" class="py-16px text-center text-13px text-gray-400">
            暂无待退房
          </div>
          <div
            v-for="order in dueCheckOuts"
            :key="order.id"
            class="mb-8px rounded bg-[var(--el-fill-color-light)] p-10px"
          >
            <div class="flex items-center justify-between">
              <span class="font-bold">{{ order.roomNo }} · {{ order.guestName }}</span>
              <el-button
                link
                type="warning"
                size="small"
                @click="handleCheckOut(order)"
                v-hasPermi="['hotel:order:update']"
              >
                退房
              </el-button>
            </div>
            <div class="text-12px text-gray-500">
              应离店 {{ order.checkOutDate }} ／ 已收 ¥{{ formatMoney(order.paidAmount) }}
            </div>
          </div>
        </ContentWrap>
      </el-col>
    </el-row>

    <!-- 房间操作弹窗 -->
    <Dialog v-model="roomDialogVisible" :title="`房间 ${currentRoom?.roomNo || ''}`" width="480">
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="房型">{{ currentRoom?.roomTypeName }}</el-descriptions-item>
        <el-descriptions-item label="楼层">{{ currentRoom?.floor }} 层</el-descriptions-item>
        <el-descriptions-item label="面积">{{ currentRoom?.area || '-' }} ㎡</el-descriptions-item>
        <el-descriptions-item label="售价">¥ {{ formatMoney(currentRoom?.price) }}</el-descriptions-item>
        <el-descriptions-item label="当前房态">
          <el-tag :type="tagTypeOf(ROOM_STATUS, currentRoom?.status)">
            {{ currentRoom?.statusName || labelOf(ROOM_STATUS, currentRoom?.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="当前住客">
          {{ currentRoom?.currentGuest || '—' }}
        </el-descriptions-item>
        <el-descriptions-item label="备注">{{ currentRoom?.remark || '-' }}</el-descriptions-item>
      </el-descriptions>

      <div class="mt-16px">
        <div class="mb-8px text-13px font-bold">调整房态</div>
        <div class="flex flex-wrap gap-8px">
          <el-button
            v-for="item in ROOM_STATUS"
            :key="item.value"
            size="small"
            :type="item.value === currentRoom?.status ? 'primary' : 'default'"
            :disabled="item.value === currentRoom?.status"
            @click="handleStatusChange(item.value)"
            v-hasPermi="['hotel:room:update']"
          >
            {{ item.label }}
          </el-button>
        </div>
      </div>

      <div class="mt-16px">
        <div class="mb-8px text-13px font-bold">锁定状态</div>
        <div class="flex flex-wrap gap-8px">
          <el-button
            v-for="item in LOCK_STATUS"
            :key="item.value"
            size="small"
            :type="item.value === (currentRoom?.lockStatus ?? 0) ? 'warning' : 'default'"
            :disabled="item.value === (currentRoom?.lockStatus ?? 0)"
            @click="handleLockChange(item.value)"
            v-hasPermi="['hotel:room:update']"
          >
            {{ item.label }}
          </el-button>
        </div>
      </div>

      <template #footer>
        <el-button type="primary" @click="handleCreateOrder" v-hasPermi="['hotel:order:create']">
          为这间房开单
        </el-button>
        <el-button @click="roomDialogVisible = false">关 闭</el-button>
      </template>
    </Dialog>
  </div>
</template>
<script lang="ts" setup>
import { useRouter } from 'vue-router'
import {
  ROOM_STATUS,
  colorOf,
  formatMoney,
  labelOf,
  tagTypeOf
} from '@/utils/hotel'
import * as RoomApi from '@/api/hotel/room'
import * as OrderApi from '@/api/hotel/order'
import * as AuditApi from '@/api/hotel/audit'

defineOptions({ name: 'HotelBoard' })

const message = useMessage()
const router = useRouter()

const loading = ref(false)
const board = ref<Partial<RoomApi.RoomBoardVO>>({ rooms: [] })
const todayCheckIns = ref<OrderApi.OrderVO[]>([])
const dueCheckOuts = ref<OrderApi.OrderVO[]>([])
const floorFilter = ref(0)
const businessDate = ref<AuditApi.BusinessDateVO>({})

const LOCK_STATUS = [
  { value: 0, label: '正常' },
  { value: 1, label: '维修锁定' },
  { value: 2, label: '预留锁定' }
]

const rooms = computed(() => board.value.rooms ?? [])

const floors = computed(() => {
  const set = new Set<number>()
  rooms.value.forEach((room) => {
    if (room.floor !== null && room.floor !== undefined) set.add(room.floor)
  })
  return Array.from(set).sort((a, b) => a - b)
})

const visibleFloors = computed(() =>
  floorFilter.value === 0 ? floors.value : floors.value.filter((f) => f === floorFilter.value)
)

const visibleRooms = computed(() =>
  floorFilter.value === 0
    ? rooms.value
    : rooms.value.filter((room) => room.floor === floorFilter.value)
)

const roomsOfFloor = (floor: number) =>
  visibleRooms.value
    .filter((room) => room.floor === floor)
    .sort((a, b) => a.roomNo!.localeCompare(b.roomNo!, 'zh-CN', { numeric: true }))

const statusCount = (status: number) =>
  rooms.value.filter((room) => room.status === status).length

const occupancyRate = computed(() => {
  const total = board.value.total ?? 0
  if (!total) return '0'
  const occupied = board.value.counts?.occupied ?? 0
  return (Math.round((occupied * 1000) / total) / 10).toFixed(1)
})

const summaryCards = computed(() => [
  {
    key: 'total',
    label: '房间总数',
    value: board.value.total ?? 0,
    hint: `${floors.value.length} 个楼层`,
    color: 'var(--el-text-color-primary)'
  },
  {
    key: 'available',
    label: '空闲',
    value: board.value.counts?.available ?? 0,
    hint: '可直接入住',
    color: '#67c23a'
  },
  {
    key: 'occupied',
    label: '已入住',
    value: board.value.counts?.occupied ?? 0,
    hint: '在住房间',
    color: '#f56c6c'
  },
  {
    key: 'cleaning',
    label: '待清洁',
    value: board.value.counts?.cleaning ?? 0,
    hint: '需保洁处理',
    color: '#e6a23c'
  },
  {
    key: 'maintenance',
    label: '维修中',
    value: board.value.counts?.maintenance ?? 0,
    hint: '暂停售卖',
    color: '#909399'
  }
])

const loadBoard = async () => {
  loading.value = true
  try {
    const [boardData, checkIns, checkOuts, bizDate] = await Promise.all([
      RoomApi.getRoomBoard(),
      OrderApi.getTodayCheckIns(),
      OrderApi.getDueCheckOuts(),
      AuditApi.getBusinessDate()
    ])
    board.value = boardData
    todayCheckIns.value = checkIns
    dueCheckOuts.value = checkOuts
    businessDate.value = bizDate || {}
  } finally {
    loading.value = false
  }
}

/** 房间操作 */
const roomDialogVisible = ref(false)
const currentRoom = ref<RoomApi.RoomVO>()

const openRoom = (room: RoomApi.RoomVO) => {
  currentRoom.value = room
  roomDialogVisible.value = true
}

const handleStatusChange = async (status: number) => {
  if (!currentRoom.value?.id) return
  try {
    await RoomApi.updateRoomStatus(currentRoom.value.id, status)
    message.success('房态已更新')
    roomDialogVisible.value = false
    await loadBoard()
  } catch {}
}

const handleLockChange = async (lockStatus: number) => {
  if (!currentRoom.value?.id) return
  try {
    await RoomApi.updateRoomLockStatus(currentRoom.value.id, lockStatus)
    message.success('锁定状态已更新')
    roomDialogVisible.value = false
    await loadBoard()
  } catch {}
}

const handleCreateOrder = () => {
  roomDialogVisible.value = false
  router.push({ name: 'HotelOrder', query: { roomId: String(currentRoom.value?.id) } })
}

const handleCheckIn = async (order: OrderApi.OrderVO) => {
  try {
    await message.confirm(`为 ${order.guestName} 办理 ${order.roomNo} 入住？`)
    await OrderApi.checkInOrder(order.id!)
    message.success('入住成功')
    await loadBoard()
  } catch {}
}

const handleCheckOut = async (order: OrderApi.OrderVO) => {
  try {
    await message.confirm(`为 ${order.roomNo} 办理退房？退房后将自动生成清洁任务。`)
    await OrderApi.checkOutOrder(order.id!)
    message.success('退房成功')
    await loadBoard()
  } catch {}
}

onMounted(() => {
  loadBoard()
})
</script>

<style lang="scss" scoped>
.room-card {
  width: 132px;
  padding: 10px;
  border: 2px solid transparent;
  border-left-width: 4px;
  border-radius: 6px;
  background: var(--el-fill-color-blank);
  box-shadow: 0 1px 4px rgb(0 0 0 / 8%);
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    box-shadow: 0 4px 12px rgb(0 0 0 / 14%);
    transform: translateY(-2px);
  }
}
</style>
