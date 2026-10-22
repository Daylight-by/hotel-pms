<template>
  <div v-loading="loading">
    <el-empty v-if="!loading && !folio" description="该订单还没有账页">
      <el-button type="primary" @click="handleCreate" v-hasPermi="['hotel:folio:create']">
        开主账页
      </el-button>
    </el-empty>
    <FolioPanel v-else-if="folio" :folio="folio" @refresh="load" />
  </div>
</template>

<script lang="ts" setup>
import * as FolioApi from '@/api/hotel/folio'
import FolioPanel from '@/views/hotel/folio/FolioPanel.vue'

defineOptions({ name: 'HotelOrderFolio' })

const props = defineProps<{ orderId?: number; guestId?: number; roomId?: number }>()
const message = useMessage()

const loading = ref(false)
const folio = ref<FolioApi.FolioVO | null>(null)

const load = async () => {
  if (!props.orderId) {
    folio.value = null
    return
  }
  loading.value = true
  try {
    folio.value = await FolioApi.getFolioByOrder(props.orderId)
  } catch {
    folio.value = null
  } finally {
    loading.value = false
  }
}

const handleCreate = async () => {
  if (!props.orderId || !props.guestId) return
  await FolioApi.createFolio({
    orderId: props.orderId,
    guestId: props.guestId,
    roomId: props.roomId,
    type: 10,
    remark: '手工开账'
  })
  message.success('账页已创建')
  await load()
}

watch(
  () => props.orderId,
  () => load(),
  { immediate: true }
)
</script>
