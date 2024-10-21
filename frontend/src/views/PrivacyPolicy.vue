<template>
  <v-main>
    <v-container fluid>
      <v-row justify="center">
        <v-col cols="12" md="8">
          <v-card>
            <v-card-title>
              <h1>{{ t('privacy.title') }}</h1>
            </v-card-title>
            <v-card-subtitle class="policy-subtitle">
              {{ t('privacy.description') }}
            </v-card-subtitle>
          </v-card>
        </v-col>
      </v-row>

      <v-row justify="center">
        <v-col cols="12" md="8">
          <v-card>
            <v-card-text class="policy-content">
              <div class="policy-section">
                <h2>1. {{ t('privacy.intro.title') }}</h2>
                <p>{{ t('privacy.intro.content') }}</p>
              </div>

              <div class="policy-section">
                <h2>2. {{ t('privacy.collection.title') }}</h2>
                <p>{{ t('privacy.collection.content') }}</p>
                <ul>
                  <li>
                    <strong>{{ t('privacy.collection.profileInfo.title') }}</strong>:
                    {{ t('privacy.collection.profileInfo.content') }}
                  </li>
                  <li>
                    <strong>{{ t('privacy.collection.profilePicture.title') }}</strong>:
                    {{ t('privacy.collection.profilePicture.content') }}
                  </li>
                </ul>
              </div>

              <div class="policy-section">
                <h2>3. {{ t('privacy.usage.title') }}</h2>
                <p>{{ t('privacy.usage.content') }}</p>
                <ul>
                  <li>{{ t('privacy.usage.usage1') }}</li>
                  <li>
                    {{ t('privacy.usage.usage2') }}
                  </li>
                </ul>
              </div>

              <div class="policy-section">
                <h2>4. {{ t('privacy.storage.title') }}</h2>
                <p>
                  <strong>{{ t('privacy.storage.userData.title') }}</strong>:
                  {{ t('privacy.storage.userData.content') }}
                </p>
                <p>
                  <strong>{{ t('privacy.storage.pictures.title') }}</strong>:
                  {{ t('privacy.storage.pictures.content') }}
                </p>
              </div>

              <div class="policy-section">
                <h2>5. {{ t('privacy.access.title') }}</h2>
                <p>{{ t('privacy.access.content') }}</p>
              </div>

              <div class="policy-section">
                <h2>6. {{ t('privacy.sharing.title') }}</h2>
                <p>{{ t('privacy.sharing.content') }}</p>
              </div>

              <div class="policy-section">
                <h2>7. {{ t('privacy.third-party.title') }}</h2>
                <p>{{ t('privacy.third-party.content') }}</p>
              </div>

              <div class="policy-section">
                <h2>8. {{ t('privacy.contact.title') }}</h2>
                <p>
                  {{ t('privacy.contact.content') }}
                  <a :href="`mailto:${contactEmail}`">{{ contactEmail }}</a
                  >.
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
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const contactEmail = 'contact@mycvhub.ch'
</script>

