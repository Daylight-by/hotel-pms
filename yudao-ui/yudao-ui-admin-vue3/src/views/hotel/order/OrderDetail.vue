<template>
  <Dialog v-model="dialogVisible" title="订单详情" width="900">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="订单信息" name="info">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ data.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="tagTypeOf(ORDER_STATUS, data.status)">
              {{ data.statusName || labelOf(ORDER_STATUS, data.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="订单类型">
            <el-tag effect="plain" :type="data.orderType === 20 ? 'warning' : 'info'">
              {{ data.orderTypeName || (data.orderType === 20 ? '钟点房' : '全日房') }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="所属营业日">
            {{ data.businessDate || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="住客">{{ data.guestName }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ data.guestMobile || '-' }}</el-descriptions-item>
          <el-descriptions-item label="房间号">{{ data.roomNo }}</el-descriptions-item>
          <el-descriptions-item label="房型">{{ data.roomTypeName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="入住日期">{{ data.checkInDate }}</el-descriptions-item>
          <el-descriptions-item label="离店日期">
            {{ data.checkOutDate }}（{{ data.nights }} 晚）
          </el-descriptions-item>
          <el-descriptions-item label="入住 / 离店时间">
            {{ data.checkInTime ? formatDate(data.checkInTime) : '-' }} ~
            {{ data.checkOutTime ? formatDate(data.checkOutTime) : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="人数">
            {{ data.adultCount ?? 1 }} 成人 / {{ data.childCount ?? 0 }} 儿童
          </el-descriptions-item>
          <el-descriptions-item label="订单金额">
            ¥ {{ formatMoney(data.amount) }}
          </el-descriptions-item>
          <el-descriptions-item label="押金">
            ¥ {{ formatMoney(data.deposit) }}
          </el-descriptions-item>
          <el-descriptions-item label="已收金额">
            ¥ {{ formatMoney(data.paidAmount) }}
          </el-descriptions-item>
          <el-descriptions-item label="支付状态">
            <el-tag :type="tagTypeOf(PAYMENT_STATUS, data.paymentStatus)" effect="plain">
              {{ data.paymentStatusName || labelOf(PAYMENT_STATUS, data.paymentStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="支付方式">
            {{ data.paymentMethodName || labelOf(PAYMENT_METHOD, data.paymentMethod) }}
          </el-descriptions-item>
          <el-descriptions-item label="需要发票">
            {{ data.invoiceRequested ? '是' : '否' }}
          </el-descriptions-item>
          <el-descriptions-item label="发票号">{{ data.invoiceNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="实际入住">
            {{ data.actualCheckInTime ? formatDate(data.actualCheckInTime) : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="实际离店">
            {{ data.actualCheckOutTime ? formatDate(data.actualCheckOutTime) : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ data.createTime ? formatDate(data.createTime) : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="备注">{{ data.remark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <template v-if="(detail?.roomChanges || []).length">
          <div class="mt-14px mb-6px font-bold">换房记录</div>
          <el-table :data="detail?.roomChanges" size="small" border>
            <el-table-column label="时间" prop="changeTime" width="180" :formatter="dateFormatter" />
            <el-table-column label="原房间" prop="fromRoomNo" width="100" />
            <el-table-column label="新房间" prop="toRoomNo" width="100" />
            <el-table-column label="原因" prop="reason" />
          </el-table>
        </template>
      </el-tab-pane>

      <el-tab-pane label="账务账页" name="folio" lazy>
        <OrderFolio v-if="data.id" :order-id="data.id" :guest-id="data.guestId" :room-id="data.roomId" />
      </el-tab-pane>
    </el-tabs>
    <template #footer>
      <el-button @click="dialogVisible = false">关 闭</el-button>
    </template>
  </Dialog>
</template>
<script lang="ts" setup>
import { dateFormatter, formatDate } from '@/utils/formatTime'
import {
  ORDER_STATUS,
  PAYMENT_METHOD,
  PAYMENT_STATUS,
  formatMoney,
  labelOf,
  tagTypeOf
} from '@/utils/hotel'
import * as OrderApi from '@/api/hotel/order'
import OrderFolio from './OrderFolio.vue'

defineOptions({ name: 'HotelOrderDetail' })

const dialogVisible = ref(false)
const activeTab = ref('info')
const data = ref<OrderApi.OrderVO>({})
const detail = ref<any>(null)

const open = async (row: OrderApi.OrderVO) => {
  data.value = row
  detail.value = null
  activeTab.value = 'info'
  dialogVisible.value = true
  if (row.id) {
    try {
      detail.value = await OrderApi.getOrderDetail(row.id)
      if (detail.value?.order) {
        data.value = { ...row, ...detail.value.order }
      }
    } catch {}
  }
}
defineExpose({ open })
</script>
