import ConfirmationDialog from '../../../src/components/ConfirmationDialog.vue'

describe('ConfirmationDialog', () => {
  it('renders', () => {
    cy.mountInApp(ConfirmationDialog)
  })

  it('emits confirm when save button is clicked', () => {
    cy.mountInApp(ConfirmationDialog)
      .then(wrapper => {
        cy.get('div[test-id=dialog-controls] button[test-id=save-button]').click()
        cy.get('@vue').should(() => {
        const emitted = wrapper.emitted()
        expect(emitted).to.have.property('confirm')
        })
      })
  })

  it('emits cancel when cancel button is clicked', () => {
    cy.mountInApp(ConfirmationDialog)
      .then(wrapper => {
        cy.get('div[test-id=dialog-controls] button[test-id=cancel-button]').click()
        cy.get('@vue').should(() => {
          const emitted = wrapper.emitted()
          expect(emitted).to.have.property('cancel')
        })
      })
  })
})