package com.zootropolis.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "CORSA")
@Getter
@Setter
@NoArgsConstructor
public class Corsa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime oraInizio;

    private LocalDateTime oraFine;

    @Column(columnDefinition = "double default 0.0")
    private Double importo;

    private String areaAttuale;

    @ManyToOne
    @JoinColumn(name = "id_utente")
    private Utente utente;

    @ManyToOne
    @JoinColumn(name = "id_mezzo")
    private Mezzo mezzo;

    @OneToOne
    @JoinColumn(name = "id_prenotazione")
    private Prenotazione prenotazione;
}