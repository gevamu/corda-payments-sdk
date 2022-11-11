<template>
  <q-page class="col q-pa-lg" style="max-width: 1000px; margin: auto">
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

export default defineComponent({
  name: 'IndexPage',
  components: {
    InitiatePaymentForm, PayerInformation, PaymentsTable
  }
});
</script>
