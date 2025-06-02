<template>
  <!-- Input v-model="input" placeholder="Input" />
  <Button type="submit" @click="validate">Validate</Button>
  <div v-if="result">
    <p>{{ result.valid }}</p>
    <p v-if="result.term">{{ result.term }}</p>
  </div -->



  <div class="flex flex-col">
    <div>
      <p class="font-bold text-8xl text-center">ucumate</p>
    </div>
    <div class="flex items-center justify-center mt-10">
      <Tabs default-value="validate" class="w-[1600px]">
        <TabsList class="grid w-full grid-cols-3">
          <TabsTrigger value="validate">
            Validate
          </TabsTrigger>
          <TabsTrigger value="canonicalize">
            Canonicalize
          </TabsTrigger>
          <TabsTrigger value="convert">
            Convert
          </TabsTrigger>
        </TabsList>
        <TabsContent value="validate">
          <Card>
            <CardHeader>
              <CardTitle>Validate</CardTitle>
              <CardDescription>
                Validate a given input string. The input will be attempted to be parsed into a UCUM
                expression. More details about the parsing can be obtained with the UCUM Expressive Syntax
                print setting. This may be used to clear up misinterpretation about the parsed term. I.e.
                <ValidateClickableUnit label="mph" @click="insertAndValidate('mph')" /> is valid
                and one might assume it means "miles per hour". However, in UCUM "mph"
                means "milli phot". The term "miles per hour" is written as
                <ValidateClickableUnit label="[mi_i]/h" @click="insertAndValidate('[mi_i]/h')" />.
              </CardDescription>
            </CardHeader>
            <CardContent class="space-y-2">
              <ValidateComponent ref="validateComponentRef" />
            </CardContent>
          </Card>
        </TabsContent>
        <TabsContent value="canonicalize">
          <Card>
            <CardHeader>
              <CardTitle>Canonicalize</CardTitle>
              <CardDescription>
                Canonicalize a given UCUM expression. The input will be validated first.
              </CardDescription>
            </CardHeader>
            <CardContent class="space-y-2">
              <KeepAlive>
                <CanonicalizeComponent />
              </KeepAlive>
            </CardContent>
          </Card>
        </TabsContent>
        <TabsContent value="convert">
          <Card>
            <CardHeader>
              <CardTitle>Convert</CardTitle>
              <CardDescription>
                Convert a given UCUM expression into another UCUM expression and receive the conversion factor. The input will be validated first.
              </CardDescription>
            </CardHeader>
            <CardContent class="space-y-2">
              <KeepAlive>
                <ConvertComponent />
              </KeepAlive>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>

    <Card class="mt-20 mx-10">
      <CardContent>
        <ul class="list-disc list-inside space-y-1 text-muted-foreground">
          <li >UCUM expressions are case-sensitive.</li>
          <li>There are some deviations from the UCUM standard:
            <ul class="list-disc list-inside space-y-1 pl-5">
              <li>
                The <a href="https://ucum.org/ucum#para-4" target="_blank" class="font-semibold underline">spec states</a>
                that only metric units may be prefixed, i.e. "km" (kilo meter). ucumate
                allows all units to be prefixed. Therefore "k[in_i]" (kilo inches) is valid. This is a deliberate
                decision to conform to existing UCUM libraries such as
                <a href="https://github.com/FHIR/Ucum-java" target="_blank" class="font-semibold underline">ucum-java</a>
                and <a href="https://ucum.nlm.nih.gov/ucum-lhc/demo.html" target="_blank" class="font-semibold underline">ucum-lhc</a>.
              </li>
              <li>
                The "Torr" unit was added.
                <a href="https://github.com/ucum-org/ucum/issues/289" target="_blank" class="font-semibold underline">
                  It was probably an accident that is was left out.
                </a>
              </li>
            </ul>
          </li>
          <li>There are also some ambiguities in the UCUM standard, here is how ucumate handles them:
            <ul class="list-disc list-inside space-y-1 pl-5">
              <li>
                Annotations can be put on terms with parenthesis. I.e. "(m.s){annot}" is valid.
              </li>
              <li>
                Integer units and dimensionless units can be combined in arithmetic operations. I.e.
                "5.3", "5/(3.2)", "5+2", "5-2", "10*3", "5.10^4" are all valid. The expression will
                be simplified down into a single factor during canonicalization and conversion.
                Note that integer units cannot have prefixes, i.e. "c5" (centi 5) is invalid.
              </li>
            </ul>

          </li>
        </ul>
      </CardContent>
    </Card>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue'
  import { Button } from '@/components/ui/button'
  import { Input } from '@/components/ui/input'
  import ValidateComponent from '@/components/rest/ValidateComponent.vue'
  import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
  import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
  import CanonicalizeComponent from '@/components/rest/CanonicalizeComponent.vue'
  import ConvertComponent from '@/components/rest/ConvertComponent.vue'
  import ClickableUnit from '@/components/clickableunit/ClickableUnit.vue'
  import ValidateClickableUnit from '@/components/clickableunit/ValidateClickableUnit.vue'


  const validateComponentRef = ref()

  const insertAndValidate = (term: string) => {
    if (validateComponentRef.value) {
      validateComponentRef.value.input = term
      validateComponentRef.value.validate()
    }
  }
</script>
