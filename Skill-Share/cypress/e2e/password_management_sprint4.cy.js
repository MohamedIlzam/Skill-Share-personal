describe('Sprint 4 - User Security & Password Management', () => {

    const TEST_EMAIL = 'arun@gmail.com';
    const TEST_PASS = '12345678';
    const NEW_PASS = 'NewPass123';
    const WEAK_PASS = '123';

    context('LIFECYCLE: Change Password (Profile Security)', () => {
        beforeEach(() => {
            cy.login(TEST_EMAIL, TEST_PASS);
            cy.visit('/profile/edit');
        });

        it('Should reject empty input fields (TC-CP05)', () => {
            // Remove 'required' attribute to bypass HTML5 validation and test backend/UI errors
            cy.get('input#currentPassword').invoke('removeAttr', 'required');
            cy.get('input#newPassword').invoke('removeAttr', 'required');
            cy.get('input#confirmPassword').invoke('removeAttr', 'required');
            
            cy.get('button').contains('Update Password').click();
            cy.contains('Current password is required').should('be.visible');
            cy.contains('New password is required').should('be.visible');
        });

        it('Should reject weak passwords (TC-CP03)', () => {
            cy.get('input#currentPassword').type(TEST_PASS);
            cy.get('input#newPassword').type(WEAK_PASS);
            cy.get('input#confirmPassword').type(WEAK_PASS);
            cy.get('button').contains('Update Password').click();
            
            cy.contains('Password must be at least 8 characters').should('be.visible');
        });

        it('Should reject unmatching passwords (TC-CP04)', () => {
            cy.get('input#currentPassword').type(TEST_PASS);
            cy.get('input#newPassword').type('NewPass123');
            cy.get('input#confirmPassword').type('DifferentPass123');
            cy.get('button').contains('Update Password').click();
            
            cy.contains('New password and confirm password do not match').should('be.visible');
        });

        it('Should reject incorrect current password (TC-CP02)', () => {
            cy.get('input#currentPassword').type('wrongpassword');
            cy.get('input#newPassword').type(NEW_PASS);
            cy.get('input#confirmPassword').type(NEW_PASS);
            cy.get('button').contains('Update Password').click();
            
            cy.contains('Incorrect current password').should('be.visible');
        });

        it('Should reject new password if it is the same as current password (TC-CP06)', () => {
            cy.get('input#currentPassword').type(TEST_PASS);
            cy.get('input#newPassword').type(TEST_PASS);
            cy.get('input#confirmPassword').type(TEST_PASS);
            cy.get('button').contains('Update Password').click();
            
            cy.contains('New password cannot be the same as the current password').should('be.visible');
        });

        it('Should successfully change the password and revert it (TC-CP01)', () => {
            // Step 1: Change to new password
            cy.get('input#currentPassword').type(TEST_PASS);
            cy.get('input#newPassword').type(NEW_PASS);
            cy.get('input#confirmPassword').type(NEW_PASS);
            cy.get('button').contains('Update Password').click();
            
            cy.contains('Password changed successfully').should('be.visible');
            
            // Step 2: Revert back to original password to maintain test state
            cy.visit('/profile/edit');
            cy.get('input#currentPassword').type(NEW_PASS);
            cy.get('input#newPassword').type(TEST_PASS);
            cy.get('input#confirmPassword').type(TEST_PASS);
            cy.get('button').contains('Update Password').click();
            
            cy.contains('Password changed successfully').should('be.visible');
        });
    });

    context('LIFECYCLE: Forgot Password (Account Recovery)', () => {
        beforeEach(() => {
            cy.visit('/login');
        });

        it('Should provide a workflow for unregistered emails without leaking data (TC-FP09)', () => {
            cy.contains('Forgot your password?').click();
            cy.url().should('include', '/forgot-password');

            // Ghost email
            cy.get('input#email').type('ghost@nonexistent.com');
            cy.get('button').contains('Send Reset Link').click();
            
            cy.contains('If an account with that email exists, a password reset link has been sent').should('be.visible');
        });

        it('Should provide a workflow for registered emails (TC-FP01)', () => {
            cy.contains('Forgot your password?').click();
            cy.get('input#email').type(TEST_EMAIL);
            cy.get('button').contains('Send Reset Link').click();
            
            cy.contains('If an account with that email exists, a password reset link has been sent').should('be.visible');
        });

        it('Should block access if the reset token is invalid or expired (TC-FP05)', () => {
            // Attempt to visit the reset password page with a fake token
            cy.visit('/reset-password?token=invalid_or_fake_token_123', { failOnStatusCode: false });
            
            // Should redirect back to forgot-password and show an error
            cy.url().should('include', '/forgot-password');
            cy.contains('Invalid or expired password reset token').should('be.visible');
        });
    });
});
