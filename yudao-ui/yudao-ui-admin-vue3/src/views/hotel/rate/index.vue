<template>
  <ContentWrap>
    <div class="flex flex-wrap items-center gap-12px">
      <el-button-group>
        <el-button @click="shiftMonth(-1)"><Icon icon="ep:arrow-left" /></el-button>
        <el-button @click="resetMonth">{{ monthLabel }}</el-button>
        <el-button @click="shiftMonth(1)"><Icon icon="ep:arrow-right" /></el-button>
      </el-button-group>
      <el-date-picker
        v-model="monthPicker"
        type="month"
        placeholder="选择月份"
        class="!w-160px"
        @change="onMonthPicked"
      />
      <el-select v-model="filterRoomTypeId" placeholder="全部房型" clearable class="!w-180px">
        <el-option
          v-for="item in board"
          :key="item.roomTypeId"
          :label="item.roomTypeName"
          :value="item.roomTypeId"
        />
      </el-select>
      <el-tag type="info" effect="plain">拖拽选择日期区间可批量改价</el-tag>
      <div class="flex-grow" />
      <el-button type="primary" @click="openBatch()" v-hasPermi="['hotel:rate:update']">
        <Icon icon="ep:edit" class="mr-5px" /> 批量改价 / 改库存
      </el-button>
      <el-button @click="loadBoard"><Icon icon="ep:refresh" class="mr-5px" /> 刷新</el-button>
    </div>
  </ContentWrap>

  <ContentWrap v-loading="loading">
    <div class="overflow-x-auto">
      <table class="rate-table">
        <thead>
          <tr>
            <th class="sticky-col">房型 / 日期</th>
            <th v-for="day in days" :key="day" :class="{ weekend: isWeekend(day) }">
              <div class="text-12px">{{ day.slice(5) }}</div>
              <div class="text-11px text-gray-400">{{ weekdayOf(day) }}</div>
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in visibleBoard" :key="row.roomTypeId">
            <td class="sticky-col">
              <div class="font-bold">{{ row.roomTypeName }}</div>
              <div class="text-11px text-gray-400">基准 ¥{{ row.basePrice }} · {{ row.roomCount }} 间</div>
            </td>
            <td
              v-for="cell in row.days"
              :key="cell.bizDate"
              class="rate-cell"
              :class="{
                closed: cell.status === 20,
                selected: isSelected(row.roomTypeId, cell.bizDate),
                weekend: isWeekend(cell.bizDate)
              }"
              @mousedown="startSelect(row.roomTypeId, cell.bizDate)"
              @mouseenter="moveSelect(row.roomTypeId, cell.bizDate)"
              @mouseup="finishSelect()"
            >
              <div class="price">¥{{ formatMoney(cell.price, 0) }}</div>
              <div class="stock" :class="{ low: cell.status === 10 && cell.remaining <= 1 }">
                {{ cell.status === 20 ? '关房' : '剩 ' + cell.remaining }}
              </div>
            </td>
          </tr>
          <tr v-if="!visibleBoard.length">
            <td :colspan="days.length + 1" class="py-30px text-center text-gray-400">暂无房型数据</td>
          </tr>
        </tbody>
      </table>
    </div>
    <div class="mt-10px flex items-center gap-16px text-12px text-gray-500">
      <span><span class="legend normal" /> 开房</span>
      <span><span class="legend low" /> 余量紧张</span>
      <span><span class="legend closed" /> 关房（不可售）</span>
      <span>合计可售 {{ totalAvailable }} 间夜 · 已售 {{ totalSold }} 间夜</span>
    </div>
  </ContentWrap>

  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" append-to-body>
    <el-form ref="formRef" :model="form" label-width="90px">
      <el-form-item label="房型" required>
        <el-select v-model="form.roomTypeId" class="w-full" :disabled="!!form.fixedRoomType">
          <el-option
            v-for="item in board"
            :key="item.roomTypeId"
            :label="item.roomTypeName"
            :value="item.roomTypeId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="日期区间" required>
        <el-date-picker
          v-model="range"
          type="daterange"
          value-format="YYYY-MM-DD"
          start-placeholder="开始"
          end-placeholder="结束"
          class="!w-full"
        />
      </el-form-item>
      <el-form-item label="房价" required>
        <el-input-number v-model="form.price" :min="0" :precision="2" :step="10" class="!w-200px" />
      </el-form-item>
      <el-form-item label="总库存">
        <el-input-number v-model="form.totalInventory" :min="0" :precision="0" class="!w-200px" />
      </el-form-item>
      <el-form-item label="售卖状态">
        <el-radio-group v-model="form.status">
          <el-radio :value="10">开房</el-radio>
          <el-radio :value="20">关房</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="如：周末价 / 装修关房" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { formatMoney } from '@/utils/hotel'
