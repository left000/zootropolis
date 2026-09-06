package com.zootropolis.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SegnalazioneDTO {
    private Long id;
    private String problema;
    private LocalDate data;
    private Boolean stato;
    private Long idUtente;
    private Long idMezzo;
}