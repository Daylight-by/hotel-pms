<template>
  <view class="page" v-if="order">
    <view class="hero">
      <text class="hero__status">{{ statusText(order.status) }}</text>
      <text class="hero__hint">{{ heroHint }}</text>
      <view class="hero__amount">
        <text class="hero__cur">¥</text>
        <text class="hero__num">{{ money(order.payableAmount) }}</text>
      </view>
      <text class="hero__no">订单号 {{ order.orderNo || order.id }}</text>
    </view>

    <view class="card">
      <view class="room">
        <view class="room__cover">
          <h-cover
            :variant="variant"
            mark="栖"
            mark-size="100rpx"
            height="160rpx"
            radius="16rpx"
          />
        </view>
        <view class="room__body">
          <text class="room__name">{{ order.roomTypeName }}</text>
          <text class="room__meta">{{ order.bedType }} · {{ order.breakfast || '不含早' }}</text>
          <text class="room__plan">{{ order.ratePlanName || '标准价' }}</text>
        </view>
      </view>

      <view class="rows">
        <view class="row">
          <text class="row__label">入住</text>
          <text class="row__value">{{ order.checkInDate }}（14:00 后）</text>
        </view>
        <view class="row">
          <text class="row__label">离店</text>
          <text class="row__value">{{ order.checkOutDate }}（12:00 前）</text>
        </view>
        <view class="row">
          <text class="row__label">间夜</text>
          <text class="row__value">{{ nights }} 晚 · {{ order.roomCount || 1 }} 间</text>
        </view>
        <view class="row">
          <text class="row__label">入住人</text>
          <text class="row__value">{{ order.guestName }} {{ maskMobile(order.guestMobile) }}</text>
        </view>
        <view v-if="order.arrivalTime" class="row">
          <text class="row__label">预计到店</text>
          <text class="row__value">{{ order.arrivalTime }}</text>
        </view>
        <view v-if="order.remark" class="row">
          <text class="row__label">备注</text>
          <text class="row__value row__value--wrap">{{ order.remark }}</text>
        </view>
      </view>
    </view>

    <view class="card">
      <view class="card__head">
        <text class="card__title">费用明细</text>
        <text class="card__sub">{{ payMethodText }}</text>
      </view>
      <view class="fee">
        <view class="fee__row">
          <text class="fee__label">房费（{{ nights }} 晚）</text>
          <text class="fee__value">¥{{ money(order.roomAmount || order.payableAmount) }}</text>
        </view>
        <view class="fee__row">
          <text class="fee__label">税费与服务费</text>
          <text class="fee__value">已包含</text>
        </view>
        <view class="fee__row">
          <text class="fee__label">已支付</text>
          <text class="fee__value">¥{{ money(order.paidAmount) }}</text>
        </view>
        <view class="fee__total">
          <text class="fee__total-label">应付总额</text>
          <text class="fee__total-value">¥{{ money(order.payableAmount) }}</text>
        </view>
      </view>
    </view>

    <view class="card">
      <view class="card__head">
        <text class="card__title">电子发票</text>
        <text class="card__sub">{{ invoiceText }}</text>
      </view>
      <view v-if="canInvoice" class="invoice-row">
        <text class="invoice-row__hint">支持开具电子普票，退房后 24 小时内发送至邮箱</text>
        <view class="invoice-row__btn" @tap="openInvoice">申请开票</view>
      </view>
      <view v-else class="invoice-row">
        <text class="invoice-row__hint">{{ invoiceHint }}</text>
      </view>
    </view>

    <view class="card">
      <view class="card__head">
        <text class="card__title">订单进度</text>
      </view>
      <view class="timeline">
        <view v-for="(step, idx) in steps" :key="idx" class="step" :class="{ 'step--done': step.done }">
          <view class="step__dot"></view>
          <view v-if="idx < steps.length - 1" class="step__line"></view>
          <view class="step__body">
            <text class="step__name">{{ step.name }}</text>
            <text class="step__time">{{ step.time || step.hint }}</text>
          </view>
        </view>
      </view>
    </view>

    <view class="bottom-space"></view>

    <view class="bar">
      <view class="bar__ghost" @tap="goList">全部订单</view>
      <view class="bar__actions">
        <view v-if="order.status === 10" class="bar__btn bar__btn--ghost" @tap="cancelOrderAction">取消</view>
        <view v-if="order.status === 10" class="bar__btn bar__btn--primary" @tap="goPay">去支付</view>
        <view v-if="order.status === 20" class="bar__btn bar__btn--primary" @tap="doCheckIn">自助入住</view>
        <view v-if="order.status === 30" class="bar__btn bar__btn--primary" @tap="doCheckOut">自助退房</view>
        <view v-if="order.status === 40 && canInvoice" class="bar__btn bar__btn--primary" @tap="openInvoice">
          申请发票
        </view>
        <view v-if="order.status === 50 || order.status === 40" class="bar__btn bar__btn--ghost" @tap="rebook">
          再次预订
        </view>
      </view>
    </view>

    <view v-if="invoiceVisible" class="mask" @tap="closeInvoice"></view>
    <view v-if="invoiceVisible" class="sheet">
      <view class="sheet__head">
        <text class="sheet__title">申请电子发票</text>
        <text class="sheet__close" @tap="closeInvoice">×</text>
      </view>
      <view class="sheet__field">
        <text class="sheet__label">发票抬头</text>
        <input v-model="invoice.title" class="sheet__input" placeholder="个人或公司名称" />
      </view>
      <view class="sheet__field">
        <text class="sheet__label">税号</text>
        <input v-model="invoice.taxNo" class="sheet__input" placeholder="个人抬头可不填" />
      </view>
      <view class="sheet__field">
        <text class="sheet__label">接收邮箱</text>
        <input v-model="invoice.email" class="sheet__input" placeholder="电子发票发送地址" />
      </view>
      <view class="sheet__btn" @tap="submitInvoice">提交申请</view>
      <text class="sheet__tip">开票金额 ¥{{ money(order.payableAmount) }} · 电子普票</text>
    </view>
  </view>

  <view v-else class="loading-page">
    <text class="loading-page__text">{{ loadingText }}</text>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { cancelOrder, fetchOrderDetail } from '@/api/booking'
