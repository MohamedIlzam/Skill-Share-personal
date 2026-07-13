describe('Story: Filter Exchange Requests and History', () => {
    const user = { email: 'arun@gmail.com', pass: '12345678' };

    beforeEach(() => {
        cy.clearCookies();
        cy.clearLocalStorage();
        cy.visit('/login');
        cy.get('input[name="email"]').type(user.email);
        cy.get('input[name="password"]').type(user.pass);
        cy.get('button[type="submit"]').click();
        cy.visit('/requests');
    });

    it('Scenario 1 & 6: View all sent requests without filter', () => {
        cy.get('#sent-tab').click();
        cy.get('#sent').should('be.visible');
        cy.get('select[name="outgoingStatus"]').should('have.value', '');
        
        cy.get('body').then($body => {
            if ($body.find('#sent .request-card').length > 0) {
                cy.get('#sent .request-card').should('be.visible');
            } else {
                cy.contains('No sent requests').should('be.visible');
            }
        });
    });

    it('Scenario 2: Filter sent requests by status', () => {
        cy.get('#sent-tab').click();
        cy.get('select[name="outgoingStatus"]').select('pending');
        
        cy.location('search').should('include', 'outgoingStatus=pending');
        cy.get('#sent-tab').should('have.class', 'active');
        
        cy.get('body').then($body => {
            if ($body.find('#sent .request-card').length > 0) {
                cy.get('#sent .request-card .status-badge').each($badge => {
                    cy.wrap($badge).should('contain.text', 'PENDING');
                });
            } else {
                cy.contains(/No (matching|sent) requests/i).should('be.visible');
            }
        });
    });

    it('Scenario 3: Filter received requests by status', () => {
        cy.get('#received-tab').click();
        cy.get('select[name="incomingStatus"]').select('ongoing');
        
        cy.location('search').should('include', 'incomingStatus=ongoing');
        cy.get('#received-tab').should('have.class', 'active');
        
        cy.get('body').then($body => {
            if ($body.find('#received .request-card').length > 0) {
                cy.get('#received .request-card .status-badge').each($badge => {
                    cy.wrap($badge).should('contain.text', 'ACCEPTED');
                });
            } else {
                cy.contains(/No (matching|received) requests/i).should('be.visible');
            }
        });
    });

    it('Scenario 4: Filter exchange history by role', () => {
        cy.get('#history-tab').click();
        cy.get('select[name="historyRole"]').select('provider');
        
        cy.location('search').should('include', 'historyRole=provider');
        cy.get('#history-tab').should('have.class', 'active');
        
        cy.get('body').then($body => {
            if ($body.find('#history .request-card').length > 0) {
                cy.get('#history .request-card').contains('Provider').should('exist');
            } else {
                cy.contains(/No (matching exchanges|history records)/i).should('be.visible');
            }
        });
    });

    it('Scenario 5: Empty filter result message', () => {
        cy.get('#received-tab').click();
        cy.get('select[name="incomingStatus"]').select('rejected');
        
        cy.location('search').should('include', 'incomingStatus=rejected');
        
        cy.get('body').then($body => {
            if ($body.find('#received .request-card').length === 0) {
                cy.contains(/No (matching|received) requests/i).should('be.visible');
            }
        });
    });

    it('Scenario 7: Filtering works without errors', () => {
        cy.get('#received-tab').click();
        cy.get('select[name="incomingStatus"]').select('ongoing');
        cy.get('.alert-danger').should('not.exist');
        
        cy.get('#sent-tab').click();
        cy.get('select[name="outgoingStatus"]').select('completed');
        cy.get('.alert-danger').should('not.exist');
        
        cy.get('#history-tab').click();
        cy.get('select[name="historyRole"]').select('receiver');
        cy.get('.alert-danger').should('not.exist');
    });
});
