package com.cmn.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/api/users/1");
        request = req;
    }

    @Test
    void notFound_mapsTo404() {
        ResponseEntity<ErrorResponse> res = handler.handleNotFound(
                new NotFoundException("User not found: 1"), request);

        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
        ErrorResponse body = res.getBody();
        assertNotNull(body);
        assertEquals(404, body.getStatus());
        assertEquals("User not found: 1", body.getMessage());
        assertEquals("/api/users/1", body.getPath());
    }

    @Test
    void unauthorized_mapsTo401() {
        ResponseEntity<ErrorResponse> res = handler.handleUnauthorized(
                new UnauthorizedException("bad creds"), request);

        assertEquals(HttpStatus.UNAUTHORIZED, res.getStatusCode());
        assertEquals(401, res.getBody().getStatus());
    }

    @Test
    void illegalArgument_mapsTo400() {
        ResponseEntity<ErrorResponse> res = handler.handleIllegalArgument(
                new IllegalArgumentException("param X is invalid"), request);

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals(400, res.getBody().getStatus());
        assertEquals("param X is invalid", res.getBody().getMessage());
    }

    @Test
    void methodArgumentNotValid_aggregatesFieldErrors() throws Exception {
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), "target");
        binding.addError(new FieldError("target", "loginId", "loginId is required"));
        binding.addError(new FieldError("target", "password", "password must be 8-72 characters"));

        Method dummy = DummyController.class.getDeclaredMethod("dummy", String.class);
        MethodParameter parameter = new MethodParameter(dummy, 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, binding);

        ResponseEntity<ErrorResponse> res = handler.handleValidation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        String msg = res.getBody().getMessage();
        assertTrue(msg.contains("loginId: loginId is required"), msg);
        assertTrue(msg.contains("password: password must be 8-72 characters"), msg);
    }

    @Test
    void unreadable_mapsTo400_withGenericMessage() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                "cause",
                new MockHttpInputMessage(new byte[0]));

        ResponseEntity<ErrorResponse> res = handler.handleUnreadable(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals("Malformed request body", res.getBody().getMessage());
    }

    @Test
    void unexpectedException_mapsTo500_withGenericMessage() {
        ResponseEntity<ErrorResponse> res = handler.handleUnexpected(
                new RuntimeException("stack trace with secrets"), request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, res.getStatusCode());
        // 내부 상세 메시지는 응답에 노출되면 안 된다
        assertEquals("Internal server error", res.getBody().getMessage());
    }

    // MethodParameter 를 만들기 위한 더미
    static class DummyController {
        @SuppressWarnings("unused")
        public void dummy(String param) {}
    }
}
