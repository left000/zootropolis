package com.zootropolis.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "MEZZO")
@Getter
@Setter
@NoArgsConstructor
public class Mezzo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String tipo;

    private Integer percentualeBatteria;

    @Column(columnDefinition = "boolean default true")
    private Boolean stato;

    @Column(nullable = false)
    private String posizione;

    // TODO: new attribute
    private Double latitudine;         // 41.1255 (per la mappa)
    private Double longitudine;        // 16.8670 (per la mappa)

    // Traduzione della chiave esterna verso l'operatore (ON DELETE SET NULL applicato a logica)
    @ManyToOne
    @JoinColumn(name = "id_operatore")
    private Account operatore;
}