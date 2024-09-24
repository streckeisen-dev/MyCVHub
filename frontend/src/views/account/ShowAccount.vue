<template>
  <v-container v-if="account">
    <v-row>
      <h2>Account Details</h2>
    </v-row>

    <v-row>
      <v-col>
        <h2>Personal Data</h2>
        <v-sheet class="account-details" rounded color="primary">
          <dl class="formatted">
            <dt>First Name</dt>
            <dd>{{ account.firstName }}</dd>

            <dt>Last Name</dt>
            <dd>{{ account.lastName }}</dd>

            <dt>E-Mail Address</dt>
            <dd>{{ account.email }}</dd>

            <dt>Phone</dt>
            <dd>{{ account.phone }}</dd>

            <dt>Birthday</dt>
            <dd>{{ account.birthday }}</dd>

            <dt>Public Profile</dt>
            <dd>{{ account.hasPublicProfile ? 'Yes' : 'No' }}</dd>
          </dl>
        </v-sheet>
      </v-col>
      <v-col>
        <h2>Address</h2>
        <v-sheet class="account-details" rounded color="primary">
          <dl class="formatted">
            <dt>Street</dt>
            <dd>{{ account.street }}</dd>

            <dt>House Number</dt>
            <dd>{{ account.houseNumber }}</dd>

            <dt>Postcode</dt>
            <dd>{{ account.postcode }}</dd>

            <dt>City</dt>
            <dd>{{ account.city }}</dd>

            <dt>Country</dt>
            <dd>{{ account.country }}</dd>
          </dl>
        </v-sheet>
      </v-col>
    </v-row>
  </v-container>
  <v-container v-else-if="isAccountLoading">
    <v-progress-circular indeterminate
                         color="secondary"
                         class="loading-spinner" />
  </v-container>
  <v-empty-state v-else headline="Oops, something went wrong"
                 title="Failed to load your account"
                 text="Make sure you're logged in and try again" />
</template>

<script setup lang="ts">
import accountApi from '@/api/account-api'
import type { AccountDto } from '@/dto/AccountDto'
import { ref } from 'vue'

const account = ref<AccountDto>()
const isAccountLoading = ref<boolean>(true)
try {
  account.value = await accountApi.getAccountInfo()
} catch (error) {
  console.log('account error', error)
} finally {
  isAccountLoading.value = false
}
</script>

<style scoped>
.account-details {
  padding: 10px;
  height: 100%;
}
</style>
