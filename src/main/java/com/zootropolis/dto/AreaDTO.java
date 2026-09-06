package com.zootropolis.dto;

import lombok.Data;

@Data
public class AreaDTO {
    private Long id;
    private String nome;
    private String tipo;
    private String stato;
    private String descrizione;
    private Long idAmministrazione;
}