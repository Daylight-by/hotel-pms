<template>
  <ContentWrap>
    <el-row :gutter="12">
      <el-col :span="14">
        <el-card shadow="never" class="biz-card">
          <div class="flex items-center">
            <div>
              <div class="text-13px text-gray-500">当前营业日</div>
              <div class="mt-6px text-30px font-bold text-[var(--el-color-primary)]">
                {{ businessDate.bizDate || '-' }}
              </div>
              <div class="mt-4px text-12px text-gray-400">
                自然日 {{ businessDate.today || '-' }} ·
                {{
                  businessDate.bizDate === businessDate.today
                    ? '与自然日一致'
                    : '营业日与自然日相差，属正常（夜审后营业日推进）'
                }}
              </div>
            </div>
            <div class="flex-grow" />
            <div class="text-right">
              <el-tag :type="businessDate.status === 10 ? 'primary' : 'success'" effect="dark">
                {{ businessDate.statusName || '进行中' }}
              </el-tag>
              <div class="mt-10px text-12px text-gray-400">
                上次夜审：{{ businessDate.auditTime ? formatDate(businessDate.auditTime) : '暂无' }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never" class="biz-card">
          <div class="flex h-full flex-col justify-between">
            <div>
              <div class="text-13px font-bold">夜审（Night Audit）</div>
              <div class="mt-6px text-12px text-gray-500 leading-5">
                过账全部在住订单的当日房费 → 生成夜审记录 → 营业日 +1。
                存在「应离未离」订单时会被拦截，需先处理退房或续住。
              </div>
            </div>
            <el-button
              class="mt-12px"
              type="primary"
              size="large"
              :loading="running"
              @click="handleRun"
              v-hasPermi="['hotel:audit:audit']"
            >
              <Icon icon="ep:moon-night" class="mr-5px" /> 执行夜审
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </ContentWrap>

  <ContentWrap title="夜审历史">
    <el-table v-loading="loading" :data="list">
      <el-table-column label="营业日" prop="bizDate" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 20 ? 'success' : row.status === 30 ? 'danger' : 'primary'">
            {{ row.statusName }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="过账房费" width="130" align="right">
        <template #default="{ row }">¥{{ formatMoney(row.roomChargeTotal) }}</template>
      </el-table-column>
      <el-table-column label="过账房数" prop="postedRoomCount" width="110" align="center" />
      <el-table-column label="过账明细" prop="postedItemCount" width="110" align="center" />
      <el-table-column
        label="开始时间"
        prop="startTime"
        width="180"
        :formatter="dateFormatter"
      />
      <el-table-column label="结束时间" prop="endTime" width="180" :formatter="dateFormatter" />
      <el-table-column label="耗时" width="100" align="center">
        <template #default="{ row }">
          {{ row.durationMs ? (row.durationMs / 1000).toFixed(2) + 's' : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="失败原因" prop="failReason" min-width="200" show-overflow-tooltip />
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
import * as AuditApi from '@/api/hotel/audit'

defineOptions({ name: 'HotelAudit' })

const message = useMessage()
const loading = ref(false)
const running = ref(false)
const businessDate = ref<AuditApi.BusinessDateVO>({})
const list = ref<AuditApi.NightAuditVO[]>([])
const total = ref(0)
const queryParams = reactive({ pageNo: 1, pageSize: 10 })

const getBusinessDate = async () => {
  businessDate.value = await AuditApi.getBusinessDate()
}

const getList = async () => {
  loading.value = true
  try {
    const data = await AuditApi.getNightAuditPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleRun = async () => {
  try {
    await message.confirm(
      `确认对营业日 ${businessDate.value.bizDate} 执行夜审？夜审会过账全部在住订单的当日房费，并把营业日推进到次日。`
    )
  } catch {
    return
  }
  running.value = true
  try {
    const result: any = await AuditApi.runNightAudit()
    message.success(
      `夜审完成：过账房费 ¥${formatMoney(result?.roomChargeTotal ?? 0)}，房数 ${
        result?.postedRoomCount ?? 0
      }`
    )
    await Promise.all([getBusinessDate(), getList()])
  } finally {
    running.value = false
  }
}

onMounted(() => {
  getBusinessDate()
  getList()
})
</script>

<style scoped>
.biz-card {
  height: 100%;
  min-height: 140px;
}
</style>
