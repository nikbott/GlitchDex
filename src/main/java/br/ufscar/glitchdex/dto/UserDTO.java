package br.ufscar.glitchdex.dto;

import br.ufscar.glitchdex.domain.Role;
import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private Role role;
}