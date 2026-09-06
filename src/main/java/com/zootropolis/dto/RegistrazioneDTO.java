package com.zootropolis.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RegistrazioneDTO {
    private String nome;
    private String cognome;
    private String email;
    private String password;
    private LocalDate dataNascita;
}