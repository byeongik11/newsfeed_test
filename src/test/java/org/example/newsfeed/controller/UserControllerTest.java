package org.example.newsfeed.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.Principal;
import org.example.newsfeed.config.WebSecurityConfig;
import org.example.newsfeed.dto.PasswordRequestDTO;
import org.example.newsfeed.dto.SignupRequestDTO;
import org.example.newsfeed.dto.UserRequestDTO;
import org.example.newsfeed.dto.WithdrawRequestDTO;
import org.example.newsfeed.entity.User;
import org.example.newsfeed.exception.AlreadyWithdrawnUserException;
import org.example.newsfeed.exception.DuplicateUserException;
import org.example.newsfeed.exception.PasswordMismatchException;
import org.example.newsfeed.exception.UserIdNotFoundException;
import org.example.newsfeed.jwt.JwtUtil;
import org.example.newsfeed.repository.UserRepository;
import org.example.newsfeed.security.UserDetailsImpl;
import org.example.newsfeed.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureMockMvc
@WebMvcTest(
    controllers = {UserController.class, UserController.class},
    excludeFilters = {
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = WebSecurityConfig.class
        )
    }
)
class UserControllerTest {


    @Autowired
    private MockMvc mvc;

    private Principal mockPrincipal;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private UserService userService;

    @MockBean
    private UserDetailsImpl userDetails;

    @MockBean
    private JwtUtil jwtUtil; // JwtUtil 모킹

