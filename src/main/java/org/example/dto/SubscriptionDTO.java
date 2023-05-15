package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import static org.example.constant.ExceptionMessageConstant.FIELD_FOLLOWEE_ID_MUST_NOT_BE_NEGATIVE;
import static org.example.constant.ExceptionMessageConstant.FIELD_FOLLOWEE_ID_MUST_NOT_BE_NULL;
import static org.example.constant.ExceptionMessageConstant.FIELD_USER_ID_MUST_NOT_BE_NEGATIVE;
import static org.example.constant.ExceptionMessageConstant.FIELD_USER_ID_MUST_NOT_BE_NULL;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubscriptionDTO {

    @NotNull(message = FIELD_USER_ID_MUST_NOT_BE_NULL)
    @Min(value = 0, message = FIELD_USER_ID_MUST_NOT_BE_NEGATIVE)
    private Long userId;

    @NotNull(message = FIELD_FOLLOWEE_ID_MUST_NOT_BE_NULL)
    @Min(value = 0, message = FIELD_FOLLOWEE_ID_MUST_NOT_BE_NEGATIVE)
    private Long followeeId;
}
