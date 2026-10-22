<template>
  <Dialog v-model="dialogVisible" title="新建清洁任务" width="560">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="90px"
    >
      <el-form-item label="房间" prop="roomId">
        <el-select v-model="formData.roomId" placeholder="请选择房间" filterable class="!w-full">
          <el-option
            v-for="item in props.roomList"
            :key="item.id"
            :label="`${item.roomNo} · ${item.roomTypeName || ''}`"
            :value="item.id!"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="任务类型" prop="type">
        <el-select v-model="formData.type" placeholder="请选择任务类型" class="!w-full">
          <el-option
            v-for="item in CLEANING_TYPE"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="计划日期" prop="planDate">
        <el-date-picker
          v-model="formData.planDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择计划日期"
          class="!w-full"
        />
      </el-form-item>
      <el-form-item label="保洁人员编号" prop="assigneeId">
        <el-input-number v-model="formData.assigneeId" :min="1" class="!w-full" placeholder="可留空稍后指派" />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :disabled="formLoading" type="primary" @click="submitForm">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script lang="ts" setup>
import { CLEANING_TYPE } from '@/utils/hotel'
import * as CleaningApi from '@/api/hotel/cleaning'
import * as RoomApi from '@/api/hotel/room'

defineOptions({ name: 'HotelCleaningTaskForm' })

const props = defineProps<{
  roomList: RoomApi.RoomVO[]
}>()

const message = useMessage()

const dialogVisible = ref(false)
const formLoading = ref(false)
const formData = ref<CleaningApi.CleaningTaskVO>({
  roomId: undefined,
  type: 10,
  planDate: '',
  assigneeId: undefined,
  remark: ''
})
const formRules = reactive({
  roomId: [{ required: true, message: '请选择房间', trigger: 'change' }],
  type: [{ required: true, message: '请选择任务类型', trigger: 'change' }],
  planDate: [{ required: true, message: '请选择计划日期', trigger: 'change' }]
})
const formRef = ref()

const open = () => {
  dialogVisible.value = true
  formData.value = {
    roomId: undefined,
    type: 10,
    planDate: new Date().toISOString().slice(0, 10),
    assigneeId: undefined,
    remark: ''
  }
  formRef.value?.resetFields()
}
defineExpose({ open })

const emit = defineEmits(['success'])
const submitForm = async () => {
  if (!formRef) return
  const valid = await formRef.value.validate()
  if (!valid) return
  formLoading.value = true
  try {
    await CleaningApi.createCleaningTask(formData.value)
    message.success('清洁任务已创建')
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}
</script>
