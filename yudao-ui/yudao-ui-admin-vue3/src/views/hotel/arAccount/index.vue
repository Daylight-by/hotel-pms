<template>
  <ContentWrap>
    <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px" class="-mb-15px">
      <el-form-item label="单位名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="单位名称" clearable class="!w-200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="单位编码" prop="code">
        <el-input v-model="queryParams.code" placeholder="单位编码" clearable class="!w-180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable class="!w-140px">
          <el-option label="正常" :value="0" />
          <el-option label="停用" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" />搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" />重置</el-button>
        <el-button type="primary" plain @click="openForm()" v-hasPermi="['hotel:ar:create']">
          <Icon icon="ep:plus" class="mr-5px" />新增协议单位
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="单位名称" prop="name" min-width="200" show-overflow-tooltip />
      <el-table-column label="编码" prop="code" width="120" />
      <el-table-column label="联系人" prop="contactName" width="110" />
      <el-table-column label="联系电话" prop="contactMobile" width="130" />
      <el-table-column label="信用额度" width="120"><template #default="{ row }">{{ money(row.creditLimit) }}</template></el-table-column>
      <el-table-column label="已用额度" width="120"><template #default="{ row }">{{ money(row.usedAmount) }}</template></el-table-column>
      <el-table-column label="可用额度" width="120"><template #default="{ row }"><span class="success">{{ money(row.availableAmount) }}</span></template></el-table-column>
      <el-table-column label="结算周期" width="100"><template #default="{ row }">{{ cycleLabel(row.settleCycle) }}</template></el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }"><el-tag :type="row.status === 0 ? 'success' : 'info'">{{ row.status === 0 ? '正常' : '停用' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="290" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openForm(row)" v-hasPermi="['hotel:ar:update']">编辑</el-button>
          <el-button link type="success" @click="openPost(row)" v-hasPermi="['hotel:ar:create']">挂账</el-button>
          <el-button link type="warning" @click="openSettlement(row)" v-hasPermi="['hotel:ar:create']">生成结算单</el-button>
          <el-button link @click="openLedger(row)">明细</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>

  <el-dialog v-model="formVisible" :title="formData.id ? '编辑协议单位' : '新增协议单位'" width="620px">
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
      <el-form-item label="单位名称" prop="name"><el-input v-model="formData.name" /></el-form-item>
      <el-form-item label="单位编码" prop="code"><el-input v-model="formData.code" /></el-form-item>
      <el-form-item label="税号"><el-input v-model="formData.taxNo" /></el-form-item>
      <el-form-item label="联系人"><el-input v-model="formData.contactName" /></el-form-item>
      <el-form-item label="联系手机"><el-input v-model="formData.contactMobile" /></el-form-item>
      <el-form-item label="信用额度"><el-input-number v-model="formData.creditLimit" :min="0" :precision="2" controls-position="right" /></el-form-item>
      <el-form-item label="结算周期">
        <el-select v-model="formData.settleCycle" class="!w-full">
          <el-option label="现结" :value="10" /><el-option label="周结" :value="20" />
          <el-option label="月结" :value="30" /><el-option label="季结" :value="40" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="formData.status"><el-radio :value="0">正常</el-radio><el-radio :value="1">停用</el-radio></el-radio-group>
      </el-form-item>
      <el-form-item label="备注"><el-input v-model="formData.remark" type="textarea" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="formVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="postVisible" title="协议单位挂账" width="480px">
    <el-form label-width="100px">
      <el-form-item label="协议单位"><el-input :model-value="current?.name" disabled /></el-form-item>
      <el-form-item label="挂账金额"><el-input-number v-model="postForm.amount" :min="0.01" :precision="2" controls-position="right" /></el-form-item>
      <el-form-item label="关联订单"><el-input-number v-model="postForm.orderId" :min="0" controls-position="right" /></el-form-item>
      <el-form-item label="备注"><el-input v-model="postForm.remark" type="textarea" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="postVisible = false">取消</el-button>
      <el-button type="primary" @click="handlePost">确认挂账</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="settleVisible" title="生成结算单" width="480px">
    <el-form label-width="100px">
      <el-form-item label="协议单位"><el-input :model-value="current?.name" disabled /></el-form-item>
      <el-form-item label="账期起"><el-date-picker v-model="settleForm.beginDate" type="date" value-format="YYYY-MM-DD" class="!w-full" /></el-form-item>
      <el-form-item label="账期止"><el-date-picker v-model="settleForm.endDate" type="date" value-format="YYYY-MM-DD" class="!w-full" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="settleVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSettlement">生成</el-button>
    </template>
  </el-dialog>

  <el-drawer v-model="ledgerVisible" size="60%" :title="`挂账明细 - ${current?.name || ''}`">
    <el-table :data="ledgers" v-loading="ledgerLoading">
      <el-table-column label="金额" width="120"><template #default="{ row }">{{ money(row.amount) }}</template></el-table-column>
      <el-table-column label="类型" width="90"><template #default="{ row }">{{ ledgerType(row.type) }}</template></el-table-column>
      <el-table-column label="营业日" prop="bizDate" width="120" />
      <el-table-column label="订单" prop="orderId" width="100" />
      <el-table-column label="状态" width="90"><template #default="{ row }">{{ row.status === 20 ? '已结' : '未结' }}</template></el-table-column>
      <el-table-column label="结算单" prop="settlementId" width="100" />
      <el-table-column label="备注" prop="remark" min-width="160" show-overflow-tooltip />
    </el-table>
  </el-drawer>
