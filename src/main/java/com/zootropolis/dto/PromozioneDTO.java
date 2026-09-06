package com.zootropolis.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PromozioneDTO {
    private Long id;
    private String descrizione;
    private Float sconto;
    private LocalDateTime dataInizio;
    private LocalDateTime dataFine;
    private Long idAmministrazione;
}