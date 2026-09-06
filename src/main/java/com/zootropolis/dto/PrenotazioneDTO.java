package com.zootropolis.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PrenotazioneDTO {
    private Long id;
    private LocalDateTime oraInizio;
    private LocalDateTime oraFine;
    private Integer codiceSblocco;
    private Boolean stato;
    private Long idUtente;
    private Long idMezzo;
}