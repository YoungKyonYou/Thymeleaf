package com.example.thymeleaf.user.mapper;


import com.example.thymeleaf.user.dto.UserDto;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    // 검색 목록
    List<UserDto> search(
            @Param("q") String q,
            @Param("by") String by,
            @Param("offset") int offset,
            @Param("limit") int limit,
            @Param("sort") String sort,
            @Param("dir") String dir
    );

    // 전체 건수
    long count(
            @Param("q") String q,
            @Param("by") String by
    );

    // 단건
    UserDto findById(@Param("id") Long id);

    // 생성 (자동 증가키)
    int insert(UserDto u);

    // 수정
    int update(UserDto u);

    // 삭제
    int delete(@Param("id") Long id);
}