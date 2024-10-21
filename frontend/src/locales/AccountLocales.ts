const accountLocales = {
  de: {
    account: {
      personalData: 'Persönliche Daten',
      address: 'Adresse',
      confirmPassword: 'Passwort bestätigen',
      passwordRequirements: {
        title: 'Passwort Anforderungen',
        length: 'Muss mindestens {length} Zeichen haben',
        whitespaces: 'Darf keine Leerzeichen enthalten',
        digits: 'Muss mindestens eine Zahl enthalten',
        specialChars: 'Muss mindestens ein Sonderzeichen enthalten',
        uppercase: 'Muss mindestens einen Großbuchstaben enthalten',
        lowercase: 'Muss mindestens einen Kleinbuchstaben enthalten',
        match: 'Passwörter müssen übereinstimmen'
      }
    }
  },
  en: {
    account: {
      personalData: 'Personal Data',
      address: 'Address',
      confirmPassword: 'Confirm Password',
      passwordRequirements: {
        title: 'Password Requirements',
        length: 'Must have at least {length} characters',
        whitespaces: 'Must not contain whitespaces',
        digits: 'Must contain at least one digit',
        specialChars: 'Must contain at least one special character',
        uppercase: 'Must contain at least one uppercase letter',
        lowercase: 'Must contain at least one lowercase letter',
        match: 'Passwords must match'
      }
    }
  }
}

export default accountLocales
