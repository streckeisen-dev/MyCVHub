describe('Read-only application template', () => {
  it('uses the full cover-letter section width on desktop and mobile', () => {
    const longToken = 'x'.repeat(200)
    const content = `Dear hiring team,\n\nHere is my cover letter. ${longToken}`

    cy.intercept('GET', '/api/auth/login/verify', {
      username: 'template-user',
      displayName: 'Template User',
      authLevel: 'VERIFIED',
      language: 'en',
      hasProfile: true,
      thumbnail: null
    })
    cy.intercept('GET', '/api/admin/auth/login/verify', { statusCode: 401 })
    cy.intercept('GET', '/api/cv/styles', [])
    cy.intercept('GET', '/api/cover-letter/styles', [
      { key: 'modern', name: 'Modern', description: 'Modern cover letter style' }
    ])
    cy.intercept('GET', '/api/profile', {
      workExperiences: [],
      education: [],
      skills: [],
      projects: []
    })
    cy.intercept('GET', '/api/application-template/1', {
      id: 1,
      name: 'Engineer template',
      cvConfiguration: { cvStyle: 'modern' },
      coverLetterConfiguration: {
        style: 'modern',
        language: 'en',
        mirrorProfileImage: false,
        content,
        closing: 'Regards'
      }
    })

    cy.viewport(1440, 900)
    cy.visit('/ui/applications/templates/1', {
      onBeforeLoad(win) {
        win.localStorage.setItem('i18nextLng', 'en')
      }
    })

    for (const width of [1440, 390]) {
      cy.viewport(width, 900)
      cy.contains('dt', 'Cover Letter Content')
        .next('dd')
        .find('p')
        .should(($paragraph) => {
          const paragraph = $paragraph[0]
          const section = paragraph.closest('section')
          if (!section) throw new Error('Cover letter section is missing')
          expect(paragraph.textContent).to.equal(content)
          expect(paragraph.clientWidth).to.be.greaterThan(section.clientWidth * 0.85)
          expect(paragraph.scrollWidth).to.equal(paragraph.clientWidth)
        })
    }
  })
})
