<template>
  <div class="form-action-buttons">
    <v-btn
      test-id="save-button"
      type="submit"
      :text="submitText || t('forms.save')"
      :color="submitColor || 'primary'"
      @click.prevent="save"
      :loading="isSaving"
    />
    <v-btn
      test-id="cancel-button"
      :text="cancelText || t('forms.cancel')"
      @click="cancel"
      :disabled="isSaving"
      :color="cancelColor || 'surface'"
      variant="flat"
    />
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n({
  useScope: 'global'
})

defineProps<{
  isSaving?: boolean,
  submitText?: string
  cancelText?: string,
  submitColor?: string
  cancelColor?: string
}>()

const emit = defineEmits(['save', 'cancel'])

function save() {
  emit('save')
}

function cancel() {
  emit('cancel')
}
</script>

<style scoped lang="scss">
.form-action-buttons {
  margin-top: 10px;
  display: flex;
  gap: 10px;
}
</style>
