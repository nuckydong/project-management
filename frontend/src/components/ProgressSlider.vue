<template>
  <div class="progress-slider" :class="{ mini: size === 'mini' }">
    <div
      class="slider-track"
      ref="trackRef"
      @mousedown="onMouseDown"
    >
      <div class="slider-fill" :style="{ width: progress + '%' }"></div>
      <div v-if="editable" class="slider-handle" :style="{ left: progress + '%' }"></div>
    </div>
    <span v-if="editable || size === 'mini'" class="slider-text">{{ progress }}%</span>
  </div>
</template>

<script setup lang="ts">
import { ref, onBeforeUnmount } from 'vue'

const props = withDefaults(defineProps<{
  progress: number
  editable?: boolean
  size?: 'default' | 'mini'
}>(), {
  editable: false,
  size: 'default'
})

const emit = defineEmits<{
  update: [value: number]
}>()

const trackRef = ref<HTMLElement | null>(null)
const dragging = ref(false)

function calcProgress(clientX: number): number {
  if (!trackRef.value) return props.progress
  const rect = trackRef.value.getBoundingClientRect()
  const ratio = (clientX - rect.left) / rect.width
  return Math.round(Math.min(100, Math.max(0, ratio * 100)))
}

function onMouseDown(e: MouseEvent) {
  if (!props.editable) return
  e.preventDefault()
  dragging.value = true
  const val = calcProgress(e.clientX)
  emit('update', val)

  const onMouseMove = (ev: MouseEvent) => {
    if (!dragging.value) return
    const v = calcProgress(ev.clientX)
    emit('update', v)
  }

  const onMouseUp = (ev: MouseEvent) => {
    dragging.value = false
    const v = calcProgress(ev.clientX)
    emit('update', v)
    document.removeEventListener('mousemove', onMouseMove)
    document.removeEventListener('mouseup', onMouseUp)
  }

  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
}

onBeforeUnmount(() => {
  dragging.value = false
})
</script>

<style scoped>
.progress-slider {
  display: flex;
  align-items: center;
  gap: 8px;
  user-select: none;
}

.slider-track {
  position: relative;
  width: 100%;
  height: 20px;
  background: #e8e8e8;
  border-radius: 20px;
  cursor: default;
}

.progress-slider:not(.mini) .slider-track {
  cursor: pointer;
}

.slider-fill {
  height: 100%;
  background: #52c41a;
  border-radius: 20px 0 0 20px;
  transition: width 0.1s ease;
  pointer-events: none;
}

.slider-handle {
  position: absolute;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 14px;
  height: 14px;
  background: #fff;
  border: 2px solid #52c41a;
  border-radius: 50%;
  pointer-events: none;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.15);
}

.slider-text {
  font-size: 13px;
  color: #595959;
  white-space: nowrap;
  min-width: 36px;
  text-align: right;
}

/* Mini mode for TaskCard */
.progress-slider.mini .slider-track {
  height: 4px;
}

.progress-slider.mini .slider-text {
  font-size: 11px;
  min-width: 30px;
}
</style>
