package sleep.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

/**
 * Security configuration class for setting up authentication and authorization rules for the application.
 * It includes settings for JWT-based authentication, password encoding, and custom user details services.
 */
@Configuration
@EnableWebSecurity(debug = false)
public class SecurityConfig {

    private JwtEntryPoint jwtEntryPoint;
    private CustomUserDetailsService userDetailsService;
    private JwtGenerator jwtGenerator;

    public SecurityConfig(final CustomUserDetailsService userDetailsService, JwtEntryPoint jwtEntryPoint, JwtGenerator jwtGenerator) {
        this.userDetailsService = userDetailsService;
        this.jwtEntryPoint = jwtEntryPoint;
        this.jwtGenerator = jwtGenerator;
    }

    /**
     * Configures the security filter chain, including HTTP request authorization and CSRF token handling.
     *
     * - Configures CSRF token repository using cookies.
     * - Defines rules for session management (stateless).
     * - Specifies URL patterns that should be publicly accessible or require authentication.
     * - Adds JWT authentication filter before the standard UsernamePasswordAuthenticationFilter.
     *
     * @param http HttpSecurity object to configure security settings
     * @return SecurityFilterChain with defined settings
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                .exceptionHandling(customEx -> customEx.authenticationEntryPoint(jwtEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll() // access to static resources
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/gatherSleepSessions", "/personalOverview","/login", "/register", "/", "/profile", "/optimization", "/api/session/getByDate").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/session/*/update", "/api/person/*/update").permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/session/create", "/api/session/*/delete", "api/person/*/delete").permitAll()
                        .anyRequest().authenticated()
                );
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * Configures the AuthenticationManager bean for handling user authentication.
     *
     * @param authenticationConfiguration The configuration that provides the authentication manager
     * @return The AuthenticationManager bean
     * @throws Exception if authentication manager creation fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Provides a PasswordEncoder bean for securely hashing passwords.
     *
     * @return The BCryptPasswordEncoder instance used for password hashing
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Creates and returns the JWTAuthenticationFilter bean for handling JWT-based authentication.
     *
     * @return JWTAuthenticationFilter that will intercept HTTP requests and handle JWT validation
     * @throws Exception if the creation of the filter fails
     */
    @Bean
    public JWTAuthenticationFilter jwtAuthenticationFilter() throws Exception {
        return new JWTAuthenticationFilter(jwtGenerator, userDetailsService);
    }
}
