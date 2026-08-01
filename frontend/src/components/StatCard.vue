<template>
  <div class="stat-card" @click="handleClick" :class="{ clickable: clickable }">
    <div class="stat-icon" :style="{ background: iconBg }">
      <el-icon :size="iconSize"><component :is="icon" /></el-icon>
    </div>
    <div class="stat-info">
      <div class="stat-label">{{ label }}</div>
      <div class="stat-value">
        <slot name="value">{{ value }}</slot>
      </div>
      <div v-if="trend !== undefined" class="stat-trend" :class="trendClass">
        <el-icon :size="14">
          <CaretTop v-if="trend > 0" />
          <CaretBottom v-else-if="trend < 0" />
          <Minus v-else />
        </el-icon>
        <span>{{ Math.abs(trend) }}%</span>
        <span class="trend-label">{{ trendLabel }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  icon: { type: [String, Object], required: true },
  iconBg: { type: String, default: 'linear-gradient(135deg, #5B6AF0, #7B8AF5)' },
  iconSize: { type: Number, default: 28 },
  label: { type: String, required: true },
  value: { type: [String, Number], default: 0 },
  trend: { type: Number, default: undefined },
  trendLabel: { type: String, default: '较昨日' },
  clickable: { type: Boolean, default: false },
})

const emit = defineEmits(['click'])

function handleClick() {
  if (props.clickable) {
    emit('click')
  }
}

const trendClass = computed(() => {
  if (props.trend === undefined) return ''
  if (props.trend > 0) return 'up'
  if (props.trend < 0) return 'down'
  return 'flat'
})
</script>

<style scoped lang="scss">
.stat-card {
  padding: var(--space-lg);
  background: var(--bg-card);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  gap: var(--space-lg);
  box-shadow: var(--shadow-sm);
  transition: transform var(--transition-base), box-shadow var(--transition-base);

  &.clickable {
    cursor: pointer;
    &:hover {
      transform: translateY(-3px);
      box-shadow: var(--shadow-lg);
    }
  }

  .stat-icon {
    width: 60px;
    height: 60px;
    border-radius: var(--radius-md);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    flex-shrink: 0;
  }

  .stat-info {
    min-width: 0;

    .stat-label {
      font-size: var(--font-size-base);
      color: var(--text-secondary);
      margin-bottom: var(--space-xs);
    }

    .stat-value {
      font-size: 28px;
      font-weight: 700;
      color: var(--text-primary);
      line-height: 1.2;
    }

    .stat-trend {
      font-size: var(--font-size-xs);
      display: flex;
      align-items: center;
      gap: 2px;
      margin-top: var(--space-xs);

      &.up { color: var(--color-success); }
      &.down { color: var(--color-danger); }
      &.flat { color: var(--text-secondary); }

      .trend-label {
        margin-left: 4px;
        color: var(--text-secondary);
      }
    }
  }
}
</style>
