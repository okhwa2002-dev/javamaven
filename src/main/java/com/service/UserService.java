package com.service;

import com.cmn.exception.NotFoundException;
import com.domain.UserCreateRequest;
import com.domain.UserDto;
import com.domain.UserUpdateRequest;
import com.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserDto findById(Long id) {
        UserDto user = userMapper.selectById(id);
        if (user == null) {
            throw new NotFoundException("User not found: " + id);
        }
        return user;
    }

    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        return userMapper.selectAll();
    }

    @Transactional
    public UserDto create(UserCreateRequest request) {
        UserDto user = new UserDto();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setLoginId(request.getLoginId());
        // 평문이 DB에 저장되지 않도록 반드시 인코딩 후 저장한다.
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userMapper.insert(user);
        return user;
    }

    @Transactional
    public UserDto update(Long id, UserUpdateRequest request) {
        UserDto user = new UserDto();
        user.setId(id);
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        int updated = userMapper.update(user);
        if (updated == 0) {
            throw new NotFoundException("User not found: " + id);
        }
        return user;
    }

    @Transactional
    public void delete(Long id) {
        int deleted = userMapper.deleteById(id);
        if (deleted == 0) {
            throw new NotFoundException("User not found: " + id);
        }
    }
}
