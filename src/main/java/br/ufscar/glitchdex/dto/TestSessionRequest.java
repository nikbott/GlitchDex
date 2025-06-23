package br.ufscar.glitchdex.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TestSessionRequest {
    @NotNull
    @Positive
    Integer durationInMinutes;
    private Long id;
    private String description;

    @NotNull
    private Long strategyId;

    @NotNull
    private Long projectId;
}