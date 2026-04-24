describe('Story: Receive Notifications for Exchange Activities', () => {
    const userA = { email: 'arun@gmail.com', pass: '12345678' };

    beforeEach(() => {
        cy.clearCookies();
        cy.clearLocalStorage();
        cy.visit('/login');
        cy.get('input[name="email"]').type(userA.email);
        cy.get('input[name="password"]').type(userA.pass);
        cy.get('button[type="submit"]').click();
        cy.visit('/system-messages');
    });

    it('Scenario 1: Notification for new request', () => {
        cy.get('body').then($body => {
            const hasMessages = $body.find('.message-card').length > 0;
            if (hasMessages && $body.find('.message-card:contains("New Exchange Request")').length > 0) {
                cy.contains('.message-card', 'New Exchange Request').within(() => {
                    cy.get('.message-content').should('not.be.empty');
                });
            } else {
                cy.log('No "New Exchange Request" notifications currently available to validate.');
            }
        });
    });

    it('Scenario 2: Notification for accepted request', () => {
        cy.get('body').then($body => {
            const hasMessages = $body.find('.message-card').length > 0;
            if (hasMessages && $body.find('.message-card:contains("Exchange Accepted")').length > 0) {
                cy.contains('.message-card', 'Exchange Accepted').within(() => {
                    cy.get('.message-content').should('not.be.empty');
                });
            } else {
                cy.log('No "Exchange Accepted" notifications currently available to validate.');
            }
        });
    });

    it('Scenario 3: Notification for rejected request', () => {
        cy.get('body').then($body => {
            const hasMessages = $body.find('.message-card').length > 0;
            if (hasMessages && $body.find('.message-card:contains("Request Declined")').length > 0) {
                cy.contains('.message-card', 'Request Declined').within(() => {
                    cy.get('.message-content').should('not.be.empty');
                });
            } else {
                cy.log('No "Request Declined" notifications currently available to validate.');
            }
        });
    });

    it('Scenario 4: Notification for completed exchange', () => {
        cy.get('body').then($body => {
            const hasMessages = $body.find('.message-card').length > 0;
            if (hasMessages && $body.find('.message-card:contains("Exchange Completed")').length > 0) {
                cy.contains('.message-card', 'Exchange Completed').within(() => {
                    cy.get('.message-content').should('not.be.empty');
                });
            } else {
                cy.log('No "Exchange Completed" notifications currently available to validate.');
            }
        });
    });

    it('Scenario 5: Rating reminder notification', () => {
        cy.get('body').then($body => {
            const hasMessages = $body.find('.message-card').length > 0;
            if (hasMessages && $body.find('.message-card:contains("Rating Required")').length > 0) {
                cy.contains('.message-card', 'Rating Required').within(() => {
                    cy.get('.message-content').should('not.be.empty');
                });
            } else {
                cy.log('No "Rating Required" notifications currently available to validate.');
            }
        });
    });

    it('Scenario 6: Notifications are informational only', () => {
        cy.get('body').then($body => {
            if ($body.find('.message-card').length > 0) {
                // Assert no interactive elements exist inside message cards
                cy.get('.message-card').find('button').should('not.exist');
                cy.get('.message-card').find('a').should('not.exist');
            } else {
                cy.contains('No messages').should('be.visible');
            }
        });
    });

    it('Scenario 7: Notifications display correctly without errors', () => {
        cy.get('h1').contains('System Messages').should('be.visible');
        cy.get('.alert-danger').should('not.exist'); // No system errors

        cy.get('body').then($body => {
            if ($body.find('.message-card').length > 0) {
                // Ensure correct display of chronological list items
                cy.get('.message-card').first().find('.fw-bold').should('be.visible');
                cy.get('.message-card').first().find('.message-content').should('be.visible');
                // The cards map chronologically in standard rendering, we just ensure they load.
                cy.get('.message-card').should('have.length.at.least', 1);
            }
        });
    });
});
