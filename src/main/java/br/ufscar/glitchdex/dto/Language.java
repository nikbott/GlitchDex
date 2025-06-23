package br.ufscar.glitchdex.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Language {
    private String code;
    private String name;
    private String countryCode;
}