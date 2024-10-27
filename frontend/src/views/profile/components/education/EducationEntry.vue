<template>
	<v-row class="education-entry">
		<v-col
			class="institution"
			cols="8"
			md="10"
			>{{ education.institution }}</v-col
		>
		<v-col
			class="location"
			cols="4"
			md="2"
			>{{ education.location }}</v-col
		>
		<v-col
			class="degree-title"
			cols="8"
			md="10"
			>{{ education.degreeName }}</v-col
		>
		<v-col
			class="duration"
			cols="4"
			md="2">
			{{ toShortDate(education.educationStart) }} - {{ toShortDate(education.educationEnd) }}
		</v-col>
		<v-col
			v-if="education.description"
			class="description"
			cols="12">
			<pre>{{ education.description }}</pre>
		</v-col>
	</v-row>
</template>

<script setup lang="ts">
import type { PublicEducationDto } from '@/dto/PublicEducationDto'
import { toShortDate } from '@/services/DateHelper'
import type { EducationDto } from '@/dto/EducationDto'

defineProps<{
	education: EducationDto | PublicEducationDto
}>()
</script>

<style lang="scss" scoped>
.education-entry {
	> .institution,
	> .location {
		padding-top: 10px;
		padding-bottom: 0;
	}

	> .degree-title,
	> .duration {
		padding-top: 0;
		padding-bottom: 5px;
		font-weight: lighter;
	}

	> .institution {
		font-size: 14pt;
		font-weight: bold;
	}

	> .description {
		padding-top: 5px;
		padding-bottom: 10px;

		pre {
			white-space: break-spaces;
		}
	}
}
</style>
