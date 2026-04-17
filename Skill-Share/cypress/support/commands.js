// -- This is a parent command --
// cy.login('email@example.com', 'password')

Cypress.Commands.add('login', (email, password) => {
    // We remove cy.session to ensure a fresh, reliable login for every test stage.
    // This prevents "Stale Session" redirects that cause element-not-found errors.
    cy.visit('/login');
    cy.get('input#email').type(email);
    cy.get('input#password').type(password);
    cy.get('button[type="submit"]').first().click();
    
    // Verify successful login (Redirection away from login page)
    cy.url().should('not.include', '/login');
});
