<template>
  <v-main>
    <v-container fluid>
      <!-- Privacy Policy Header -->
      <v-row justify="center">
        <v-col cols="12" md="8">
          <v-card>
            <v-card-title>
              <h1>Privacy Policy</h1>
            </v-card-title>
            <v-card-subtitle>
              This Privacy Policy explains how MyCVHub collects, uses, and manages personal data.
            </v-card-subtitle>
          </v-card>
        </v-col>
      </v-row>

      <!-- Privacy Policy Content -->
      <v-row justify="center">
        <v-col cols="12" md="8">
          <v-card>
            <v-card-text class="policy-content">

              <div class="policy-section">
              <h2>1. Introduction</h2>
              <p>
                This Privacy Policy explains how MyCVHub collects, uses, and manages personal data
                for registered users.
              </p>
              </div>

              <div class="policy-section">
              <h2>2. Data Collection</h2>
              <p>MyCVHub collects the following personal information:</p>
              <ul>
                <li>
                  <strong>Profile Information</strong>: Name, address, phone number, and CV content,
                  as provided by users.
                </li>
                <li>
                  <strong>Profile Picture</strong>: The profile picture is stored securely with our
                  third-party provider, Cloudinary.
                </li>
              </ul>
              </div>

              <div class="policy-section">
              <h2>3. Data Usage</h2>
              <p>Collected data is used to:</p>
              <ul>
                <li>Enable users to create, edit, and share their CVs.</li>
                <li>
                  Allow users to make their CV publicly accessible, with controls over which data
                  fields (email, phone, address) appear on the public CV.
                </li>
              </ul>
              </div>

              <div class="policy-section">
              <h2>4. Data Storage and Security</h2>
              <p>
                <strong>User Data</strong>: Personal information is stored securely in our database.
                By default, only the user has access to this data, unless they opt to make specific
                information public. The developer has limited database access for maintenance
                purposes only.
              </p>
              <p>
                <strong>Profile Pictures</strong>: Profile pictures are stored and managed through
                Cloudinary, a secure third-party storage provider, with restricted access to ensure
                privacy.
              </p>
              </div>

              <div class="policy-section">
              <h2>5. Data Access and User Control</h2>
              <p>
                MyCVHub provides users with direct access to their data. Users can view, edit, and
                delete any information, including their account and profile picture, directly within
                the app. No external data access requests are required.
              </p>
              </div>

              <div class="policy-section">
              <h2>6. Data Sharing</h2>
              <p>
                MyCVHub does not share any user data with third parties, except as required for
                media storage. Publicly shared CVs are accessible only by the shared link and
                contain only the information the user has designated as public.
              </p>
              </div>

              <div class="policy-section">
              <h2>7. Use of Third-Party Services</h2>
              <p>
                MyCVHub uses Cloudinary solely for the storage and retrieval of profile pictures.
                Cloudinary adheres to strict security standards to ensure the privacy and security
                of stored images.
              </p>
              </div>

              <div class="policy-section">
              <h2>8. Contact Information</h2>
              <p>
                For questions or concerns, please contact us at
                <a :href="`mailto:${contactEmail}`">{{ contactEmail }}</a>.
              </p>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </v-main>
</template>

<script setup lang="ts">
const contactEmail = 'contact@mycvhub.ch'
</script>

<style lang="scss" scoped>
.policy-content {
  padding: 30px;

  .policy-section {
    margin-bottom: 10px;

    ul {
      padding-left: 15px;
      padding-bottom: 5px;
    }

    h2 {
      margin-bottom: 5px;
    }
  }
}
</style>
