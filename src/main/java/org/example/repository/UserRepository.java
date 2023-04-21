package org.example.repository;

import org.example.enums.Queries;
import org.example.model.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserRepository {
    @Autowired
    JdbcTemplate jdbcTemplate;

    public UserInfo loadUserInfoById(Long userId) {
        try
        {
            return jdbcTemplate.queryForObject(
                    Queries.GET_USER_ID_BY_ID.getValue(),
                    (rs, n) -> {
                        String username = rs.getString("username");
                        String firstName = rs.getString("first_name");
                        String lastName = rs.getString("last_name");
                        String followeeChatId = rs.getString("chat_id");
                        int timezone = rs.getInt("time_zone");
                        boolean feedbackModeAllowed = rs.getBoolean("feedback_mode_allowed");
                        boolean feedbackModeEnabled = rs.getBoolean("feedback_mode_enabled");
                        Long replyModeFolloweeId = rs.getLong("reply_mode_followee_id");
                        if (replyModeFolloweeId == 0) {
                            replyModeFolloweeId = null;
                        }
                        Integer replyModeMessageId = rs.getInt("reply_mode_message_id");
                        if (replyModeMessageId == 0) {
                            replyModeMessageId = null;
                        }
                        System.out.println("replyModeFolloweeId: " + replyModeFolloweeId);
                        System.out.println("replyModeMessageId: " + replyModeMessageId);
                        return new UserInfo(userId, followeeChatId,
                                username, firstName, lastName,
                                timezone,
                                feedbackModeAllowed, feedbackModeEnabled,
                                replyModeFolloweeId, replyModeMessageId);
                    },
                    userId
            );
        }
        catch (EmptyResultDataAccessException e)
        {
            return null;
        }
    }
}
