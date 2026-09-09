package com.zootropolis.controller;

import com.zootropolis.dto.MezzoDTO;
import com.zootropolis.entity.Mezzo;
import com.zootropolis.exception.EccezioneMezzoNonDisponibile;
import com.zootropolis.exception.EccezionePosizioneMancante;
import com.zootropolis.repository.MezzoRepository;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GestioneMezzi {

    private static final Logger log = LoggerFactory.getLogger(GestioneMezzi.class);
    private final MezzoRepository mezzoRepository;

    @Value("${app.gps.mock.enabled:false}")
    private boolean gpsMockEnabled;

    @Value("${app.gps.mock.default-location:Via Roma 1, Zootropolis}")
    private String defaultLocation;

    @Getter
    private double raggioRicerca = 1.0;
    @Getter
    private String posizioneAttualeUtente;

    public GestioneMezzi(MezzoRepository mezzoRepository) {
        this.mezzoRepository = mezzoRepository;
    }

    // Message: acquisisciPosizioneERicerca()
    public List<Mezzo> acquisisciPosizioneERicerca() {
        log.info("Avvio acquisizione posizione GPS...");

        // Self-message: rilevaPosizioneGPS()
        boolean gpsRilevato = rilevaPosizioneGPS();

        if (!gpsRilevato) {
            log.warn("Impossibile acquisire posizione GPS.");
            // Return: eccezionePosizioneMancante
            throw new EccezionePosizioneMancante("Impossibile acquisire la posizione automatica");
        }

        return ricercaMezziInternal();
    }

    // Self-message: rilevaPosizioneGPS() -> Legge le proprietà mock da file di config
    private boolean rilevaPosizioneGPS() {
        if (gpsMockEnabled) {
            this.posizioneAttualeUtente = defaultLocation;
            log.info("GPS Mock attivo! Posizione rilevata automaticamente: {}", posizioneAttualeUtente);
            return true;
        }

        // Se non è mockato e l'utente non ha mai inserito un indirizzo
        return posizioneAttualeUtente != null && !posizioneAttualeUtente.isBlank();
    }

    // Message: aggiornaPosizione(indirizzo)
    public List<Mezzo> aggiornaPosizione(String indirizzo) {
        log.info("Aggiornamento posizione manuale a: {}", indirizzo);
        this.posizioneAttualeUtente = indirizzo;
        return ricercaMezziInternal();
    }

    // Message: incrementaRaggioRicerca()
    public List<Mezzo> incrementaRaggioRicerca() {
        this.raggioRicerca += 1.5;
        log.info("Raggio di ricerca incrementato a: {} km", raggioRicerca);
        return ricercaMezziInternal();
    }

    private List<Mezzo> ricercaMezziInternal() {
        List<Mezzo> tuttiIMezzi = mezzoRepository.findAll();

        // Filtra i mezzi invocando getStato() e getPosizione() forniti da Lombok
        List<Mezzo> mezziDisponibili = tuttiIMezzi.stream()
                .filter(m -> Boolean.TRUE.equals(m.getStato())) // getStato()
                .collect(Collectors.toList());

        // Self-message: filtraMezziDisponibiliVicini(raggio)
        return filtraMezziDisponibiliVicini(mezziDisponibili, raggioRicerca);
    }

    // Self-message: filtraMezziDisponibiliVicini(raggio)
    // TODO: Modificare per trovare mezzi alle vicinanze utilizzando il raggio, perche questo metodo ritorna solo i mezzi con lo stato a true.
    private List<Mezzo> filtraMezziDisponibiliVicini(List<Mezzo> mezzi, double raggio) {
        if (posizioneAttualeUtente == null) {
            return new ArrayList<>();
        }
        return mezzi;
    }

    public void resetRicerca() {
        this.raggioRicerca = 1.0;
        this.posizioneAttualeUtente = null;
    }

    public List<Mezzo> resetRaggioRicerca() {
        this.raggioRicerca = 1.0;
        log.info("Raggio di ricerca resettato al valore predefinito: {} km", raggioRicerca);
        return ricercaMezziInternal();
    }

    // Message: richiediDettagli(idMezzo)
    public Mezzo richiediDettagli(Long idMezzo) {
        log.info("Richiesta dettagli per il mezzo ID: {}", idMezzo);

        // Message: getDettagli() -> Return: datiMezzo
        Mezzo mezzo = mezzoRepository.findById(idMezzo)
                .orElseThrow(() -> new EccezioneMezzoNonDisponibile("Mezzo non trovato nel sistema"));

        // Self-message: verificaDisponibilita(datiMezzo)
        boolean disponibile = verificaDisponibilita(mezzo);

        if (!disponibile) {
            log.warn("Mezzo ID {} non disponibile per il noleggio.", idMezzo);
            // Return: erroreNonDisponibile
            throw new EccezioneMezzoNonDisponibile("Veicolo non più disponibile");
        }

        return mezzo;
    }

    // Self-message: verificaDisponibilita(datiMezzo)
    private boolean verificaDisponibilita(Mezzo mezzo) {
        // Un mezzo è disponibile se lo stato è true e la batteria è > 10%
        return Boolean.TRUE.equals(mezzo.getStato()) &&
                (mezzo.getPercentualeBatteria() == null || mezzo.getPercentualeBatteria() > 10);
    }

    public MezzoDTO convertiInDTO(Mezzo mezzo) {
        if (mezzo == null) return null;

        MezzoDTO dto = new MezzoDTO();
        dto.setId(mezzo.getId());
        dto.setTipo(mezzo.getTipo());
        dto.setPercentualeBatteria(mezzo.getPercentualeBatteria());
        dto.setStato(mezzo.getStato());
        dto.getPosizione();
        dto.setPosizione(mezzo.getPosizione());

        if (mezzo.getOperatore() != null) {
            dto.setIdOperatore(mezzo.getOperatore().getId());
        }
        return dto;
    }
    public List<MezzoDTO> acquisisciPosizioneERicercaDTO() {
        List<Mezzo> mezziEntity = acquisisciPosizioneERicerca();
        return mezziEntity.stream()
                .map(this::convertiInDTO)
                .collect(Collectors.toList());
    }

    public MezzoDTO richiediDettagliDTO(Long idMezzo) {
        Mezzo mezzoEntity = richiediDettagli(idMezzo);
        return convertiInDTO(mezzoEntity);
    }
}