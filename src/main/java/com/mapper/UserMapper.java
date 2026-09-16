package com.mapper;

import com.domain.UserDto;

public interface UserMapper {

    UserDto selectById(Long id);

    UserDto selectByLoginId(String loginId);

    int insert(UserDto user);

    int update(UserDto user);

    int deleteById(Long id);
}
