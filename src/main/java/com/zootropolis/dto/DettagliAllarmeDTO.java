package com.zootropolis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DettagliAllarmeDTO {
    private Long idMezzo;
    private String tipoMezzo;
    private String posizioneAttuale;
    private Integer percentualeBatteria;
    private String tipoAllarme; // Es. "SPOSTAMENTO NON AUTORIZZATO"
}