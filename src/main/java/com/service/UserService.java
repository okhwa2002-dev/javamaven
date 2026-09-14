package com.service;

import com.cmn.exception.NotFoundException;
import com.domain.PageResponse;
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

    // 페이지 크기 상한. 클라이언트가 큰 값을 넣어도 서버 부담을 제한한다.
    private static final int MAX_PAGE_SIZE = 100;

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

    /**
     * 사용자 목록을 페이지 단위로 조회한다.
     * page 는 0-indexed. 잘못된 값은 기본값으로 보정한다.
     */
    @Transactional(readOnly = true)
    public PageResponse<UserDto> findPage(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int offset = safePage * safeSize;

        long total = userMapper.countAll();
        List<UserDto> content = total == 0
                ? List.of()
                : userMapper.selectPage(offset, safeSize);
        return new PageResponse<>(content, safePage, safeSize, total);
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
