<template>
  <div class="error-result">
    <div class="error-icon">
      <el-icon :size="56" color="#EF4444"><WarningFilled /></el-icon>
    </div>
    <h3>{{ title }}</h3>
    <p v-if="description">{{ description }}</p>
    <div class="error-actions">
      <el-button @click="$emit('retry')" :loading="retrying" type="primary">
        <el-icon><Refresh /></el-icon> {{ retryText }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, default: '加载失败' },
  description: { type: String, default: '请检查网络连接后重试' },
  retryText: { type: String, default: '重新加载' },
  retrying: { type: Boolean, default: false },
})
defineEmits(['retry'])
</script>

<style scoped lang="scss">
.error-result {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-2xl) var(--space-lg);
  text-align: center;

  .error-icon {
    margin-bottom: var(--space-lg);
    opacity: 0.8;
  }

  h3 {
    font-size: var(--font-size-lg);
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: var(--space-xs);
  }

  p {
    font-size: var(--font-size-base);
    color: var(--text-secondary);
    max-width: 360px;
    margin-bottom: var(--space-lg);
  }

  .error-actions {
    display: flex;
    gap: var(--space-sm);
  }
}
</style>
