package com.zootropolis.controller;

import com.zootropolis.dto.DettagliAllarmeDTO;
import com.zootropolis.entity.Mezzo;
import com.zootropolis.repository.MezzoRepository;
import com.zootropolis.repository.SegnalazioneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GestioneSegnalazioni {

    private static final Logger log = LoggerFactory.getLogger(GestioneSegnalazioni.class);

    private final MezzoRepository mezzoRepository;
    private final SegnalazioneRepository segnalazioneRepository;

    public GestioneSegnalazioni(MezzoRepository mezzoRepository, SegnalazioneRepository segnalazioneRepository) {
        this.mezzoRepository = mezzoRepository;
        this.segnalazioneRepository = segnalazioneRepository;
    }

    // Message: generaAvvisoAnomalia()
    public String generaAvvisoAnomalia(Long idMezzo) {
        log.info("Generazione avviso di anomalia spostamento per mezzo ID: {}", idMezzo);
        return "Rilevato spostamento non autorizzato per il veicolo #" + idMezzo;
    }

    // 3. recuperaDettagliAllarme()
    public DettagliAllarmeDTO recuperaDettagliAllarme(Long idMezzo) {
        log.info("Recupero dettagli allarme per mezzo ID: {}", idMezzo);

        // Message: getDatiEPosizione() su :Mezzo -> Return: datiMezzo
        Mezzo mezzo = mezzoRepository.findById(idMezzo)
                .orElseThrow(() -> new IllegalStateException("Dati non recuperabili, mezzo non rintracciabile"));

        // Simula Sequenza 3.a (localizzazione non disponibile per ID 777)
        if (idMezzo != null && idMezzo == 777L) {
            throw new IllegalStateException("Dati non recuperabili, mezzo non rintracciabile");
        }

        // Self-Message: verificaStatoAnomalia(datiMezzo)
        boolean anomaliaRientrata = verificaStatoAnomalia(mezzo);
        if (anomaliaRientrata) {
            // Sequenza 3.b: allarmeRisolto
            throw new IllegalArgumentException("Allarme non più attivo: mezzo in area consentita");
        }

        // Return: dettagliAllarme
        return new DettagliAllarmeDTO(
                mezzo.getId(),
                mezzo.getTipo(),
                mezzo.getPosizione(),
                mezzo.getPercentualeBatteria(),
                "SPOSTAMENTO NON AUTORIZZATO"
        );
    }

    // Self-Message: verificaStatoAnomalia(datiMezzo)
    private boolean verificaStatoAnomalia(Mezzo mezzo) {
        // Simula anomalia rientrata (Sequenza 3.b) se la posizione contiene "autorizzata" o se l'ID è 666
        if (mezzo.getPosizione() != null && mezzo.getPosizione().toLowerCase().contains("autorizzata")) {
            return true;
        }
        return mezzo.getId() != null && mezzo.getId() == 666L;
    }
}