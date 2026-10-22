<template>
  <ContentWrap>
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="76px"
    >
      <el-form-item label="姓名" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入姓名"
          clearable
          class="!w-200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="手机号" prop="mobile">
        <el-input
          v-model="queryParams.mobile"
          placeholder="请输入手机号"
          clearable
          class="!w-200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['hotel:guest:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="编号" align="center" prop="id" width="80" />
      <el-table-column label="姓名" align="center" prop="name" width="120" />
      <el-table-column label="手机号" align="center" prop="mobile" width="140" />
      <el-table-column label="身份证号" align="center" prop="idCard" min-width="180" />
      <el-table-column label="邮箱" align="center" prop="email" min-width="180" show-overflow-tooltip />
      <el-table-column label="性别" align="center" prop="sex" width="80">
        <template #default="scope">{{ sexLabel(scope.row.sex) }}</template>
      </el-table-column>
      <el-table-column label="出生日期" align="center" prop="birthday" width="120" />
      <el-table-column label="累计订单" align="center" prop="orderCount" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.orderCount" type="warning">{{ scope.row.orderCount }}</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" min-width="160" show-overflow-tooltip />
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        width="180"
        :formatter="dateFormatter"
      />
      <el-table-column label="操作" align="center" width="200" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['hotel:guest:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="success"
            @click="handleCreateOrder(scope.row)"
            v-hasPermi="['hotel:order:create']"
          >
            下单
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['hotel:guest:delete']"
          >
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

  <GuestForm ref="formRef" @success="getList" />
</template>
<script lang="ts" setup>
import { useRouter } from 'vue-router'
import { dateFormatter } from '@/utils/formatTime'
import * as GuestApi from '@/api/hotel/guest'
import GuestForm from './GuestForm.vue'

defineOptions({ name: 'HotelGuest' })

const message = useMessage()
const router = useRouter()

const loading = ref(true)
const total = ref(0)
const list = ref<GuestApi.GuestVO[]>([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: '',
  mobile: ''
})
const queryFormRef = ref()

const sexLabel = (sex?: number) => (sex === 1 ? '男' : sex === 2 ? '女' : '-')

const getList = async () => {
  loading.value = true
  try {
    const data = await GuestApi.getGuestPage(queryParams)
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
  queryFormRef.value.resetFields()
  handleQuery()
}

const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

/** 跳转订单页并预填住客 */
const handleCreateOrder = (row: GuestApi.GuestVO) => {
  router.push({ name: 'HotelOrder', query: { guestId: String(row.id) } })
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await GuestApi.deleteGuest(id)
    message.success('删除成功')
    await getList()
  } catch {}
}

onMounted(() => {
  getList()
})
</script>
