package br.ufscar.glitchdex.service;

import br.ufscar.glitchdex.domain.Role;
import br.ufscar.glitchdex.domain.User;
import br.ufscar.glitchdex.dto.UserDTO;
import br.ufscar.glitchdex.dto.UserRequest;
import br.ufscar.glitchdex.exception.EmailAlreadyExistsException;
import br.ufscar.glitchdex.exception.PasswordValidationException;
import br.ufscar.glitchdex.exception.ResourceNotFoundException;
import br.ufscar.glitchdex.mapper.UserMapper;
import br.ufscar.glitchdex.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing users.
 * Provides business logic for creating, retrieving, updating, and deleting users.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final MessageSource messageSource;

    /**
     * Finds a user by their ID.
     *
     * @param id The ID of the user.
     * @return An Optional containing the UserDTO if found.
     */
    @Transactional(readOnly = true)
    public Optional<UserDTO> findById(Long id) {
        log.info("Finding user by id: {}", id);
        return userRepository.findById(id).map(userMapper::toUserDTO);
    }

    /**
     * Finds a user by their email.
     *
     * @param email The email of the user.
     * @return An Optional containing the User if found.
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        log.info("Finding user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    /**
     * Finds a user by their email and returns their DTO.
     *
     * @param email The email of the user.
     * @return An Optional containing the UserDTO if found.
     */
    @Transactional(readOnly = true)
    public Optional<UserDTO> findByEmailDTO(String email) {
        log.info("Finding user DTO by email: {}", email);
        return userRepository.findByEmail(email).map(userMapper::toUserDTO);
    }

    /**
     * Finds all users and returns their DTOs.
     *
     * @return A list of all UserDTOs.
     */
    @Transactional(readOnly = true)
    public List<UserDTO> findAll() {
        log.info("Finding all users");
        return userMapper.toUserDTOs(userRepository.findAll());
    }

    /**
     * Creates a new user.
     *
     * @param userRequest The request object with the user details.
     * @return The created UserDTO.
     * @throws EmailAlreadyExistsException if the email is already in use.
     * @throws PasswordValidationException if the password is not provided.
     */
    @Transactional
    public UserDTO create(UserRequest userRequest) {
        log.info("Creating user with email: {}", userRequest.getEmail());
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            log.warn("Attempt to create user with existing email: {}", userRequest.getEmail());
            throw new EmailAlreadyExistsException(messageSource.getMessage("error.email.in_use", null, LocaleContextHolder.getLocale()));
        }
        if (userRequest.getPassword() == null || userRequest.getPassword().isBlank()) {
            log.warn("Attempt to create user with empty password");
            throw new PasswordValidationException(messageSource.getMessage("error.password.required", null, LocaleContextHolder.getLocale()));
        }
        User user = userMapper.toUser(userRequest);
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        UserDTO userDTO = userMapper.toUserDTO(userRepository.save(user));
        log.info("User with email {} created successfully with id {}", userDTO.getEmail(), userDTO.getId());
        return userDTO;
    }

    /**
     * Updates an existing user.
     *
     * @param id          The ID of the user to update.
     * @param userRequest The request object with the updated user details.
     * @return The updated UserDTO.
     * @throws ResourceNotFoundException   if no user is found with the given ID.
     * @throws EmailAlreadyExistsException if the new email is already in use.
     */
    @Transactional
    public UserDTO update(Long id, UserRequest userRequest) {
        log.info("Updating user with id: {}", id);
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("error.user.not_found", new Object[]{id}, LocaleContextHolder.getLocale())));

        if (!existingUser.getEmail().equals(userRequest.getEmail()) &&
                userRepository.existsByEmail(userRequest.getEmail())) {
            log.warn("Attempt to update user with existing email: {}", userRequest.getEmail());
            throw new EmailAlreadyExistsException(messageSource.getMessage("error.email.in_use", null, LocaleContextHolder.getLocale()));
        }

        existingUser.setName(userRequest.getName());
        existingUser.setEmail(userRequest.getEmail());
        existingUser.setRole(userRequest.getRole());

        if (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()) {
            log.info("Updating password for user with id: {}", id);
            existingUser.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        }
        UserDTO userDTO = userMapper.toUserDTO(userRepository.save(existingUser));
        log.info("User with id {} updated successfully", id);
        return userDTO;
    }

    /**
     * Deletes a user by their ID.
     *
     * @param id The ID of the user to delete.
     */
    @Transactional
    public void delete(Long id) {
        log.info("Deleting user with id: {}", id);
        userRepository.deleteById(id);
        log.info("User with id {} deleted successfully", id);
    }

    /**
     * Creates an admin user if no users exist in the database.
     *
     * @param email    The admin's email.
     * @param password The admin's password.
     * @return The created admin UserDTO, or null if users already exist.
     */
    @Transactional
    public UserDTO createAdminIfNotExists(String email, String password) {
        if (userRepository.count() == 0) {
            log.info("No users found, creating admin user with email: {}", email);
            User admin = new User();
            admin.setName("Administrator");
            admin.setEmail(email);
            admin.setPassword(passwordEncoder.encode(password));
            admin.setRole(Role.ADMIN);
            UserDTO adminDTO = userMapper.toUserDTO(userRepository.save(admin));
            log.info("Admin user created successfully with id {}", adminDTO.getId());
            return adminDTO;
        }
        return null;
    }
}