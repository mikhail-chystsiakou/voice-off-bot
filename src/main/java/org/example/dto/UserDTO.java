package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

import static org.example.constant.ExceptionMessageConstant.FIELD_CHAT_ID_MUSt_NOT_BE_BLANK;
import static org.example.constant.ExceptionMessageConstant.FIELD_LAST_NAME_MUST_NOT_BE_NULL;
import static org.example.constant.ExceptionMessageConstant.FIELD_USERNAME_MUST_NOT_BE_BLANK;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {

    private Long id;

    @NotBlank(message = FIELD_CHAT_ID_MUSt_NOT_BE_BLANK)
    private String chatId;

    @NotBlank(message = FIELD_USERNAME_MUST_NOT_BE_BLANK)
    private String username;

    private String firstName;

    @NotBlank(message = FIELD_LAST_NAME_MUST_NOT_BE_NULL)
    private String lastName;

}
