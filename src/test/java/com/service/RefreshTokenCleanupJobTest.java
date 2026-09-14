package com.service;

import com.mapper.RefreshTokenMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenCleanupJobTest {

    @Mock RefreshTokenMapper mapper;
    @InjectMocks RefreshTokenCleanupJob job;

    @Test
    void cleanup_delegatesToMapper() {
        when(mapper.deleteExpired()).thenReturn(3);

        job.cleanup();

        verify(mapper).deleteExpired();
    }

    @Test
    void cleanup_noExpiredRows_stillCallsMapper() {
        when(mapper.deleteExpired()).thenReturn(0);

        job.cleanup();

        verify(mapper).deleteExpired();
    }
}
