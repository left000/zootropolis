package com.zootropolis.controller;

import com.zootropolis.dto.CorsaDTO;
import com.zootropolis.entity.Account;
import com.zootropolis.entity.Corsa;
import com.zootropolis.entity.Mezzo;
import com.zootropolis.entity.Utente;
import com.zootropolis.exception.EccezioneMezzoNonDisponibile;
import com.zootropolis.exception.ErroreValidazioneException;
import com.zootropolis.repository.CorsaRepository;
import com.zootropolis.repository.MezzoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GestioneCorse {

    private static final Logger log = LoggerFactory.getLogger(GestioneCorse.class);

    private final MezzoRepository mezzoRepository;
    private final CorsaRepository corsaRepository;

    public GestioneCorse(MezzoRepository mezzoRepository, CorsaRepository corsaRepository) {
        this.mezzoRepository = mezzoRepository;
        this.corsaRepository = corsaRepository;
    }

    // 2. elaboraSblocco(idMezzo, idUtente)
    @Transactional
    public CorsaDTO elaboraSblocco(Long idMezzo, Account utente) {
        log.info("Elaborazione sblocco per mezzo ID: {} da utente ID: {}", idMezzo, utente.getId());

        // getStato()
        Mezzo mezzo = mezzoRepository.findById(idMezzo)
                .orElseThrow(() -> new EccezioneMezzoNonDisponibile("Mezzo non trovato"));

        // validaStatoVeicolo(statoMezzo)
        if (!validaStatoVeicolo(mezzo)) {
            // Sequenza 2.a: erroreVeicoloNonNoleggiabile
            throw new EccezioneMezzoNonDisponibile("Impossibile sbloccare: veicolo non noleggiabile");
        }

        // validaRequisitiUtente(idUtente)
        if (!validaRequisitiUtente(utente)) {
            // Sequenza 2.b: erroreRequisitiMancanti
            throw new ErroreValidazioneException("Requisiti non soddisfatti per il noleggio");
        }

        // inviaComandoSblocco(idMezzo)
        boolean sbloccoRiuscito = inviaComandoSblocco(idMezzo);
        if (!sbloccoRiuscito) {
            // Sequenza 3.a: erroreTimeoutConnessione
            annullaPrenotazioneAttiva(utente);
            mezzo.setStato(true); // setStatoMezzo("DISPONIBILE")
            mezzoRepository.save(mezzo);
            throw new RuntimeException("Anomalia tecnica: connessione col veicolo fallita");
        }

        // OK (Sequenza Principale)
        mezzo.setStato(false); // setStatoMezzo("IN_USO")
        mezzoRepository.save(mezzo);

        // create(idUtente, idMezzo, oraAttuale) -> nuovaCorsa
        Corsa corsa = new Corsa();
        if (utente instanceof Utente) {
            corsa.setUtente((Utente) utente);
        }
        corsa.setMezzo(mezzo);
        corsa.setOraInizio(LocalDateTime.now());
        corsa.setAreaAttuale(mezzo.getPosizione());
        corsaRepository.save(corsa);

        return convertiInDTO(corsa);
    }

    // Self-messages
    private boolean validaStatoVeicolo(Mezzo mezzo) {
        return mezzo != null;
    }

    private boolean validaRequisitiUtente(Account utente) {
        return utente != null && utente.getId() != null;
    }

    private boolean inviaComandoSblocco(Long idMezzo) {
        // Simulazione invio comando hardware al mezzo (ritorna true per esito positivo)
        return true;
    }

    private void annullaPrenotazioneAttiva(Account utente) {
        log.info("Annullamento prenotazione attiva per utente: {}", utente.getId());
    }

    public CorsaDTO convertiInDTO(Corsa corsa) {
        if (corsa == null) return null;
        CorsaDTO dto = new CorsaDTO();
        dto.setId(corsa.getId());
        dto.setOraInizio(corsa.getOraInizio());
        dto.setOraFine(corsa.getOraFine());
        dto.setImporto(corsa.getImporto());
        dto.setAreaAttuale(corsa.getAreaAttuale());
        if (corsa.getUtente() != null) dto.setIdUtente(corsa.getUtente().getId());
        if (corsa.getMezzo() != null) dto.setIdMezzo(corsa.getMezzo().getId());
        if (corsa.getPrenotazione() != null) dto.setIdPrenotazione(corsa.getPrenotazione().getId());
        return dto;
    }

    // TODO: METODO DENTRO IL DOCUMENTO
    // Recupera tutte le corse in corso dell'utente come DTO
    public List<CorsaDTO> ottieniCorseInCorsoDTO(Long idUtente) {
        return corsaRepository.findByUtenteIdAndOraFineIsNull(idUtente)
                .stream()
                .map(this::convertiInDTO)
                .collect(Collectors.toList());
    }
}