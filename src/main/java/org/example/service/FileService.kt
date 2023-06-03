package org.example.service

import org.example.dto.FileDTO
import org.example.model.UserAudio
import org.springframework.web.multipart.MultipartFile

interface FileService {

    fun saveNewFile(multipartFile: MultipartFile, fileDTO: FileDTO) : UserAudio
}