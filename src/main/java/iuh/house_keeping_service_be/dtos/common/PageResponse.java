package iuh.house_keeping_service_be.dtos.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private int currentPage;
    private int pageSize;
    private long totalItems;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
    
    public PageResponse(List<T> content, int page, int size, long totalItems) {
        this.content = content;
        this.currentPage = page;
        this.pageSize = size;
        this.totalItems = totalItems;
        this.totalPages = (int) Math.ceil((double) totalItems / size);
        this.hasNext = page < totalPages - 1;
        this.hasPrevious = page > 0;
    }
}
