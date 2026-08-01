<template>
  <div class="page-header">
    <div class="page-header-left">
      <h2>{{ title }}</h2>
      <slot name="extra" />
    </div>
    <div class="page-header-right">
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, required: true },
})
</script>

<style scoped lang="scss">
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-lg);
  flex-wrap: wrap;
  gap: var(--space-md);

  &-left {
    display: flex;
    align-items: center;
    gap: var(--space-md);

    h2 {
      font-size: var(--font-size-xl);
      font-weight: 600;
      color: var(--text-primary);
      display: flex;
      align-items: center;
      gap: var(--space-sm);

      &::before {
        content: '';
        display: inline-block;
        width: 4px;
        height: 20px;
        border-radius: 2px;
        background: var(--color-primary);
      }
    }
  }

  &-right {
    display: flex;
    align-items: center;
    gap: var(--space-sm);
    flex-wrap: wrap;
  }
}
</style>
