import { FormButtons } from '@/components/FormButtons.tsx'

describe('<FormButtons />', () => {
  it('renders', () => {
    cy.mount(<FormButtons isSaving={false} onCancel={() => {/* empty */}}/>)
  })
})