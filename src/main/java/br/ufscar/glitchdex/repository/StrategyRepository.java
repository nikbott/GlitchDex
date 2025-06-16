package br.ufscar.glitchdex.repository;

import br.ufscar.glitchdex.domain.Project;
import br.ufscar.glitchdex.domain.Strategy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StrategyRepository extends JpaRepository<Strategy, Long> {

    Optional<Strategy> findByName(String name);

    List<Strategy> findByNameContainingIgnoreCase(String name);

    List<Strategy> findByDescriptionContainingIgnoreCase(String description);

    List<Strategy> findByProject(Project project);
}