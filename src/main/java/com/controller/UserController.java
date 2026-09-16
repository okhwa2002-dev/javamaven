package com.controller;

import com.cmn.exception.ErrorResponse;
import com.cmn.exception.NotFoundException;
import com.cmn.jwt.AuthenticatedUser;
import com.domain.UserCreateRequest;
import com.domain.UserDto;
import com.domain.UserUpdateRequest;
import com.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "사용자 CRUD API")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "사용자 단건 조회",
               description = "본인 정보만 조회할 수 있다. 다른 id 는 404 로 응답한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자 없음 또는 접근 권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public UserDto get(@Parameter(description = "사용자 id", example = "1") @PathVariable Long id,
                       @RequestAttribute(AuthenticatedUser.ATTRIBUTE) AuthenticatedUser authUser) {
        assertSelf(id, authUser);
        return userService.findById(id);
    }

    @Operation(summary = "사용자 등록", description = "새 사용자를 생성한다.")
    @ApiResponse(responseCode = "200", description = "생성 성공")
    @PostMapping
    public ResponseEntity<UserDto> create(@Valid @RequestBody UserCreateRequest request) {
        UserDto created = userService.create(request);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "사용자 수정",
               description = "본인 정보만 수정할 수 있다. 다른 id 는 404 로 응답한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "사용자 없음 또는 접근 권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public UserDto update(@Parameter(description = "사용자 id", example = "1") @PathVariable Long id,
                          @Valid @RequestBody UserUpdateRequest request,
                          @RequestAttribute(AuthenticatedUser.ATTRIBUTE) AuthenticatedUser authUser) {
        assertSelf(id, authUser);
        return userService.update(id, request);
    }

    @Operation(summary = "사용자 삭제",
               description = "본인 정보만 삭제할 수 있다. 다른 id 는 404 로 응답한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "사용자 없음 또는 접근 권한 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Parameter(description = "사용자 id", example = "1") @PathVariable Long id,
                                       @RequestAttribute(AuthenticatedUser.ATTRIBUTE) AuthenticatedUser authUser) {
        assertSelf(id, authUser);
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // 존재 자체를 감추기 위해 인가 실패도 404 로 응답한다 (enumeration 방지).
    private void assertSelf(Long pathId, AuthenticatedUser authUser) {
        if (!authUser.userId().equals(pathId)) {
            throw new NotFoundException("User not found: " + pathId);
        }
    }
}
