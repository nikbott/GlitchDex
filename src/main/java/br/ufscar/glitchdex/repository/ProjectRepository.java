package br.ufscar.glitchdex.repository;

import br.ufscar.glitchdex.domain.Project;
import br.ufscar.glitchdex.domain.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the Project entity.
 * Provides methods for standard CRUD operations and custom queries on Project entities.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    /**
     * Overrides the default findById to eagerly fetch the members association.
     * This is crucial for operations that need project details along with member info,
     * preventing lazy initialization exceptions and N+1 queries.
     * The `attributePaths` defines which fields to fetch.
     *
     * @param id the project ID
     * @return an Optional containing the project with its members if found
     */
    @Override
    @EntityGraph(attributePaths = "members")
    Optional<Project> findById(Long id);

    /**
     * Overrides the default findAll to eagerly fetch the members association.
     * This is crucial for operations that need project details along with member info,
     * preventing lazy initialization exceptions and N+1 queries.
     * The `attributePaths` defines which fields to fetch.
     */
    @Override
    @EntityGraph(attributePaths = "members")
    List<Project> findAll();

    /**
     * Find all projects where the given user is a member.
     * The @EntityGraph annotation ensures that the 'members' collection is fetched
     * in a single query (JOIN FETCH) to avoid the N+1 problem.
     * <p>
     * NOTE: For very large datasets, this query could still be slow.
     * Consider a more optimized query or a different data fetching strategy if needed.
     *
     * @param user the user to search for
     * @param sort the sorting criteria
     * @return a list of projects where the user is a member
     */
    @EntityGraph("Project.withMembers")
    List<Project> findByMembersContaining(User user, Sort sort);

    /**
     * Find projects by name containing the given string (case-insensitive)
     *
     * @param name the name to search for
     * @return a list of projects with names containing the search string
     */
    List<Project> findByNameContainingIgnoreCase(String name);

    boolean existsByIdAndMembersContaining(Long projectId, User user);
}