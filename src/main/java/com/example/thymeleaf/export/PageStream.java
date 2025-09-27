package com.example.thymeleaf.export;


import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.IntFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

final class PagingStreams {

    private PagingStreams() {}

    /**
     * pageSupplier.apply(pageIndex) 가 다음 페이지(List<T>)를 돌려줌
     * 반환 리스트의 크기가 pageSize보다 작아지는 순간 마지막 페이지로 간주하고 종료
     * 메모리 고정, 지연평가 스트림을 제공
     */
    public static <T> Stream<T> paging(int pageSize, IntFunction<List<T>> pageSupplier) {
        Objects.requireNonNull(pageSupplier, "pageSupplier");
        if (pageSize <= 0) throw new IllegalArgumentException("pageSize must be > 0");

        Iterator<T> it = new Iterator<>() {
            int page = 0;
            List<T> buf = Collections.emptyList();
            int idx = 0;
            boolean done = false;

            @Override public boolean hasNext() {
                if (done) return false;
                if (idx < buf.size()) return true;

                // 다음 페이지 로드
                buf = pageSupplier.apply(page++);
                if (buf == null || buf.isEmpty()) {
                    done = true;
                    return false;
                }
                idx = 0;
                // 마지막 페이지 판단: 이번에 받은게 pageSize보다 작으면 다음은 없음
                if (buf.size() < pageSize) {
                    // 다음 hasNext()에서 소진되면 done=true 될 것
                }
                return true;
            }

            @Override public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                return buf.get(idx++);
            }
        };

        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED | Spliterator.NONNULL),
                false
        );
    }
}
