<template>
  <el-select
    v-model="currentStoreId"
    class="store-switch mr-3"
    size="default"
    :teleported="true"
    @change="handleChange"
  >
    <template #prefix>
      <Icon icon="ep:office-building" color="var(--top-header-text-color)" />
    </template>
    <el-option :value="0" label="集团视角（全部门店）" />
    <el-option v-for="store in stores" :key="store.id" :value="store.id!" :label="store.name" />
  </el-select>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getStoreList, StoreVO } from '@/api/hotel/store'
import { getStoreId, setStoreId } from '@/utils/store'

defineOptions({ name: 'StoreSwitch' })

const stores = ref<StoreVO[]>([])
const currentStoreId = ref<number>(Number(getStoreId() || 0))

const loadStores = async () => {
  try {
    stores.value = (await getStoreList()) || []
  } catch (e) {
    stores.value = []
  }
}

const handleChange = (value: number) => {
  setStoreId(value)
  // 切换门店后所有列表/看板都按新门店重查，直接刷新最稳妥
  window.location.reload()
}

onMounted(loadStores)
</script>

<style lang="scss" scoped>
.store-switch {
  width: 210px;
}
</style>
