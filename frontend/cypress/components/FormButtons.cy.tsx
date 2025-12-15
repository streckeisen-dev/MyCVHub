import { FormButtons } from '@/components/FormButtons.tsx'

describe('<FormButtons />', () => {
  function dummyHandleCancel() {
    // empty
  }

  it('triggers cancel on cancel-click', () => {
    const handleCancel = cy.spy().as('handleCancel')
    cy.mount(<FormButtons onCancel={handleCancel} isSaving={false} />)
    cy.get('[data-testid="cancel-button"]').should('exist').click()
    cy.get('@handleCancel').should('have.been.calledOnce')
  })

  it('buttons are disabled when saving', () => {
    cy.mount(<FormButtons onCancel={dummyHandleCancel} isSaving={true} />)
    cy.get('[data-testid="save-button"]').should('be.disabled')
    cy.get('[data-testid="cancel-button"]').should('be.disabled')
    cy.get('[data-testid="save-button"] div[aria-label="Loading"]').should('exist')
  })

  it('submits form on save-click', () => {
    const handleSubmit = cy.spy().as('handleSubmit')
    cy.mount(
      <form onSubmit={handleSubmit}>
        <FormButtons onCancel={dummyHandleCancel} isSaving={false} />
      </form>
    )
    cy.get('[data-testid="save-button"]').should('exist').click()
    cy.get('@handleSubmit').should('have.been.calledOnce')
  })
})
