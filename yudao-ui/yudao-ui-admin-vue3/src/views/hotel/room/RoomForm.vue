<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="640">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="90px"
    >
      <el-row>
        <el-col :span="12">
          <el-form-item label="房间号" prop="roomNo">
            <el-input v-model="formData.roomNo" placeholder="如 301" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="房型" prop="roomTypeId">
            <el-select v-model="formData.roomTypeId" placeholder="请选择房型" class="!w-full">
              <el-option
                v-for="item in props.roomTypeList"
                :key="item.id"
                :label="item.name"
                :value="item.id!"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="楼层" prop="floor">
            <el-input-number v-model="formData.floor" :min="1" :max="99" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="面积（㎡）" prop="area">
            <el-input-number v-model="formData.area" :min="0" class="!w-full" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="售价" prop="price">
            <el-input-number
              v-model="formData.price"
              :min="0.01"
              :precision="2"
              :step="10"
              class="!w-full"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="房态" prop="status">
            <el-select v-model="formData.status" placeholder="请选择房态" class="!w-full">
              <el-option
                v-for="item in ROOM_STATUS"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :disabled="formLoading" type="primary" @click="submitForm">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script lang="ts" setup>
import { ROOM_STATUS } from '@/utils/hotel'
import * as RoomApi from '@/api/hotel/room'
import * as RoomTypeApi from '@/api/hotel/roomType'

defineOptions({ name: 'HotelRoomForm' })

const props = defineProps<{
  roomTypeList: RoomTypeApi.RoomTypeVO[]
}>()

const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref<RoomApi.RoomVO>({
  id: undefined,
  roomNo: '',
  roomTypeId: undefined,
  floor: undefined,
  area: undefined,
  price: undefined,
  status: 10,
  remark: ''
})
const formRules = reactive({
  roomNo: [{ required: true, message: '房间号不能为空', trigger: 'blur' }],
  roomTypeId: [{ required: true, message: '请选择房型', trigger: 'change' }],
  price: [{ required: true, message: '售价不能为空', trigger: 'change' }]
})
const formRef = ref()

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增房间' : '编辑房间'
  formType.value = type
  resetForm()
  if (id) {
    formLoading.value = true
    try {
      formData.value = await RoomApi.getRoom(id)
    } finally {
      formLoading.value = false
    }
  } else {
    // 新增时默认取第一个房型的门市价
    const first = props.roomTypeList?.[0]
    if (first) {
      formData.value.roomTypeId = first.id
      formData.value.price = first.basePrice
    }
  }
}
defineExpose({ open })

const emit = defineEmits(['success'])
const submitForm = async () => {
  if (!formRef) return
  const valid = await formRef.value.validate()
  if (!valid) return
  formLoading.value = true
  try {
    if (formType.value === 'create') {
      await RoomApi.createRoom(formData.value)
      message.success('新增成功')
    } else {
      await RoomApi.updateRoom(formData.value)
      message.success('修改成功')
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

const resetForm = () => {
  formData.value = {
    id: undefined,
    roomNo: '',
    roomTypeId: undefined,
    floor: undefined,
    area: undefined,
    price: undefined,
    status: 10,
    remark: ''
  }
  formRef.value?.resetFields()
}
</script>
