<template>
  <div>
    <el-card shadow="never">
      <el-row :gutter="16" justify="space-between">
        <el-col :xl="12" :lg="12" :md="12" :sm="24" :xs="24">
          <div class="flex items-center">
            <el-avatar :src="avatar" :size="70" class="mr-16px">
              <img src="@/assets/imgs/avatar.gif" alt="" />
            </el-avatar>
            <div>
              <div class="text-20px">{{ greeting }}，{{ username }}</div>
              <div class="mt-10px text-14px text-gray-500">
                {{ todayText }} · 欢迎使用酒店管理系统
              </div>
            </div>
          </div>
        </el-col>
        <el-col :xl="12" :lg="12" :md="12" :sm="24" :xs="24">
          <div class="h-70px flex items-center justify-end lt-sm:mt-10px">
            <el-button type="primary" @click="go('/hotel/board')">
              <Icon icon="ep:grid" class="mr-4px" />进入房态看板
            </el-button>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-row class="mt-8px" :gutter="8">
      <el-col
        v-for="item in entries"
        :key="item.url"
        :xl="8"
        :lg="12"
        :md="12"
        :sm="24"
        :xs="24"
        class="mb-8px"
      >
        <el-card shadow="never" class="entry-card cursor-pointer" @click="go(item.url)">
          <div class="flex items-center">
            <Icon :icon="item.icon" class="text-28px" :style="{ color: item.color }" />
            <div class="ml-16px">
              <div class="text-16px">{{ item.name }}</div>
              <div class="mt-4px text-12px text-gray-400">{{ item.desc }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/modules/user'

defineOptions({ name: 'Home' })

const router = useRouter()
const userStore = useUserStore()

const avatar = userStore.getUser.avatar
const username = computed(() => userStore.getUser.nickname || '管理员')

const now = ref(new Date())

const greeting = computed(() => {
  const h = now.value.getHours()
  if (h < 6) return '凌晨好'
  if (h < 11) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const todayText = computed(() => {
  const d = now.value
  const week = ['日', '一', '二', '三', '四', '五', '六'][d.getDay()]
  return `${d.getFullYear()} 年 ${d.getMonth() + 1} 月 ${d.getDate()} 日 星期${week}`
})

const entries = [
  { name: '房态看板', desc: '实时房态与营业日', url: '/hotel/board', icon: 'ep:grid', color: '#1fdaca' },
  { name: '订单管理', desc: '预订、入住、续住换房', url: '/hotel/order', icon: 'ep:tickets', color: '#378add' },
  { name: '账务账页', desc: '挂账、收款、结账', url: '/hotel/folio', icon: 'ep:wallet', color: '#ba7517' },
  { name: '智能定价', desc: '每日建议价与回测', url: '/hotel-revenue/pricing', icon: 'ep:trend-charts', color: '#d85a30' },
  { name: '夜审管理', desc: '过账与营业日推进', url: '/hotel-finance/audit', icon: 'ep:moon', color: '#534ab7' },
  { name: '经营统计', desc: 'OCC / ADR / RevPAR', url: '/hotel-report/statistics', icon: 'ep:data-line', color: '#e24b4a' }
]

const go = (url: string) => {
  router.push(url)
}
</script>

<style scoped lang="scss">
.entry-card {
  &:hover {
    border-color: var(--el-color-primary);
  }
}
</style>
