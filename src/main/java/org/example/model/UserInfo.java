package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserInfo {
    Long userId;
    String chatId;
    String username;
    String firstName;
    String lastName;
    int timezone;
    boolean feedbackModeAllowed;
    boolean feedbackModeEnabled;
    Long replyModeFolloweeId;
    Integer replyModeMessageId;


    public String getUserNameWithAt()
    {
        if (username != null)
        {
            return "@" + username;
        }
        if (lastName != null)
        {
            return firstName + " " + lastName;
        }
        return firstName;
    }
}