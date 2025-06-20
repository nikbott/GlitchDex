package br.ufscar.glitchdex.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StrategyDTO {

    private Long id;
    private String name;
    private String description;
    private String examples;
    private String tips;
    private String imageUrl;
    private Long creatorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}