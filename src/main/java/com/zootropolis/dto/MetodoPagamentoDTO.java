package com.zootropolis.dto;

import lombok.Data;

@Data
public class MetodoPagamentoDTO {
    private Long id;
    private String tipo;
    private String intestatario;
    private String numero;
    private Long idUtente;
}