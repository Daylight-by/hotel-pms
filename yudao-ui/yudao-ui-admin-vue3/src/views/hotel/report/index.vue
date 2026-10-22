<template>
  <ContentWrap>
    <el-tabs v-model="activeTab">
      <!-- ==================== 日报 ==================== -->
      <el-tab-pane label="经营日报" name="daily">
        <div class="toolbar">
          <el-date-picker
            v-model="dailyDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择营业日"
            :clearable="false"
          />
          <el-button type="primary" :loading="dailyLoading" @click="loadDaily">查询</el-button>
          <el-button :loading="exporting" @click="exportDaily">导出近 7 天日报</el-button>
        </div>

        <div class="metric-row" v-loading="dailyLoading">
          <div class="metric-box">
            <div class="m-label">出租率 OCC</div>
            <div class="m-value">{{ fmt(daily?.occ) }}<span class="m-unit">%</span></div>
          </div>
          <div class="metric-box">
            <div class="m-label">平均房价 ADR</div>
            <div class="m-value"><span class="m-unit">¥</span>{{ fmt(daily?.adr) }}</div>
          </div>
          <div class="metric-box">
            <div class="m-label">RevPAR</div>
            <div class="m-value"><span class="m-unit">¥</span>{{ fmt(daily?.revpar) }}</div>
          </div>
          <div class="metric-box">
            <div class="m-label">总营收</div>
            <div class="m-value"><span class="m-unit">¥</span>{{ fmt(daily?.totalRevenue) }}</div>
          </div>
        </div>

        <el-row :gutter="14">
          <el-col :span="15">
            <el-card shadow="never" class="sub-card">
              <template #header>指标明细（{{ daily?.bizDate }}）</template>
              <el-descriptions :column="2" border size="default">
                <el-descriptions-item label="房费收入">{{ money(daily?.roomRevenue) }}</el-descriptions-item>
                <el-descriptions-item label="其他收入">{{ money(daily?.otherRevenue) }}</el-descriptions-item>
                <el-descriptions-item label="在住间夜">{{ daily?.soldNights ?? '--' }}</el-descriptions-item>
                <el-descriptions-item label="可售间夜">{{ daily?.availableNights ?? '--' }}</el-descriptions-item>
                <el-descriptions-item label="到店数">{{ daily?.arrivals ?? '--' }}</el-descriptions-item>
                <el-descriptions-item label="离店数">{{ daily?.departures ?? '--' }}</el-descriptions-item>
                <el-descriptions-item label="离店间夜">{{ daily?.departureNights ?? '--' }}</el-descriptions-item>
                <el-descriptions-item label="欠账余额">{{ money(daily?.arBalance) }}</el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-col>
          <el-col :span="9">
            <el-card shadow="never" class="sub-card">
              <template #header>收款结构</template>
              <Echart v-if="paymentOption.series?.length" :height="260" :options="paymentOption" />
              <el-empty v-else description="当日无收款" :image-size="70" />
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <!-- ==================== 月报 ==================== -->
      <el-tab-pane label="经营月报" name="monthly">
        <div class="toolbar">
          <el-date-picker
            v-model="month"
            type="month"
            value-format="YYYY-MM"
            placeholder="选择月份"
            :clearable="false"
          />
          <el-button type="primary" :loading="monthlyLoading" @click="loadMonthly">查询</el-button>
          <el-button :loading="exporting" @click="exportMonthly">导出月报</el-button>
        </div>

        <div class="metric-row" v-loading="monthlyLoading">
          <div class="metric-box">
            <div class="m-label">当月出租率</div>
            <div class="m-value">{{ fmt(summary?.occ) }}<span class="m-unit">%</span></div>
          </div>
          <div class="metric-box">
            <div class="m-label">当月 ADR</div>
            <div class="m-value"><span class="m-unit">¥</span>{{ fmt(summary?.adr) }}</div>
          </div>
          <div class="metric-box">
            <div class="m-label">当月 RevPAR</div>
            <div class="m-value"><span class="m-unit">¥</span>{{ fmt(summary?.revpar) }}</div>
          </div>
          <div class="metric-box">
            <div class="m-label">当月总营收</div>
            <div class="m-value"><span class="m-unit">¥</span>{{ fmt(summary?.totalRevenue) }}</div>
          </div>
        </div>

        <el-card shadow="never" class="sub-card">
          <template #header>逐日明细（{{ month }} · 共 {{ summary?.days || 0 }} 天）</template>
          <el-table :data="dailyRows" v-loading="monthlyLoading" border stripe :max-height="380">
            <el-table-column prop="bizDate" label="营业日" width="120" />
            <el-table-column label="出租率" width="100">
              <template #default="{ row }">{{ fmt(row.occ) }}%</template>
            </el-table-column>
            <el-table-column label="ADR" width="110">
              <template #default="{ row }">¥{{ fmt(row.adr) }}</template>
            </el-table-column>
            <el-table-column label="RevPAR" width="110">
              <template #default="{ row }">¥{{ fmt(row.revpar) }}</template>
            </el-table-column>
            <el-table-column label="房费收入">
              <template #default="{ row }">{{ money(row.roomRevenue) }}</template>
            </el-table-column>
            <el-table-column label="其他收入">
              <template #default="{ row }">{{ money(row.otherRevenue) }}</template>
            </el-table-column>
            <el-table-column label="总营收">
              <template #default="{ row }">{{ money(row.totalRevenue) }}</template>
            </el-table-column>
            <el-table-column prop="arrivals" label="到店" width="70" />
            <el-table-column prop="departures" label="离店" width="70" />
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </ContentWrap>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { EChartsOption } from 'echarts'
import { getDailyReportApi, getMonthlyReportApi, exportReportApi } from '@/api/hotel/report'
import download from '@/utils/download'

