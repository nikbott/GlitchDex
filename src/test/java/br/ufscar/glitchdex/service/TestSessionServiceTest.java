package br.ufscar.glitchdex.service;

import br.ufscar.glitchdex.domain.Project;
import br.ufscar.glitchdex.domain.Strategy;
import br.ufscar.glitchdex.domain.TestSession;
import br.ufscar.glitchdex.domain.User;
import br.ufscar.glitchdex.dto.TestSessionDTO;
import br.ufscar.glitchdex.dto.TestSessionRequest;
import br.ufscar.glitchdex.dto.UserDTO;
import br.ufscar.glitchdex.mapper.TestSessionMapper;
import br.ufscar.glitchdex.repository.ProjectRepository;
import br.ufscar.glitchdex.repository.StrategyRepository;
import br.ufscar.glitchdex.repository.TestSessionRepository;
import br.ufscar.glitchdex.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test Session Service Tests")
class TestSessionServiceTest {

    @Mock
    private TestSessionRepository testSessionRepository;
    @Mock
    private StrategyRepository strategyRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TestSessionMapper testSessionMapper;

    @InjectMocks
    private TestSessionService testSessionService;

    private User user;
    private UserDTO userDTO;
    private Project project;
    private Strategy strategy;
    private TestSession testSession;
    private TestSessionRequest testSessionRequest;
    private TestSessionDTO testSessionDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        userDTO = new UserDTO();
        userDTO.setId(1L);
        project = new Project();
        project.setId(1L);
        strategy = new Strategy();
        strategy.setId(1L);

        testSession = new TestSession();
        testSession.setId(1L);

        testSessionRequest = new TestSessionRequest();
        testSessionRequest.setProjectId(1L);
        testSessionRequest.setStrategyId(1L);
        testSessionRequest.setDurationInMinutes(60);
        testSessionRequest.setDescription("Test description");

        testSessionDTO = new TestSessionDTO();
        testSessionDTO.setId(1L);
    }

    @Test
    @DisplayName("Should create and return test session")
    void whenCreate_thenReturnTestSession() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(strategyRepository.findById(1L)).thenReturn(Optional.of(strategy));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(testSessionRepository.save(any(TestSession.class))).thenReturn(testSession);
        when(testSessionMapper.toTestSessionDTO(testSession)).thenReturn(testSessionDTO);

        TestSessionDTO result = testSessionService.create(testSessionRequest, userDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(testSessionRepository).save(any(TestSession.class));
    }
}