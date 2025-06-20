package br.ufscar.glitchdex.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a software testing project.
 * Each project has a name, description, a list of members, and associated test sessions.
 * This entity is mapped to the "Project" table.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@NamedEntityGraph(
        name = "Project.withMembers",
        attributeNodes = @NamedAttributeNode("members")
)
@Table(indexes = @Index(name = "idx_project_name", columnList = "name"))
public class Project {
    /**
     * The unique identifier for the project.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The name of the project.
     */
    @NotBlank
    private String name;

    /**
     * A brief description of the project.
     */
    @NotBlank
    @Size(max = 500)
    private String description;

    /**
     * The list of users who are members of this project.
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(
            name = "project_members",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> members = new ArrayList<>();

    /**
     * The timestamp of when the project was created.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * The timestamp of the last update to the project's information.
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