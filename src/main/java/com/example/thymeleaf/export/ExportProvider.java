package com.example.thymeleaf.export;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface ExportProvider<T> {
    Integer QUERY_LIMIT = 10_000;
    Integer TOTAL_LIMIT = 100_000;
    String name();                                  // 예: "user"
    List<ExportColumn<T>> columns();                // 헤더 정의
    Stream<T> stream(Map<String, String> params);   // 검색 조건 (Map으로 받음)
}