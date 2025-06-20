package br.ufscar.glitchdex.service;

import br.ufscar.glitchdex.domain.Project;
import br.ufscar.glitchdex.domain.User;
import br.ufscar.glitchdex.dto.ProjectDTO;
import br.ufscar.glitchdex.dto.ProjectRequest;
import br.ufscar.glitchdex.exception.ResourceNotFoundException;
import br.ufscar.glitchdex.mapper.ProjectMapper;
import br.ufscar.glitchdex.repository.ProjectRepository;
import br.ufscar.glitchdex.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Project Service Tests")
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private ProjectService projectService;

    private User user;
    private Project project;
    private ProjectRequest projectRequest;
    private ProjectDTO projectDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        project = new Project();
        project.setId(1L);
        project.setName("Test Project");
        project.setMembers(new ArrayList<>(Collections.singletonList(user)));

        projectRequest = new ProjectRequest();
        projectRequest.setName("Test Project");
        projectRequest.setMemberIds(Collections.singletonList(1L));

        projectDTO = new ProjectDTO();
        projectDTO.setId(1L);
        projectDTO.setName("Test Project");
    }

    @Test
    @DisplayName("Should return project when finding by existing ID")
    void whenFindById_andProjectExists_thenReturnProjectDTO() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectMapper.toProjectDTO(project)).thenReturn(projectDTO);

        ProjectDTO found = projectService.findById(1L);

        assertNotNull(found);
        assertEquals(projectDTO.getId(), found.getId());
        verify(projectRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when finding by non-existing ID")
    void whenFindById_andProjectDoesNotExist_thenThrowException() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectService.findById(1L));
        verify(projectRepository).findById(1L);
    }

    @Test
    @DisplayName("Should create and return project")
    void whenCreate_thenSaveAndReturnProject() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(user.getEmail());
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(new UsernamePasswordAuthenticationToken(userDetails, null));
        SecurityContextHolder.setContext(securityContext);

        when(userService.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(projectMapper.toProject(any(ProjectRequest.class))).thenReturn(project);
        when(userRepository.findAllById(anyList())).thenReturn(Collections.singletonList(user));
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectMapper.toProjectDTO(any(Project.class))).thenReturn(projectDTO);

        ProjectDTO created = projectService.create(projectRequest);

        assertNotNull(created);
        assertEquals(projectDTO.getName(), created.getName());
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    @DisplayName("Should update and return project when project exists")
    void whenUpdate_andProjectExists_thenUpdateAndReturnProject() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findAllById(anyList())).thenReturn(Collections.singletonList(user));
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectMapper.toProjectDTO(any(Project.class))).thenReturn(projectDTO);

        ProjectDTO updated = projectService.update(1L, projectRequest);

        assertNotNull(updated);
        assertEquals(projectDTO.getName(), updated.getName());
        verify(projectRepository).save(project);
    }

    @Test
    @DisplayName("Should delete project")
    void whenDelete_thenCallDeleteById() {
        doNothing().when(projectRepository).deleteById(1L);

        projectService.delete(1L);

        verify(projectRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should add member to project")
    void whenAddMember_thenAddMemberToProject() {
        User newUser = new User();
        newUser.setId(2L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findById(2L)).thenReturn(Optional.of(newUser));
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectMapper.toProjectDTO(any(Project.class))).thenReturn(projectDTO);

        projectService.addMember(1L, 2L);

        assertEquals(2, project.getMembers().size());
        verify(projectRepository).save(project);
    }

    @Test
    @DisplayName("Should remove member from project")
    void whenRemoveMember_thenRemoveMemberFromProject() {
        User memberToRemove = new User();
        memberToRemove.setId(2L);
        project.getMembers().add(memberToRemove);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findById(2L)).thenReturn(Optional.of(memberToRemove));
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectMapper.toProjectDTO(any(Project.class))).thenReturn(projectDTO);

        projectService.removeMember(1L, 2L);

        assertEquals(1, project.getMembers().size());
        verify(projectRepository).save(project);
    }

    @Test
    @DisplayName("Should throw exception when removing the last member from a project")
    void whenRemoveLastMember_thenThrowException() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(IllegalStateException.class, () -> projectService.removeMember(1L, 1L));
        verify(projectRepository, never()).save(any(Project.class));
    }
}