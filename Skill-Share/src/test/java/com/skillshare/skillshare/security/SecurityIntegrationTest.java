package com.skillshare.skillshare.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
}
