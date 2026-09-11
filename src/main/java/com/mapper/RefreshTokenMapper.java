package com.mapper;

import com.domain.RefreshToken;

public interface RefreshTokenMapper {

    void insert(RefreshToken token);

    RefreshToken selectByHash(String tokenHash);

    int revokeById(Long id);
}
