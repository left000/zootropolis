package com.zootropolis.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "PRENOTAZIONE")
@Getter
@Setter
@NoArgsConstructor
public class Prenotazione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime oraInizio;

    private LocalDateTime oraFine;

    @Column(nullable = false)
    private Integer codiceSblocco;

    @Column(columnDefinition = "boolean default true")
    private Boolean stato;

    @ManyToOne
    @JoinColumn(name = "id_utente")
    private Utente utente;

    @ManyToOne
    @JoinColumn(name = "id_mezzo")
    private Mezzo mezzo;
}