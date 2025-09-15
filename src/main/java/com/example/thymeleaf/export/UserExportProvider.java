package com.example.thymeleaf.export;

import com.example.thymeleaf.user.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Component
public class UserExportProvider implements ExportProvider<UserDto> {

    private final UserQueryService service;

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
        return service.stream(params);
    }
}
