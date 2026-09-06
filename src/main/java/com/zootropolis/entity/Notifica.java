package com.zootropolis.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "NOTIFICA")
@Getter
@Setter
@NoArgsConstructor
public class Notifica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String messaggio;

    @Column(nullable = false)
    private LocalDate data;

    @Column(columnDefinition = "boolean default false")
    private Boolean letto;

    @ManyToOne
    @JoinColumn(name = "id_account")
    private Account account;

    @ManyToOne
    @JoinColumn(name = "id_segnalazione")
    private Segnalazione segnalazione;
}