package br.ufscar.glitchdex.service;

import br.ufscar.glitchdex.domain.Strategy;
import br.ufscar.glitchdex.domain.User;
import br.ufscar.glitchdex.dto.StrategyDTO;
import br.ufscar.glitchdex.dto.StrategyRequest;
import br.ufscar.glitchdex.dto.UserDTO;
import br.ufscar.glitchdex.exception.ResourceNotFoundException;
import br.ufscar.glitchdex.mapper.StrategyMapper;
import br.ufscar.glitchdex.repository.StrategyRepository;
import br.ufscar.glitchdex.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Strategy Service Tests")
class StrategyServiceTest {

    @Mock
    private StrategyRepository strategyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StrategyMapper strategyMapper;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private ImageProcessingService imageProcessingService;

    @InjectMocks
    private StrategyService strategyService;

    private User user;
    private UserDTO userDTO;
    private Strategy strategy;
    private StrategyRequest strategyRequest;
    private StrategyDTO strategyDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setEmail("test@example.com");

        strategy = new Strategy();
        strategy.setId(1L);
        strategy.setName("Test Strategy");
        strategy.setCreator(user);

        strategyRequest = new StrategyRequest();
        strategyRequest.setName("Test Strategy");

        strategyDTO = new StrategyDTO();
        strategyDTO.setId(1L);
        strategyDTO.setName("Test Strategy");
    }

    @Test
    @DisplayName("Should return strategy DTO when finding by existing ID")
    void whenFindById_andStrategyExists_thenReturnStrategyDTO() {
        when(strategyRepository.findById(1L)).thenReturn(Optional.of(strategy));
        when(strategyMapper.toStrategyDTO(strategy)).thenReturn(strategyDTO);

        StrategyDTO found = strategyService.findById(1L);

        assertNotNull(found);
        assertEquals(strategyDTO.getId(), found.getId());
        verify(strategyRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when finding by non-existing ID")
    void whenFindById_andStrategyDoesNotExist_thenThrowException() {
        when(strategyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> strategyService.findById(1L));
    }

    @Test
    @DisplayName("Should create and return strategy")
    void whenCreate_thenSaveAndReturnStrategy() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(strategyRepository.save(any(Strategy.class))).thenReturn(strategy);
        when(strategyMapper.toStrategyDTO(any(Strategy.class))).thenReturn(strategyDTO);

        StrategyDTO created = strategyService.create(strategyRequest, userDTO);

        assertNotNull(created);
        assertEquals(strategyDTO.getName(), created.getName());
        verify(strategyRepository).save(any(Strategy.class));
    }

    @Test
    @DisplayName("Should delete strategy when it exists")
    void whenDelete_thenCallDelete() {
        when(strategyRepository.findById(1L)).thenReturn(Optional.of(strategy));
        doNothing().when(strategyRepository).delete(strategy);

        strategyService.delete(1L);

        verify(strategyRepository, times(1)).delete(strategy);
    }
}