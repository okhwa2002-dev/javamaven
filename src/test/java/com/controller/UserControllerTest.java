package com.controller;

import com.cmn.exception.NotFoundException;
import com.cmn.jwt.AuthenticatedUser;
import com.domain.UserDto;
import com.domain.UserUpdateRequest;
import com.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * /users/{id} 는 본인 리소스만 접근할 수 있어야 한다.
 * IDOR 방지를 위해 인가 실패는 존재까지 감추도록 404 로 응답한다.
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock UserService userService;
    @InjectMocks UserController userController;

    private AuthenticatedUser auth(long userId) {
        return new AuthenticatedUser(userId, "u" + userId);
    }

    // ----- get -----

    @Test
    void get_self_returnsUser() {
        UserDto stored = new UserDto();
        stored.setId(1L);
        when(userService.findById(1L)).thenReturn(stored);

        assertSame(stored, userController.get(1L, auth(1L)));
    }

    @Test
    void get_other_throwsNotFound_andDoesNotCallService() {
        assertThrows(NotFoundException.class,
                () -> userController.get(1L, auth(2L)));

        verifyNoInteractions(userService);
    }

    // ----- update -----

    @Test
    void update_self_delegatesToService() {
        UserUpdateRequest req = new UserUpdateRequest();
        req.setUsername("new-name");
        req.setEmail("new@example.com");
        UserDto result = new UserDto();
        when(userService.update(1L, req)).thenReturn(result);

        assertSame(result, userController.update(1L, req, auth(1L)));
    }

    @Test
    void update_other_throwsNotFound_andDoesNotCallService() {
        UserUpdateRequest req = new UserUpdateRequest();
        req.setUsername("x");
        req.setEmail("x@x.com");

        assertThrows(NotFoundException.class,
                () -> userController.update(1L, req, auth(2L)));

        verifyNoInteractions(userService);
    }

    // ----- delete -----

    @Test
    void delete_self_delegatesToService_andReturns204() {
        ResponseEntity<Void> response = userController.delete(1L, auth(1L));

        verify(userService).delete(1L);
        assertEquals(204, response.getStatusCode().value());
        assertNull(response.getBody());
    }

    @Test
    void delete_other_throwsNotFound_andDoesNotCallService() {
        assertThrows(NotFoundException.class,
                () -> userController.delete(1L, auth(2L)));

        verifyNoInteractions(userService);
    }
}
