package com.mapper;

import com.domain.RefreshToken;

public interface RefreshTokenMapper {

    void insert(RefreshToken token);

    RefreshToken selectByHash(String tokenHash);

    int revokeById(Long id);

    /**
     * 이미 만료된 토큰 행을 삭제한다. 반환값은 삭제된 row 수.
     * 스케줄러(RefreshTokenCleanupJob) 가 주기적으로 호출한다.
     */
    int deleteExpired();
}
