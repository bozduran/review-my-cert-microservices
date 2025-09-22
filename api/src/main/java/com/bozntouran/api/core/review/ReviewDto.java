package com.bozntouran.api.core.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class ReviewDto {

    @Min(0)
    @Max(5)
    private short stars;
    private LocalDateTime updateDate;

    private String comment;
    private String userName;
    private String publicId;

}
