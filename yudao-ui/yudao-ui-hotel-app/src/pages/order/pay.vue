<template>
  <view class="page">
    <template v-if="phase !== 'success'">
      <view class="countdown">
        <text class="countdown__label">支付剩余时间</text>
        <text class="countdown__time">{{ countdownText }}</text>
      </view>

      <view class="amount">
        <text class="amount__label">应付金额</text>
        <view class="amount__value">
          <text class="amount__cur">¥</text>
          <text class="amount__num">{{ money(amount) }}</text>
        </view>
        <text class="amount__hint">栖屿酒店 · 在线预付</text>
      </view>

      <view class="card">
        <view class="order-line">
          <text class="order-line__label">订单号</text>
          <text class="order-line__value">{{ orderNo || '—' }}</text>
        </view>
        <view class="order-line">
          <text class="order-line__label">房型</text>
          <text class="order-line__value">{{ order.roomTypeName || '栖屿客房' }}</text>
        </view>
        <view class="order-line">
          <text class="order-line__label">入住</text>
          <text class="order-line__value">{{ order.checkInDate }} → {{ order.checkOutDate }}</text>
        </view>
        <view class="order-line">
          <text class="order-line__label">入住人</text>
          <text class="order-line__value">{{ order.guestName }} {{ maskMobile(order.guestMobile) }}</text>
        </view>
      </view>

      <view class="card">
        <view class="card__head">
          <text class="card__title">选择支付方式</text>
          <text v-if="mockMode" class="card__mock">演示环境</text>
        </view>

        <view
          v-for="m in methods"
          :key="m.key"
          class="method"
          :class="{ 'method--active': method === m.key }"
          @tap="selectMethod(m.key)"
        >
          <view class="method__icon" :style="{ background: m.color }">
            <text class="method__icon-text">{{ m.icon }}</text>
          </view>
          <view class="method__body">
            <text class="method__name">{{ m.name }}</text>
            <text class="method__desc">{{ m.desc }}</text>
          </view>
          <view class="method__radio">
            <view v-if="method === m.key" class="method__dot"></view>
          </view>
        </view>
      </view>

      <view class="tips">
        <text class="tips__line">· 演示环境不会产生真实扣款</text>
        <text class="tips__line">· 支付成功后订单状态变为「已确认」，可在我的订单查看</text>
        <view class="cancel-link" @tap="cancel">
          <text>不想要了，取消订单</text>
        </view>
      </view>

      <view class="bottom-space"></view>

      <view class="bar" :class="{ 'bar--mock': mockMode }">
        <view v-if="mockMode" class="mock-entry" @tap="mockSuccess">
          <text class="mock-entry__main">模拟支付成功（答辩演示按钮）</text>
          <text class="mock-entry__sub">POST /app-api/hotel/pay/mock-success</text>
        </view>
        <view class="bar__row">
          <view class="bar__info">
            <text class="bar__label">待支付</text>
            <text class="bar__amount">¥{{ money(amount) }}</text>
          </view>
          <view class="bar__btn" :class="{ 'bar__btn--loading': paying }" @tap="pay">
            {{ paying ? '处理中…' : '立即支付' }}
          </view>
        </view>
      </view>
    </template>

    <view v-else class="success">
      <view class="success__ring">
        <view class="success__check"></view>
      </view>
      <text class="success__title">支付成功</text>
      <text class="success__sub">房间已为您保留，期待与您相见</text>
      <view class="success__amount">
        <text class="success__cur">¥</text>
        <text class="success__num">{{ money(amount) }}</text>
      </view>
      <view class="success__card">
        <view class="order-line">
          <text class="order-line__label">订单号</text>
          <text class="order-line__value">{{ orderNo }}</text>
        </view>
        <view class="order-line">
          <text class="order-line__label">房型</text>
          <text class="order-line__value">{{ order.roomTypeName || '栖屿客房' }}</text>
        </view>
        <view class="order-line">
          <text class="order-line__label">入住</text>
          <text class="order-line__value">{{ order.checkInDate }} → {{ order.checkOutDate }}</text>
        </view>
      </view>
      <view class="success__actions">
        <view class="success__btn success__btn--primary" @tap="goDetail">查看订单</view>
        <view class="success__btn" @tap="goHome">返回首页</view>
      </view>
      <view class="success__foot">
        <text class="success__foot-line">入住当天可在订单详情自助办理入住</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed, onUnmounted, ref } from 'vue'
