<template>
  <div v-loading="loading">
    <!-- 区间筛选 -->
    <ContentWrap>
      <div class="flex flex-wrap items-center gap-12px">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          value-format="YYYY-MM-DD"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :shortcuts="shortcuts"
          class="!w-280px"
          @change="loadAll"
        />
        <el-radio-group v-model="trendType" size="small" @change="loadTrend">
          <el-radio-button value="day">按日</el-radio-button>
          <el-radio-button value="month">按月</el-radio-button>
        </el-radio-group>
        <span class="text-12px text-gray-400">
          可售 {{ summary.availableNights || 0 }} 间夜 · 已售 {{ summary.soldNights || 0 }} 间夜 ·
          订单 {{ summary.orderCount || 0 }} 单
        </span>
        <div class="flex-grow" />
        <el-button @click="loadAll"><Icon icon="ep:refresh" class="mr-5px" /> 刷新</el-button>
      </div>
    </ContentWrap>

    <!-- 核心指标 -->
    <el-row :gutter="12" class="mb-12px">
      <el-col v-for="card in metricCards" :key="card.label" :span="6">
        <el-card shadow="hover" :body-style="{ padding: '18px' }">
          <div class="flex items-center justify-between">
            <div>
              <div class="text-13px text-gray-500">{{ card.label }}</div>
              <div class="mt-8px text-26px font-bold" :style="{ color: card.color }">
                {{ card.value }}
              </div>
              <div class="mt-6px text-12px text-gray-400">{{ card.hint }}</div>
            </div>
            <Icon :icon="card.icon" :size="34" :style="{ color: card.color, opacity: 0.25 }" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" class="mb-12px">
      <el-col :span="16">
        <ContentWrap title="房费收入与出租率趋势">
          <Echart :height="340" :options="trendOption" />
        </ContentWrap>
      </el-col>
      <el-col :span="8">
        <ContentWrap title="客源结构">
          <Echart v-if="hasGuestSource" :height="340" :options="guestSourceOption" />
          <el-empty v-else description="区间内暂无订单" :image-size="80" />
        </ContentWrap>
      </el-col>
    </el-row>

    <el-row :gutter="12">
      <el-col :span="8">
        <ContentWrap title="收款结构">
          <Echart v-if="hasPaymentSource" :height="300" :options="paymentSourceOption" />
          <el-empty v-else description="区间内暂无收款" :image-size="80" />
        </ContentWrap>
      </el-col>
      <el-col :span="8">
        <ContentWrap title="房态分布">
          <Echart :height="300" :options="roomOption" />
        </ContentWrap>
      </el-col>
      <el-col :span="8">
        <ContentWrap title="订单量趋势">
          <Echart :height="300" :options="orderOption" />
        </ContentWrap>
      </el-col>
    </el-row>
  </div>
</template>

<script lang="ts" setup>
import type { EChartsOption } from 'echarts'
import { formatMoney } from '@/utils/hotel'
import * as StatisticsApi from '@/api/hotel/statistics'

defineOptions({ name: 'HotelStatistics' })

const loading = ref(false)
const trendType = ref<'day' | 'month'>('day')
const summary = ref<Partial<StatisticsApi.HotelSummaryVO>>({})
const trend = ref<StatisticsApi.TrendRangePointVO[]>([])
const source = ref<StatisticsApi.SourceVO>({ guestSource: [], paymentSource: [] })
const overview = ref<Partial<StatisticsApi.HotelOverviewVO>>({})

