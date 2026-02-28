package com.skillshare.skillshare.service.auth;

import com.skillshare.skillshare.model.user.User;

public interface AuthService {
    User registerUser(String fullName, String email, String rawPassword);
}
