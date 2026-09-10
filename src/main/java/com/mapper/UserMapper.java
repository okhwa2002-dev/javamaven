package com.mapper;

import com.domain.UserDto;

import java.util.List;

public interface UserMapper {

    UserDto selectById(Long id);

    UserDto selectByLoginId(String loginId);

    List<UserDto> selectAll();

    int insert(UserDto user);

    int update(UserDto user);

    int deleteById(Long id);
}
