<template>
  <q-layout view="lHh Lpr lFf" class="main-layout">
    <q-page-container>
      <router-view />
    </q-page-container>
    <ErrorBanner class="fixed-bottom" />
  </q-layout>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import ErrorBanner from 'components/ErrorBanner.vue';

export default defineComponent({
  name: 'MainLayout',
  components: {ErrorBanner}
});
</script>

<style>
.main-layout .q-card__section {
   padding: 0;
}
</style>
