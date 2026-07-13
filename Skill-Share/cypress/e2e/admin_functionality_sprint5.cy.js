describe('Complete Admin Functionality', () => {
    const admin = { email: 'admin@skillshare.com', pass: 'admin123' };
    const targetUser = 'Arun';

    beforeEach(() => {
        cy.viewport(1280, 800);
        cy.clearCookies();
        cy.clearLocalStorage();
        cy.visit('/login');
        cy.get('input[name="email"]').type(admin.email);
        cy.get('input[name="password"]').type(admin.pass);
        cy.get('button[type="submit"]').click();
        cy.visit('/admin/users');
    });

    it('TC01: Verify Admin can log in successfully', () => {
        cy.get('.navbar-brand-text').should('contain.text', 'SkillShare Admin');
    });

    it('TC02: Verify Admin can access the "Users" list and view the data table', () => {
        cy.get('h1').should('contain.text', 'Manage Users');
        cy.get('.table-custom').should('be.visible');
        cy.get('table tbody tr').should('have.length.at.least', 1);
    });

    it('TC03: Verify Admin can search for a specific user using the search bar', () => {
        cy.get('input[name="query"]').type(targetUser);
        cy.get('#adminSearchBtn').click();
        
        cy.get('table tbody tr').each(($row) => {
            cy.wrap($row).should('contain.text', targetUser);
        });
    });

    it('TC04: Verify Admin can navigate to a specific user\'s detailed profile from the search results', () => {
        cy.get('input[name="query"]').type(targetUser);
        cy.get('#adminSearchBtn').click();
        
        cy.contains('td', targetUser).parent().find('a').contains('View Profile').click();
        
        cy.get('.card-custom').should('be.visible');
        cy.get('h2').should('contain.text', targetUser);
    });

    it('TC05: Verify the user\'s statistics and account status are displayed correctly', () => {
        cy.get('input[name="query"]').type(targetUser);
        cy.get('#adminSearchBtn').click();
        cy.contains('td', targetUser).parent().find('a').contains('View Profile').click();
        
        cy.contains('.stat-box', 'Listed Skills').should('be.visible');
        cy.contains('.stat-box', 'Total Ratings').should('be.visible');
        cy.contains('.stat-box', 'Avg Rating').should('be.visible');
        cy.get('.badge').contains(/(ACTIVE|INACTIVE)/).should('be.visible');
    });

    it('TC06: Verify Admin can deactivate an ACTIVE user account', () => {
        cy.get('input[name="query"]').type(targetUser);
        cy.get('#adminSearchBtn').click();
        cy.contains('td', targetUser).parent().find('a').contains('View Profile').click();

        cy.get('body').then($body => {
            if ($body.find('button:contains("Deactivate Account")').length > 0) {
                // Intercept the JS confirm dialog and automatically accept it
                cy.on('window:confirm', () => true);
                cy.contains('button', 'Deactivate Account').click();
                cy.get('.alert-success').should('be.visible');
                cy.get('.badge.bg-danger').should('contain.text', 'INACTIVE');
            } else {
                cy.log('User is already inactive, skipping deactivation step.');
            }
        });
    });

    it('TC07: Verify Admin can reactivate an INACTIVE user account', () => {
        cy.get('input[name="query"]').type(targetUser);
        cy.get('#adminSearchBtn').click();
        cy.contains('td', targetUser).parent().find('a').contains('View Profile').click();

        cy.get('body').then($body => {
            if ($body.find('button:contains("Reactivate Account")').length > 0) {
                cy.contains('button', 'Reactivate Account').click();
                cy.get('.alert-success').should('be.visible');
                cy.get('.badge.bg-success').should('contain.text', 'ACTIVE');
            } else {
                cy.log('User is already active, skipping reactivation step.');
            }
        });
    });

    it('TC08 & TC09: Verify Admin can successfully write and send a system message to a user', () => {
        cy.get('input[name="query"]').type(targetUser);
        cy.get('#adminSearchBtn').click();
        cy.contains('td', targetUser).parent().find('a').contains('View Profile').click();

        const testMessage = 'Hello ' + targetUser + ', this is a test message. ' + Date.now();
        cy.get('textarea[name="content"]').type(testMessage);
        cy.contains('button', 'Send Message').click();
        
        cy.get('.alert-success').should('be.visible');
    });

    it('TC10: Verify the newly sent message appears in the Admin\'s "Message History" section', () => {
        cy.get('input[name="query"]').type(targetUser);
        cy.get('#adminSearchBtn').click();
        cy.contains('td', targetUser).parent().find('a').contains('View Profile').click();

        const testMessage = 'History Test Message ' + Date.now();
        cy.get('textarea[name="content"]').type(testMessage);
        cy.contains('button', 'Send Message').click();

        cy.get('.card-custom').contains('Message History').parent().within(() => {
            cy.contains(testMessage).should('be.visible');
            cy.contains('Admin').should('be.visible');
        });
    });

    it('TC11: Verify Admin chat history is strictly isolated and does not display automated exchange notifications', () => {
        cy.get('input[name="query"]').type(targetUser);
        cy.get('#adminSearchBtn').click();
        cy.contains('td', targetUser).parent().find('a').contains('View Profile').click();

        cy.get('body').then($body => {
            const historyHeader = $body.find('h6:contains("Message History")');
            if (historyHeader.length > 0) {
                cy.get('.card-custom').contains('Message History').parent().then($historyBlock => {
                    const historyText = $historyBlock.text();
                    expect(historyText).not.to.include('Exchange Accepted');
                    expect(historyText).not.to.include('New Exchange Request');
                    expect(historyText).not.to.include('Rating Required');
                    expect(historyText).not.to.include('Request Declined');
                    expect(historyText).not.to.include('Exchange Completed');
                });
            } else {
                cy.log('No message history exists yet to verify isolation.');
            }
        });
    });
});
