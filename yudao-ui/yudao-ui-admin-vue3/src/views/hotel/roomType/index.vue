<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="80px"
    >
      <el-form-item label="房型名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入房型名称"
          clearable
          class="!w-240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="房型编码" prop="code">
        <el-input
          v-model="queryParams.code"
          placeholder="请输入房型编码"
          clearable
          class="!w-240px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="!w-240px">
          <el-option label="开启" :value="0" />
          <el-option label="关闭" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['hotel:room-type:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="编号" align="center" prop="id" width="80" />
      <el-table-column label="房型名称" align="center" prop="name" min-width="140" />
      <el-table-column label="编码" align="center" prop="code" width="120" />
      <el-table-column label="基础价格" align="center" prop="basePrice" width="120">
        <template #default="scope">¥ {{ formatMoney(scope.row.basePrice) }}</template>
      </el-table-column>
      <el-table-column label="钟点房" align="center" width="140">
        <template #default="scope">
          <el-tag v-if="scope.row.hourlyEnabled" type="warning" effect="plain">
            ¥{{ formatMoney(scope.row.hourlyPrice) }} / {{ scope.row.hourlyHours }}h
          </el-tag>
          <span v-else class="text-gray-400">未开放</span>
        </template>
      </el-table-column>
      <el-table-column label="建议押金" align="center" width="110">
        <template #default="scope">¥ {{ formatMoney(scope.row.deposit) }}</template>
      </el-table-column>
      <el-table-column label="可住人数" align="center" prop="maxOccupancy" width="100" />
      <el-table-column label="面积（㎡）" align="center" prop="area" width="100" />
      <el-table-column label="房间数" align="center" prop="roomCount" width="90" />
      <el-table-column label="设施" align="center" prop="amenities" min-width="200" show-overflow-tooltip />
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.status === 0 ? 'success' : 'info'">
            {{ scope.row.status === 0 ? '开启' : '关闭' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="排序" align="center" prop="sort" width="80" />
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        width="180"
        :formatter="dateFormatter"
      />
      <el-table-column label="操作" align="center" width="150" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['hotel:room-type:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['hotel:room-type:delete']"
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

  <!-- 表单弹窗 -->
  <RoomTypeForm ref="formRef" @success="getList" />
</template>
<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import { formatMoney } from '@/utils/hotel'
import * as RoomTypeApi from '@/api/hotel/roomType'
import RoomTypeForm from './RoomTypeForm.vue'

defineOptions({ name: 'HotelRoomType' })

const message = useMessage()

const loading = ref(true)
const total = ref(0)
const list = ref<RoomTypeApi.RoomTypeVO[]>([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: '',
  code: '',
  status: undefined
})
const queryFormRef = ref()

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await RoomTypeApi.getRoomTypePage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 搜索 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

/** 打开表单 */
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

/** 删除 */
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await RoomTypeApi.deleteRoomType(id)
    message.success('删除成功')
    await getList()
  } catch {}
}

onMounted(() => {
  getList()
})
</script>
