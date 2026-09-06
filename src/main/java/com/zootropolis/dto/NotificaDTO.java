package com.zootropolis.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class NotificaDTO {
    private Long id;
    private String messaggio;
    private LocalDate data;
    private Boolean letto;
    private Long idAccount;
    private Long idSegnalazione;
}