defineOptions({ name: 'HotelReport' })

const activeTab = ref('daily')
const dailyDate = ref('')
const month = ref('')
const dailyLoading = ref(false)
const monthlyLoading = ref(false)
const exporting = ref(false)
const daily = ref<any>(null)
const monthly = ref<any>(null)

const todayStr = () => {
  const d = new Date()
  const m = `${d.getMonth() + 1}`.padStart(2, '0')
  const day = `${d.getDate()}`.padStart(2, '0')
  return `${d.getFullYear()}-${m}-${day}`
}

const loadDaily = async () => {
  dailyLoading.value = true
  try {
    daily.value = await getDailyReportApi({ bizDate: dailyDate.value || todayStr() })
  } finally {
    dailyLoading.value = false
  }
}

const loadMonthly = async () => {
  monthlyLoading.value = true
  try {
    monthly.value = await getMonthlyReportApi({ month: month.value || todayStr().slice(0, 7) })
  } finally {
    monthlyLoading.value = false
  }
}

const dailyRows = computed(() => monthly.value?.days || [])
const summary = computed(() => monthly.value?.summary || null)

const paymentOption = computed<EChartsOption>(() => {
  const structure: any[] = daily.value?.paymentStructure || []
  return {
    tooltip: { trigger: 'item', formatter: '{b}: ¥{c} ({d}%)' },
    legend: { bottom: 0, textStyle: { color: '#606266', fontSize: 11 } },
    series: [
      {
        name: '收款结构',
        type: 'pie',
        radius: ['38%', '62%'],
        center: ['50%', '44%'],
        itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 1 },
        label: { formatter: '{b}\n¥{c}', fontSize: 11 },
        data: structure.map((s: any) => ({ name: s.name, value: Number(s.amount) }))
      }
    ]
  }
})

const fmt = (v: any) => (v === null || v === undefined ? '--' : Number(v).toLocaleString('zh-CN', { maximumFractionDigits: 2 }))
const money = (v: any) => (v === null || v === undefined ? '--' : '¥' + Number(v).toLocaleString('zh-CN', { maximumFractionDigits: 2 }))

const exportDaily = async () => {
  exporting.value = true
  try {
    const end = dailyDate.value || todayStr()
    const begin = new Date(end)
    begin.setDate(begin.getDate() - 6)
    const b = `${begin.getFullYear()}-${`${begin.getMonth() + 1}`.padStart(2, '0')}-${`${begin.getDate()}`.padStart(2, '0')}`
    const res = await exportReportApi({ beginDate: b, endDate: end, type: 'daily' })
    download.excel(res, '酒店经营日报.xls')
    ElMessage.success('日报导出成功')
  } finally {
    exporting.value = false
  }
}

const exportMonthly = async () => {
  exporting.value = true
  try {
    const end = month.value || todayStr().slice(0, 7)
    const res = await exportReportApi({ beginDate: end + '-01', endDate: end + '-28', type: 'monthly' })
    download.excel(res, '酒店经营月报.xls')
    ElMessage.success('月报导出成功')
  } finally {
    exporting.value = false
  }
}

onMounted(() => {
  dailyDate.value = todayStr()
  month.value = todayStr().slice(0, 7)
  loadDaily()
  loadMonthly()
})
</script>

<style scoped lang="scss">
.toolbar {
  margin-bottom: 14px;
  display: flex;
  gap: 10px;
  align-items: center;
}
.metric-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-bottom: 14px;
  .metric-box {
    background: linear-gradient(135deg, #f0f7ff, #e8f2ff);
    border: 1px solid #dbeafe;
    border-radius: 8px;
    padding: 14px 18px;
    .m-label { font-size: 13px; color: #5b7294; }
    .m-value {
      font-size: 26px;
      font-weight: 700;
      color: #1d4ed8;
      margin-top: 4px;
      .m-unit { font-size: 13px; color: #6b8bc0; margin-right: 2px; }
    }
  }
}
.sub-card {
  margin-bottom: 14px;
  :deep(.el-card__header) { font-weight: 600; }
}
</style>
