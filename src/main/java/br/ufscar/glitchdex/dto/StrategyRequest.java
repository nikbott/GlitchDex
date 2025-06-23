package br.ufscar.glitchdex.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StrategyRequest {

    @NotBlank
    @Size(min = 2, max = 100)
    private String name;

    @Size(max = 1000)
    private String description;

    @Size(max = 2000)
    private String examples;

    @Size(max = 2000)
    private String tips;

    private Long projectId;
}
