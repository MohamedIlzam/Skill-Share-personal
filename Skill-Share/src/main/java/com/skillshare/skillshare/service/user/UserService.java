package com.skillshare.skillshare.service.user;

import com.skillshare.skillshare.model.user.User;

public interface UserService {
    User createForSmokeTest(String fullName, String email, String rawPassword);
    User getByEmail(String email);
}
