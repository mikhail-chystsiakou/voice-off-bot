package org.example.model

import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "users")
class UserInfo {

    @Id
    var userId: Long? = null

    @Column(unique = true, nullable = false)
    var chatId: String? = null

    @Column(unique = true)
    var username: String? = null

    var firstName: String?= null

    var lastName: String?= null

    @Column(name = "time_zone")
    var timezone: Int?= null

    var feedbackModeAllowed: Boolean?= null

    var feedbackModeEnabled: Boolean?= null

    var replyModeFolloweeId: Long?= null

    var replyModeMessageId: Int?= null

    @OneToMany(targetEntity = UserInfo::class)
    var subscriptions: List<Subscription> = ArrayList()

    @OneToMany(targetEntity = Subscription::class)
    var followers: List<Subscription>?= ArrayList()

    fun getUserNameWithAt() : String? {
        if (username != null)
        {
            return "@$username"
        }
        if (lastName != null)
        {
            return "$firstName $lastName"
        }
        return firstName
    }
}