import { applyInvoice, selfCheckIn as apiSelfCheckIn, selfCheckOut as apiSelfCheckOut } from '@/api/member'
import { money, nightsBetween, parseDate, statusText } from '@/utils/format'

const order = ref(null)
const loadingText = ref('订单读取中…')
const invoiceVisible = ref(false)
const invoice = ref({ title: '', taxNo: '', email: '' })
const autoInvoice = ref(false)

const nights = computed(() => (order.value ? nightsBetween(order.value.checkInDate, order.value.checkOutDate) : 1))
const variant = computed(() => ((Number(order.value && order.value.roomTypeId) || 1) % 4) + 1)

const canInvoice = computed(() => {
  const o = order.value
  if (!o) return false
  const paid = o.paymentStatus === 30 || Number(o.paidAmount) > 0
  return (
    paid &&
    [20, 30, 40].indexOf(Number(o.status)) >= 0 &&
    (!o.invoiceStatus || o.invoiceStatus === 0 || o.invoiceStatus === 10)
  )
})

const invoiceText = computed(() => {
  const s = order.value && order.value.invoiceStatus
  if (!s) return '未申请'
  if (s === 10) return '待开票'
  if (s === 20) return '已提交'
  if (s === 30) return '已开具'
  return '未申请'
})

const invoiceHint = computed(() => {
  const s = order.value && order.value.invoiceStatus
  if (s === 20) return '发票申请已提交，开票后将发送至 ' + (order.value.invoiceEmail || '预留邮箱')
  if (s === 30) return '发票已开具，可在邮箱查收'
  if (!s) return '入住或退房后可申请电子发票'
  return '发票信息已记录'
})

const payMethodText = computed(() => {
  const m = order.value && order.value.payMethod
  if (m === 3 || m === 'wxpay') return '微信支付'
  if (m === 4 || m === 'alipay') return '支付宝'
  if (m === 5) return '在线支付'
  return '未支付'
})

