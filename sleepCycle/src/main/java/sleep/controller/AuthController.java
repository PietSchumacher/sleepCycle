package sleep.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import sleep.dto.AuthResponseDto;
import sleep.dto.LoginDto;
import sleep.dto.RegisterDto;
import sleep.models.Role;
import sleep.models.User;
import sleep.repository.RoleRepository;
import sleep.repository.UserRepository;
import sleep.security.JwtGenerator;
import sleep.service.AuthService;

import java.util.Collections;

/**
 * Rest Controller for managing authentication.
 *
 * Provides endpoints for register, login and logout of a user.
 */
@RestController
@RequestMapping("/api/auth/")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private AuthenticationManager authenticationManager;
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private JwtGenerator jwtGenerator;
    private AuthService authService;

    public AuthController(final AuthenticationManager authenticationManager, final UserRepository userRepository, final RoleRepository roleRepository, final PasswordEncoder passwordEncoder, final JwtGenerator jwtGenerator, final AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtGenerator = jwtGenerator;
        this.authService = authService;
    }

    /**
     * Registers a new user in the system.
     *
     * @param registerDto DTO containing user registration details.
     * @return ResponseEntity with success or error message and appropriate HTTP status code.
     */
    @PostMapping("register")
    public ResponseEntity<String> register(@RequestBody RegisterDto registerDto) {
        logger.info("Registrierung für {} aufgerufen", registerDto.getUsername());

        if(userRepository.existsByUsername(registerDto.getUsername())) {
            logger.warn("Registrierung fehlgeschlagen: Username wird schon verwendet");
            return new ResponseEntity<>("Username ist bereits vergeben!", HttpStatus.BAD_REQUEST);
        }
        if(!registerDto.getControllPassword().equals(registerDto.getPassword())) {
            logger.warn("Registrierung fehlgeschlagen: Passwörter stimmen nicht überein");
            return new ResponseEntity<>("Die Passwörter stimmen nicht überein!", HttpStatus.BAD_REQUEST);
        }

        try {
            logger.info("Versuche User zur registrieren: {}", registerDto.getUsername());
            authService.register(registerDto);
            logger.info("User '{}' erfolgreich registriert", registerDto.getUsername());
            return new ResponseEntity<>("User wurde erfolgreich erstellt", HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Registrierung fehlgeschlagen für den User '{}': {}", registerDto.getUsername(), e.getMessage(), e);
            return new ResponseEntity<>("Ein Fehler ist aufgetreten", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param loginDto DTO containing username and password.
     * @param response HTTP response for setting the authentication cookie.
     * @return ResponseEntity with AuthResponseDto containing the JWT token, or an error status.
     */
    @PostMapping("login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginDto loginDto, HttpServletResponse response) {
        logger.info("Login für {} aufgerufen", loginDto.getUsername());

        try {
            // auth
            logger.info("Versuche User: {} zu authentifizieren", loginDto.getUsername());
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info("User '{}' erfolgreich authentifiziert", loginDto.getUsername());

            String token = jwtGenerator.generateToken(authentication);
            logger.debug("Generiere JWT Token für User: {}", loginDto.getUsername());

            // set cookie
            Cookie cookie = new Cookie("auth_token", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            cookie.setPath("/");
            cookie.setMaxAge(1200); // 20 minutes
            response.addCookie(cookie);
            logger.info("Auth Token cookie für User: {} gesetzt", loginDto.getUsername());

            return new ResponseEntity<>(new AuthResponseDto(token), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Login fehlgeschlagen für User '{}': {}", loginDto.getUsername(), e.getMessage());
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Logs out a user by invalidating the authentication cookie.
     *
     * @param response HTTP response to clear the authentication cookie.
     * @return ResponseEntity with a success message.
     */
    @PostMapping("logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        logger.info("Logout wird aufgerufen und Auth Cookie gelöscht");
        Cookie cookie = new Cookie("auth_token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok("Logout erfolgreich");
    }


}
