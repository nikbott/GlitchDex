package br.ufscar.glitchdex.service;

import br.ufscar.glitchdex.domain.Project;
import br.ufscar.glitchdex.domain.TestSession;
import br.ufscar.glitchdex.domain.User;
import br.ufscar.glitchdex.dto.ProjectDTO;
import br.ufscar.glitchdex.dto.ProjectRequest;
import br.ufscar.glitchdex.dto.UserDTO;
import br.ufscar.glitchdex.exception.ResourceNotFoundException;
import br.ufscar.glitchdex.mapper.ProjectMapper;
import br.ufscar.glitchdex.repository.ProjectRepository;
import br.ufscar.glitchdex.repository.TestSessionRepository;
import br.ufscar.glitchdex.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service class for managing projects.
 * Provides business logic for creating, retrieving, updating, and deleting projects,
 * as well as managing project members.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;
    private final UserService userService;
    private final MessageSource messageSource;
    private final TestSessionRepository testSessionRepository;

    /**
     * Retrieves a project by its ID.
     *
     * @param projectId The ID of the project.
     * @return The Project object.
     * @throws ResourceNotFoundException if no project is found with the given ID.
     */
    private Project getProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("error.project.not_found", new Object[]{projectId}, LocaleContextHolder.getLocale())));
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param userId The ID of the user.
     * @return The User object.
     * @throws ResourceNotFoundException if no user is found with the given ID.
     */
    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("error.user.not_found", new Object[]{userId}, LocaleContextHolder.getLocale())));
    }

    /**
     * Finds a project by its ID and returns its DTO.
     *
     * @param id The ID of the project.
     * @return The ProjectDTO.
     */
    public ProjectDTO findById(Long id) {
        log.info("Finding project with id: {}", id);
        Project project = getProjectById(id);
        return projectMapper.toProjectDTO(project);
    }

    /**
     * Finds all projects and returns their DTOs.
     *
     * @return A list of all ProjectDTOs.
     */
    public List<ProjectDTO> findAll() {
        log.info("Finding all projects");
        return projectMapper.toProjectDTOs(projectRepository.findAll());
    }

    /**
     * Finds all projects where a given user is a member.
     *
     * @param userDto The DTO of the user.
     * @param sort    The field to sort by.
     * @param order   The sort order.
     * @return A list of ProjectDTOs.
     */
    public List<ProjectDTO> findByMember(UserDTO userDto, String sort, String order) {
        log.info("Finding projects for member {} with sort {} and order {}", userDto.getEmail(), sort, order);
        User user = getUserById(userDto.getId());
        Sort.Direction direction = "desc".equalsIgnoreCase(order) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sorting = Sort.by(direction, sort);
        return projectMapper.toProjectDTOs(projectRepository.findByMembersContaining(user, sorting));
    }

    /**
     * Searches for projects by name.
     *
     * @param name The name to search for.
     * @return A list of ProjectDTOs matching the search query.
     */
    public List<ProjectDTO> searchByName(String name) {
        log.info("Searching for projects with name containing: {}", name);
        return projectMapper.toProjectDTOs(projectRepository.findByNameContainingIgnoreCase(name));
    }

    /**
     * Creates a new project.
     *
     * @param projectRequest The request object containing the project details.
     * @return The created ProjectDTO.
     */
    @Transactional
    public ProjectDTO create(@Valid ProjectRequest projectRequest) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User creatorUser = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("error.user.not_found_context", null, LocaleContextHolder.getLocale())));
        log.info("User {} is creating a new project with name: {}", creatorUser.getEmail(), projectRequest.getName());
        Project project = projectMapper.toProject(projectRequest);

        List<User> members = new ArrayList<>(userRepository.findAllById(projectRequest.getMemberIds()));

        if (members.stream().noneMatch(member -> member.getId().equals(creatorUser.getId()))) {
            members.add(creatorUser);
        }

        project.setMembers(members);
        Project savedProject = projectRepository.save(project);
        log.info("Project '{}' created successfully with id {}", savedProject.getName(), savedProject.getId());
        return projectMapper.toProjectDTO(savedProject);
    }

    /**
     * Updates an existing project.
     *
     * @param id             The ID of the project to update.
     * @param projectRequest The request object with the updated project details.
     * @return The updated ProjectDTO.
     */
    @Transactional
    public ProjectDTO update(Long id, @Valid ProjectRequest projectRequest) {
        log.info("Updating project with id: {}", id);
        Project existingProject = getProjectById(id);

        existingProject.setName(projectRequest.getName());
        existingProject.setDescription(projectRequest.getDescription());

        if (projectRequest.getMemberIds() != null) {
            if (projectRequest.getMemberIds().isEmpty()) {
                throw new IllegalStateException(messageSource.getMessage("error.project.at_least_one_member", null, LocaleContextHolder.getLocale()));
            }
            List<User> members = userRepository.findAllById(projectRequest.getMemberIds());
            existingProject.setMembers(members);
        }

        Project updatedProject = projectRepository.save(existingProject);
        log.info("Project with id {} updated successfully", id);
        return projectMapper.toProjectDTO(updatedProject);
    }

    /**
     * Adds a member to a project.
     *
     * @param projectId The ID of the project.
     * @param userId    The ID of the user to add.
     * @return The updated ProjectDTO.
     */
    @Transactional
    public ProjectDTO addMember(Long projectId, Long userId) {
        log.info("Adding member {} to project {}", userId, projectId);
        Project project = getProjectById(projectId);
        User user = getUserById(userId);

        if (!project.getMembers().contains(user)) {
            project.getMembers().add(user);
            projectRepository.save(project);
            log.info("Member {} added to project {} successfully", userId, projectId);
        } else {
            log.warn("User {} is already a member of project {}", userId, projectId);
        }

        return projectMapper.toProjectDTO(project);
    }

    /**
     * Removes a member from a project.
     *
     * @param projectId The ID of the project.
     * @param userId    The ID of the user to remove.
     * @return The updated ProjectDTO.
     */
    @Transactional
    public ProjectDTO removeMember(Long projectId, Long userId) {
        log.info("Removing member {} from project {}", userId, projectId);
        Project project = getProjectById(projectId);
        User user = getUserById(userId);

        if (project.getMembers().size() <= 1) {
            log.error("Cannot remove the last member from a project {}", projectId);
            throw new IllegalStateException(messageSource.getMessage("error.project.remove_last_member", null, LocaleContextHolder.getLocale()));
        }

        project.getMembers().remove(user);
        Project updatedProject = projectRepository.save(project);
        log.info("Member {} removed from project {} successfully", userId, projectId);
        return projectMapper.toProjectDTO(updatedProject);
    }

    /**
     * Deletes a project by its ID, along with all associated test sessions.
     *
     * @param id The ID of the project to delete.
     */
    @Transactional
    public void delete(Long id) {
        log.info("Deleting project with id: {}", id);
        Project project = getProjectById(id);

        // Find and delete all associated test sessions
        List<TestSession> sessions = testSessionRepository.findByProject(project);
        testSessionRepository.deleteAll(sessions);
        log.info("Deleted {} associated test sessions for project with id {}", sessions.size(), id);

        projectRepository.deleteById(id);
        log.info("Project with id {} deleted successfully", id);
    }

    public void verifyUserAssociation(Long userId, Long projectId) {
        User user = getUserById(userId);  // busca o User da base
        boolean isAssociated = projectRepository.existsByIdAndMembersContaining(projectId, user);
        if (!isAssociated) {
            throw new AccessDeniedException(messageSource.getMessage("error.project.access_denied", null, LocaleContextHolder.getLocale()));
        }
    }
}