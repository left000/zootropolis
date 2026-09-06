package com.zootropolis.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ReportDTO {
    private Long id;
    private Boolean tipo;
    private LocalDate data;
    private String contenuto;
    private Long idAmministrazione;
}