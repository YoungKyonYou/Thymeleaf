package com.example.thymeleaf.user.repository.impl;

import com.example.thymeleaf.common.PageData;
import com.example.thymeleaf.user.dto.UserDto;
import com.example.thymeleaf.user.mapper.UserMapper;
import com.example.thymeleaf.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
@Repository
public class UserRepositoryImpl implements UserRepository {
    private final UserMapper mapper;


    @Override
    public PageData<UserDto> search(String q, String by, int page, int size, String sort, String dir) {
        // 방어코드 & 오프셋 계산
        final int safeSize = size <= 0 ? 20 : size;
        final int safePage = Math.max(page, 0);
        final int offset = safePage * safeSize;

        final String safeQ   = (q == null) ? null : q.trim();
        final String safeBy  = StringUtils.hasText(by) ? by : "all";
        final String safeSort= StringUtils.hasText(sort) ? sort : "id";
        final String safeDir = StringUtils.hasText(dir) ? dir : "desc";

        long total = mapper.count(safeQ, safeBy);
        List<UserDto> content = total == 0
                ? List.of()
                : mapper.search(safeQ, safeBy, offset, safeSize, safeSort, safeDir);

        return new PageData<>(content, safePage, safeSize, total);
    }

    @Override
    public UserDto findById(Long id) {
        return mapper.findById(id);
    }

    @Override
    public UserDto insert(UserDto u) {
        mapper.insert(u); // useGeneratedKeys 로 u.id 세팅
        return u;
    }

    @Override
    public UserDto update(Long id, UserDto u) {
        u.setId(id);
        mapper.update(u);
        return u;
    }

    @Override
    public void delete(Long id) {
        mapper.delete(id);
    }
}
