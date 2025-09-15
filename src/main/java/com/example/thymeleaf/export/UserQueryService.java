package com.example.thymeleaf.export;

import com.example.thymeleaf.common.PageData;
import com.example.thymeleaf.user.dto.UserDto;
import com.example.thymeleaf.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class UserQueryService {
    private final UserRepository repository;

    public Stream<UserDto> stream(Map<String, String> params) {
        String by   = params.getOrDefault("by", "all");
        String q    = params.getOrDefault("q", "");
        String sort = params.getOrDefault("sort", "id");
        String dir  = params.getOrDefault("dir", "asc");
        int size    = Integer.parseInt(params.getOrDefault("size", String.valueOf(Integer.MAX_VALUE)));

        PageData<UserDto> search = repository.search(q, by, 0, size, sort, dir);

        List<UserDto> content = search.getContent();


        return content.stream();
    }


}
