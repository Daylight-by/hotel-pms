<template>
  <ContentWrap>
    <el-card shadow="never" class="shift-card">
      <template v-if="current">
        <div class="flex flex-wrap items-center">
          <div>
            <div class="text-13px text-gray-500">当前班次</div>
            <div class="mt-4px text-22px font-bold">{{ current.shiftNo }}</div>
            <div class="mt-4px text-12px text-gray-400">
              营业日 {{ current.bizDate }} · 开班 {{ formatDate(current.startTime) }} · 收银员
              {{ current.operatorName || '-' }}
            </div>
          </div>
          <div class="flex-grow" />
          <div class="flex flex-wrap gap-10px">
            <div v-for="item in amountCards" :key="item.label" class="amount-item">
              <span class="label">{{ item.label }}</span>
              <span class="value">¥{{ formatMoney(item.value) }}</span>
            </div>
            <div class="amount-item total">
              <span class="label">合计</span>
              <span class="value">¥{{ formatMoney(current.totalAmount) }}</span>
            </div>
          </div>
          <el-button
            class="ml-16px"
            type="danger"
            @click="handleClose"
            v-hasPermi="['hotel:shift:update']"
          >
            交班
          </el-button>
        </div>
      </template>
      <template v-else>
        <div class="flex items-center">
          <div>
            <div class="text-13px text-gray-500">当前无进行中的班次</div>
            <div class="mt-4px text-12px text-gray-400">
              开班后才能统计当班收款并交班；同一时间只能有一个班次。
            </div>
          </div>
          <div class="flex-grow" />
          <el-button
            type="primary"
            size="large"
            @click="handleOpen"
            v-hasPermi="['hotel:shift:create']"
          >
            <Icon icon="ep:video-play" class="mr-5px" /> 开班
          </el-button>
        </div>
      </template>
    </el-card>
  </ContentWrap>

  <ContentWrap title="班次历史">
    <el-table v-loading="loading" :data="list">
      <el-table-column label="班次号" prop="shiftNo" width="160" />
      <el-table-column label="营业日" prop="bizDate" width="110" />
      <el-table-column label="收银员" prop="operatorName" width="110" />
      <el-table-column label="开班时间" prop="startTime" width="180" :formatter="dateFormatter" />
      <el-table-column label="交班时间" prop="endTime" width="180" :formatter="dateFormatter" />
      <el-table-column label="现金" width="110" align="right">
        <template #default="{ row }">¥{{ formatMoney(row.cashAmount) }}</template>
      </el-table-column>
      <el-table-column label="银行卡" width="110" align="right">
        <template #default="{ row }">¥{{ formatMoney(row.cardAmount) }}</template>
      </el-table-column>
      <el-table-column label="微信" width="110" align="right">
        <template #default="{ row }">¥{{ formatMoney(row.wechatAmount) }}</template>
      </el-table-column>
      <el-table-column label="支付宝" width="110" align="right">
        <template #default="{ row }">¥{{ formatMoney(row.alipayAmount) }}</template>
      </el-table-column>
      <el-table-column label="其他" width="110" align="right">
        <template #default="{ row }">¥{{ formatMoney(row.otherAmount) }}</template>
      </el-table-column>
      <el-table-column label="合计" width="120" align="right">
        <template #default="{ row }">
          <span class="font-bold">¥{{ formatMoney(row.totalAmount) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 10 ? 'primary' : 'success'">{{ row.statusName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" prop="remark" min-width="140" show-overflow-tooltip />
    </el-table>
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>
</template>

<script lang="ts" setup>
import { formatMoney } from '@/utils/hotel'
import { dateFormatter, formatDate } from '@/utils/formatTime'
import * as ShiftApi from '@/api/hotel/shift'

defineOptions({ name: 'HotelShift' })

const message = useMessage()
const loading = ref(false)
const current = ref<ShiftApi.ShiftVO | null>(null)
const list = ref<ShiftApi.ShiftVO[]>([])
const total = ref(0)
const queryParams = reactive({ pageNo: 1, pageSize: 10 })

const amountCards = computed(() => [
  { label: '现金', value: current.value?.cashAmount ?? 0 },
  { label: '银行卡', value: current.value?.cardAmount ?? 0 },
  { label: '微信', value: current.value?.wechatAmount ?? 0 },
  { label: '支付宝', value: current.value?.alipayAmount ?? 0 },
  { label: '其他', value: current.value?.otherAmount ?? 0 }
])

const loadCurrent = async () => {
  current.value = await ShiftApi.getCurrentShift()
}

const getList = async () => {
  loading.value = true
  try {
    const data = await ShiftApi.getShiftPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleOpen = async () => {
  try {
    await message.confirm('确认开班？同一时间只能有一个进行中的班次。')
  } catch {
    return
  }
  await ShiftApi.openShift({})
  message.success('开班成功')
  await Promise.all([loadCurrent(), getList()])
}

const handleClose = async () => {
  if (!current.value?.id) return
  try {
    await message.confirm('确认交班？系统将统计本班各支付方式的收款合计。')
  } catch {
    return
  }
  await ShiftApi.closeShift(current.value.id, {})
  message.success('交班成功')
  await Promise.all([loadCurrent(), getList()])
}

onMounted(() => {
  loadCurrent()
  getList()
})
</script>

<style scoped>
.shift-card {
  min-height: 120px;
}
.amount-item {
  display: flex;
  flex-direction: column;
  padding: 6px 12px;
  border-radius: 6px;
  background: var(--el-fill-color-light);
  min-width: 90px;
}
.amount-item.total {
  background: var(--el-color-primary-light-9);
}
.amount-item .label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.amount-item .value {
  font-size: 16px;
  font-weight: 700;
}
</style>
