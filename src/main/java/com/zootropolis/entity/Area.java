package com.zootropolis.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "AREA")
@Getter
@Setter
@NoArgsConstructor
public class Area {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 50)
    private String tipo;

    @Column(nullable = false, length = 50)
    private String stato;

    @Column(columnDefinition = "TEXT")
    private String descrizione;

    @ManyToOne
    @JoinColumn(name = "id_amministrazione")
    private Amministrazione amministrazione;
}