    @Autowired
    private ObjectMapper objectMapper; // ObjectMapper 빈 주입

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity(new MockSpringSecurityFilter()))
            .build();
    }

    @Test
    @DisplayName("회원가입 성공테스트")
    public void testSignupSuccess() throws Exception {
        // given
        SignupRequestDTO requestDTO = new SignupRequestDTO();
        requestDTO.setUserId("sollertia4351");
        requestDTO.setPassword("Robbie12344!!@@");
        requestDTO.setName("robbie1234");
        requestDTO.setComment("내용 테스트");
        requestDTO.setEmail("sollertia@sparta.com");

        // when - then
        mvc.perform(post("/api/signup")
                .content(objectMapper.writeValueAsString(requestDTO))
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statusCode").value("200"))
            .andExpect(jsonPath("$.message").value("회원가입에 성공하였습니다."));
    }

    @Test
    @DisplayName("회원가입 중복 테스트")
    public void testSignupDuplicateUser() throws Exception {
        SignupRequestDTO requestDTO = new SignupRequestDTO();
        requestDTO.setUserId("sollertia4351");
        requestDTO.setPassword("Robbie12344!!@@");
        requestDTO.setName("robbie1234");
        requestDTO.setComment("내용 테스트");
        requestDTO.setEmail("sollertia@sparta.com");

        Mockito.doThrow(new DuplicateUserException("중복된 사용자가 존재합니다."))
            .when(userService).signup(any(SignupRequestDTO.class));

        mvc.perform(post("/api/signup")
                .content(objectMapper.writeValueAsString(requestDTO))
                .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errorCode").value("400"))
            .andExpect(jsonPath("$.errorMessage").value("중복된 사용자가 존재합니다."));
    }

    @Test
    @DisplayName("탈퇴 테스트")
    public void testWithdrawUserSuccess() throws Exception {
        WithdrawRequestDTO request = new WithdrawRequestDTO();
        request.setUserId("user1");
        request.setPassword("password");

        // Mock the behavior of userService.withdrawUser
        Mockito.doNothing().when(userService)
            .withdrawUser(Mockito.anyString(), Mockito.anyString());

        mvc.perform(patch("/api/deletemembers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"message\":\"회원 탈퇴에 성공하였습니다. 감사합니다.\"}"));
    }

    @Test
    @DisplayName("탈퇴후 사용자를 찾을 수 없는 테스트")
    public void testWithdrawUserIdNotFound() throws Exception {
        WithdrawRequestDTO request = new WithdrawRequestDTO();
        request.setUserId("user1");
        request.setPassword("password");

        // Mock the behavior to throw UserIdNotFoundException
        Mockito.doThrow(new UserIdNotFoundException("해당 사용자를 찾을 수 없습니다.")).when(userService)
            .withdrawUser(Mockito.anyString(), Mockito.anyString());

        mvc.perform(patch("/api/deletemembers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(
                content().json("{\"errorCode\":\"404\",\"errorMessage\":\"해당 사용자를 찾을 수 없습니다.\"}"));
    }

    @Test
    @DisplayName("탈퇴할 때 아이디 비밀번호가 일치하지 않는 테스트")
    public void testWithdrawPasswordMismatch() throws Exception {
        WithdrawRequestDTO request = new WithdrawRequestDTO();
        request.setUserId("user1");
        request.setPassword("wrongpassword");

        Mockito.doThrow(new PasswordMismatchException("아이디와 비밀번호가 일치하지 않습니다.")).when(userService)
            .withdrawUser(Mockito.anyString(), Mockito.anyString());

        mvc.perform(patch("/api/deletemembers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(content().json(
                "{\"errorCode\":\"400\",\"errorMessage\":\"아이디와 비밀번호가 일치하지 않습니다.\"}"));
    }

    @Test
    @DisplayName("이미 탈퇴한 사용자인지 아닌지 테스트")
    public void testWithdrawAlreadyWithdrawnUser() throws Exception {
        WithdrawRequestDTO request = new WithdrawRequestDTO();
        request.setUserId("user1");
        request.setPassword("password");

        Mockito.doThrow(new AlreadyWithdrawnUserException("이미 탈퇴된 사용자입니다.")).when(userService)
            .withdrawUser(Mockito.anyString(), Mockito.anyString());

        mvc.perform(patch("/api/deletemembers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(
                content().json("{\"errorCode\":\"400\",\"errorMessage\":\"이미 탈퇴된 사용자입니다.\"}"));
    }

    @Test
    @DisplayName("테스트 실패")
    @WithMockUser(username = "user1", roles = {"USER"})
    public void testGetUserProfileSuccess() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUserId("user112345");
        user.setName("user112345");

        // Mock the behavior of userService.getUser
        Mockito.when(userService.getUser(1L)).thenReturn(user);

        mvc.perform(get("/api/profile"))
            .andExpect(status().isOk())
            .andExpect(content().json(
                "{\"status\":\"200\",\"data\":{\"id\":1,\"username\":\"user1\"},\"error\":null}"));
    }

    @Test
    @DisplayName("테스트 실패")
    @WithMockUser(username = "user1", roles = {"USER"})
    public void testGetUserProfileFail() throws Exception {
        // Mock the behavior to throw IllegalArgumentException
        Mockito.when(userService.getUser(1L))
            .thenThrow(new IllegalArgumentException("Invalid user ID"));

        mvc.perform(patch("/api/profile"))
            .andExpect(status().isBadRequest())
            .andExpect(content().json(
                "{\"errorCode\":\"400\",\"message\":\"프로필 조회에 실패하였습니다.\",\"details\":\"Invalid user ID\"}"));
    }

    @Test
    @DisplayName("테스트 실패")
    @WithMockUser(username = "user1", roles = {"USER"})
    public void testUpdatePasswordSuccess() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName("user1");
        user.setPassword("oldPassword");

        PasswordRequestDTO passwordRequestDTO = new PasswordRequestDTO();
        passwordRequestDTO.setBeforePassword("oldPassword");
        passwordRequestDTO.setUpdatePassword("newPassword");

        Mockito.doNothing().when(userService)
            .updatePassword(Mockito.eq(1L), Mockito.any(PasswordRequestDTO.class));

        mvc.perform(put("/api/profile/password")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(passwordRequestDTO)))
            .andExpect(status().isOk())
            .andExpect(content().json(
                "{\"status\":\"200\",\"data\":\"비밀번호 수정에 성공하셨습니다.\",\"error\":null}"));
    }

    @Test
    @DisplayName("테스트 실패")
    @WithMockUser(username = "user1", roles = {"USER"})
    public void testUpdatePasswordFail() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName("user1");
        user.setPassword("user12345");

        PasswordRequestDTO passwordRequestDTO = new PasswordRequestDTO();
        passwordRequestDTO.setBeforePassword("oldPassword");
        passwordRequestDTO.setUpdatePassword("newPassword");

        Mockito.doThrow(new IllegalArgumentException("Invalid password")).when(userService)
            .updatePassword(Mockito.eq(1L), Mockito.any(
                PasswordRequestDTO.class));

        mvc.perform(put("/api/profile/password")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(passwordRequestDTO)))
            .andExpect(status().isBadRequest())
            .andExpect(content().json(
                "{\"errorCode\":\"400\",\"message\":\"비밀번호 수정에 실패하였습니다.\",\"details\":\"Invalid password\"}"));
    }
}