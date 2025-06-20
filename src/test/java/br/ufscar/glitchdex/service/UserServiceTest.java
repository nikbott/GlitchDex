package br.ufscar.glitchdex.service;

import br.ufscar.glitchdex.domain.Role;
import br.ufscar.glitchdex.domain.User;
import br.ufscar.glitchdex.dto.UserDTO;
import br.ufscar.glitchdex.dto.UserRequest;
import br.ufscar.glitchdex.exception.EmailAlreadyExistsException;
import br.ufscar.glitchdex.mapper.UserMapper;
import br.ufscar.glitchdex.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Service Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserRequest userRequest;
    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.TESTER);

        userRequest = new UserRequest();
        userRequest.setEmail("test@example.com");
        userRequest.setPassword("password");
        userRequest.setRole(Role.TESTER);
        userRequest.setName("Test User");

        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setEmail("test@example.com");
        userDTO.setRole(Role.TESTER);
        userDTO.setName("Test User");
    }

    @Test
    @DisplayName("Should create and return user when email does not exist")
    void whenCreateUser_withNonExistingEmail_thenSaveAndReturnUser() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userMapper.toUser(any(UserRequest.class))).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserDTO(any(User.class))).thenReturn(userDTO);

        UserDTO created = userService.create(userRequest);

        assertNotNull(created);
        assertEquals(userDTO.getEmail(), created.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when creating user with existing email")
    void whenCreateUser_withExistingEmail_thenThrowException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> userService.create(userRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should update and return user when user exists")
    void whenUpdateUser_andUserExists_thenUpdateAndReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserDTO(any(User.class))).thenReturn(userDTO);

        UserDTO updated = userService.update(1L, userRequest);

        assertNotNull(updated);
        assertEquals(userDTO.getName(), updated.getName());
        verify(userRepository).save(user);
    }
}