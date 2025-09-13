package com.bozntouran.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class CustomPageRequests {

    private static final Integer DEFAULT_PAGE_SIZE = 10;
    private static final Integer DEFAULT_PAGE = 0;

    public static PageRequest pageRequestBuilder(Integer pageNumber, Integer pageSize, String sortBy){
        int queryPageSize ;
        int queryPageNumber;

        if (pageNumber != null && pageNumber > 0){
            queryPageNumber = pageNumber - 1;
        }else{
            queryPageNumber = DEFAULT_PAGE;
        }

        if (pageSize != null && pageSize > 0){
            if (pageSize > 250){
                queryPageSize = 250;
            }else{
                queryPageSize = pageSize;
            }
        }else{
            queryPageSize = DEFAULT_PAGE_SIZE;
        }

        Sort sort = Sort.by(sortBy);

        return PageRequest.of(queryPageNumber, queryPageSize, sort);
    }
}