import * as RateApi from '@/api/hotel/rate'

defineOptions({ name: 'HotelRate' })

const message = useMessage()
const loading = ref(false)
const submitting = ref(false)
const board = ref<RateApi.RateBoardVO[]>([])
const filterRoomTypeId = ref<number>()
const currentMonth = ref(new Date())
const monthPicker = ref<Date>(new Date())

const days = computed(() => board.value[0]?.days?.map((d) => d.bizDate) ?? [])
const visibleBoard = computed(() =>
  filterRoomTypeId.value
    ? board.value.filter((i) => i.roomTypeId === filterRoomTypeId.value)
    : board.value
)
const monthLabel = computed(
  () => `${currentMonth.value.getFullYear()} 年 ${currentMonth.value.getMonth() + 1} 月`
)
const totalAvailable = computed(() =>
  visibleBoard.value.reduce(
    (sum, row) => sum + row.days.reduce((s, d) => s + (d.status === 20 ? 0 : d.remaining), 0),
    0
  )
)
const totalSold = computed(() =>
  visibleBoard.value.reduce((sum, row) => sum + row.days.reduce((s, d) => s + d.soldInventory, 0), 0)
)

const monthRange = () => {
  const y = currentMonth.value.getFullYear()
  const m = currentMonth.value.getMonth()
  const begin = new Date(y, m, 1)
  const end = new Date(y, m + 1, 0)
  return { beginDate: fmt(begin), endDate: fmt(end) }
}

const fmt = (date: Date) => {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

const isWeekend = (date: string) => {
  const day = new Date(date).getDay()
  return day === 0 || day === 6
}

const weekdayOf = (date: string) => ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][new Date(date).getDay()]

const loadBoard = async () => {
  loading.value = true
  try {
    board.value = await RateApi.getRateBoard(monthRange())
  } finally {
    loading.value = false
  }
}

const shiftMonth = (delta: number) => {
  currentMonth.value = new Date(
    currentMonth.value.getFullYear(),
    currentMonth.value.getMonth() + delta,
    1
  )
  monthPicker.value = currentMonth.value
  loadBoard()
}

const resetMonth = () => {
  currentMonth.value = new Date()
  monthPicker.value = new Date()
  loadBoard()
}

const onMonthPicked = (value: Date) => {
  if (value) {
    currentMonth.value = new Date(value.getFullYear(), value.getMonth(), 1)
    loadBoard()
  }
}

/* ---------------- 拖拽选择 ---------------- */
const selecting = ref(false)
const selectStart = ref<{ roomTypeId: number; date: string }>()
const selectEnd = ref<{ roomTypeId: number; date: string }>()

const isSelected = (roomTypeId: number, date: string) => {
  if (!selectStart.value || !selectEnd.value) return false
  if (selectStart.value.roomTypeId !== roomTypeId) return false
  const a = selectStart.value.date
  const b = selectEnd.value.date
  const [from, to] = a <= b ? [a, b] : [b, a]
  return date >= from && date <= to
}

const startSelect = (roomTypeId: number, date: string) => {
  selecting.value = true
  selectStart.value = { roomTypeId, date }
  selectEnd.value = { roomTypeId, date }
}

const moveSelect = (roomTypeId: number, date: string) => {
  if (!selecting.value || !selectStart.value) return
  if (selectStart.value.roomTypeId !== roomTypeId) return
  selectEnd.value = { roomTypeId, date }
}

