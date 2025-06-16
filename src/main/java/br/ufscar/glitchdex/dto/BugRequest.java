package br.ufscar.glitchdex.dto;

import br.ufscar.glitchdex.domain.BugPriority;
import br.ufscar.glitchdex.domain.BugSeverity;
import br.ufscar.glitchdex.domain.BugStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BugRequest {

    @NotBlank
    @Size(min = 5, max = 200)
    private String title;

    @NotBlank
    private String description;

    private String stepsToReproduce;

    @NotNull
    private BugStatus status;

    @NotNull
    private BugSeverity severity;

    @NotNull
    private BugPriority priority;

    @NotNull
    private Long testSessionId;
}