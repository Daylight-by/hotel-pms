<template>
  <view class="page" v-if="room">
    <view class="gallery">
      <swiper
        class="gallery__swiper"
        :current="current"
        circular
        @change="onSwiper"
      >
        <swiper-item v-for="(slide, idx) in slides" :key="idx">
          <h-cover
            :src="slide.src"
            :variant="slide.variant"
            :title="idx === 0 ? room.roomTypeName : slide.title"
            :sub="idx === 0 ? room.bedType : slide.sub"
            height="540rpx"
            radius="0"
          />
        </swiper-item>
      </swiper>
      <view class="gallery__count">{{ current + 1 }} / {{ slides.length }}</view>
    </view>

    <view class="sheet">
      <view class="head">
        <view class="head__left">
          <text class="name">{{ room.roomTypeName }}</text>
          <text class="code">{{ room.roomTypeCode }} · {{ room.area }}㎡ · 至多 {{ room.maxOccupancy }} 人</text>
        </view>
        <view class="head__price">
          <text class="head__cur">¥</text>
          <text class="head__num">{{ money(selectedPlan ? selectedPlan.price : room.price) }}</text>
          <text class="head__unit">起/晚</text>
        </view>
      </view>

      <view class="tags">
        <text class="tag">{{ room.bedType }}</text>
        <text class="tag">{{ room.breakfast }}</text>
        <text class="tag">{{ room.window }}</text>
      </view>

      <text class="intro">{{ room.intro }}</text>

      <view class="block">
        <text class="block__kicker">AMENITIES</text>
        <text class="block__title">房间设施</text>
        <view class="amenities">
          <view v-for="a in room.amenities" :key="a" class="amenity">
            <view class="amenity__dot"></view>
            <text class="amenity__text">{{ a }}</text>
          </view>
        </view>
      </view>

      <view class="block">
        <view class="block__head">
          <view>
            <text class="block__kicker">RATES</text>
            <text class="block__title">逐日房价</text>
          </view>
          <text class="block__hint">{{ checkInDate }} 至 {{ checkOutDate }}</text>
        </view>
        <view class="daily">
          <view v-for="day in daily" :key="day.date" class="daily__row">
            <text class="daily__date">{{ shortDate(day.date) }}</text>
            <text class="daily__week">{{ weekday(day.date) }}<text v-if="day.weekend" class="daily__weekend">周末</text></text>
            <text class="daily__price">¥{{ money(day.price) }}</text>
          </view>
        </view>
        <view class="daily__total">
          <text class="daily__total-label">{{ nights }} 晚合计</text>
          <text class="daily__total-value">¥{{ money(dailyTotal) }}</text>
        </view>
      </view>

      <view class="block">
        <view class="block__head">
          <view>
            <text class="block__kicker">PLANS</text>
            <text class="block__title">选择房价计划</text>
          </view>
        </view>
        <view
          v-for="plan in room.ratePlans"
          :key="plan.id || plan.code"
          class="plan"
          :class="{ 'plan--active': isActive(plan) }"
          @tap="selectPlan(plan)"
        >
          <view class="plan__radio">
            <view class="plan__dot" v-if="isActive(plan)"></view>
          </view>
          <view class="plan__body">
            <view class="plan__top">
              <text class="plan__name">{{ plan.name }}</text>
              <text class="plan__price">¥{{ money(plan.price) }}</text>
            </view>
            <text class="plan__desc">{{ plan.desc }}</text>
            <text class="plan__policy">{{ plan.cancelPolicyText }}</text>
          </view>
        </view>
        <text class="plan__tip">房价已含税及服务费，到店无需另付</text>
      </view>

      <view class="notice">
        <text class="notice__title">预订须知</text>
        <text class="notice__line">· 入住时间 14:00 后，离店时间 12:00 前</text>
        <text class="notice__line">· 入住需出示本人有效身份证件</text>
        <text class="notice__line">· 订单支持在线支付，支付成功后房量即为您保留</text>
      </view>

      <view class="bottom-space"></view>
    </view>

    <view class="bar">
      <view class="bar__price">
        <text class="bar__label">{{ nights }} 晚 · {{ roomCount }} 间</text>
        <view class="bar__amount">
          <text class="bar__cur">¥</text>
          <text class="bar__num">{{ money(payable) }}</text>
        </view>
      </view>
      <view class="bar__btn" :class="{ 'bar__btn--disabled': room.availableCount <= 0 }" @tap="book">
        {{ room.availableCount > 0 ? '立即预订' : '已满房' }}
      </view>
    </view>
  </view>

  <view v-else class="loading-page">
    <text class="loading-page__text">房型加载中…</text>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { fetchAvailability } from '@/api/booking'
import {
  addDays,
  dateRange,
  fmtDate,
  money,
  nightsBetween,
  weekday
} from '@/utils/format'
import { SELECTED_ROOM_KEY } from '@/utils/config'

const room = ref(null)
const current = ref(0)
const checkInDate = ref('')
const checkOutDate = ref('')
const roomCount = ref(1)
const adults = ref(2)
const selectedPlan = ref(null)

