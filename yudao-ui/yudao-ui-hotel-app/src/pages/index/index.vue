<template>
  <view class="page">
    <view class="hero">
      <view :style="{ height: statusBarHeight + 'px' }"></view>
      <view class="topbar">
        <view class="brand">
          <text class="brand__cn">栖屿</text>
          <text class="brand__en">QIYU HOTEL</text>
        </view>
        <view class="topbar__right">
          <view v-if="mockMode" class="mock-chip">演示数据</view>
          <view class="orders-link" @tap="goOrders">
            <text>{{ member ? '我的订单' : '登录 / 订单' }}</text>
          </view>
        </view>
      </view>

      <view class="hero__body">
        <text class="hero__kicker">杭州 · 西湖</text>
        <text class="hero__title">把夜晚，交给一片安静</text>
        <text class="hero__sub">十二间客房 · 步行至湖边八分钟</text>
      </view>
      <view class="hero__arc"></view>
    </view>

    <view class="search-card">
      <view class="date-row">
        <picker
          class="date-picker"
          mode="date"
          :value="checkIn"
          :start="todayStr"
          @change="onCheckInChange"
        >
          <view class="date-cell">
            <text class="date-label">入住</text>
            <text class="date-value">{{ shortDate(checkIn) }}</text>
            <text class="date-week">{{ weekday(checkIn) }}</text>
          </view>
        </picker>
        <view class="nights">
          <text class="nights__num">{{ nights }}</text>
          <text class="nights__unit">晚</text>
        </view>
        <picker
          class="date-picker"
          mode="date"
          :value="checkOut"
          :start="minCheckOut"
          @change="onCheckOutChange"
        >
          <view class="date-cell date-cell--right">
            <text class="date-label">离店</text>
            <text class="date-value">{{ shortDate(checkOut) }}</text>
            <text class="date-week">{{ weekday(checkOut) }}</text>
          </view>
        </picker>
      </view>

      <view class="divider"></view>

      <view class="guest-row">
        <view class="guest-cell">
          <text class="guest-label">成人</text>
          <view class="stepper">
            <view class="stepper__btn" :class="{ disabled: adults <= 1 }" @tap="changeAdults(-1)">−</view>
            <text class="stepper__num">{{ adults }}</text>
            <view class="stepper__btn" @tap="changeAdults(1)">+</view>
          </view>
        </view>
        <view class="guest-cell">
          <text class="guest-label">房间</text>
          <view class="stepper">
            <view class="stepper__btn" :class="{ disabled: roomCount <= 1 }" @tap="changeRooms(-1)">−</view>
            <text class="stepper__num">{{ roomCount }}</text>
            <view class="stepper__btn" :class="{ disabled: roomCount >= 3 }" @tap="changeRooms(1)">+</view>
          </view>
        </view>
        <view class="search-btn" @tap="reload">
          <text>查询</text>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="section__head">
        <view>
          <text class="section__kicker">ROOMS</text>
          <text class="section__title">可订房型</text>
        </view>
        <text class="section__hint">{{ nights }} 晚 · {{ roomCount }} 间</text>
      </view>

      <view v-if="loading" class="skeleton-wrap">
        <view v-for="i in 2" :key="i" class="skeleton">
          <view class="skeleton__cover"></view>
          <view class="skeleton__line skeleton__line--lg"></view>
          <view class="skeleton__line"></view>
        </view>
      </view>

      <template v-else>
        <view v-if="error" class="error-box">
          <text class="error-box__text">{{ error }}</text>
          <view class="error-box__btn" @tap="reload">重新加载</view>
        </view>

        <view
          v-for="(room, idx) in rooms"
          :key="room.roomTypeId"
          class="room-card"
          @tap="goDetail(room)"
        >
          <h-cover
            :variant="variantOf(room, idx)"
            :mark="room.roomTypeName"
            :badge="room.availableCount > 0 && room.availableCount <= 2 ? '仅剩 ' + room.availableCount + ' 间' : ''"
            height="300rpx"
            radius="0"
          />
          <view class="room-card__body">
            <view class="room-card__top">
              <text class="room-name">{{ room.roomTypeName }}</text>
              <view class="price">
                <text class="price__cur">¥</text>
                <text class="price__num">{{ money(room.price) }}</text>
                <text class="price__unit">起/晚</text>
              </view>
            </view>

            <view class="room-tags">
              <text class="tag">{{ room.bedType }}</text>
              <text class="tag">{{ room.breakfast }}</text>
              <text class="tag">{{ room.window }}</text>
              <text v-if="room.area" class="tag">{{ room.area }}㎡</text>
            </view>

            <view class="room-card__foot">
              <text class="room-intro">{{ room.intro }}</text>
              <text class="room-link">详情 ›</text>
            </view>

            <view v-if="room.availableCount <= 0" class="sold-out">该日期已满房</view>
          </view>
        </view>

        <h-empty
          v-if="!rooms.length && !error"
          title="这几天暂无可订房型"
          desc="试着换个日期，或直接致电前台 0571-8888 6666"
        />
      </template>
    </view>

    <view class="hotel-foot">
      <text class="hotel-foot__name">栖屿酒店 · QIYU HOTEL</text>
      <text class="hotel-foot__line">杭州市西湖区留白路 18 号</text>
      <text class="hotel-foot__line">前台 0571-8888 6666 · 24 小时</text>
      <text class="hotel-foot__tip">本页面为酒店 PMS 演示环境</text>
    </view>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad, onShow, onUnload } from '@dcloudio/uni-app'
