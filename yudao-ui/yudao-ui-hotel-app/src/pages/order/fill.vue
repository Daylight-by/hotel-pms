<template>
  <view class="page" v-if="room">
    <view class="stay">
      <view class="stay__cover">
        <h-cover
          :variant="variant"
          mark="栖"
          mark-size="120rpx"
          height="200rpx"
          radius="18rpx"
        />
      </view>
      <view class="stay__body">
        <text class="stay__name">{{ room.roomTypeName }}</text>
        <text class="stay__date">{{ checkInDate }} → {{ checkOutDate }}</text>
        <text class="stay__meta">{{ nights }} 晚 · {{ roomCount }} 间 · {{ planName }}</text>
        <text class="stay__policy">{{ planPolicy }}</text>
      </view>
    </view>

    <view class="card">
      <view class="card__head">
        <text class="card__title">入住信息</text>
        <text v-if="member" class="card__badge">会员 · {{ member.memberNo }}</text>
      </view>

      <view class="field">
        <text class="field__label">入住人</text>
        <input
          v-model="form.guestName"
          class="field__input"
          placeholder="请填写入住人真实姓名"
          placeholder-class="ph"
          maxlength="20"
        />
      </view>

      <view class="field">
        <text class="field__label">手机号</text>
        <input
          v-model="form.guestMobile"
          class="field__input"
          type="number"
          placeholder="用于接收入住通知"
          placeholder-class="ph"
          maxlength="11"
        />
      </view>

      <picker mode="selector" :range="arrivalOptions" :value="arrivalIndex" @change="onArrival">
        <view class="field">
          <text class="field__label">预计到店</text>
          <text class="field__value" :class="{ 'field__value--empty': !form.arrivalTime }">
            {{ form.arrivalTime || '请选择到店时间' }}
          </text>
          <text class="field__arrow">›</text>
        </view>
      </picker>

      <view class="field field--column">
        <text class="field__label">备注</text>
        <textarea
          v-model="form.remark"
          class="field__textarea"
          placeholder="如：需要安静房间 / 加婴儿床 / 预计晚到"
          placeholder-class="ph"
          maxlength="120"
        />
      </view>
    </view>

    <view class="card">
      <view class="card__head">
        <text class="card__title">发票需求</text>
        <switch
          :checked="invoiceOn"
          color="#b0865a"
          style="transform: scale(0.8)"
          @change="onInvoiceToggle"
        />
      </view>
      <view v-if="invoiceOn" class="invoice-fields">
        <view class="field">
          <text class="field__label">发票抬头</text>
          <input v-model="form.invoiceTitle" class="field__input" placeholder="个人或公司名称" placeholder-class="ph" />
        </view>
        <view class="field">
          <text class="field__label">税号</text>
          <input v-model="form.invoiceTaxNo" class="field__input" placeholder="个人抬头可不填" placeholder-class="ph" />
        </view>
        <view class="field">
          <text class="field__label">接收邮箱</text>
          <input v-model="form.invoiceEmail" class="field__input" placeholder="电子发票将发送至此邮箱" placeholder-class="ph" />
        </view>
        <text class="invoice-tip">退房后可在订单详情申请开具，电子发票 24 小时内送达</text>
      </view>
      <text v-else class="invoice-off">未选择开票，退房后仍可补开</text>
    </view>

    <view class="card">
      <view class="card__head">
        <text class="card__title">金额明细</text>
        <text class="card__unit">人民币</text>
      </view>
      <view class="amount-row">
        <text class="amount-label">{{ planName }} × {{ nights }} 晚 × {{ roomCount }} 间</text>
        <text class="amount-value">¥{{ money(roomAmount) }}</text>
      </view>
      <view class="amount-row">
        <text class="amount-label">税费与服务费</text>
        <text class="amount-value">已包含</text>
      </view>
      <view class="amount-total">
        <text class="amount-total__label">应付总额</text>
        <view class="amount-total__value">
          <text class="amount-total__cur">¥</text>
          <text class="amount-total__num">{{ money(roomAmount) }}</text>
        </view>
      </view>
      <text class="amount-tip">提交后将为您保留 15 分钟房间，请及时支付</text>
    </view>

    <view class="agreement">
      <text class="agreement__text">
        提交即表示同意《预订与取消政策》。{{ planPolicy }}
      </text>
    </view>

    <view class="bottom-space"></view>

    <view class="bar">
      <view class="bar__price">
        <text class="bar__label">应付</text>
        <view class="bar__amount">
          <text class="bar__cur">¥</text>
          <text class="bar__num">{{ money(roomAmount) }}</text>
        </view>
      </view>
      <view class="bar__btn" :class="{ 'bar__btn--loading': submitting }" @tap="submit">
        {{ submitting ? '提交中…' : '提交订单' }}
      </view>
    </view>
  </view>

  <view v-else class="loading-page">
    <text class="loading-page__text">订单信息读取中…</text>
    <view class="loading-page__btn" @tap="goHome">返回首页</view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { createHold, submitBooking } from '@/api/booking'
