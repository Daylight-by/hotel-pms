<template>
  <view class="page">
    <scroll-view class="tabs" scroll-x :show-scrollbar="false">
      <view class="tabs__inner">
        <view
          v-for="tab in tabs"
          :key="tab.value"
          class="tab"
          :class="{ 'tab--active': active === tab.value }"
          @tap="switchTab(tab.value)"
        >
          <text>{{ tab.label }}</text>
        </view>
      </view>
    </scroll-view>

    <view class="list">
      <view v-if="loading" class="skeleton-wrap">
        <view v-for="i in 3" :key="i" class="skeleton">
          <view class="skeleton__line skeleton__line--sm"></view>
          <view class="skeleton__line skeleton__line--lg"></view>
          <view class="skeleton__line"></view>
        </view>
      </view>

      <template v-else>
        <view v-for="order in orders" :key="order.id" class="order" @tap="goDetail(order)">
          <view class="order__head">
            <text class="order__no">{{ order.orderNo || ('订单 #' + order.id) }}</text>
            <text class="order__status" :class="'order__status--' + tone(order.status)">
              {{ statusText(order.status) }}
            </text>
          </view>

          <view class="order__body">
            <view class="order__cover">
              <h-cover
                :variant="variantOf(order)"
                mark="栖"
                mark-size="100rpx"
                height="160rpx"
                radius="16rpx"
              />
            </view>
            <view class="order__info">
              <text class="order__name">{{ order.roomTypeName }}</text>
              <text class="order__date">{{ order.checkInDate }} → {{ order.checkOutDate }}</text>
              <text class="order__meta">
                {{ order.nights || nightsOf(order) }} 晚 · {{ order.roomCount || 1 }} 间 ·
                {{ order.breakfast || '不含早' }}
              </text>
            </view>
          </view>

          <view class="order__foot">
            <view class="order__amount">
              <text class="order__amount-label">应付</text>
              <text class="order__amount-value">¥{{ money(order.payableAmount || order.totalAmount) }}</text>
            </view>
            <view class="order__actions" @tap.stop>
              <view
                v-if="order.status === 10"
                class="action action--ghost"
                @tap.stop="cancel(order)"
              >
                取消
              </view>
              <view v-if="order.status === 10" class="action action--primary" @tap.stop="pay(order)">
                去支付
              </view>
              <view
                v-if="order.status === 20"
                class="action action--primary"
                @tap.stop="selfCheckIn(order)"
              >
                自助入住
              </view>
              <view v-if="order.status === 30" class="action action--primary" @tap.stop="selfCheckOut(order)">
                自助退房
              </view>
              <view
                v-if="order.status === 20 || order.status === 30 || order.status === 40"
                class="action action--ghost"
                @tap.stop="invoice(order)"
              >
                {{ order.invoiceStatus === 20 ? '开票中' : '申请发票' }}
              </view>
              <view v-if="order.status === 50 || order.status === 40" class="action action--ghost" @tap.stop="rebook">
                再次预订
              </view>
            </view>
          </view>
        </view>

        <h-empty
          v-if="!orders.length"
          :title="emptyTitle"
          desc="换个筛选条件看看，或回到首页选择房型"
        >
          <view class="empty-btn" @tap="goHome">去预订</view>
        </h-empty>
      </template>
    </view>

    <view class="list-foot" v-if="orders.length">
      <text>共 {{ total }} 笔订单</text>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app'
import { cancelOrder, fetchMyOrders } from '@/api/booking'
import { selfCheckIn as apiSelfCheckIn, selfCheckOut as apiSelfCheckOut } from '@/api/member'
import { money, nightsBetween, statusText } from '@/utils/format'
import { loadSession, session } from '@/store/session'

const tabs = [
  { label: '全部', value: 0 },
  { label: '待支付', value: 10 },
  { label: '已确认', value: 20 },
  { label: '已入住', value: 30 },
  { label: '已退房', value: 40 },
  { label: '已取消', value: 50 }
]

const active = ref(0)
const orders = ref([])
const total = ref(0)
const loading = ref(true)
const emptyTitle = computed(() => (active.value ? '这个状态下没有订单' : '还没有订单'))

onShow(() => {
  loadSession()
  if (!session.token) {
    uni.redirectTo({
      url: '/pages/login/index?redirect=' + encodeURIComponent('/pages/order/list')
    })
    return
  }
  load()
})

onPullDownRefresh(async () => {
  await load()
  uni.stopPullDownRefresh()
})

