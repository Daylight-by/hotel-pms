<template>
  <ContentWrap>
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="76px"
    >
      <el-form-item label="订单号" prop="orderNo">
        <el-input
          v-model="queryParams.orderNo"
          placeholder="请输入订单号"
          clearable
          class="!w-200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="订单状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable class="!w-150px">
          <el-option
            v-for="item in ORDER_STATUS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="支付状态" prop="paymentStatus">
        <el-select v-model="queryParams.paymentStatus" placeholder="请选择" clearable class="!w-150px">
          <el-option
            v-for="item in PAYMENT_STATUS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="订单类型" prop="orderType">
        <el-select v-model="queryParams.orderType" placeholder="全部" clearable class="!w-130px">
          <el-option label="全日房" :value="10" />
          <el-option label="钟点房" :value="20" />
        </el-select>
      </el-form-item>
      <el-form-item label="住客" prop="guestId">
        <el-select
          v-model="queryParams.guestId"
          placeholder="请选择住客"
          clearable
          filterable
          class="!w-180px"
        >
          <el-option
            v-for="item in guestList"
            :key="item.id"
            :label="item.name + (item.mobile ? ' · ' + item.mobile : '')"
            :value="item.id!"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="日期区间">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          value-format="YYYY-MM-DD"
          start-placeholder="入住起"
          end-placeholder="离店止"
          class="!w-260px"
          @change="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['hotel:order:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新建预订
        </el-button>
        <el-button
          type="warning"
          plain
          @click="hourlyRef.open()"
          v-hasPermi="['hotel:order:create']"
        >
          <Icon icon="ep:timer" class="mr-5px" /> 钟点房
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="订单号" align="center" prop="orderNo" width="190" />
      <el-table-column label="类型" align="center" width="90">
        <template #default="scope">
          <el-tag size="small" effect="plain" :type="scope.row.orderType === 20 ? 'warning' : 'info'">
            {{ scope.row.orderType === 20 ? '钟点房' : '全日房' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="营业日" align="center" prop="businessDate" width="110" />
      <el-table-column label="住客" align="center" min-width="150">
        <template #default="scope">
          <div>{{ scope.row.guestName }}</div>
          <div class="text-12px text-gray-500">{{ scope.row.guestMobile }}</div>
        </template>
      </el-table-column>
      <el-table-column label="房间" align="center" width="130">
        <template #default="scope">
          <div>{{ scope.row.roomNo }}</div>
          <div class="text-12px text-gray-500">{{ scope.row.roomTypeName }}</div>
        </template>
      </el-table-column>
      <el-table-column label="入住 / 离店" align="center" width="220">
        <template #default="scope">
          {{ scope.row.checkInDate }} ~ {{ scope.row.checkOutDate }}
          <span class="text-gray-500">（{{ scope.row.nights }}晚）</span>
        </template>
      </el-table-column>
      <el-table-column label="订单金额" align="right" prop="amount" width="120">
        <template #default="scope">¥ {{ formatMoney(scope.row.amount) }}</template>
      </el-table-column>
      <el-table-column label="已收" align="center" prop="paidAmount" width="120">
        <template #default="scope">¥ {{ formatMoney(scope.row.paidAmount) }}</template>
      </el-table-column>
      <el-table-column label="订单状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag :type="tagTypeOf(ORDER_STATUS, scope.row.status)">
            {{ scope.row.statusName || labelOf(ORDER_STATUS, scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="支付状态" align="center" prop="paymentStatus" width="100">
        <template #default="scope">
          <el-tag :type="tagTypeOf(PAYMENT_STATUS, scope.row.paymentStatus)" effect="plain">
            {{ scope.row.paymentStatusName || labelOf(PAYMENT_STATUS, scope.row.paymentStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="支付方式" align="center" width="100">
        <template #default="scope">
          {{ scope.row.paymentMethodName || labelOf(PAYMENT_METHOD, scope.row.paymentMethod) }}
        </template>
      </el-table-column>
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        width="180"
        :formatter="dateFormatter"
      />
      <el-table-column label="操作" align="center" width="300" fixed="right">
        <template #default="scope">
          <el-button
            v-if="scope.row.status === 10"
            link
            type="primary"
            @click="handleConfirm(scope.row)"
            v-hasPermi="['hotel:order:update']"
          >
            确认
          </el-button>
          <el-button
            v-if="[10, 20].includes(scope.row.status)"
            link
            type="success"
            @click="handleCheckIn(scope.row)"
            v-hasPermi="['hotel:order:update']"
          >
            入住
          </el-button>
          <el-button
            v-if="scope.row.status === 30"
            link
            type="warning"
            @click="handleCheckOut(scope.row)"
            v-hasPermi="['hotel:order:update']"
          >
            退房
          </el-button>
          <el-button link type="primary" @click="openDetail(scope.row)">详情</el-button>
          <el-button link type="primary" @click="openPayment(scope.row, 'payment')" v-hasPermi="['hotel:order:update']">
            收款
          </el-button>
          <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, scope.row)">
            <el-button link type="primary">
              更多<Icon icon="ep:arrow-down" class="ml-2px" />
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-if="scope.row.status === 30 && scope.row.orderType !== 20"
                  command="extend"
                  v-hasPermi="['hotel:order:update']"
                >
                  续住
                </el-dropdown-item>
                <el-dropdown-item
                  v-if="[20, 30].includes(scope.row.status)"
                  command="changeRoom"
                  v-hasPermi="['hotel:order:update']"
                >
                  换房
                </el-dropdown-item>
                <el-dropdown-item
                  v-if="scope.row.status === 30"
                  command="earlyCheckout"
                  v-hasPermi="['hotel:order:update']"
                >
                  提前退房
                </el-dropdown-item>
                <el-dropdown-item
                  v-if="[20, 30, 40].includes(scope.row.status)"
                  command="refund"
                  v-hasPermi="['hotel:order:update']"
                >
                  退款
                </el-dropdown-item>
                <el-dropdown-item
                  v-if="[10, 20].includes(scope.row.status)"
                  command="edit"
                  v-hasPermi="['hotel:order:update']"
                >
                  编辑
                </el-dropdown-item>
                <el-dropdown-item
                  v-if="[10, 20].includes(scope.row.status)"
                  command="cancel"
                  v-hasPermi="['hotel:order:update']"
                >
                  取消订单
                </el-dropdown-item>
                <el-dropdown-item
                  v-if="scope.row.status === 50"
                  command="delete"
                  divided
                  v-hasPermi="['hotel:order:delete']"
                >
                  删除
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <OrderForm ref="formRef" :guest-list="guestList" :room-list="roomList" @success="getList" />
  <OrderPaymentForm ref="paymentRef" @success="getList" />
  <OrderDetail ref="detailRef" />
  <OrderActionForm ref="actionRef" @success="getList" />
  <OrderHourlyForm ref="hourlyRef" @success="getList" />
</template>
<script lang="ts" setup>
import { useRoute } from 'vue-router'
import { dateFormatter } from '@/utils/formatTime'
import { ORDER_STATUS, PAYMENT_METHOD, PAYMENT_STATUS, formatMoney, labelOf, tagTypeOf } from '@/utils/hotel'
import * as OrderApi from '@/api/hotel/order'
import * as GuestApi from '@/api/hotel/guest'
import * as RoomApi from '@/api/hotel/room'
import OrderForm from './OrderForm.vue'
import OrderPaymentForm from './OrderPaymentForm.vue'
import OrderDetail from './OrderDetail.vue'
import OrderActionForm from './OrderActionForm.vue'
import OrderHourlyForm from './OrderHourlyForm.vue'

defineOptions({ name: 'HotelOrder' })

const message = useMessage()
const route = useRoute()

const loading = ref(true)
const total = ref(0)
const list = ref<OrderApi.OrderVO[]>([])
const guestList = ref<GuestApi.GuestVO[]>([])
const roomList = ref<RoomApi.RoomVO[]>([])
const dateRange = ref<[string, string] | []>([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  orderNo: '',
  guestId: undefined,
  status: undefined,
  paymentStatus: undefined,
  orderType: undefined as number | undefined,
  checkInDateStart: undefined,
  checkOutDateEnd: undefined
})
const queryFormRef = ref()

const getList = async () => {
  loading.value = true
  try {
    const data = await OrderApi.getOrderPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const getOptions = async () => {
  const [guests, rooms] = await Promise.all([
    GuestApi.getGuestSimpleList(),
    RoomApi.getRoomSimpleList()
  ])
  guestList.value = guests
  roomList.value = rooms
}

const handleQuery = () => {
  queryParams.pageNo = 1
  if (dateRange.value?.length === 2) {
    queryParams.checkInDateStart = dateRange.value[0]
    queryParams.checkOutDateEnd = dateRange.value[1]
  } else {
    queryParams.checkInDateStart = undefined
    queryParams.checkOutDateEnd = undefined
  }
  getList()
}

const resetQuery = () => {
  queryFormRef.value.resetFields()
  dateRange.value = []
  handleQuery()
}

const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

const paymentRef = ref()
const openPayment = (row: OrderApi.OrderVO, mode: 'payment' | 'refund') => {
  paymentRef.value.open(row, mode)
}

const detailRef = ref()
const openDetail = (row: OrderApi.OrderVO) => {
  detailRef.value.open(row)
}

/** 确认订单 */
const handleConfirm = async (row: OrderApi.OrderVO) => {
  try {
    await message.confirm(`确认订单 ${row.orderNo}？`)
    await OrderApi.confirmOrder(row.id!)
    message.success('订单已确认')
    await getList()
  } catch {}
}

/** 办理入住 */
const handleCheckIn = async (row: OrderApi.OrderVO) => {
  try {
    await message.confirm(`为 ${row.guestName} 办理 ${row.roomNo} 入住？`)
    await OrderApi.checkInOrder(row.id!)
    message.success('入住办理成功')
    await getList()
  } catch {}
}

/** 办理退房 */
const handleCheckOut = async (row: OrderApi.OrderVO) => {
  try {
    await message.confirm(
      `为 ${row.roomNo} 办理退房？退房后房间将转为「待清洁」并自动生成清洁任务。`
    )
    await OrderApi.checkOutOrder(row.id!)
    message.success('退房成功，已生成清洁任务')
    await getList()
  } catch {}
}

const actionRef = ref()
const hourlyRef = ref()

/** 更多操作 */
const handleCommand = async (command: string, row: OrderApi.OrderVO) => {
  if (command === 'extend' || command === 'changeRoom' || command === 'cancel') {
    actionRef.value.open(command, row)
    return
  }
  if (command === 'refund') {
    openPayment(row, 'refund')
    return
  }
  if (command === 'edit') {
    openForm('update', row.id)
    return
  }
  if (command === 'delete') {
    await handleDelete(row.id!)
    return
  }
  if (command === 'earlyCheckout') {
    try {
      await message.confirm(
        `提前退房将按实际夜数重算房费并退回差额，确认继续？`
      )
      await OrderApi.earlyCheckout(row.id!)
      message.success('提前退房成功')
      await getList()
    } catch {}
  }
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await OrderApi.deleteOrder(id)
    message.success('删除成功')
    await getList()
  } catch {}
}

onMounted(async () => {
  await getOptions()
  await getList()
  // 从住客页 / 看板跳转过来时，预填住客或房间并直接打开下单
  const guestId = route.query.guestId
  const roomId = route.query.roomId
  if (guestId || roomId) {
    const presetGuest = guestId
      ? guestList.value.find((g) => String(g.id) === String(guestId))
      : undefined
    const presetRoom = roomId
      ? roomList.value.find((r) => String(r.id) === String(roomId))
      : undefined
    formRef.value.open('create', undefined, presetGuest, presetRoom)
  }
})
</script>
