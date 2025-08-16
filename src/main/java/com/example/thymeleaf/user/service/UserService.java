package com.example.thymeleaf.user.service;

import com.example.thymeleaf.common.PageData;
import com.example.thymeleaf.user.dto.UserDto;
import com.example.thymeleaf.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    @Transactional(readOnly = true)
    public PageData<UserDto> search(String q, String by, int page, int size, String sort, String dir) {
        return userRepository.search(q, by, page, size, sort, dir);
    }

    @Transactional(readOnly = true)
    public UserDto findUser(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public UserDto createUser(UserDto user){
        return userRepository.insert(user);
    }

    @Transactional
    public UserDto updateUser(Long id, UserDto user) {
        return userRepository.update(id, user);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.delete(id);
    }


}
