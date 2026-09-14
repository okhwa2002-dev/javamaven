package com.service;

import com.mapper.RefreshTokenMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 만료된 리프레시 토큰 행을 주기적으로 정리한다.
 *
 * <p>리프레시 회전(rotation) 을 도입한 뒤로 재발급마다 새 행이 삽입되고 이전 것은
 * revoke 마킹만 되어 계속 쌓인다. 만료 시각이 지난 행은 어차피 사용될 수 없으므로
 * 삭제해 테이블을 관리한다.
 *
 * <p>revoke 됐지만 아직 만료 전인 행은 그대로 둔다. 자연 만료(14일) 후 이 작업이 걷어감.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupJob {

    private final RefreshTokenMapper mapper;

    // 매일 04:00 (초 분 시 일 월 요일)
    @Scheduled(cron = "0 0 4 * * *")
    public void cleanup() {
        int removed = mapper.deleteExpired();
        if (removed > 0) {
            log.info("Expired refresh tokens removed: {}", removed);
        }
    }
}
