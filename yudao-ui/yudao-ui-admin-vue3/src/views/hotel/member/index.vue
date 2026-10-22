<template>
  <ContentWrap>
    <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="76px" class="-mb-15px">
      <el-form-item label="会员卡号" prop="memberNo">
        <el-input v-model="queryParams.memberNo" placeholder="会员卡号" clearable class="!w-180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="姓名" prop="name">
        <el-input v-model="queryParams.name" placeholder="姓名" clearable class="!w-150px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="手机号" prop="mobile">
        <el-input v-model="queryParams.mobile" placeholder="手机号" clearable class="!w-180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="等级" prop="level">
        <el-select v-model="queryParams.level" placeholder="全部" clearable class="!w-140px">
          <el-option label="普通" :value="10" />
          <el-option label="银卡" :value="20" />
          <el-option label="金卡" :value="30" />
          <el-option label="铂金" :value="40" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm()" v-hasPermi="['hotel:member:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新增会员
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="会员卡号" prop="memberNo" width="180" />
      <el-table-column label="姓名" prop="name" width="120" />
      <el-table-column label="手机号" prop="mobile" width="140" />
      <el-table-column label="等级" width="100">
        <template #default="{ row }">
          <el-tag :type="levelTag(row.level)" effect="plain">{{ row.levelName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="积分" prop="points" width="90" align="right" />
      <el-table-column label="储值余额" width="120" align="right">
        <template #default="{ row }">￥{{ formatMoney(row.balance) }}</template>
      </el-table-column>
      <el-table-column label="累计消费" width="120" align="right">
        <template #default="{ row }">￥{{ formatMoney(row.totalAmount) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'">{{ row.statusName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" prop="registerTime" width="170" />
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openForm(row)" v-hasPermi="['hotel:member:update']">编辑</el-button>
          <el-button link type="primary" @click="openPoints(row)" v-hasPermi="['hotel:member:update']">调积分</el-button>
          <el-button link type="warning" @click="openBalance(row)" v-hasPermi="['hotel:member:update']">调储值</el-button>
          <el-button link @click="openLog(row)">积分流水</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>

  <el-dialog v-model="formVisible" :title="form.id ? '编辑会员' : '新增会员'" width="480px" append-to-body>
    <el-form :model="form" label-width="90px">
      <el-form-item label="姓名" required><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="手机号" required><el-input v-model="form.mobile" /></el-form-item>
      <el-form-item label="密码"><el-input v-model="form.password" placeholder="留空则验证码/免密登录" show-password /></el-form-item>
      <el-form-item label="等级">
        <el-select v-model="form.level" class="w-full">
          <el-option label="普通" :value="10" />
          <el-option label="银卡" :value="20" />
          <el-option label="金卡" :value="30" />
          <el-option label="铂金" :value="40" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="form.status">
          <el-radio :value="0">正常</el-radio>
          <el-radio :value="1">冻结</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="formVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="adjustVisible" :title="adjustType === 'points' ? '调整积分' : '调整储值余额'" width="420px" append-to-body>
    <el-form label-width="90px">
      <el-form-item label="会员"><el-input :model-value="adjustMember?.name" disabled /></el-form-item>
      <el-form-item :label="adjustType === 'points' ? '积分变动' : '金额变动'">
        <el-input-number v-model="adjustValue" :precision="adjustType === 'points' ? 0 : 2" class="!w-200px" />
      </el-form-item>
      <el-form-item label="备注"><el-input v-model="adjustRemark" type="textarea" :rows="2" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="adjustVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submitAdjust">确定</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="logVisible" title="积分流水" width="720px" append-to-body>
    <el-table :data="logList" max-height="420">
      <el-table-column label="时间" prop="createTime" width="170" />
      <el-table-column label="类型" prop="typeName" width="100" />
      <el-table-column label="变动" prop="points" width="90" align="right" />
      <el-table-column label="变动后" prop="balance" width="90" align="right" />
      <el-table-column label="备注" prop="remark" show-overflow-tooltip />
    </el-table>
  </el-dialog>
</template>

<script lang="ts" setup>
import { formatMoney } from '@/utils/hotel'
import * as MemberApi from '@/api/hotel/member'

defineOptions({ name: 'HotelMember' })

const message = useMessage()
const loading = ref(false)
const submitting = ref(false)
const list = ref<MemberApi.MemberVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  memberNo: '',
  name: '',
  mobile: '',
  level: undefined as number | undefined
})
const queryFormRef = ref()

const formVisible = ref(false)
const form = reactive<MemberApi.MemberSaveVO>({
  id: undefined,
  name: '',
  mobile: '',
  password: '',
  level: 10,
  status: 0
})

const adjustVisible = ref(false)
const adjustType = ref<'points' | 'balance'>('points')
const adjustMember = ref<MemberApi.MemberVO>()
const adjustValue = ref<number>(0)
const adjustRemark = ref('')

const logVisible = ref(false)
const logList = ref<MemberApi.MemberPointsLogVO[]>([])

const levelTag = (level?: number) => {
  if (level === 40) return 'danger'
  if (level === 30) return 'warning'
  if (level === 20) return 'success'
  return 'info'
}

const getList = async () => {
  loading.value = true
  try {
    const data = await MemberApi.getMemberPage(queryParams as any)
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
  queryFormRef.value?.resetFields()
  handleQuery()
}

const openForm = (row?: MemberApi.MemberVO) => {
  form.id = row?.id
  form.name = row?.name || ''
  form.mobile = row?.mobile || ''
  form.password = ''
  form.level = row?.level || 10
  form.status = row?.status ?? 0
  formVisible.value = true
}

const submitForm = async () => {
  if (!form.name || !form.mobile) {
    message.warning('请填写姓名和手机号')
    return
  }
  submitting.value = true
  try {
    if (form.id) {
      await MemberApi.updateMember({ ...form })
    } else {
      await MemberApi.createMember({ ...form })
    }
    message.success('保存成功')
    formVisible.value = false
    await getList()
  } finally {
    submitting.value = false
  }
}

const openPoints = (row: MemberApi.MemberVO) => {
  adjustType.value = 'points'
  adjustMember.value = row
  adjustValue.value = 0
  adjustRemark.value = ''
  adjustVisible.value = true
}

const openBalance = (row: MemberApi.MemberVO) => {
  adjustType.value = 'balance'
  adjustMember.value = row
  adjustValue.value = 0
  adjustRemark.value = ''
  adjustVisible.value = true
}

const submitAdjust = async () => {
  if (!adjustMember.value?.id || !adjustValue.value) {
    message.warning('请输入调整值')
    return
  }
  submitting.value = true
  try {
    if (adjustType.value === 'points') {
      await MemberApi.adjustPoints({
        memberId: adjustMember.value.id,
        points: adjustValue.value,
        remark: adjustRemark.value
      })
    } else {
      await MemberApi.adjustBalance({
        memberId: adjustMember.value.id,
        amount: adjustValue.value,
        remark: adjustRemark.value
      })
    }
    message.success('调整成功')
    adjustVisible.value = false
    await getList()
  } finally {
    submitting.value = false
  }
}

const openLog = async (row: MemberApi.MemberVO) => {
  logList.value = (await MemberApi.getPointsLog(row.id!)) || []
  logVisible.value = true
}

onMounted(getList)
</script>
