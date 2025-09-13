package com.bozntouran.reviewservice.mapper;


import com.bozntouran.api.core.review.ReviewDto;
import com.bozntouran.reviewservice.persistence.Review;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    ReviewMapper MAPPER = Mappers.getMapper(ReviewMapper.class);

    @Mapping(source = "stars", target = "stars")
    @Mapping(source = "comment", target = "comment")
    @Mapping(source = "updateDate", target = "updateDate", dateFormat = "dd.MM.yyyy")
    @Mapping(target = "userName", ignore = true )
    ReviewDto fromReview(Review review);

    Review toReview(ReviewDto reviewDto);


}
