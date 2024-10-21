<template>
  <v-main>
    <v-empty-state
      :headline="t('notFound.headline')"
      :title="t('notFound.title')"
      :text="t('notFound.text')"
    />
  </v-main>
</template>

<script lang="ts" setup>
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
</script>

<i18n>
{
  "de": {
    "notFound": {
      "headline": "Wo willst du hin?",
      "title": "404, Seite nicht gefunden",
      "text": "Die Seite nach der du suchst gibt es nicht"
    }
  },
  "en": {
    "notFound": {
      "headline": "Where are you going?",
      "title": "404, Page not found",
      "text": "The page you're looking for doesn't exist"
    }
  }
}
</i18n>