package br.ufscar.glitchdex.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user of the system.
 * A user can have the role of ADMIN or TESTER.
 * This entity is mapped to the "Users" table.
 */
@Entity
@Table(name = "Users", indexes = @Index(name = "idx_user_name", columnList = "name"))
@Getter
@Setter
@NoArgsConstructor
public class User {
    /**
     * The unique identifier for the user.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The name of the user.
     */
    @NotBlank
    @Size(min = 2, max = 100)
    private String name;

    /**
     * The email of the user, which is unique.
     */
    @NotBlank
    @Column(unique = true)
    @Email
    private String email;

    /**
     * The user's password, stored as a BCrypt hash.
     */
    @NotBlank
    @Size(min = 60, max = 60) // For BCrypt hashed password
    private String password;

    /**
     * The role of the user (e.g., ADMIN, TESTER).
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    private Role role;

    /**
     * The list of test sessions conducted by the user.
     */
    @OneToMany(mappedBy = "tester", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestSession> testSessions = new ArrayList<>();

    /**
     * The timestamp of when the user was created.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * The timestamp of the last update to the user's information.
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Sets the creation and update timestamps before the entity is first persisted.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    /**
     * Updates the 'updatedAt' timestamp before an existing entity is updated.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}