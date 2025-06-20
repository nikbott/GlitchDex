package br.ufscar.glitchdex.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single test session within a project.
 * A test session is conducted by a tester, follows a specific strategy, and has a defined duration.
 * This entity is mapped to the "TestSession" table.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class TestSession {
    /**
     * The unique identifier for the test session.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user who is conducting the test session.
     */
    @ManyToOne
    @NotNull
    private User tester;

    /**
     * The project to which this test session belongs.
     */
    @ManyToOne
    @NotNull
    private Project project;

    /**
     * The testing strategy used in this session.
     */
    @ManyToOne
    @NotNull
    private Strategy strategy;

    /**
     * The duration of the test session in minutes.
     */
    private Integer durationInMinutes;

    /**
     * A description of the test session's scope or goals.
     */
    @NotBlank
    private String description;

    /**
     * The current status of the test session (e.g., CREATED, IN_EXECUTION, FINALIZED).
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    /**
     * The timestamp of when the session started.
     */
    private LocalDateTime startTimestamp;

    /**
     * The timestamp of when the session was finalized.
     */
    private LocalDateTime finalizationTimestamp;

    /**
     * The list of bugs reported during this test session.
     */
    @OneToMany(mappedBy = "testSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bug> bugs = new ArrayList<>();

    /**
     * The timestamp of when the test session was created.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * The timestamp of the last update to the test session's information.
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Sets the creation and update timestamps and the default status before the entity is first persisted.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
        // Default status upon creation
        status = SessionStatus.CREATED;
    }

    /**
     * Updates the 'updatedAt' timestamp before an existing entity is updated.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}