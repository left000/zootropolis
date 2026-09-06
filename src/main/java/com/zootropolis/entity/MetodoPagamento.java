package com.zootropolis.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "METODO_PAGAMENTO")
@Getter
@Setter
@NoArgsConstructor
public class MetodoPagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String tipo;

    @Column(nullable = false, length = 100)
    private String intestatario;

    @Column(nullable = false, length = 50)
    private String numero;

    @ManyToOne
    @JoinColumn(name = "id_utente")
    private Utente utente;
}