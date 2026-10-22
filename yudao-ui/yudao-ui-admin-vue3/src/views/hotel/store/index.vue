<template>
  <ContentWrap>
    <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px" class="-mb-15px">
      <el-form-item label="门店名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入门店名称" clearable class="!w-200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="门店编码" prop="code">
        <el-input v-model="queryParams.code" placeholder="请输入门店编码" clearable class="!w-180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable class="!w-140px">
          <el-option label="营业" :value="0" />
          <el-option label="停业" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" />搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" />重置</el-button>
        <el-button type="primary" plain @click="openForm()" v-hasPermi="['hotel:store:create']">
          <Icon icon="ep:plus" class="mr-5px" />新增门店
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="门店名称" prop="name" min-width="200" show-overflow-tooltip />
      <el-table-column label="编码" prop="code" width="100" />
      <el-table-column label="城市" width="120">
        <template #default="{ row }">{{ row.city }} {{ row.district }}</template>
      </el-table-column>
      <el-table-column label="地址" prop="address" min-width="200" show-overflow-tooltip />
      <el-table-column label="前台电话" prop="phone" width="130" />
      <el-table-column label="公安编码" prop="psbCode" width="130" />
      <el-table-column label="入住/离店" width="130">
        <template #default="{ row }">{{ row.checkInTime }} / {{ row.checkOutTime }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'info'">{{ row.status === 0 ? '营业' : '停业' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openForm(row)" v-hasPermi="['hotel:store:update']">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row.id)" v-hasPermi="['hotel:store:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>

  <el-dialog v-model="dialogVisible" :title="formData.id ? '编辑门店' : '新增门店'" width="720px">
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
      <el-row :gutter="12">
        <el-col :span="12"><el-form-item label="门店名称" prop="name"><el-input v-model="formData.name" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="门店编码" prop="code"><el-input v-model="formData.code" /></el-form-item></el-col>
        <el-col :span="8"><el-form-item label="省"><el-input v-model="formData.province" /></el-form-item></el-col>
        <el-col :span="8"><el-form-item label="市"><el-input v-model="formData.city" /></el-form-item></el-col>
        <el-col :span="8"><el-form-item label="区"><el-input v-model="formData.district" /></el-form-item></el-col>
        <el-col :span="24"><el-form-item label="详细地址"><el-input v-model="formData.address" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="前台电话"><el-input v-model="formData.phone" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="法定代表人"><el-input v-model="formData.legalPerson" /></el-form-item></el-col>
        <el-col :span="12"><el-form-item label="公安编码"><el-input v-model="formData.psbCode" /></el-form-item></el-col>
        <el-col :span="6"><el-form-item label="入住时间"><el-input v-model="formData.checkInTime" placeholder="14:00" /></el-form-item></el-col>
        <el-col :span="6"><el-form-item label="离店时间"><el-input v-model="formData.checkOutTime" placeholder="12:00" /></el-form-item></el-col>
        <el-col :span="12">
          <el-form-item label="状态">
            <el-radio-group v-model="formData.status">
              <el-radio :value="0">营业</el-radio>
              <el-radio :value="1">停业</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="24"><el-form-item label="备注"><el-input v-model="formData.remark" type="textarea" /></el-form-item></el-col>
      </el-row>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import * as StoreApi from '@/api/hotel/store'

defineOptions({ name: 'HotelStore' })

const message = useMessage()
const loading = ref(false)
const saving = ref(false)
const total = ref(0)
const list = ref<StoreApi.StoreVO[]>([])
const queryParams = reactive({ pageNo: 1, pageSize: 10, name: '', code: '', status: undefined as number | undefined })
const queryFormRef = ref()

const dialogVisible = ref(false)
const formRef = ref()
const formData = ref<StoreApi.StoreVO>({})

const rules = {
  name: [{ required: true, message: '门店名称不能为空', trigger: 'blur' }],
  code: [{ required: true, message: '门店编码不能为空', trigger: 'blur' }]
}

const getList = async () => {
  loading.value = true
  try {
    const data = await StoreApi.getStorePage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => { queryParams.pageNo = 1; getList() }
const resetQuery = () => { queryFormRef.value?.resetFields(); handleQuery() }

const openForm = (row?: StoreApi.StoreVO) => {
  formData.value = row ? { ...row } : { status: 0, checkInTime: '14:00', checkOutTime: '12:00', sort: 0 }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    if (formData.value.id) {
      await StoreApi.updateStore(formData.value)
    } else {
      await StoreApi.createStore(formData.value)
    }
    message.success('保存成功')
    dialogVisible.value = false
    await getList()
  } finally {
    saving.value = false
  }
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await StoreApi.deleteStore(id)
    message.success('删除成功')
    await getList()
  } catch {}
}

onMounted(getList)
</script>
