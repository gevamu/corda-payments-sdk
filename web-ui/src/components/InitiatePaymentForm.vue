<template>
  <div class="relative-position">
    <q-card-section>
      <div class="text-h6"><q-icon name="eva-credit-card-outline" size="lg" /> Transfer Payment</div>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Error, itaque!
      </p>
    </q-card-section>
    <q-form @submit.prevent="submitPayment">
      <q-card-section class="row items-end">
        <div class="q-mr-lg payment-form__field">
          <label class="text-bold">Amount</label>
          <q-input type="number" v-model="amount" :disable="disabled"
                   dense outlined>
            <template v-slot:prepend> <q-icon name="attach_money" /> </template>
            <template v-slot:append> USD </template>
          </q-input>
        </div>
        <div class="q-mr-lg payment-form__field">
          <label class="text-bold">Recipient</label>
          <q-select v-model="creditorAccount" :disable="disabled"
                    :options="creditorOptions" map-options emit-value
                    dense outlined>
          </q-select>
        </div>

        <q-btn type="submit" :disable="disabled"
               size="15.5px" unelevated
               no-caps color="primary">
          Submit
        </q-btn>
      </q-card-section>
    </q-form>
    <!-- Loading -->
    <q-inner-loading :showing="loading">
      <q-spinner-ball size="50px" color="primary" />
    </q-inner-loading>
  </div>

</template>

<script lang="ts">
import { defineComponent, ref } from 'vue'
import {useAuthStore} from 'stores/auth.store'
import {useCreditorsStore} from 'stores/creditors.store'
import {usePaymentsStore} from 'stores/payments.store'
import {QSelectOption} from 'quasar'

export default defineComponent({
  name: 'InitiatePaymentForm',
  setup(){
    const amount = ref(0)
    const creditorAccount = ref('')
    const authStore = useAuthStore()
    const creditorsStore = useCreditorsStore()
    const paymentsStore = usePaymentsStore()
    const loading = ref(false)

    return {
      amount, creditorAccount,
      authStore, creditorsStore, paymentsStore,
      loading
    }
  },
  computed: {
    disabled(): boolean{
      return !this.authStore.isAuthorized
    },
    creditorOptions(){
      return this.creditorsStore.creditors
        .map((creditor): QSelectOption => {
          return {
            label: creditor.name,
            value: creditor.id
          }
        })
    }
  },
  methods: {
    async submitPayment(){
      this.loading = true
      await this.paymentsStore.submitPayment(this.amount, this.creditorAccount)
      this.resetForm()
      await this.paymentsStore.fetchPayments()
      this.loading = false
    },
    resetForm(){
      this.amount = 0
      this.creditorAccount = this.creditorsStore.creditors[0]?.name || ''
    }
  },
  async created() {
    await this.creditorsStore.fetchCreditors()
    this.resetForm()
  }
})
</script>

<style scoped>
.payment-form__field{
  width: 30%;
  min-width: 15rem;
}
</style>