const heroHint = computed(() => {
  const s = order.value && order.value.status
  if (s === 10) return '房间已为您保留，请尽快完成支付'
  if (s === 20) return '预订已确认，期待与您相见'
  if (s === 30) return '已入住，祝您入住愉快'
  if (s === 40) return '感谢入住，欢迎再次光临'
  if (s === 50) return '订单已取消，房量已释放'
  return ''
})

const steps = computed(() => {
  const o = order.value || {}
  const payed = o.paymentStatus === 30 || !!o.payTime
  return [
    { name: '提交订单', done: true, time: fmtTime(o.createTime) },
    { name: '支付成功', done: payed, time: payed ? fmtTime(o.payTime) : '待支付' },
    { name: '预订确认', done: Number(o.status) >= 20, time: Number(o.status) >= 20 ? '已确认' : '支付后自动确认' },
    {
      name: '办理入住',
      done: Number(o.status) >= 30 && Number(o.status) !== 50,
      time: o.actualCheckInTime ? fmtTime(o.actualCheckInTime) : '入住当天可自助办理'
    },
    {
      name: '办理退房',
      done: Number(o.status) >= 40 && Number(o.status) !== 50,
      time: o.actualCheckOutTime ? fmtTime(o.actualCheckOutTime) : '离店当天 12:00 前'
    }
  ]
})

onLoad(async (options) => {
  autoInvoice.value = options.invoice === '1'
  if (!options.id) {
    loadingText.value = '订单不存在'
    return
  }
  await load(options.id)
  if (autoInvoice.value && canInvoice.value) {
    openInvoice()
  }
})

async function load(id) {
  try {
    const detail = await fetchOrderDetail(id)
    order.value = detail
    if (detail) {
      invoice.value.title = detail.invoiceTitle || ''
      invoice.value.taxNo = detail.invoiceTaxNo || ''
      invoice.value.email = detail.invoiceEmail || ''
    }
    loadingText.value = '订单不存在'
  } catch (e) {
    loadingText.value = e.message || '订单加载失败'
  }
}

function fmtTime(value) {
  if (!value) return ''
  if (typeof value === 'number') {
    const d = new Date(value)
    return `${d.getMonth() + 1}月${d.getDate()}日 ${String(d.getHours()).padStart(2, '0')}:${String(
      d.getMinutes()
    ).padStart(2, '0')}`
  }
  const d = parseDate(value)
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

function maskMobile(mobile) {
  if (!mobile) return ''
  return String(mobile).replace(/^(\d{3})\d{4}(\d{4})$/, '$1****$2')
}

function goList() {
  uni.redirectTo({ url: '/pages/order/list' })
}

function goPay() {
  uni.navigateTo({
    url: `/pages/order/pay?orderId=${order.value.id}&orderNo=${order.value.orderNo || ''}&amount=${
      order.value.payableAmount || 0
    }`
  })
}

function cancelOrderAction() {
  uni.showModal({
    title: '取消订单',
    content: '取消后房间将释放，确定取消吗？',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await cancelOrder(order.value.id, '客人主动取消')
        uni.showToast({ title: '已取消', icon: 'none' })
        await load(order.value.id)
      } catch (e) {
        uni.showToast({ title: e.message || '取消失败', icon: 'none' })
      }
    }
  })
}

function doCheckIn() {
  uni.showModal({
    title: '自助入住',
    content: '确认办理入住？办理后可直接前往客房。',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await apiSelfCheckIn(order.value.id)
        uni.showToast({ title: '入住成功', icon: 'none' })
        await load(order.value.id)
      } catch (e) {
        uni.showToast({ title: e.message || '办理失败', icon: 'none' })
      }
    }
  })
}

function doCheckOut() {
  uni.showModal({
    title: '自助退房',
    content: '确认退房？退房后将通知客房清洁。',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await apiSelfCheckOut(order.value.id)
        uni.showToast({ title: '退房成功', icon: 'none' })
        await load(order.value.id)
      } catch (e) {
        uni.showToast({ title: e.message || '办理失败', icon: 'none' })
      }
    }
  })
}

