package org.example.controller

import org.example.dto.FileDTO
import org.example.model.UserAudio
import org.example.service.FileService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.net.URI

@RestController
@RequestMapping("api/v1/file")
class FileControllerV1(private val fileService: FileService)  {

    @PostMapping
    fun uploadFile(@RequestParam("file") file : MultipartFile, @ModelAttribute fileDTO: FileDTO) : ResponseEntity<UserAudio> {
        val userAudio = fileService.saveNewFile(file, fileDTO)
        return ResponseEntity.created(URI.create("api/v1/file")).body(userAudio);
    }
}