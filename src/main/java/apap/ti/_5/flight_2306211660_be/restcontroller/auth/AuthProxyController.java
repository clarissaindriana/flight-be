package apap.ti._5.flight_2306211660_be.restcontroller.auth;

import apap.ti._5.flight_2306211660_be.config.security.ProfileClient;
import apap.ti._5.flight_2306211660_be.restdto.response.BaseResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthProxyController {

    @Autowired
    private ProfileClient profileClient;

    @PostMapping("/login")
    public ResponseEntity<BaseResponseDTO<ProfileClient.LoginResponse>> login(@RequestBody Map<String, String> loginData, HttpServletResponse response) {
        var base = new BaseResponseDTO<ProfileClient.LoginResponse>();
        try {
            // Convert email to username for profile service
            String username = loginData.get("email");
            String password = loginData.get("password");
            
            if (username == null || password == null) {
                base.setStatus(HttpStatus.BAD_REQUEST.value());
                base.setMessage("Email and password are required");
                base.setTimestamp(new Date());
                return new ResponseEntity<>(base, HttpStatus.BAD_REQUEST);
            }
            
            // Create LoginRequest for profile service
            ProfileClient.LoginRequest req = new ProfileClient.LoginRequest();
            req.setEmail(username);  // username variable contains the email value
            req.setPassword(password);
            
            ProfileClient.LoginResponse lr = profileClient.login(req);
            if (lr == null || lr.getToken() == null) {
                base.setStatus(HttpStatus.UNAUTHORIZED.value());
                base.setMessage("Invalid credentials or profile service error");
                base.setTimestamp(new Date());
                return new ResponseEntity<>(base, HttpStatus.UNAUTHORIZED);
            }

            // set cookie (HttpOnly) for browser sessions
            String cookie = String.format("JWT_TOKEN=%s; HttpOnly; Path=/; SameSite=None; Secure", lr.getToken());
            response.addHeader("Set-Cookie", cookie);

            base.setStatus(HttpStatus.OK.value());
            base.setMessage("User logged in successfully");
            base.setData(lr);
            base.setTimestamp(new Date());
            return ResponseEntity.ok(base);
        } catch (Exception ex) {
            base.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            base.setMessage("Error: " + ex.getMessage());
            base.setTimestamp(new Date());
            return new ResponseEntity<>(base, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<BaseResponseDTO<Object>> register(@RequestBody Object registerPayload) {
        var base = new BaseResponseDTO<Object>();
        try {
            Object res = profileClient.register(registerPayload);

            base.setStatus(HttpStatus.OK.value());
            base.setMessage("Register proxied");
            base.setData(res);
            base.setTimestamp(new Date());
            return ResponseEntity.ok(base);
        } catch (Exception ex) {
            base.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            base.setMessage("Error: " + ex.getMessage());
            base.setTimestamp(new Date());
            return new ResponseEntity<>(base, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseResponseDTO<Object>> logout(HttpServletResponse response) {
        var base = new BaseResponseDTO<Object>();
        // clear cookie
        String cookie = "JWT_TOKEN=; HttpOnly; Path=/; Max-Age=0; SameSite=None; Secure";
        response.addHeader("Set-Cookie", cookie);
        base.setStatus(HttpStatus.OK.value());
        base.setMessage("Logged out (cookie cleared)");
        base.setTimestamp(new Date());
        return ResponseEntity.ok(base);
    }
}
