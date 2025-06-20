package br.ufscar.glitchdex.repository;

import br.ufscar.glitchdex.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("User Repository Tests")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should not find user by email when user does not exist")
    void whenFindByEmail_andUserDoesNotExist_thenReturnEmpty() {
        // when
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // then
        assertThat(found).isNotPresent();
    }

    @Test
    @DisplayName("Should return false when user does not exist by email")
    void whenExistsByEmail_andUserDoesNotExist_thenReturnFalse() {
        // when
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // then
        assertThat(exists).isFalse();
    }
}