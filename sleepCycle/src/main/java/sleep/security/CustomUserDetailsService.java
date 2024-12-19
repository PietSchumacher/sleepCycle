package sleep.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sleep.models.Role;
import sleep.models.User;
import sleep.repository.UserRepository;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Custom implementation of the UserDetailsService interface.
 * This service loads user-specific data (e.g., username, password, roles) from the database
 * and returns a Spring Security UserDetails object, which is used for authentication and authorization.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private UserRepository userRepository;

    public CustomUserDetailsService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads the user by username from the database.
     * This method is used by Spring Security to authenticate users based on their username.
     *
     * @param username The username of the user to be loaded.
     * @return A UserDetails object containing the user's credentials and authorities.
     * @throws UsernameNotFoundException If no user is found with the provided username.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), mapRolesToAuthorities(user.getRoles()));
    }

    /**
     * Maps the list of roles to a collection of GrantedAuthority objects.
     * Each role is mapped to a SimpleGrantedAuthority, which is used for authorization in Spring Security.
     *
     * @param roles The list of roles assigned to the user.
     * @return A collection of GrantedAuthority objects.
     */
    private Collection<GrantedAuthority> mapRolesToAuthorities(List<Role> roles) {
        return roles.stream().map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList());
    }
}
