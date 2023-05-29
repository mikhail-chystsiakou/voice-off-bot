package org.example.service;

import org.example.exception.EntityNotFoundException;
import org.example.model.UserInfo;

import java.util.List;

public interface UserService {
    UserInfo getUserById(Long userId) throws EntityNotFoundException;

    List<UserInfo> getFollowers(Long userId) throws EntityNotFoundException;

    List<UserInfo> getUsers();

    List<UserInfo> getSubscriptions(Long userId) throws EntityNotFoundException;
}
