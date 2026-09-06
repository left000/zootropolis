package com.zootropolis.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AccountDTO {
    private Long id;
    private String ruolo;
    private String nome;
    private String cognome;
    private String email;
    // La password viene omessa nei DTO di output per sicurezza
    private LocalDate dataNascita;
    private Boolean statoAccount;
}