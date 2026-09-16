package com.zootropolis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AreaSquilibrataDTO {
    private String nomeArea;
    private int mezzePresenti;
    private int capacitaMassima;
    private String tipoSquilibrio;
    private int deltaMezzi;
}