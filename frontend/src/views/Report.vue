<template>
  <div class="report-page">
    <!-- Top Filter Bar -->
    <div class="filter-bar">
      <div class="filter-left">
        <a-select
          v-model:value="selectedProjectId"
          placeholder="选择项目"
          style="width: 200px"
          @change="onProjectChange"
        >
          <a-select-option v-for="p in projects" :key="p.id" :value="p.id">
            {{ p.name }}
          </a-select-option>
        </a-select>
        <a-select
          v-model:value="selectedSprintId"
          placeholder="选择冲刺"
          style="width: 200px"
          allow-clear
          @change="onSprintChange"
        >
          <a-select-option v-for="s in sprints" :key="s.id" :value="s.id">
            {{ s.name }}
          </a-select-option>
        </a-select>
        <a-range-picker
          v-model:value="dateRange"
          @change="onDateChange"
        />
      </div>
    </div>

    <!-- Charts Grid -->
    <div class="charts-grid">
      <!-- Top Left: Project Progress Pie -->
      <div class="chart-card">
        <div class="chart-title">项目进度</div>
        <div class="chart-container" v-if="selectedProjectId">
          <v-chart :option="progressOption" autoresize class="chart" />
        </div>
        <a-empty v-else description="请选择项目" style="margin-top: 60px" />
      </div>

      <!-- Top Right: Task Distribution Bar -->
      <div class="chart-card">
        <div class="chart-title">任务状态分布</div>
        <div class="chart-container" v-if="selectedProjectId">
          <v-chart :option="distributionOption" autoresize class="chart" />
        </div>
        <a-empty v-else description="请选择项目" style="margin-top: 60px" />
      </div>

      <!-- Bottom Left: Burndown Line -->
      <div class="chart-card">
        <div class="chart-title">燃尽图</div>
        <div class="chart-container" v-if="selectedSprintId">
          <v-chart :option="burndownOption" autoresize class="chart" />
        </div>
        <a-empty v-else description="请选择冲刺" style="margin-top: 60px" />
      </div>

      <!-- Bottom Right: Member Workload Horizontal Bar -->
      <div class="chart-card">
        <div class="chart-title">成员工作量</div>
        <div class="chart-container" v-if="selectedProjectId">
          <v-chart :option="workloadOption" autoresize class="chart" />
        </div>
        <a-empty v-else description="请选择项目" style="margin-top: 60px" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { PieChart, BarChart, LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { listProjects } from '@/api/project'
import { listSprints } from '@/api/sprint'
import {
  getProjectProgress,
  getTaskDistribution,
  getBurndown,
  getMemberWorkload
} from '@/api/report'
import type { Project, Sprint, ProjectProgress, TaskDistribution, Burndown as BurndownType, MemberWorkload } from '@/types'
import type { Dayjs } from 'dayjs'

use([
  PieChart,
  BarChart,
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  CanvasRenderer
])

// Filter state
const projects = ref<Project[]>([])
const sprints = ref<Sprint[]>([])
const selectedProjectId = ref<number | undefined>(undefined)
const selectedSprintId = ref<number | undefined>(undefined)
const dateRange = ref<[Dayjs, Dayjs] | null>(null)

// Chart data
const progressData = ref<ProjectProgress | null>(null)
const distributionData = ref<TaskDistribution | null>(null)
const burndownData = ref<BurndownType | null>(null)
const workloadData = ref<MemberWorkload[]>([])

// Chart options
const progressOption = computed(() => {
  if (!progressData.value) return {}
  const total = progressData.value.totalTasks
  const done = progressData.value.doneTasks
  const remaining = total - done
  return {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      bottom: 0,
      itemWidth: 12,
      itemHeight: 12
    },
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 4,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        formatter: '{b}\n{d}%'
      },
      data: [
        { value: done, name: '已完成', itemStyle: { color: '#52C41A' } },
        { value: remaining, name: '剩余', itemStyle: { color: '#E8F1FB' } }
      ]
    }]
  }
})

const distributionOption = computed(() => {
  if (!distributionData.value) return {}
  const d = distributionData.value
  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: 40,
      right: 20,
      bottom: 30,
      top: 20
    },
    xAxis: {
      type: 'category',
      data: ['待办', '进行中', '评审中', '已完成'],
      axisLabel: { color: '#8c8c8c' }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#8c8c8c' },
      splitLine: { lineStyle: { type: 'dashed', color: '#f0f0f0' } }
    },
    series: [{
      type: 'bar',
      barWidth: '40%',
      data: [
        { value: d.todoCount, itemStyle: { color: '#5B9BD5' } },
        { value: d.inProgressCount, itemStyle: { color: '#FAAD14' } },
        { value: d.inReviewCount, itemStyle: { color: '#722ED1' } },
        { value: d.doneCount, itemStyle: { color: '#52C41A' } }
      ]
    }]
  }
})