import { onLoad, onUnload } from '@dcloudio/uni-app'
import { cancelOrder, createPayOrder, fetchOrderDetail, fetchPayResult, mockPaySuccess } from '@/api/booking'
import { isMockMode } from '@/utils/request'
import { money } from '@/utils/format'
import { loadSession } from '@/store/session'

const methods = [
  { key: 'wxpay', name: '微信支付', desc: '推荐使用，秒级到账', icon: '微', color: '#2f9c5f' },
  { key: 'alipay', name: '支付宝', desc: '支持花呗与余额', icon: '支', color: '#2a7fd4' }
]

const orderId = ref('')
const orderNo = ref('')
const amount = ref(0)
const order = ref({})
const method = ref('wxpay')
const phase = ref('pay')
const payInfo = ref(null)
const paying = ref(false)
const mockMode = ref(isMockMode())
const remain = ref(15 * 60)
let timer = null
let pollTimer = null

const countdownText = computed(() => {
  const s = Math.max(0, remain.value)
  const m = Math.floor(s / 60)
  const sec = s % 60
  return `${String(m).padStart(2, '0')}:${String(sec).padStart(2, '0')}`
})

onLoad(async (options) => {
  loadSession()
  orderId.value = options.orderId || ''
  orderNo.value = options.orderNo || ''
  amount.value = Number(options.amount || 0)
  try {
    const detail = await fetchOrderDetail(orderId.value)
    if (detail) {
      order.value = detail
      if (!orderNo.value) orderNo.value = detail.orderNo
      if (!amount.value) amount.value = detail.payableAmount
    }
  } catch (e) {}
  await preparePay()

  timer = setInterval(() => {
    remain.value -= 1
    if (remain.value <= 0) {
      clearInterval(timer)
      phase.value = 'expired'
      uni.showModal({
        title: '支付超时',
        content: '房间保留时间已结束，请返回重新预订。',
        showCancel: false,
        success: () => goHome()
      })
    }
  }, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (pollTimer) clearInterval(pollTimer)
})

onUnload(() => {
  if (timer) clearInterval(timer)
  if (pollTimer) clearInterval(pollTimer)
})

async function preparePay() {
  try {
    const res = await createPayOrder({ orderId: orderId.value, payMethod: method.value })
    payInfo.value = res
    mockMode.value = isMockMode() || !!res.mock
  } catch (e) {
    uni.showToast({ title: e.message || '支付单创建失败', icon: 'none' })
  }
}

function selectMethod(key) {
  if (method.value === key) return
  method.value = key
  preparePay()
}

function maskMobile(mobile) {
  if (!mobile) return ''
  return String(mobile).replace(/^(\d{3})\d{4}(\d{4})$/, '$1****$2')
}

async function pay() {
  if (paying.value) return
  if (!payInfo.value) {
    await preparePay()
    if (!payInfo.value) return
  }
  if (mockMode.value) {
    await mockSuccess()
    return
  }
  paying.value = true
  uni.showToast({ title: '请在弹出的收银台完成支付', icon: 'none' })
  // 真实环境下由 payParams 拉起微信/支付宝收银台；此处轮询支付结果
  pollResult()
}

async function mockSuccess() {
  if (paying.value) return
  if (!payInfo.value || !payInfo.value.payOrderNo) {
    uni.showToast({ title: '支付单准备中，请稍候', icon: 'none' })
    await preparePay()
    if (!payInfo.value) return
  }
  paying.value = true
  try {
    await mockPaySuccess(payInfo.value.payOrderNo)
    await new Promise((resolve) => setTimeout(resolve, 500))
    if (timer) clearInterval(timer)
    phase.value = 'success'
    uni.showToast({ title: '支付成功', icon: 'success' })
  } catch (e) {
    uni.showToast({ title: e.message || '模拟支付失败', icon: 'none' })
  } finally {
    paying.value = false
  }
}

function pollResult() {
  if (pollTimer) clearInterval(pollTimer)
  let tries = 0
  pollTimer = setInterval(async () => {
    tries += 1
    try {
      const res = await fetchPayResult(payInfo.value.payOrderNo)
      if (res && Number(res.status) === 20) {
        clearInterval(pollTimer)
        if (timer) clearInterval(timer)
        paying.value = false
        phase.value = 'success'
        return
      }
    } catch (e) {}
    if (tries >= 20) {
      clearInterval(pollTimer)
      paying.value = false
      uni.showToast({ title: '暂未查询到支付结果，请稍后刷新', icon: 'none' })
    }
  }, 2000)
}

function cancel() {
  uni.showModal({
    title: '取消订单',
    content: '确定取消这笔未支付的订单吗？',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await cancelOrder(orderId.value, '客人主动取消')
        uni.showToast({ title: '订单已取消', icon: 'none' })
        setTimeout(() => uni.redirectTo({ url: '/pages/order/list' }), 600)
      } catch (e) {
        uni.showToast({ title: e.message || '取消失败', icon: 'none' })
      }
    }
  })
}

