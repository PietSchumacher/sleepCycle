package sleep.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

@RestController
@RequestMapping("/api/auth/")
public class AuthController {

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

    @PostMapping("register")
    public ResponseEntity<String> register(@RequestBody RegisterDto registerDto) {
        if(userRepository.existsByUsername(registerDto.getUsername())) {
            return new ResponseEntity<>("Username ist bereits vergeben!", HttpStatus.BAD_REQUEST);
        }
        if(!registerDto.getControllPassword().equals(registerDto.getPassword())) {
            return new ResponseEntity<>("Die Passwörter stimmen nicht überein!", HttpStatus.BAD_REQUEST);
        }
        authService.register(registerDto);
        return new ResponseEntity<>("User wurde erfolgreich erstellt", HttpStatus.CREATED);
    }

    @PostMapping("login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginDto loginDto) {
        System.out.println(loginDto);
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtGenerator.generateToken(authentication);
        System.out.println("Hier die Anfrage:");
        System.out.println(new ResponseEntity<>(new AuthResponseDto(token), HttpStatus.OK));
        return new ResponseEntity<>(new AuthResponseDto(token), HttpStatus.OK);
    }
}
