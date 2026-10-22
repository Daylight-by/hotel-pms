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
          placeholder="渠道名称"
          clearable
          class="!w-200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="编码" prop="code">
        <el-input
          v-model="queryParams.code"
          placeholder="如 DIRECT"
          clearable
          class="!w-180px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="全部" clearable class="!w-140px">
          <el-option v-for="item in CHANNEL_TYPE" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm()" v-hasPermi="['hotel:channel:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新增渠道
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="编号" prop="id" width="80" align="center" />
      <el-table-column label="渠道名称" prop="name" min-width="180" show-overflow-tooltip />
      <el-table-column label="编码" prop="code" width="120" />
      <el-table-column label="类型" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.type === 10 ? 'success' : 'info'">{{ row.typeName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="佣金比例" width="110" align="right">
        <template #default="{ row }">{{ Number(row.commissionRate || 0).toFixed(2) }}%</template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'info'">{{ row.statusName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="排序" prop="sort" width="80" align="center" />
      <el-table-column label="备注" prop="remark" min-width="200" show-overflow-tooltip />
      <el-table-column
        label="创建时间"
        prop="createTime"
        width="180"
        :formatter="dateFormatter"
      />
      <el-table-column label="操作" width="140" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" @click="openForm(row)" v-hasPermi="['hotel:channel:update']">
            编辑
          </el-button>
          <el-button link type="danger" @click="handleDelete(row.id)" v-hasPermi="['hotel:channel:delete']">
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

  <el-dialog v-model="dialogVisible" :title="form.id ? '编辑渠道' : '新增渠道'" width="520px" append-to-body>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
      <el-form-item label="渠道名称" prop="name">
        <el-input v-model="form.name" placeholder="如 直销（官网/小程序）" />
      </el-form-item>
      <el-form-item label="渠道编码" prop="code">
        <el-input v-model="form.code" placeholder="如 DIRECT，唯一" :disabled="!!form.id" />
      </el-form-item>
      <el-form-item label="类型" prop="type">
        <el-select v-model="form.type" class="w-full">
          <el-option v-for="item in CHANNEL_TYPE" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="佣金比例">
        <el-input-number v-model="form.commissionRate" :min="0" :max="100" :precision="2" class="!w-200px" />
        <span class="ml-8px text-12px text-gray-400">OTA 才需要，直销填 0</span>
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="form.status">
          <el-radio :value="0">开启</el-radio>
          <el-radio :value="1">关闭</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="form.sort" :min="0" class="!w-200px" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import * as ChannelApi from '@/api/hotel/channel'

defineOptions({ name: 'HotelChannel' })

const CHANNEL_TYPE = [
  { value: 10, label: '直销' },
  { value: 20, label: 'OTA' },
  { value: 30, label: '协议' },
  { value: 40, label: '会员' },
  { value: 50, label: '门店' }
]

const message = useMessage()
const loading = ref(false)
const submitting = ref(false)
const list = ref<ChannelApi.ChannelVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: '',
  code: '',
  type: undefined as number | undefined
})
const queryFormRef = ref()

const dialogVisible = ref(false)
const formRef = ref()
const form = reactive<ChannelApi.ChannelVO>({
  id: undefined,
  name: '',
  code: '',
  type: 10,
  commissionRate: 0,
  status: 0,
  sort: 0,
  remark: ''
})

const rules = {
  name: [{ required: true, message: '请填写渠道名称', trigger: 'blur' }],
  code: [{ required: true, message: '请填写渠道编码', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

const getList = async () => {
  loading.value = true
  try {
    const data = await ChannelApi.getChannelPage(queryParams)
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
  queryParams.name = ''
  queryParams.code = ''
  queryParams.type = undefined
  handleQuery()
}

const openForm = (row?: ChannelApi.ChannelVO) => {
  Object.assign(form, {
    id: row?.id,
    name: row?.name || '',
    code: row?.code || '',
    type: row?.type ?? 10,
    commissionRate: row?.commissionRate ?? 0,
    status: row?.status ?? 0,
    sort: row?.sort ?? 0,
    remark: row?.remark || ''
  })
  dialogVisible.value = true
}

const submit = async () => {
  await formRef.value?.validate()
  submitting.value = true
  try {
    if (form.id) {
      await ChannelApi.updateChannel({ ...form })
      message.success('更新成功')
    } else {
      await ChannelApi.createChannel({ ...form })
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
    await ChannelApi.deleteChannel(id)
    message.success('删除成功')
    await getList()
  } catch {}
}

onMounted(getList)
</script>
