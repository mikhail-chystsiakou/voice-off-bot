package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.converter.Converter;
import org.example.dto.UserDTO;
import org.example.model.UserInfo;
import org.example.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final Converter<UserInfo, UserDTO> userConverter;

    @GetMapping("{userId}/followers")
    public List<UserDTO> getFollowers(@PathVariable Long userId) {
        return userService.getFollowers(userId).stream().map(userConverter::toDTO).collect(Collectors.toList());
    }

    @GetMapping("{userId}/subscriptions")
    public List<UserDTO> getSubscriptions(@PathVariable Long userId) {
        return userService.getSubscriptions(userId).stream().map(userConverter::toDTO).collect(Collectors.toList());
    }
}
