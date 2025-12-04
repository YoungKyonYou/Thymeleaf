package tmoney.co.kr.imports;

import lombok.Getter;

import java.util.function.BiConsumer;

@Getter
public class ImportColumn<T> {

    private final String header;          // 엑셀 헤더명 (검증/에러 메시지용)
    private final int index;              // 0-based column index
    private final BiConsumer<T, String> binder; // (DTO, cellString) -> 필드 세팅

    public ImportColumn(String header, int index, BiConsumer<T, String> binder) {
        this.header = header;
        this.index = index;
        this.binder = binder;
    }

    public void bind(T target, String value) {
        binder.accept(target, value != null ? value.trim() : null);
    }
}
