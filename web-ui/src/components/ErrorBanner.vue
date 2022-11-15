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
  setup(){
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
