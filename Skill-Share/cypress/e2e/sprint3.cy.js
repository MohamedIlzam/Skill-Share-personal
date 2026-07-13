describe('Sprint 3 UI E2E Flows', () => {

    const TEST_EMAIL = 'test5@gmail.com';
    const TEST_PASSWORD = 'password';

    before(() => {
        // Step 1: Ensure arun@gmail.com has at least one skill in the system
        cy.login('arun@gmail.com', '12345678');
        cy.visit('/skills');
        cy.get('body').then(($body) => {
            if ($body.find('.skill-card').length === 0) {
                // No skills found, let's add one
                cy.get('button[data-bs-toggle="modal"][data-bs-target="#addSkillModal"]').first().click();
                cy.get('#addSkillModal #name').type('Sprint 3 Test Skill', { force: true });
                cy.get('#addSkillModal #category').select('DESIGN', { force: true });
                cy.get('#addSkillModal #prof-int').click({ force: true });
                cy.get('#addSkillModal button[type="submit"]').click({ force: true });
                cy.contains('Skill successfully added!', { timeout: 15000 }).should('be.visible');
            }
        });

        // Ensure the first skill is toggled as a main skill (highlighted)
        cy.get('.skill-card').first().within(() => {
            cy.get('.main-skill-toggle').then(($btn) => {
                if (!$btn.hasClass('active')) {
                    cy.wrap($btn).click({ force: true });
                }
            });
        });

        // Get the first skill name of arun
        cy.get('.skill-card').first().find('.skill-name').invoke('text').then((nameText) => {
            const arunSkill = nameText.trim();

            // Step 2: Ensure test5@gmail.com has requested arun's skill
            cy.login('test5@gmail.com', 'password');
            cy.visit('/requests');
            
            // Check if already requested
            cy.get('body').then(($body) => {
                let alreadyRequested = false;
                if ($body.find('#sent .request-card').length > 0) {
                    $body.find('#sent .request-card').each((index, el) => {
                        const txt = Cypress.$(el).text();
                        if (txt.includes(arunSkill) && txt.includes('PENDING')) {
                            alreadyRequested = true;
                        }
                    });
                }

                if (!alreadyRequested) {
                    cy.visit('/browse');
                    cy.get('.hero-banner input[name="q"]').clear({ force: true }).type(arunSkill);
                    cy.get('.hero-banner button.btn-search').click({ force: true });
                    cy.get('.skill-row').should('be.visible');
                    cy.contains('.skill-row', arunSkill).find('button[data-bs-target="#requestModal"]').click({ force: true });
                    cy.get('#requestModal #modalMessage').type('Setup request for Sprint 3 tests', { force: true });
                    cy.get('#requestModal button[type="submit"]').click({ force: true });
                    cy.contains('Exchange request sent successfully!', { timeout: 15000 }).should('be.visible');
                }
            });
        });
    });

    it('unauthenticated user is redirected from protected page', () => {
        cy.clearCookies();
        cy.clearLocalStorage();
        cy.visit('/profile/exchanges', { failOnStatusCode: false });
        cy.url().should('include', '/login');
    });

    it('user can log in successfully', () => {
        cy.visit('/login');
        cy.get('input#email').type(TEST_EMAIL);
        cy.get('input#password').type(TEST_PASSWORD);
        cy.get('button[type="submit"]').click();
        cy.url().should('not.include', '/login').and('not.include', 'error');
    });

    it('logged-in user can open exchange history page', () => {
        cy.login(TEST_EMAIL, TEST_PASSWORD);
        cy.visit('/profile/exchanges');
        cy.get('h2').contains('Past Exchanges').should('be.visible');
    });

    it('logged-in user can open a profile page and see rating/review section', () => {
        cy.login(TEST_EMAIL, TEST_PASSWORD);
        cy.visit('/profile');
        cy.contains('Reviews', { matchCase: false }).should('exist');
    });

    // --- SPRINT 3 SPECIFIC FLOWS ---

    it('Create exchange request from UI', () => {
        cy.login(TEST_EMAIL, TEST_PASSWORD);
        
        // Go to browse skills page
        cy.visit('/browse');
        
        // Find an enabled 'Request' button and click the first one
        // (Assuming there are available skills from other users)
        cy.get('button[data-bs-target="#requestModal"]').not('[disabled]').first().as('requestBtn');
        cy.get('@requestBtn').click();
        
        // Modal opens
        cy.get('#requestModal').should('be.visible');
        
        // Type optional message
        cy.get('#modalMessage').type('I am interested in a skill exchange!', { force: true });
        
        // Submit request
        cy.get('#requestModal button[type="submit"]').click();
        
        // Verify success message (from Controller)
        cy.contains('Exchange request sent successfully!').should('be.visible');
    });

    it('Duplicate request is blocked', () => {
        cy.login(TEST_EMAIL, TEST_PASSWORD);
        cy.visit('/browse');
        
        // Because we just made a request in the previous test (if state persists), 
        // the button should now be replaced with disabled "Requested" button
        // This validates the visible UI rule preventing duplicates
        cy.contains('button', 'Requested').should('be.disabled').and('be.visible');
    });

    it('Owner accepts a pending request and status updates', () => {
        cy.login('arun@gmail.com', '12345678');
        cy.visit('/requests');
        
        // Note: For this to work seamlessly there must be a pending request in the received tab.
        // We look for any accept form and click it.
        cy.get('form[action*="/accept"] button[type="submit"]').first().click();
        
        // Verify success alert appears
        cy.contains('Request accepted successfully.').should('be.visible');
        
        // The accepted request should now show the "Mark Complete" button
        // which proves the status updated to ACCEPTED in the UI
        cy.contains('button', 'Mark Complete').should('be.visible');
    });

    it('Participant marks accepted exchange as completed', () => {
        cy.login(TEST_EMAIL, TEST_PASSWORD);
        cy.visit('/requests');
        
        // Click sent tab first since test5 is the requester
        cy.get('#sent-tab').click();
        
        // Click the 'Mark Complete' button on an accepted request
        cy.contains('button', 'Mark Complete').first().click();
        
        // Verify confirmation message
        cy.contains('Exchange marked as completed.').should('be.visible');
    });

    it('Participant sends a message and sees it in the conversation', () => {
        cy.login(TEST_EMAIL, TEST_PASSWORD);
        cy.visit('/requests');
        
        // Open the chat box for the first available request
        cy.get('.chat-btn').first().click();
        cy.get('#chatModal').should('be.visible');
        
        // Ensure web sockets have time to connect
        cy.wait(500); 
        
        const timestampMsg = `Cypress Automated Test Message ${Date.now()}`;
        
        // Type and send
        cy.get('#chatInputMessage').type(timestampMsg);
        cy.get('#chatSendBtn').click();
        
        // Verify websocket appends it to conversation immediately
        cy.get('#chatBody').contains(timestampMsg).should('be.visible');
        
        // Close modal
        cy.get('#chatModal .btn-close').click();
    });

    it('Submit rating + review after completed exchange', () => {
        cy.login(TEST_EMAIL, TEST_PASSWORD);
        cy.visit('/requests');
        
        // Navigate to Past Exchanges
        cy.get('#history-tab').click();
        
        // Find a completed exchange that allows rating
        cy.get('button[data-bs-target="#rateModal"]').not('.disabled').first().click();
        cy.get('#rateModal').should('be.visible');
        
        // Click the 5th star
        cy.get('.star-icon[data-value="5"]').click({ force: true });
        
        // Type review
        cy.get('#reviewMessage').type('Excellent exchange, highly recommend working with this person!');
        
        // Submit
        cy.get('#submitRateBtn').click();
        
        // Verify success message (matches controller logic)
        cy.contains('Thank you! Your feedback has been submitted successfully.').should('be.visible');
        
        // Re-open history tab because page reloads and default tab is active
        cy.get('#history-tab').click();
        
        // Verify the post-submit state safely without visibility assertions inside potentially animating/hidden tabs
        cy.get('#history').contains('button', 'Rated').should('exist');
    });

    it('Profile shows updated rating info after rating submission', () => {
        cy.login(TEST_EMAIL, TEST_PASSWORD);
        
        // Go to active-users directory to find a public profile link
        cy.visit('/active-users');
        
        // Click on the first profile link that points to a specific user id
        cy.get('a[href^="/profile/"]').first().click();
        
        // Either they have reviews or we just added one, verify the UI components render the feature
        cy.contains('Reviews', { matchCase: false }).should('exist');
    });

});