const burndownOption = computed(() => {
  if (!burndownData.value) return {}
  const d = burndownData.value
  return {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      bottom: 0,
      data: ['理想剩余', '实际剩余']
    },
    grid: {
      left: 40,
      right: 20,
      bottom: 40,
      top: 20
    },
    xAxis: {
      type: 'category',
      data: d.dates,
      axisLabel: { color: '#8c8c8c', rotate: 30 }
    },
    yAxis: {
      type: 'value',
      axisLabel: { color: '#8c8c8c' },
      splitLine: { lineStyle: { type: 'dashed', color: '#f0f0f0' } }
    },
    series: [
      {
        name: '理想剩余',
        type: 'line',
        data: d.idealRemaining,
        lineStyle: { type: 'dashed', color: '#d9d9d9' },
        itemStyle: { color: '#d9d9d9' },
        symbol: 'none'
      },
      {
        name: '实际剩余',
        type: 'line',
        data: d.actualRemaining,
        lineStyle: { color: '#5B9BD5', width: 2 },
        itemStyle: { color: '#5B9BD5' },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(91,155,213,0.25)' },
              { offset: 1, color: 'rgba(91,155,213,0.02)' }
            ]
          }
        }
      }
    ]
  }
})

const workloadOption = computed(() => {
  if (!workloadData.value || workloadData.value.length === 0) return {}
  const data = workloadData.value
  const names = data.map(m => m.username)
  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    legend: {
      bottom: 0,
      itemWidth: 12,
      itemHeight: 12
    },
    grid: {
      left: 80,
      right: 20,
      bottom: 40,
      top: 20
    },
    xAxis: {
      type: 'value',
      axisLabel: { color: '#8c8c8c' },
      splitLine: { lineStyle: { type: 'dashed', color: '#f0f0f0' } }
    },
    yAxis: {
      type: 'category',
      data: names,
      axisLabel: { color: '#595959' }
    },
    series: [
      {
        name: '待办',
        type: 'bar',
        stack: 'total',
        data: data.map(m => m.todoCount),
        itemStyle: { color: '#5B9BD5' }
      },
      {
        name: '进行中',
        type: 'bar',
        stack: 'total',
        data: data.map(m => m.inProgressCount),
        itemStyle: { color: '#FAAD14' }
      },
      {
        name: '已完成',
        type: 'bar',
        stack: 'total',
        data: data.map(m => m.doneCount),
        itemStyle: { color: '#52C41A' }
      }
    ]
  }
})

onMounted(async () => {
  try {
    const res = await listProjects()
    projects.value = res.data
    if (projects.value.length > 0) {
      selectedProjectId.value = projects.value[0].id
      await loadProjectData()
    }
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '加载项目列表失败')
  }
})

async function loadProjectData() {
  if (!selectedProjectId.value) return
  try {
    // Load sprints for project
    const sprintsRes = await listSprints(selectedProjectId.value)
    sprints.value = sprintsRes.data

    // Load chart data in parallel
    const [progressRes, distRes, workloadRes] = await Promise.allSettled([
      getProjectProgress(selectedProjectId.value),
      getTaskDistribution(selectedProjectId.value),
      getMemberWorkload(selectedProjectId.value)
    ])

    if (progressRes.status === 'fulfilled') {
      progressData.value = progressRes.value.data
    }
    if (distRes.status === 'fulfilled') {
      distributionData.value = distRes.value.data
    }
    if (workloadRes.status === 'fulfilled') {
      workloadData.value = workloadRes.value.data
    }

    // Auto-select first sprint if available
    if (sprints.value.length > 0) {
      selectedSprintId.value = sprints.value[0].id
      await loadSprintData()
    }
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '加载数据失败')
  }
}

async function loadSprintData() {
  if (!selectedSprintId.value) {
    burndownData.value = null
    return
  }
  try {
    const res = await getBurndown(selectedSprintId.value)
    burndownData.value = res.data
  } catch (err: unknown) {
    message.error(err instanceof Error ? err.message : '加载燃尽图数据失败')
  }
}

async function onProjectChange() {
  progressData.value = null
  distributionData.value = null
  workloadData.value = []
  burndownData.value = null
  selectedSprintId.value = undefined
  await loadProjectData()
}

async function onSprintChange() {
  await loadSprintData()
}

function onDateChange() {
  // Date range filter can be used for future enhancements
}
</script>

<style scoped>
.report-page {
  display: flex;
  flex-direction: column;
}

.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.filter-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.charts-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.chart-card {
  background: #fafbfc;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  padding: 16px;
  min-height: 320px;
}

.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 12px;
}

.chart-container {
  width: 100%;
  height: 260px;
}

.chart {
  width: 100%;
  height: 100%;
}
</style>
