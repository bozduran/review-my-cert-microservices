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
    public String CERTIFICATE_URL;
    private final RestTemplate restTemplate;


    @Autowired
    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             RestTemplate restTemplate
) {

        this.restTemplate = restTemplate;
        //this.CERTIFICATE_URL = "http://certificate/internal/certificate/";
        this.CERTIFICATE_URL = "http://localhost:8080/";

        this.reviewRepository = reviewRepository;
    }

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
    public Optional<ReviewDto> retrieveReviewByPublicId(String publicIdd) {
        return Optional.of(ReviewMapper.MAPPER.fromReview(reviewRepository.getByPublicIdIs(publicIdd)));
    }

    @Override
    public Optional<ReviewDto> save(ReviewDto review) {
        Review reviewEntity = ReviewMapper.MAPPER.toReview(review);
        // get the public id of  user and certificate and get the

        reviewEntity.setPublicId(UUID.randomUUID().toString());
        // get the certificate id
        Optional<Long> resultID = getCertificateID(review);
        if (resultID.isPresent()) {
            System.out.println(resultID.get());
        }else {
            System.out.println("fails");
        }
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

        log.info("getCertificateID call to {}",CERTIFICATE_URL + review.getPublicId());

        return restTemplate.exchange(CERTIFICATE_URL + review.getPublicId(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Optional<Long>>() {
                }).getBody();


    }

    public void setCERTIFICATE_URL(String certificateUrl){
        // TODO TESTING PURPOSE
        this.CERTIFICATE_URL = certificateUrl;
    }


}
