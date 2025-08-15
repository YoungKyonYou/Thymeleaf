package com.example.thymeleaf.repository;


import com.example.thymeleaf.common.PageData;
import com.example.thymeleaf.dto.UserDto;

public interface UserRepository {
    PageData<UserDto> search(String q, int page, int size, String sort, String dir);

    UserDto findById(Long id);
    UserDto insert(UserDto u);
    UserDto update(Long id, UserDto u);
    void delete(Long id);
}
