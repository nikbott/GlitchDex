package br.ufscar.glitchdex.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class StrategyDTO {
    private Long id;
    private String name;
    private String description;
    private LocalDate creationDate;
    private Long projectId;
    private Long creatorId;
}