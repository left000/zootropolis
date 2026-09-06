package com.zootropolis.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "REPORT")
@Getter
@Setter
@NoArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Boolean tipo;

    @Column(nullable = false)
    private LocalDate data;

    @Column(columnDefinition = "TEXT")
    private String contenuto;

    @ManyToOne
    @JoinColumn(name = "id_amministrazione")
    private Amministrazione amministrazione;
}