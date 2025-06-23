package br.ufscar.glitchdex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Application Context Tests")
class GlitchDexApplicationTests {

    @Test
    @DisplayName("Should load the application context")
    void contextLoads() {
        // Empty test that verifies the application context loads
    }

}