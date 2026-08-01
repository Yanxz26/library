<template>
  <div class="skeleton-table">
    <div class="skeleton-toolbar">
      <div class="skeleton-line" style="width:120px;height:32px"></div>
      <div class="skeleton-line" style="width:80px;height:32px;margin-left:auto"></div>
    </div>
    <!-- 表头 -->
    <div class="skeleton-header">
      <div v-for="i in columns" :key="'h'+i" class="skeleton-line" :style="{ width: headerWidths[i-1] || '80px' }"></div>
    </div>
    <!-- 行 -->
    <div v-for="row in rows" :key="'r'+row" class="skeleton-row">
      <div v-for="i in columns" :key="'c'+i" class="skeleton-line" :style="{ width: cellWidths[i-1] || '60px' }"></div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  rows: { type: Number, default: 5 },
  columns: { type: Number, default: 6 },
  headerWidths: { type: Array, default: () => [] },
  cellWidths: { type: Array, default: () => [] },
})
</script>

<style scoped lang="scss">
.skeleton-table {
  .skeleton-toolbar {
    display: flex;
    gap: 12px;
    margin-bottom: 16px;
    align-items: center;
  }

  .skeleton-header {
    display: grid;
    gap: 12px;
    padding: 12px 0;
    border-bottom: 1px solid var(--border-light);
    margin-bottom: 8px;
  }

  .skeleton-row {
    display: grid;
    gap: 12px;
    padding: 14px 0;
    border-bottom: 1px solid var(--border-light);
  }

  .skeleton-header, .skeleton-row {
    grid-template-columns: repeat(auto-fit, minmax(60px, 1fr));
  }

  .skeleton-line {
    height: 16px;
    border-radius: 4px;
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: shimmer 1.5s infinite;
  }
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
</style>
