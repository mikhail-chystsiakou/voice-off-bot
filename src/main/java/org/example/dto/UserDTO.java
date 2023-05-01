package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {

    private Long id;

    private String chatId;

    private String username;

    private String firstName;

    private String lastName;

    private int timezone;

    private boolean feedbackModeAllowed;

    private boolean feedbackModeEnabled;

    private Long replyModeFolloweeId;

    private Integer replyModeMessageId;

}
