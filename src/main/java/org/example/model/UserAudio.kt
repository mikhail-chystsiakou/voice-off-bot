package org.example.model

import java.sql.Timestamp
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Index
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.persistence.UniqueConstraint

@Entity
@Table(name = "user_audios",
    uniqueConstraints = [UniqueConstraint(name = "audios_pk", columnNames = ["file_order_number", "user_id"])],
    indexes = [Index(name = "user_audios_timestamp_index", columnList = "user_id, recording_timestamp")]
)
class UserAudio(

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "file_order_number", columnDefinition = "serial")
    val fileOrderNumber: Long?,

    val fileId: String,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val userInfo: UserInfo,

    val duration: Int,

    val messageId: Int?= null,

    val description: String? = null,

    val fileSize: Long,

    @Column(name = "recording_timestamp")
    val recordingTimestamp: Timestamp,

    val pullCount: Long? = 0,

    val okMessageId: Long? = null,

    val replyToMessageId: Int?= null
)