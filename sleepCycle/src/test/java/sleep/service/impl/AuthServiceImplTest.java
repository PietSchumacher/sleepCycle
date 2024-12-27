package sleep.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import sleep.dto.RegisterDto;
import sleep.dto.SleepPersonDto;
import sleep.models.Role;
import sleep.models.User;
import sleep.repository.RoleRepository;
import sleep.repository.UserRepository;
import sleep.service.SleepPersonService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SleepPersonService sleepPersonService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void testRegister() {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("testUser");
        registerDto.setPassword("testPassword");
        SleepPersonDto sleepPersonDto = new SleepPersonDto();
        sleepPersonDto.setName("Test User");
        registerDto.setSleepPersonDto(sleepPersonDto);

        User mockUser = new User();
        mockUser.setId(1);

        Role mockRole = new Role();
        mockRole.setName("USER");

        when(roleRepository.findByName("USER")).thenReturn(java.util.Optional.of(mockRole));
        when(passwordEncoder.encode("testPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1);
            return user;
        });

        authService.register(registerDto);

        verify(userRepository).save(argThat(user ->
                user.getUsername().equals("testUser") &&
                        user.getPassword().equals("encodedPassword") &&
                        user.getRoles().contains(mockRole)
        ));

        verify(sleepPersonService).createSleepPerson(argThat(dto ->
                dto.getUserId() == 1 &&
                        dto.getName().equals("Test User")
        ));

        verify(roleRepository).findByName("USER");
    }
}
