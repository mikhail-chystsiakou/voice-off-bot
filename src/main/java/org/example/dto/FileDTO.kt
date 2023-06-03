package org.example.dto

import org.example.enums.MessageType
import org.jetbrains.annotations.NotNull
import javax.validation.constraints.Min

class FileDTO(
    @NotNull
    @Min(value = 0)
    val userId: Long,

    @NotNull
    @Min(value = 0)
    val duration: Int,

    @NotNull
    val messageType: MessageType
) {
}