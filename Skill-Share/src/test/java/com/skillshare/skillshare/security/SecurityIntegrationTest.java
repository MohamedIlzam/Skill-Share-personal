package com.skillshare.skillshare.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unauthenticatedRequestToProfile_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().isFound()) // 302 Redirect
                .andExpect(redirectedUrlPattern("**/login"));
    }
    
    @Test
    void unauthenticatedRequestToLogin_ShouldSucceed() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void authenticatedRequestToProfile_ShouldSucceed() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk());
    }

    @Autowired
    private com.skillshare.skillshare.service.auth.AuthService authService;

    @Test
    void dbBackedAuthentication_ShouldSucceed() throws Exception {
        String testEmail = "testdb@example.com";
        String testPassword = "Password123#";
        
        try {
            authService.registerUser("DB Test User", testEmail, testPassword);
        } catch (com.skillshare.skillshare.exception.ResourceConflictException e) {
            // Ignore if user already exists
        }

        // Perform real DB-backed login via the form
        MvcResult result = mockMvc.perform(formLogin("/login")
                        .user("username", testEmail)
                        .password("password", testPassword))
                .andExpect(authenticated())
                .andExpect(status().isFound()) // 302 redirect after successful login
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession();

        // Verify protected route is now accessible with the real session
        mockMvc.perform(get("/profile").session(session))
                .andExpect(status().isOk());
    }
}
