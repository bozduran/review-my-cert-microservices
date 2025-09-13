package com.bozntouran.reviewservice.persistence;


import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {


    Review getByPublicIdIs(String publicId);
    Page<Review> getAllByCertificateId(Long certificateId, Pageable pageable);

    Page<Review> getAllByUserId(Long userId, Pageable pageable);

    void deleteByPublicId(String publicId);
}
