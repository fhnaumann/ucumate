<template>
  <ClickableUnit :label=label>
    <template #hover>
      Click to run validation
    </template>
  </ClickableUnit>
</template>
<script setup lang="ts">
import ClickableUnit from '@/components/clickableunit/ClickableUnit.vue'

defineProps<{ label: string }>()
</script>
