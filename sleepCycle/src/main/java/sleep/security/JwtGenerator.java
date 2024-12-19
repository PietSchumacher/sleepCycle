package sleep.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * This class generates, validates, and parses JWT (JSON Web Token) tokens.
 * It provides methods for creating a token, extracting the username from a token, and validating the token's authenticity.
 */
@Component
public class JwtGenerator {
    // Secret key used for signing the JWT. In this case, HMAC SHA-512 algorithm.
    private SecretKey key = Jwts.SIG.HS512.key().build();

    /**
     * Generates a JWT token for the authenticated user.
     *
     * @param auth The authentication object containing user details (like username).
     * @return A JWT token as a String.
     */
    public String generateToken(Authentication auth) {
        String username = auth.getName();
        Date now = new Date();

        Date expiryDate = new Date(now.getTime() + SecurityConstants.JWT_EXPIRES_IN);

        String token = Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(expiryDate)
                .signWith(key)
                .compact();

        return token;
    }

    /**
     * Extracts the username (subject) from the JWT token.
     *
     * @param token The JWT token as a String.
     * @return The username contained in the token.
     */
    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    /**
     * Validates the JWT token by checking its authenticity and expiration.
     *
     * @param token The JWT token to validate.
     * @return true if the token is valid, false otherwise.
     * @throws AuthenticationCredentialsNotFoundException if the token is invalid or expired.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            throw new AuthenticationCredentialsNotFoundException("JWT ist abgelaufen oder falsch");
        }
    }

}
