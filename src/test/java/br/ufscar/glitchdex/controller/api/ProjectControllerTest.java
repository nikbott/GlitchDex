package br.ufscar.glitchdex.controller.api;

import br.ufscar.glitchdex.dto.ProjectDTO;
import br.ufscar.glitchdex.dto.UserDTO;
import br.ufscar.glitchdex.service.ProjectService;
import br.ufscar.glitchdex.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Project Controller API Tests")
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("Should return projects for authenticated user")
    @WithMockUser(username = "test@example.com", authorities = "TESTER")
    void whenListProjects_andUserIsAuthenticated_thenReturnProjects() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setEmail("test@example.com");

        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setId(1L);
        projectDTO.setName("Test Project");

        when(userService.findByEmailDTO(anyString())).thenReturn(Optional.of(userDTO));
        when(projectService.findByMember(any(), any(), any())).thenReturn(Collections.singletonList(projectDTO));

        mockMvc.perform(get("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}