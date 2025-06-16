package br.ufscar.glitchdex.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Bug {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    // This annotation is changed to a searchable VARCHAR
    @Column(length = 2048)
    private String description;

    @Column(length = 2048)
    private String stepsToReproduce;

    private String attachmentFilename;

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