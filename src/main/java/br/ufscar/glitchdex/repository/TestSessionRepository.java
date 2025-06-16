package br.ufscar.glitchdex.repository;

import br.ufscar.glitchdex.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TestSessionRepository extends JpaRepository<TestSession, Long> {

    /**
     * Find all test sessions for a specific tester
     *
     * @param tester the tester to search for
     * @return a list of test sessions conducted by the tester
     */
    List<TestSession> findByTester(User tester);

    /**
     * Find all test sessions for a specific project
     *
     * @param project the project to search for
     * @return a list of test sessions for the project
     */
    List<TestSession> findByProject(Project project);

    /**
     * Find all test sessions using a specific strategy
     *
     * @param strategy the strategy to search for
     * @return a list of test sessions using the strategy
     */
    List<TestSession> findByStrategy(Strategy strategy);

    /**
     * Find all test sessions with a specific status
     *
     * @param status the status to search for
     * @return a list of test sessions with the status
     */
    List<TestSession> findByStatus(SessionStatus status);

    /**
     * Find all test sessions created after a specific date
     *
     * @param date the date to search from
     * @return a list of test sessions created after the date
     */
    List<TestSession> findByCreationTimestampAfter(LocalDateTime date);

    /**
     * Find all test sessions for a specific project with a specific status
     *
     * @param project the project to search for
     * @param status  the status to search for
     * @return a list of test sessions for the project with the status
     */
    List<TestSession> findByProjectAndStatus(Project project, SessionStatus status);

    Optional<TestSession> findByIdAndTester(Long id, User user);
}
