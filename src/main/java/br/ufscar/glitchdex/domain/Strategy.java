package br.ufscar.glitchdex.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a testing strategy.
 * A strategy provides guidelines, examples, and tips for testers to follow during a test session.
 * This entity is mapped to the "Strategy" table.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(indexes = @Index(name = "idx_strategy_name", columnList = "name"))
public class Strategy {
    /**
     * The unique identifier for the strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The name of the strategy.
     */
    @NotBlank
    private String name;

    /**
     * A detailed description of the strategy.
     */
    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Examples of how to apply the strategy.
     */
    @Column(columnDefinition = "TEXT")
    private String examples;

    /**
     * Tips for effectively using the strategy.
     */
    @Column(columnDefinition = "TEXT")
    private String tips;

    /**
     * The URL of an image associated with the strategy.
     */
    @Column(name = "image_url")
    private String imageUrl;

    /**
     * The user who created the strategy.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    /**
     * The list of test sessions that use this strategy.
     */
    @OneToMany(mappedBy = "strategy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestSession> testSessions = new ArrayList<>();

    /**
     * The timestamp of when the strategy was created.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * The timestamp of the last update to the strategy's information.
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