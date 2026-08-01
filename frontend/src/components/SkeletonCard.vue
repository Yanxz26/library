<template>
  <div class="skeleton-card">
    <div class="skeleton-icon"></div>
    <div class="skeleton-content">
      <div class="skeleton-line skeleton-label"></div>
      <div class="skeleton-line skeleton-value"></div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.skeleton-card {
  padding: var(--space-lg);
  background: var(--bg-card);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  gap: var(--space-lg);
  box-shadow: var(--shadow-sm);

  .skeleton-icon {
    width: 60px;
    height: 60px;
    border-radius: var(--radius-md);
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: shimmer 1.5s infinite;
    flex-shrink: 0;
  }

  .skeleton-content {
    flex: 1;
    .skeleton-line {
      border-radius: 4px;
      background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
      background-size: 200% 100%;
      animation: shimmer 1.5s infinite;
    }
    .skeleton-label {
      width: 60%;
      height: 14px;
      margin-bottom: 8px;
    }
    .skeleton-value {
      width: 40%;
      height: 28px;
    }
  }
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
</style>
