package eu.essi_lab.accessor.dataloggers;

import java.util.List;

public class VariablesResponse {
    private List<Variable> content;
    private Pageable pageable;
    private Integer totalPages;
    private Long totalElements;
    private Boolean last;
    private Boolean first;

    public List<Variable> getContent() {
        return content;
    }

    public void setContent(List<Variable> content) {
        this.content = content;
    }

    public Pageable getPageable() {
        return pageable;
    }

    public void setPageable(Pageable pageable) {
        this.pageable = pageable;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }

    public Boolean getLast() {
        return last;
    }

    public void setLast(Boolean last) {
        this.last = last;
    }

    public Boolean getFirst() {
        return first;
    }

    public void setFirst(Boolean first) {
        this.first = first;
    }
}

