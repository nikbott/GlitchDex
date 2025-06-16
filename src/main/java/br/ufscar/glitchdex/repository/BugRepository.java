package br.ufscar.glitchdex.repository;

import br.ufscar.glitchdex.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BugRepository extends JpaRepository<Bug, Long> {

    Optional<Bug> findByTitle(String title);

    List<Bug> findByTitleContainingIgnoreCase(String title);

    List<Bug> findByStatus(BugStatus status);

    List<Bug> findByPriority(BugPriority priority);

    List<Bug> findBySeverity(BugSeverity severity);

    List<Bug> findByReportDateAfter(LocalDateTime date);

    List<Bug> findByTestSession(TestSession testSession);
}