function goDetail() {
  uni.redirectTo({ url: '/pages/order/detail?id=' + orderId.value })
}

function goHome() {
  uni.reLaunch({ url: '/pages/index/index' })
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: $c-bg;
  padding: 0 32rpx 40rpx;
}
.countdown {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 22rpx 0 0;
}
.countdown__label {
  font-size: 21rpx;
  color: $c-muted;
  letter-spacing: 2rpx;
}
.countdown__time {
  margin-left: 12rpx;
  font-size: 24rpx;
  color: $c-gold-deep;
  letter-spacing: 2rpx;
}
.amount {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 46rpx 0 40rpx;
}
.amount__label {
  font-size: 22rpx;
  color: $c-muted;
  letter-spacing: 3rpx;
}
.amount__value {
  display: flex;
  align-items: baseline;
  margin-top: 12rpx;
}
.amount__cur {
  font-size: 36rpx;
  color: $c-ink;
  margin-right: 6rpx;
}
.amount__num {
  font-family: 'Songti SC', 'STSong', serif;
  font-size: 96rpx;
  line-height: 1;
  color: $c-ink;
}
.amount__hint {
  margin-top: 14rpx;
  font-size: 20rpx;
  color: $c-muted;
  letter-spacing: 2rpx;
}

.card {
  background: $c-card;
  border-radius: $radius-lg;
  padding: 12rpx 28rpx;
  box-shadow: $shadow-card;
  margin-bottom: 24rpx;
}
.card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22rpx 0 12rpx;
}
.card__title {
  font-family: 'Songti SC', 'STSong', serif;
  font-size: 30rpx;
  color: $c-ink;
}
.card__mock {
  font-size: 19rpx;
  color: $c-gold-deep;
  background: rgba(176, 134, 90, 0.12);
  border-radius: 100rpx;
  padding: 4rpx 14rpx;
}
.order-line {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 16rpx 0;
  border-bottom: 1rpx solid $c-line;
}
.order-line:last-child {
  border-bottom: none;
}
.order-line__label {
  font-size: 22rpx;
  color: $c-muted;
  flex-shrink: 0;
  margin-right: 24rpx;
}
.order-line__value {
  font-size: 23rpx;
  color: $c-ink-2;
  text-align: right;
}

