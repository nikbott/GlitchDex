package br.ufscar.glitchdex.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProjectDTO {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private List<UserDTO> members;
}