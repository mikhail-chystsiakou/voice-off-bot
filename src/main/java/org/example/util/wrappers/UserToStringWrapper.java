package org.example.util.wrappers;

import org.telegram.telegrambots.meta.api.objects.User;

public class UserToStringWrapper {
    private User user;

    public UserToStringWrapper(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        if (user == null) {
            return "null";
        }
        return "UserToStringWrapper{" +
                "user=" + user +
                '}';
    }
}
