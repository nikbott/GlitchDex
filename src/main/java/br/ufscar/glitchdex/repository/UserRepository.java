package br.ufscar.glitchdex.repository;

import br.ufscar.glitchdex.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the User entity.
 * Provides methods for standard CRUD operations and custom queries on User entities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Overrides the default findById to eagerly fetch the testSessions association.
     *
     * @param id the user ID
     * @return an Optional containing the user with its test sessions if found
     */
    @Override
    @EntityGraph(attributePaths = "testSessions")
    Optional<User> findById(Long id);

    /**
     * Overrides the default findAll to eagerly fetch the testSessions association.
     *
     * @return a list of all users with their test sessions
     */
    @Override
    @EntityGraph(attributePaths = "testSessions")
    List<User> findAll();

    /**
     * Find a user by email.
     *
     * @param email the email to search for
     * @return an Optional containing the user if found
     */
    @EntityGraph(attributePaths = "testSessions")
    Optional<User> findByEmail(String email);

    /**
     * Check if a user with the given email exists.
     *
     * @param email the email to check
     * @return true if a user with the email exists, false otherwise
     */
    boolean existsByEmail(String email);
}