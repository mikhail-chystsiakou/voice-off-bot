package org.example.repository

import org.example.model.UserAudio
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserAudioRepository : JpaRepository<UserAudio, Long> {
}