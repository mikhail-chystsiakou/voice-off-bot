package org.example.service

import org.example.exception.EntityNotFoundException
import org.example.model.UserInfo

interface FileUserService {

    @Throws(EntityNotFoundException::class)
    fun getUserById(userId : Long) : UserInfo
}