.method {
  display: flex;
  align-items: center;
  padding: 22rpx 0;
  border-top: 1rpx solid $c-line;
}
.method__icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
}
.method__icon-text {
  color: #fff;
  font-size: 30rpx;
  font-weight: 600;
}
.method__body {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.method__name {
  font-size: 26rpx;
  color: $c-ink;
}
.method__desc {
  margin-top: 2rpx;
  font-size: 20rpx;
  color: $c-muted;
}
.method__radio {
  width: 34rpx;
  height: 34rpx;
  border-radius: 50%;
  border: 1rpx solid #cfc2ad;
  display: flex;
  align-items: center;
  justify-content: center;
}
.method--active .method__radio {
  border-color: $c-gold-deep;
}
.method__dot {
  width: 18rpx;
  height: 18rpx;
  border-radius: 50%;
  background: $c-gold-deep;
}

.tips {
  padding: 6rpx 10rpx 0;
  display: flex;
  flex-direction: column;
}
.tips__line {
  font-size: 20rpx;
  color: $c-muted;
  line-height: 1.9;
}
.bottom-space {
  height: 280rpx;
}

.bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 18rpx 32rpx calc(18rpx + env(safe-area-inset-bottom));
  background: rgba(255, 253, 249, 0.98);
  border-top: 1rpx solid $c-line;
  z-index: 20;
}
.bar__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.bar--mock {
  padding-top: 14rpx;
}
.bar__info {
  display: flex;
  flex-direction: column;
}
.bar__label {
  font-size: 19rpx;
  color: $c-muted;
}
.bar__amount {
  font-family: 'Songti SC', 'STSong', serif;
  font-size: 38rpx;
  color: $c-ink;
}
.bar__btn {
  width: 330rpx;
  height: 88rpx;
  border-radius: 100rpx;
  background: $c-ink;
  color: #f7f1e6;
  font-size: 28rpx;
  letter-spacing: 4rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.bar__btn--loading {
  background: #6f6656;
}

.mock-entry {
  border-radius: 100rpx;
  border: 1rpx solid rgba(176, 134, 90, 0.55);
  background: #fffaf0;
  padding: 14rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 16rpx;
}
.mock-entry__main {
  font-size: 24rpx;
  color: $c-gold-deep;
  letter-spacing: 1rpx;
}
.mock-entry__sub {
  margin-top: 2rpx;
  font-size: 16rpx;
  color: #b3a894;
}
.cancel-link {
  margin-top: 18rpx;
  text-align: center;
  font-size: 21rpx;
  color: $c-muted;
  padding: 10rpx 0;
}

.success {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 90rpx 32rpx 40rpx;
}
.success__ring {
  width: 150rpx;
  height: 150rpx;
  border-radius: 50%;
  background: #e7efe7;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: pop 0.45s ease-out;
}
.success__check {
  width: 56rpx;
  height: 28rpx;
  border-left: 5rpx solid #2f4a3f;
  border-bottom: 5rpx solid #2f4a3f;
  transform: rotate(-45deg) translate(2rpx, -6rpx);
}
@keyframes pop {
  0% {
    transform: scale(0.6);
    opacity: 0;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}
.success__title {
  margin-top: 36rpx;
  font-family: 'Songti SC', 'STSong', serif;
  font-size: 46rpx;
  color: $c-ink;
  letter-spacing: 6rpx;
}
.success__sub {
  margin-top: 10rpx;
  font-size: 22rpx;
  color: $c-muted;
}
.success__amount {
  margin-top: 34rpx;
  display: flex;
  align-items: baseline;
}
.success__cur {
  font-size: 28rpx;
  color: $c-ink;
}
.success__num {
  font-family: 'Songti SC', 'STSong', serif;
  font-size: 68rpx;
  color: $c-ink;
}
.success__card {
  width: 100%;
  margin-top: 40rpx;
  background: $c-card;
  border-radius: $radius-lg;
  padding: 10rpx 28rpx;
  box-shadow: $shadow-card;
}
.success__actions {
  width: 100%;
  margin-top: 40rpx;
}
.success__btn {
  height: 92rpx;
  border-radius: 100rpx;
  border: 1rpx solid $c-line;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 27rpx;
  color: $c-ink-2;
  margin-bottom: 20rpx;
  background: $c-card;
}
.success__btn--primary {
  background: $c-ink;
  color: #f7f1e6;
  border-color: $c-ink;
}
.success__foot {
  margin-top: 10rpx;
}
.success__foot-line {
  font-size: 20rpx;
  color: $c-muted;
}
</style>
