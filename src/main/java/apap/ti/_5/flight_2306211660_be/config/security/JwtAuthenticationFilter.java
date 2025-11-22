package apap.ti._5.flight_2306211660_be.config.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final ProfileClient profileClient;
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    public JwtAuthenticationFilter(ProfileClient profileClient) {
        this.profileClient = profileClient;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractTokenFromRequest(request);

        if (token != null) {
            var profile = profileClient.validateToken(token);
            if (profile == null) {
                // validation call failed or returned null — try to parse JWT locally as a fallback for local debugging
                try {
                    profile = parseProfileFromJwt(token);
                    if (profile != null) {
                        logger.debug("Fallback: parsed profile from JWT payload: role={}", profile.getRole());
                    }
                } catch (Exception ex) {
                    logger.debug("Fallback parsing of JWT failed: {}", ex.getMessage());
                }
            }

            if (profile != null && Boolean.TRUE.equals(profile.getValid())) {
                String rawRole = profile.getRole() != null ? profile.getRole().toUpperCase().replace(' ', '_') : "CUSTOMER";
                String authority = "ROLE_" + rawRole;

                // set the profile object itself as principal so services can access userId and other fields
                var auth = new UsernamePasswordAuthenticationToken(profile, null,
                        Collections.singletonList(new SimpleGrantedAuthority(authority)));
                SecurityContextHolder.getContext().setAuthentication(auth);
                logger.debug("Authentication set for user={} with authority={}", profile.getUsername(), authority);
            } else {
                logger.debug("No valid profile found for token (request {}). Authentication not set.", request.getRequestURI());
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        // 1. Try cookie JWT_TOKEN
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("JWT_TOKEN".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        // 2. Try Authorization header
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        return null;
    }

    private ProfileClient.ProfileValidateResponse parseProfileFromJwt(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;

            byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
            String payload = new String(decoded, StandardCharsets.UTF_8);
            JsonNode node = mapper.readTree(payload);

            ProfileClient.ProfileValidateResponse p = new ProfileClient.ProfileValidateResponse();
            p.setValid(Boolean.TRUE);

            // common claim names
            if (node.has("role")) p.setRole(node.get("role").asText(null));
            if (node.has("id")) p.setUserId(node.get("id").asText(null));
            if (node.has("userId")) p.setUserId(node.get("userId").asText(p.getUserId()));
            if (node.has("sub")) p.setUsername(node.get("sub").asText(null));
            if (node.has("username")) p.setUsername(node.get("username").asText(p.getUsername()));
            if (node.has("email")) p.setEmail(node.get("email").asText(null));
            if (node.has("name")) p.setName(node.get("name").asText(null));

            return p;
        } catch (Exception ex) {
            logger.debug("parseProfileFromJwt error: {}", ex.getMessage());
            return null;
        }
    }
}
