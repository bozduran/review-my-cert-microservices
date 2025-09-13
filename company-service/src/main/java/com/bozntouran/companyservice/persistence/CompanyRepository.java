package com.bozntouran.companyservice.persistence;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Page<Company> getAllByNameContainingIgnoreCase(String name, Pageable pageable);
    Optional<Company> findByPublicId(String publicId);

    boolean existsByPublicId(String id);

    boolean deleteByPublicId(String publicId);
}
