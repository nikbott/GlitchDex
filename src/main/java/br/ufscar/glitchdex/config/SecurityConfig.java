package br.ufscar.glitchdex.config;

import br.ufscar.glitchdex.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;


/**
 * Configures the security settings for the application, including authentication, authorization,
 * login, and logout behavior.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
    private final UserService userService;

    /**
     * Configures the security filter chain that defines which paths are secured and how.
     *
     * @param http The HttpSecurity object to configure.
     * @return The configured SecurityFilterChain.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // Publicly accessible paths
                        .requestMatchers(
                                "/css/**", "/js/**", "/images/**", "/webjars/**",
                                "/login", "/error", "/", "/home", "/strategies", "/files/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/strategies").permitAll()
                        // All other requests must be authenticated
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler((request, response, authentication) -> {
                            log.info("User {} logged in successfully", authentication.getName());
                            response.sendRedirect("/home");
                        })
                        .failureHandler((request, response, exception) -> {
                            log.warn("Failed login attempt for user {}", request.getParameter("username"));
                            response.sendRedirect("/login?error");
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessHandler((request, response, authentication) -> {
                            if (null != authentication) {
                                log.info("User {} logged out successfully", authentication.getName());
                            }
                            response.sendRedirect("/home");
                        })
                        .permitAll()
                );

        return http.build();
    }

    /**
     * Provides a custom UserDetailsService bean that loads user-specific data.
     * It uses the UserService to find a user by their email address.
     *
     * @return The UserDetailsService implementation.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            log.debug("Attempting to load user by username: {}", username);
            return userService.findByEmail(username)
                    .map(user -> {
                        log.debug("User found: {}", user.getEmail());
                        return org.springframework.security.core.userdetails.User
                                .withUsername(user.getEmail())
                                .password(user.getPassword())
                                .authorities(user.getRole().name())
                                .build();
                    })
                    .orElseThrow(() -> {
                        log.warn("User not found with username: {}", username);
                        return new UsernameNotFoundException("User not found: " + username);
                    });
        };
    }
}