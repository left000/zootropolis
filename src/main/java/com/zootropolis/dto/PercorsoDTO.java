package com.zootropolis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PercorsoDTO {
    private String partenza;
    private String destinazione;
    private double distanzaKm;
    private int tempoStimatoMinuti;
    private String indicazioniMappa;
}