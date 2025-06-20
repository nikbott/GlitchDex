package br.ufscar.glitchdex.service;

import br.ufscar.glitchdex.domain.Bug;
import br.ufscar.glitchdex.domain.SessionStatus;
import br.ufscar.glitchdex.domain.TestSession;
import br.ufscar.glitchdex.domain.User;
import br.ufscar.glitchdex.dto.BugDTO;
import br.ufscar.glitchdex.dto.BugRequest;
import br.ufscar.glitchdex.dto.UserDTO;
import br.ufscar.glitchdex.exception.IllegalStatusChangeException;
import br.ufscar.glitchdex.mapper.BugMapper;
import br.ufscar.glitchdex.repository.BugRepository;
import br.ufscar.glitchdex.repository.TestSessionRepository;
import br.ufscar.glitchdex.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Bug Service Tests")
class BugServiceTest {

    @Mock
    private BugRepository bugRepository;
    @Mock
    private TestSessionRepository testSessionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BugMapper bugMapper;
    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private BugService bugService;

    private User user;
    private UserDTO userDTO;
    private TestSession testSession;
    private Bug bug;
    private BugRequest bugRequest;
    private BugDTO bugDTO;
    private MultipartFile multipartFile;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        userDTO = new UserDTO();
        userDTO.setId(1L);
        testSession = new TestSession();
        testSession.setId(1L);
        testSession.setStatus(SessionStatus.IN_EXECUTION);
        bug = new Bug();
        bug.setId(1L);
        bugRequest = new BugRequest();
        bugRequest.setTestSessionId(1L);
        bugRequest.setTitle("Test Bug");
        bugDTO = new BugDTO();
        bugDTO.setId(1L);
        multipartFile = mock(MultipartFile.class);
    }

    @Test
    @DisplayName("Should create and return bug")
    void whenCreate_thenReturnBug() throws IllegalStatusChangeException {
        when(testSessionRepository.findById(1L)).thenReturn(Optional.of(testSession));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(fileStorageService.store(multipartFile)).thenReturn("filename.txt");
        when(bugRepository.save(any(Bug.class))).thenReturn(bug);
        when(bugMapper.toBugDTO(bug)).thenReturn(bugDTO);

        BugDTO result = bugService.create(bugRequest, userDTO, multipartFile);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }
}