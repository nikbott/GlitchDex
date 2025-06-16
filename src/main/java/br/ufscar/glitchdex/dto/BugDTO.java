package br.ufscar.glitchdex.dto;

import br.ufscar.glitchdex.domain.BugPriority;
import br.ufscar.glitchdex.domain.BugSeverity;
import br.ufscar.glitchdex.domain.BugStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BugDTO {
    private Long id;
    private String title;
    private String description;
    private String stepsToReproduce;
    private String attachmentFilename;
    private LocalDate reportDate;
    private BugStatus status;
    private BugSeverity severity;
    private BugPriority priority;
    private Long testSessionId;
    private Long reporterId;
    private String reporterName;
}