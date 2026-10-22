<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="680">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="100px"
    >
      <el-row>
        <el-col :span="12">
          <el-form-item label="房型名称" prop="name">
            <el-input v-model="formData.name" placeholder="请输入房型名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="房型编码" prop="code">
            <el-input v-model="formData.code" placeholder="如 DOUBLE / TWIN" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="基础价格" prop="basePrice">
            <el-input-number
              v-model="formData.basePrice"
              :min="0.01"
              :precision="2"
              :step="10"
              class="!w-full"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="可住人数" prop="maxOccupancy">
            <el-input-number v-model="formData.maxOccupancy" :min="1" :max="20" class="!w-full" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="面积（㎡）" prop="area">
            <el-input-number v-model="formData.area" :min="0" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="排序" prop="sort">
            <el-input-number v-model="formData.sort" :min="0" class="!w-full" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="钟点房价" prop="hourlyPrice">
            <el-input-number v-model="formData.hourlyPrice" :min="0" :precision="2" :step="10" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="钟点时长(h)" prop="hourlyHours">
            <el-input-number v-model="formData.hourlyHours" :min="1" :max="24" class="!w-full" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="开放钟点房" prop="hourlyEnabled">
            <el-switch v-model="formData.hourlyEnabled" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="建议押金" prop="deposit">
            <el-input-number v-model="formData.deposit" :min="0" :precision="2" :step="50" class="!w-full" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="设施" prop="amenities">
        <el-input v-model="formData.amenities" placeholder="逗号分隔，如 WiFi,空调,电视" />
      </el-form-item>
      <el-form-item label="描述" prop="description">
        <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入描述" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio :value="0">开启</el-radio>
          <el-radio :value="1">关闭</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :disabled="formLoading" type="primary" @click="submitForm">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script lang="ts" setup>
import * as RoomTypeApi from '@/api/hotel/roomType'

defineOptions({ name: 'HotelRoomTypeForm' })

const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref<RoomTypeApi.RoomTypeVO>({
  id: undefined,
  name: '',
  code: '',
  basePrice: undefined,
  hourlyPrice: undefined,
  hourlyHours: 3,
  hourlyEnabled: false,
  deposit: 0,
  maxOccupancy: 2,
  area: undefined,
  amenities: '',
  description: '',
  status: 0,
  sort: 0
})
const formRules = reactive({
  name: [{ required: true, message: '房型名称不能为空', trigger: 'blur' }],
  code: [{ required: true, message: '房型编码不能为空', trigger: 'blur' }],
  basePrice: [{ required: true, message: '基础价格不能为空', trigger: 'change' }]
})
const formRef = ref()

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增房型' : '编辑房型'
  formType.value = type
  resetForm()
  if (id) {
    formLoading.value = true
    try {
      formData.value = await RoomTypeApi.getRoomType(id)
    } finally {
      formLoading.value = false
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
      await RoomTypeApi.createRoomType(formData.value)
      message.success('新增成功')
    } else {
      await RoomTypeApi.updateRoomType(formData.value)
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
    name: '',
    code: '',
    basePrice: undefined,
    hourlyPrice: undefined,
    hourlyHours: 3,
    hourlyEnabled: false,
    deposit: 0,
    maxOccupancy: 2,
    area: undefined,
    amenities: '',
    description: '',
    status: 0,
    sort: 0
  }
  formRef.value?.resetFields()
}
</script>
