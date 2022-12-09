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
