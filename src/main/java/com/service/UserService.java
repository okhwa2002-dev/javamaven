package com.service;

import com.cmn.exception.NotFoundException;
import com.domain.UserDto;
import com.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

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
    public UserDto create(UserDto user) {
        userMapper.insert(user);
        return user;
    }

    @Transactional
    public UserDto update(Long id, UserDto user) {
        user.setId(id);
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
