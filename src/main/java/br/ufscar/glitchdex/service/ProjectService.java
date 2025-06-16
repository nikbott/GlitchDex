package br.ufscar.glitchdex.service;

import br.ufscar.glitchdex.domain.Project;
import br.ufscar.glitchdex.domain.User;
import br.ufscar.glitchdex.dto.ProjectDTO;
import br.ufscar.glitchdex.dto.ProjectRequest;
import br.ufscar.glitchdex.exception.ResourceNotFoundException;
import br.ufscar.glitchdex.mapper.ProjectMapper;
import br.ufscar.glitchdex.repository.ProjectRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    public ProjectDTO findById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        return projectMapper.toProjectDTO(project);
    }

    public List<ProjectDTO> findAll() {
        return projectMapper.toProjectDTOs(projectRepository.findAll());
    }

    public List<ProjectDTO> findByMember(User user, String sort, String order) {
        Sort.Direction direction = "desc".equalsIgnoreCase(order) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sorting = Sort.by(direction, sort);
        return projectMapper.toProjectDTOs(projectRepository.findByMembersContaining(user, sorting));
    }

    public List<ProjectDTO> searchByName(String name) {
        return projectMapper.toProjectDTOs(projectRepository.findByNameContainingIgnoreCase(name));
    }

    @Transactional
    public ProjectDTO create(ProjectRequest projectRequest, User creator) {
        Project project = projectMapper.toProject(projectRequest);
        if (!project.getMembers().contains(creator)) {
            project.getMembers().add(creator);
        }
        Project savedProject = projectRepository.save(project);
        return projectMapper.toProjectDTO(savedProject);
    }

    @Transactional
    public ProjectDTO update(Long id, ProjectRequest projectRequest) {
        Project existingProject = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        existingProject.setName(projectRequest.getName());
        existingProject.setDescription(projectRequest.getDescription());

        Project updatedProject = projectRepository.save(existingProject);
        return projectMapper.toProjectDTO(updatedProject);
    }

    @Transactional
    public ProjectDTO addMember(Long projectId, User user) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        if (!project.getMembers().contains(user)) {
            project.getMembers().add(user);
            projectRepository.save(project);
        }

        return projectMapper.toProjectDTO(project);
    }

    @Transactional
    public ProjectDTO removeMember(Long projectId, User user) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        if (1 >= project.getMembers().size()) {
            throw new IllegalStateException("Cannot remove the last member from a project");
        }

        project.getMembers().remove(user);
        Project updatedProject = projectRepository.save(project);
        return projectMapper.toProjectDTO(updatedProject);
    }

    @Transactional
    public void delete(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Project not found with id: " + id);
        }
        projectRepository.deleteById(id);
    }
}