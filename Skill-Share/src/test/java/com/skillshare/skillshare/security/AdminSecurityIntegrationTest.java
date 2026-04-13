package com.skillshare.skillshare.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void unauthenticatedUser_cannotAccessAdminDashboard() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void regularUser_cannotAccessAdminDashboard() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
               .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void adminUser_canAccessAdminDashboard() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
               .andExpect(status().isOk())
               .andExpect(view().name("admin-dashboard"));
    }
    
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void adminUser_canAccessAdminUsersList() throws Exception {
        mockMvc.perform(get("/admin/users"))
               .andExpect(status().isOk())
               .andExpect(view().name("admin-users"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void regularUser_cannotAccessUsersList() throws Exception {
        mockMvc.perform(get("/admin/users"))
               .andExpect(status().isForbidden());
    }
}
