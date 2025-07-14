package br.ufscar.glitchdex.controller.api;

import br.ufscar.glitchdex.config.Constants;
import br.ufscar.glitchdex.dto.UserDTO;
import br.ufscar.glitchdex.dto.UserRequest;
import br.ufscar.glitchdex.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * REST controller for managing users.
 * All endpoints are restricted to ADMIN authority.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize(Constants.HAS_AUTHORITY_ADMIN) // Applying ADMIN restriction to the entire controller
public class UserRestController {

    private final UserService userService;

    /**
     * Lists all users.
     * Corresponds to AdminController.listUsers() but returns JSON.
     */
    @GetMapping
    public List<UserDTO> listUsers() {
        return userService.findAll();
    }

    /**
     * Retrieves a single user by ID.
     * Corresponds to AdminController.showEditUserForm() logic (findById).
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Creates a new user.
     * Corresponds to AdminController.saveUser() (create logic).
     */
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserRequest userRequest) {
        UserDTO createdUser = userService.create(userRequest);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdUser.getId())
                .toUri();
        return ResponseEntity.created(location).body(createdUser);
    }

    /**
     * Updates an existing user.
     * Corresponds to AdminController.saveUser() (update logic).
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest userRequest) {
        // Ensure the ID in the path is used for the update
        userRequest.setId(id);
        UserDTO updatedUser = userService.update(id, userRequest);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Deletes a user by ID.
     * Corresponds to AdminController.deleteUser().
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}