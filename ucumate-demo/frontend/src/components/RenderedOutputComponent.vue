<template>
  <div class="flex items-center space-x-2">
    <!-- Label column -->
    <Label :for="title" class="w-50 text-right shrink-0">{{ title }}:</Label>

    <!-- Input + action slot -->
    <div class="flex items-center space-x-2 w-full">
      <TooltipProvider>
        <Tooltip>
          <TooltipTrigger as-child>
            <Input
              :id="title"
              :model-value="renderedForm"
              readonly
              @focus="$event.target.select()"
              class="w-1/2 cursor-text select-text bg-muted text-muted-foreground"
            />
          </TooltipTrigger>
          <TooltipContent>
            <p>Click to select</p>
          </TooltipContent>
        </Tooltip>
      </TooltipProvider>

      <slot name="action" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { Input } from '@/components/ui/input'
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/components/ui/tooltip'
import { Label } from '@/components/ui/label'

const props = defineProps<{
  renderedForm: string
  title: string
}>()
</script>
