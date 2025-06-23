package br.ufscar.glitchdex.util;

import br.ufscar.glitchdex.dto.UserDTO;
import br.ufscar.glitchdex.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private static final Logger log = LoggerFactory.getLogger(SecurityUtils.class);

    private final UserService userService;

    private static SecurityUtils instance;

    @PostConstruct
    public void init() {
        instance = this;
    }

    public static UserDTO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("No authenticated user found in security context or user is anonymous.");
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            String userEmail = userDetails.getUsername(); // O username no seu caso é o email

            if (instance != null && instance.userService != null) {
                return instance.userService.findByEmail(userEmail)
                        .map(user -> new UserDTO(user.getId(), user.getName(), user.getEmail(), user.getRole())) // Passando todos os 4 argumentos
                        .orElseGet(() -> {
                            log.warn("User details not found in database for email: {}", userEmail);
                            return new UserDTO(null, userEmail, userEmail, null); // Construtor com 4 argumentos, role nulo
                        });
            } else {
                log.error("SecurityUtils instance or UserService is null. Cannot fetch user details.");
                return new UserDTO(null, userEmail, "Unknown User", null); // Construtor com 4 argumentos, role nulo
            }

        } else if (principal instanceof String) {
            log.warn("Principal is a String (possibly 'anonymousUser'): {}", principal);
            return new UserDTO(null, (String) principal, (String) principal, null); // Construtor com 4 argumentos, role nulo
        }
        log.warn("Unknown principal type: {}", principal.getClass().getName());
        return null;
    }
}