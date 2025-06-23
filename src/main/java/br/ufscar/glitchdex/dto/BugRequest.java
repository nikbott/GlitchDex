package br.ufscar.glitchdex.dto;

import br.ufscar.glitchdex.domain.BugPriority;
import br.ufscar.glitchdex.domain.BugSeverity;
import br.ufscar.glitchdex.domain.BugStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor; // Adicione este

@Data
@NoArgsConstructor // Adicione este construtor padrão
public class BugRequest {

    @NotBlank
    @Size(min = 2, max = 200)
    private String title;

    @Size(max = 2048)
    private String description;

    @Size(max = 2048)
    private String stepsToReproduce;

    @NotNull
    private BugStatus status;

    @NotNull
    private BugSeverity severity;

    @NotNull
    private BugPriority priority;

    @NotNull
    private Long testSessionId;

    // ALTERAÇÃO AQUI: Adicionar projectId
    private Long projectId;

    // Construtor para facilitar a conversão de BugDTO para BugRequest
    public BugRequest(BugDTO bugDto) {
        this.title = bugDto.getTitle();
        this.description = bugDto.getDescription();
        this.stepsToReproduce = bugDto.getStepsToReproduce();
        this.status = bugDto.getStatus();
        this.severity = bugDto.getSeverity();
        this.priority = bugDto.getPriority();
        this.testSessionId = bugDto.getTestSessionId();
        this.projectId = bugDto.getProjectId(); // Certifique-se de que BugDTO também tem projectId
    }
}