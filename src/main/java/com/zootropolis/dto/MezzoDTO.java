package com.zootropolis.dto;

import lombok.Data;

@Data
public class MezzoDTO {
    private Long id;
    private String tipo;
    private Integer percentualeBatteria;
    private Boolean stato;
    private String posizione;
    private Long idOperatore;
    private String descrizioneAnomalia; // Campo dinamico per UC-12
}