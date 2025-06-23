package br.ufscar.glitchdex.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ProjectRequest {
    private Long id;

    @NotBlank(message = "Project name is required.")
    @Size(min = 2, max = 100, message = "Project name must be between 2 and 100 characters.")
    private String name;

    @Size(max = 500, message = "Project description cannot exceed 500 characters.")
    private String description;

    @NotEmpty(message = "A project must have at least one member.")
    private List<Long> memberIds;
}