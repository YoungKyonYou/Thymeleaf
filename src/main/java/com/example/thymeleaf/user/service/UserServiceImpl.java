package com.example.thymeleaf.user.service;

import com.example.thymeleaf.common.PageData;
import com.example.thymeleaf.user.dto.UserDto;
import com.example.thymeleaf.user.dto.UserSearchRequest;
import com.example.thymeleaf.user.mapper.UserMapper;
import com.example.thymeleaf.user.service.impl.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper mapper;

    @Transactional(readOnly = true)
    @Override
    public PageData<UserDto> search(UserSearchRequest req, int page, int size, String sort, String dir) {
        final int offset = page * size;

        long total = mapper.count(req);
        List<UserDto> content = (total == 0)
                ? List.of()
                : mapper.search(req, offset, size, sort, dir);

        return new PageData<>(content, page, size, total);
    }

    @Transactional(readOnly = true)
    @Override
    public long count(UserSearchRequest req) {
        return mapper.count(req);
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> selectPage(UserSearchRequest req, int offset, int limit, String sort, String dir) {
        return mapper.search(req, offset, limit, sort, dir);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto selectOne(Long id) {
        return mapper.findById(id);
    }

    @Transactional
    @Override
    public UserDto insertOne(UserDto u) {
        mapper.insert(u); // useGeneratedKeys 로 id 세팅
        return u;
    }

    @Transactional
    @Override
    public UserDto updateOne(UserDto u) {
        mapper.update(u);
        return u;
    }

    @Transactional
    @Override
    public void deleteOne(Long id) {
        mapper.delete(id);
    }
}