import { SELECTED_ROOM_KEY } from '@/utils/config'
import { money, nightsBetween } from '@/utils/format'
import { loadSession, session } from '@/store/session'

const room = ref(null)
const plan = ref(null)
const checkInDate = ref('')
const checkOutDate = ref('')
const roomCount = ref(1)
const submitting = ref(false)
const invoiceOn = ref(false)
const member = computed(() => session.member)

const arrivalOptions = ['14:00 前', '14:00 - 16:00', '16:00 - 18:00', '18:00 - 20:00', '20:00 后', '次日 06:00 前']
const arrivalIndex = ref(2)

const form = ref({
  guestName: '',
  guestMobile: '',
  arrivalTime: '',
  remark: '',
  invoiceTitle: '',
  invoiceTaxNo: '',
  invoiceEmail: ''
})

const nights = computed(() => nightsBetween(checkInDate.value, checkOutDate.value) || 1)
const planName = computed(() => (plan.value ? plan.value.name : '标准价'))
const planPolicy = computed(() => (plan.value ? plan.value.cancelPolicyText : '入住前可免费取消'))
const roomAmount = computed(() => {
  const unit = plan.value ? Number(plan.value.price) : room.value ? Number(room.value.price) : 0
  return Math.round(unit * nights.value * roomCount.value * 100) / 100
})
const variant = computed(() => (((room.value && Number(room.value.roomTypeId)) || 2) % 4) + 1)

onLoad(() => {
  loadSession()
  let cached = null
  try {
    cached = uni.getStorageSync(SELECTED_ROOM_KEY)
  } catch (e) {}
  if (!cached || !cached.room) {
    return
  }
  room.value = cached.room
  plan.value = cached.plan || (cached.room.ratePlans && cached.room.ratePlans[0])
  checkInDate.value = cached.checkInDate
  checkOutDate.value = cached.checkOutDate
  roomCount.value = cached.roomCount || 1
  if (session.member) {
    form.value.guestMobile = session.member.mobile || ''
    form.value.guestName = session.member.nickname && session.member.nickname !== '栖屿会员' ? session.member.nickname : ''
  }
})

function onArrival(e) {
  arrivalIndex.value = Number(e.detail.value)
  form.value.arrivalTime = arrivalOptions[arrivalIndex.value]
}

function onInvoiceToggle(e) {
  invoiceOn.value = e.detail.value
}

function goHome() {
  uni.reLaunch({ url: '/pages/index/index' })
}

function validate() {
  const name = form.value.guestName.trim()
  if (name.length < 2) {
    uni.showToast({ title: '请填写入住人姓名', icon: 'none' })
    return false
  }
  if (!/^1\d{10}$/.test(form.value.guestMobile)) {
    uni.showToast({ title: '请填写正确的手机号', icon: 'none' })
    return false
  }
  if (invoiceOn.value && !form.value.invoiceTitle.trim()) {
    uni.showToast({ title: '请填写发票抬头', icon: 'none' })
    return false
  }
  return true
}

