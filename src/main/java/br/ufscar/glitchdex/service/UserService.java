package br.ufscar.glitchdex.service;

import br.ufscar.glitchdex.domain.Role;
import br.ufscar.glitchdex.domain.User;
import br.ufscar.glitchdex.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Find a user by ID
     *
     * @param id the user ID
     * @return an Optional containing the user if found
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Find a user by email
     *
     * @param email the user email
     * @return an Optional containing the user if found
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Get all users
     *
     * @return a list of all users
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * Create a new user
     *
     * @param user the user to create
     * @return the created user
     */
    @Transactional
    public User create(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        // Encode the password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    /**
     * Update an existing user
     *
     * @param id          the user ID
     * @param updatedUser the updated user data
     * @return the updated user
     */
    @Transactional
    public User update(Long id, User updatedUser) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if email is being changed and if it's already in use
        if (!existingUser.getEmail().equals(updatedUser.getEmail()) &&
                userRepository.existsByEmail(updatedUser.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        // Update fields
        existingUser.setName(updatedUser.getName());
        existingUser.setEmail(updatedUser.getEmail());

        // Only update password if it's provided and different
        if (null != updatedUser.getPassword() && !updatedUser.getPassword().isEmpty() &&
                !passwordEncoder.matches(updatedUser.getPassword(), existingUser.getPassword())) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        // Update role if provided and user is admin
        if (null != updatedUser.getRole()) {
            existingUser.setRole(updatedUser.getRole());
        }

        return userRepository.save(existingUser);
    }

    /**
     * Delete a user
     *
     * @param id the user ID
     */
    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found");
        }
        userRepository.deleteById(id);
    }

    /**
     * Create an admin user if no users exist
     *
     * @param email    the admin email
     * @param password the admin password
     * @return the created admin user
     */
    @Transactional
    public User createAdminIfNotExists(String email, String password) {
        if (0 == userRepository.count()) {
            User admin = new User();
            admin.setName("Administrator");
            admin.setEmail(email);
            admin.setPassword(passwordEncoder.encode(password));
            admin.setRole(Role.ADMIN);
            return userRepository.save(admin);
        }
        return null;
    }
}
