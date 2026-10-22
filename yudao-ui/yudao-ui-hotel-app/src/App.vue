<script>
export default {
  onLaunch() {
    // 记录一次启动，便于调试（不涉及隐私）
    try {
      uni.setStorageSync('hotel_app_last_launch', Date.now())
    } catch (e) {}
  }
}
</script>

<style lang="scss">
page {
  background-color: #f6f1e8;
  color: #211d17;
  font-family: -apple-system, BlinkMacSystemFont, 'Helvetica Neue', 'PingFang SC',
    'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  font-size: 28rpx;
  line-height: 1.6;
  -webkit-font-smoothing: antialiased;
}
view,
text,
image,
button,
input,
textarea {
  box-sizing: border-box;
}
button::after {
  border: none;
}

/* #ifdef H5 */
/* 桌面浏览器里把 H5 收成一列手机画布，投屏演示更接近真机比例 */
body {
  background: #e6ddcd;
}
uni-app {
  display: block;
  max-width: 430px;
  margin: 0 auto;
  min-height: 100vh;
  background: #f6f1e8;
  box-shadow: 0 0 120rpx rgba(60, 46, 28, 0.18);
}
@media (min-width: 500px) {
  .bar {
    left: 50% !important;
    right: auto !important;
    width: 430px;
    transform: translateX(-50%);
  }
  .sheet,
  .mask {
    left: 50% !important;
    right: auto !important;
    width: 430px;
    transform: translateX(-50%);
  }
}
/* #endif */
</style>