import { fetchAvailability } from '@/api/booking'
import { addDays, fmtDate, money, nightsBetween, weekday } from '@/utils/format'
import { SELECTED_ROOM_KEY } from '@/utils/config'
import { loadSession, session } from '@/store/session'
import { isMockMode, onMockModeChange } from '@/utils/request'

const statusBarHeight = ref(20)
const todayStr = ref(fmtDate(new Date()))
const checkIn = ref('')
const checkOut = ref('')
const adults = ref(2)
const roomCount = ref(1)
const rooms = ref([])
const loading = ref(true)
const error = ref('')
const mockMode = ref(isMockMode())
const member = computed(() => session.member)

const nights = computed(() => nightsBetween(checkIn.value, checkOut.value) || 1)
const minCheckOut = computed(() => fmtDate(addDays(checkIn.value, 1)))

let offMock = null

onLoad(() => {
  const info = uni.getSystemInfoSync()
  statusBarHeight.value = info.statusBarHeight || 20
  todayStr.value = fmtDate(new Date())
  checkIn.value = todayStr.value
  checkOut.value = fmtDate(addDays(todayStr.value, 1))
  loadSession()
  offMock = onMockModeChange((on) => {
    mockMode.value = on
  })
  reload()
})

onShow(() => {
  loadSession()
})

function shortDate(value) {
  if (!value) return '--'
  const parts = String(value).split('-')
  return `${Number(parts[1])}月${Number(parts[2])}日`
}

function variantOf(room, idx) {
  return ((Number(room.roomTypeId) || idx + 1) % 4) + 1
}

async function reload() {
  if (nights.value <= 0) {
    uni.showToast({ title: '离店日期需晚于入住日期', icon: 'none' })
    return
  }
  loading.value = true
  error.value = ''
  try {
    const res = await fetchAvailability({
      checkInDate: checkIn.value,
      checkOutDate: checkOut.value,
      adultCount: adults.value,
      roomCount: roomCount.value
    })
    rooms.value = res.list || []
    mockMode.value = isMockMode()
  } catch (e) {
    error.value = e.message || '房型加载失败'
    rooms.value = []
  } finally {
    loading.value = false
  }
}

function onCheckInChange(e) {
  checkIn.value = e.detail.value
  if (nightsBetween(checkIn.value, checkOut.value) <= 0) {
    checkOut.value = fmtDate(addDays(checkIn.value, 1))
  }
  reload()
}

function onCheckOutChange(e) {
  if (nightsBetween(checkIn.value, e.detail.value) <= 0) {
    uni.showToast({ title: '离店日期需晚于入住日期', icon: 'none' })
    return
  }
  checkOut.value = e.detail.value
  reload()
}

function changeAdults(delta) {
  const next = adults.value + delta
  if (next < 1 || next > 4) return
  adults.value = next
}

function changeRooms(delta) {
  const next = roomCount.value + delta
  if (next < 1 || next > 3) return
  roomCount.value = next
  reload()
}

function goDetail(room) {
  try {
    uni.setStorageSync(SELECTED_ROOM_KEY, {
      room,
      checkInDate: checkIn.value,
      checkOutDate: checkOut.value,
      nights: nights.value,
      adults: adults.value,
      roomCount: roomCount.value
    })
  } catch (e) {}
  uni.navigateTo({ url: `/pages/room/detail?id=${room.roomTypeId}` })
}

function goOrders() {
  if (!session.token) {
    uni.navigateTo({ url: '/pages/login/index?redirect=' + encodeURIComponent('/pages/order/list') })
    return
  }
  uni.navigateTo({ url: '/pages/order/list' })
}

