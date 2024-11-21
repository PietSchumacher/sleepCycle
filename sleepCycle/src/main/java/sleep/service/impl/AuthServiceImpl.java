package sleep.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sleep.dto.RegisterDto;
import sleep.dto.SleepPersonDto;
import sleep.models.Role;
import sleep.models.SleepPerson;
import sleep.models.User;
import sleep.repository.RoleRepository;
import sleep.repository.SleepPersonRepository;
import sleep.repository.UserRepository;
import sleep.service.AuthService;
import sleep.service.SleepPersonService;

import java.util.Collections;

@Service
public class AuthServiceImpl implements AuthService {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private SleepPersonService sleepPersonService;

    public AuthServiceImpl(final UserRepository userRepository, final RoleRepository roleRepository, PasswordEncoder passwordEncoder, SleepPersonService sleepPersonService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.sleepPersonService = sleepPersonService;
    }

    @Override
    public void register(RegisterDto registerDto) {
        User user = new User();
        user.setUsername(registerDto.getUsername());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        // standard USER ROLE
        Role roles = roleRepository.findByName("USER").get();
        user.setRoles(Collections.singletonList(roles));
        userRepository.save(user);
        SleepPersonDto sleepPersonDto = registerDto.getSleepPersonDto();
        sleepPersonDto.setUserId(user.getId());
        sleepPersonService.createSleepPerson(sleepPersonDto);
    }
}
