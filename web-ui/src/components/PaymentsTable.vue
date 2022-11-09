<template>
  <div>
    <q-card-section>
      <div class="text-h6"> <q-icon name="eva-file-text-outline" size="lg" /> Payments</div>
      <p>
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Dolorum, ratione!
      </p>
    </q-card-section>
    <q-markup-table class="text-left" separator="none" bordered flat>
      <thead class="bg-grey-2">
        <tr>
          <th>PAYMENT ID</th>
          <th>AMOUNT</th>
          <th>STATUS</th>
          <th>RECIPIENT</th>
        </tr>
      </thead>
      <tbody>
        <q-tr v-for="(payment, index) in currentPagePayments" :key="payment.id"
            no-hover
            :class="{
              'bg-grey-1': (index + 1) % 2 === 0
            }">
          <td class="text-bold"># {{payment.id}}</td>
          <td>{{payment.amount}} {{payment.currency}}</td>
          <td>
            <q-chip :style="getStatusColor(payment.status)"
                    dense>
              {{payment.status}}
            </q-chip>
          </td>
          <td>{{payment.creditorId}}</td>
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
               size="15.5px" unelevated outline
               @click="currentPage--"
               :disable="firstItemOnPage === 1">
          Previous
        </q-btn>
        <q-btn no-caps class="q-mx-md"
               size="15.5px" unelevated outline
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
  setup(){
    const paymentsStore = usePaymentsStore()
    const currentPage = ref(1)
    const itemsOnPages = 10
    const firstItemOnPage = computed<number>(() => {
      return itemsOnPages * (currentPage.value - 1) + 1
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
          color: '#065F46',
          backgroundColor: '#D1FAE5'
        }
      if (negativeStatuses.includes(status))
        return {
          color: '#991B1B',
          backgroundColor: '#FEE2E2'
        }
      return {}
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

<style scoped>

</style>
