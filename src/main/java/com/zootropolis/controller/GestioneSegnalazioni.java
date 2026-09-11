package com.zootropolis.controller;

import com.zootropolis.dto.DettagliAllarmeDTO;
import com.zootropolis.entity.Mezzo;
import com.zootropolis.repository.MezzoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GestioneSegnalazioni {

    private static final Logger log = LoggerFactory.getLogger(GestioneSegnalazioni.class);
    private final MezzoRepository mezzoRepository;

    public GestioneSegnalazioni(MezzoRepository mezzoRepository) {
        this.mezzoRepository = mezzoRepository;
    }

    // Message: generaAvvisoAnomalia()
    public String generaAvvisoAnomalia(Long idMezzo) {
        log.info("Generazione avviso spostamento anomalo per mezzo ID: {}", idMezzo);
        return "Spostamento non autorizzato rilevato per il veicolo #" + idMezzo;
    }

    // Message: recuperaDettagliAllarme(idMezzo)
    public DettagliAllarmeDTO recuperaDettagliAllarme(Long idMezzo) {
        log.info("Recupero dettagli allarme per mezzo ID: {}", idMezzo);

        // Message: getDatiEPosizione() su :Mezzo -> Return: datiMezzo
        Mezzo mezzo = mezzoRepository.findById(idMezzo)
                .orElseThrow(() -> new IllegalStateException("Dati non recuperabili, mezzo non rintracciabile")); // Sequenza 3.a

        // Self-Message: verificaStatoAnomalia(datiMezzo)
        String statoAnomalia = verificaStatoAnomalia(mezzo);

        if ("PERSO".equalsIgnoreCase(statoAnomalia)) {
            // Sequenza 3.a: erroreNonRintracciabile
            throw new IllegalStateException("Dati non recuperabili, mezzo non rintracciabile");
        }

        if ("RIENTRATO".equalsIgnoreCase(statoAnomalia)) {
            // Sequenza 3.b: allarmeRisolto
            throw new IllegalArgumentException("Allarme non più attivo: mezzo in area consentita");
        }

        // Return: dettagliAllarme (Sequenza Principale)
        return new DettagliAllarmeDTO(
                mezzo.getId(),
                mezzo.getTipo(),
                mezzo.getPosizione(),
                mezzo.getPercentualeBatteria(),
                "Spostamento non autorizzato / Rilevamento GPS Attivo"
        );
    }

    // Self-Message: verificaStatoAnomalia(datiMezzo)
    private String verificaStatoAnomalia(Mezzo mezzo) {
        if (mezzo.getPosizione() == null || mezzo.getPosizione().toLowerCase().contains("sconosciuta")) {
            return "PERSO";
        }
        if (mezzo.getPosizione().toLowerCase().contains("deposito") || mezzo.getPosizione().toLowerCase().contains("stazione")) {
            return "RIENTRATO";
        }
        return "ATTIVA";
    }
}