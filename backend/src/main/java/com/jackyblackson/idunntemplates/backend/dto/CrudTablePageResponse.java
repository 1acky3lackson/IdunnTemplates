package com.jackyblackson.idunntemplates.backend.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public class CrudTablePageResponse<T> {

    private List<T> content;
    private boolean last;
    private long totalElements;
    private int number;

    public CrudTablePageResponse() {
    }

    public CrudTablePageResponse(List<T> content, boolean last, long totalElements, int number) {
        this.content = content;
        this.last = last;
        this.totalElements = totalElements;
        this.number = number;
    }

    public static <T> CrudTablePageResponse<T> from(Page<T> page) {
        return new CrudTablePageResponse<>(
                page.getContent(),
                page.isLast(),
                page.getTotalElements(),
                page.getNumber()
        );
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
