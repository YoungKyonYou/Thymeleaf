package tmoney.co.kr.hxz.common.page.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PageDataVO<T> {
    private List<T> content;
    private int page;
    private int size;
    private long total;

    public long getTotalPages() { return (total + size - 1) / size; }
    public boolean isHasPrev() { return page > 0; }
    public boolean isHasNext() { return page + 1 < getTotalPages(); }
}
