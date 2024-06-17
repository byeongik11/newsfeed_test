package org.example.newsfeed.entity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import org.example.newsfeed.dto.PasswordRequestDTO;
import org.example.newsfeed.dto.UserRequestDTO;
import org.example.newsfeed.exception.InvalidPasswordException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class UserTest {

    @InjectMocks
    private User user;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        user = User.builder()
            .userId("testUserId")
            .password("$2a$10$abcdefg1234567") // 이미 암호화된 패스워드
            .name("Test User")
            .email("test@example.com")
            .comment("comment")
            .refreshToken("refreshToken")
            .statusChangeTime("2024-01-01T00:00:00")
            .status(UserStatusEnum.ACTIVE.getStatus())
            .newsfeeds(new ArrayList<>())
            .build();
    }

    @Test
    @DisplayName("사용자 변경 테스트")
    public void testUpdateUser() {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setName("Updated User");
        dto.setComment("Updated comment");

        user.updateUser(dto);

        assertEquals("Updated User", user.getName());
        assertEquals("Updated comment", user.getComment());
    }

    @Test
    @DisplayName("패스워드 변경 테스트")
    public void testUpdatePassword_Success() {
        PasswordRequestDTO dto = new PasswordRequestDTO();
        dto.setBeforePassword("oldPassword");
        dto.setUpdatePassword("newPassword");

        when(passwordEncoder.matches("oldPassword", user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("$2a$10$newpasswordhash");

        user.updatePassword(dto, passwordEncoder);

        assertEquals("$2a$10$newpasswordhash", user.getPassword());
    }

    @Test
    @DisplayName("패스워드 변경 실패 테스트")
    public void testUpdatePassword_Failure() {
        PasswordRequestDTO dto = new PasswordRequestDTO();
        dto.setBeforePassword("wrongPassword");
        dto.setUpdatePassword("newPassword");

        when(passwordEncoder.matches("wrongPassword", user.getPassword())).thenReturn(false);

        InvalidPasswordException thrown = assertThrows(InvalidPasswordException.class, () -> {
            user.updatePassword(dto, passwordEncoder);
        });

        assertEquals("패스워드가 일치하지 않습니다.", thrown.getMessage());
    }

    @Test
    @DisplayName("상태값 테스트")
    public void testSetStatus() {
        LocalDateTime beforeTime = LocalDateTime.now();
        user.setStatus(UserStatusEnum.ACTIVE);

        assertEquals(UserStatusEnum.ACTIVE.getStatus(), user.getStatus());
        assertNotNull(user.getModifyDate());

        // LocalDateTime.now()와 modifyDate 간의 시간 차이가 1초 이내인지 확인
        assertTrue(ChronoUnit.SECONDS.between(beforeTime, user.getModifyDate()) <= 1,
            "modifyDate는 상태 설정 직후에 설정되어야 합니다.");
    }
}