<template>
  <div class="page-container">
    <div class="search-form">
      <el-form inline>
        <el-form-item label="日志类型">
          <el-select v-model="logType" placeholder="全部" clearable style="width: 160px" @change="handleSearch">
            <el-option label="操作日志" :value="1" />
            <el-option label="业务日志" :value="2" />
            <el-option label="异常日志" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button :icon="Download" :loading="exporting" @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-container">
      <PageHeader :title="dict.label('logType', logType) || '全部日志'" />

      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="日志类型" width="100">
          <template #default="{ row }">
            <el-tag :type="dict.tag('logType', row.logType)" size="small">
              <span class="status-dot" :class="'dot-' + dict.tag('logType', row.logType)"></span>
              {{ dict.label('logType', row.logType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operation" label="操作描述" min-width="180" show-overflow-tooltip />
        <el-table-column prop="requestMethod" label="请求方式" width="90">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.requestMethod }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requestUrl" label="请求地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ipAddr" label="IP地址" width="140" />
        <el-table-column label="时间" width="170">
          <template #default="{ row }">{{ row.createTime || '-' }}</template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 20px; display: flex; justify-content: flex-end">
        <el-pagination
          v-model:current-page="queryForm.current"
          v-model:page-size="queryForm.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @change="fetchData"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onBeforeMount } from 'vue'
import { Download } from '@element-plus/icons-vue'
import { getLogPage, exportLogs } from '@/api/log'
import { useDict } from '@/composables/useDict'
import { useExport } from '@/composables/useExport'
import PageHeader from '@/components/PageHeader.vue'

const { label, tag } = useDict()
const dict = { label, tag }
const { exporting, handleExport: doExport } = useExport(exportLogs, { filePrefix: '系统日志' })

const logType = ref(null)
const dateRange = ref(null)
const queryForm = reactive({ current: 1, size: 20 })
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

async function fetchData() {
  loading.value = true
  try {
    const params = { ...queryForm, logType: logType.value }
    if (dateRange.value) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const res = await getLogPage(params)
    if (res?.data) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (err) {
    console.error('[LogView] fetchData error:', err)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryForm.current = 1
  fetchData()
}

async function handleExport() {
  await doExport({ logType: logType.value, startDate: dateRange.value?.[0], endDate: dateRange.value?.[1] })
}

onBeforeMount(() => { fetchData() })
</script>
