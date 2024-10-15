<template>
  <v-main>
    <v-container v-if="account">
      <v-row class="account-section">
        <v-col cols="12" sm="6">
          <h2>Personal Data</h2>
          <v-sheet rounded class="account-sheet">
            <attribute-list>
              <attribute-value name="First Name" :value="account.firstName" />
              <attribute-value name="Last Name" :value="account.lastName" />
              <attribute-value name="E-Mail Address" :value="account.email" />
              <attribute-value name="Phone" :value="account.phone" />
              <attribute-value name="Birthday" :value="account.birthday" />
            </attribute-list>
          </v-sheet>
        </v-col>
        <v-col cols="12" sm="6">
          <h2>Address</h2>
          <v-sheet rounded class="account-sheet">
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

      <v-row class="account-section">
        <v-col cols="12" sm="6">
          <h2>Profile</h2>
          <v-sheet rounded class="account-sheet">
            <v-row v-if="account.profile">
              <v-col cols="12" md="5" lg="3">
                <router-link :to="{ name: 'public-profile', params: { alias: account.profile } }">
                  <v-btn text="View Profile" color="primary" />
                </router-link>
              </v-col>
              <v-col cols="12" md="5" lg="3">
                <router-link :to="{ name: 'edit-profile' }">
                  <v-btn text="Edit Profile" color="primary" />
                </router-link>
              </v-col>
            </v-row>
            <v-row v-else class="create-profile">
              <v-col cols="12" md="5" lg="3">You don't have a profile yet</v-col>
              <v-col cols="12" md="5" lg="3">
                <router-link :to="{ name: 'create-profile' }">
                  <v-btn text="Create Profile" color="primary" />
                </router-link>
              </v-col>
            </v-row>
          </v-sheet>
        </v-col>
      </v-row>
    </v-container>
    <v-container v-else-if="isAccountLoading">
      <loading-spinner />
    </v-container>
    <v-empty-state
      v-else
      headline="Oops, something went wrong"
      title="Failed to load your account"
      text="Make sure you're logged in and try again"
    />
  </v-main>
</template>

<script setup lang="ts">
import accountApi from '@/api/AccountApi'
import type { AccountDto } from '@/dto/AccountDto'
import { ref } from 'vue'
import LoadingSpinner from '@/components/LoadingSpinner.vue'
import AttributeList from '@/components/AttributeList.vue'
import AttributeValue from '@/components/AttributeValue.vue'

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

<style lang="scss" scoped>
.account-section {
  margin-bottom: 15px;

  h2 {
    margin-bottom: 10px;
  }

  .account-sheet {
    padding: 20px;

    .create-profile {
      align-items: center;
    }
  }
}
</style>
