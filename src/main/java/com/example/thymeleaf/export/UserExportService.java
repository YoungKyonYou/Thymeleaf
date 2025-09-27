package com.example.thymeleaf.export;

import com.example.thymeleaf.user.dto.UserDto;
import com.example.thymeleaf.user.dto.UserSearchRequest;
import com.example.thymeleaf.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class UserExportService implements ExportProvider<UserDto> {

    private final UserMapper userMapper;

    @Override public String name() { return "user"; }

    @Override
    public List<ExportColumn<UserDto>> columns() {
        return List.of(
                new ExportColumn<>("ID",        r -> String.valueOf(r.getId())),
                new ExportColumn<>("Username",  UserDto::getUsername),
                new ExportColumn<>("Email",     UserDto::getEmail),
                new ExportColumn<>("First",     UserDto::getFirstName),
                new ExportColumn<>("Last",      UserDto::getLastName),
                new ExportColumn<>("Phone",     UserDto::getPhone)
        );
    }


    @Override
    public Stream<UserDto> stream(Map<String, String> params) {
        // 검색 조건 공통 부분 세팅 (페이지/오프셋만 매 페이지마다 바꿔 끼웁니다)
        final String sort = params.getOrDefault("sort", "id");
        final String dir  = params.getOrDefault("dir",  "asc");

        // 페이지 크기: 너무 크면 메모리 압박, 너무 작으면 DB 왕복 많음
        final int pageSize = parseIntOrDefault(params.get("size"), QUERY_LIMIT);

        final String email      = params.get("email");
        final String firstName  = params.get("firstName");
        final String lastName   = params.get("lastName");
        final String username   = params.get("username");
        final String phone      = params.get("phone");

        return PagingStreams.paging(pageSize, page -> {
            int offset = page * pageSize;

            UserSearchRequest r = new UserSearchRequest();
            r.setEmail(email);
            r.setFirstName(firstName);
            r.setLastName(lastName);
            r.setUsername(username);
            r.setPhone(phone);
            r.setSize(pageSize);
            r.setSort(sort);
            r.setDir(dir);

            // Mapper는 원래 쓰던 페이지 쿼리 그대로 재사용
            return userMapper.search(r, offset, pageSize, sort, dir);
        });
    }

    private static int parseIntOrDefault(String s, int def) {
        try { return (s == null || s.isBlank()) ? def : Integer.parseInt(s); }
        catch (NumberFormatException e) { return def; }
    }

}