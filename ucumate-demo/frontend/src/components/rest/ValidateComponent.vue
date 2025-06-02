<template>
  <div class="flex flex-col space-y-6">
    <RenderFormats v-model="selectedRenderFormats" />
    <div class="flex flex-row space-x-4">
      <div class="flex flex-col space-y-2">
        <Label for="inputForValidation">Validate Input</Label>
        <Input class="w-60" id="inputForValidation" placeholder="Input" v-model="input" />
      </div>
      <div class="flex items-end">
        <Button class="cursor-pointer" type="submit" @click="validate">Validate</Button>
      </div>
    </div>
    <div class="flex justify-center">
      <div v-if="input && result && (result as any).valid" class="">
        <p>{{ validatedInput }} is valid!</p>
      </div>
      <div v-else-if="!freshlyLoaded">
        <InvalidInputMessage />
      </div>
    </div>
    <OutputRenderedFormats :render-outputs="(result as any)?.rendered ?? {}"/>
  </div>
</template>

<script setup lang="ts">
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { ref } from 'vue'
import { Label } from '@/components/ui/label'
import InvalidInputMessage from '@/components/InvalidInputMessage.vue'
import RenderFormats from '@/components/RenderFormats.vue'
import OutputRenderedFormats from '@/components/OutputRenderedFormats.vue'

const selectedRenderFormats = ref(['ucum_expressive'])

const freshlyLoaded = ref(true)
const input = ref('')
const validatedInput = ref('')
const result = ref(null)
const error = ref(null)

const validate = async () => {
  freshlyLoaded.value = true
  result.value = null
  validatedInput.value = input.value

  const query = new URLSearchParams({
    input: input.value
  })
  if(selectedRenderFormats.value.length > 0) {
    query.append('formats', selectedRenderFormats.value.join(','))
  }
  fetch(`/api/validate?${query.toString()}`)
    .then(res => {
      if(!res.ok) throw new Error(`Backend error: ${res.status}`)
      return res.json()
    })
    .then(json => result.value = json)
    .catch(err => error.value = err.message)
    .finally(() => freshlyLoaded.value = false)
}

defineExpose({ input, validate })

</script>