const nights = computed(() => nightsBetween(checkInDate.value, checkOutDate.value) || 1)

const daily = computed(() => {
  if (!room.value) return []
  if (room.value.dailyPrices && room.value.dailyPrices.length) return room.value.dailyPrices
  return dateRange(checkInDate.value, checkOutDate.value).map((date) => ({
    date,
    price: selectedPlan.value ? selectedPlan.value.price : room.value.price,
    weekend: false
  }))
})

const dailyTotal = computed(() => daily.value.reduce((sum, d) => sum + Number(d.price || 0), 0))

const payable = computed(() => {
  const unit = selectedPlan.value ? selectedPlan.value.price : room.value ? room.value.price : 0
  return Math.round(unit * nights.value * roomCount.value * 100) / 100
})

const slides = computed(() => {
  if (!room.value) return []
  const imgs = room.value.images || []
  const list = []
  for (let i = 0; i < 3; i++) {
    list.push({
      src: imgs[i] || '',
      variant: ((Number(room.value.roomTypeId) || 1) + i) % 4 + 1,
      title: i === 0 ? room.value.roomTypeName : i === 1 ? '客房一角' : '卫浴与备品',
      sub: i === 0 ? room.value.bedType : i === 1 ? '自然光 / 木质家具' : '独立淋浴 / 品牌洗护'
    })
  }
  return list
})

onLoad(async (options) => {
  let cached = null
  try {
    cached = uni.getStorageSync(SELECTED_ROOM_KEY)
  } catch (e) {}

  const id = options.id || (cached && cached.room && cached.room.roomTypeId)
  checkInDate.value = options.checkInDate || (cached && cached.checkInDate) || fmtDate(new Date())
  checkOutDate.value =
    options.checkOutDate || (cached && cached.checkOutDate) || fmtDate(addDays(checkInDate.value, 1))
  roomCount.value = Number(options.roomCount || (cached && cached.roomCount) || 1)
  adults.value = Number(options.adults || (cached && cached.adults) || 2)

  if (cached && cached.room && String(cached.room.roomTypeId) === String(id)) {
    room.value = cached.room
    selectedPlan.value = cached.room.ratePlans && cached.room.ratePlans[0]
  }

  try {
    const res = await fetchAvailability({
      checkInDate: checkInDate.value,
      checkOutDate: checkOutDate.value,
      adultCount: adults.value,
      roomCount: roomCount.value
    })
    const found = (res.list || []).find((r) => String(r.roomTypeId) === String(id)) || (res.list || [])[0]
    if (found) {
      room.value = found
      const keep = selectedPlan.value && found.ratePlans.find((p) => p.code === selectedPlan.value.code)
      selectedPlan.value = keep || found.ratePlans[0]
    }
  } catch (e) {
    if (!room.value) {
      uni.showToast({ title: e.message || '房型加载失败', icon: 'none' })
    }
  }

  if (room.value) {
    uni.setNavigationBarTitle({ title: room.value.roomTypeName })
  }
})

function shortDate(value) {
  const parts = String(value).split('-')
  return `${Number(parts[1])}月${Number(parts[2])}日`
}

function onSwiper(e) {
  current.value = e.detail.current
}

function isActive(plan) {
  return selectedPlan.value && String(selectedPlan.value.id) === String(plan.id)
}

function selectPlan(plan) {
  selectedPlan.value = plan
}

function book() {
  if (!room.value || room.value.availableCount <= 0) {
    uni.showToast({ title: '该日期已满房', icon: 'none' })
    return
  }
  const plan = selectedPlan.value || room.value.ratePlans[0]
  try {
    uni.setStorageSync(SELECTED_ROOM_KEY, {
      room: room.value,
      plan,
      planId: plan.id,
      checkInDate: checkInDate.value,
      checkOutDate: checkOutDate.value,
      nights: nights.value,
      adults: adults.value,
      roomCount: roomCount.value
    })
  } catch (e) {}
  uni.navigateTo({ url: '/pages/order/fill' })
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: $c-bg;
  padding-bottom: 160rpx;
}
.gallery {
  position: relative;
}
.gallery__swiper {
  height: 540rpx;
}
.gallery__count {
  position: absolute;
  right: 28rpx;
  bottom: 52rpx;
  padding: 6rpx 18rpx;
  border-radius: 100rpx;
  background: rgba(30, 26, 20, 0.42);
  color: #fffdf8;
  font-size: 20rpx;
  letter-spacing: 1rpx;
}
.sheet {
  position: relative;
  margin-top: -36rpx;
  background: $c-bg;
  border-radius: 36rpx 36rpx 0 0;
  padding: 38rpx 32rpx 0;
}
.head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}
.head__left {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-right: 24rpx;
}
.name {
  font-family: 'Songti SC', 'STSong', serif;
  font-size: 46rpx;
  color: $c-ink;
  letter-spacing: 2rpx;
}
.code {
  margin-top: 10rpx;
  font-size: 21rpx;
  color: $c-muted;
  letter-spacing: 1rpx;
}
.head__price {
  display: flex;
  align-items: baseline;
  padding-top: 12rpx;
  flex-shrink: 0;
  white-space: nowrap;
}
.head__cur {
  font-size: 22rpx;
  color: $c-gold-deep;
}
.head__num {
  font-family: 'Songti SC', 'STSong', serif;
  font-size: 52rpx;
  color: $c-gold-deep;
  margin: 0 2rpx;
}
.head__unit {
  font-size: 19rpx;
  color: $c-muted;
}
.tags {
  display: flex;
  flex-wrap: wrap;
  margin-top: 20rpx;
}
.tag {
  font-size: 21rpx;
  color: $c-ink-2;
  background: #fffdf9;
  border-radius: 6rpx;
  padding: 5rpx 16rpx;
  margin: 0 12rpx 10rpx 0;
}
.intro {
  display: block;
  margin-top: 18rpx;
  font-size: 25rpx;
  line-height: 1.75;
  color: $c-ink-2;
}

