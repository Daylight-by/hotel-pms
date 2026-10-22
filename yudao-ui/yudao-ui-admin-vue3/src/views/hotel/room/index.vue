<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="76px"
    >
      <el-form-item label="房间号" prop="roomNo">
        <el-input
          v-model="queryParams.roomNo"
          placeholder="请输入房间号"
          clearable
          class="!w-180px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="房型" prop="roomTypeId">
        <el-select
          v-model="queryParams.roomTypeId"
          placeholder="请选择房型"
          clearable
          class="!w-180px"
        >
          <el-option
            v-for="item in roomTypeList"
            :key="item.id"
            :label="item.name"
            :value="item.id!"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="楼层" prop="floor">
        <el-input-number
          v-model="queryParams.floor"
          :min="1"
          :max="99"
          controls-position="right"
          placeholder="楼层"
          class="!w-140px"
        />
      </el-form-item>
      <el-form-item label="房态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择房态" clearable class="!w-160px">
          <el-option
            v-for="item in ROOM_STATUS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['hotel:room:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="房间号" align="center" prop="roomNo" width="110" />
      <el-table-column label="房型" align="center" prop="roomTypeName" min-width="140" />
      <el-table-column label="楼层" align="center" prop="floor" width="80" />
      <el-table-column label="面积（㎡）" align="center" prop="area" width="100" />
      <el-table-column label="售价" align="center" prop="price" width="120">
        <template #default="scope">¥ {{ formatMoney(scope.row.price) }}</template>
      </el-table-column>
      <el-table-column label="房态" align="center" prop="status" width="110">
        <template #default="scope">
          <el-tag :type="tagTypeOf(ROOM_STATUS, scope.row.status)">
            {{ scope.row.statusName || labelOf(ROOM_STATUS, scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="锁定状态" align="center" width="110">
        <template #default="scope">
          <el-tag v-if="scope.row.lockStatus" type="warning" effect="dark">
            {{ scope.row.lockStatusName || (scope.row.lockStatus === 1 ? '维修锁定' : '预留锁定') }}
          </el-tag>
          <span v-else class="text-gray-400">正常</span>
        </template>
      </el-table-column>
      <el-table-column label="当前住客" align="center" prop="currentGuest" width="120">
        <template #default="scope">{{ scope.row.currentGuest || '-' }}</template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" min-width="160" show-overflow-tooltip />
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        width="180"
        :formatter="dateFormatter"
      />
      <el-table-column label="操作" align="center" width="290" fixed="right">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['hotel:room:update']"
          >
            编辑
          </el-button>
          <el-dropdown
            class="ml-2"
            @command="(status: number) => handleStatusChange(scope.row, status)"
            v-hasPermi="['hotel:room:update']"
          >
            <el-button link type="warning">改房态</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="item in ROOM_STATUS"
                  :key="item.value"
                  :command="item.value"
                  :disabled="item.value === scope.row.status"
                >
                  {{ item.label }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-dropdown
            class="ml-2"
            @command="(lockStatus: number) => handleLockChange(scope.row, lockStatus)"
            v-hasPermi="['hotel:room:update']"
          >
            <el-button link type="info">锁定</el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="0" :disabled="!scope.row.lockStatus">解锁</el-dropdown-item>
                <el-dropdown-item command="1" :disabled="scope.row.lockStatus === 1">
                  维修锁定
                </el-dropdown-item>
                <el-dropdown-item command="2" :disabled="scope.row.lockStatus === 2">
                  预留锁定
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['hotel:room:delete']"
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
  <RoomForm ref="formRef" :room-type-list="roomTypeList" @success="getList" />
</template>
<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import { ROOM_STATUS, formatMoney, labelOf, tagTypeOf } from '@/utils/hotel'
import * as RoomApi from '@/api/hotel/room'
import * as RoomTypeApi from '@/api/hotel/roomType'
import RoomForm from './RoomForm.vue'

defineOptions({ name: 'HotelRoom' })

const message = useMessage()

const loading = ref(true)
const total = ref(0)
const list = ref<RoomApi.RoomVO[]>([])
const roomTypeList = ref<RoomTypeApi.RoomTypeVO[]>([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  roomNo: '',
  roomTypeId: undefined,
  floor: undefined,
  status: undefined
})
const queryFormRef = ref()

const getList = async () => {
  loading.value = true
  try {
    const data = await RoomApi.getRoomPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const getRoomTypeList = async () => {
  roomTypeList.value = await RoomTypeApi.getRoomTypeSimpleList()
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

/** 调整房态 */
const handleStatusChange = async (row: RoomApi.RoomVO, status: number) => {
  try {
    await message.confirm(`确认将房间 ${row.roomNo} 的房态改为「${labelOf(ROOM_STATUS, status)}」？`)
    await RoomApi.updateRoomStatus(row.id!, status)
    message.success('房态已更新')
    await getList()
  } catch {}
}

const LOCK_LABELS: Record<number, string> = { 0: '正常', 1: '维修锁定', 2: '预留锁定' }

const handleLockChange = async (row: RoomApi.RoomVO, lockStatus: number | string) => {
  const value = Number(lockStatus)
  try {
    await message.confirm(`确认将房间 ${row.roomNo} 的锁定状态改为「${LOCK_LABELS[value]}」？`)
    await RoomApi.updateRoomLockStatus(row.id!, value)
    message.success('锁定状态已更新')
    await getList()
  } catch {}
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await RoomApi.deleteRoom(id)
    message.success('删除成功')
    await getList()
  } catch {}
}

onMounted(async () => {
  await getRoomTypeList()
  await getList()
})
</script>
