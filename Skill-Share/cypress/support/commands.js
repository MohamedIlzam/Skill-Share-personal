// -- This is a parent command --
// cy.login('email@example.com', 'password')

Cypress.Commands.add('login', (email, password) => {
  cy.session([email, password], () => {
    cy.visit('/login')
    cy.get('input#email').type(email)
    cy.get('input#password').type(password)
    cy.get('button[type="submit"]').click()
    // Ensure we reached a logged-in state without an error
    cy.url().should('not.include', 'error')
    cy.url().should('not.include', '/login')
  })
})
