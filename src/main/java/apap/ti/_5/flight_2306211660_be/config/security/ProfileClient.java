package apap.ti._5.flight_2306211660_be.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Component
public class ProfileClient {

    private final WebClient webClient;

    public ProfileClient(@Value("${profile.service.base-url:https://2306219575-be.hafizmuh.site}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Validate token by calling external profile microservice /api/auth/validate-token.
     * If token is null or empty, returns null.
     */
    public ProfileValidateResponse validateToken(String token) {
        if (token == null || token.isBlank()) return null;

        try {
            // The profile service accepts cookie-based or Authorization header. We'll call with header.
            Mono<ProfileValidateResponse> mono = webClient.post()
                    .uri("/api/auth/validate-token")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .bodyToMono(ProfileValidateWrapper.class)
                    .map(ProfileValidateWrapper::getData)
                    .onErrorReturn(null);

            return mono.block();
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Login via profile microservice and return full login data (including token).
     */
    public LoginResponse login(LoginRequest req) {
        try {
            Mono<LoginWrapper> mono = webClient.post()
                    .uri("/api/auth/login")
                    .bodyValue(req)
                    .retrieve()
                    .bodyToMono(LoginWrapper.class)
                    .onErrorReturn(null);

            LoginWrapper wrapper = mono.block();
            if (wrapper == null) return null;
            return wrapper.getData();
        } catch (Exception ex) {
            return null;
        }
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class LoginWrapper {
        private Integer status;
        private String message;
        private LoginResponse data;

        public LoginResponse getData() { return data; }
        public void setData(LoginResponse data) { this.data = data; }
    }

    public static class LoginResponse {
        private String token;
        private String type;
        private String id;
        private String username;
        private String email;
        private String name;
        private String role;

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    // wrapper matching the profile microservice response
    public static class ProfileValidateWrapper {
        private Integer status;
        private String message;
        private ProfileValidateResponse data;

        public ProfileValidateResponse getData() { return data; }
        public void setData(ProfileValidateResponse data) { this.data = data; }
    }

    public static class ProfileValidateResponse {
        private Boolean valid;
        private String userId;
        private String username;
        private String email;
        private String name;
        private String role;

        public Boolean getValid() { return valid; }
        public void setValid(Boolean valid) { this.valid = valid; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    /**
     * Proxy register call. Accepts any payload and returns the response body as Object.
     */
    public Object register(Object payload) {
        try {
            Mono<Object> mono = webClient.post()
                    .uri("/api/auth/register")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Object.class)
                    .onErrorReturn(null);

            return mono.block();
        } catch (Exception ex) {
            return null;
        }
    }

    // Fetch user by id: GET /api/users/{id}
    public ProfileUserWrapper getUserById(String id) {
        try {
            Mono<ProfileUserWrapper> mono = webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/users/{id}").build(id))
                    .retrieve()
                    .bodyToMono(ProfileUserWrapper.class)
                    .onErrorReturn(null);

            return mono.block();
        } catch (Exception ex) {
            return null;
        }
    }

    public static class ProfileUserWrapper {
        private Integer status;
        private String message;
        private ProfileUser data;

        public ProfileUser getData() { return data; }
        public void setData(ProfileUser data) { this.data = data; }
    }

    public static class ProfileUsersWrapper {
        private Integer status;
        private String message;
        private java.util.List<ProfileUser> data;

        public java.util.List<ProfileUser> getData() { return data; }
        public void setData(java.util.List<ProfileUser> data) { this.data = data; }
    }

    // Fetch all customers: GET /api/users/customers
    public ProfileUsersWrapper getCustomers() {
        try {
            Mono<ProfileUsersWrapper> mono = webClient.get()
                    .uri("/api/users/customers")
                    .retrieve()
                    .bodyToMono(ProfileUsersWrapper.class)
                    .onErrorReturn(null);

            return mono.block();
        } catch (Exception ex) {
            return null;
        }
    }

    // Fetch all users: GET /api/users
    public ProfileUsersWrapper getAllUsers() {
        try {
            Mono<ProfileUsersWrapper> mono = webClient.get()
                    .uri("/api/users")
                    .retrieve()
                    .bodyToMono(ProfileUsersWrapper.class)
                    .onErrorReturn(null);

            return mono.block();
        } catch (Exception ex) {
            return null;
        }
    }

    // Update user profile: PUT /api/users/{id}
    public ProfileUserWrapper updateUser(String id, Object payload) {
        try {
            Mono<ProfileUserWrapper> mono = webClient.put()
                    .uri(uriBuilder -> uriBuilder.path("/api/users/{id}").build(id))
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(ProfileUserWrapper.class)
                    .onErrorReturn(null);

            return mono.block();
        } catch (Exception ex) {
            return null;
        }
    }

    public static class ProfileUser {
        private String id;
        private String username;
        private String email;
        private String name;
        private String role;
        private java.math.BigDecimal balance;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public java.math.BigDecimal getBalance() { return balance; }
        public void setBalance(java.math.BigDecimal balance) { this.balance = balance; }
    }
}