.block {
  margin-top: 44rpx;
}
.block__head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
}
.block__kicker {
  display: block;
  font-size: 18rpx;
  letter-spacing: 5rpx;
  color: $c-gold;
}
.block__title {
  display: block;
  margin-top: 4rpx;
  font-family: 'Songti SC', 'STSong', serif;
  font-size: 34rpx;
  color: $c-ink;
}
.block__hint {
  font-size: 20rpx;
  color: $c-muted;
}

.amenities {
  display: flex;
  flex-wrap: wrap;
  margin-top: 18rpx;
}
.amenity {
  width: 33.33%;
  display: flex;
  align-items: center;
  padding: 10rpx 0;
}
.amenity__dot {
  width: 8rpx;
  height: 8rpx;
  border-radius: 50%;
  background: $c-gold;
  margin-right: 12rpx;
}
.amenity__text {
  font-size: 23rpx;
  color: $c-ink-2;
}

.daily {
  margin-top: 20rpx;
  background: $c-card;
  border-radius: $radius-md;
  padding: 8rpx 26rpx;
  box-shadow: $shadow-card;
}
.daily__row {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid $c-line;
}
.daily__row:last-child {
  border-bottom: none;
}
.daily__date {
  width: 150rpx;
  font-size: 24rpx;
  color: $c-ink;
}
.daily__week {
  flex: 1;
  font-size: 21rpx;
  color: $c-muted;
}
.daily__weekend {
  margin-left: 10rpx;
  color: $c-gold-deep;
  font-size: 19rpx;
}
.daily__price {
  font-size: 26rpx;
  color: $c-ink;
}
.daily__total {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16rpx;
  padding: 0 6rpx;
}
.daily__total-label {
  font-size: 22rpx;
  color: $c-muted;
}
.daily__total-value {
  font-family: 'Songti SC', 'STSong', serif;
  font-size: 32rpx;
  color: $c-ink;
}

.plan {
  display: flex;
  align-items: flex-start;
  background: $c-card;
  border: 1rpx solid transparent;
  border-radius: $radius-md;
  padding: 24rpx 24rpx;
  margin-top: 18rpx;
  box-shadow: $shadow-card;
}
.plan--active {
  border-color: $c-gold;
  background: #fffbf3;
}
.plan__radio {
  width: 34rpx;
  height: 34rpx;
  border-radius: 50%;
  border: 1rpx solid #cfc2ad;
  margin: 6rpx 20rpx 0 0;
  display: flex;
  align-items: center;
  justify-content: center;
}
.plan--active .plan__radio {
  border-color: $c-gold-deep;
}
.plan__dot {
  width: 18rpx;
  height: 18rpx;
  border-radius: 50%;
  background: $c-gold-deep;
}
.plan__body {
  flex: 1;
}
.plan__top {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}
.plan__name {
  font-size: 27rpx;
  color: $c-ink;
}
.plan__price {
  font-size: 28rpx;
  color: $c-gold-deep;
  font-family: 'Songti SC', 'STSong', serif;
}
.plan__desc {
  display: block;
  margin-top: 6rpx;
  font-size: 21rpx;
  color: $c-muted;
}
.plan__policy {
  display: block;
  margin-top: 4rpx;
  font-size: 20rpx;
  color: #9aa88f;
}
.plan__tip {
  display: block;
  margin-top: 18rpx;
  font-size: 20rpx;
  color: $c-muted;
}

.notice {
  margin-top: 44rpx;
  padding: 26rpx 28rpx;
  background: #f1e9db;
  border-radius: $radius-md;
  display: flex;
  flex-direction: column;
}
.notice__title {
  font-size: 23rpx;
  color: $c-ink-2;
  margin-bottom: 10rpx;
}
.notice__line {
  font-size: 21rpx;
  color: $c-muted;
  line-height: 1.8;
}
.bottom-space {
  height: 40rpx;
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
  width: 300rpx;
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
.bar__btn--disabled {
  background: #b8ae9d;
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
