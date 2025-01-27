import FormButtons from '../../../src/components/FormButtons.vue'

describe('FormButtons', () => {
  it('renders', () => {
    // see: https://on.cypress.io/mounting-vue
    cy.mountInApp(FormButtons)
  })

  it('emits save when save button is clicked', () => {
    cy.mountInApp(FormButtons)
      .then(wrapper => {
        wrapper.find('button[test-id=save-button]').trigger('click')
        const emitted = wrapper.emitted()
        expect(emitted).to.have.property('save')
      })
  })

  it('emits cancel when cancel button is clicked', () => {
    cy.mountInApp(FormButtons)
      .then(wrapper => {
        wrapper.find('button[test-id=cancel-button]').trigger('click')
        const emitted = wrapper.emitted()
        expect(emitted).to.have.property('cancel')
      })
  })
})