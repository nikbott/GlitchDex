package br.ufscar.glitchdex.dto;

import br.ufscar.glitchdex.domain.SessionStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TestSessionRequest {

    @NotNull
    @Positive
    Integer durationInMinutes;
    @NotNull
    private Long strategyId;
    @NotNull
    private SessionStatus status;
}