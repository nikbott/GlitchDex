package br.ufscar.glitchdex.config;

import br.ufscar.glitchdex.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;

    @Value("${glitchdex.admin.email}")
    private String adminEmail;

    @Value("${glitchdex.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        userService.createAdminIfNotExists(adminEmail, adminPassword);
    }
}