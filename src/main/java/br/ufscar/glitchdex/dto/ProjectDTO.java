package br.ufscar.glitchdex.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ProjectDTO {
    private Long id;
    private String name;
    private String description;
    private LocalDate creationDate;
    private List<Long> memberIds;
}