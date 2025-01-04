package sleep.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sleep.dto.RegisterDto;
import sleep.dto.SleepPersonDto;
import sleep.models.Role;
import sleep.models.User;
import sleep.repository.RoleRepository;
import sleep.repository.UserRepository;
import sleep.service.AuthService;
import sleep.service.SleepPersonService;

import java.util.Collections;

/**
 * Implementation of the AuthService interface for user authentication and registration functionality.
 *
 * Provides methods to register new users with associated roles and linked SleepPerson entities.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

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

    /**
     * Registers a new user with associated role and linked SleepPerson entity.
     *
     * - The method hashes the user's password.
     * - Assigns a default "USER" role.
     * - Links a SleepPerson entity to the user using data from the RegisterDto.
     *
     * @param registerDto Data transfer object containing user registration details and linked SleepPerson data.
     */
    @Override
    public void register(RegisterDto registerDto) {
        logger.info("Erstelle den User: " + registerDto.getUsername());
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
