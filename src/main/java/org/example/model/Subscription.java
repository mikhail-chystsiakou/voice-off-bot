package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "user_subscriptions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Subscription {

    @EmbeddedId
    private SubscriptionId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private UserInfo userInfo;

    @ManyToOne
    @MapsId("followeeId")
    @JoinColumn(name = "followee_id")
    private UserInfo followee;

    @Column(name = "last_pull_timestamp")
    private Timestamp lastPull;

    @Column(name = "last_reply_pull_timestamp")
    private Timestamp lastReplyPull;
}
