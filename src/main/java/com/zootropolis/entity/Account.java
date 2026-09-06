package com.zootropolis.entity;

import lombok.Setter;
import jakarta.persistence.*;
import lombok.Getter;

import lombok.NoArgsConstructor;

@Entity
@Table(name = "ACCOUNT")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "ruolo", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 100)
    private String cognome;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "stato_account")
    private Boolean statoAccount = true;

    // --- METODI SPECIFICI RICHIESTI DAL DIAGRAMMA UC-02 ---

    // Message: getDatiAccesso() -> Return: credenzialiRegistrate
    public String getDatiAccesso() {
        return this.password;
    }

    // Message: setStatoAutenticazione(true) -> Return: statoAggiornato
    public void setStatoAutenticazione(boolean stato) {
        this.statoAccount = stato;
    }
}