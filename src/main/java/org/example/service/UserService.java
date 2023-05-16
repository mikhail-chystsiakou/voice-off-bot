package org.example.service;

import org.example.model.UserInfo;

import java.util.List;

public interface UserService {
    UserInfo getUserById(Long userId);

    List<UserInfo> getFollowers(Long userId);

    List<UserInfo> getUsers();

    List<UserInfo> getSubscriptions(Long userId);
}
