<template>
  <view class="h-empty">
    <view class="h-empty__ring"></view>
    <text class="h-empty__title">{{ title }}</text>
    <text v-if="desc" class="h-empty__desc">{{ desc }}</text>
    <slot />
  </view>
</template>

<script setup>
defineProps({
  title: { type: String, default: '暂无内容' },
  desc: { type: String, default: '' }
})
</script>

<style lang="scss" scoped>
.h-empty {
  padding: 90rpx 40rpx 60rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.h-empty__ring {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  border: 2rpx solid #d9cbb6;
  position: relative;
  margin-bottom: 26rpx;
}
.h-empty__ring::after {
  content: '';
  position: absolute;
  left: 24rpx;
  top: 24rpx;
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  background: #ecdfcc;
}
.h-empty__title {
  font-family: 'Songti SC', 'STSong', serif;
  font-size: 30rpx;
  color: #4a443a;
}
.h-empty__desc {
  margin-top: 10rpx;
  font-size: 24rpx;
  color: #8b8173;
  text-align: center;
}
</style>
