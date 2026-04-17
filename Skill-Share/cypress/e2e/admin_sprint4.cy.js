describe('Sprint 4 - Admin Management E2E Suite (Hardened & Optimized)', () => {

    const ADMIN_EMAIL = 'admin@skillshare.com';
    const ADMIN_PASSWORD = 'admin123';
    const TEST_USER_EMAIL = 'arun@gmail.com';
    const TEST_USER_PASSWORD = '12345678';
    const TEST_USER_NAME = 'Arun'; 

    beforeEach(() => {
        // We handle session clearing inside the login command for stability
    });

    /**
     * STAGE 1: AUTHENTICATION & DASHBOARD
     */
    it('LIFECYCLE: Standard Admin Monitoring & Security', () => {
        // 1. Initial Access Check (Admin Dashboard)
        cy.login(ADMIN_EMAIL, ADMIN_PASSWORD);
        cy.visit('/admin/dashboard');
        cy.contains('System Overview').should('be.visible');

        // 2. Data Accuracy (Stats vs List)
        cy.visit('/admin/users');
        cy.get('tbody tr').then($rows => {
            const listCount = $rows.length;
            cy.visit('/admin/dashboard');
            cy.contains('Total Users').parent().find('.stat-value').then($val => {
                expect(parseInt($val.text())).to.equal(listCount);
            });
        });

        // 3. Profile Monitoring
        cy.visit('/admin/users');
        cy.get('input[name="query"]').type(TEST_USER_EMAIL);
        cy.get('#adminSearchBtn').click();
        // Wait for search results to render
        cy.get('a').contains('View Profile', { timeout: 10000 }).should('be.visible').first().click();
        cy.get('h2').should('contain', TEST_USER_NAME);
        cy.contains('Total Ratings').should('be.visible');
    });

    /**
     * STAGE 2: ACCOUNT LOCKDOWN (DEACTIVATION)
     */
    it('LIFECYCLE: Enforce User Deactivation & Access Blocking', () => {
        // 1. Deactivate the user
        cy.login(ADMIN_EMAIL, ADMIN_PASSWORD);
        cy.visit('/admin/users');
        cy.get('input[name="query"]').type(TEST_USER_EMAIL);
        cy.get('#adminSearchBtn').click();
        // Wait for search results to render
        cy.get('a').contains('View Profile', { timeout: 10000 }).should('be.visible').first().click();
        cy.get('button').contains('Deactivate Account').click();
        cy.contains('deactivated successfully').should('be.visible');

        // 2. PROVE ACCESS IS BLOCKED (RBAC Check)
        cy.visit('/login');
        cy.get('input#email').type(TEST_USER_EMAIL);
        cy.get('input#password').type(TEST_USER_PASSWORD);
        cy.get('button[type="submit"]').first().click();
        
        // Success criteria: Either URL contains ?disabled OR body contains "deactivated"
        cy.url().should('include', 'disabled');
        cy.contains('Your account has been deactivated').should('be.visible');

        // 3. PROVE ADMIN ROUTES ARE BLOCKED (Visit without session)
        cy.clearAllCookies();
        cy.clearAllLocalStorage();
        cy.visit('/admin/dashboard', { failOnStatusCode: false });
        cy.url().should('include', '/login');
        cy.get('body').should('not.contain', 'System Overview');
    });

    /**
     * STAGE 3: RESTORATION (REACTIVATION)
     */
    it('LIFECYCLE: Restore User Access & Clean Data State', () => {
        // 1. Reactivate the user
        cy.login(ADMIN_EMAIL, ADMIN_PASSWORD);
        cy.visit('/admin/users');
        cy.get('input[name="query"]').type(TEST_USER_EMAIL);
        cy.get('#adminSearchBtn').click();
        // Wait for search results to render
        cy.get('a').contains('View Profile', { timeout: 10000 }).should('be.visible').first().click();
        cy.get('button').contains('Reactivate Account').click();
        cy.contains('reactivated successfully').should('be.visible');

        // 2. Final Message Verification (Security Constraint)
        // We are already on the user's profile page after the redirect
        cy.get('textarea[name="content"]').invoke('removeAttr', 'required'); 
        cy.get('button').contains('Send Message').first().click();
        cy.contains('delivered').should('not.exist'); // Should fail validation on empty
    });

});
