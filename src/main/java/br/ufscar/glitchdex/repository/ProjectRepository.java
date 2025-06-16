package br.ufscar.glitchdex.repository;

import br.ufscar.glitchdex.domain.Project;
import br.ufscar.glitchdex.domain.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * Find all projects where the given user is a member
     *
     * @param user the user to search for
     * @return a list of projects where the user is a member
     */
    List<Project> findByMembersContaining(User user, Sort sort);

    /**
     * Find projects by name containing the given string (case-insensitive)
     *
     * @param name the name to search for
     * @return a list of projects with names containing the search string
     */
    List<Project> findByNameContainingIgnoreCase(String name);
}