</template>

<script setup lang="ts">
import * as ArApi from '@/api/hotel/ar'

defineOptions({ name: 'HotelArAccount' })

const message = useMessage()
const loading = ref(false)
const saving = ref(false)
const total = ref(0)
const list = ref<ArApi.ArAccountVO[]>([])
const queryParams = reactive({ pageNo: 1, pageSize: 10, name: '', code: '', status: undefined as number | undefined })
const queryFormRef = ref()

const formVisible = ref(false)
const formRef = ref()
const formData = ref<ArApi.ArAccountVO>({})
const rules = {
  name: [{ required: true, message: '单位名称不能为空', trigger: 'blur' }],
  code: [{ required: true, message: '单位编码不能为空', trigger: 'blur' }]
}

const current = ref<ArApi.ArAccountVO>()
const postVisible = ref(false)
const postForm = reactive({ amount: 1000, orderId: undefined as number | undefined, remark: '' })
const settleVisible = ref(false)
const settleForm = reactive({ beginDate: '', endDate: '' })
const ledgerVisible = ref(false)
const ledgerLoading = ref(false)
const ledgers = ref<ArApi.ArLedgerVO[]>([])

const money = (v?: number) => `¥ ${Number(v || 0).toFixed(2)}`
const cycleLabel = (v?: number) => ({ 10: '现结', 20: '周结', 30: '月结', 40: '季结' } as any)[v || 0] || '-'
const ledgerType = (v?: number) => ({ 10: '挂账', 20: '冲销', 30: '结算' } as any)[v || 0] || '-'

const getList = async () => {
  loading.value = true
  try {
    const data = await ArApi.getArAccountPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => { queryParams.pageNo = 1; getList() }
const resetQuery = () => { queryFormRef.value?.resetFields(); handleQuery() }

const openForm = (row?: ArApi.ArAccountVO) => {
  formData.value = row ? { ...row } : { status: 0, settleCycle: 30, creditLimit: 10000 }
  formVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    if (formData.value.id) {
      await ArApi.updateArAccount(formData.value)
    } else {
      await ArApi.createArAccount(formData.value)
    }
    message.success('保存成功')
    formVisible.value = false
    await getList()
  } finally {
    saving.value = false
  }
}

const openPost = (row: ArApi.ArAccountVO) => {
  current.value = row
  postForm.amount = 1000
  postForm.orderId = undefined
  postForm.remark = ''
  postVisible.value = true
}

const handlePost = async () => {
  await ArApi.postToAr({
    arAccountId: current.value?.id,
    amount: postForm.amount,
    orderId: postForm.orderId || undefined,
    remark: postForm.remark
  })
  message.success('挂账成功')
  postVisible.value = false
  await getList()
}

const openSettlement = (row: ArApi.ArAccountVO) => {
  current.value = row
  const today = new Date()
  const first = new Date(today.getFullYear(), today.getMonth(), 1)
  settleForm.beginDate = first.toISOString().slice(0, 10)
  settleForm.endDate = today.toISOString().slice(0, 10)
  settleVisible.value = true
}

const handleSettlement = async () => {
  const id = await ArApi.generateSettlement({
    arAccountId: current.value?.id,
    beginDate: settleForm.beginDate,
    endDate: settleForm.endDate
  })
  message.success(`结算单已生成（ID ${id}）`)
  settleVisible.value = false
  await getList()
}

const openLedger = async (row: ArApi.ArAccountVO) => {
  current.value = row
  ledgerVisible.value = true
  ledgerLoading.value = true
  try {
    const data = await ArApi.getArLedgerPage({ pageNo: 1, pageSize: 100, arAccountId: row.id })
    ledgers.value = data.list
  } finally {
    ledgerLoading.value = false
  }
}

onMounted(getList)
</script>

<style scoped>
.success { color: var(--el-color-success); font-weight: 600; }
</style>
