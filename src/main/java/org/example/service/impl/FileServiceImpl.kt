package org.example.service.impl

import mu.KLogger
import mu.KotlinLogging
import org.example.dto.FileDTO
import org.example.model.UserAudio
import org.example.model.UserInfo
import org.example.service.FileService
import org.example.service.FileUserAudioService
import org.example.service.FileUserService
import org.example.storage.FileStorage
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.sql.Timestamp
import java.util.*
import javax.transaction.Transactional

@Service
open class FileServiceImpl(
    private val userAudioService: FileUserAudioService,
    private val fileStorage: FileStorage,
    private val fileUserService: FileUserService
) : FileService {

    private val logger: KLogger = KotlinLogging.logger {}

    @Transactional
    override fun saveNewFile(multipartFile: MultipartFile, fileDTO: FileDTO) : UserAudio {
        val userInfo: UserInfo = fileUserService.getUserById(fileDTO.userId)
        val userAudio = UserAudio(
            fileId = UUID.randomUUID().toString(),
            userInfo = userInfo,
            duration = fileDTO.duration,
            messageId = generateTelegramMessageId(),
            recordingTimestamp = Timestamp(System.currentTimeMillis()),
            fileSize = multipartFile.size,
            replyToMessageId = userInfo.replyModeMessageId,
            fileOrderNumber = null
        )
        fileStorage.storeFile(multipartFile, fileDTO, userAudio)
        val createdUserAudio = userAudioService.createUserAudio(userAudio)
        logger.info { "created userAudio $createdUserAudio" }
        return createdUserAudio
    }

    private fun generateTelegramMessageId(): Int {
        val timestamp = System.currentTimeMillis()
        val random = Random().nextInt(99999) // Adjust the range as needed
        return (timestamp * 100000 + random).toInt()
    }

}