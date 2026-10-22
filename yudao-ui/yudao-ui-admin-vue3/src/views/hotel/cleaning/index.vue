<template>
  <ContentWrap>
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="76px"
    >
      <el-form-item label="房间" prop="roomId">
        <el-select v-model="queryParams.roomId" placeholder="请选择房间" clearable filterable class="!w-180px">
          <el-option
            v-for="item in roomList"
            :key="item.id"
            :label="item.roomNo + ' · ' + (item.roomTypeName || '')"
            :value="item.id!"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="任务类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="请选择" clearable class="!w-150px">
          <el-option
            v-for="item in CLEANING_TYPE"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="任务状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择" clearable class="!w-150px">
          <el-option
            v-for="item in CLEANING_STATUS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="计划日期" prop="planDate">
        <el-date-picker
          v-model="queryParams.planDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择日期"
          class="!w-160px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm" v-hasPermi="['hotel:cleaning:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新建任务
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="编号" align="center" prop="id" width="80" />
      <el-table-column label="房间号" align="center" prop="roomNo" width="110" />
      <el-table-column label="楼层" align="center" prop="floor" width="80" />
      <el-table-column label="任务类型" align="center" prop="type" width="120">
        <template #default="scope">
          {{ scope.row.typeName || labelOf(CLEANING_TYPE, scope.row.type) }}
        </template>
      </el-table-column>
      <el-table-column label="任务状态" align="center" prop="status" width="120">
        <template #default="scope">
          <el-tag :type="tagTypeOf(CLEANING_STATUS, scope.row.status)">
            {{ scope.row.statusName || labelOf(CLEANING_STATUS, scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="保洁人员" align="center" prop="assigneeName" width="120">
        <template #default="scope">{{ scope.row.assigneeName || '未指派' }}</template>
      </el-table-column>
      <el-table-column label="计划日期" align="center" prop="planDate" width="120" />
      <el-table-column
        label="完成时间"
        align="center"
        prop="finishTime"
        width="180"
        :formatter="dateFormatter"
      />
      <el-table-column label="备注" align="center" prop="remark" min-width="200" show-overflow-tooltip />
      <el-table-column label="操作" align="center" width="200" fixed="right">
        <template #default="scope">
          <el-button
            v-if="scope.row.status === 10"
            link
            type="warning"
            @click="handleStatus(scope.row, 20, '开始清洁')"
            v-hasPermi="['hotel:cleaning:update']"
          >
            开始
          </el-button>
          <el-button
            v-if="[10, 20].includes(scope.row.status)"
            link
            type="success"
            @click="handleStatus(scope.row, 30, '完成清洁')"
            v-hasPermi="['hotel:cleaning:update']"
          >
            完成
          </el-button>
          <el-button
            v-if="[10, 20].includes(scope.row.status)"
            link
            type="info"
            @click="openAssign(scope.row)"
            v-hasPermi="['hotel:cleaning:update']"
          >
            指派
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['hotel:cleaning:delete']"
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

  <CleaningTaskForm ref="formRef" :room-list="roomList" @success="getList" />
</template>
<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import { CLEANING_STATUS, CLEANING_TYPE, labelOf, tagTypeOf } from '@/utils/hotel'
import * as CleaningApi from '@/api/hotel/cleaning'
import * as RoomApi from '@/api/hotel/room'
import CleaningTaskForm from './CleaningTaskForm.vue'

defineOptions({ name: 'HotelCleaning' })

const message = useMessage()

const loading = ref(true)
const total = ref(0)
const list = ref<CleaningApi.CleaningTaskVO[]>([])
const roomList = ref<RoomApi.RoomVO[]>([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  roomId: undefined,
  type: undefined,
  status: undefined,
  planDate: undefined
})
const queryFormRef = ref()

const getList = async () => {
  loading.value = true
  try {
    const data = await CleaningApi.getCleaningTaskPage(queryParams)
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
const openForm = () => {
  formRef.value.open()
}

/** 流转任务状态 */
const handleStatus = async (row: CleaningApi.CleaningTaskVO, status: number, action: string) => {
  try {
    await message.confirm(`确认对房间 ${row.roomNo} 执行「${action}」？`)
    await CleaningApi.updateCleaningTaskStatus(row.id!, status)
    message.success(`${action}成功`)
    await getList()
  } catch {}
}

/** 指派保洁（用简单的输入弹窗） */
const openAssign = async (row: CleaningApi.CleaningTaskVO) => {
  try {
    const { value } = await message.prompt('请输入保洁人员编号', '指派保洁')
    if (!value) return
    await CleaningApi.assignCleaningTask(row.id!, Number(value))
    message.success('指派成功')
    await getList()
  } catch {}
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await CleaningApi.deleteCleaningTask(id)
    message.success('删除成功')
    await getList()
  } catch {}
}

onMounted(async () => {
  roomList.value = await RoomApi.getRoomSimpleList()
  await getList()
})
</script>
