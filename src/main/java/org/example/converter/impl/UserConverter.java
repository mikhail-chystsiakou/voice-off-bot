package org.example.converter.impl;

import org.example.converter.Converter;
import org.example.dto.UserDTO;
import org.example.model.UserInfo;
import org.springframework.stereotype.Component;

@Component
public class UserConverter implements Converter<UserInfo, UserDTO> {
    @Override
    public UserInfo fromDTO(UserDTO userDTO) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userDTO.getId());
        userInfo.setChatId(userDTO.getChatId());
        userInfo.setUsername(userDTO.getUsername());
        userInfo.setFirstName(userInfo.getFirstName());
        userInfo.setLastName(userDTO.getLastName());
        return userInfo;
    }

    @Override
    public UserDTO toDTO(UserInfo userInfo) {
        return UserDTO.builder()
                .id(userInfo.getUserId())
                .chatId(userInfo.getChatId())
                .username(userInfo.getUsername())
                .firstName(userInfo.getFirstName())
                .lastName(userInfo.getLastName())
                .build();
    }
}
