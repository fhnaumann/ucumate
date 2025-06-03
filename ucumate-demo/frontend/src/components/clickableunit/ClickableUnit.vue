<template>
  <HoverCard :open-delay="300">
    <HoverCardTrigger as-child>
      <Button class="cursor-pointer inline p-0 m-0" variant="link" @click="$emit('click')">{{ label }}</Button>
    </HoverCardTrigger>
    <HoverCardContent class="w-min flex items-center justify-center">
      <div>
        <p class="text-sm font-semibold whitespace-nowrap">
          <slot name="hover">

          </slot>
        </p>
      </div>
    </HoverCardContent>
  </HoverCard>
</template>

<script setup lang="ts">
import { HoverCard, HoverCardContent, HoverCardTrigger } from '@/components/ui/hover-card'
import { Button } from '@/components/ui/button'

defineProps<{ label: string }>()
defineEmits(['click'])
</script>
