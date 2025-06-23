package br.ufscar.glitchdex.dto;

import br.ufscar.glitchdex.domain.Role;
import lombok.AllArgsConstructor; // Adicionar este import
import lombok.Data;
import lombok.NoArgsConstructor; // Adicionar este import

@Data
@NoArgsConstructor // Gera o construtor padrão sem argumentos
@AllArgsConstructor // Gera um construtor com todos os argumentos na ordem dos campos
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private Role role;
}