async function load() {
  loading.value = true
  try {
    const res = await fetchMyOrders({ pageNo: 1, pageSize: 30, status: active.value })
    orders.value = res.list || []
    total.value = res.total || orders.value.length
  } catch (e) {
    orders.value = []
    uni.showToast({ title: e.message || '订单加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function switchTab(value) {
  if (active.value === value) return
  active.value = value
  load()
}

function tone(status) {
  if (status === 10) return 'gold'
  if (status === 20) return 'green'
  if (status === 30) return 'blue'
  return 'muted'
}

function nightsOf(order) {
  return nightsBetween(order.checkInDate, order.checkOutDate)
}

function variantOf(order) {
  return ((Number(order.roomTypeId) || 1) % 4) + 1
}

function goDetail(order) {
  uni.navigateTo({ url: '/pages/order/detail?id=' + order.id })
}

function pay(order) {
  uni.navigateTo({
    url: `/pages/order/pay?orderId=${order.id}&orderNo=${order.orderNo || ''}&amount=${
      order.payableAmount || 0
    }`
  })
}

function cancel(order) {
  uni.showModal({
    title: '取消订单',
    content: '取消后房间将立即释放，确定取消吗？',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await cancelOrder(order.id, '客人主动取消')
        uni.showToast({ title: '已取消', icon: 'none' })
        load()
      } catch (e) {
        uni.showToast({ title: e.message || '取消失败', icon: 'none' })
      }
    }
  })
}

function selfCheckIn(order) {
  uni.showModal({
    title: '自助入住',
    content: '确认办理入住？办理后房间将分配给您，可直接前往客房。',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await apiSelfCheckIn(order.id)
        uni.showToast({ title: '入住成功，房间已分配', icon: 'none' })
        load()
      } catch (e) {
        uni.showToast({ title: e.message || '办理失败', icon: 'none' })
      }
    }
  })
}

function selfCheckOut(order) {
  uni.showModal({
    title: '自助退房',
    content: '确认退房？退房后将通知客房清洁。',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await apiSelfCheckOut(order.id)
        uni.showToast({ title: '退房成功，感谢入住', icon: 'none' })
        load()
      } catch (e) {
        uni.showToast({ title: e.message || '办理失败', icon: 'none' })
      }
    }
  })
}

function invoice(order) {
  uni.navigateTo({ url: '/pages/order/detail?id=' + order.id + '&invoice=1' })
}

function rebook() {
  uni.reLaunch({ url: '/pages/index/index' })
}

function goHome() {
  uni.reLaunch({ url: '/pages/index/index' })
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: $c-bg;
  padding-bottom: 40rpx;
}
.tabs {
  background: $c-bg;
  padding: 12rpx 0 4rpx;
  white-space: nowrap;
}
.tabs__inner {
  display: inline-flex;
  padding: 0 24rpx;
}
.tab {
  padding: 14rpx 26rpx;
  margin: 0 6rpx;
  font-size: 24rpx;
  color: $c-muted;
  border-radius: 100rpx;
}
.tab--active {
  background: $c-ink;
  color: #f7f1e6;
}
.list {
  padding: 20rpx 32rpx 0;
}
.order {
  background: $c-card;
  border-radius: $radius-lg;
  padding: 24rpx 26rpx 20rpx;
  margin-bottom: 26rpx;
  box-shadow: $shadow-card;
}
.order__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}
.order__no {
  font-size: 21rpx;
  color: $c-muted;
  letter-spacing: 1rpx;
}
.order__status {
  font-size: 22rpx;
  letter-spacing: 1rpx;
}
.order__status--gold {
  color: $c-gold-deep;
}
.order__status--green {
  color: $c-green;
}
.order__status--blue {
  color: #3b5f80;
}
.order__status--muted {
  color: $c-muted;
}
.order__body {
  display: flex;
}
.order__cover {
  width: 170rpx;
  margin-right: 20rpx;
}
.order__info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.order__name {
  font-family: 'Songti SC', 'STSong', serif;
  font-size: 30rpx;
  color: $c-ink;
}
.order__date {
  margin-top: 8rpx;
  font-size: 22rpx;
  color: $c-ink-2;
}
.order__meta {
  margin-top: 6rpx;
  font-size: 20rpx;
  color: $c-muted;
}
.order__foot {
  margin-top: 20rpx;
  padding-top: 18rpx;
  border-top: 1rpx solid $c-line;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.order__amount {
  display: flex;
  align-items: baseline;
}
.order__amount-label {
  font-size: 20rpx;
  color: $c-muted;
  margin-right: 8rpx;
}
.order__amount-value {
  font-family: 'Songti SC', 'STSong', serif;
  font-size: 32rpx;
  color: $c-ink;
}
.order__actions {
  display: flex;
  align-items: center;
}
.action {
  height: 58rpx;
  padding: 0 24rpx;
  border-radius: 100rpx;
  font-size: 22rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 14rpx;
}
.action--ghost {
  border: 1rpx solid $c-line;
  color: $c-ink-2;
  background: transparent;
}
.action--primary {
  background: $c-ink;
  color: #f7f1e6;
}

.skeleton-wrap {
  padding-top: 6rpx;
}
.skeleton {
  background: $c-card;
  border-radius: $radius-lg;
  padding: 26rpx;
  margin-bottom: 26rpx;
}
.skeleton__line {
  height: 22rpx;
  width: 50%;
  background: #efe8dc;
  border-radius: 8rpx;
  margin-bottom: 16rpx;
}
.skeleton__line--sm {
  width: 32%;
  height: 18rpx;
}
.skeleton__line--lg {
  width: 68%;
  height: 30rpx;
}
.empty-btn {
  margin-top: 30rpx;
  padding: 14rpx 40rpx;
  border-radius: 100rpx;
  background: $c-ink;
  color: #f7f1e6;
  font-size: 23rpx;
  letter-spacing: 2rpx;
}
.list-foot {
  text-align: center;
  font-size: 19rpx;
  color: #b3a894;
  padding: 14rpx 0 30rpx;
}
</style>
