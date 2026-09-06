package com.zootropolis.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CorsaDTO {
    private Long id;
    private LocalDateTime oraInizio;
    private LocalDateTime oraFine;
    private Double importo;
    private String areaAttuale;
    private Long idUtente;
    private Long idMezzo;
    private Long idPrenotazione;
}