onUnload(() => {
  if (offMock) offMock()
})
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  padding-bottom: 60rpx;
  background: $c-bg;
}

.hero {
  position: relative;
  background: linear-gradient(168deg, #e9dfcf 0%, #f2e9db 52%, #f6f1e8 100%);
  padding: 0 40rpx 150rpx;
  overflow: hidden;
}
.hero__arc {
  position: absolute;
  right: -160rpx;
  top: -120rpx;
  width: 520rpx;
  height: 520rpx;
  border-radius: 50%;
  border: 1rpx solid rgba(176, 134, 90, 0.28);
  pointer-events: none;
}
.hero__arc::after {
  content: '';
  position: absolute;
  left: 60rpx;
  top: 60rpx;
  right: 60rpx;
  bottom: 60rpx;
  border-radius: 50%;
  border: 1rpx solid rgba(176, 134, 90, 0.18);
}
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18rpx 0 0;
}
/* #ifdef MP-WEIXIN */
/* 给微信右上角胶囊按钮留位 */
.topbar {
  padding-right: 180rpx;
}
/* #endif */
.brand {
  display: flex;
  align-items: baseline;
}
.brand__cn {
  font-family: 'Songti SC', 'STSong', 'Noto Serif SC', serif;
  font-size: 38rpx;
  letter-spacing: 8rpx;
  color: $c-ink;
}
.brand__en {
  margin-left: 14rpx;
  font-size: 17rpx;
  letter-spacing: 4rpx;
  color: $c-muted;
}
.topbar__right {
  display: flex;
  align-items: center;
}
.mock-chip {
  margin-right: 14rpx;
  padding: 4rpx 14rpx;
  border-radius: 100rpx;
  background: rgba(176, 134, 90, 0.12);
  color: $c-gold-deep;
  font-size: 19rpx;
  letter-spacing: 1rpx;
}
.orders-link {
  padding: 10rpx 22rpx;
  border: 1rpx solid rgba(33, 29, 23, 0.18);
  border-radius: 100rpx;
  font-size: 22rpx;
  color: $c-ink-2;
  background: rgba(255, 253, 249, 0.5);
}
.hero__body {
  margin-top: 74rpx;
  display: flex;
  flex-direction: column;
}
.hero__kicker {
  font-size: 21rpx;
  letter-spacing: 6rpx;
  color: $c-gold-deep;
  margin-bottom: 20rpx;
}
.hero__title {
  font-family: 'Songti SC', 'STSong', 'Noto Serif SC', serif;
  font-size: 58rpx;
  line-height: 1.32;
  color: $c-ink;
  letter-spacing: 2rpx;
}
.hero__sub {
  margin-top: 22rpx;
  font-size: 23rpx;
  letter-spacing: 2rpx;
  color: $c-muted;
}

.search-card {
  position: relative;
  margin: -110rpx 32rpx 0;
  background: $c-card;
  border-radius: $radius-lg;
  box-shadow: $shadow-float;
  padding: 10rpx 30rpx 26rpx;
}
.date-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 26rpx 0 22rpx;
}
.date-picker {
  flex: 1;
  display: block;
}
.date-cell {
  display: flex;
  flex-direction: column;
}
.date-cell--right {
  align-items: flex-end;
  text-align: right;
}
.date-label {
  font-size: 20rpx;
  letter-spacing: 3rpx;
  color: $c-muted;
}
.date-value {
  margin-top: 6rpx;
  font-family: 'Songti SC', 'STSong', serif;
  font-size: 40rpx;
  color: $c-ink;
}
.date-week {
  margin-top: 2rpx;
  font-size: 21rpx;
  color: $c-muted;
}
.nights {
  width: 116rpx;
  height: 60rpx;
  border-radius: 100rpx;
  background: $c-bg-deep;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 18rpx;
}
.nights__num {
  font-size: 26rpx;
  color: $c-gold-deep;
}
.nights__unit {
  margin-left: 4rpx;
  font-size: 20rpx;
  color: $c-gold-deep;
}
.divider {
  height: 1rpx;
  background: $c-line;
}
.guest-row {
  display: flex;
  align-items: center;
  padding-top: 24rpx;
}
.guest-cell {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.guest-label {
  font-size: 20rpx;
  letter-spacing: 3rpx;
  color: $c-muted;
}
.stepper {
  margin-top: 10rpx;
  display: flex;
  align-items: center;
}
.stepper__btn {
  width: 46rpx;
  height: 46rpx;
  border-radius: 50%;
  border: 1rpx solid $c-line;
  color: $c-ink-2;
  font-size: 30rpx;
  line-height: 44rpx;
  text-align: center;
}
.stepper__btn.disabled {
  color: #cdc4b5;
}
.stepper__num {
  width: 62rpx;
  text-align: center;
  font-size: 28rpx;
  color: $c-ink;
}
.search-btn {
  width: 156rpx;
  height: 78rpx;
  border-radius: 100rpx;
  background: $c-ink;
  color: #f7f1e6;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 27rpx;
  letter-spacing: 4rpx;
}

.section {
  padding: 54rpx 32rpx 0;
}
.section__head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 26rpx;
  padding: 0 8rpx;
}
.section__kicker {
  display: block;
  font-size: 18rpx;
  letter-spacing: 5rpx;
  color: $c-gold;
}
.section__title {
  display: block;
  margin-top: 6rpx;
  font-family: 'Songti SC', 'STSong', serif;
  font-size: 40rpx;
  color: $c-ink;
}
.section__hint {
  font-size: 21rpx;
  color: $c-muted;
}