function openInvoice() {
  if (!canInvoice.value) {
    uni.showToast({ title: '当前订单暂不可开票', icon: 'none' })
    return
  }
  invoiceVisible.value = true
}

function closeInvoice() {
  invoiceVisible.value = false
}

async function submitInvoice() {
  if (!invoice.value.title || invoice.value.title.trim().length < 2) {
    uni.showToast({ title: '请填写发票抬头', icon: 'none' })
    return
  }
  try {
    await applyInvoice({
      orderId: order.value.id,
      title: invoice.value.title.trim(),
      taxNo: invoice.value.taxNo,
      email: invoice.value.email,
      type: 1
    })
    invoiceVisible.value = false
    uni.showToast({ title: '申请已提交', icon: 'none' })
    await load(order.value.id)
  } catch (e) {
    uni.showToast({ title: e.message || '提交失败', icon: 'none' })
  }
}

function rebook() {
  uni.reLaunch({ url: '/pages/index/index' })
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: $c-bg;
  padding-bottom: 40rpx;
}
.hero {
  padding: 46rpx 40rpx 52rpx;
  background: linear-gradient(160deg, #ebe1d1 0%, #f4ecdf 60%, #f6f1e8 100%);
  display: flex;
  flex-direction: column;
}
.hero__status {
  font-family: 'Songti SC', 'STSong', serif;
  font-size: 44rpx;
  color: $c-ink;
  letter-spacing: 3rpx;
}
.hero__hint {
  margin-top: 10rpx;
  font-size: 21rpx;
  color: $c-muted;
}
.hero__amount {
  display: flex;
  align-items: baseline;
  margin-top: 26rpx;
}
.hero__cur {
  font-size: 28rpx;
  color: $c-ink;
}
.hero__num {
  font-family: 'Songti SC', 'STSong', serif;
  font-size: 64rpx;
  color: $c-ink;
}
.hero__no {
  margin-top: 8rpx;
  font-size: 19rpx;
  color: #a39682;
  letter-spacing: 1rpx;
}

.card {
  margin: 24rpx 32rpx 0;
  background: $c-card;
  border-radius: $radius-lg;
  padding: 8rpx 28rpx 20rpx;
  box-shadow: $shadow-card;
}
.card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 0 10rpx;
}
.card__title {
  font-family: 'Songti SC', 'STSong', serif;
  font-size: 29rpx;
  color: $c-ink;
}
.card__sub {
  font-size: 20rpx;
  color: $c-muted;
}
.room {
  display: flex;
  padding: 20rpx 0 6rpx;
}
.room__cover {
  width: 170rpx;
  margin-right: 20rpx;
}
.room__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.room__name {
  font-family: 'Songti SC', 'STSong', serif;
  font-size: 30rpx;
  color: $c-ink;
}
.room__meta {
  margin-top: 6rpx;
  font-size: 21rpx;
  color: $c-muted;
}
.room__plan {
  margin-top: 6rpx;
  font-size: 20rpx;
  color: $c-gold-deep;
}
.rows {
  margin-top: 12rpx;
  border-top: 1rpx solid $c-line;
}
.row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 16rpx 0;
}
.row__label {
  font-size: 22rpx;
  color: $c-muted;
  flex-shrink: 0;
  margin-right: 24rpx;
}
.row__value {
  font-size: 23rpx;
  color: $c-ink-2;
  text-align: right;
}
.row__value--wrap {
  flex: 1;
  line-height: 1.6;
}
.fee {
  padding-top: 6rpx;
}
.fee__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14rpx 0;
}
.fee__label {
  font-size: 22rpx;
  color: $c-muted;
}
.fee__value {
  font-size: 23rpx;
  color: $c-ink-2;
}
.fee__total {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-top: 1rpx solid $c-line;
  margin-top: 6rpx;
  padding-top: 20rpx;
}
.fee__total-label {
  font-size: 24rpx;
  color: $c-ink;
}
.fee__total-value {
  font-family: 'Songti SC', 'STSong', serif;
  font-size: 38rpx;
  color: $c-gold-deep;
}

