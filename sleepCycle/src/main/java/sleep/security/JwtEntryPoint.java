package sleep.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * This class is a custom implementation of Spring Security's AuthenticationEntryPoint.
 * It handles unauthorized access attempts by sending an HTTP 401 Unauthorized response.
 */
@Component
public class JwtEntryPoint implements AuthenticationEntryPoint {

    /**
     * This method is triggered when an unauthenticated request tries to access a protected resource.
     * It sends an HTTP 401 Unauthorized status code along with the exception message.
     *
     * @param request The HTTP request that triggered the unauthorized access.
     * @param response The HTTP response to be sent to the client.
     * @param authException The exception that caused the authentication failure.
     * @throws IOException If there is an input/output error while sending the response.
     * @throws ServletException If there is an error in the servlet processing.
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
    }
}
