package com.mapper;

import com.domain.UserDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {

    UserDto selectById(Long id);

    UserDto selectByLoginId(String loginId);

    List<UserDto> selectPage(@Param("offset") int offset, @Param("size") int size);

    long countAll();

    int insert(UserDto user);

    int update(UserDto user);

    int deleteById(Long id);
}