.invoice-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10rpx 0 16rpx;
}
.invoice-row__hint {
  flex: 1;
  font-size: 21rpx;
  color: $c-muted;
  margin-right: 20rpx;
  line-height: 1.6;
}
.invoice-row__btn {
  padding: 10rpx 26rpx;
  border: 1rpx solid $c-gold;
  color: $c-gold-deep;
  border-radius: 100rpx;
  font-size: 21rpx;
  white-space: nowrap;
}

.timeline {
  padding: 16rpx 0 6rpx;
}
.step {
  position: relative;
  padding-left: 46rpx;
  padding-bottom: 34rpx;
}
.step:last-child {
  padding-bottom: 0;
}
.step__dot {
  position: absolute;
  left: 6rpx;
  top: 8rpx;
  width: 18rpx;
  height: 18rpx;
  border-radius: 50%;
  background: #d8ccb8;
}
.step--done .step__dot {
  background: $c-gold-deep;
  box-shadow: 0 0 0 6rpx rgba(176, 134, 90, 0.14);
}
.step__line {
  position: absolute;
  left: 14rpx;
  top: 30rpx;
  bottom: 6rpx;
  width: 1rpx;
  background: $c-line;
}
.step__body {
  display: flex;
  flex-direction: column;
}
.step__name {
  font-size: 24rpx;
  color: #9d9384;
}
.step--done .step__name {
  color: $c-ink;
}
.step__time {
  margin-top: 2rpx;
  font-size: 20rpx;
  color: $c-muted;
}

.bottom-space {
  height: 170rpx;
}
.bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 16rpx 32rpx calc(16rpx + env(safe-area-inset-bottom));
  background: rgba(255, 253, 249, 0.98);
  border-top: 1rpx solid $c-line;
  display: flex;
  align-items: center;
  justify-content: space-between;
  z-index: 20;
}
.bar__ghost {
  font-size: 22rpx;
  color: $c-muted;
  padding: 16rpx 10rpx;
}
.bar__actions {
  display: flex;
  align-items: center;
}
.bar__btn {
  height: 78rpx;
  padding: 0 34rpx;
  border-radius: 100rpx;
  font-size: 25rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 16rpx;
}
.bar__btn--primary {
  background: $c-ink;
  color: #f7f1e6;
  letter-spacing: 2rpx;
}
.bar__btn--ghost {
  border: 1rpx solid $c-line;
  color: $c-ink-2;
}

.mask {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  background: rgba(24, 20, 15, 0.42);
  z-index: 40;
}
.sheet {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: $c-bg;
  border-radius: 36rpx 36rpx 0 0;
  padding: 36rpx 40rpx calc(40rpx + env(safe-area-inset-bottom));
  z-index: 41;
}
.sheet__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}
.sheet__title {
  font-family: 'Songti SC', 'STSong', serif;
  font-size: 34rpx;
  color: $c-ink;
}
.sheet__close {
  font-size: 40rpx;
  color: $c-muted;
  padding: 0 8rpx;
}
.sheet__field {
  display: flex;
  align-items: center;
  height: 92rpx;
  border-bottom: 1rpx solid $c-line;
}
.sheet__label {
  width: 150rpx;
  font-size: 23rpx;
  color: $c-muted;
}
.sheet__input {
  flex: 1;
  font-size: 25rpx;
  color: $c-ink;
}
.sheet__btn {
  margin-top: 32rpx;
  height: 92rpx;
  border-radius: 100rpx;
  background: $c-ink;
  color: #f7f1e6;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 27rpx;
  letter-spacing: 3rpx;
}
.sheet__tip {
  display: block;
  margin-top: 14rpx;
  text-align: center;
  font-size: 19rpx;
  color: $c-muted;
}
.loading-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $c-bg;
}
.loading-page__text {
  font-size: 24rpx;
  color: $c-muted;
}
</style>
