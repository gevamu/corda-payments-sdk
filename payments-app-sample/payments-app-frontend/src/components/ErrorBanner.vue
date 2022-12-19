<!--
* Copyright 2022 Exactpro Systems Limited
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
-->

<template>
  <transition name="fade">
    <q-banner inline-actions class="text-white bg-red" v-if="!!errorHandler.error">
      <div class="row justify-between">
        <span>{{message}}</span>
        <q-btn flat color="white" label="Hide" @click="errorHandler.hideError()" />
      </div>
    </q-banner>
  </transition>

</template>

<script lang="ts">
import {defineComponent} from 'vue';
import {useErrorHandler} from 'stores/errorHandler.store';

export default defineComponent({
  name: 'ErrorBanner',
  setup() {
    const errorHandler = useErrorHandler()
    return {
      errorHandler
    }
  },
  computed: {
    message() {
      return this.errorHandler.error
    },
  }
})
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
