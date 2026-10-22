<template>
  <div class="dash-wrap">
    <!-- 头部 -->
    <div class="dash-header">
      <div class="dash-title">
        <span class="dash-title-icon">◆</span> 酒店经营数据大屏
        <span class="dash-sub">HOTEL OPERATIONS DASHBOARD</span>
      </div>
      <div class="dash-header-right">
        <span class="biz-date">营业日 {{ overview?.bizDate || '--' }}</span>
        <el-button-group class="demo-btns">
          <el-button v-hasPermi="['hotel:demo:generate']" size="small" type="primary" plain :loading="demoLoading" @click="handleGenerate">
            生成演示数据
          </el-button>
          <el-button v-hasPermi="['hotel:demo:clean']" size="small" type="danger" plain :loading="demoLoading" @click="handleClean">
            清理演示数据
          </el-button>
        </el-button-group>
      </div>
    </div>

    <div class="dash-body" v-loading="loading">
      <!-- 左区：核心指标 + 趋势 -->
      <div class="dash-left">
        <div class="panel">
          <div class="panel-title">今日核心指标 <span class="panel-en">KEY METRICS</span></div>
          <div class="metric-grid">
            <div class="metric-card">
              <div class="metric-name">出租率 OCC</div>
              <div class="metric-value">
                {{ fmtNum(overview?.today?.occ) }}<span class="unit">%</span>
              </div>
              <div class="metric-change" :class="changeCls(overview?.today?.occChange)">
                {{ arrow(overview?.today?.occChange) }} {{ fmtSigned(overview?.today?.occChange) }} pp
              </div>
            </div>
            <div class="metric-card">
              <div class="metric-name">平均房价 ADR</div>
              <div class="metric-value">
                <span class="unit">¥</span>{{ fmtNum(overview?.today?.adr) }}
              </div>
              <div class="metric-change" :class="changeCls(overview?.today?.adrChange)">
                {{ arrow(overview?.today?.adrChange) }} {{ fmtSigned(overview?.today?.adrChange) }}%
              </div>
            </div>
            <div class="metric-card">
              <div class="metric-name">RevPAR</div>
              <div class="metric-value">
                <span class="unit">¥</span>{{ fmtNum(overview?.today?.revpar) }}
              </div>
              <div class="metric-change" :class="changeCls(overview?.today?.revparChange)">
                {{ arrow(overview?.today?.revparChange) }} {{ fmtSigned(overview?.today?.revparChange) }}%
              </div>
            </div>
            <div class="metric-card">
              <div class="metric-name">今日营收</div>
              <div class="metric-value">
                <span class="unit">¥</span>{{ fmtNum(overview?.today?.totalRevenue) }}
              </div>
              <div class="metric-change" :class="changeCls(overview?.today?.revenueChange)">
                {{ arrow(overview?.today?.revenueChange) }} {{ fmtSigned(overview?.today?.revenueChange) }}%
              </div>
            </div>
          </div>
          <div class="metric-note">
            房费 {{ fmtMoney(overview?.today?.roomRevenue) }} · 其他 {{ fmtMoney(overview?.today?.otherRevenue) }}
            <span class="dim">（环比 = 较昨日）</span>
          </div>
        </div>

        <div class="panel trend-panel">
          <div class="panel-title">近 30 天经营趋势 <span class="panel-en">30-DAY TREND</span></div>
          <Echart :height="300" :options="trendOption" />
        </div>
      </div>

      <!-- 右区：房态 + 预抵预离 + 待办 -->
      <div class="dash-right">
        <div class="panel">
          <div class="panel-title">
            实时房态 <span class="panel-en">LIVE ROOM STATUS</span>
            <span class="room-legend">
              <i class="dot dot-avail"></i>空闲 {{ overview?.roomStatus?.available }}
              <i class="dot dot-occ"></i>在住 {{ overview?.roomStatus?.occupied }}
              <i class="dot dot-clean"></i>待清洁 {{ overview?.roomStatus?.cleaning }}
              <i class="dot dot-maint"></i>维修 {{ overview?.roomStatus?.maintenance }}
            </span>
          </div>
          <div class="room-grid">
            <div v-for="floor in overview?.roomGrid" :key="floor.floor" class="floor-block">
              <div class="floor-label">{{ floor.floor }}F</div>
              <div class="floor-rooms">
                <div
                  v-for="room in floor.rooms"
                  :key="room.roomId"
                  class="room-cell"
                  :class="'st-' + room.status"
                  :title="room.roomNo + ' ' + room.roomTypeName + ' · ' + room.statusName"
                >
                  <div class="room-no">{{ room.roomNo }}</div>
                  <div class="room-type">{{ room.roomTypeName }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="dash-right-bottom">
          <div class="panel">
            <div class="panel-title">今日预抵 <span class="badge">{{ overview?.todayArrivals?.length }}</span></div>
            <div class="guest-list">
              <div v-if="!overview?.todayArrivals?.length" class="empty">暂无预抵订单</div>
              <div v-for="item in overview?.todayArrivals" :key="item.orderId" class="guest-row">
                <span class="g-name">{{ item.guestName }}</span>
                <span class="g-room">{{ item.roomNo }} {{ item.roomTypeName }}</span>
                <span class="g-tag" :class="item.status === 30 ? 'tag-in' : 'tag-arrive'">
                  {{ item.status === 30 ? '已到店' : '预抵' }}
                </span>
              </div>
            </div>
          </div>
          <div class="panel">
            <div class="panel-title">今日预离 <span class="badge">{{ overview?.todayDepartures?.length }}</span></div>
            <div class="guest-list">
              <div v-if="!overview?.todayDepartures?.length" class="empty">暂无预离订单</div>
              <div v-for="item in overview?.todayDepartures" :key="item.orderId" class="guest-row">
                <span class="g-name">{{ item.guestName }}</span>
                <span class="g-room">{{ item.roomNo }} {{ item.roomTypeName }}</span>
                <span class="g-tag" :class="item.status === 40 ? 'tag-out' : 'tag-leave'">
                  {{ item.status === 40 ? '已离店' : '待离店' }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <div class="panel todo-panel">
          <div class="panel-title">待办提醒 <span class="panel-en">TO-DO</span></div>
          <div class="todo-row" v-if="overview?.todo">
            <div class="todo-item">
              <div class="todo-num" :class="{ warn: overview.todo.pendingConfirm > 0 }">{{ overview.todo.pendingConfirm }}</div>
              <div class="todo-name">待确认订单</div>
            </div>
            <div class="todo-item">
              <div class="todo-num" :class="{ warn: overview.todo.dueCheckOuts > 0 }">{{ overview.todo.dueCheckOuts }}</div>
              <div class="todo-name">今日应退房</div>
            </div>
            <div class="todo-item">
              <div class="todo-num" :class="{ warn: overview.todo.openCleaning > 0 }">{{ overview.todo.openCleaning }}</div>
              <div class="todo-name">待清洁房间</div>
            </div>
            <div class="todo-item">
              <div class="todo-num" :class="{ warn: overview.todo.unpaidOrders > 0 }">{{ overview.todo.unpaidOrders }}</div>
              <div class="todo-name">未结账订单</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { EChartsOption } from 'echarts'
import { getDashboardOverviewApi } from '@/api/hotel/dashboard'
import { cleanDemoDataApi, generateDemoDataApi } from '@/api/hotel/demo'

defineOptions({ name: 'HotelDashboard' })

const loading = ref(false)
const demoLoading = ref(false)
const overview = ref<any>(null)

const loadData = async () => {
  loading.value = true
  try {
    overview.value = await getDashboardOverviewApi()
  } finally {
    loading.value = false
  }
}

// ==================== 指标格式化 ====================
const fmtNum = (v: any) => (v === null || v === undefined || v === '' ? '--' : Number(v).toLocaleString('zh-CN', { maximumFractionDigits: 2 }))
const fmtMoney = (v: any) => (v === null || v === undefined ? '¥--' : '¥' + Number(v).toLocaleString('zh-CN', { maximumFractionDigits: 2 }))
const fmtSigned = (v: any) => (v === null || v === undefined ? '--' : Number(v) > 0 ? '+' + Number(v).toFixed(2) : Number(v).toFixed(2))
const arrow = (v: any) => (v === null || v === undefined || Number(v) === 0 ? '' : Number(v) > 0 ? '▲' : '▼')
const changeCls = (v: any) => (v === null || v === undefined || Number(v) === 0 ? '' : Number(v) > 0 ? 'up' : 'down')

// ==================== 趋势图 ====================
const trendOption = computed<EChartsOption>(() => {
  const trend = overview.value?.trend || []
  return {
    backgroundColor: 'transparent',
    tooltip: { trigger: 'axis', backgroundColor: '#10264d', borderColor: '#1d3a68', textStyle: { color: '#dbeafe' } },
    legend: { data: ['营收(元)', '出租率(%)'], textStyle: { color: '#93b4e0' }, top: 0 },
    grid: { left: 55, right: 55, top: 34, bottom: 28 },
    xAxis: {
      type: 'category',
      data: trend.map((t: any) => (t.date || '').slice(5)),
      axisLine: { lineStyle: { color: '#1d3a68' } },
      axisLabel: { color: '#7d9bc7', fontSize: 11 }
    },
    yAxis: [
      {
        type: 'value',
        name: '营收',
        nameTextStyle: { color: '#7d9bc7' },
        splitLine: { lineStyle: { color: 'rgba(29,58,104,.35)' } },
        axisLabel: { color: '#7d9bc7', fontSize: 11 }
      },
      {
        type: 'value',
        name: '出租率%',
        max: 100,
        nameTextStyle: { color: '#7d9bc7' },
        splitLine: { show: false },
        axisLabel: { color: '#7d9bc7', fontSize: 11 }
      }
    ],
    series: [
      {
        name: '营收(元)',
        type: 'bar',
        data: trend.map((t: any) => t.revenue),
        itemStyle: { color: '#22d3ee', borderRadius: [3, 3, 0, 0] },
        barMaxWidth: 14
      },
      {
        name: '出租率(%)',
        type: 'line',
        yAxisIndex: 1,
        data: trend.map((t: any) => t.occ),
        smooth: true,
        symbol: 'circle',
        symbolSize: 5,
        lineStyle: { color: '#fbbf24', width: 2 },
        itemStyle: { color: '#fbbf24' }
      }
    ]
  }
})

// ==================== 演示数据 ====================
const handleGenerate = async () => {
  try {
    const { value } = await ElMessageBox.prompt('请输入演示数据月数（1-12，默认 6）', '生成演示数据', {
      inputValue: '6',
      confirmButtonText: '生成',
      cancelButtonText: '取消',
      inputValidator: (v: string) => {
        const n = Number(v)
        return Number.isInteger(n) && n >= 1 && n <= 12 ? true : '请输入 1-12 之间的整数'
      }
    })
    demoLoading.value = true
    const res: any = await generateDemoDataApi(Number(value || 6))
    ElMessage.success(`演示数据生成完成：${res?.orderCount || 0} 笔订单，${res?.guestCount || 0} 位住客`)
    await loadData()
  } catch (e: any) {
    if (e !== 'cancel' && e?.message) {
      ElMessage.warning(typeof e.message === 'string' ? e.message : '生成失败')
    }
  } finally {
    demoLoading.value = false
  }
}

const handleClean = async () => {
  try {
    await ElMessageBox.confirm('将清空全部演示数据（不触碰验收数据），确认继续？', '清理演示数据', {
      type: 'warning',
      confirmButtonText: '确认清理',
      cancelButtonText: '取消'
    })
    demoLoading.value = true
    await cleanDemoDataApi()
    ElMessage.success('演示数据已清理')
    await loadData()
  } catch (e: any) {
    if (e !== 'cancel' && e?.message) {
      ElMessage.warning(typeof e.message === 'string' ? e.message : '清理失败')
    }
  } finally {
    demoLoading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.dash-wrap {
  height: calc(100vh - 106px);
  min-height: 640px;
  padding: 12px 14px;
  background: radial-gradient(ellipse at top, #12224a 0%, #0a1428 55%, #070d1d 100%);
  border-radius: 8px;
  color: #dbeafe;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.dash-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 8px 14px;
  .dash-title {
    font-size: 22px;
    font-weight: 700;
    letter-spacing: 2px;
    background: linear-gradient(90deg, #67e8f9, #93c5fd, #fbbf24);
    -webkit-background-clip: text;
    background-clip: text;
    color: transparent;
    .dash-title-icon { color: #22d3ee; }
    .dash-sub {
      margin-left: 10px;
      font-size: 12px;
      letter-spacing: 3px;
      color: #4d6a99;
      font-weight: 400;
    }
  }
  .dash-header-right {
    display: flex;
    align-items: center;
    gap: 12px;
    .biz-date {
      font-size: 14px;
      color: #93c5fd;
      border: 1px solid #1d3a68;
      background: rgba(16, 32, 64, 0.6);
      padding: 4px 12px;
      border-radius: 20px;
    }
  }
}

.dash-body {
  flex: 1;
  display: grid;
  grid-template-columns: 56% 44%;
  gap: 12px;
  min-height: 0;
}

.dash-left {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 0;
  .trend-panel { flex: 1; }
}

.dash-right {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 0;
}

.dash-right-bottom {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  flex: 1;
  min-height: 0;
}

.panel {
  background: linear-gradient(180deg, rgba(20, 40, 80, 0.55), rgba(12, 26, 54, 0.55));
  border: 1px solid #1d3a68;
  border-radius: 8px;
  padding: 10px 12px;
  box-shadow: 0 0 18px rgba(34, 211, 238, 0.06) inset;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #bcd7ff;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
  .panel-en {
    font-size: 10px;
    letter-spacing: 2px;
    color: #4d6a99;
    font-weight: 400;
  }
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}

.metric-card {
  background: rgba(13, 30, 62, 0.7);
  border: 1px solid rgba(34, 211, 238, 0.25);
  border-radius: 6px;
  padding: 10px 12px;
  .metric-name { font-size: 12px; color: #7d9bc7; }
  .metric-value {
    font-size: 26px;
    font-weight: 700;
    margin: 4px 0 2px;
    color: #67e8f9;
    font-family: 'DIN', 'Roboto', sans-serif;
    .unit { font-size: 13px; color: #93b4e0; margin-right: 2px; }
  }
  .metric-change { font-size: 12px; color: #64748b; }
  .metric-change.up { color: #f87171; }
  .metric-change.down { color: #34d399; }
}

.metric-note {
  margin-top: 8px;
  font-size: 12px;
  color: #7d9bc7;
  .dim { color: #4d6a99; }
}

.room-legend {
  margin-left: auto;
  font-size: 11px;
  color: #93b4e0;
  .dot {
    display: inline-block;
    width: 8px;
    height: 8px;
    border-radius: 2px;
    margin: 0 4px 0 10px;
  }
  .dot-avail { background: #34d399; }
  .dot-occ { background: #22d3ee; }
  .dot-clean { background: #fbbf24; }
  .dot-maint { background: #f87171; }
}

.room-grid {
  flex: 1;
  overflow: auto;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.floor-block {
  display: flex;
  align-items: center;
  gap: 8px;
  .floor-label {
    width: 26px;
    text-align: center;
    font-size: 14px;
    font-weight: 700;
    color: #4d6a99;
  }
  .floor-rooms { display: flex; gap: 6px; flex-wrap: wrap; }
}

.room-cell {
  width: 64px;
  border-radius: 4px;
  padding: 4px 6px;
  text-align: center;
  border: 1px solid transparent;
  cursor: default;
  .room-no { font-size: 14px; font-weight: 700; }
  .room-type { font-size: 10px; opacity: 0.85; }
}
.room-cell.st-10 { background: rgba(52, 211, 153, 0.14); border-color: rgba(52, 211, 153, 0.45); }
.room-cell.st-20 { background: rgba(34, 211, 238, 0.16); border-color: rgba(34, 211, 238, 0.5); }
.room-cell.st-30 { background: rgba(251, 191, 36, 0.14); border-color: rgba(251, 191, 36, 0.45); }
.room-cell.st-40 { background: rgba(248, 113, 113, 0.16); border-color: rgba(248, 113, 113, 0.5); }

.guest-list {
  flex: 1;
  overflow: auto;
  .empty { color: #4d6a99; font-size: 12px; padding: 6px 0; }
}
.guest-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
  border-bottom: 1px dashed rgba(29, 58, 104, 0.5);
  font-size: 12px;
  .g-name { color: #dbeafe; width: 62px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  .g-room { color: #93b4e0; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  .g-tag { font-size: 11px; padding: 1px 6px; border-radius: 8px; }
  .tag-arrive { color: #fbbf24; background: rgba(251, 191, 36, 0.12); }
  .tag-in { color: #34d399; background: rgba(52, 211, 153, 0.12); }
  .tag-leave { color: #22d3ee; background: rgba(34, 211, 238, 0.12); }
  .tag-out { color: #94a3b8; background: rgba(148, 163, 184, 0.12); }
}

.badge {
  background: rgba(34, 211, 238, 0.2);
  color: #67e8f9;
  border-radius: 10px;
  padding: 0 7px;
  font-size: 12px;
}

.todo-panel { flex: none; }
.todo-row { display: flex; justify-content: space-around; }
.todo-item { text-align: center; }
.todo-num {
  font-size: 22px;
  font-weight: 700;
  color: #67e8f9;
  &.warn { color: #fbbf24; }
}
.todo-name { font-size: 11px; color: #7d9bc7; }
</style>
