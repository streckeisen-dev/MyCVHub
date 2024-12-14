<template>
  <v-main>
    <v-container v-if="account">
      <v-row class="account-section">
        <v-col
          cols="12"
          sm="6"
        >
          <h2>{{ t('account.personalData') }}</h2>
          <v-sheet
            rounded
            class="account-sheet"
          >
            <attribute-list>
              <attribute-value
                :name="t('fields.firstName')"
                :value="account.firstName"
              />
              <attribute-value
                :name="t('fields.lastName')"
                :value="account.lastName"
              />
              <attribute-value
                :name="t('fields.email')"
                :value="account.email"
              />
              <attribute-value
                :name="t('fields.phone')"
                :value="account.phone"
              />
              <attribute-value
                :name="t('fields.birthday')"
                :value="d(account.birthday, 'simpleDate')"
              />
            </attribute-list>
          </v-sheet>
        </v-col>
        <v-col
          cols="12"
          sm="6"
        >
          <h2>{{ t('account.address') }}</h2>
          <v-sheet
            rounded
            class="account-sheet"
          >
            <attribute-list>
              <attribute-value
                :name="t('fields.street')"
                :value="account.street"
              />
              <attribute-value
                :name="t('fields.houseNumber')"
                :value="account.houseNumber"
              />
              <attribute-value
                :name="t('fields.postcode')"
                :value="account.postcode"
              />
              <attribute-value
                :name="t('fields.city')"
                :value="account.city"
              />
              <attribute-value
                :name="t('fields.country')"
                :value="account.country"
              />
            </attribute-list>
          </v-sheet>
        </v-col>
      </v-row>

      <v-row class="account-section">
        <v-col
          cols="12"
          sm="6"
        >
          <h2>{{ t('profile.title') }}</h2>
          <v-sheet
            rounded
            class="account-sheet"
          >
            <v-row v-if="account.hasProfile">
              <v-col
                cols="12"
                md="5"
                lg="3"
              >
                <v-btn
                  :text="t('account.profile.view')"
                  color="primary"
                  :to="{ name: 'public-profile', params: { username: account.username } }"
                />
              </v-col>
              <v-col
                cols="12"
                md="5"
                lg="3"
              >
                <v-btn
                  :text="t('account.profile.edit')"
                  color="primary"
                  :to="{ name: 'edit-profile' }"
                />
              </v-col>
            </v-row>
            <v-row
              v-else
              class="create-profile"
            >
              <v-col
                cols="12"
                md="5"
                lg="3"
              >
                {{ t('account.profile.notFound') }}
              </v-col>
              <v-col
                cols="12"
                md="5"
                lg="3"
              >
                <v-btn
                  :text="t('account.profile.create')"
                  color="primary"
                  :to="{ name: 'create-profile' }"
                />
              </v-col>
            </v-row>
          </v-sheet>
        </v-col>
        <v-col
          cols="12"
          sm="6"
        >
          <h2>{{ t('account.title') }}</h2>
          <v-sheet
            rounded
            class="account-sheet"
          >
            <v-row>
              <v-col
                cols="12"
                md="5"
                lg="3"
              >
                <v-btn
                  :text="t('account.edit.title')"
                  color="primary"
                  :to="{ name: 'edit-account' }"
                />
              </v-col>
              <v-col
                v-if="account.hasPassword"
                cols="12"
                md="5"
                lg="3"
              >
                <v-btn
                  :text="t('account.edit.changePassword')"
                  color="primary"
                  :to="{ name: 'change-password' }"
                />
              </v-col>
            </v-row>
          </v-sheet>
        </v-col>

        <v-col
          cols="12"
          sm="6"
        >
          <h2>{{ t('account.delete.title') }}</h2>
          <v-sheet
            rounded
            class="account-sheet"
          >
            <v-row>
              <v-col
                cols="12"
                md="5"
                lg="3"
              >
                <v-btn
                  :text="t('account.delete.title')"
                  color="error"
                  @click="requestDeletionConfirmation"
                />
              </v-col>
            </v-row>
          </v-sheet>
          <confirmation-dialog
            v-if="shouldDeleteAccount"
            :title="t('account.delete.title')"
            :description="t('account.delete.confirmationText')"
            :confirmation-btn-text="t('account.delete.title')"
            confirmation-btn-color="error"
            @confirm="deleteAccount"
            @cancel="hideDeleteConfirmation"
          />
        </v-col>
      </v-row>
    </v-container>
    <v-container v-else-if="isAccountLoading">
      <loading-spinner />
    </v-container>
    <v-empty-state
      v-else
      :headline="t('error.genericMessageTitle')"
      :title="t('account.loadingError.title')"
      :text="t('account.loadingError.text')"
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
import { useI18n } from 'vue-i18n'
import ConfirmationDialog from '@/components/ConfirmationDialog.vue'
import router from '@/router'
import toastService from '@/services/ToastService.ts'
import type { ErrorDto } from '@/dto/ErrorDto.ts'

const { t, d } = useI18n({
  useScope: 'global'
})

const account = ref<AccountDto>()
const isAccountLoading = ref(true)
const shouldDeleteAccount = ref(false)
try {
  account.value = await accountApi.getAccountInfo()
} catch (ignore) {
  // ignore
} finally {
  isAccountLoading.value = false
}

function hideDeleteConfirmation() {
  shouldDeleteAccount.value = false
}

function requestDeletionConfirmation() {
  shouldDeleteAccount.value = true
}

async function deleteAccount() {
  hideDeleteConfirmation()
  try {
    await accountApi.deleteAccount()
    await accountApi.logout()
    toastService.success(t('account.delete.success'))
    await router.push({ name: 'home' })
  } catch (e) {
    const error = e as ErrorDto
    const errorDetails = error?.message || t('error.genericMessage')
    toastService.error(t('account.delete.error', errorDetails))
  }
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
