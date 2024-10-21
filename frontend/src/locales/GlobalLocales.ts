import { de as vuetifyDe, en as vuetifyEn } from 'vuetify/locale'

const globalLocales = {
  en: {
    $vuetify: vuetifyEn,
    error: {
      genericMessage: 'Oops, something went wrong'
    },
    date: {
      today: 'Today'
    },
    forms: {
      save: 'Save',
      cancel: 'Cancel'
    },
    validations: {
      required: '{label} must not be blank',
      email: 'E-Mail is not valid',
      passwordValidator: 'Password must fulfill requirements',
      fileSizeValidator: 'File must be less than {maxSize} MB',
      dateIsBeforeValidator: '{laterDate} must be after {earlierDate}',
      numberWithinRange: '{name} must be between {min} and {max}'
    },
    fields: {
      password: 'Password',
      firstName: 'First Name',
      lastName: 'Last Name',
      email: 'E-Mail Address',
      phone: 'Phone',
      birthday: 'Birthday',
      street: 'Street',
      houseNumber: 'House Number',
      postcode: 'Postcode',
      city: 'City',
      country: 'Country',
      confirmedPassword: 'Confirmed Password',
      profilePicture: 'Profile Picture',
      alias: 'Alias',
      jobTitle: 'Job Title',
      bio: 'Bio',
      location: 'Location',
      company: 'Company',
      positionStart: 'Position Start',
      positionEnd: 'Position End',
      description: 'Description',
      name: 'Name',
      type: 'Type',
      level: 'Level',
      institution: 'Institution',
      degreeName: 'Degree Name',
      educationStart: 'Education Start',
      educationEnd: 'Education End'
    }
  },
  de: {
    $vuetify: vuetifyDe,
    error: {
      genericMessage: 'Ups, etwas ist schief gelaufen'
    },
    date: {
      today: 'Heute'
    },
    forms: {
      save: 'Speichern',
      cancel: 'Abbrechen'
    },
    validations: {
      required: '{label} darf nicht leer sein',
      email: 'E-Mail ist nicht gültig',
      passwordValidator: 'Passwort muss Anforderungen erfüllen',
      fileSizeValidator: 'Datei muss kleiner als {maxSize} MB sein',
      dateIsBeforeValidator: '{laterDate} muss nach {earlierDate} sein',
      numberWithinRange: '{name} muss zwischen {min} und {max} sein'
    },
    fields: {
      password: 'Passwort',
      firstName: 'Vorname',
      lastName: 'Nachname',
      email: 'E-Mail Adresse',
      phone: 'Telefon',
      birthday: 'Geburtsdatum',
      street: 'Strasse',
      houseNumber: 'Hausnummer',
      postcode: 'Postleitzahl',
      city: 'Stadt',
      country: 'Land',
      confirmedPassword: 'Bestätigtes Passwort',
      profilePicture: 'Profilbild',
      alias: 'Alias',
      jobTitle: 'Tätigkeitsbezeichnung',
      bio: 'Bio',
      location: 'Standort',
      company: 'Firma',
      positionStart: 'Positionsbeginn',
      positionEnd: 'Positionsende',
      description: 'Beschreibung',
      name: 'Name',
      type: 'Typ',
      level: 'Niveau',
      institution: 'Institution',
      degreeName: 'Abschlussname',
      educationStart: 'Ausbildungsstart',
      educationEnd: 'Ausbildungsende'
    }
  }
}

export default globalLocales
