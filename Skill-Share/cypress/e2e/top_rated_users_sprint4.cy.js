describe('Top Rated Users Highlight - Sprint 4', () => {

    const TEST_EMAIL = 'arun@gmail.com';
    const TEST_PASSWORD = '12345678';

    beforeEach(() => {
        // Log in and go to the active users page
        cy.login(TEST_EMAIL, TEST_PASSWORD);
        cy.visit('/active-users');
    });

    it('Scenario 1: View top rated users section', () => {
        // Check if the "Top Rated Partners" section exists
        cy.contains('Top Rated Partners').should('be.visible');
        cy.get('.bg-primary-subtle').find('i.bi-stars').should('exist');
    });

    it('Scenario 3: Display limited number of top rated users', () => {
        // The business logic limits this to 5 users
        // We select the grid inside the Top Rated section specifically
        cy.get('h2').contains('Top Rated Partners')
            .closest('.mb-5')
            .find('.user-card')
            .should('have.length.at.most', 5);
    });

    it('Scenario 4: Show rating summary for top rated users', () => {
        // Verify that stars and numeric ratings are visible in the top rated cards
        cy.get('h2').contains('Top Rated Partners')
            .closest('.mb-5')
            .find('.user-card').first()
            .within(() => {
                cy.get('.bi-star-fill.text-warning').should('be.visible');
                cy.get('.fw-bold.text-dark').invoke('text').then((text) => {
                    const rating = parseFloat(text);
                    expect(rating).to.be.within(0, 5);
                });
            });
    });

    it('Scenario: Navigate to top rated user profile', () => {
        // Click "View Profile" on a top-rated card
        cy.get('h2').contains('Top Rated Partners')
            .closest('.mb-5')
            .find('.btn-view-profile')
            .first()
            .click();

        // Should land on a public profile page
        cy.url().should('include', '/profile/');
        // Use extended timeout for profile data rendering
        cy.contains('Main Skills Offered', { timeout: 10000 }).should('be.visible');
    });
});
