package com.bozntouran.reviewservice.controller;


import com.bozntouran.api.core.review.ReviewDto;
import com.bozntouran.api.core.review.ReviewService;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.security.Principal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.bozntouran.utils.Res4jHelperMethod.someThingBadHappened;
/**
 * Controller responsible for managing review-related operations.
 * <p>
 * Exposes REST endpoints to:
 * <ul>
 *   <li>Retrieve existing reviews</li>
 *   <li>Create new reviews</li>
 *   <li>Delete reviews</li>
 * </ul>
 * <p>
 * Delegates the business logic to {@link ReviewService}.
 *
 * Typical usage:
 * <pre>
 *   GET    /reviews/{id}      // Retrieve a review by ID
 *   POST   /reviews           // Create a new review
 *   DELETE /reviews/{id}      // Delete a review
 * </pre>
 *
 * @author YourName
 * @since 1.0
 */
@RestController
@Log4j2
public class ReviewController {


    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    private static final String REVIEW_URL = "/review";
    private static final String REVIEW_URL_ID = "/review/{publicId}";


    @Operation(
            summary = "${api.review.get-review-byId.description}",
            description = "${api.review.get-review-byId.notes}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}"),
            @ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}")
    })
    @GetMapping(REVIEW_URL_ID)
    public ReviewDto getReviewById(@PathVariable String publicId) {
        log.info("getReviewById publicId -> :{}", publicId);
        return reviewService.retrieveReviewByPublicId(publicId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));

    }

    @Operation(
            summary = "${api.review.get-reviews.description}",
            description = "${api.review.get-reviews.notes}")
    @GetMapping(REVIEW_URL)
    public ResponseEntity<Page<ReviewDto>> getAllReviews(
            @RequestParam(required = false) String certificateId,
            @RequestParam(required = false) String reviewId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer pageNumber) {

        Page<ReviewDto> page = reviewService.retrieveAllReviews(certificateId, reviewId, username, pageNumber);

        return new ResponseEntity<>(page, HttpStatus.OK);

    }

    @Operation(
            summary = "${api.review.create-review.description}",
            description = "${api.review.create-review.notes}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "${api.responseCodes.created.description}"),
            @ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}")
    })
    @Retry(name = "getRealCertificateID")
    @TimeLimiter(name = "getRealCertificateID")
    @CircuitBreaker(name = "getRealCertificateID",
    fallbackMethod = "postReviewFallBack")
    @PostMapping(REVIEW_URL)
    public CompletableFuture<ResponseEntity<ReviewDto>> postReview(@RequestBody @Valid ReviewDto review,
                                                                   @RequestParam(value = "delay", required = false,
                                                                           defaultValue ="0") int delay,
                                                                   @RequestParam(value = "faultPercent", required = false,
                                                                           defaultValue = "0") int faultPercent,
                                                                   Principal principal
    ) {
        // TODO REMOVE ONLY USED FOR DEV Resilience4j
        return CompletableFuture.supplyAsync(() -> {
            if (delay > 0 || faultPercent > 0){
                someThingBadHappened(delay,faultPercent);
            }

            Optional<ReviewDto> newReviewDto = reviewService.save(review);
            newReviewDto.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cant create new review"));
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setLocation(URI.create("/review/"+newReviewDto.get().getPublicId()));
            return new ResponseEntity<>(httpHeaders,HttpStatus.CREATED);
        });
    }

    public CompletableFuture<ResponseEntity<ReviewDto>>  postReviewFallBack(@RequestBody @Valid ReviewDto review,
                                                                            @RequestParam(value = "delay", required = false,
                                                                                    defaultValue ="0") int delay,
                                                                            @RequestParam(value = "faultPercent", required = false,
                                                                                    defaultValue = "0") int faultPercent,
                                                                            Principal principal,
                                                                            CallNotPermittedException exception
    ){
        return CompletableFuture.supplyAsync(() -> {
            log.info("postReviewFallBack ===> {}", exception.getMessage());
            return new ResponseEntity<>(HttpStatus.CREATED);
        });
    }

    @Operation(
            summary = "${api.review.create-review.description}",
            description = "${api.review.create-review.notes}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "${api.responseCodes.noContent.description}"),
            @ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}")
    })
    @DeleteMapping(REVIEW_URL_ID)
    public ResponseEntity<ReviewDto> deleteReview(@PathVariable String publicId) {
        boolean complete = reviewService.remove(publicId);
        if (complete) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

    }
}
