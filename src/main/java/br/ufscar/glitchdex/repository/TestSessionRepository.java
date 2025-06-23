package br.ufscar.glitchdex.repository;

import br.ufscar.glitchdex.domain.*;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TestSessionRepository extends JpaRepository<TestSession, Long> {

    @Override
    @EntityGraph(attributePaths = {"bugs.attachments", "bugs.reporter", "tester", "project", "strategy"})
    Optional<TestSession> findById(Long id);

    @EntityGraph(attributePaths = {"bugs.attachments", "tester", "project", "strategy"})
    List<TestSession> findByTester(User tester);

    @EntityGraph(attributePaths = {"bugs.attachments", "tester", "project", "strategy"})
    List<TestSession> findByProject(Project project);

    @EntityGraph(attributePaths = {"bugs.attachments", "tester", "project", "strategy"})
    List<TestSession> findByStrategy(Strategy strategy);

    @EntityGraph(attributePaths = {"bugs.attachments", "tester", "project", "strategy"})
    List<TestSession> findByStatus(SessionStatus status);

    @EntityGraph(attributePaths = {"bugs.attachments", "tester", "project", "strategy"})
    List<TestSession> findByCreatedAtAfter(LocalDateTime date);

    @EntityGraph(attributePaths = {"bugs.attachments", "tester", "project", "strategy"})
    List<TestSession> findByProjectIdAndStatus(Long project, SessionStatus status);

    @EntityGraph(attributePaths = {"bugs.attachments", "tester", "project", "strategy"})
    Optional<TestSession> findByIdAndTester(Long id, User user);
}