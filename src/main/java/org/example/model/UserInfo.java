package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfo {
    @Id
    private Long userId;
    private String chatId;
    private String username;
    private String firstName;
    private String lastName;

    @Column(name = "time_zone")
    private int timezone;
    private boolean feedbackModeAllowed;
    private boolean feedbackModeEnabled;
    private Long replyModeFolloweeId;
    private Integer replyModeMessageId;

    @OneToMany(mappedBy = "userInfo")
    private List<Subscription> subscriptions;

    @OneToMany(mappedBy = "followee")
    private List<Subscription> followers;


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