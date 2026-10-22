<template>
  <ContentWrap>
    <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px" class="-mb-15px">
      <el-form-item label="OTA" prop="otaCode">
        <el-select v-model="queryParams.otaCode" placeholder="全部" clearable class="!w-160px">
          <el-option v-for="o in otaOptions" :key="o" :label="o" :value="o" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable class="!w-140px">
          <el-option label="启用" :value="0" />
          <el-option label="停用" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" />搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" />重置</el-button>
        <el-button type="primary" plain @click="openForm()" v-hasPermi="['hotel:ota:create']">
          <Icon icon="ep:plus" class="mr-5px" />新增配置
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="OTA" prop="otaCode" width="100" />
      <el-table-column label="渠道编号" prop="channelId" width="100" />
      <el-table-column label="AppKey" prop="appKey" min-width="140" show-overflow-tooltip />
      <el-table-column label="推房价" width="90"><template #default="{ row }">{{ row.pushRate ? '是' : '否' }}</template></el-table-column>
      <el-table-column label="拉订单" width="90"><template #default="{ row }">{{ row.pullOrder ? '是' : '否' }}</template></el-table-column>
      <el-table-column label="同步间隔(秒)" prop="syncInterval" width="120" />
      <el-table-column label="模拟器" width="90"><template #default="{ row }"><el-tag :type="row.mockEnabled ? 'success' : 'info'">{{ row.mockEnabled ? '开' : '关' }}</el-tag></template></el-table-column>
      <el-table-column label="状态" width="90"><template #default="{ row }"><el-tag :type="row.status === 0 ? 'success' : 'info'">{{ row.status === 0 ? '启用' : '停用' }}</el-tag></template></el-table-column>
      <el-table-column label="最后同步" prop="lastSyncTime" width="170" :formatter="dateFormatter" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openForm(row)" v-hasPermi="['hotel:ota:update']">编辑</el-button>
          <el-button link type="success" @click="handleSync(row.id)" v-hasPermi="['hotel:ota:update']">同步订单</el-button>
          <el-button link type="warning" @click="handlePush(row.id)" v-hasPermi="['hotel:ota:update']">推房价</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>

  <el-dialog v-model="dialogVisible" :title="formData.id ? '编辑 OTA 配置' : '新增 OTA 配置'" width="620px">
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="110px">
      <el-form-item label="渠道编号" prop="channelId"><el-input-number v-model="formData.channelId" :min="1" controls-position="right" /></el-form-item>
      <el-form-item label="OTA 编码" prop="otaCode">
        <el-select v-model="formData.otaCode" class="!w-full">
          <el-option v-for="o in otaOptions" :key="o" :label="o" :value="o" />
        </el-select>
      </el-form-item>
      <el-form-item label="AppKey"><el-input v-model="formData.appKey" /></el-form-item>
      <el-form-item label="AppSecret"><el-input v-model="formData.appSecret" type="password" show-password /></el-form-item>
      <el-form-item label="接口地址"><el-input v-model="formData.apiUrl" /></el-form-item>
      <el-form-item label="同步间隔(秒)"><el-input-number v-model="formData.syncInterval" :min="30" controls-position="right" /></el-form-item>
      <el-form-item label="推房价"><el-switch v-model="formData.pushRate" /></el-form-item>
      <el-form-item label="拉订单"><el-switch v-model="formData.pullOrder" /></el-form-item>
      <el-form-item label="模拟器"><el-switch v-model="formData.mockEnabled" /></el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="formData.status">
          <el-radio :value="0">启用</el-radio>
          <el-radio :value="1">停用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注"><el-input v-model="formData.remark" type="textarea" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import * as OtaApi from '@/api/hotel/ota'

defineOptions({ name: 'HotelOtaConfig' })

const message = useMessage()
const otaOptions = ['CTRIP', 'MEITUAN', 'FLIGGY', 'QUNAR']
const loading = ref(false)
const saving = ref(false)
const total = ref(0)
const list = ref<OtaApi.OtaConfigVO[]>([])
const queryParams = reactive({ pageNo: 1, pageSize: 10, otaCode: '', status: undefined as number | undefined })
const queryFormRef = ref()
const dialogVisible = ref(false)
const formRef = ref()
const formData = ref<OtaApi.OtaConfigVO>({})

const rules = {
  channelId: [{ required: true, message: '渠道编号不能为空', trigger: 'blur' }],
  otaCode: [{ required: true, message: 'OTA 编码不能为空', trigger: 'change' }]
}

const getList = async () => {
  loading.value = true
  try {
    const data = await OtaApi.getOtaConfigPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => { queryParams.pageNo = 1; getList() }
const resetQuery = () => { queryFormRef.value?.resetFields(); handleQuery() }

const openForm = (row?: OtaApi.OtaConfigVO) => {
  formData.value = row ? { ...row } : {
    channelId: 2, otaCode: 'CTRIP', pushRate: true, pullOrder: true,
    mockEnabled: true, syncInterval: 300, status: 0
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    if (formData.value.id) {
      await OtaApi.updateOtaConfig(formData.value)
    } else {
      await OtaApi.createOtaConfig(formData.value)
    }
    message.success('保存成功')
    dialogVisible.value = false
    await getList()
  } finally {
    saving.value = false
  }
}

const handleSync = async (id: number) => {
  const created = await OtaApi.syncOtaOrder(id)
  message.success(`同步完成，新增 ${created ?? 0} 条 OTA 订单`)
  await getList()
}

const handlePush = async (id: number) => {
  await OtaApi.pushOtaRate(id)
  message.success('房价库存已推送 (Mock)')
  await getList()
}

onMounted(getList)
</script>
