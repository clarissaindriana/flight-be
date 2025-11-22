package apap.ti._5.flight_2306211660_be.config.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import apap.ti._5.flight_2306211660_be.config.security.ProfileClient.ProfileValidateResponse;

public final class CurrentUser {

    private CurrentUser() {}

    public static String getUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof ProfileValidateResponse p) {
            return p.getUsername();
        }
        return principal != null ? principal.toString() : null;
    }

    public static String getRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) return null;
        return auth.getAuthorities().stream().findFirst().map(a -> a.getAuthority()).orElse(null);
    }

    public static String getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof ProfileValidateResponse p) {
            return p.getUserId();
        }
        return null;
    }

    public static String getEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof ProfileValidateResponse p) {
            return p.getEmail();
        }
        return null;
    }

    public static String getName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof ProfileValidateResponse p) {
            return p.getName();
        }
        return null;
    }
}