const finishSelect = () => {
  if (!selecting.value) return
  selecting.value = false
  if (!selectStart.value || !selectEnd.value) return
  const a = selectStart.value.date
  const b = selectEnd.value.date
  const [from, to] = a <= b ? [a, b] : [b, a]
  openBatch(selectStart.value.roomTypeId, from, to)
  selectStart.value = undefined
  selectEnd.value = undefined
}

/* ---------------- 批量维护弹窗 ---------------- */
const dialogVisible = ref(false)
const dialogTitle = ref('批量维护房价')
const formRef = ref()
const range = ref<[string, string]>()
const form = reactive({
  roomTypeId: undefined as number | undefined,
  fixedRoomType: false,
  price: 300,
  totalInventory: 5,
  status: 10,
  remark: ''
})

const openBatch = (roomTypeId?: number, begin?: string, end?: string) => {
  if (!board.value.length) {
    message.warning('暂无房型数据')
    return
  }
  const defaultType = roomTypeId ?? visibleBoard.value[0]?.roomTypeId ?? board.value[0].roomTypeId
  const row = board.value.find((i) => i.roomTypeId === defaultType)
  const month = monthRange()
  range.value = [begin ?? month.beginDate, end ?? month.endDate]
  form.roomTypeId = defaultType
  form.fixedRoomType = !!roomTypeId
  form.price = row?.days?.[0]?.price ?? row?.basePrice ?? 300
  form.totalInventory = row?.days?.[0]?.totalInventory ?? row?.roomCount ?? 5
  form.status = 10
  form.remark = ''
  dialogTitle.value = begin && end && begin === end ? `维护 ${begin} 房价` : '批量维护房价 / 库存'
  dialogVisible.value = true
}

const submit = async () => {
  if (!form.roomTypeId) {
    message.warning('请选择房型')
    return
  }
  if (!range.value || range.value.length !== 2) {
    message.warning('请选择日期区间')
    return
  }
  submitting.value = true
  try {
    await RateApi.batchSaveRate({
      roomTypeId: form.roomTypeId,
      beginDate: range.value[0],
      endDate: range.value[1],
      price: form.price,
      totalInventory: form.totalInventory,
      status: form.status,
      remark: form.remark
    })
    message.success('保存成功')
    dialogVisible.value = false
    await loadBoard()
  } finally {
    submitting.value = false
  }
}

onMounted(loadBoard)
</script>

<style scoped>
.rate-table {
  border-collapse: collapse;
  min-width: 100%;
  font-size: 12px;
}
.rate-table th,
.rate-table td {
  border: 1px solid var(--el-border-color-lighter);
  padding: 6px 4px;
  text-align: center;
  min-width: 78px;
}
.rate-table th {
  background: var(--el-fill-color-light);
  font-weight: 600;
}
.sticky-col {
  position: sticky;
  left: 0;
  z-index: 2;
  background: var(--el-bg-color);
  text-align: left;
  padding-left: 10px;
  min-width: 150px;
}
.rate-table th.sticky-col {
  background: var(--el-fill-color-light);
}
.rate-cell {
  cursor: pointer;
  user-select: none;
  transition: background 0.12s;
}
.rate-cell:hover {
  background: var(--el-color-primary-light-9);
}
.rate-cell .price {
  font-weight: 600;
  color: var(--el-text-color-primary);
}
.rate-cell .stock {
  margin-top: 2px;
  color: var(--el-color-success);
}
.rate-cell .stock.low {
  color: var(--el-color-danger);
}
.rate-cell.closed {
  background: var(--el-fill-color);
  color: var(--el-text-color-disabled);
}
.rate-cell.closed .price,
.rate-cell.closed .stock {
  color: var(--el-text-color-disabled);
}
.rate-cell.selected {
  outline: 2px solid var(--el-color-primary);
  outline-offset: -2px;
}
.weekend {
  background: var(--el-color-warning-light-9);
}
.legend {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 2px;
  margin-right: 4px;
  vertical-align: middle;
}
.legend.normal {
  background: var(--el-color-success-light-3);
}
.legend.low {
  background: var(--el-color-danger-light-3);
}
.legend.closed {
  background: var(--el-fill-color-dark);
}
</style>
