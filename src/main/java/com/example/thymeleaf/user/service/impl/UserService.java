package com.example.thymeleaf.user.service.impl;

import com.example.thymeleaf.common.PageData;
import com.example.thymeleaf.user.dto.UserDto;
import java.util.List;


public interface UserService {

    PageData<UserDto> search(String q, String by, int page, int size, String sort, String dir);



    long count(String q, String by);

    List<UserDto> selectPage(String q, String by, int offset, int limit, String sort, String dir);

    UserDto selectOne(Long id);

    UserDto insertOne(UserDto u);

    UserDto updateOne(UserDto u);

    void deleteOne(Long id);
}
