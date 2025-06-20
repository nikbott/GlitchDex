package br.ufscar.glitchdex.repository;

import br.ufscar.glitchdex.domain.Strategy;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the Strategy entity.
 * Provides methods for standard CRUD operations and custom queries on Strategy entities.
 */
@Repository
public interface StrategyRepository extends JpaRepository<Strategy, Long> {
    /**
     * Overrides the default findById to eagerly fetch the testSessions association.
     *
     * @param id the strategy ID
     * @return an Optional containing the strategy with its test sessions if found
     */
    @Override
    @EntityGraph(attributePaths = "testSessions")
    Optional<Strategy> findById(Long id);

    /**
     * Finds a strategy by its name.
     *
     * @param name the name to search for
     * @return an Optional containing the strategy if found
     */
    Optional<Strategy> findByName(String name);

    /**
     * Finds strategies by name containing the given string (case-insensitive).
     *
     * @param name the name to search for
     * @return a list of strategies with names containing the search string
     */
    List<Strategy> findByNameContainingIgnoreCase(String name);

    /**
     * Finds strategies by description containing the given string (case-insensitive).
     *
     * @param description the description to search for
     * @return a list of strategies with descriptions containing the search string
     */
    List<Strategy> findByDescriptionContainingIgnoreCase(String description);

}