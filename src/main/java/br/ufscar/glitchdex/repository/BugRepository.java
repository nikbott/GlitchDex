package br.ufscar.glitchdex.repository;

import br.ufscar.glitchdex.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the Bug entity.
 * Provides methods for standard CRUD operations and custom queries on Bug entities.
 */
@Repository
public interface BugRepository extends JpaRepository<Bug, Long> {

    /**
     * Finds a bug by its title.
     *
     * @param title The title of the bug.
     * @return an Optional containing the bug if found.
     */
    Optional<Bug> findByTitle(String title);

    /**
     * Finds bugs whose titles contain the given string, ignoring case.
     *
     * @param title The string to search for in bug titles.
     * @return a list of matching bugs.
     */
    List<Bug> findByTitleContainingIgnoreCase(String title);

    /**
     * Finds bugs by their status.
     *
     * @param status The status of the bugs to find.
     * @return a list of bugs with the specified status.
     */
    List<Bug> findByStatus(BugStatus status);

    /**
     * Finds bugs by their priority.
     *
     * @param priority The priority of the bugs to find.
     * @return a list of bugs with the specified priority.
     */
    List<Bug> findByPriority(BugPriority priority);

    /**
     * Finds bugs by their severity.
     *
     * @param severity The severity of the bugs to find.
     * @return a list of bugs with the specified severity.
     */
    List<Bug> findBySeverity(BugSeverity severity);

    /**
     * Finds bugs that were reported after a specific date.
     *
     * @param date The date to compare against.
     * @return a list of bugs reported after the specified date.
     */
    List<Bug> findByReportDateAfter(LocalDateTime date);

    /**
     * Finds all bugs associated with a specific test session.
     *
     * @param testSession The test session to find bugs for.
     * @return a list of bugs for the given test session.
     */
    List<Bug> findByTestSession(TestSession testSession);
}