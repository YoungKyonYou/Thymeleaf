package com.example.thymeleaf.user.repository;


import com.example.thymeleaf.common.PageData;
import com.example.thymeleaf.user.dto.UserDto;

public interface UserRepository {
    PageData<UserDto> search(String q, String by, int page, int size, String sort, String dir);

    UserDto findById(Long id);
    UserDto insert(UserDto u);
    UserDto update(Long id, UserDto u);
    void delete(Long id);
}
