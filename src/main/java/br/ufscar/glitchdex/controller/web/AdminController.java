package br.ufscar.glitchdex.controller.web;

import br.ufscar.glitchdex.dto.UserRequest;
import br.ufscar.glitchdex.mapper.UserMapper;
import br.ufscar.glitchdex.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling administrative tasks related to user management.
 * All endpoints in this controller require 'ADMIN' authority.
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);
    private final UserService userService;
    private final UserMapper userMapper;

    /**
     * Displays a list of all users.
     *
     * @param model The model to which the list of users will be added.
     * @return The view name for the user list page.
     */
    @GetMapping("/users")
    public String listUsers(Model model) {
        log.info("Admin request to list all users");
        model.addAttribute("users", userService.findAll());
        return "admin/users";
    }

    /**
     * Shows the form for creating a new user.
     *
     * @param model The model to which a new UserRequest object will be added.
     * @return The view name for the user form.
     */
    @GetMapping("/users/new")
    public String showUserForm(Model model) {
        log.info("Admin request to show new user form");
        model.addAttribute("userRequest", new UserRequest());
        return "admin/user-form";
    }

    /**
     * Saves a new or updated user.
     *
     * @param userRequest The UserRequest object containing the user's data.
     * @param result      The BindingResult for validation.
     * @return A redirect to the user list page if successful, otherwise the user form.
     */
    @PostMapping("/users")
    public String saveUser(@Valid @ModelAttribute("userRequest") UserRequest userRequest, BindingResult result) {
        log.info("Admin request to save user with email: {}", userRequest.getEmail());
        if (result.hasErrors()) {
            log.warn("Validation errors while saving user: {}", result.getAllErrors());
            return "admin/user-form";
        }
        userService.save(userRequest);
        log.info("User with email: {} saved successfully", userRequest.getEmail());
        return "redirect:/admin/users";
    }

    /**
     * Shows the form for editing an existing user.
     *
     * @param id    The ID of the user to edit.
     * @param model The model to which the UserRequest object will be added.
     * @return The view name for the user form.
     */
    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        log.info("Admin request to show edit form for user with id: {}", id);
        UserRequest userRequest = userService.findById(id)
                .map(userMapper::toUserRequest)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        model.addAttribute("userRequest", userRequest);
        return "admin/user-form";
    }

    /**
     * Deletes a user by their ID.
     *
     * @param id The ID of the user to delete.
     * @return A redirect to the user list page.
     */
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        log.info("Admin request to delete user with id: {}", id);
        userService.delete(id);
        log.info("User with id: {} deleted successfully", id);
        return "redirect:/admin/users";
    }
}