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
                .timezone(userDTO.getTimezone())
                .feedbackModeAllowed(userDTO.isFeedbackModeAllowed())
                .feedbackModeEnabled(userDTO.isFeedbackModeEnabled())
                .replyModeFolloweeId(userDTO.getReplyModeFolloweeId())
                .replyModeMessageId(userDTO.getReplyModeMessageId())
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
                .timezone(userInfo.getTimezone())
                .feedbackModeAllowed(userInfo.isFeedbackModeAllowed())
                .feedbackModeEnabled(userInfo.isFeedbackModeEnabled())
                .replyModeFolloweeId(userInfo.getReplyModeFolloweeId())
                .replyModeMessageId(userInfo.getReplyModeMessageId())
                .build();
    }
}
