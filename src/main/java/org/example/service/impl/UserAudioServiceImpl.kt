package org.example.service.impl

import lombok.extern.slf4j.Slf4j
import mu.KLogger
import mu.KotlinLogging
import org.example.model.UserAudio
import org.example.repository.UserAudioRepository
import org.example.service.FileUserAudioService
import org.springframework.stereotype.Service

@Service
@Slf4j
class UserAudioServiceImpl(
    private val userAudioRepository: UserAudioRepository,
) : FileUserAudioService {

    private val logger: KLogger = KotlinLogging.logger {}

    override fun createUserAudio(userAudio: UserAudio) : UserAudio {
        val createdUserAudio : UserAudio = userAudioRepository.save(userAudio)
        logger.info { "createUserAudio: created userAudio $createdUserAudio" }
        return createdUserAudio
    }
}