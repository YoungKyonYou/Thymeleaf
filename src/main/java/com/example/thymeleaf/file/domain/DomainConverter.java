package com.example.thymeleaf.file.domain;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class DomainConverter implements Converter<String, Domain> {
    @Override
    public Domain convert(String source) {
        if (source == null) return null;
        String s = source.trim();
        // pathSegment나 enum name 둘 다 허용
        for (Domain d : Domain.values()) {
            if (d.name().equalsIgnoreCase(s) || d.pathSegment().equalsIgnoreCase(s)) {
                return d;
            }
        }
        // 스프링 표준 예외로 흘려보내기
        throw new IllegalArgumentException("지원하지 않는 domain: " + source);
    }
}
