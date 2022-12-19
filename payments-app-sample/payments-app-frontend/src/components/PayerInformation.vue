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
  <div class="register__section">
    <q-card-section>
      <div class="text-h6 register__header">
        <q-icon name="eva-person-outline" size="sm" class="register__icon" />
        Participant Information
      </div>
      <p v-if="authStore.payerProfile">
        To complete your registration, submit this information to the Payment Service Provider
      </p>
    </q-card-section>
    <q-card-section v-if="!authStore.payerProfile" class="row">
      <div class="q-mr-lg register__prompt">To proceed, register as a Participant:</div>
      <q-btn color="primary" no-caps class="register__action"
             unelevated
             @click="register()">
        Register
      </q-btn>
    </q-card-section>
    <q-card-section v-else class="row">
      <div class="q-mr-lg register__field">
        <div class="register__label">Participant ID</div>
        <q-input v-model="profile.participantId" readonly
                 dense outlined standout/>
      </div>
      <div class="q-mr-lg register__field">
        <div class="register__label">Network ID</div>
        <q-input v-model="profile.networkId" readonly
                 dense outlined standout/>
      </div>
      <div class="q-mr-lg register__field" />
      <div class="register__action" />
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
  setup() {
    const authStore = useAuthStore()
    return {
      authStore
    }
  },
  computed: {
    loading(): boolean {
      return this.authStore.loading
    },
    profile(): PayerProfile {
      const unknownValue = 'xxxxxxx'
      return {
        networkId: this.authStore.payerProfile?.networkId || unknownValue,
        participantId: this.authStore.payerProfile?.participantId || unknownValue
      }
    }
  },
  methods: {
    async register() {
      await this.authStore.register()
    }
  }
})
</script>

<style scoped>
.register__section {
  padding-top: 8px;
}
.register__field {
  flex: 1;
  min-width: 15rem;
}
.register__label {
  color: #344A68;
  padding-bottom: 3px;
  font-weight: 500;
}
.register__action {
  min-width: 6rem;
}
.register__action .q-btn__content {
  line-height: 20px;
  padding-top: 6px;
  padding-bottom: 6px;
}
.register__header {
  color: #246455;
}
.register__icon {
  color: #50B680;
  bottom: 2px;
}
.register__prompt {
    line-height: 28px;
    padding-top: 4px;
    padding-bottom: 4px;
}
</style>