<i18n>
{
  "de": {
    "privacy": {
      "title": "Datenschutzerklärung",
      "description": "In dieser Datenschutzerklärung wird erläutert, wie MyCVHub personenbezogene Daten sammelt, verwendet und verwaltet.",
      "intro": {
        "title": "Einleitung",
        "content": "In dieser Datenschutzerklärung wird erläutert, wie MyCVHub personenbezogene Daten von registrierten Benutzern sammelt, verwendet und verwaltet."
      },
      "collection": {
        "title": "Datenerhebung",
        "content": "MyCVHub sammelt die folgenden persönlichen Informationen:",
        "profileInfo": {
          "title": "Profil Informationen",
          "content": "Name, Adresse, Telefonnummer und Inhalt des Lebenslaufs, wie von den Nutzern angegeben."
        },
        "profilePicture": {
          "title": "Profil Bild",
          "content": "Das Profilbild wird sicher bei unserem Drittanbieter, Cloudinary, gespeichert."
        }
      },
      "usage": {
        "title": "Datenverwendung",
        "content": "Die gesammelten Daten werden verwendet, um:",
        "usage1": "Nutzern zu ermöglichen, ihre Lebensläufe zu erstellen, zu bearbeiten und zu teilen.",
        "usage2": "Nutzern zu ermöglichen, ihren Lebenslauf öffentlich zugänglich zu machen, wobei kontrolliert werden kann, welche Datenfelder (E-Mail, Telefon, Adresse) im öffentlichen Lebenslauf erscheinen."
      },
      "storage": {
        "title": "Datenspeicherung und -sicherheit",
        "userData": {
          "title": "Benutzerdaten",
          "content": "Persönliche Informationen werden sicher in unserer Datenbank gespeichert. Standardmäßig hat nur der Nutzer Zugang zu diesen Daten, es sei denn, er entscheidet sich dafür, bestimmte Informationen öffentlich zu machen. Der Entwickler hat nur zu Wartungszwecken begrenzten Zugriff auf die Datenbank."
        },
        "pictures": {
          "title": "Profilbilder",
          "content": "Profilbilder werden über Cloudinary, einen sicheren Drittanbieter, mit eingeschränktem Zugriff gespeichert und verwaltet, um den Datenschutz zu gewährleisten."
        }
      },
      "access": {
        "title": "Datenzugriff und Nutzerkontrolle",
        "content": "MyCVHub bietet den Nutzern einen direkten Zugang zu ihren Daten. Die Nutzer können alle Informationen, einschließlich ihres Kontos und Profilbildes, direkt in der App anzeigen, bearbeiten und löschen. Es sind keine externen Datenzugriffsanfragen erforderlich."
      },
      "sharing": {
        "title": "Datenweitergabe",
        "content": "MyCVHub gibt keine Nutzerdaten an Dritte weiter, es sei denn, dies ist für die Medienspeicherung erforderlich. Öffentlich freigegebene Lebensläufe sind nur über den freigegebenen Link zugänglich und enthalten nur die Informationen, die der Nutzer als öffentlich gekennzeichnet hat."
      },
      "third-party": {
        "title": "Nutzung von Diensten Dritter",
        "content": "MyCVHub nutzt Cloudinary ausschließlich für die Speicherung und den Abruf von Profilbildern. Cloudinary hält sich an strenge Sicherheitsstandards, um die Privatsphäre und Sicherheit der gespeicherten Bilder zu gewährleisten."
      },
      "contact": {
        "title": "Kontaktinformationen",
        "content": "Bei Fragen oder Anliegen kontaktieren Sie uns bitte unter"
      }
    }
  },
  "en": {
    "privacy": {
      "title": "Privacy Policy",
      "description": "This Privacy Policy explains how MyCVHub collects, uses, and manages personal data.",
      "intro": {
        "title": "Introduction",
        "content": "This Privacy Policy explains how MyCVHub collects, uses, and manages personal data for registered users."
      },
      "collection": {
        "title": "Data Collection",
        "content": "MyCVHub collects the following personal information:",
        "profileInfo": {
          "title": "Profile Information",
          "content": "Name, address, phone number, and CV content, as provided by users."
        },
        "profilePicture": {
          "title": "Profile Picture",
          "content": "The profile picture is stored securely with our third-party provider, Cloudinary."
        }
      },
      "usage": {
        "title": "Data Usage",
        "content": "Collected data is used to:",
        "usage1": "Enable users to create, edit, and share their CVs.",
        "usage2": "Allow users to make their CV publicly accessible, with controls over which data fields (email, phone, address) appear on the public CV."
      },
      "storage": {
        "title": "Data Storage and Security",
        "userData": {
          "title": "User Data",
          "content": "Personal information is stored securely in our database. By default, only the user has access to this data, unless they opt to make specific information public. The developer has limited database access for maintenance purposes only."
        },
        "pictures": {
          "title": "Profile Pictures",
          "content": "Profile pictures are stored and managed through Cloudinary, a secure third-party storage provider, with restricted access to ensure privacy."
        }
      },
      "access": {
        "title": "Data Access and User Control",
        "content": "MyCVHub provides users with direct access to their data. Users can view, edit, and delete any information, including their account and profile picture, directly within the app. No external data access requests are required."
      },
      "sharing": {
        "title": "Data Sharing",
        "content": "MyCVHub does not share any user data with third parties, except as required for media storage. Publicly shared CVs are accessible only by the shared link and contain only the information the user has designated as public."
      },
      "third-party": {
        "title": "Use of Third-Party Services",
        "content": "MyCVHub uses Cloudinary solely for the storage and retrieval of profile pictures. Cloudinary adheres to strict security standards to ensure the privacy and security of stored images."
      },
      "contact": {
        "title": "Contact Information",
        "content": "For questions or concerns, please contact us at"
      }
    }
  }
}
</i18n>

<style lang="scss" scoped>
.policy-subtitle {
  padding-bottom: 10px;
}

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
