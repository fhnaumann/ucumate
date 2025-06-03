<template>
  <div class="flex flex-col space-y-2">
    <RenderedOutputComponent
      v-if="ucumRender"
      title="UCUM Syntax"
      :rendered-form="ucumRender"
    >
      <template #action>
        <Button class="cursor-pointer" @click="copy('ucum', ucumRender)">Copy</Button>
        <span v-if="copied === 'ucum'" class="text-sm text-muted-foreground">Copied!</span>
      </template>
    </RenderedOutputComponent>

    <RenderedOutputComponent
      v-if="ucumExpressiveRender"
      title="UCUM Expressive Syntax"
      :rendered-form="ucumExpressiveRender"
    >
      <template #action>
        <Button class="cursor-pointer" @click="copy('ucum_expressive', ucumExpressiveRender)">Copy</Button>
        <span v-if="copied === 'ucum_expressive'" class="text-sm text-muted-foreground">Copied!</span>
      </template>
    </RenderedOutputComponent>

    <RenderedOutputComponent
      v-if="commonRender"
      title="Common Syntax"
      :rendered-form="commonRender"
    >
      <template #action>
        <Button class="cursor-pointer" @click="copy('common', commonRender)">Copy</Button>
        <span v-if="copied === 'common'" class="text-sm text-muted-foreground">Copied!</span>
      </template>
    </RenderedOutputComponent>
    <RenderedOutputComponent
      v-if="latexRender"
      title="LaTeX Syntax"
      :rendered-form="latexRender"
    >
      <template #action>
        <Button class="cursor-pointer" @click="copy('latex', latexRender)">Copy</Button>
        <span v-if="copied === 'latex'" class="text-sm text-muted-foreground">Copied!</span>
        <Button class="cursor-pointer" @click="latexRenderedVisible = true">Render LaTeX</Button>
      </template>
    </RenderedOutputComponent>
    <div v-if="latexRenderedVisible" class="flex flex-col items-center justify-center">
      <div ref="latexContainer" v-html="renderedLatexHtml" class="inline-block p-4" ></div>
      <div>
        <Button @click="downloadLatexAsImage">Download as PNG</Button>
      </div>
    </div>
  </div>
</template>


<script setup lang="ts">
import RenderedOutputComponent from '@/components/RenderedOutputComponent.vue'
import { Button } from '@/components/ui/button'
import { computed, ref } from 'vue'
import katex from 'katex'
import 'katex/dist/katex.min.css'
import html2canvas from 'html2canvas-pro'

const props = defineProps<{
  renderOutputs: Record<string, string>
}>()

const ucumRender = computed(() => props.renderOutputs['ucum'])
const ucumExpressiveRender = computed(() => props.renderOutputs['ucum_expressive'])
const commonRender = computed(() => props.renderOutputs['common'])
const latexRender = computed(() => props.renderOutputs['latex'])

const copied = ref('')

const copy = async (format: string, text: string) => {
  await navigator.clipboard.writeText(text)
  copied.value = format
  setTimeout(() => {
    if (copied.value === format) copied.value = ''
  }, 1000)
}

const latexRenderedVisible = ref(false)

const renderedLatexHtml = computed(() => {
    console.log(latexRender.value)
  if(latexRender.value === undefined) {
    return ""
  }
    return katex.renderToString(latexRender.value, {
      throwOnError: false,
      displayMode: true
    })
}

)

const latexContainer = ref<HTMLElement | null>(null)

async function downloadLatexAsImage() {
  console.log(latexContainer)
  if (!latexContainer.value) return

  const canvas = await html2canvas(latexContainer.value, {
    backgroundColor: null // transparent
  })

  const dataUrl = canvas.toDataURL('image/png')
  const link = document.createElement('a')
  link.href = dataUrl
  link.download = 'latex.png'
  link.click()
}

</script>
