<template>
  <ContentWrap>
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="76px"
    >
      <el-form-item label="名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="房价计划名称"
          clearable
          class="!w-200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="渠道" prop="channelId">
        <el-select v-model="queryParams.channelId" placeholder="全部" clearable class="!w-180px">
          <el-option v-for="item in channelList" :key="item.id" :label="item.name" :value="item.id!" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable class="!w-120px">
          <el-option label="开启" :value="0" />
          <el-option label="关闭" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm()" v-hasPermi="['hotel:rate-plan:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新增房价计划
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="编号" prop="id" width="70" align="center" />
      <el-table-column label="名称" prop="name" min-width="200" show-overflow-tooltip />
      <el-table-column label="编码" prop="code" width="120" />
      <el-table-column label="渠道" prop="channelName" width="140" show-overflow-tooltip />
      <el-table-column label="适用房型" width="120">
        <template #default="{ row }">{{ row.roomTypeName || '全部房型' }}</template>
      </el-table-column>
      <el-table-column label="价格规则" width="200">
        <template #default="{ row }">
          <span>{{ row.priceRuleTypeName }}</span>
          <span v-if="row.priceRuleType === 20" class="ml-4px text-gray-500">
            （{{ Number(row.priceRuleValue).toFixed(0) }}%）
          </span>
          <span v-else-if="row.priceRuleValue != null" class="ml-4px text-gray-500">
            （{{ Number(row.priceRuleValue).toFixed(2) }}）
          </span>
        </template>
      </el-table-column>
      <el-table-column label="早餐" width="90" align="center">
        <template #default="{ row }">{{ breakfastLabel(row.breakfast) }}</template>
      </el-table-column>
      <el-table-column label="入住限制" width="150">
        <template #default="{ row }">
          {{ row.minNights || 1 }} 晚起<template v-if="row.maxNights">，最多 {{ row.maxNights }} 晚</template>
        </template>
      </el-table-column>
      <el-table-column label="取消政策" prop="cancelPolicyName" width="170" show-overflow-tooltip />
      <el-table-column label="预付" width="90" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.needDeposit" type="warning">{{ Number(row.depositRate || 0).toFixed(0) }}%</el-tag>
          <span v-else class="text-gray-400">否</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'info'">{{ row.statusName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="190" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="openCalc(row)">试算</el-button>
          <el-button link type="primary" @click="openForm(row)" v-hasPermi="['hotel:rate-plan:update']">
            编辑
          </el-button>
          <el-button link type="danger" @click="handleDelete(row.id)" v-hasPermi="['hotel:rate-plan:delete']">
            删除
          </el-button>
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

  <!-- 新增 / 编辑 -->
  <el-dialog
    v-model="dialogVisible"
    :title="form.id ? '编辑房价计划' : '新增房价计划'"
    width="720px"
    append-to-body
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
      <el-row>
        <el-col :span="12">
          <el-form-item label="名称" prop="name">
            <el-input v-model="form.name" placeholder="如 直销 9 折含双早" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="编码" prop="code">
            <el-input v-model="form.code" placeholder="如 DIRECT90" :disabled="!!form.id" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="渠道" prop="channelId">
            <el-select v-model="form.channelId" class="w-full" placeholder="选择渠道">
              <el-option v-for="item in channelList" :key="item.id" :label="item.name" :value="item.id!" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="适用房型">
            <el-select v-model="form.roomTypeId" class="w-full" clearable placeholder="全部房型">
              <el-option v-for="item in roomTypeList" :key="item.id" :label="item.name" :value="item.id!" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="价格规则" prop="priceRuleType">
            <el-select v-model="form.priceRuleType" class="w-full">
              <el-option label="按日历价" :value="10" />
              <el-option label="日历价折扣（填 90 = 9 折）" :value="20" />
              <el-option label="日历价减额（填 50 = 减 50 元）" :value="30" />
              <el-option label="固定价" :value="40" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="规则值" :required="form.priceRuleType !== 10">
            <el-input-number
              v-model="form.priceRuleValue"
              :min="0"
              :precision="2"
              class="!w-200px"
              :disabled="form.priceRuleType === 10"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="早餐">
            <el-select v-model="form.breakfast" class="w-full">
              <el-option label="不含早" :value="0" />
              <el-option label="含单早" :value="1" />
              <el-option label="含双早" :value="2" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="取消政策">
            <el-select v-model="form.cancelPolicy" class="w-full">
              <el-option label="免费取消" :value="10" />
              <el-option label="入住前 N 小时可免费取消" :value="20" />
              <el-option label="不可取消" :value="30" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="最少晚数">
            <el-input-number v-model="form.minNights" :min="1" class="!w-160px" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="最多晚数">
            <el-input-number v-model="form.maxNights" :min="1" class="!w-160px" placeholder="不限" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="最少提前(小时)">
            <el-input-number v-model="form.minAdvanceHours" :min="0" class="!w-160px" placeholder="不限" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="免费取消(小时)">
            <el-input-number
              v-model="form.cancelFreeHours"
              :min="0"
              class="!w-160px"
              :disabled="form.cancelPolicy !== 20"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="需预付">
            <el-switch v-model="form.needDeposit" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="预付比例(%)">
            <el-input-number
              v-model="form.depositRate"
              :min="0"
              :max="100"
              :precision="2"
              class="!w-160px"
              :disabled="!form.needDeposit"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态">
            <el-radio-group v-model="form.status">
              <el-radio :value="0">开启</el-radio>
              <el-radio :value="1">关闭</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="排序">
            <el-input-number v-model="form.sort" :min="0" class="!w-160px" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="备注">
            <el-input v-model="form.remark" type="textarea" :rows="2" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
    </template>
  </el-dialog>

  <!-- 试算 -->
  <el-dialog v-model="calcVisible" title="房价试算" width="620px" append-to-body>
    <el-form :inline="true" label-width="90px">
      <el-form-item label="房型">
        <el-select v-model="calcQuery.roomTypeId" clearable placeholder="按计划绑定房型" class="!w-180px">
          <el-option v-for="item in roomTypeList" :key="item.id" :label="item.name" :value="item.id!" />
        </el-select>
      </el-form-item>
      <el-form-item label="日期区间">
        <el-date-picker
          v-model="calcQuery.range"
          type="daterange"
          value-format="YYYY-MM-DD"
          start-placeholder="开始"
          end-placeholder="结束"
          class="!w-260px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="calcLoading" @click="doCalc">试算</el-button>
      </el-form-item>
    </el-form>
    <template v-if="calcResult">
      <el-alert
        v-if="calcResult.dailyPrices?.length"
        :closable="false"
        type="success"
        class="mb-10px"
        :title="`${calcResult.roomTypeName} · ${calcResult.ratePlanName}：${calcResult.nights} 晚合计 ¥${Number(calcResult.totalAmount).toFixed(2)}`"
      />
      <el-table :data="calcResult.dailyPrices" size="small" max-height="320">
        <el-table-column label="日期" prop="bizDate" width="130" />
        <el-table-column label="日历价（基线）" width="140" align="right">
          <template #default="{ row }">¥{{ Number(row.basePrice).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="本计划售价" width="140" align="right">
          <template #default="{ row }">
            <span class="text-14px font-bold text-red-500">¥{{ Number(row.price).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="剩余房量" prop="remaining" align="center" />
      </el-table>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import * as RatePlanApi from '@/api/hotel/ratePlan'
import * as ChannelApi from '@/api/hotel/channel'
import * as RoomTypeApi from '@/api/hotel/roomType'

defineOptions({ name: 'HotelRatePlan' })

const message = useMessage()
const loading = ref(false)
const submitting = ref(false)
const list = ref<RatePlanApi.RatePlanVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: '',
  channelId: undefined as number | undefined,
  status: undefined as number | undefined
})
const queryFormRef = ref()

const channelList = ref<ChannelApi.ChannelVO[]>([])
const roomTypeList = ref<RoomTypeApi.RoomTypeVO[]>([])

const dialogVisible = ref(false)
const formRef = ref()
const form = reactive<RatePlanApi.RatePlanVO>({
  id: undefined,
  name: '',
  code: '',
  channelId: undefined,
  roomTypeId: undefined,
  priceRuleType: 20,
  priceRuleValue: 90,
  breakfast: 0,
  minNights: 1,
  maxNights: undefined,
  minAdvanceHours: undefined,
  cancelPolicy: 10,
  cancelFreeHours: undefined,
  needDeposit: false,
  depositRate: undefined,
  status: 0,
  sort: 0,
  remark: ''
})

const rules = {
  name: [{ required: true, message: '请填写名称', trigger: 'blur' }],
  code: [{ required: true, message: '请填写编码', trigger: 'blur' }],
  channelId: [{ required: true, message: '请选择渠道', trigger: 'change' }],
  priceRuleType: [{ required: true, message: '请选择价格规则', trigger: 'change' }]
}

const breakfastLabel = (v?: number) => (v === 1 ? '单早' : v === 2 ? '双早' : '无早')

const getList = async () => {
  loading.value = true
  try {
    const data = await RatePlanApi.getRatePlanPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryParams.name = ''
  queryParams.channelId = undefined
  queryParams.status = undefined
  handleQuery()
}

const openForm = (row?: RatePlanApi.RatePlanVO) => {
  Object.assign(form, {
    id: row?.id,
    name: row?.name || '',
    code: row?.code || '',
    channelId: row?.channelId ?? channelList.value[0]?.id,
    roomTypeId: row?.roomTypeId ?? undefined,
    priceRuleType: row?.priceRuleType ?? 20,
    priceRuleValue: row?.priceRuleValue ?? 90,
    breakfast: row?.breakfast ?? 0,
    minNights: row?.minNights ?? 1,
    maxNights: row?.maxNights ?? undefined,
    minAdvanceHours: row?.minAdvanceHours ?? undefined,
    cancelPolicy: row?.cancelPolicy ?? 10,
    cancelFreeHours: row?.cancelFreeHours ?? undefined,
    needDeposit: row?.needDeposit ?? false,
    depositRate: row?.depositRate ?? undefined,
    status: row?.status ?? 0,
    sort: row?.sort ?? 0,
    remark: row?.remark || ''
  })
  if (!form.channelId && channelList.value.length) {
    form.channelId = channelList.value[0].id
  }
  dialogVisible.value = true
}

const submit = async () => {
  await formRef.value?.validate()
  submitting.value = true
  try {
    if (form.id) {
      await RatePlanApi.updateRatePlan({ ...form })
      message.success('更新成功')
    } else {
      await RatePlanApi.createRatePlan({ ...form })
      message.success('创建成功')
    }
    dialogVisible.value = false
    await getList()
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await RatePlanApi.deleteRatePlan(id)
    message.success('删除成功')
    await getList()
  } catch {}
}

// ============ 试算 ============
const calcVisible = ref(false)
const calcLoading = ref(false)
const calcResult = ref<RatePlanApi.CalcPriceVO>()
const calcQuery = reactive({
  ratePlanId: undefined as number | undefined,
  roomTypeId: undefined as number | undefined,
  range: [] as string[]
})

const today = () => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}
const plusDays = (base: string, days: number) => {
  const d = new Date(base)
  d.setDate(d.getDate() + days)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const openCalc = (row: RatePlanApi.RatePlanVO) => {
  calcResult.value = undefined
  calcQuery.ratePlanId = row.id
  calcQuery.roomTypeId = row.roomTypeId
  const begin = today()
  calcQuery.range = [begin, plusDays(begin, 2)]
  calcVisible.value = true
}

const doCalc = async () => {
  if (!calcQuery.ratePlanId || !calcQuery.range?.length) {
    message.warning('请选择日期区间')
    return
  }
  calcLoading.value = true
  try {
    calcResult.value = await RatePlanApi.calcRatePlanPrice({
      ratePlanId: calcQuery.ratePlanId!,
      roomTypeId: calcQuery.roomTypeId,
      beginDate: calcQuery.range[0],
      endDate: calcQuery.range[1]
    })
  } finally {
    calcLoading.value = false
  }
}

onMounted(async () => {
  await getList()
  channelList.value = (await ChannelApi.getEnabledChannelList()) || []
  roomTypeList.value = (await RoomTypeApi.getRoomTypeSimpleList()) || []
})
</script>
