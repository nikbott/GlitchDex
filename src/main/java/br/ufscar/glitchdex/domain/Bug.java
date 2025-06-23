package br.ufscar.glitchdex.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    @Column(length = 2048)
    private String description;

    @Column(length = 2048)
    private String stepsToReproduce;

    @OneToMany(mappedBy = "bug", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BugAttachment> attachments = new HashSet<>();

    private LocalDateTime reportDate;

    @Enumerated(EnumType.STRING)
    private BugStatus status;

    @Enumerated(EnumType.STRING)
    private BugSeverity severity;

    @Enumerated(EnumType.STRING)
    private BugPriority priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_session_id")
    private TestSession testSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private User reporter;

    @PrePersist
    protected void onCreate() {
        reportDate = LocalDateTime.now();
    }
}