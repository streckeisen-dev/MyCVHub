<template>
  <v-container v-if="account">
    <v-row>
      <h2>Account Details</h2>
    </v-row>

    <v-row>
      <v-col>
        <h2>Personal Data</h2>
        <v-sheet class="account-details" rounded color="primary">
          <attribute-list>
            <attribute-value name="First Name" :value="account.firstName" />
            <atribute-value name="Last Name" :value="account.lastName" />
            <atribute-value name="E-Mail Address" :value="account.email" />
            <attribute-value name="Phone" :value="account.phone" />
            <attribute-value name="Birthday" :value="account.birthday" />
          </attribute-list>
        </v-sheet>
      </v-col>
      <v-col>
        <h2>Address</h2>
        <v-sheet class="account-details" rounded color="primary">
          <attribute-list>
            <attribute-value name="Street" :value="account.street" />
            <attribute-value name="House Number" :value="account.houseNumber" />
            <attribute-value name="Postcode" :value="account.postcode" />
            <attribute-value name="City" :value="account.city" />
            <attribute-value name="Country" :value="account.country" />
          </attribute-list>
        </v-sheet>
      </v-col>
    </v-row>
  </v-container>
  <v-container v-else-if="isAccountLoading">
    <loading-spinner />
  </v-container>
  <v-empty-state v-else headline="Oops, something went wrong"
                 title="Failed to load your account"
                 text="Make sure you're logged in and try again" />
</template>

<script setup lang="ts">
import accountApi from '@/api/account-api'
import type { AccountDto } from '@/dto/AccountDto'
import { ref } from 'vue'
import LoadingSpinner from '@/components/LoadingSpinner.vue'
import AttributeList from '@/components/AttributeList.vue'
import AttributeValue from '@/components/AtributeValue.vue'
import AtributeValue from '@/components/AtributeValue.vue'

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
