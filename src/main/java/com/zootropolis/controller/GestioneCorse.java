package com.zootropolis.controller;

import com.zootropolis.dto.CorsaDTO;
import com.zootropolis.dto.PercorsoDTO;
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

    // ==========================================
    // UC-06 SBLOCCARE MEZZO
    // ==========================================

    @Transactional
    public CorsaDTO elaboraSblocco(Long idMezzo, Account utente) {
        log.info("Elaborazione sblocco per mezzo ID: {} da utente ID: {}", idMezzo, utente.getId());

        Mezzo mezzo = mezzoRepository.findById(idMezzo)
                .orElseThrow(() -> new EccezioneMezzoNonDisponibile("Mezzo non trovato"));

        if (!validaStatoVeicolo(mezzo)) {
            throw new EccezioneMezzoNonDisponibile("Impossibile sbloccare: veicolo non noleggiabile");
        }

        if (!validaRequisitiUtente(utente)) {
            throw new ErroreValidazioneException("Requisiti non soddisfatti per il noleggio");
        }

        boolean sbloccoRiuscito = inviaComandoSblocco(idMezzo);
        if (!sbloccoRiuscito) {
            annullaPrenotazioneAttiva(utente);
            mezzo.setStato(true);
            mezzoRepository.save(mezzo);
            throw new RuntimeException("Anomalia tecnica: connessione col veicolo fallita");
        }

        mezzo.setStato(false);
        mezzoRepository.save(mezzo);

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

    private boolean validaStatoVeicolo(Mezzo mezzo) {
        return mezzo != null;
    }

    private boolean validaRequisitiUtente(Account utente) {
        return utente != null && utente.getId() != null;
    }

    private boolean inviaComandoSblocco(Long idMezzo) {
        return true;
    }

    private void annullaPrenotazioneAttiva(Account utente) {
        log.info("Annullamento prenotazione attiva per utente: {}", utente.getId());
    }

    // TODO: METODO DENTRO IL DOCUMENTO
    public List<CorsaDTO> ottieniCorseInCorsoDTO(Long idUtente) {
        return corsaRepository.findByUtenteIdAndOraFineIsNull(idUtente)
                .stream()
                .map(this::convertiInDTO)
                .collect(Collectors.toList());
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

    // ==========================================
    // UC-07 CALCOLARE PERCORSO
    // ==========================================

    public PercorsoDTO elaboraRichiestaPercorso(Long idMezzo, String destinazione, String partenzaManuale) {
        log.info("Elaborazione richiesta percorso verso: {}", destinazione);

        // Self-Message: validaDestinazione(destinazione)
        if (!validaDestinazione(destinazione)) {
            // Return: erroreDestinazione
            throw new IllegalArgumentException("Destinazione non valida o non trovata");
        }

        // Message: getPosizione() su :Mezzo -> Return: posizioneAttuale
        String posizionePartenza = partenzaManuale;
        if (posizionePartenza == null || posizionePartenza.isBlank()) {
            if (idMezzo != null) {
                Mezzo mezzo = mezzoRepository.findById(idMezzo).orElse(null);
                if (mezzo != null) {
                    posizionePartenza = mezzo.getPosizione();
                }
            }
        }

        // Sequenza 4.a: errorePosizioneAssente
        if (posizionePartenza == null || posizionePartenza.isBlank()) {
            throw new IllegalStateException("Impossibile acquisire posizione attuale");
        }

        // Self-Message: calcolaPercorsoOttimale(posizionePartenza, destinazione)
        PercorsoDTO percorso = calcolaPercorsoOttimale(posizionePartenza, destinazione);

        // Sequenza 5.a: errorePercorsoImpossibile
        if (percorso == null) {
            throw new RuntimeException("Impossibile elaborare il percorso verso la destinazione");
        }

        // Return: datiPercorso
        return percorso;
    }

    // Self-Message: validaDestinazione(destinazione)
    private boolean validaDestinazione(String destinazione) {
        return destinazione != null && destinazione.trim().length() >= 3;
    }

    // Self-Message: calcolaPercorsoOttimale(posizionePartenza, destinazione)
    private PercorsoDTO calcolaPercorsoOttimale(String partenza, String destinazione) {
        if (destinazione.equalsIgnoreCase("errore")) {
            return null; // Simula errorePercorsoImpossibile
        }

        int calcoloDistanza = Math.abs(partenza.hashCode() - destinazione.hashCode()) % 15 + 1;
        int tempoStimato = calcoloDistanza * 4;

        return new PercorsoDTO(
                partenza,
                destinazione,
                calcoloDistanza,
                tempoStimato,
                "Procedere lungo la via principale verso " + destinazione
        );
    }

    // Recupera i dettagli di una corsa specifica come DTO
    public CorsaDTO ottieniCorsaDTO(Long idCorsa) {
        return corsaRepository.findById(idCorsa)
                .map(this::convertiInDTO)
                .orElse(null);
    }

    // ==========================================
    // UC-08 TERMINARE CORSA
    // ==========================================

    @Transactional
    public boolean elaboraTermineCorsa(Long idCorsa) {
        log.info("Elaborazione termine corsa per ID Corsa: {}", idCorsa);

        Corsa corsa = corsaRepository.findById(idCorsa)
                .orElseThrow(() -> new IllegalArgumentException("Corsa non trovata"));

        // Message: getIdMezzo() su :Corsa -> Return: idMezzo
        Mezzo mezzo = corsa.getMezzo();
        if (mezzo == null) {
            throw new IllegalStateException("Nessun mezzo associato alla corsa");
        }

        // Message: getPosizione() su :Mezzo -> Return: coordinateAttuali
        String coordinateAttuali = mezzo.getPosizione();

        // Self-Message: verificaAreaSosta(coordinateAttuali)
        if (!verificaAreaSosta(coordinateAttuali)) {
            // Sequenza 3.a: erroreAreaNonConsentita
            throw new IllegalArgumentException("Impossibile terminare: area di sosta non consentita");
        }

        // Self-Message: inviaComandoBloccoFisico(idMezzo)
        boolean bloccoRiuscito = inviaComandoBloccoFisico(mezzo.getId());
        if (!bloccoRiuscito) {
            // Sequenza 4.a: erroreConnessioneHardware
            throw new IllegalStateException("Anomalia tecnica: connessione con il veicolo fallita");
        }

        // Message: setStatoMezzo("BLOCCATO") / setStato(true)
        mezzo.setStato(true); // Mezzo ripristinato e bloccato in attesa di un nuovo noleggio
        mezzoRepository.save(mezzo);

        // Message: setOraFine(oraAttuale)
        corsa.setOraFine(LocalDateTime.now());

        // Message: setStatoCorsa("CONCLUSA")
        corsaRepository.save(corsa);

        // Return: corsaTerminataConSuccesso
        return true;
    }

    // Self-Message: verificaAreaSosta(coordinateAttuali)
    private boolean verificaAreaSosta(String coordinateAttuali) {
        if (coordinateAttuali != null && coordinateAttuali.toLowerCase().contains("vietata")) {
            return false; // Simula area non consentita
        }
        return true;
    }

    // Self-Message: inviaComandoBloccoFisico(idMezzo)
    private boolean inviaComandoBloccoFisico(Long idMezzo) {
        // Ritorna false se l'ID è un valore convenzionale di test hardware (es. 999)
        return idMezzo != 999L;
    }
// ==========================================
    // UC-09 PAGARE
    // ==========================================

    // Message: richiediCalcoloImporto(idCorsa) -> Return: importoTotale
    public Double richiediCalcoloImporto(Long idCorsa) {
        log.info("Calcolo importo per corsa ID: {}", idCorsa);

        Corsa corsa = corsaRepository.findById(idCorsa)
                .orElseThrow(() -> new IllegalArgumentException("Corsa non trovata"));

        Double importo = calcolaImporto(corsa);
        corsa.setImporto(importo);
        corsaRepository.save(corsa);

        return importo;
    }

    // Self-Message: calcolaImporto(datiCorsa)
    private Double calcolaImporto(Corsa corsa) {
        if (corsa.getOraInizio() == null) return 1.50; // Quota minima

        LocalDateTime oraFine = (corsa.getOraFine() != null) ? corsa.getOraFine() : LocalDateTime.now();
        long minuti = java.time.Duration.between(corsa.getOraInizio(), oraFine).toMinutes();

        // Quota fissa sblocco (1.00 €) + 0.20 €/min (minimo 1.50 €)
        double totale = 1.00 + (minuti * 0.20);
        return Math.max(1.50, Math.round(totale * 100.0) / 100.0);
    }

    // Message: elaboraTransazione(importoTotale, metodo)
    @Transactional
    public boolean elaboraTransazione(Long idCorsa, Double importoTotale, String metodo) {
        log.info("Elaborazione transazione di €{} con metodo: {} per corsa ID: {}", importoTotale, metodo, idCorsa);

        Corsa corsa = corsaRepository.findById(idCorsa)
                .orElseThrow(() -> new IllegalArgumentException("Corsa non trovata"));

        // Message: richiediAutorizzazione(importoTotale, metodo) a SistemaPagamentoEsterno
        boolean autorizzata = richiediAutorizzazioneSistemaEsterno(importoTotale, metodo);

        if (!autorizzata) {
            // Sequenza 7.a: esitoNegativo -> erroreTransazione
            throw new IllegalStateException("Impossibile elaborare il pagamento: carta rifiutata o fondi insufficienti");
        }

        // Return: esitoPositivo & setStatoPagamento(true)
        corsa.setImporto(importoTotale);
        corsaRepository.save(corsa);

        return true;
    }

    // Simulazione del SistemaPagamentoEsterno
    private boolean richiediAutorizzazioneSistemaEsterno(Double importo, String metodo) {
        if (metodo != null && (metodo.equalsIgnoreCase("errore") || metodo.equalsIgnoreCase("rifiutata"))) {
            return false; // Simula il rifiuto di pagamento per la Sequenza 7.a
        }
        return true;
    }

}