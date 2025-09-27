package com.example.thymeleaf.export;

import com.example.thymeleaf.user.dto.UserDto;
import com.example.thymeleaf.user.dto.UserSearchRequest;
import com.example.thymeleaf.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
        UserSearchRequest req = new UserSearchRequest();

        req.setEmail(params.get("email"));
        req.setFirstName(params.get("firstName"));
        req.setLastName(params.get("lastName"));
        req.setUsername(params.get("username"));
        req.setPhone(params.get("phone"));
        req.setPage(Integer.parseInt(params.get("page")));
        req.setSize(LIMIT);
        req.setSort(params.get("sort"));
        req.setDir(params.get("dir"));

        final int offset = req.getPage() * req.getSize();

        return userMapper.search(req, offset, req.getSize(), req.getSort(), req.getDir()).stream();
    }

}