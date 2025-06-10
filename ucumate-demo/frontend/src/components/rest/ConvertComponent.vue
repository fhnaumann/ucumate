<template>
  <div class="flex flex-col space-y-6">
    <div class="flex flex-row space-x-4">
      <div class="flex flex-col space-y-2">
        <NumberField class="w-40" id="fromConversionFactor" v-model="fromConvFactor" :default-value="1" :step-snapping="false" :format-options="{
          maximumFractionDigits: 100,
          useGrouping: false
        }">
          <div class="flex flex-col space-y-2">
            <Label for="fromConversionFactor">From Conversion Factor</Label>
            <NumberFieldContent>
              <NumberFieldInput />
            </NumberFieldContent>
          </div>
        </NumberField>
      </div>
      <div class="flex flex-col space-y-2">
        <Label for="inputForConversionFrom">From UCUM expression</Label>
        <Input class="w-60" id="inputForConversionFrom" placeholder="From UCUM expression" v-model="fromUnit" />
      </div>
      <div class="flex flex-col space-y-2">
        <Label for="inputForConversionTo">To UCUM expression</Label>
        <Input class="w-60" id="inputForConversionTo" placeholder="To UCUM expression" v-model="toUnit" />
      </div>
      <div class="flex flex-col space-y-2">
        <Label for="inputForConversionTo">To Conversion Factor</Label>
        <Input class="w-30 cursor-text select-text bg-muted text-muted-foreground" id="inputForConversionTo" readonly v-model="toConvFactor" />
      </div>
      <div class="flex items-end">
        <Button class="cursor-pointer" type="submit" @click="convert">Convert</Button>
      </div>
    </div>
    <div class="flex justify-center">
      <div v-if="fromConvFactor && fromUnit && toUnit && result && (result as any).valid" class="">
        <p>The input is valid!</p>
      </div>
      <div v-else-if="!freshlyLoaded">
        <InvalidInputMessage />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">

import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import RenderFormats from '@/components/RenderFormats.vue'
import InvalidInputMessage from '@/components/InvalidInputMessage.vue'
import OutputRenderedFormats from '@/components/OutputRenderedFormats.vue'
import { computed, ref } from 'vue'
import { NumberField, NumberFieldContent, NumberFieldInput } from '@/components/ui/number-field'

const selectedRenderFormats = ref(['ucum_expressive'])

const freshlyLoaded = ref(true)
const fromConvFactor = ref(1)
const fromUnit = ref('')
const toUnit = ref('')
const toConvFactor = computed(() => (result.value as any)?.resultingConversionFactor ?? '')
const result = ref(null)
const error = ref(null)

const convert = async () => {
  freshlyLoaded.value = true
  result.value = null

  const query = new URLSearchParams({
    fromFactor: fromConvFactor.value.toString(),
    fromInput: fromUnit.value,
    toInput: toUnit.value
  })
  if(selectedRenderFormats.value.length > 0) {
    query.append('formats', selectedRenderFormats.value.join(','))
  }
  fetch(`/api/convert?${query.toString()}`)
    .then(res => {
      if(!res.ok) throw new Error(`Backend error: ${res.status}`)
      return res.json()
    })
    .then(json => result.value = json)
    .catch(err => error.value = err.message)
    .finally(() => freshlyLoaded.value = false)
}

</script>
