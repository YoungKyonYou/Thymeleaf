package com.example.thymeleaf.user.service;

import com.example.thymeleaf.common.PageData;
import com.example.thymeleaf.user.dto.UserDto;
import com.example.thymeleaf.user.dto.UserSearchRequest;
import com.example.thymeleaf.user.mapper.UserMapper;
import com.example.thymeleaf.user.service.impl.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper mapper;

    @Override
    public PageData<UserDto> search(UserSearchRequest req) {
        final int offset = req.getPage() * req.getSize();

        long total = mapper.count(req);
        List<UserDto> content = (total == 0)
                ? List.of()
                : mapper.search(req, offset, req.getSize(), req.getSort(), req.getDir());

        return new PageData<>(content, req.getSize(), req.getSize(), total);
    }

    @Override
    public long count(UserSearchRequest req) {
        return mapper.count(req);
    }

    @Override
    public List<UserDto> selectPage(UserSearchRequest req, int offset, int limit, String sort, String dir) {
        return mapper.search(req, offset, limit, sort, dir);
    }

    @Override
    public UserDto selectOne(Long id) {
        return mapper.findById(id);
    }

    @Override
    public UserDto insertOne(UserDto u) {
        mapper.insert(u); // useGeneratedKeys 로 id 세팅
        return u;
    }

    @Override
    public UserDto updateOne(UserDto u) {
        mapper.update(u);
        return u;
    }

    @Override
    public void deleteOne(Long id) {
        mapper.delete(id);
    }
}
