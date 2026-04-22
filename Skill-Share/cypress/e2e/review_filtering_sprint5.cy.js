describe('Review Filtering & Sorting E2E Tests - Sprint 5', () => {
  const login = (email, password) => {
    cy.visit('/login');
    cy.get('input[name="email"]').type(email);
    cy.get('input[name="password"]').type(password);
    cy.get('button[type="submit"]').click();
  };

  const navigateToReviews = () => {
    // Navigate to active users first to find a provider
    cy.visit('/active-users');
    // Find the first user card and go to their profile, then reviews
    // Alternatively, visit a known profile if data is guaranteed. 
    // Here we will try to find a user with an average rating displayed.
    cy.get('.user-card').first().find('a').contains('View Profile').click();
    
    // Look for the "View Reviews" link or rating summary
    cy.get('body').then(($body) => {
      if ($body.find('a[href*="/ratings/user/"]').length > 0) {
        cy.get('a[href*="/ratings/user/"]').first().click();
      } else {
        // Fallback: Manually visit a likely ID if navigation fails
        cy.visit('/ratings/user/1');
      }
    });
  };

  beforeEach(() => {
    login('arun@gmail.com', '12345678');
    navigateToReviews();
  });

  it('Scenario 1: Default sorting shows most recent reviews', () => {
    // Check if select has 'recent' as default
    cy.get('select[name="sort"]').should('have.value', 'recent');
    
    cy.get('body').then(($body) => {
      if ($body.find('.review-card').length > 1) {
        // Collect dates and verify they are descending
        const dates = [];
        cy.get('.review-card small.fw-bold').each(($el) => {
          dates.push(new Date($el.text()));
        }).then(() => {
          for (let i = 0; i < dates.length - 1; i++) {
            expect(dates[i].getTime()).to.be.at.least(dates[i+1].getTime());
          }
        });
      }
    });
  });

  it('Scenario 2 & 6: Filter reviews by rating', () => {
    // Apply 4 stars and above filter
    cy.get('select[name="minRating"]').select('4');
    cy.get('button[type="submit"]').contains('Apply').click();

    cy.get('body').then(($body) => {
      if ($body.find('.review-card').length > 0) {
        cy.get('.review-card').each(($card) => {
          // Count filled stars
          cy.wrap($card).find('.bi-star-fill').its('length').should('be.at.least', 4);
        });
      } else {
        // Scenario 6: No reviews match
        cy.get('.empty-state').should('be.visible');
        cy.contains('No reviews match your filter').should('be.visible');
      }
    });
  });

  it('Scenario 3: Sort reviews by highest rating', () => {
    cy.get('select[name="sort"]').select('highest');
    cy.get('button[type="submit"]').contains('Apply').click();

    cy.get('body').then(($body) => {
      if ($body.find('.review-card').length > 1) {
        const ratings = [];
        cy.get('.review-card').each(($card) => {
          cy.wrap($card).find('.bi-star-fill').its('length').then((len) => {
            ratings.push(len);
          });
        }).then(() => {
          for (let i = 0; i < ratings.length - 1; i++) {
            expect(ratings[i]).to.be.at.least(ratings[i+1]);
          }
        });
      }
    });
  });

  it('Scenario 4: Sort reviews by lowest rating', () => {
    cy.get('select[name="sort"]').select('lowest');
    cy.get('button[type="submit"]').contains('Apply').click();

    cy.get('body').then(($body) => {
      if ($body.find('.review-card').length > 1) {
        const ratings = [];
        cy.get('.review-card').each(($card) => {
          cy.wrap($card).find('.bi-star-fill').its('length').then((len) => {
            ratings.push(len);
          });
        }).then(() => {
          for (let i = 0; i < ratings.length - 1; i++) {
            expect(ratings[i]).to.be.at.most(ratings[i+1]);
          }
        });
      }
    });
  });

  it('Scenario 5: Apply filtering and sorting together', () => {
    cy.get('select[name="minRating"]').select('3');
    cy.get('select[name="sort"]').select('highest');
    cy.get('button[type="submit"]').contains('Apply').click();

    cy.get('body').then(($body) => {
      if ($body.find('.review-card').length > 0) {
        // Verify filter (min 3)
        cy.get('.review-card').first().find('.bi-star-fill').its('length').should('be.at.least', 3);
        
        // General check that sort param is in URL
        cy.url().should('include', 'minRating=3').and('include', 'sort=highest');
      }
    });
  });

  it('Scenario 7: No reviews available', () => {
    // Visit our own profile to get a valid ID that exists
    cy.visit('/profile');
    
    // Find the "View Reviews" link which contains our ID
    cy.get('a[href*="/ratings/user/"]').first().then(($link) => {
      const url = $link.attr('href');
      cy.visit(url);
      
      // Now verify the empty state on this valid profile
      cy.get('.empty-state').should('be.visible');
      cy.contains('No reviews yet').should('be.visible');
    });
  });
});
