package sleep.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import sleep.dto.AuthResponseDto;
import sleep.dto.LoginDto;
import sleep.dto.RegisterDto;
import sleep.repository.RoleRepository;
import sleep.repository.UserRepository;
import sleep.security.JwtGenerator;
import sleep.service.AuthService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtGenerator jwtGenerator;

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthController authController;

    private RegisterDto registerDto;

    private LoginDto loginDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        registerDto = new RegisterDto();
        registerDto.setUsername("testUser");
        registerDto.setPassword("password");

        loginDto = new LoginDto();
        loginDto.setUsername("testUser");
        loginDto.setPassword("password");
    }

    @Test
    void testRegisterUserAlreadyExists() {
        registerDto.setControllPassword("password");

        when(userRepository.existsByUsername("testUser")).thenReturn(true);

        ResponseEntity<String> response = authController.register(registerDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Username ist bereits vergeben!", response.getBody());
    }

    @Test
    void testRegisterPasswordsDoNotMatch() {
        registerDto.setControllPassword("otherPassword");

        ResponseEntity<String> response = authController.register(registerDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Die Passwörter stimmen nicht überein!", response.getBody());
    }

    @Test
    void testRegisterExpectInternalServerError() {
        registerDto.setControllPassword("password");

        when(userRepository.existsByUsername("testUser")).thenReturn(false);
        doThrow(NullPointerException.class).when(authService).register(any());

        ResponseEntity<String> response = authController.register(registerDto);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Ein Fehler ist aufgetreten"));
    }

    @Test
    void testRegisterSuccess() {
        registerDto.setControllPassword("password");

        when(userRepository.existsByUsername("testUser")).thenReturn(false);

        ResponseEntity<String> response = authController.register(registerDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("User wurde erfolgreich erstellt", response.getBody());
    }

    @Test
    void testLoginSuccess() {
        loginDto.setPassword("password");

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtGenerator.generateToken(authentication)).thenReturn("testToken");

        ResponseEntity<AuthResponseDto> responseEntity = authController.login(loginDto, response);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals("testToken", responseEntity.getBody().getToken());

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookieCaptor.capture());
        Cookie cookie = cookieCaptor.getValue();

        assertEquals("auth_token", cookie.getName());
        assertEquals("testToken", cookie.getValue());
        assertTrue(cookie.isHttpOnly());
    }

    @Test
    void testLoginFailure() {
        loginDto.setPassword("otherPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Authentication failed"));

        ResponseEntity<AuthResponseDto> responseEntity = authController.login(loginDto, response);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody());
    }

    @Test
    void testLogout() {
        ResponseEntity<String> responseEntity = authController.logout(response);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Logout erfolgreich", responseEntity.getBody());

        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookieCaptor.capture());
        Cookie cookie = cookieCaptor.getValue();

        assertEquals("auth_token", cookie.getName());
        assertNull(cookie.getValue());
        assertEquals(0, cookie.getMaxAge());
    }
}