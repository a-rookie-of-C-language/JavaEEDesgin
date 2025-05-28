package site.arookieofc.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> {
    private List<T> data;     // 当前页数据
    private long total;       // 总记录数
    private int page;         // 当前页码
    private int size;         // 每页大小
    private int totalPages;   // 总页数
    
    public PageResult(List<T> data, long total, int page, int size) {
        this.data = data;
        this.total = total;
        this.page = page;
        this.size = size;
        this.totalPages = (int) Math.ceil((double) total / size);
    }
}