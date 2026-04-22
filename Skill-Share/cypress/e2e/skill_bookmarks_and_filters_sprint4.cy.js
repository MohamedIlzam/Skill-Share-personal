describe('Sprint 4 - Skill Bookmarking & Filtering', () => {

    const TEST_EMAIL = 'arun@gmail.com';
    const TEST_PASSWORD = '12345678';
    const SEARCH_PROVIDER = 'Heshanth'; 
    const GHOST_PROVIDER = 'NonExistentUser123';

    it('Complete Skill Management Workflow (Bookmarks, Filters, and Search)', () => {
        // --- STEP 1: INITIALIZATION ---
        cy.login(TEST_EMAIL, TEST_PASSWORD);
        cy.visit('/browse');
        cy.get('.skill-row', { timeout: 15000 }).should('be.visible');

        // --- STEP 2: BOOKMARK MANAGEMENT ---
        cy.get('.skill-row').first().find('h4.skill-title').invoke('text').then((nameText) => {
            const skillName = nameText.trim();
            
            // Clean slate: Unbookmark if needed
            cy.contains('.skill-row', skillName).then(($row) => {
                if ($row.find('i.bi-bookmark-fill').length > 0) {
                    cy.wrap($row).find('button[type="submit"]').click({ force: true });
                    cy.contains('Bookmark removed', { timeout: 10000 }).should('be.visible');
                }
            });

            // Action: Toggle Bookmark On
            cy.contains('.skill-row', skillName).find('i.bi-bookmark').parent('button').click({ force: true });
            cy.contains('Skill bookmarked', { timeout: 10000 }).should('be.visible');
            
            // Verify: Bookmarked View
            cy.contains('Bookmarked').click({ force: true });
            cy.url().should('include', 'view=bookmarked');
            cy.get('.skill-row').should('contain', skillName);

            // Cleanup: Toggle Bookmark Off
            cy.contains('.skill-row', skillName).find('i.bi-bookmark-fill').parent('button').click({ force: true });
            cy.contains('Bookmark removed', { timeout: 10000 }).should('be.visible');
            
            // Final check: Skill is gone from bookmarked view
            cy.get('body').should('not.contain', skillName);
        });

        // --- STEP 3: FILTERING & SEARCH ---
        cy.intercept('GET', '**/browse*').as('pageLoad');

        // Go back to Available view
        cy.contains('Available Skills').click({ force: true });
        cy.wait('@pageLoad'); // Wait for network request
        cy.wait(1000); // SSR DOM stabilization buffer

        // Search for a specific provider
        cy.get('.hero-banner input[name="q"]').clear({ force: true }).type(SEARCH_PROVIDER);
        cy.get('.hero-banner button.btn-search').click({ force: true });
        cy.wait('@pageLoad');
        cy.wait(1000); // Stabilize
        
        cy.url().should('include', `q=${SEARCH_PROVIDER}`);
        cy.get('.skill-row').first().should('contain.text', SEARCH_PROVIDER);

        // Search for non-existent provider (Negative test)
        cy.get('.hero-banner input[name="q"]').clear({ force: true }).type(GHOST_PROVIDER);
        cy.get('.hero-banner button.btn-search').click({ force: true });
        cy.wait('@pageLoad');
        cy.wait(1000); // Stabilize

        cy.contains('No matching skills found', { timeout: 10000 }).should('be.visible');

        // Combined Filter Interaction (This triggers an AUTO-SUBMIT in the UI)
        cy.get('select[name="category"]').select(1);
        cy.wait('@pageLoad');
        cy.wait(1000); // Stabilize auto-submit reload

        cy.get('.hero-banner input[name="q"]').clear({ force: true }).type(SEARCH_PROVIDER);
        cy.get('.hero-banner button.btn-search').click({ force: true });
        cy.wait('@pageLoad');
        cy.wait(1000);
        
        // Final assertion on state persistence
        cy.url().should('include', 'category=');
        cy.url().should('include', `q=${SEARCH_PROVIDER}`);
    });
});
