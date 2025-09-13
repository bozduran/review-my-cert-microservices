package com.bozntouran.reviewservice.service;


import com.bozntouran.api.core.review.ReviewDto;
import com.bozntouran.api.core.review.ReviewService;
import com.bozntouran.reviewservice.mapper.ReviewMapper;
import com.bozntouran.reviewservice.persistence.Review;
import com.bozntouran.reviewservice.persistence.ReviewRepository;
import com.bozntouran.utils.CustomPageRequests;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class ReviewServiceImpl implements ReviewService {


    private final ReviewRepository reviewRepository;
    private final String CERTIFICATE_URL;
    private final RestTemplate restTemplate;


    @Autowired
    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             RestTemplate restTemplate,
                             @Value("${certificate.host}") String certificateServiceHost,
                             @Value("${certificate.port}") String certificateServicePort) {

        this.restTemplate = restTemplate;
        this.CERTIFICATE_URL = "http://certificate/internal/certificate/";


        this.reviewRepository = reviewRepository;
    }
/*

    public Review patchReviewByCertificateId(Certificate certificate, UserData userData, ReviewDto reviewDto){

        Review review = reviewRepository.findByUserData(userData);

        review.setComment(reviewDto.getComment());
        review.setStars(reviewDto.getStars());
        review.setUpdateDate(LocalDateTime.now());
        return reviewRepository.save(review);
    }

    @Override
    public Review saveNewReviewByCertificateId(Long id, String username, ReviewDto reviewDto) {
        Certificate certificate = certificateRepository.findById(id).orElse(null);
        UserData userData = userDataRepository.findUserByUsername(username);
        if ( certificate == null ){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");
        }
        if (reviewRepository.existsByCertificateAndUserData(certificate,userData)){
            return patchReviewByCertificateId(certificate, userData, reviewDto);
        }

        Review review = new Review();

        review.setStars(reviewDto.getStars());
        review.setComment(reviewDto.getComment());
        review.setCertificate(certificate);
        review.setUserData(userData);
        review.setUpdateDate(LocalDateTime.now());
        review.setCreatedDate(LocalDateTime.now());

        return reviewRepository.save(review);
    }
*/

    @Override
    public Page<ReviewDto> retrieveAllReviews(String certificateId,
                                              String reviewId,
                                              String username,
                                              Integer pageNumber) {

        PageRequest pageRequest = CustomPageRequests.pageRequestBuilder(pageNumber, 0, "updateDate");
        Page<Review> page;
        if (certificateId != null) {
            page = reviewRepository.getAllByCertificateId(
                    getCertificateID(ReviewDto.builder().publicId(certificateId).build()).orElseThrow(),
                    pageRequest
            );
        } else if (username != null) {
            // TODO : make this work use username
            page = reviewRepository.getAllByUserId(1L, pageRequest);
        } else {
            page = reviewRepository.findAll(pageRequest);
        }

        return page.map(ReviewMapper.MAPPER::fromReview);
    }

    @Override
    public Optional<ReviewDto> retrieveReviewById(String id) {
        return Optional.of(ReviewMapper.MAPPER.fromReview(reviewRepository.getByPublicIdIs(id)));
    }

    @Override
    public Optional<ReviewDto> save(ReviewDto review) {
        Review reviewEntity = ReviewMapper.MAPPER.toReview(review);
        // get the public id of  user and certificate and get the

        reviewEntity.setPublicId(UUID.randomUUID().toString());
        // get the certificate id
        Optional<Long> resultID = getCertificateID(review);
        reviewEntity.setCertificateId(resultID.orElseThrow(
                () -> new RuntimeException("No certificate id returned by th exchange on save")));

        // get info regarding user after auth implementation
        reviewEntity.setUserId(1L);
        return Optional.of(ReviewMapper.MAPPER.fromReview(reviewRepository.save(reviewEntity)));
    }

    @Override
    @Transactional
    public boolean remove(String id) {
        try {
            System.out.println(id);
            reviewRepository.deleteByPublicId(id);
        } catch (Exception e) {
            System.out.println(e.getCause());
            return false;
        }
        return true;
    }

    private Optional<Long> getCertificateID(ReviewDto review) {
        log.info("getCertificateID call to {}",CERTIFICATE_URL+review.getPublicId());
        return restTemplate.exchange(CERTIFICATE_URL + review.getPublicId(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Optional<Long>>() {
                }).getBody();
    }


}
