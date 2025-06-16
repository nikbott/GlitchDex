package br.ufscar.glitchdex.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TestSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @NotNull
    private User tester;

    @ManyToOne
    @NotNull
    private Project project;

    @ManyToOne
    @NotNull
    private Strategy strategy;

    private Integer durationInMinutes;

    @NotBlank
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    private LocalDateTime creationTimestamp;
    private LocalDateTime startTimestamp;
    private LocalDateTime finalizationTimestamp;

    @OneToMany(mappedBy = "testSession", cascade = CascadeType.ALL)
    private List<Bug> bugs = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
        creationTimestamp = LocalDateTime.now();
        status = SessionStatus.CREATED;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void start() {
        if (SessionStatus.CREATED != status) {
            throw new IllegalStateException("Session can only be started when in CREATED status");
        }
        startTimestamp = LocalDateTime.now();
        status = SessionStatus.IN_EXECUTION;
    }

    public void finish() {
        if (SessionStatus.IN_EXECUTION != status) {
            throw new IllegalStateException("Session can only be finalized when in IN_EXECUTION status");
        }
        finalizationTimestamp = LocalDateTime.now();
        status = SessionStatus.FINALIZED;
    }

    public void setSessionDate(LocalDate now) {
        creationTimestamp = now.atStartOfDay();
    }
}