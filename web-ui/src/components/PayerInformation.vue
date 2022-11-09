<template>
  <div class="relative-position">
    <q-card-section>
      <div class="text-h6"> <q-icon name="eva-person-outline" size="lg" /> Payer Information</div>
      <p v-if="authStore.payerProfile">
        Lorem ipsum dolor sit amet, consectetur adipisicing elit. Dolorum, ratione!
      </p>
    </q-card-section>
    <q-card-section v-if="!authStore.payerProfile">
      You need to register as a Payer before you can proceed:
      <q-btn color="primary" no-caps
             size="15.5px" unelevated
             @click="register()">
        Register
      </q-btn>
    </q-card-section>
    <q-card-section v-else class="row">
      <div class="q-mr-lg register__field">
        <label class="text-bold">Participant ID</label>
        <q-input v-model="profile.participantId" readonly
                 dense outlined standout/>
      </div>
      <div class="q-mr-lg register__field">
        <label class="text-bold">Network ID</label>
        <q-input v-model="profile.networkId" readonly
                 dense outlined standout/>
      </div>
    </q-card-section>
    <!-- Loading -->
    <q-inner-loading :showing="loading">
      <q-spinner-ball size="50px" color="primary" />
    </q-inner-loading>
  </div>
</template>

<script lang="ts">
import {defineComponent} from 'vue'
import {useAuthStore} from 'stores/auth.store'
import {PayerProfile} from 'src/models/PayerProfile.interface';

export default defineComponent({
  name: 'PayerInformation',
  setup(){
    const authStore = useAuthStore()
    return {
      authStore
    }
  },
  computed: {
    loading(): boolean {
      return this.authStore.loading
    },
    profile(): PayerProfile{
      const unknownValue = 'xxxxxxx'
      return {
        networkId: this.authStore.payerProfile?.networkId || unknownValue,
        participantId: this.authStore.payerProfile?.participantId || unknownValue
      }
    }
  },
  methods: {
    async register(){
      await this.authStore.register()
    }
  }
})
</script>

<style scoped>
.register__field{
  width: 30%;
  min-width: 15rem;
}
</style>
