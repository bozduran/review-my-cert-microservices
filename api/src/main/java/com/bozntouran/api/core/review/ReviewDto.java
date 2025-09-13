package com.bozntouran.api.core.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class ReviewDto {

    private short stars;
    private LocalDateTime updateDate;
    private String comment;
    private String userName;
    private String publicId;

}
