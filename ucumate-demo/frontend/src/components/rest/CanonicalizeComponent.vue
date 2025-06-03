<template>
  <div class="flex flex-col space-y-6">
    <RenderFormats v-model="selectedRenderFormats" />
    <div class="flex flex-row space-x-4">
      <div class="flex flex-col space-y-2">
        <Label for="inputForCanonicalization">Canonicalize UCUM expression</Label>
        <Input class="w-60" id="inputForCanonicalization" placeholder="UCUM expression" v-model="input" />
      </div>
      <div class="flex items-end">
        <Button class="cursor-pointer" type="submit" @click="canonicalize">Canonicalize</Button>
      </div>
    </div>
    <div class="flex justify-center">
      <div v-if="input && result && (result as any).valid" class="">
        <p>{{ canonicalizedInput }} is valid!</p>
      </div>
      <div v-else-if="!freshlyLoaded">
        <InvalidInputMessage />
      </div>
    </div>
    <div v-if="input && result && (result as any).valid" class="flex space-x-4">
      <Label for="magnitude">Magnitude:</Label>
      <Input class="w-30 cursor-text select-text bg-muted text-muted-foreground" id="magnitude" readonly :model-value="(result as any)?.magnitude ?? '-'" />
    </div>
    <OutputRenderedFormats :render-outputs="(result as any)?.rendered ?? {}"/>
  </div>
</template>

<script setup lang="ts">

import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import RenderFormats from '@/components/RenderFormats.vue'
import InvalidInputMessage from '@/components/InvalidInputMessage.vue'
import OutputRenderedFormats from '@/components/OutputRenderedFormats.vue'
import { ref } from 'vue'

const selectedRenderFormats = ref(['ucum_expressive'])

const freshlyLoaded = ref(true)
const input = ref('')
const canonicalizedInput = ref('')
const result = ref(null)
const error = ref(null)

const canonicalize = async () => {
  freshlyLoaded.value = true
  result.value = null
  canonicalizedInput.value = input.value

  const query = new URLSearchParams({
    input: input.value
  })
  if(selectedRenderFormats.value.length > 0) {
    query.append('formats', selectedRenderFormats.value.join(','))
  }
  fetch(`/api/canonicalize?${query.toString()}`)
    .then(res => {
      if(!res.ok) throw new Error(`Backend error: ${res.status}`)
      return res.json()
    })
    .then(json => result.value = json)
    .catch(err => error.value = err.message)
    .finally(() => freshlyLoaded.value = false)
}

</script>
