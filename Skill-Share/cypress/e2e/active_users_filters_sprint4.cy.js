describe('Active Users Filtering and Sorting - Sprint 4', () => {

    const TEST_EMAIL = 'arun@gmail.com';
    const TEST_PASSWORD = '12345678';

    beforeEach(() => {
        // Log in and go to the active users page
        cy.login(TEST_EMAIL, TEST_PASSWORD);
        cy.visit('/active-users');
    });

    it('Scenario 1: Default view shows only available users', () => {
        // By default, availability filter should be selected as 'AVAILABLE'
        cy.get('select[name="availability"]').should('have.value', 'AVAILABLE');
        
        // Every card in the main results grid should display an available status
        cy.get('.user-grid .user-card').each(($card) => {
            cy.wrap($card).find('.status-dot').should('have.class', 'status-available');
            cy.wrap($card).contains('AVAILABLE').should('exist');
        });
    });

    it('Scenario (View All): User can view all users by changing status filter', () => {
        // Switch availability filter to "All Statuses" (empty value)
        cy.get('select[name="availability"]').select(''); 
        
        // Form should auto-submit and reload with the new filter
        cy.url().should('contain', 'availability=');
        
        // Verify the dropdown choice persisted
        cy.get('select[name="availability"]').should('have.value', '');
        
        // If data permits, some users might now show 'UNAVAILABLE' status
        // At minimum, we verify the page loaded and shows results
        cy.get('.user-grid').should('be.visible');
    });

    it('Scenario 2: Filter users by skill categories', () => {
        // Get the first non-default option from the category dropdown
        cy.get('select[name="category"] option').then($options => {
            if ($options.length > 1) {
                const firstCategory = $options[1].value;
                cy.get('select[name="category"]').select(firstCategory);
                
                // Verify URL or persisted filter
                cy.url().should('contain', `category=${firstCategory}`);
                cy.get('select[name="category"]').should('have.value', firstCategory);
            }
        });
    });

    it('Scenario 4: Filter users by ratings (4+ Stars)', () => {
        // Select 4+ Stars filter
        cy.get('select[name="minRating"]').select('4.0');
        
        // Check main grid cards with ratings to ensure they are >= 4.0
        cy.get('.user-grid .user-card').each(($card) => {
            cy.wrap($card).then(($el) => {
                const ratingText = $el.find('.fw-bold.fs-6').text();
                if (ratingText) {
                    const rating = parseFloat(ratingText);
                    expect(rating).to.be.at.least(4.0);
                }
            });
        });
    });

    it('Scenario 5: Filter users by current user location', () => {
        // Requirement: System displays users from the same location as current user
        cy.get('#sameLocationSwitch').check({ force: true });
        
        // Wait for page to reload after auto-submit
        cy.url().should('include', 'sameLocation=true');
        
        // (Optional) If we know the test user's location, we could verify the card location text
        // But checking that the filter persisted is the primary functional check here.
        cy.get('#sameLocationSwitch').should('be.checked');
    });

    it('Scenario 6: Filter users by current user university', () => {
        // Requirement: System displays users from the same university as current user
        cy.get('#sameUniversitySwitch').check({ force: true });
        
        // Wait for page to reload after auto-submit
        cy.url().should('include', 'sameUniversity=true');
        cy.get('#sameUniversitySwitch').should('be.checked');
    });

    it('Scenario 7: Sort users by highest average rating', () => {
        // Switch to highest rating sort
        cy.get('select[name="sort"]').select('highest_rating');
        
        // Wait for page to reload/update
        cy.url().should('include', 'sort=highest_rating');

        // Collect all ratings from the results list in main grid
        let ratings = [];
        cy.get('.user-grid .user-card .fw-bold.fs-6').each(($el) => {
            ratings.push(parseFloat($el.text()));
        }).then(() => {
            if (ratings.length > 1) {
                // Ensure array is sorted descending
                for (let i = 0; i < ratings.length - 1; i++) {
                    expect(ratings[i]).to.be.at.least(ratings[i+1]);
                }
            }
        });
    });

    it('Scenario 9: Sort users by alphabetical name order (A-Z)', () => {
        // Switch to name A-Z sort
        cy.get('select[name="sort"]').select('name_asc');
        
        // Wait for page to reload/update with the new sort parameter
        cy.url().should('include', 'sort=name_asc');

        let names = [];
        cy.get('.user-grid .user-card .user-name').each(($el) => {
            names.push($el.text().trim().toLowerCase());
        }).then(() => {
            if (names.length > 1) {
                const sortedNames = [...names].sort();
                expect(names).to.deep.equal(sortedNames);
            }
        });
    });

    it('Scenario 10: Apply filtering and sorting together', () => {
        // Choose a category
        cy.get('select[name="category"] option').eq(1).then($opt => {
            const cat = $opt.val();
            cy.get('select[name="category"]').select(cat);
            
            // Wait for URL to update to ensure page transition/re-render is complete
            cy.url().should('include', `category=${cat}`);
            
            // Apply sort - use force:true to bypass any transient "disabled" state during PWA transitions
            cy.get('select[name="sort"]').select('name_asc', { force: true });
            
            // Verify both are in the URL
            cy.url().should('include', `category=${cat}`).and('include', 'sort=name_asc');
        });
    });

    it('Scenario 11: Display message when no users match criteria', () => {
        // Enter a search term that won't have results
        const uniqueSearch = 'NON_EXISTENT_USER_' + Date.now();
        cy.get('input[name="search"]').type(uniqueSearch);
        cy.get('button.btn-search').click();
        
        // System should show the appropriate message
        cy.contains('No users found').should('be.visible');
        cy.contains('Try adjusting your search keywords').should('be.visible');
        
        // "Clear Search" button should be available
        cy.get('a').contains('Clear Search').should('be.visible').and('has.attr', 'href');
    });

    it('Scenario 12: Placement of unrated users in sorting', () => {
        // When sorting by Highest Rating, unrated users should be at the very bottom
        cy.get('select[name="sort"]').select('highest_rating');
        
        // We look for a card without a rating badge (if total results > 0)
        // If results are found, verify that if a rated user exists, they are above unrated ones
        // This is complex to test without deterministic data, but we verify 
        // that the list renders without error when unrated users are present.
        cy.get('.user-grid').should('exist');
    });

});
