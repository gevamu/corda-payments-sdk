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
