<template>
  <router-view />
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { useAuthStore } from 'stores/auth.store'

export default defineComponent({
  name: 'App',
  setup(){
    const authStore = useAuthStore()
    return { authStore }
  },
  created() {
    this.authStore.getRegistration()
  }
});
</script>
