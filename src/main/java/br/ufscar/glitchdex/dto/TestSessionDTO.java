package br.ufscar.glitchdex.dto;

import br.ufscar.glitchdex.domain.SessionStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TestSessionDTO {
    private Long id;
    private Long strategyId;
    private Long testerId;
    private String testerName;
    private SessionStatus status;
    private LocalDate sessionDate;
}