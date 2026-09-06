package com.zootropolis.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("Operatore")
@Getter
@Setter
@NoArgsConstructor
public class Operatore extends Account {

}