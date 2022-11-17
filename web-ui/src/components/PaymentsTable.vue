<template>
  <div class="payments__section">
    <q-card-section>
      <div class="text-h6 payments__header">
        <q-icon name="eva-file-text-outline" size="sm" class="payments__icon" />
        Payments
      </div>
      <p>
        Some payments may take several business days to complete
      </p>
    </q-card-section>
    <q-markup-table class="text-left" separator="none" bordered flat>
      <thead class="bg-grey-2">
        <tr>
          <th>PAYMENT ID</th>
          <th>CREATED</th>
          <th>AMOUNT</th>
          <th>STATUS</th>
          <th>DEBTOR ACCOUNT</th>
          <th>CREDITOR ACCOUNT</th>
        </tr>
      </thead>
      <tbody>
        <q-tr v-for="(payment, index) in currentPagePayments" :key="payment.id"
            no-hover
            :class="{
              'bg-grey-1': (index + 1) % 2 === 0
            }">
          <td class="payments__id-column"># {{payment.endToEndId}}</td>
          <td>{{payment.creationTime.toLocaleString('en-UK')}}</td>
          <td>{{payment.currency}} {{payment.amount}}</td>
          <td>
            <q-chip :style="getStatusColor(payment.status)"
                    dense>
              {{payment.status}}
            </q-chip>
          </td>
          <td>{{ payment.debtor }}</td>
          <td>{{ payment.creditor }}</td>
        </q-tr>
      </tbody>
    </q-markup-table>
    <q-separator class="q-my-md" />
    <div class="row justify-between">
      <p>
        Showing
        <span class="text-bold">{{firstItemOnPage}} to {{lastItemOnPage}}</span>
        of
        <span class="text-bold">{{paymentsStore.payments.length}} results</span>
      </p>
      <div class="row">
        <q-btn no-caps class="q-mx-md"
               unelevated outline
               @click="currentPage--"
               :disable="firstItemOnPage === 1">
          Previous
        </q-btn>
        <q-btn no-caps class="q-mx-md"
               unelevated outline
               @click="currentPage++"
               :disable="lastItemOnPage === paymentsStore.payments.length">
          Next
        </q-btn>
      </div>
    </div>

  </div>
</template>

<script lang="ts">
import {defineComponent, ref, computed} from 'vue';
import {usePaymentsStore} from 'stores/payments.store';
import {PaymentStatus} from 'src/models/payment/PaymentStatus.type';

export default defineComponent({
  name: 'PaymentsTable',
  setup() {
    const paymentsStore = usePaymentsStore()
    const currentPage = ref(1)
    const itemsOnPages = 10
    const firstItemOnPage = computed<number>(() => {
      return paymentsStore.payments.length === 0 ? 0 : itemsOnPages * (currentPage.value - 1) + 1
    })
    const lastItemOnPage = computed<number>(() => {
      const numberIfNotLastPage = itemsOnPages * currentPage.value
      const allPaymentsCount = paymentsStore.payments.length
      if (numberIfNotLastPage >= allPaymentsCount)
        return allPaymentsCount
      else return numberIfNotLastPage
    })

    const positiveStatuses: PaymentStatus[] = ['Accepted', 'Sent to Gateway']
    const negativeStatuses: PaymentStatus[] = ['Rejected']
    function getStatusColor(status: PaymentStatus): Partial<CSSStyleDeclaration> {
      if (positiveStatuses.includes(status))
        return {
          color: '#246455',
          backgroundColor: '#DCE6E4',
          fontSize: '12px',
          lineHeight: '1.5em',
          height: '1.5em',
          fontWeight: '400',
          padding: '1em',
        }
      if (negativeStatuses.includes(status))
        return {
          color: '#B64C4C',
          backgroundColor: '#F3E2E2',
          fontSize: '12px',
          lineHeight: '1.5em',
          height: '1.5em',
          fontWeight: '400',
          padding: '1em',
        }
      return {
        color: '#374151',
        backgroundColor: '#F3F4F6',
        fontSize: '12px',
        lineHeight: '1.5em',
        height: '1.5em',
        fontWeight: '400',
        padding: '1em',
      }
    }
    return {
      currentPage, itemsOnPages,
      firstItemOnPage, lastItemOnPage,
      getStatusColor,
      paymentsStore
    }
  },
  computed:{
    loading(): boolean{
      return this.paymentsStore.loading || !!this.paymentsStore.fetchingProcess
    },
    currentPagePayments(){
      return this.paymentsStore.payments.slice(this.firstItemOnPage-1, this.lastItemOnPage)
    },

  },
  created() {
    // TODO: fetch payments only when authorised
    this.paymentsStore.fetchPayments()
    this.paymentsStore.startConstantFetching()
  },
  beforeUnmount() {
    this.paymentsStore.stopConstantFetching()
  }
})
</script>

<style>
.payments__section {
  padding-top: 32px;
}
.payments__header {
  color: #246455;
}
.payments__icon {
  color: #50B680;
  bottom: 2px;
}
.payments__section .q-table__card {
  color: #64758B;
}
.payments__id-column {
  color: #344A68;
  font-weight: 500;
}
</style>
