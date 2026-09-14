package com.mapper;

import com.domain.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 실제 Postgres 컨테이너에 대해 UserMapper XML/쿼리 동작을 검증한다.
 * Flyway 가 컨텍스트 기동 시 V1__init.sql 을 적용하므로 스키마는 자동 준비된다.
 * 각 테스트는 @Transactional 로 롤백되어 격리된다.
 */
@Testcontainers
@SpringBootTest
@Transactional
class UserMapperIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    UserMapper userMapper;

    private UserDto newUser(String slug) {
        UserDto u = new UserDto();
        u.setUsername("u_" + slug);
        u.setEmail(slug + "@example.com");
        u.setLoginId(slug);
        u.setPassword("hashed_" + slug);
        return u;
    }

    @Test
    void insert_thenSelectById_excludesPassword() {
        UserDto u = newUser("hong1");
        userMapper.insert(u);
        assertNotNull(u.getId(), "insert 후 id 가 채워져야 한다");

        UserDto found = userMapper.selectById(u.getId());

        assertNotNull(found);
        assertEquals("hong1", found.getLoginId());
        assertEquals("u_hong1", found.getUsername());
        assertNull(found.getPassword(),
                "조회 응답용 selectById 는 password 를 노출하면 안 된다");
    }

    @Test
    void selectByLoginId_returnsRowIncludingPassword() {
        UserDto u = newUser("hong2");
        userMapper.insert(u);

        UserDto found = userMapper.selectByLoginId("hong2");

        assertNotNull(found);
        assertEquals("hashed_hong2", found.getPassword(),
                "인증용 selectByLoginId 는 password 를 포함해야 한다");
    }

    @Test
    void selectByLoginId_notFound_returnsNull() {
        assertNull(userMapper.selectByLoginId("no-such-user"));
    }

    @Test
    void update_changesOnlyUsernameAndEmail() {
        UserDto u = newUser("hong3");
        userMapper.insert(u);

        UserDto patch = new UserDto();
        patch.setId(u.getId());
        patch.setUsername("변경된이름");
        patch.setEmail("new@example.com");
        int affected = userMapper.update(patch);

        assertEquals(1, affected);

        UserDto reloaded = userMapper.selectByLoginId("hong3");
        assertEquals("변경된이름", reloaded.getUsername());
        assertEquals("new@example.com", reloaded.getEmail());
        assertEquals("hong3", reloaded.getLoginId(), "loginId 는 이 경로에서 변경 안 된다");
        assertEquals("hashed_hong3", reloaded.getPassword(), "password 도 변경 안 된다");
    }

    @Test
    void deleteById_returnsOneWhenFound_zeroWhenMissing() {
        UserDto u = newUser("hong4");
        userMapper.insert(u);

        assertEquals(1, userMapper.deleteById(u.getId()));
        assertEquals(0, userMapper.deleteById(u.getId()), "이미 삭제된 대상은 0 반환");
    }

    @Test
    void selectPage_returnsDescendingByIdAndRespectsLimitOffset() {
        UserDto a = newUser("aaa");
        UserDto b = newUser("bbb");
        UserDto c = newUser("ccc");
        userMapper.insert(a);
        userMapper.insert(b);
        userMapper.insert(c);

        // 최신 2건: c, b (id DESC)
        List<UserDto> firstPage = userMapper.selectPage(0, 2);
        assertEquals(2, firstPage.size());
        assertEquals("ccc", firstPage.get(0).getLoginId());
        assertEquals("bbb", firstPage.get(1).getLoginId());

        List<UserDto> secondPage = userMapper.selectPage(2, 2);
        assertEquals("aaa", secondPage.get(0).getLoginId());
    }

    @Test
    void countAll_matchesInserted() {
        long before = userMapper.countAll();
        userMapper.insert(newUser("cnt1"));
        userMapper.insert(newUser("cnt2"));

        assertEquals(before + 2, userMapper.countAll());
    }
}
