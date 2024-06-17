package org.example.newsfeed.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.Principal;
import org.example.newsfeed.config.WebSecurityConfig;
import org.example.newsfeed.dto.SignupRequestDTO;
import org.example.newsfeed.entity.User;
import org.example.newsfeed.exception.DuplicateUserException;
import org.example.newsfeed.jwt.JwtUtil;
import org.example.newsfeed.repository.UserRepository;
import org.example.newsfeed.security.UserDetailsImpl;
import org.example.newsfeed.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.ArgumentMatchers.any;


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
    private JwtUtil jwtUtil; // JwtUtil 모킹

    @MockBean
    private UserRepository userRepository; // UserRepository 모킹

    @Autowired
    private ObjectMapper objectMapper; // ObjectMapper 빈 주입

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity(new MockSpringSecurityFilter()))
            .build();
    }
    private void mockUserSetup() {
        // Mock 테스트 유져 생성
        String userId = "sollertia4351";
        String password = "robbie1234";
        String email = "sollertia@sparta.com";
        String comment = "내용 테스트";
        User testUser = new User(userId, password, email, comment);
        UserDetailsImpl testUserDetails = new UserDetailsImpl(testUser);
        mockPrincipal = new UsernamePasswordAuthenticationToken(testUserDetails, "", testUserDetails.getAuthorities());
    }

    @Test
    public void testSignupSuccess() throws Exception {
        // given
        MultiValueMap<String, String> signupRequestForm = new LinkedMultiValueMap<>();
        signupRequestForm.add("userId", "sollertia4351");
        signupRequestForm.add("password", "robbie1234");
        signupRequestForm.add("email", "sollertia@sparta.com");

        // when - then
        mvc.perform(post("/api/signup")
                .params(signupRequestForm)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statusCode").value("200"))
            .andExpect(jsonPath("$.message").value("회원가입에 성공하였습니다."));
    }

    @Test
    public void testSignupDuplicateUser() throws Exception {
        Mockito.doThrow(new DuplicateUserException("중복된 사용자가 존재합니다."))
            .when(userService).signup(any(SignupRequestDTO.class));

        mvc.perform(post("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"1\",\"password\":\"testpass\"}"))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errorCode").value("400"))
            .andExpect(jsonPath("$.errorMessage").value("중복된 사용자가 존재합니다."));
    }
}