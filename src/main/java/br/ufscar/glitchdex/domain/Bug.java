package br.ufscar.glitchdex.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Represents a bug identified during a test session.
 * This entity is mapped to the "Bug" table in the database and includes indexes for frequently queried columns.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(indexes = {
        @Index(name = "idx_bug_title", columnList = "title"),
        @Index(name = "idx_bug_status", columnList = "status"),
        @Index(name = "idx_bug_priority", columnList = "priority"),
        @Index(name = "idx_bug_severity", columnList = "severity")
})
public class Bug {

    /**
     * The unique identifier for the bug.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The title of the bug. It must not be blank.
     */
    @NotBlank
    private String title;

    /**
     * A detailed description of the bug.
     */
    @Column(length = 2048)
    private String description;

    /**
     * The steps required to reproduce the bug.
     */
    @Column(length = 2048)
    private String stepsToReproduce;

    /**
     * The filename of an attachment associated with the bug, if any.
     */
    private String attachmentFilename;

    /**
     * The date and time when the bug was reported. This is set automatically on creation.
     */
    private LocalDateTime reportDate;

    /**
     * The current status of the bug (e.g., OPEN, IN_PROGRESS).
     */
    @Enumerated(EnumType.STRING)
    private BugStatus status;

    /**
     * The severity of the bug (e.g., CRITICAL, HIGH).
     */
    @Enumerated(EnumType.STRING)
    private BugSeverity severity;

    /**
     * The priority of the bug (e.g., HIGHEST, HIGH).
     */
    @Enumerated(EnumType.STRING)
    private BugPriority priority;

    /**
     * The test session during which this bug was identified.
     * It is a many-to-one relationship, lazily fetched.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_session_id")
    private TestSession testSession;

    /**
     * The user who reported the bug.
     * It is a many-to-one relationship, lazily fetched.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private User reporter;

    /**
     * Sets the reportDate to the current date and time before the entity is persisted.
     */
    @PrePersist
    protected void onCreate() {
        reportDate = LocalDateTime.now();
    }
}