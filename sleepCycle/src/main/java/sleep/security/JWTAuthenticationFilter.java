package sleep.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Custom filter that processes JWT authentication for incoming requests.
 * It checks for a valid JWT token in the Authorization header and authenticates the user if the token is valid.
 */
public class JWTAuthenticationFilter extends OncePerRequestFilter {
    private JwtGenerator jwtGenerator;
    private CustomUserDetailsService customUserDetailsService;

    public JWTAuthenticationFilter(final JwtGenerator jwtGenerator, final CustomUserDetailsService customUserDetailsService) {
        this.jwtGenerator = jwtGenerator;
        this.customUserDetailsService = customUserDetailsService;
    }

    /**
     * The main logic for filtering the incoming request and performing authentication.
     * This method is executed once per request and checks if a valid JWT token exists in the request.
     *
     * @param request The incoming HTTP request.
     * @param response The HTTP response.
     * @param filterChain The filter chain to continue the request processing.
     * @throws ServletException If an error occurs during filtering.
     * @throws IOException If an I/O error occurs during filtering.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = getJWTFromRequest(request);
        if (StringUtils.hasText(token) && jwtGenerator.validateToken(token)) {
            String username = jwtGenerator.getUsernameFromJWT(token);
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the JWT token from the Authorization header of the HTTP request.
     *
     * @param request The HTTP request containing the Authorization header.
     * @return The JWT token, or null if not found.
     */
    public String getJWTFromRequest(HttpServletRequest request) {
        String berarerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(berarerToken) && berarerToken.startsWith("Bearer ")) {
            return berarerToken.substring(7);
        }
        return null;
    }
}
