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
  <q-page class="col q-pa-lg" style="max-width: 1000px; margin: auto">
    <img :src="logo" style="height: 80px; margin-left: -25px" />
    <PayerInformation class="q-my-md" />
    <InitiatePaymentForm class="q-my-md"  />
    <PaymentsTable class="q-my-md"  />
  </q-page>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import InitiatePaymentForm from 'src/components/InitiatePaymentForm.vue'
import PayerInformation from 'components/PayerInformation.vue'
import PaymentsTable from 'components/PaymentsTable.vue'
const logo = require('assets/LogoDesignGevamu.png')

export default defineComponent({
  name: 'IndexPage',
  components: {
    InitiatePaymentForm, PayerInformation, PaymentsTable
  },
  setup(){
    return {
      logo
    }
  }
});
</script>
