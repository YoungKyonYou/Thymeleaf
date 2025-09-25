package com.example.thymeleaf.user.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class UserSearchRequest {
    private String q;          // 전체 검색 (optional)
    private String email;
    private String firstName;
    private String lastName;
    private String username;
    private String phone;
    private int page  = 0;     // 기본값 0
    private int size  = 10;    // 기본값 10
    private String sort = "id";   // 기본 정렬 기준
    private String dir  = "asc";  // 기본 정렬 방향
}
