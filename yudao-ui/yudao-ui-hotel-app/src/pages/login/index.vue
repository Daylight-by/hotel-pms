<template>
  <view class="page">
    <view class="brand">
      <text class="brand__kicker">QIYU HOTEL</text>
      <text class="brand__title">栖屿</text>
      <text class="brand__sub">会员登录 · 预订与入住，一处打理</text>
    </view>

    <view v-if="!member" class="panel">
      <view class="panel__head">
        <text class="panel__title">手机号登录</text>
        <text class="panel__hint">未注册手机号将自动创建会员</text>
      </view>

      <view class="field">
        <text class="field__prefix">+86</text>
        <input
          v-model="mobile"
          class="field__input"
          type="number"
          maxlength="11"
          placeholder="请输入手机号"
          placeholder-class="ph"
        />
      </view>

      <view class="field">
        <input
          v-model="code"
          class="field__input"
          type="number"
          maxlength="6"
          placeholder="短信验证码"
          placeholder-class="ph"
        />
        <view
          class="code-btn"
          :class="{ 'code-btn--disabled': counting || sending }"
          @tap="sendCode"
        >
          {{ counting ? countdown + 's 后重发' : sending ? '发送中…' : '获取验证码' }}
        </view>
      </view>

      <view class="agree" @tap="agreed = !agreed">
        <view class="agree__box" :class="{ 'agree__box--on': agreed }">
          <text v-if="agreed" class="agree__tick">✓</text>
        </view>
        <text class="agree__text">我已阅读并同意《会员服务协议》与《隐私政策》</text>
      </view>

      <view class="submit" :class="{ 'submit--loading': loading }" @tap="doLogin">
        {{ loading ? '登录中…' : '登录 / 注册' }}
      </view>

      <view class="demo-tip">
        <text class="demo-tip__title">演示环境</text>
        <text class="demo-tip__line">验证码固定为 123456，点击「获取验证码」会自动填入</text>
      </view>
    </view>

    <view v-else class="profile">
      <view class="profile__card">
        <view class="profile__avatar">{{ (member.nickname || '会').slice(0, 1) }}</view>
        <view class="profile__body">
          <text class="profile__name">{{ member.nickname || '栖屿会员' }}</text>
          <text class="profile__meta">{{ member.level || '会员' }} · {{ member.memberNo }}</text>
          <text class="profile__mobile">{{ maskMobile(member.mobile) }}</text>
        </view>
        <view class="profile__points">
          <text class="profile__points-num">{{ member.points || 0 }}</text>
          <text class="profile__points-label">积分</text>
        </view>
      </view>

      <view class="profile__actions">
        <view class="profile__btn profile__btn--primary" @tap="goOrders">我的订单</view>
        <view class="profile__btn" @tap="logout">退出登录</view>
      </view>

      <text class="profile__tip">积分按实付金额 1 元 = 1 分累计，可在下次入住抵扣</text>
    </view>
  </view>
</template>

