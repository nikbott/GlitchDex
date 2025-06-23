package br.ufscar.glitchdex.dto;

import br.ufscar.glitchdex.domain.BugPriority;
import br.ufscar.glitchdex.domain.BugSeverity;
import br.ufscar.glitchdex.domain.BugStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BugDTO {
    private Long id;
    private String title;
    private String description;
    private String stepsToReproduce;
    private Set<AttachmentDTO> attachments;
    private LocalDateTime reportDate;
    private BugStatus status;
    private BugSeverity severity;
    private BugPriority priority;
    private Long testSessionId;
    private Long reporterId;
    private String reporterName;

    // ALTERAÇÃO AQUI: Adicionar projectId
    private Long projectId;


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttachmentDTO {
        private Long id;
        private String filename;
    }

    public List<String> getAttachmentFilenames() {
        if (attachments == null) {
            return List.of();
        }
        return attachments.stream()
                .map(AttachmentDTO::getFilename)
                .collect(Collectors.toList());
    }
}