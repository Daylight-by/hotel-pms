<template>
  <ContentWrap>
    <el-form :inline="true" label-width="76px" class="-mb-15px">
      <el-form-item label="房型">
        <el-select v-model="roomTypeId" placeholder="选择房型" class="!w-200px" @change="loadSuggestions">
          <el-option v-for="item in roomTypeList" :key="item.id" :label="item.name" :value="item.id!" />
        </el-select>
      </el-form-item>
      <el-form-item label="日期区间">
        <el-date-picker
          v-model="range"
          type="daterange"
          value-format="YYYY-MM-DD"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          class="!w-260px"
          @change="loadSuggestions"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="generating" @click="handleGenerate" v-hasPermi="['hotel:pricing:create']">
          <Icon icon="ep:magic-stick" class="mr-5px" /> 生成建议
        </el-button>
        <el-button @click="loadSuggestions"><Icon icon="ep:refresh" class="mr-5px" /> 刷新</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="定价建议" name="suggestion">
        <el-alert
          class="mb-10px"
          type="info"
          :closable="false"
          title="建议价 = 基线价 × 出租率系数 × 提前期系数 × 周末系数，并限制在基线价的 0.7~1.5 倍之间"
          description="采纳后会把建议价（或你修改后的价格）写回房价日历，前台与线上渠道立即生效。"
        />
        <el-table v-loading="loading" :data="suggestions" max-height="560">
          <el-table-column label="日期" prop="bizDate" width="120" fixed />
          <el-table-column label="基线价" width="100" align="right">
            <template #default="{ row }">¥{{ Number(row.basePrice).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column label="建议价" width="110" align="right">
            <template #default="{ row }">
              <span
                class="text-15px font-bold"
                :class="Number(row.suggestedPrice) >= Number(row.basePrice) ? 'text-red-500' : 'text-green-600'"
              >
                ¥{{ Number(row.suggestedPrice).toFixed(2) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="出租率系数" width="100" align="center">
            <template #default="{ row }">{{ Number(row.occFactor).toFixed(3) }}</template>
          </el-table-column>
          <el-table-column label="提前期系数" width="100" align="center">
            <template #default="{ row }">{{ Number(row.advanceFactor).toFixed(3) }}</template>
          </el-table-column>
          <el-table-column label="周末系数" width="100" align="center">
            <template #default="{ row }">{{ Number(row.weekendFactor).toFixed(3) }}</template>
          </el-table-column>
          <el-table-column label="历史出租率" width="110" align="center">
            <template #default="{ row }">
              <span v-if="row.histOccupancy != null">{{ (Number(row.histOccupancy) * 100).toFixed(0) }}%</span>
              <span v-else class="text-gray-400">无数据</span>
            </template>
          </el-table-column>
          <el-table-column label="已售进度" width="110" align="center">
            <template #default="{ row }">
              <el-progress
                :percentage="Math.min(100, Math.round(Number(row.bookingPace || 0) * 100))"
                :stroke-width="8"
              />
            </template>
          </el-table-column>
          <el-table-column label="建议理由（人话）" prop="reason" min-width="320" show-overflow-tooltip />
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)">{{ row.statusName }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right" align="center">
            <template #default="{ row }">
              <template v-if="row.status === 10">
                <el-button link type="primary" @click="handleAdopt(row)" v-hasPermi="['hotel:pricing:update']">
                  采纳
                </el-button>
                <el-button link type="info" @click="handleIgnore(row)" v-hasPermi="['hotel:pricing:update']">
                  忽略
                </el-button>
              </template>
              <span v-else-if="row.status === 20" class="text-12px text-gray-500">
                已采纳 ¥{{ Number(row.adoptedPrice || 0).toFixed(0) }}
              </span>
              <span v-else class="text-12px text-gray-400">已忽略</span>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="回测对比（答辩用）" name="backtest">
        <el-form :inline="true" label-width="76px">
          <el-form-item label="回测区间">
            <el-date-picker
              v-model="btRange"
              type="daterange"
              value-format="YYYY-MM-DD"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              class="!w-260px"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="btLoading" @click="handleBacktest" v-hasPermi="['hotel:pricing:query']">
              <Icon icon="ep:data-analysis" class="mr-5px" /> 执行回测
            </el-button>
          </el-form-item>
        </el-form>

        <template v-if="backtest">
          <el-row :gutter="16" class="mb-16px">
            <el-col :span="8">
              <el-card shadow="never">
                <div class="text-12px text-gray-500">按原价的 RevPAR</div>
                <div class="mt-6px text-26px font-bold">¥{{ Number(backtest.actualRevpar).toFixed(2) }}</div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="never">
                <div class="text-12px text-gray-500">按建议价的 RevPAR</div>
                <div class="mt-6px text-26px font-bold">¥{{ Number(backtest.modelRevpar).toFixed(2) }}</div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="never">
                <div class="text-12px text-gray-500">提升比例</div>
                <div
                  class="mt-6px text-26px font-bold"
                  :class="Number(backtest.upliftRate) >= 0 ? 'text-red-500' : 'text-green-600'"
                >
                  {{ Number(backtest.upliftRate) >= 0 ? '+' : '' }}{{ Number(backtest.upliftRate).toFixed(2) }}%
                </div>
              </el-card>
            </el-col>
          </el-row>
          <el-alert
            class="mb-10px"
            type="warning"
            :closable="false"
            title="结论口径说明（答辩务必如实说明）"
            :description="backtest.assumption"
          />
          <Echart :height="320" :options="chartOption" />
          <el-table :data="backtest.days || []" size="small" max-height="360" class="mt-10px">
            <el-table-column label="日期" prop="bizDate" width="120" />
            <el-table-column label="原价" width="100" align="right">
              <template #default="{ row }">¥{{ Number(row.basePrice).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column label="建议价" width="100" align="right">
              <template #default="{ row }">¥{{ Number(row.suggestedPrice).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column label="出租率" width="120" align="center">
              <template #default="{ row }">
                {{ (Number(row.occupancy) * 100).toFixed(1) }}%
                <el-tooltip v-if="row.occupancyEstimated" content="该日无日历数据，出租率按 60% 估算">
                  <Icon icon="ep:warning" class="ml-4px text-orange-400" />
                </el-tooltip>
              </template>
            </el-table-column>
            <el-table-column label="原价 RevPAR" width="130" align="right">
              <template #default="{ row }">¥{{ Number(row.actualRevpar).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column label="建议价 RevPAR" width="140" align="right">
              <template #default="{ row }">¥{{ Number(row.modelRevpar).toFixed(2) }}</template>
            </el-table-column>
          </el-table>
        </template>
        <el-empty v-else description="选择历史区间后点「执行回测」，对比原价与建议价的 RevPAR" />
      </el-tab-pane>
    </el-tabs>
  </ContentWrap>
</template>

<script lang="ts" setup>
import type { EChartsOption } from 'echarts'
import * as PricingApi from '@/api/hotel/pricing'
import * as RoomTypeApi from '@/api/hotel/roomType'

defineOptions({ name: 'HotelPricing' })

const message = useMessage()
const loading = ref(false)
const generating = ref(false)
const activeTab = ref('suggestion')

const roomTypeList = ref<RoomTypeApi.RoomTypeVO[]>([])
const roomTypeId = ref<number>()
const range = ref<string[]>([])
const suggestions = ref<PricingApi.PriceSuggestionVO[]>([])

const btLoading = ref(false)
const btRange = ref<string[]>([])
const backtest = ref<PricingApi.BacktestVO>()

const fmt = (date: Date) =>
  `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`

const initRange = () => {
  const today = new Date()
  const begin = fmt(today)
  const end = new Date(today)
  end.setDate(end.getDate() + 13)
  range.value = [begin, fmt(end)]
  const btBegin = new Date(today)
  btBegin.setDate(btBegin.getDate() - 30)
  const btEnd = new Date(today)
  btEnd.setDate(btEnd.getDate() - 1)
  btRange.value = [fmt(btBegin), fmt(btEnd)]
}

const statusType = (status: number) => (status === 20 ? 'success' : status === 30 ? 'info' : 'warning')

const loadSuggestions = async () => {
  if (!roomTypeId.value || !range.value?.length) {
    suggestions.value = []
    return
  }
  loading.value = true
  try {
    suggestions.value = await PricingApi.getSuggestionList({
      roomTypeId: roomTypeId.value,
      beginDate: range.value[0],
      endDate: range.value[1]
    })
  } finally {
    loading.value = false
  }
}

const handleGenerate = async () => {
  if (!roomTypeId.value || !range.value?.length) {
    message.warning('请先选择房型和日期区间')
    return
  }
  generating.value = true
  try {
    const count = await PricingApi.generateSuggestions({
      roomTypeId: roomTypeId.value,
      beginDate: range.value[0],
      endDate: range.value[1]
    })
    message.success(`已生成 ${count} 条建议`)
    await loadSuggestions()
  } finally {
    generating.value = false
  }
}

const handleAdopt = async (row: PricingApi.PriceSuggestionVO) => {
  try {
    const { value } = await message.prompt(
      `建议价 ¥${Number(row.suggestedPrice).toFixed(2)}，如要改价直接输入新价格（回车沿用建议价）`,
      '采纳定价建议'
    )
    await PricingApi.adoptSuggestion(row.id!, value ? Number(value) : undefined)
    message.success('已采纳并写回房价日历')
    await loadSuggestions()
  } catch {}
}

const handleIgnore = async (row: PricingApi.PriceSuggestionVO) => {
  try {
    await PricingApi.ignoreSuggestion(row.id!)
    message.success('已忽略')
    await loadSuggestions()
  } catch {}
}

const handleBacktest = async () => {
  if (!roomTypeId.value || !btRange.value?.length) {
    message.warning('请先选择房型和回测区间')
    return
  }
  btLoading.value = true
  try {
    backtest.value = await PricingApi.runBacktest({
      roomTypeId: roomTypeId.value,
      beginDate: btRange.value[0],
      endDate: btRange.value[1]
    })
    message.success('回测完成')
  } finally {
    btLoading.value = false
  }
}

const chartOption = computed<EChartsOption>(() => {
  const days = backtest.value?.days || []
  return {
    tooltip: { trigger: 'axis' },
    legend: { data: ['原价 RevPAR', '建议价 RevPAR'] },
    grid: { left: 50, right: 24, top: 40, bottom: 40 },
    xAxis: { type: 'category', data: days.map((d) => d.bizDate) },
    yAxis: { type: 'value', name: 'RevPAR（元）' },
    series: [
      {
        name: '原价 RevPAR',
        type: 'line',
        smooth: true,
        data: days.map((d) => Number(d.actualRevpar)),
        itemStyle: { color: '#909399' }
      },
      {
        name: '建议价 RevPAR',
        type: 'line',
        smooth: true,
        data: days.map((d) => Number(d.modelRevpar)),
        itemStyle: { color: '#f56c6c' }
      }
    ]
  }
})

onMounted(async () => {
  initRange()
  roomTypeList.value = (await RoomTypeApi.getRoomTypeSimpleList()) || []
  if (roomTypeList.value.length) {
    roomTypeId.value = roomTypeList.value[0].id
    await loadSuggestions()
  }
})
</script>