const fmtDate = (date: Date) => {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

const dateRange = ref<[string, string]>(() => {
  const end = new Date()
  const begin = new Date()
  begin.setDate(begin.getDate() - 6)
  return [fmtDate(begin), fmtDate(end)]
})

const shortcuts = [
  {
    text: '近 7 天',
    value: () => {
      const end = new Date()
      const begin = new Date()
      begin.setDate(begin.getDate() - 6)
      return [begin, end]
    }
  },
  {
    text: '近 30 天',
    value: () => {
      const end = new Date()
      const begin = new Date()
      begin.setDate(begin.getDate() - 29)
      return [begin, end]
    }
  },
  {
    text: '本月',
    value: () => {
      const now = new Date()
      return [new Date(now.getFullYear(), now.getMonth(), 1), now]
    }
  }
]

const metricCards = computed(() => [
  {
    label: 'OCC 出租率',
    value: (summary.value.occ ?? 0).toFixed(2) + '%',
    hint: `已售 ${summary.value.soldNights ?? 0} / 可售 ${summary.value.availableNights ?? 0} 间夜`,
    icon: 'ep:pie-chart',
    color: '#409eff'
  },
  {
    label: 'ADR 平均房价',
    value: '¥ ' + formatMoney(summary.value.adr),
    hint: '房费收入 ÷ 已售间夜',
    icon: 'ep:price-tag',
    color: '#e6a23c'
  },
  {
    label: 'RevPAR 每间可售房收入',
    value: '¥ ' + formatMoney(summary.value.revpar),
    hint: '房费收入 ÷ 可售间夜 = OCC × ADR',
    icon: 'ep:trend-charts',
    color: '#67c23a'
  },
  {
    label: '房费收入',
    value: '¥ ' + formatMoney(summary.value.roomRevenue),
    hint: `总营收 ¥${formatMoney(summary.value.totalRevenue)} · 平均停留 ${
      summary.value.avgStay ?? 0
    } 晚`,
    icon: 'ep:money',
    color: '#f56c6c'
  }
])

const trendOption = computed<EChartsOption>(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['房费收入', '出租率'], top: 0 },
  grid: { left: 60, right: 55, top: 40, bottom: 30 },
  xAxis: { type: 'category', data: trend.value.map((i) => i.date) },
  yAxis: [
    { type: 'value', name: '收入(元)' },
    { type: 'value', name: '出租率(%)', max: 100, splitLine: { show: false } }
  ],
  series: [
    {
      name: '房费收入',
      type: 'bar',
      data: trend.value.map((i) => i.revenue),
      itemStyle: { color: '#f56c6c', borderRadius: [4, 4, 0, 0] },
      barMaxWidth: 22
    },
    {
      name: '出租率',
      type: 'line',
      yAxisIndex: 1,
      smooth: true,
      data: trend.value.map((i) => i.occupancyRate),
      itemStyle: { color: '#409eff' }
    }
  ]
}))

const hasGuestSource = computed(() => source.value.guestSource?.some((i) => i.value > 0))
const hasPaymentSource = computed(() => source.value.paymentSource?.length > 0)

const guestSourceOption = computed<EChartsOption>(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} 单 ({d}%)' },
  legend: { bottom: 0 },
  series: [
    {
      name: '客源',
      type: 'pie',
      radius: ['40%', '66%'],
      label: { formatter: '{b}\n{d}%' },
      data: (source.value.guestSource || [])
        .filter((i) => i.value > 0)
        .map((i) => ({ name: i.name, value: i.value }))
    }
  ]
}))

const paymentSourceOption = computed<EChartsOption>(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: ¥{c} ({d}%)' },
  legend: { bottom: 0 },
  series: [
    {
      name: '收款',
      type: 'pie',
      radius: '62%',
      label: { formatter: '{b}: ¥{c}' },
      data: (source.value.paymentSource || []).map((i) => ({ name: i.name, value: i.value }))
    }
  ]
}))

const roomOption = computed<EChartsOption>(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0 },
  series: [
    {
      name: '房态',
      type: 'pie',
      radius: '62%',
      label: { formatter: '{b}: {c}' },
      data: [
        { name: '空闲', value: overview.value.availableRooms ?? 0, itemStyle: { color: '#67c23a' } },
        { name: '已入住', value: overview.value.occupiedRooms ?? 0, itemStyle: { color: '#f56c6c' } },
        { name: '待清洁', value: overview.value.cleaningRooms ?? 0, itemStyle: { color: '#e6a23c' } },
        { name: '维修中', value: overview.value.maintenanceRooms ?? 0, itemStyle: { color: '#909399' } }
      ].filter((i) => i.value > 0)
    }
  ]
}))

const orderOption = computed<EChartsOption>(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 45, right: 20, top: 30, bottom: 30 },
  xAxis: { type: 'category', data: trend.value.map((i) => i.date) },
  yAxis: { type: 'value', name: '订单数', minInterval: 1 },
  series: [
    {
      name: '订单数',
      type: 'line',
      smooth: true,
      areaStyle: { opacity: 0.15 },
      data: trend.value.map((i) => i.orders),
      itemStyle: { color: '#67c23a' }
    }
  ]
}))

const loadTrend = async () => {
  const [beginDate, endDate] = dateRange.value
  trend.value = await StatisticsApi.getTrendRange({ beginDate, endDate, type: trendType.value })
}

const loadAll = async () => {
  loading.value = true
  try {
    const [beginDate, endDate] = dateRange.value
    const [summaryData, sourceData, overviewData] = await Promise.all([
      StatisticsApi.getSummary({ beginDate, endDate }),
      StatisticsApi.getSource({ beginDate, endDate }),
      StatisticsApi.getOverview()
    ])
    summary.value = summaryData || {}
    source.value = sourceData || { guestSource: [], paymentSource: [] }
    overview.value = overviewData || {}
    await loadTrend()
  } finally {
    loading.value = false
  }
}

onMounted(loadAll)
</script>
