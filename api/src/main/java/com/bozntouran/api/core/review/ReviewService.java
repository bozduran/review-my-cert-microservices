package com.bozntouran.api.core.review;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@SecurityRequirement(name = "security_auth")
public interface ReviewService {

    //ReviewDto saveNewReviewByCertificateId(Long id, String username, ReviewDto reviewDto);

    Page<ReviewDto> retrieveAllReviews(String certificateId,String reviewId, String username, Integer pageNumber);

    Optional<ReviewDto> retrieveReviewByPublicId(String id);

    Optional<ReviewDto> save(ReviewDto review);

    boolean remove(String id);
}
