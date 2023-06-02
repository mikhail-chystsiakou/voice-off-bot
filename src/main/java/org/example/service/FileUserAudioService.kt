package org.example.service

import org.example.model.UserAudio

interface FileUserAudioService {
    fun createUserAudio(userAudio: UserAudio) : UserAudio
}