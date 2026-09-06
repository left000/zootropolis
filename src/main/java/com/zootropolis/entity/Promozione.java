package com.zootropolis.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "PROMOZIONE")
@Getter
@Setter
@NoArgsConstructor
public class Promozione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String descrizione;

    @Column(nullable = false)
    private Float sconto;

    @Column(nullable = false)
    private LocalDateTime dataInizio;

    @Column(nullable = false)
    private LocalDateTime dataFine;

    @ManyToOne
    @JoinColumn(name = "id_amministrazione")
    private Amministrazione amministrazione;

    @ManyToMany
    @JoinTable(
            name = "UTENTE_PROMOZIONE",
            joinColumns = @JoinColumn(name = "id_promozione"),
            inverseJoinColumns = @JoinColumn(name = "id_utente")
    )
    private List<Utente> utenti;
}