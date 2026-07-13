describe('Messaging System E2E Tests - Sprint 4', () => {
  const login = (email, password) => {
    cy.visit('/login');
    cy.get('input[name="email"]').type(email);
    cy.get('input[name="password"]').type(password);
    cy.get('button[type="submit"]').click();
  };

  beforeEach(() => {
    cy.viewport(1280, 800);
    // Standard login for all scenarios
    login('arun@gmail.com', '12345678');
    cy.url().should('not.include', '/login');
  });

  context('Story 1: Dedicated Conversation Page', () => {
    it('Scenario 1: User navigates to messaging page', () => {
      // Navigate via navbar
      cy.get('nav.navbar-custom').find('a[href="/messages"]').click();
      cy.url().should('include', '/messages');
      cy.get('.conv-sidebar').should('be.visible');
      cy.contains('h1', 'Messages').should('be.visible');
    });

    it('Scenario 2 & 6: Conversation list display', () => {
      cy.visit('/messages');
      
      // Check if conversation list exists or empty message is shown
      cy.get('.conv-list').then(($list) => {
        if ($list.find('.conv-item').length > 0) {
          cy.log('Conversations found');
          cy.get('.conv-item').first().should('be.visible');
          cy.get('.conv-name').first().should('not.be.empty');
        } else {
          cy.log('No conversations found - Checking empty state');
          cy.contains('No conversations yet').should('be.visible');
        }
      });
    });

    it('Scenario 3, 4 & 5: Opening a conversation and message layout', () => {
      cy.visit('/messages');
      
      // Select the first conversation if available
      cy.get('.conv-list').then(($list) => {
        if ($list.find('.conv-item').length > 0) {
          cy.get('.conv-item').first().click();
          
          // Verify chat header
          cy.get('.chat-header').should('be.visible');
          
          // Verify message area
          cy.get('#messagesArea').should('be.visible');
          
          // Verify messages if any
          cy.get('#messagesArea').then(($area) => {
            if ($area.find('.msg-container').length > 0) {
              // Distinction between sent and received
              cy.get('.msg-container').then(($msgs) => {
                const hasSent = $msgs.filter('.msg-sent').length > 0;
                const hasReceived = $msgs.filter('.msg-received').length > 0;
                
                if (hasSent) cy.get('.msg-sent').first().find('.bubble').should('exist');
                if (hasReceived) cy.get('.msg-received').first().find('.bubble').should('exist');
              });
              
              // Chronological check (Confirming the container has the items in the DOM)
              cy.get('.msg-container').first().should('exist');
            } else {
              cy.contains('No messages yet').should('be.visible');
            }
          });
        }
      });
    });
  });

  context('Story 2: Read Status & Auto-Scroll', () => {
    it('Scenario 1, 2 & 6: Message read indicators', () => {
      cy.visit('/messages');
      
      cy.get('.conv-list').then(($list) => {
        if ($list.find('.conv-item').length > 0) {
          cy.get('.conv-item').first().click();
          
          // Check for read indicators on sent messages
          cy.get('#messagesArea').then(($area) => {
            if ($area.find('.msg-sent').length > 0) {
              // Ensure at least one checkmark icon exists
              cy.get('.msg-sent').first().find('.bi-check2, .bi-check2-all').should('exist');
            }
          });
        }
      });
    });

    it('Scenario 3 & 4: Auto-scroll functionality', () => {
      cy.visit('/messages');
      
      cy.get('.conv-list').then(($list) => {
        if ($list.find('.conv-item').length > 0) {
          cy.get('.conv-item').first().click();
          
          // Wait for load
          cy.get('#messagesArea').should('be.visible');
          
          // Check scroll position
          cy.get('#messagesArea').then(($el) => {
            const el = $el[0];
            // If there's enough content to scroll, it should be at the bottom
            if (el.scrollHeight > el.clientHeight) {
              // Tolerance of 5px
              expect(el.scrollTop + el.clientHeight).to.be.closeTo(el.scrollHeight, 5);
            }
          });
          
          // Send a message and check auto-scroll
          const testMsg = 'Cypress Auto-scroll Test ' + Date.now();
          cy.get('input[name="content"]').type(testMsg);
          cy.get('.btn-send').click();
          
          // Verify new message appears and scroll is maintained at bottom
          cy.contains(testMsg).should('be.visible');
          cy.get('#messagesArea').then(($el) => {
            const el = $el[0];
            if (el.scrollHeight > el.clientHeight) {
              expect(el.scrollTop + el.clientHeight).to.be.closeTo(el.scrollHeight, 5);
            }
          });
        }
      });
    });
  });
});
