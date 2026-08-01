<template>
  <div class="empty-state">
    <div class="empty-icon">
      <slot name="icon">
        <el-icon :size="64" color="#CDD1DE"><FolderOpened /></el-icon>
      </slot>
    </div>
    <div class="empty-text">
      <h3>{{ title }}</h3>
      <p v-if="description">{{ description }}</p>
    </div>
    <div v-if="$slots.action || actionText" class="empty-action">
      <slot name="action">
        <el-button type="primary" @click="$emit('action')">{{ actionText }}</el-button>
      </slot>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, default: '暂无数据' },
  description: { type: String, default: '' },
  actionText: { type: String, default: '' },
})
defineEmits(['action'])
</script>

<style scoped lang="scss">
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-2xl) var(--space-lg);
  text-align: center;

  .empty-icon {
    margin-bottom: var(--space-lg);
    opacity: 0.6;
  }

  .empty-text {
    h3 {
      font-size: var(--font-size-lg);
      font-weight: 600;
      color: var(--text-primary);
      margin-bottom: var(--space-xs);
    }

    p {
      font-size: var(--font-size-base);
      color: var(--text-secondary);
      max-width: 320px;
    }
  }

  .empty-action {
    margin-top: var(--space-lg);
  }
}
</style>
