package com.controller;

import com.cmn.exception.ErrorResponse;
import com.domain.PageResponse;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "사용자 CRUD API")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "사용자 목록 조회",
               description = "사용자를 페이지 단위로 반환한다. page 는 0-indexed, size 는 1~100 범위.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public PageResponse<UserDto> list(
            @Parameter(description = "페이지 번호(0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        return userService.findPage(page, size);
    }

    @Operation(summary = "사용자 단건 조회", description = "id로 사용자 한 명을 조회한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public UserDto get(@Parameter(description = "사용자 id", example = "1") @PathVariable Long id) {
        return userService.findById(id);
    }

    @Operation(summary = "사용자 등록", description = "새 사용자를 생성한다.")
    @ApiResponse(responseCode = "200", description = "생성 성공")
    @PostMapping
    public ResponseEntity<UserDto> create(@Valid @RequestBody UserCreateRequest request) {
        UserDto created = userService.create(request);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "사용자 수정", description = "id에 해당하는 사용자를 수정한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public UserDto update(@Parameter(description = "사용자 id", example = "1") @PathVariable Long id,
                          @Valid @RequestBody UserUpdateRequest request) {
        return userService.update(id, request);
    }

    @Operation(summary = "사용자 삭제", description = "id에 해당하는 사용자를 삭제한다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Parameter(description = "사용자 id", example = "1") @PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
