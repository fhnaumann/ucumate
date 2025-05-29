<template>
  <input v-model="input" />
  <button @click="validate">Validate</button>
  <div v-if="result">
    <p>{{ result.valid }}</p>
    <p v-if="result.term">{{ result.term }}</p>
  </div>

</template>

<script setup lang="ts">
  import { ref } from 'vue'

  const input = ref('')
  const result = ref(null)
  const error = ref(null)

  const validate = async () => {
    result.value = null
    fetch(`/api/validate?input=${encodeURIComponent(input.value)}`)
      .then(res => {
        if(!res.ok) throw new Error(`Backend error: ${res.status}`)
        return res.json()
      })
      .then(json => result.value = json)
      .catch(err => error.value = err.message)
  }
</script>