async function submit() {
  if (submitting.value) return
  if (!validate()) return
  submitting.value = true
  try {
    const hold = await createHold({
      roomTypeId: room.value.roomTypeId,
      ratePlanId: plan.value ? plan.value.id : undefined,
      checkInDate: checkInDate.value,
      checkOutDate: checkOutDate.value,
      roomCount: roomCount.value,
      guestName: form.value.guestName.trim(),
      guestMobile: form.value.guestMobile
    })
    const order = await submitBooking({
      holdNo: hold.holdNo,
      guestName: form.value.guestName.trim(),
      guestMobile: form.value.guestMobile,
      arrivalTime: form.value.arrivalTime,
      remark: form.value.remark,
      invoiceNeeded: invoiceOn.value,
      invoiceTitle: form.value.invoiceTitle,
      invoiceTaxNo: form.value.invoiceTaxNo,
      invoiceEmail: form.value.invoiceEmail
    })
    const orderId = order.orderId || order.id
    uni.redirectTo({
      url: `/pages/order/pay?orderId=${orderId}&orderNo=${order.orderNo || ''}&amount=${
        order.payableAmount || roomAmount.value
      }`
    })
  } catch (e) {
    uni.showToast({ title: e.message || '提交失败，请稍后重试', icon: 'none' })
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: $c-bg;
  padding: 28rpx 32rpx 0;
}
.stay {
  display: flex;
  background: $c-card;
  border-radius: $radius-lg;
  padding: 20rpx;
  box-shadow: $shadow-card;
}
.stay__cover {
  width: 200rpx;
  margin-right: 22rpx;
}
.stay__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.stay__name {
  font-family: 'Songti SC', 'STSong', serif;
  font-size: 32rpx;
  color: $c-ink;
}
.stay__date {
  margin-top: 10rpx;
  font-size: 23rpx;
  color: $c-ink-2;
}
.stay__meta {
  margin-top: 6rpx;
  font-size: 21rpx;
  color: $c-muted;
}
.stay__policy {
  margin-top: 8rpx;
  font-size: 20rpx;
  color: #9aa88f;
}

.card {
  margin-top: 26rpx;
  background: $c-card;
  border-radius: $radius-lg;
  padding: 10rpx 28rpx 24rpx;
  box-shadow: $shadow-card;
}
.card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 0 8rpx;
}
.card__title {
  font-family: 'Songti SC', 'STSong', serif;
  font-size: 30rpx;
  color: $c-ink;
}
.card__badge {
  font-size: 19rpx;
  color: $c-gold-deep;
  background: rgba(176, 134, 90, 0.12);
  padding: 4rpx 14rpx;
  border-radius: 100rpx;
}
.card__unit {
  font-size: 19rpx;
  color: $c-muted;
}
.field {
  display: flex;
  align-items: center;
  min-height: 96rpx;
  border-bottom: 1rpx solid $c-line;
  position: relative;
}
.field--column {
  flex-direction: column;
  align-items: flex-start;
  padding: 20rpx 0;
}
.field__label {
  width: 160rpx;
  font-size: 24rpx;
  color: $c-muted;
  flex-shrink: 0;
}
.field__input {
  flex: 1;
  font-size: 26rpx;
  color: $c-ink;
  height: 60rpx;
  line-height: 60rpx;
}
.field__value {
  flex: 1;
  font-size: 26rpx;
  color: $c-ink;
}
.field__value--empty {
  color: #b7ac9b;
}
.field__arrow {
  font-size: 32rpx;
  color: #c3b8a6;
}
.field__textarea {
  width: 100%;
  height: 130rpx;
  margin-top: 12rpx;
  font-size: 25rpx;
  color: $c-ink;
  line-height: 1.6;
}
.ph {
  color: #b7ac9b;
  font-size: 24rpx;
}
.invoice-fields .field:last-of-type {
  border-bottom: none;
}
.invoice-tip {
  display: block;
  padding: 14rpx 0 6rpx;
  font-size: 20rpx;
  color: $c-muted;
  line-height: 1.6;
}
.invoice-off {
  display: block;
  padding: 10rpx 0 20rpx;
  font-size: 21rpx;
  color: $c-muted;
}

.amount-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 0;
}
.amount-label {
  font-size: 23rpx;
  color: $c-muted;
}
.amount-value {
  font-size: 24rpx;
  color: $c-ink-2;
}
.amount-total {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid $c-line;
}
.amount-total__label {
  font-size: 25rpx;
  color: $c-ink;
}
.amount-total__value {
  display: flex;
  align-items: baseline;
}
.amount-total__cur {
  font-size: 24rpx;
  color: $c-gold-deep;
}
.amount-total__num {
  font-family: 'Songti SC', 'STSong', serif;
  font-size: 46rpx;
  color: $c-gold-deep;
}
.amount-tip {
  display: block;
  margin-top: 10rpx;
  font-size: 19rpx;
  color: $c-muted;
}

.agreement {
  padding: 24rpx 8rpx 0;
}
.agreement__text {
  font-size: 20rpx;
  color: $c-muted;
  line-height: 1.7;
}
.bottom-space {
  height: 180rpx;
}

.bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  height: 132rpx;
  padding: 0 32rpx calc(14rpx + env(safe-area-inset-bottom));
  background: rgba(255, 253, 249, 0.97);
  border-top: 1rpx solid $c-line;
  display: flex;
  align-items: center;
  justify-content: space-between;
  z-index: 20;
}
.bar__price {
  display: flex;
  flex-direction: column;
}
.bar__label {
  font-size: 20rpx;
  color: $c-muted;
}
.bar__amount {
  display: flex;
  align-items: baseline;
}
.bar__cur {
  font-size: 24rpx;
  color: $c-ink;
}
.bar__num {
  font-family: 'Songti SC', 'STSong', serif;
  font-size: 44rpx;
  color: $c-ink;
}
.bar__btn {
  width: 320rpx;
  height: 86rpx;
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
.loading-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: $c-bg;
}
.loading-page__text {
  font-size: 24rpx;
  color: $c-muted;
}
.loading-page__btn {
  margin-top: 26rpx;
  padding: 12rpx 36rpx;
  border: 1rpx solid $c-gold;
  color: $c-gold-deep;
  border-radius: 100rpx;
  font-size: 23rpx;
}
</style>
