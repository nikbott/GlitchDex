package br.ufscar.glitchdex.dto;

import br.ufscar.glitchdex.domain.Role;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequest {

    private Long id;

    @NotBlank
    @Size(min = 2, max = 100)
    private String name;

    @NotBlank
    @Column(unique = true)
    @Email
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotNull
    private Role role;
}