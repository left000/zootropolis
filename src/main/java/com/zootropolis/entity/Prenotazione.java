package com.zootropolis.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Random;

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

    // Costruttore per UC-05: create(utente, mezzo, oraAttuale)
    public Prenotazione(Utente utente, Mezzo mezzo, LocalDateTime oraInizio, int minutiValidita) {
        this.utente = utente;
        this.mezzo = mezzo;
        this.oraInizio = oraInizio;
        this.oraFine = oraInizio.plusMinutes(minutiValidita);
        this.stato = true; // true = Prenotazione Attiva
        this.codiceSblocco = 1000 + new Random().nextInt(9000); // Genera codice a 4 cifre
    }
}