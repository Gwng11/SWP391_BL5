package com.hotel.ultis;

import java.util.List;

/** Small immutable in-memory page used by catalogue-style servlet screens. */
public record PageSlice<T>(List<T> items,int currentPage,int totalPages,int totalItems) {
    public static <T> PageSlice<T> of(List<T> source,int requestedPage,int pageSize){
        if(pageSize<=0)throw new IllegalArgumentException("Page size must be positive");
        List<T> safe=source==null?List.of():source;
        int total=safe.size();
        int pages=Math.max(1,(total+pageSize-1)/pageSize);
        int current=Math.min(Math.max(1,requestedPage),pages);
        int from=Math.min((current-1)*pageSize,total);
        int to=Math.min(from+pageSize,total);
        return new PageSlice<>(List.copyOf(safe.subList(from,to)),current,pages,total);
    }
}
