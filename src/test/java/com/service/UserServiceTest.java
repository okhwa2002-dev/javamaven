package com.service;

import com.cmn.exception.NotFoundException;
import com.domain.PageResponse;
import com.domain.UserCreateRequest;
import com.domain.UserDto;
import com.domain.UserUpdateRequest;
import com.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserMapper userMapper;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UserService userService;

    // ----- findById -----

    @Test
    void findById_found_returnsUser() {
        UserDto stored = new UserDto();
        stored.setId(1L);
        when(userMapper.selectById(1L)).thenReturn(stored);

        assertSame(stored, userService.findById(1L));
    }

    @Test
    void findById_notFound_throwsNotFound() {
        when(userMapper.selectById(99L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> userService.findById(99L));
    }

    // ----- findPage -----

    @Test
    void findPage_computesOffset_andReturnsMetadata() {
        List<UserDto> stored = List.of(new UserDto(), new UserDto());
        when(userMapper.countAll()).thenReturn(45L);
        when(userMapper.selectPage(40, 20)).thenReturn(stored);

        PageResponse<UserDto> res = userService.findPage(2, 20);

        assertEquals(2, res.getPage());
        assertEquals(20, res.getSize());
        assertEquals(45L, res.getTotalElements());
        assertEquals(3, res.getTotalPages()); // ceil(45/20) = 3
        assertSame(stored, res.getContent());
    }

    @Test
    void findPage_zeroTotal_skipsSelect_andReturnsEmpty() {
        when(userMapper.countAll()).thenReturn(0L);

        PageResponse<UserDto> res = userService.findPage(0, 20);

        assertEquals(0L, res.getTotalElements());
        assertEquals(0, res.getTotalPages());
        assertTrue(res.getContent().isEmpty());
        verify(userMapper, org.mockito.Mockito.never())
                .selectPage(org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void findPage_negativePage_clampedToZero() {
        when(userMapper.countAll()).thenReturn(1L);
        when(userMapper.selectPage(0, 10)).thenReturn(List.of(new UserDto()));

        PageResponse<UserDto> res = userService.findPage(-3, 10);

        assertEquals(0, res.getPage());
    }

    @Test
    void findPage_sizeAboveMax_clampedTo100() {
        when(userMapper.countAll()).thenReturn(1L);
        when(userMapper.selectPage(0, 100)).thenReturn(List.of(new UserDto()));

        PageResponse<UserDto> res = userService.findPage(0, 9999);

        assertEquals(100, res.getSize());
    }

    @Test
    void findPage_sizeBelowOne_clampedToOne() {
        when(userMapper.countAll()).thenReturn(1L);
        when(userMapper.selectPage(0, 1)).thenReturn(List.of(new UserDto()));

        PageResponse<UserDto> res = userService.findPage(0, 0);

        assertEquals(1, res.getSize());
    }

    // ----- create -----

    @Test
    void create_encodesPassword_beforeInsert() {
        UserCreateRequest req = new UserCreateRequest();
        req.setUsername("hong");
        req.setEmail("hong@example.com");
        req.setLoginId("hong");
        req.setPassword("Plain123!");
        when(passwordEncoder.encode("Plain123!")).thenReturn("ENC");

        UserDto created = userService.create(req);

        ArgumentCaptor<UserDto> captor = ArgumentCaptor.forClass(UserDto.class);
        verify(userMapper).insert(captor.capture());
        UserDto inserted = captor.getValue();

        assertEquals("hong", inserted.getUsername());
        assertEquals("hong@example.com", inserted.getEmail());
        assertEquals("hong", inserted.getLoginId());
        assertEquals("ENC", inserted.getPassword());
        assertNotEquals("Plain123!", inserted.getPassword(),
                "평문이 mapper 로 전달되면 안 된다");

        assertSame(inserted, created);
    }

    // ----- update -----

    @Test
    void update_found_setsIdAndFields() {
        UserUpdateRequest req = new UserUpdateRequest();
        req.setUsername("new-name");
        req.setEmail("new@example.com");
        when(userMapper.update(org.mockito.ArgumentMatchers.any())).thenReturn(1);

        UserDto result = userService.update(5L, req);

        ArgumentCaptor<UserDto> captor = ArgumentCaptor.forClass(UserDto.class);
        verify(userMapper).update(captor.capture());
        UserDto sent = captor.getValue();

        assertEquals(5L, sent.getId());
        assertEquals("new-name", sent.getUsername());
        assertEquals("new@example.com", sent.getEmail());
        assertSame(sent, result);
    }

    @Test
    void update_notFound_throwsNotFound() {
        UserUpdateRequest req = new UserUpdateRequest();
        req.setUsername("x");
        req.setEmail("x@x.co");
        when(userMapper.update(org.mockito.ArgumentMatchers.any())).thenReturn(0);

        assertThrows(NotFoundException.class, () -> userService.update(99L, req));
    }

    // ----- delete -----

    @Test
    void delete_found_ok() {
        when(userMapper.deleteById(3L)).thenReturn(1);

        userService.delete(3L);

        verify(userMapper).deleteById(3L);
    }

    @Test
    void delete_notFound_throwsNotFound() {
        when(userMapper.deleteById(99L)).thenReturn(0);

        assertThrows(NotFoundException.class, () -> userService.delete(99L));
    }
}
