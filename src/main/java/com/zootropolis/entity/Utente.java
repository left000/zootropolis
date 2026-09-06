package com.zootropolis.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@DiscriminatorValue("Utente")
@Getter
@Setter
@NoArgsConstructor
public class Utente extends Account {

    private LocalDate dataNascita;

}