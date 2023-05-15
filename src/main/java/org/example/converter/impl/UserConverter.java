package org.example.converter.impl;

import org.example.converter.Converter;
import org.example.dto.UserDTO;
import org.example.model.UserInfo;
import org.springframework.stereotype.Component;

@Component
public class UserConverter implements Converter<UserInfo, UserDTO> {
    @Override
    public UserInfo fromDTO(UserDTO userDTO) {
        return UserInfo.builder()
                .userId(userDTO.getId())
                .chatId(userDTO.getChatId())
                .username(userDTO.getUsername())
                .firstName(userDTO.getFirstName())
                .lastName(userDTO.getLastName())
                .build();
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
