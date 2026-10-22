<template>
  <view class="h-cover" :style="wrapStyle">
    <image
      v-if="src && !broken"
      class="h-cover__img"
      :src="src"
      mode="aspectFill"
      @error="broken = true"
    />
    <view class="h-cover__texture"></view>
    <text class="h-cover__mark" :style="{ fontSize: markSize }">{{ mark }}</text>
    <view v-if="title || sub" class="h-cover__meta">
      <text v-if="title" class="h-cover__title">{{ title }}</text>
      <text v-if="sub" class="h-cover__sub">{{ sub }}</text>
    </view>
    <view v-if="badge" class="h-cover__badge">{{ badge }}</view>
    <slot />
  </view>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  src: { type: String, default: '' },
  title: { type: String, default: '' },
  sub: { type: String, default: '' },
  mark: { type: String, default: '' },
  badge: { type: String, default: '' },
  variant: { type: [Number, String], default: 1 },
  height: { type: String, default: '360rpx' },
  radius: { type: String, default: '24rpx' },
  markSize: { type: String, default: '220rpx' }
})

const broken = ref(false)
watch(
  () => props.src,
  () => {
    broken.value = false
  }
)

const palette = {
  1: ['#cdd6cb', '#8a9f8f'],
  2: ['#eddcc4', '#b0865a'],
  3: ['#cfd5db', '#7c8b96'],
  4: ['#ddcdbb', '#7a6350']
}

const wrapStyle = computed(() => {
  const p = palette[props.variant] || palette[1]
  return {
    height: props.height,
    borderRadius: props.radius,
    background: `linear-gradient(140deg, ${p[0]} 0%, ${p[1]} 100%)`
  }
})

const mark = computed(() => (props.mark || props.title || '栖').slice(0, 1))
</script>

<style lang="scss" scoped>
.h-cover {
  position: relative;
  overflow: hidden;
  width: 100%;
  display: flex;
  align-items: flex-end;
}
.h-cover__img {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
}
.h-cover__texture {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background: repeating-linear-gradient(
      115deg,
      rgba(255, 255, 255, 0.08) 0,
      rgba(255, 255, 255, 0.08) 1px,
      transparent 1px,
      transparent 14rpx
    ),
    radial-gradient(120% 80% at 78% 10%, rgba(255, 255, 255, 0.42) 0%, transparent 60%);
}
.h-cover__mark {
  position: absolute;
  right: 28rpx;
  top: -34rpx;
  font-family: 'Songti SC', 'STSong', 'Noto Serif SC', serif;
  font-size: 220rpx;
  line-height: 1;
  color: rgba(255, 255, 255, 0.24);
  letter-spacing: 4rpx;
}
.h-cover__meta {
  position: relative;
  padding: 26rpx 30rpx;
  display: flex;
  flex-direction: column;
}
.h-cover__title {
  font-family: 'Songti SC', 'STSong', 'Noto Serif SC', serif;
  font-size: 40rpx;
  color: #fffdf8;
  letter-spacing: 2rpx;
  text-shadow: 0 2rpx 12rpx rgba(40, 30, 18, 0.24);
}
.h-cover__sub {
  margin-top: 6rpx;
  font-size: 22rpx;
  letter-spacing: 3rpx;
  color: rgba(255, 253, 248, 0.82);
}
.h-cover__badge {
  position: absolute;
  left: 22rpx;
  top: 22rpx;
  padding: 6rpx 16rpx;
  border-radius: 100rpx;
  background: rgba(255, 253, 248, 0.9);
  color: #8c6743;
  font-size: 21rpx;
  letter-spacing: 1rpx;
}
</style>
