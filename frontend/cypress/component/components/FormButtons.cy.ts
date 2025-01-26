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
        const saveEmit = wrapper.emitted('save')
        expect(saveEmit).to.have.length
        expect(saveEmit[0][0]).to.equal(undefined)
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