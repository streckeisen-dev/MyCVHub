<template>
  <v-dialog
    test-id="confirmation-dialog"
    :model-value="true"
    @update:model-value="cancel"
  >
    <v-sheet class="dialog-sheet">
      <v-row>
        <v-col cols="12">
          <h2>{{ title }}</h2>
        </v-col>
        <v-col cols="12">
          <p>{{ description }}</p>
        </v-col>
      </v-row>
      <v-row>
        <v-col cols="12" test-id="dialog-controls">
          <form-buttons
            @save="confirm"
            @cancel="cancel"
            :submit-text="confirmationBtnText"
            :submit-color="confirmationBtnColor"
            :cancel-text="cancelBtnText"
            :cancel-color="cancelBtnColor"
          />
        </v-col>
      </v-row>
    </v-sheet>
  </v-dialog>
</template>

<script setup lang="ts">
import FormButtons from '@/components/FormButtons.vue'

defineProps<{
  title: string
  description: string
  confirmationBtnText?: string
  confirmationBtnColor?: string
  cancelBtnText?: string
  cancelBtnColor?: string
}>()

const emit = defineEmits(['confirm', 'cancel'])

function confirm() {
  emit('confirm')
}

function cancel() {
  emit('cancel')
}
</script>

<style scoped lang="scss">
.dialog-sheet {
  padding: 30px;
  width: fit-content;
  margin: auto;
}
</style>
