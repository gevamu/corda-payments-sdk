<template>
  <div class="payment-form__section">
    <q-card-section>
      <div class="text-h6 payment-form__header">
        <q-icon name="eva-credit-card-outline" size="sm" class="payment-form__icon" />
        Transfer Payment
      </div>
      <p>
        After submission, the payment cannot be amended or canceled
      </p>
    </q-card-section>
    <q-form @submit.prevent="submitPayment">
      <q-card-section class="row">
        <div class="q-mr-lg payment-form__field">
          <div class="payment-form__label">Amount</div>
          <q-input type="number" v-model="amount" :disable="disabled"
                   dense outlined>
            <template v-slot:append><div class="payment-form__field__currency">{{currency}}</div></template>
          </q-input>
        </div>
        <div class="q-mr-lg payment-form__field">
          <div class="payment-form__label">Debtor</div>
          <q-select v-model="debtorAccount" :disable="disabled"
                    :options="debtorOptions" map-options emit-value
                    dense outlined>
          </q-select>
        </div>
        <div class="q-mr-lg payment-form__field">
          <div class="payment-form__label">Creditor</div>
          <q-select v-model="creditorAccount" :disable="disabled"
                    :options="creditorOptions" map-options emit-value
                    dense outlined>
          </q-select>
        </div>
        <div class="payment-form__action">
          <q-btn type="submit" :disable="disabled"
                 class="q-mt-lg full-width"
                 unelevated
                 no-caps color="primary">
            Submit
          </q-btn>
        </div>
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
import {useParticipantsStore} from 'stores/participants.store'
import {usePaymentsStore} from 'stores/payments.store'
import {QSelectOption} from 'quasar'
import {useErrorHandler} from 'stores/errorHandler.store';

export default defineComponent({
  name: 'InitiatePaymentForm',
  setup() {
    const amount = ref(0)
    const creditorAccount = ref('')
    const debtorAccount = ref('')
    const errorHandler = useErrorHandler()
    const authStore = useAuthStore()
    const participantsStore = useParticipantsStore()
    const paymentsStore = usePaymentsStore()
    const loading = ref(false)

    return {
      amount, creditorAccount, debtorAccount,
      authStore, participantsStore, paymentsStore,
      loading, errorHandler
    }
  },
  computed: {
    disabled(): boolean {
      return !this.authStore.isAuthorized
    },
    currency() {
      return this.participantsStore.debtors.find((debtor) => debtor.id === this.debtorAccount)?.currency ?? 'USD'
    },
    creditorOptions() {
      return this.participantsStore.creditors
        .map((creditor): QSelectOption => {
          return {
            label: creditor.name,
            value: creditor.id
          }
        })
    },
    debtorOptions() {
      return this.participantsStore.debtors
        .map((debtor): QSelectOption => {
          return {
            label: debtor.name,
            value: debtor.id
          }
        })
    }
  },
  methods: {
    async submitPayment() {
      this.loading = true
      try {
        await this.paymentsStore.submitPayment(this.debtorAccount, this.creditorAccount, this.amount)
        this.resetForm()
      } catch (e) {
        this.errorHandler.handleError(e)
      } finally {
        this.loading = false
      }
      await this.paymentsStore.fetchPayments()
    },
    resetForm() {
      this.amount = 0
      this.creditorAccount = this.participantsStore.creditors[0]?.id ?? ''
      this.debtorAccount = this.participantsStore.debtors[0]?.id ?? ''
    }
  },
  async created() {
    await Promise.all([this.participantsStore.fetchCreditors(), this.participantsStore.fetchDebtors()])
    this.resetForm()
  }
})
</script>

<style>
.payment-form__section {
  padding-top: 32px;
}
.payment-form__field {
  flex: 1;
  min-width: 15rem;
}
.payment-form__label {
  color: #344A68;
  padding-bottom: 3px;
  font-weight: 500;
}
.payment-form__action {
  min-width: 6rem;
}
.payment-form__action .q-btn__content {
  line-height: 20px;
  padding-top: 6px;
  padding-bottom: 6px;
}
.payment-form__header {
  color: #246455;
}
.payment-form__icon {
  color: #50B680;
  bottom: 2px;
}
.payment-form__field__currency {
  font-size: 14px;
  line-height: 28px;
  padding-top: 6px;
  padding-bottom: 6px;
}
</style>
