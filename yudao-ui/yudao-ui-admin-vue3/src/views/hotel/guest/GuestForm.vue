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
          <el-form-item label="姓名" prop="name">
            <el-input v-model="formData.name" placeholder="请输入姓名" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="手机号" prop="mobile">
            <el-input v-model="formData.mobile" placeholder="请输入手机号" maxlength="11" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="身份证号" prop="idCard">
        <el-input v-model="formData.idCard" placeholder="请输入身份证号" maxlength="18">
          <template #append>
            <el-button :loading="reading" @click="readIdCard">读取身份证</el-button>
          </template>
        </el-input>
      </el-form-item>
      <el-row>
        <el-col :span="12">
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="formData.email" placeholder="请输入邮箱" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="性别" prop="sex">
            <el-radio-group v-model="formData.sex">
              <el-radio :value="1">男</el-radio>
              <el-radio :value="2">女</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="出生日期" prop="birthday">
        <el-date-picker
          v-model="formData.birthday"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="请选择出生日期"
          class="!w-full"
        />
      </el-form-item>
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
import * as GuestApi from '@/api/hotel/guest'
import request from '@/config/axios'

defineOptions({ name: 'HotelGuestForm' })

const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref<GuestApi.GuestVO>({
  id: undefined,
  name: '',
  mobile: '',
  idCard: '',
  email: '',
  sex: 1,
  birthday: '',
  remark: ''
})
const formRules = reactive({
  name: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
  mobile: [{ pattern: /^$|^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
})
const formRef = ref()
const reading = ref(false)

/** 优先连本机读卡 Agent（ws://127.0.0.1:17888），失败回退后端 Mock */
const readIdCard = async () => {
  reading.value = true
  try {
    const data = await new Promise<any>((resolve, reject) => {
      let done = false
      try {
        const ws = new WebSocket('ws://127.0.0.1:17888')
        const timer = setTimeout(() => {
          if (!done) { done = true; ws.close(); reject(new Error('timeout')) }
        }, 2000)
        ws.onopen = () => ws.send(JSON.stringify({ cmd: 'read' }))
        ws.onmessage = (event) => {
          if (done) return
          done = true
          clearTimeout(timer)
          ws.close()
          try {
            const result = JSON.parse(event.data)
            result.ok ? resolve(result.data) : reject(new Error(result.error || 'read failed'))
          } catch (e) { reject(e) }
        }
        ws.onerror = () => {
          if (!done) { done = true; clearTimeout(timer); reject(new Error('ws error')) }
        }
      } catch (e) { reject(e) }
    })
    applyCard(data)
  } catch (e) {
    // 本机 Agent 未启动时走后端 Mock，保证演示可用
    const data = await request.post({ url: '/hotel/device/id-card/parse', data: {} })
    applyCard(data)
  } finally {
    reading.value = false
  }
}

const applyCard = (data: any) => {
  if (!data) return
  if (data.name) formData.value.name = data.name
  if (data.idCard) formData.value.idCard = data.idCard
  if (data.sex) formData.value.sex = data.sex
  if (data.birthday) formData.value.birthday = String(data.birthday).slice(0, 10)
  message.success('读卡成功，已回填身份信息')
}

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = type === 'create' ? '新增住客' : '编辑住客'
  formType.value = type
  resetForm()
  if (id) {
    formLoading.value = true
    try {
      formData.value = await GuestApi.getGuest(id)
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
      await GuestApi.createGuest(formData.value)
      message.success('新增成功')
    } else {
      await GuestApi.updateGuest(formData.value)
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
    mobile: '',
    idCard: '',
    email: '',
    sex: 1,
    birthday: '',
    remark: ''
  }
  formRef.value?.resetFields()
}
</script>