.room-card {
  background: $c-card;
  border-radius: $radius-lg;
  overflow: hidden;
  box-shadow: $shadow-card;
  margin-bottom: 30rpx;
  position: relative;
}
.room-card__body {
  padding: 26rpx 28rpx 28rpx;
}
.room-card__top {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}
.room-name {
  font-family: 'Songti SC', 'STSong', serif;
  font-size: 36rpx;
  color: $c-ink;
  letter-spacing: 1rpx;
}
.price {
  display: flex;
  align-items: baseline;
  flex-shrink: 0;
  white-space: nowrap;
}
.price__cur {
  font-size: 22rpx;
  color: $c-gold-deep;
}
.price__num {
  font-family: 'Songti SC', 'STSong', serif;
  font-size: 42rpx;
  color: $c-gold-deep;
  margin: 0 2rpx;
}
.price__unit {
  font-size: 19rpx;
  color: $c-muted;
}
.room-tags {
  display: flex;
  flex-wrap: wrap;
  margin-top: 14rpx;
}
.tag {
  font-size: 20rpx;
  color: $c-ink-2;
  background: $c-bg;
  border-radius: 6rpx;
  padding: 4rpx 14rpx;
  margin: 0 12rpx 10rpx 0;
}
.room-card__foot {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-top: 6rpx;
  border-top: 1rpx solid $c-line;
  padding-top: 18rpx;
}
.room-intro {
  flex: 1;
  font-size: 22rpx;
  color: $c-muted;
  line-height: 1.55;
  margin-right: 20rpx;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.room-link {
  font-size: 22rpx;
  color: $c-gold-deep;
  white-space: nowrap;
}
.sold-out {
  position: absolute;
  right: 28rpx;
  top: 84rpx;
  font-size: 20rpx;
  color: $c-red;
}

.skeleton-wrap {
  padding-top: 4rpx;
}
.skeleton {
  background: $c-card;
  border-radius: $radius-lg;
  overflow: hidden;
  margin-bottom: 30rpx;
  padding-bottom: 28rpx;
}
.skeleton__cover {
  height: 300rpx;
  background: linear-gradient(100deg, #efe8dc 30%, #f7f2e9 50%, #efe8dc 70%);
}
.skeleton__line {
  height: 24rpx;
  background: #efe8dc;
  border-radius: 8rpx;
  margin: 22rpx 28rpx 0;
  width: 40%;
}
.skeleton__line--lg {
  width: 64%;
  height: 32rpx;
}

.error-box {
  background: $c-card;
  border-radius: $radius-lg;
  padding: 60rpx 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.error-box__text {
  font-size: 24rpx;
  color: $c-muted;
  margin-bottom: 22rpx;
}
.error-box__btn {
  padding: 12rpx 34rpx;
  border: 1rpx solid $c-gold;
  color: $c-gold-deep;
  border-radius: 100rpx;
  font-size: 24rpx;
}

.hotel-foot {
  margin-top: 30rpx;
  padding: 44rpx 40rpx 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  border-top: 1rpx solid $c-line;
}
.hotel-foot__name {
  font-family: 'Songti SC', 'STSong', serif;
  font-size: 26rpx;
  letter-spacing: 3rpx;
  color: $c-ink-2;
}
.hotel-foot__line {
  margin-top: 8rpx;
  font-size: 21rpx;
  color: $c-muted;
}
.hotel-foot__tip {
  margin-top: 18rpx;
  font-size: 19rpx;
  letter-spacing: 2rpx;
  color: #b3a894;
}
</style>
