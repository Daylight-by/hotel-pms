<template>
  <ContentWrap>
    <!-- 搜索栏 -->
    <el-form class="search-form" :model="queryParams" inline>
      <el-form-item label="模块">
        <el-select v-model="queryParams.module" placeholder="全部模块" clearable style="width: 150px">
          <el-option label="账务" value="folio" />
          <el-option label="订单" value="order" />
          <el-option label="房价" value="rate" />
          <el-option label="夜审" value="audit" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作">
        <el-input v-model="queryParams.operation" placeholder="如：冲销 / 入账" clearable style="width: 150px" />
      </el-form-item>
      <el-form-item label="单据号">
        <el-input v-model="queryParams.bizId" placeholder="如：F20261021000001" clearable style="width: 180px" />
      </el-form-item>
      <el-form-item label="操作人">
        <el-input v-model="queryParams.operatorName" placeholder="操作人姓名" clearable style="width: 130px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="handleQuery">
          <Icon icon="ep:search" class="mr-5px" /> 搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" class="mr-5px" /> 重置
        </el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="list" border stripe>
      <el-table-column type="expand">
        <template #default="{ row }">
          <div class="diff-box">
            <div class="diff-col">
              <div class="diff-title">变更前 beforeData</div>
              <pre class="diff-pre">{{ pretty(row.beforeData) }}</pre>
            </div>
            <div class="diff-col">
              <div class="diff-title">变更后 afterData</div>
              <pre class="diff-pre">{{ pretty(row.afterData) }}</pre>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="id" label="编号" width="90" />
      <el-table-column label="模块" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="moduleType(row.module)">{{ moduleName(row.module) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="operation" label="操作" width="110" />
      <el-table-column prop="bizId" label="业务单据号" width="180" show-overflow-tooltip />
      <el-table-column prop="operatorName" label="操作人" width="100">
        <template #default="{ row }">{{ row.operatorName || row.operatorId || '系统' }}</template>
      </el-table-column>
      <el-table-column prop="ip" label="IP" width="130" />
      <el-table-column prop="createTime" label="操作时间" width="170" />
      <el-table-column label="变更数据" min-width="120">
        <template #default="{ row }">
          <el-tag size="small" :type="hasDiff(row) ? 'success' : 'info'">
            {{ hasDiff(row) ? '含前后数据' : '无' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>

    <Pagination
      v-model:total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { FormInstance } from 'element-plus'
import { getOperationLogPageApi } from '@/api/hotel/operationLog'

defineOptions({ name: 'HotelOperationLog' })

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 20,
  module: undefined as string | undefined,
  operation: undefined as string | undefined,
  bizId: undefined as string | undefined,
  operatorName: undefined as string | undefined
})

const getList = async () => {
  loading.value = true
  try {
    const data = await getOperationLogPageApi(queryParams)
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
  queryParams.module = undefined
  queryParams.operation = undefined
  queryParams.bizId = undefined
  queryParams.operatorName = undefined
  handleQuery()
}

const moduleName = (m: string) =>
  ({ folio: '账务', order: '订单', rate: '房价', audit: '夜审', ar: '挂账' } as any)[m] || m || '--'
const moduleType = (m: string) =>
  ({ folio: 'success', order: 'primary', rate: 'warning', audit: 'danger', ar: 'info' } as any)[m] || 'info'
const hasDiff = (row: any) => !!row.beforeData || !!row.afterData
const pretty = (s: string) => {
  if (!s) return '—'
  try {
    return JSON.stringify(JSON.parse(s), null, 2)
  } catch {
    return s
  }
}

onMounted(getList)
</script>

<style scoped lang="scss">
.search-form {
  :deep(.el-form-item) { margin-bottom: 12px; }
}
.diff-box {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
  padding: 8px 10px 14px 60px;
  .diff-col {
    background: #f8fafc;
    border: 1px solid #e5eaf3;
    border-radius: 6px;
    padding: 8px 12px;
  }
  .diff-title {
    font-size: 12px;
    color: #8a94a6;
    margin-bottom: 6px;
  }
  .diff-pre {
    margin: 0;
    font-size: 12px;
    line-height: 1.6;
    color: #334155;
    white-space: pre-wrap;
    word-break: break-all;
    max-height: 260px;
    overflow: auto;
  }
}
</style>