<script setup>
import { computed, onUnmounted, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { fetchMemberInfo, loginByMobile } from '@/api/member'
import { clearSession, loadSession, saveSession, session } from '@/store/session'

const mobile = ref('')
const code = ref('')
const agreed = ref(true)
const loading = ref(false)
const sending = ref(false)
const counting = ref(false)
const countdown = ref(60)
const redirect = ref('')
const member = computed(() => session.member)
let timer = null

onLoad((options) => {
  loadSession()
  redirect.value = options.redirect ? decodeURIComponent(options.redirect) : ''
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

function maskMobile(value) {
  if (!value) return ''
  return String(value).replace(/^(\d{3})\d{4}(\d{4})$/, '$1****$2')
}

function sendCode() {
  if (counting.value || sending.value) return
  if (!/^1\d{10}$/.test(mobile.value)) {
    uni.showToast({ title: '请先填写手机号', icon: 'none' })
    return
  }
  sending.value = true
  setTimeout(() => {
    sending.value = false
    code.value = '123456'
    uni.showToast({ title: '演示验证码 123456 已填入', icon: 'none' })
    counting.value = true
    countdown.value = 60
    timer = setInterval(() => {
      countdown.value -= 1
      if (countdown.value <= 0) {
        clearInterval(timer)
        counting.value = false
      }
    }, 1000)
  }, 600)
}

async function doLogin() {
  if (loading.value) return
  if (!/^1\d{10}$/.test(mobile.value)) {
    uni.showToast({ title: '请填写正确的手机号', icon: 'none' })
    return
  }
  if (!/^\d{6}$/.test(code.value)) {
    uni.showToast({ title: '请填写 6 位验证码', icon: 'none' })
    return
  }
  if (!agreed.value) {
    uni.showToast({ title: '请先同意会员服务协议', icon: 'none' })
    return
  }
  loading.value = true
  try {
    const res = await loginByMobile(mobile.value, code.value)
    const token = res.accessToken || res.token
    const info = res.member || res.user || (await fetchMemberInfo())
    saveSession(token, info || { mobile: mobile.value, nickname: '栖屿会员' })
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => {
      if (redirect.value) uni.redirectTo({ url: redirect.value })
      else uni.reLaunch({ url: '/pages/index/index' })
    }, 600)
  } catch (e) {
    uni.showToast({ title: e.message || '登录失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function goOrders() {
  uni.redirectTo({ url: '/pages/order/list' })
}

function logout() {
  uni.showModal({
    title: '退出登录',
    content: '确定退出当前会员账号吗？',
    success: (res) => {
      if (!res.confirm) return
      clearSession()
      uni.showToast({ title: '已退出', icon: 'none' })
    }
  })
}
</script>

<style lang="scss" scoped>
.page {
  min-height: 100vh;
  background: $c-bg;
  padding: 0 44rpx;
}
.brand {
  padding: 90rpx 0 60rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.brand__kicker {
  font-size: 19rpx;
  letter-spacing: 8rpx;
  color: $c-gold;
}
.brand__title {
  margin-top: 16rpx;
  font-family: 'Songti SC', 'STSong', serif;
  font-size: 84rpx;
  letter-spacing: 10rpx;
  color: $c-ink;
}
.brand__sub {
  margin-top: 14rpx;
  font-size: 22rpx;
  color: $c-muted;
  letter-spacing: 1rpx;
}
.panel {
  background: $c-card;
  border-radius: $radius-lg;
  padding: 36rpx 32rpx 40rpx;
  box-shadow: $shadow-card;
}
.panel__head {
  margin-bottom: 20rpx;
}
.panel__title {
  display: block;
  font-family: 'Songti SC', 'STSong', serif;
  font-size: 34rpx;
  color: $c-ink;
}
.panel__hint {
  display: block;
  margin-top: 6rpx;
  font-size: 20rpx;
  color: $c-muted;
}
.field {
  display: flex;
  align-items: center;
  height: 100rpx;
  border-bottom: 1rpx solid $c-line;
}
.field__prefix {
  width: 90rpx;
  font-size: 26rpx;
  color: $c-ink;
}
.field__input {
  flex: 1;
  font-size: 27rpx;
  color: $c-ink;
  height: 60rpx;
}
.ph {
  color: #b7ac9b;
  font-size: 25rpx;
}
.code-btn {
  font-size: 22rpx;
  color: $c-gold-deep;
  padding: 10rpx 0 10rpx 20rpx;
}
.code-btn--disabled {
  color: #b9ae9b;
}
.agree {
  display: flex;
  align-items: flex-start;
  margin-top: 28rpx;
}
.agree__box {
  width: 30rpx;
  height: 30rpx;
  border-radius: 6rpx;
  border: 1rpx solid #cfc2ad;
  margin: 4rpx 12rpx 0 0;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.agree__box--on {
  background: $c-ink;
  border-color: $c-ink;
}
.agree__tick {
  color: #f7f1e6;
  font-size: 20rpx;
  line-height: 1;
}
.agree__text {
  flex: 1;
  font-size: 20rpx;
  color: $c-muted;
  line-height: 1.7;
}
.submit {
  margin-top: 34rpx;
  height: 96rpx;
  border-radius: 100rpx;
  background: $c-ink;
  color: #f7f1e6;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 29rpx;
  letter-spacing: 6rpx;
}
.submit--loading {
  background: #6f6656;
}
.demo-tip {
  margin-top: 30rpx;
  padding: 20rpx 24rpx;
  border-radius: $radius-sm;
  background: #f4ecdf;
  display: flex;
  flex-direction: column;
}
.demo-tip__title {
  font-size: 20rpx;
  color: $c-gold-deep;
  letter-spacing: 2rpx;
}
.demo-tip__line {
  margin-top: 6rpx;
  font-size: 20rpx;
  color: $c-muted;
  line-height: 1.6;
}

.profile__card {
  background: $c-card;
  border-radius: $radius-lg;
  padding: 34rpx 30rpx;
  box-shadow: $shadow-card;
  display: flex;
  align-items: center;
}
.profile__avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: linear-gradient(140deg, #d9c7ae, #a98a64);
  color: #fffdf8;
  font-family: 'Songti SC', 'STSong', serif;
  font-size: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 22rpx;
}
.profile__body {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.profile__name {
  font-family: 'Songti SC', 'STSong', serif;
  font-size: 32rpx;
  color: $c-ink;
}
.profile__meta {
  margin-top: 4rpx;
  font-size: 20rpx;
  color: $c-gold-deep;
}
.profile__mobile {
  margin-top: 2rpx;
  font-size: 20rpx;
  color: $c-muted;
}
.profile__points {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.profile__points-num {
  font-family: 'Songti SC', 'STSong', serif;
  font-size: 38rpx;
  color: $c-ink;
}
.profile__points-label {
  font-size: 19rpx;
  color: $c-muted;
}
.profile__actions {
  margin-top: 36rpx;
}
.profile__btn {
  height: 92rpx;
  border-radius: 100rpx;
  border: 1rpx solid $c-line;
  background: $c-card;
  color: $c-ink-2;
  font-size: 26rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20rpx;
}
.profile__btn--primary {
  background: $c-ink;
  color: #f7f1e6;
  border-color: $c-ink;
}
.profile__tip {
  display: block;
  margin-top: 10rpx;
  font-size: 20rpx;
  color: $c-muted;
  text-align: center;
  line-height: 1.7;
}
</style>
