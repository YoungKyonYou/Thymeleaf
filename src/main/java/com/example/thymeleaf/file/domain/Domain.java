package com.example.thymeleaf.file.domain;

public enum Domain {
    BANNER("banner"),
    // 필요시 추가: DOCUMENT("document"), IMAGE("image"), ...
    ;

    private final String pathSegment;
    Domain(String pathSegment) { this.pathSegment = pathSegment; }
    public String pathSegment() { return pathSegment; }
}
