<template>
  <v-main>
    <v-container class="home-view">
      <v-row align="center" justify="center" class="headline">
        <v-col cols="12" class="text-center">
          <h1>Welcome to MyCVHub</h1>
          <p>Create and share your professional CV online with ease!</p>
          <router-link :to="{ name: 'signup' }">
            <v-btn color="primary" large :to="{ name: 'login' }">Get Started</v-btn>
          </router-link>
        </v-col>
      </v-row>

      <!-- Features Section -->
      <v-row>
        <v-col>
          <h2 class="text-center">Features</h2>
        </v-col>
      </v-row>
      <v-row justify="center">
        <v-col cols="12" sm="6" md="4" lg="3" v-for="(feature, index) in features" :key="index">
          <v-card>
            <v-card-title>{{ feature.title }}</v-card-title>
            <v-card-text>
              {{ feature.description }}
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Call to Action Section -->
      <v-row align="center" justify="center" class="call-to-action">
        <v-col cols="12" md="8" class="text-center">
          <h2>Ready to create your online CV?</h2>
          <router-link :to="{ name: 'signup' }">
            <v-btn color="primary" large>Sign up now</v-btn>
          </router-link>
        </v-col>
      </v-row>
    </v-container>
  </v-main>
</template>

<script setup lang="ts">
import accountApi from '@/api/AccountApi'
import router from '@/router'

const features = [
  {
    title: 'Create Your CV',
    description: 'Easily create a professional CV using our user-friendly interface.'
  },
  {
    title: 'Share with a Link',
    description: 'Share your CV with potential employers using a unique link.'
  } /*,
  {
    title: 'Track Applications',
    description: 'Keep track of your job applications and their status all in one place.'
  }*/
]

if (accountApi.isUserLoggedIn()) {
  await router.push({ name: 'account' })
}
</script>

<style lang="scss" scoped>
.home-view {
  display: flex;
  flex-direction: column;

  .headline {
    p {
      margin-bottom: 10px;
    }
  }

  .call-to-action h2 {
    margin-bottom: 10px;
  }
}
</style>
