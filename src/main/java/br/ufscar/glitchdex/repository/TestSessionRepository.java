package br.ufscar.glitchdex.repository;

import br.ufscar.glitchdex.domain.*;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the TestSession entity.
 * Provides methods for standard CRUD operations and custom queries on TestSession entities.
 */
@Repository
public interface TestSessionRepository extends JpaRepository<TestSession, Long> {
    /**
     * Overrides the default findById to eagerly fetch related entities.
     *
     * @param id the test session ID
     * @return an Optional containing the test session with its related entities if found
     */
    @Override
    @EntityGraph(attributePaths = {"bugs", "tester", "project", "strategy"})
    Optional<TestSession> findById(Long id);

    /**
     * Find all test sessions for a specific tester.
     *
     * @param tester the tester to search for
     * @return a list of test sessions conducted by the tester
     */
    @EntityGraph(attributePaths = {"bugs", "tester", "project", "strategy"})
    List<TestSession> findByTester(User tester);

    /**
     * Find all test sessions for a specific project.
     *
     * @param project the project to search for
     * @return a list of test sessions for the project
     */
    @EntityGraph(attributePaths = {"bugs", "tester", "project", "strategy"})
    List<TestSession> findByProject(Project project);

    /**
     * Find all test sessions using a specific strategy.
     *
     * @param strategy the strategy to search for
     * @return a list of test sessions using the strategy
     */
    @EntityGraph(attributePaths = {"bugs", "tester", "project", "strategy"})
    List<TestSession> findByStrategy(Strategy strategy);

    /**
     * Find all test sessions with a specific status.
     *
     * @param status the status to search for
     * @return a list of test sessions with the status
     */
    @EntityGraph(attributePaths = {"bugs", "tester", "project", "strategy"})
    List<TestSession> findByStatus(SessionStatus status);

    /**
     * Find all test sessions created after a specific date.
     *
     * @param date the date to search from
     * @return a list of test sessions created after the date
     */
    @EntityGraph(attributePaths = {"bugs", "tester", "project", "strategy"})
    List<TestSession> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Find all test sessions for a specific project with a specific status.
     *
     * @param project the project to search for
     * @param status  the status to search for
     * @return a list of test sessions for the project with the status
     */
    @EntityGraph(attributePaths = {"bugs", "tester", "project", "strategy"})
    List<TestSession> findByProjectAndStatus(Project project, SessionStatus status);

    /**
     * Finds a test session by its ID and the user who is the tester.
     *
     * @param id   The ID of the test session.
     * @param user The user (tester).
     * @return An Optional containing the TestSession if found.
     */
    @EntityGraph(attributePaths = {"bugs", "tester", "project", "strategy"})
    Optional<TestSession> findByIdAndTester(Long id, User user);
}