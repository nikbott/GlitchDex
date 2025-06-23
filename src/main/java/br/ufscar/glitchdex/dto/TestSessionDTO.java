package br.ufscar.glitchdex.dto;

import br.ufscar.glitchdex.domain.SessionStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TestSessionDTO {
    private Long id;
    private Long projectId;
    private Long strategyId;
    private Long testerId;
    private String testerName;
    private String description;
    private SessionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime startTimestamp;
    private LocalDateTime finalizationTimestamp;
    private List<BugDTO> bugs;
}