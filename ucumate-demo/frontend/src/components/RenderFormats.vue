<template>
  <div class="flex space-x-6">
    <p>Print options:</p>
    <div class="flex items-center justify-center space-x-1">
      <Checkbox id="ucum_syntax" :modelValue="modelValue.includes('ucum')" @update:modelValue="toggle('ucum')" />
      <label for="ucum_syntax" class="">
        UCUM Syntax
      </label>
    </div>
    <div class="flex items-center justify-center space-x-1">
      <Checkbox id="ucum_expressive_syntax" :model-value="modelValue.includes('ucum_expressive')" @update:modelValue="toggle('ucum_expressive')" />
      <label for="ucum_expressive_syntax" class="">
        UCUM Expressive Syntax
      </label>
    </div>
    <div class="flex items-center justify-center space-x-1">
      <Checkbox id="common_syntax" :model-value="modelValue.includes('common')" @update:modelValue="toggle('common')" />
      <label for="common_syntax" class="">
        Common Syntax
      </label>
    </div>
    <div class="flex items-center justify-center space-x-1">
      <Checkbox id="latex_syntax" :model-value="modelValue.includes('latex')" @update:modelValue="toggle('latex')" />
      <label for="latex_syntax" class="">
        LaTeX Syntax
      </label>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { Checkbox } from '@/components/ui/checkbox'
  import { ref } from 'vue'

  const props = defineProps<{
    modelValue: string[]
  }>()

  const emit = defineEmits<{
    (e: 'update:modelValue', value: string[]): void
  }>()

  function toggle(value: string) {
    const newValue = props.modelValue.includes(value)
      ? props.modelValue.filter(v => v !== value)
      : [...props.modelValue, value]
    emit('update:modelValue', newValue)
  }
</script>
