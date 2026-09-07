package com.mapper;

import com.domain.UserDto;

import java.util.List;

public interface UserMapper {

    UserDto selectById(Long id);

    List<UserDto> selectAll();

    int insert(UserDto user);

    int update(UserDto user);

    int deleteById(Long id);
}
