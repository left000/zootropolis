////package com.zootropolis.controller;
////
////import com.zootropolis.dto.CorsaDTO;
////import com.zootropolis.dto.PercorsoDTO;
////import com.zootropolis.entity.Account;
////import com.zootropolis.entity.Corsa;
////import com.zootropolis.entity.Mezzo;
////import com.zootropolis.entity.Utente;
////import com.zootropolis.exception.EccezioneMezzoNonDisponibile;
////import com.zootropolis.exception.ErroreValidazioneException;
////import com.zootropolis.repository.CorsaRepository;
////import com.zootropolis.repository.MezzoRepository;
////import org.slf4j.Logger;
////import org.slf4j.LoggerFactory;
////import org.springframework.stereotype.Service;
////import org.springframework.transaction.annotation.Transactional;
////
////import java.time.LocalDateTime;
////import java.util.ArrayList;
////import java.util.List;
////import java.util.stream.Collectors;
////
////@Service
////public class GestioneCorse {
////
////    private static final Logger log = LoggerFactory.getLogger(GestioneCorse.class);
////
////    private final MezzoRepository mezzoRepository;
////    private final CorsaRepository corsaRepository;
////
////    public GestioneCorse(MezzoRepository mezzoRepository, CorsaRepository corsaRepository) {
////        this.mezzoRepository = mezzoRepository;
////        this.corsaRepository = corsaRepository;
////    }
////
////    // ==========================================
////    // UC-06 SBLOCCARE MEZZO
////    // ==========================================
////
////    @Transactional
////    public CorsaDTO elaboraSblocco(Long idMezzo, Account utente) {
////        log.info("Elaborazione sblocco per mezzo ID: {} da utente ID: {}", idMezzo, utente.getId());
////
////        Mezzo mezzo = mezzoRepository.findById(idMezzo)
////                .orElseThrow(() -> new EccezioneMezzoNonDisponibile("Mezzo non trovato"));
////
////        if (!validaStatoVeicolo(mezzo)) {
////            throw new EccezioneMezzoNonDisponibile("Impossibile sbloccare: veicolo non noleggiabile");
////        }
////
////        if (!validaRequisitiUtente(utente)) {
////            throw new ErroreValidazioneException("Requisiti non soddisfatti per il noleggio");
////        }
////
////        boolean sbloccoRiuscito = inviaComandoSblocco(idMezzo);
////        if (!sbloccoRiuscito) {
////            annullaPrenotazioneAttiva(utente);
////            mezzo.setStato(true);
////            mezzoRepository.save(mezzo);
////            throw new RuntimeException("Anomalia tecnica: connessione col veicolo fallita");
////        }
////
////        mezzo.setStato(false);
////        mezzoRepository.save(mezzo);
////
////        Corsa corsa = new Corsa();
////        if (utente instanceof Utente) {
////            corsa.setUtente((Utente) utente);
////        }
////        corsa.setMezzo(mezzo);
////        corsa.setOraInizio(LocalDateTime.now());
////        corsa.setAreaAttuale(mezzo.getPosizione());
////        corsaRepository.save(corsa);
////
////        return convertiInDTO(corsa);
////    }
////
////    private boolean validaStatoVeicolo(Mezzo mezzo) {
////        return mezzo != null;
////    }
////
////    private boolean validaRequisitiUtente(Account utente) {
////        return utente != null && utente.getId() != null;
////    }
////
////    private boolean inviaComandoSblocco(Long idMezzo) {
////        return true;
////    }
////
////    private void annullaPrenotazioneAttiva(Account utente) {
////        log.info("Annullamento prenotazione attiva per utente: {}", utente.getId());
////    }
////
////    // TODO: METODO DENTRO IL DOCUMENTO
////    public List<CorsaDTO> ottieniCorseInCorsoDTO(Long idUtente) {
////        return corsaRepository.findByUtenteIdAndOraFineIsNull(idUtente)
////                .stream()
////                .map(this::convertiInDTO)
////                .collect(Collectors.toList());
////    }
////
////    public CorsaDTO convertiInDTO(Corsa corsa) {
////        if (corsa == null) return null;
////        CorsaDTO dto = new CorsaDTO();
////        dto.setId(corsa.getId());
////        dto.setOraInizio(corsa.getOraInizio());
////        dto.setOraFine(corsa.getOraFine());
////        dto.setImporto(corsa.getImporto());
////        dto.setAreaAttuale(corsa.getAreaAttuale());
////        if (corsa.getUtente() != null) dto.setIdUtente(corsa.getUtente().getId());
////        if (corsa.getMezzo() != null) dto.setIdMezzo(corsa.getMezzo().getId());
////        if (corsa.getPrenotazione() != null) dto.setIdPrenotazione(corsa.getPrenotazione().getId());
////        return dto;
////    }
////
////    // ==========================================
////    // UC-07 CALCOLARE PERCORSO
////    // ==========================================
////
////    public PercorsoDTO elaboraRichiestaPercorso(Long idMezzo, String destinazione, String partenzaManuale) {
////        log.info("Elaborazione richiesta percorso verso: {}", destinazione);
////
////        // Self-Message: validaDestinazione(destinazione)
////        if (!validaDestinazione(destinazione)) {
////            // Return: erroreDestinazione
////            throw new IllegalArgumentException("Destinazione non valida o non trovata");
////        }
////
////        // Message: getPosizione() su :Mezzo -> Return: posizioneAttuale
////        String posizionePartenza = partenzaManuale;
////        if (posizionePartenza == null || posizionePartenza.isBlank()) {
////            if (idMezzo != null) {
////                Mezzo mezzo = mezzoRepository.findById(idMezzo).orElse(null);
////                if (mezzo != null) {
////                    posizionePartenza = mezzo.getPosizione();
////                }
////            }
////        }
////
////        // Sequenza 4.a: errorePosizioneAssente
////        if (posizionePartenza == null || posizionePartenza.isBlank()) {
////            throw new IllegalStateException("Impossibile acquisire posizione attuale");
////        }
////
////        // Self-Message: calcolaPercorsoOttimale(posizionePartenza, destinazione)
////        PercorsoDTO percorso = calcolaPercorsoOttimale(posizionePartenza, destinazione);
////
////        // Sequenza 5.a: errorePercorsoImpossibile
////        if (percorso == null) {
////            throw new RuntimeException("Impossibile elaborare il percorso verso la destinazione");
////        }
////
////        // Return: datiPercorso
////        return percorso;
////    }
////
////    // Self-Message: validaDestinazione(destinazione)
////    private boolean validaDestinazione(String destinazione) {
////        return destinazione != null && destinazione.trim().length() >= 3;
////    }
////
////    // Self-Message: calcolaPercorsoOttimale(posizionePartenza, destinazione)
////    private PercorsoDTO calcolaPercorsoOttimale(String partenza, String destinazione) {
////        if (destinazione.equalsIgnoreCase("errore")) {
////            return null; // Simula errorePercorsoImpossibile
////        }
////
////        int calcoloDistanza = Math.abs(partenza.hashCode() - destinazione.hashCode()) % 15 + 1;
////        int tempoStimato = calcoloDistanza * 4;
////
////        return new PercorsoDTO(
////                partenza,
////                destinazione,
////                calcoloDistanza,
////                tempoStimato,
////                "Procedere lungo la via principale verso " + destinazione
////        );
////    }
////
////    // Recupera i dettagli di una corsa specifica come DTO
////    public CorsaDTO ottieniCorsaDTO(Long idCorsa) {
////        return corsaRepository.findById(idCorsa)
////                .map(this::convertiInDTO)
////                .orElse(null);
////    }
////
////    // ==========================================
////    // UC-08 TERMINARE CORSA
////    // ==========================================
////
////    @Transactional
////    public boolean elaboraTermineCorsa(Long idCorsa) {
////        log.info("Elaborazione termine corsa per ID Corsa: {}", idCorsa);
////
////        Corsa corsa = corsaRepository.findById(idCorsa)
////                .orElseThrow(() -> new IllegalArgumentException("Corsa non trovata"));
////
////        // Message: getIdMezzo() su :Corsa -> Return: idMezzo
////        Mezzo mezzo = corsa.getMezzo();
////        if (mezzo == null) {
////            throw new IllegalStateException("Nessun mezzo associato alla corsa");
////        }
////
////        // Message: getPosizione() su :Mezzo -> Return: coordinateAttuali
////        String coordinateAttuali = mezzo.getPosizione();
////
////        // Self-Message: verificaAreaSosta(coordinateAttuali)
////        if (!verificaAreaSosta(coordinateAttuali)) {
////            // Sequenza 3.a: erroreAreaNonConsentita
////            throw new IllegalArgumentException("Impossibile terminare: area di sosta non consentita");
////        }
////
////        // Self-Message: inviaComandoBloccoFisico(idMezzo)
////        boolean bloccoRiuscito = inviaComandoBloccoFisico(mezzo.getId());
////        if (!bloccoRiuscito) {
////            // Sequenza 4.a: erroreConnessioneHardware
////            throw new IllegalStateException("Anomalia tecnica: connessione con il veicolo fallita");
////        }
////
////        // Message: setStatoMezzo("BLOCCATO") / setStato(true)
////        mezzo.setStato(true); // Mezzo ripristinato e bloccato in attesa di un nuovo noleggio
////        mezzoRepository.save(mezzo);
////
////        // Message: setOraFine(oraAttuale)
////        corsa.setOraFine(LocalDateTime.now());
////
////        // Message: setStatoCorsa("CONCLUSA")
////        corsaRepository.save(corsa);
////
////        // Return: corsaTerminataConSuccesso
////        return true;
////    }
////
////    // Self-Message: verificaAreaSosta(coordinateAttuali)
////    private boolean verificaAreaSosta(String coordinateAttuali) {
////        if (coordinateAttuali != null && coordinateAttuali.toLowerCase().contains("vietata")) {
////            return false; // Simula area non consentita
////        }
////        return true;
////    }
////
////    // Self-Message: inviaComandoBloccoFisico(idMezzo)
////    private boolean inviaComandoBloccoFisico(Long idMezzo) {
////        // Ritorna false se l'ID è un valore convenzionale di test hardware (es. 999)
////        return idMezzo != 999L;
////    }
////// ==========================================
////    // UC-09 PAGARE
////    // ==========================================
////
////    // Message: richiediCalcoloImporto(idCorsa) -> Return: importoTotale
////    public Double richiediCalcoloImporto(Long idCorsa) {
////        log.info("Calcolo importo per corsa ID: {}", idCorsa);
////
////        Corsa corsa = corsaRepository.findById(idCorsa)
////                .orElseThrow(() -> new IllegalArgumentException("Corsa non trovata"));
////
////        Double importo = calcolaImporto(corsa);
////        corsa.setImporto(importo);
////        corsaRepository.save(corsa);
////
////        return importo;
////    }
////
////    // Self-Message: calcolaImporto(datiCorsa)
////    private Double calcolaImporto(Corsa corsa) {
////        if (corsa.getOraInizio() == null) return 1.50; // Quota minima
////
////        LocalDateTime oraFine = (corsa.getOraFine() != null) ? corsa.getOraFine() : LocalDateTime.now();
////        long minuti = java.time.Duration.between(corsa.getOraInizio(), oraFine).toMinutes();
////
////        // Quota fissa sblocco (1.00 €) + 0.20 €/min (minimo 1.50 €)
////        double totale = 1.00 + (minuti * 0.20);
////        return Math.max(1.50, Math.round(totale * 100.0) / 100.0);
////    }
////
////    // Message: elaboraTransazione(importoTotale, metodo)
////    @Transactional
////    public boolean elaboraTransazione(Long idCorsa, Double importoTotale, String metodo) {
////        log.info("Elaborazione transazione di €{} con metodo: {} per corsa ID: {}", importoTotale, metodo, idCorsa);
////
////        Corsa corsa = corsaRepository.findById(idCorsa)
////                .orElseThrow(() -> new IllegalArgumentException("Corsa non trovata"));
////
////        // Message: richiediAutorizzazione(importoTotale, metodo) a SistemaPagamentoEsterno
////        boolean autorizzata = richiediAutorizzazioneSistemaEsterno(importoTotale, metodo);
////
////        if (!autorizzata) {
////            // Sequenza 7.a: esitoNegativo -> erroreTransazione
////            throw new IllegalStateException("Impossibile elaborare il pagamento: carta rifiutata o fondi insufficienti");
////        }
////
////        // Return: esitoPositivo & setStatoPagamento(true)
////        corsa.setImporto(importoTotale);
////        corsaRepository.save(corsa);
////
////        return true;
////    }
////
////    // Simulazione del SistemaPagamentoEsterno
////    private boolean richiediAutorizzazioneSistemaEsterno(Double importo, String metodo) {
////        if (metodo != null && (metodo.equalsIgnoreCase("errore") || metodo.equalsIgnoreCase("rifiutata"))) {
////            return false; // Simula il rifiuto di pagamento per la Sequenza 7.a
////        }
////        return true;
////    }
////
////    // ==========================================
////    // UC-10 VISUALIZZARE STORICO
////    // ==========================================
////
////    // Message: richiediCorseConcluse(idUtente)
////    public List<CorsaDTO> richiediCorseConcluse(Long idUtente) {
////        log.info("Recupero storico corse per utente ID: {}", idUtente);
////
////        // Message: getCorseUtente(idUtente) su :Corsa -> Return: listaCorse
////        List<Corsa> corseTrovate = corsaRepository.findByUtenteIdAndOraFineIsNotNull(idUtente);
////
////        if (corseTrovate.isEmpty()) {
////            // Sequenza 2.a: listaVuota
////            return new ArrayList<>();
////        }
////
////        // Self-Message: ordinaCronologicamente(listaCorse)
////        ordinaCronologicamente(corseTrovate);
////
////        // Return: storicoCorse (convertiti in DTO)
////        return corseTrovate.stream()
////                .map(this::convertiInDTO)
////                .collect(Collectors.toList());
////    }
////
////    // Self-Message: ordinaCronologicamente(listaCorse)
////    private void ordinaCronologicamente(List<Corsa> listaCorse) {
////        listaCorse.sort((c1, c2) -> {
////            if (c1.getOraInizio() == null || c2.getOraInizio() == null) return 0;
////            return c2.getOraInizio().compareTo(c1.getOraInizio()); // Ordine decrescente (più recenti prima)
////        });
////    }
////}
//package com.zootropolis.controller;
//
//import com.zootropolis.dto.CorsaDTO;
//import com.zootropolis.dto.PercorsoDTO;
//import com.zootropolis.entity.Account;
//import com.zootropolis.entity.Corsa;
//import com.zootropolis.entity.Mezzo;
//import com.zootropolis.entity.Prenotazione;
//import com.zootropolis.entity.Utente;
//import com.zootropolis.exception.EccezioneMezzoNonDisponibile;
//import com.zootropolis.exception.ErroreValidazioneException;
//import com.zootropolis.repository.CorsaRepository;
//import com.zootropolis.repository.MezzoRepository;
//import com.zootropolis.repository.PrenotazioneRepository;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Service
//public class GestioneCorse {
//
//    private static final Logger log = LoggerFactory.getLogger(GestioneCorse.class);
//
//    private final MezzoRepository mezzoRepository;
//    private final CorsaRepository corsaRepository;
//    private final PrenotazioneRepository prenotazioneRepository;
//
//    public GestioneCorse(MezzoRepository mezzoRepository, CorsaRepository corsaRepository, PrenotazioneRepository prenotazioneRepository) {
//        this.mezzoRepository = mezzoRepository;
//        this.corsaRepository = corsaRepository;
//        this.prenotazioneRepository = prenotazioneRepository;
//    }
//
//    // ==========================================
//    // UC-06 SBLOCCARE MEZZO
//    // ==========================================
//
//    @Transactional
//    public CorsaDTO elaboraSblocco(Long idMezzo, Account utente) {
//        log.info("Elaborazione sblocco per mezzo ID: {} da utente ID: {}", idMezzo, utente.getId());
//
//        // 1. Controlla se la corsa per questo utente e questo mezzo è GIÀ in corso (Evita duplicazione record)
//        List<Corsa> corseInCorso = corsaRepository.findByUtenteIdAndOraFineIsNull(utente.getId());
//        Optional<Corsa> corsaGiaAttiva = corseInCorso.stream()
//                .filter(c -> c.getMezzo() != null && c.getMezzo().getId().equals(idMezzo))
//                .findFirst();
//
//        if (corsaGiaAttiva.isPresent()) {
//            log.info("Corsa già attiva rilevata (ID: {}). Ritorno corsa esistente.", corsaGiaAttiva.get().getId());
//            return convertiInDTO(corsaGiaAttiva.get());
//        }
//
//        Mezzo mezzo = mezzoRepository.findById(idMezzo)
//                .orElseThrow(() -> new EccezioneMezzoNonDisponibile("Mezzo non trovato"));
//
//        if (!validaRequisitiUtente(utente)) {
//            throw new ErroreValidazioneException("Requisiti non soddisfatti per il noleggio");
//        }
//
//        // Recupera ed estingue la prenotazione attiva per questo mezzo/utente
//        Optional<Prenotazione> prenotazioneOpt = prenotazioneRepository.findByUtenteIdAndStatoTrue(utente.getId())
//                .stream()
//                .filter(p -> p.getMezzo() != null && p.getMezzo().getId().equals(idMezzo))
//                .findFirst();
//
//        boolean sbloccoRiuscito = inviaComandoSblocco(idMezzo);
//        if (!sbloccoRiuscito) {
//            annullaPrenotazioneAttiva(utente);
//            mezzo.setStato(true);
//            mezzoRepository.save(mezzo);
//            throw new RuntimeException("Anomalia tecnica: connessione col veicolo fallita");
//        }
//
//        // Il mezzo rimane occupato durante la corsa
//        mezzo.setStato(false);
//        mezzoRepository.save(mezzo);
//
//        Corsa corsa = new Corsa();
//        if (utente instanceof Utente) {
//            corsa.setUtente((Utente) utente);
//        }
//        corsa.setMezzo(mezzo);
//        corsa.setOraInizio(LocalDateTime.now());
//        corsa.setAreaAttuale(mezzo.getPosizione());
//
//        // Collega la prenotazione se presente e la chiude
//        if (prenotazioneOpt.isPresent()) {
//            Prenotazione prenotazione = prenotazioneOpt.get();
//            prenotazione.setStato(false); // Disattiva la prenotazione (non comparirà più tra le prenotazioni attive)
//            prenotazioneRepository.save(prenotazione);
//            corsa.setPrenotazione(prenotazione); // Inserisce l'ID della prenotazione nel record della Corsa
//        }
//
//        corsaRepository.save(corsa);
//
//        return convertiInDTO(corsa);
//    }
//
//    private boolean validaStatoVeicolo(Mezzo mezzo) {
//        return mezzo != null;
//    }
//
//    private boolean validaRequisitiUtente(Account utente) {
//        return utente != null && utente.getId() != null;
//    }
//
//    private boolean inviaComandoSblocco(Long idMezzo) {
//        return true;
//    }
//
//    private void annullaPrenotazioneAttiva(Account utente) {
//        log.info("Annullamento prenotazione attiva per utente: {}", utente.getId());
//    }
//
//    public List<CorsaDTO> ottieniCorseInCorsoDTO(Long idUtente) {
//        return corsaRepository.findByUtenteIdAndOraFineIsNull(idUtente)
//                .stream()
//                .map(this::convertiInDTO)
//                .collect(Collectors.toList());
//    }
//
//    public CorsaDTO convertiInDTO(Corsa corsa) {
//        if (corsa == null) return null;
//        CorsaDTO dto = new CorsaDTO();
//        dto.setId(corsa.getId());
//        dto.setOraInizio(corsa.getOraInizio());
//        dto.setOraFine(corsa.getOraFine());
//        dto.setImporto(corsa.getImporto());
//        dto.setAreaAttuale(corsa.getAreaAttuale());
//        if (corsa.getUtente() != null) dto.setIdUtente(corsa.getUtente().getId());
//        if (corsa.getMezzo() != null) dto.setIdMezzo(corsa.getMezzo().getId());
//        if (corsa.getPrenotazione() != null) dto.setIdPrenotazione(corsa.getPrenotazione().getId());
//        return dto;
//    }
//
//    // ==========================================
//    // UC-07 CALCOLARE PERCORSO
//    // ==========================================
//
//    public PercorsoDTO elaboraRichiestaPercorso(Long idMezzo, String destinazione, String partenzaManuale) {
//        log.info("Elaborazione richiesta percorso verso: {}", destinazione);
//
//        if (!validaDestinazione(destinazione)) {
//            throw new IllegalArgumentException("Destinazione non valida o non trovata");
//        }
//
//        String posizionePartenza = partenzaManuale;
//        if (posizionePartenza == null || posizionePartenza.isBlank()) {
//            if (idMezzo != null) {
//                Mezzo mezzo = mezzoRepository.findById(idMezzo).orElse(null);
//                if (mezzo != null) {
//                    posizionePartenza = mezzo.getPosizione();
//                }
//            }
//        }
//
//        if (posizionePartenza == null || posizionePartenza.isBlank()) {
//            throw new IllegalStateException("Impossibile acquisire posizione attuale");
//        }
//
//        PercorsoDTO percorso = calcolaPercorsoOttimale(posizionePartenza, destinazione);
//
//        if (percorso == null) {
//            throw new RuntimeException("Impossibile elaborare il percorso verso la destinazione");
//        }
//
//        return percorso;
//    }
//
//    private boolean validaDestinazione(String destinazione) {
//        return destinazione != null && destinazione.trim().length() >= 3;
//    }
//
//    private PercorsoDTO calcolaPercorsoOttimale(String partenza, String destinazione) {
//        if (destinazione.equalsIgnoreCase("errore")) {
//            return null;
//        }
//
//        int calcoloDistanza = Math.abs(partenza.hashCode() - destinazione.hashCode()) % 15 + 1;
//        int tempoStimato = calcoloDistanza * 4;
//
//        return new PercorsoDTO(
//                partenza,
//                destinazione,
//                calcoloDistanza,
//                tempoStimato,
//                "Procedere lungo la via principale verso " + destinazione
//        );
//    }
//
//    public CorsaDTO ottieniCorsaDTO(Long idCorsa) {
//        return corsaRepository.findById(idCorsa)
//                .map(this::convertiInDTO)
//                .orElse(null);
//    }
//
//    // ==========================================
//    // UC-08 TERMINARE CORSA
//    // ==========================================
//
//    @Transactional
//    public boolean elaboraTermineCorsa(Long idCorsa) {
//        log.info("Elaborazione termine corsa per ID Corsa: {}", idCorsa);
//
//        Corsa corsa = corsaRepository.findById(idCorsa)
//                .orElseThrow(() -> new IllegalArgumentException("Corsa non trovata"));
//
//        Mezzo mezzo = corsa.getMezzo();
//        if (mezzo == null) {
//            throw new IllegalStateException("Nessun mezzo associato alla corsa");
//        }
//
//        String coordinateAttuali = mezzo.getPosizione();
//
//        if (!verificaAreaSosta(coordinateAttuali)) {
//            throw new IllegalArgumentException("Impossibile terminare: area di sosta non consentita");
//        }
//
//        boolean bloccoRiuscito = inviaComandoBloccoFisico(mezzo.getId());
//        if (!bloccoRiuscito) {
//            throw new IllegalStateException("Anomalia tecnica: connessione con il veicolo fallita");
//        }
//
//        mezzo.setStato(true);
//        mezzoRepository.save(mezzo);
//
//        corsa.setOraFine(LocalDateTime.now());
//        corsaRepository.save(corsa);
//
//        return true;
//    }
//
//    private boolean verificaAreaSosta(String coordinateAttuali) {
//        if (coordinateAttuali != null && coordinateAttuali.toLowerCase().contains("vietata")) {
//            return false;
//        }
//        return true;
//    }
//
//    private boolean inviaComandoBloccoFisico(Long idMezzo) {
//        return idMezzo != 999L;
//    }
//
//    // ==========================================
//    // UC-09 PAGARE
//    // ==========================================
//
//    public Double richiediCalcoloImporto(Long idCorsa) {
//        log.info("Calcolo importo per corsa ID: {}", idCorsa);
//
//        Corsa corsa = corsaRepository.findById(idCorsa)
//                .orElseThrow(() -> new IllegalArgumentException("Corsa non trovata"));
//
//        Double importo = calcolaImporto(corsa);
//        corsa.setImporto(importo);
//        corsaRepository.save(corsa);
//
//        return importo;
//    }
//
//    private Double calcolaImporto(Corsa corsa) {
//        if (corsa.getOraInizio() == null) return 1.50;
//
//        LocalDateTime oraFine = (corsa.getOraFine() != null) ? corsa.getOraFine() : LocalDateTime.now();
//        long minuti = java.time.Duration.between(corsa.getOraInizio(), oraFine).toMinutes();
//
//        double totale = 1.00 + (minuti * 0.20);
//        return Math.max(1.50, Math.round(totale * 100.0) / 100.0);
//    }
//
//    @Transactional
//    public boolean elaboraTransazione(Long idCorsa, Double importoTotale, String metodo) {
//        log.info("Elaborazione transazione di €{} con metodo: {} per corsa ID: {}", importoTotale, metodo, idCorsa);
//
//        Corsa corsa = corsaRepository.findById(idCorsa)
//                .orElseThrow(() -> new IllegalArgumentException("Corsa non trovata"));
//
//        boolean autorizzata = richiediAutorizzazioneSistemaEsterno(importoTotale, metodo);
//
//        if (!autorizzata) {
//            throw new IllegalStateException("Impossibile elaborare il pagamento: carta rifiutata o fondi insufficienti");
//        }
//
//        corsa.setImporto(importoTotale);
//        corsaRepository.save(corsa);
//
//        return true;
//    }
//
//    private boolean richiediAutorizzazioneSistemaEsterno(Double importo, String metodo) {
//        if (metodo != null && (metodo.equalsIgnoreCase("errore") || metodo.equalsIgnoreCase("rifiutata"))) {
//            return false;
//        }
//        return true;
//    }
//
//    // ==========================================
//    // UC-10 VISUALIZZARE STORICO
//    // ==========================================
//
//    public List<CorsaDTO> richiediCorseConcluse(Long idUtente) {
//        log.info("Recupero storico corse per utente ID: {}", idUtente);
//
//        List<Corsa> corseTrovate = corsaRepository.findByUtenteIdAndOraFineIsNotNull(idUtente);
//
//        if (corseTrovate.isEmpty()) {
//            return new ArrayList<>();
//        }
//
//        ordinaCronologicamente(corseTrovate);
//
//        return corseTrovate.stream()
//                .map(this::convertiInDTO)
//                .collect(Collectors.toList());
//    }
//
//    private void ordinaCronologicamente(List<Corsa> listaCorse) {
//        listaCorse.sort((c1, c2) -> {
//            if (c1.getOraInizio() == null || c2.getOraInizio() == null) return 0;
//            return c2.getOraInizio().compareTo(c1.getOraInizio());
//        });
//    }
//}

package com.zootropolis.controller;

import com.zootropolis.dto.CorsaDTO;
import com.zootropolis.dto.PercorsoDTO;
import com.zootropolis.entity.Account;
import com.zootropolis.entity.Corsa;
import com.zootropolis.entity.Mezzo;
import com.zootropolis.entity.Prenotazione;
import com.zootropolis.entity.Utente;
import com.zootropolis.exception.EccezioneMezzoNonDisponibile;
import com.zootropolis.exception.ErroreValidazioneException;
import com.zootropolis.repository.CorsaRepository;
import com.zootropolis.repository.MezzoRepository;
import com.zootropolis.repository.PrenotazioneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GestioneCorse {

    private static final Logger log = LoggerFactory.getLogger(GestioneCorse.class);

    private final MezzoRepository mezzoRepository;
    private final CorsaRepository corsaRepository;
    private final PrenotazioneRepository prenotazioneRepository;

    public GestioneCorse(MezzoRepository mezzoRepository, CorsaRepository corsaRepository, PrenotazioneRepository prenotazioneRepository) {
        this.mezzoRepository = mezzoRepository;
        this.corsaRepository = corsaRepository;
        this.prenotazioneRepository = prenotazioneRepository;
    }

    // ==========================================
    // UC-06 SBLOCCARE MEZZO
    // ==========================================

    @Transactional
    public CorsaDTO elaboraSblocco(Long idMezzo, Account utente) {
        log.info("Elaborazione sblocco per mezzo ID: {} da utente ID: {}", idMezzo, utente.getId());

        // Idempotenza: controlla se c'è già una corsa attiva
        List<Corsa> corseInCorso = corsaRepository.findByUtenteIdAndOraFineIsNull(utente.getId());
        Optional<Corsa> corsaGiaAttiva = corseInCorso.stream()
                .filter(c -> c.getMezzo() != null && c.getMezzo().getId().equals(idMezzo))
                .findFirst();

        if (corsaGiaAttiva.isPresent()) {
            return convertiInDTO(corsaGiaAttiva.get());
        }

        Mezzo mezzo = mezzoRepository.findById(idMezzo)
                .orElseThrow(() -> new EccezioneMezzoNonDisponibile("Impossibile sbloccare: veicolo non noleggiabile"));

        // Message: getStato() + Self-Message: validaStatoVeicolo(statoMezzo)
        Boolean statoMezzo = mezzo.getStato();
        if (!validaStatoVeicolo(statoMezzo)) {
            // Sequenza 2.a: erroreVeicoloNonNoleggiabile
            throw new EccezioneMezzoNonDisponibile("Impossibile sbloccare: veicolo non noleggiabile");
        }

        // Self-Message: validaRequisitiUtente(idUtente)
        if (!validaRequisitiUtente(utente)) {
            // Sequenza 2.b: erroreRequisitiMancanti
            throw new ErroreValidazioneException("Requisiti non soddisfatti per il noleggio");
        }

        // Self-Message: inviaComandoSblocco(idMezzo)
        boolean sbloccoRiuscito = inviaComandoSblocco(idMezzo);

        if (!sbloccoRiuscito) {
            // Sequenza 3.a: erroreTimeoutConnessione
            // Self-Message: annullaPrenotazioneAttiva(idUtente)
            annullaPrenotazioneAttiva(utente.getId());

            // Message: setStatoMezzo("DISPONIBILE") / setStato(true)
            mezzo.setStato(true);
            mezzoRepository.save(mezzo);

            throw new RuntimeException("Anomalia tecnica: connessione col veicolo fallita");
        }

        // Sequenza Principale: setStatoMezzo("IN_USO") -> setStato(false)
        mezzo.setStato(false);
        mezzoRepository.save(mezzo);

        // Recupera ed estingue l'eventuale prenotazione attiva
        Optional<Prenotazione> prenotazioneOpt = prenotazioneRepository.findByUtenteIdAndStatoTrue(utente.getId())
                .stream()
                .filter(p -> p.getMezzo() != null && p.getMezzo().getId().equals(idMezzo))
                .findFirst();

        // Message: create(idUtente, idMezzo, oraAttuale) -> nuovaCorsa
        Corsa corsa = new Corsa();
        if (utente instanceof Utente) {
            corsa.setUtente((Utente) utente);
        }
        corsa.setMezzo(mezzo);
        corsa.setOraInizio(LocalDateTime.now());
        corsa.setAreaAttuale(mezzo.getPosizione());

        if (prenotazioneOpt.isPresent()) {
            Prenotazione prenotazione = prenotazioneOpt.get();
            prenotazione.setStato(false);
            prenotazioneRepository.save(prenotazione);
            corsa.setPrenotazione(prenotazione);
        }

        corsaRepository.save(corsa);

        // Return: sbloccoCompletato (via DTO)
        return convertiInDTO(corsa);
    }

    // Self-Message: validaStatoVeicolo(statoMezzo)
    private boolean validaStatoVeicolo(Boolean statoMezzo) {
        // Il veicolo è sbloccabile se è libero o associato a una prenotazione valida
        return statoMezzo != null;
    }

    // Self-Message: validaRequisitiUtente(idUtente)
    private boolean validaRequisitiUtente(Account utente) {
        return utente != null && utente.getId() != null;
    }

    // Self-Message: inviaComandoSblocco(idMezzo)
    private boolean inviaComandoSblocco(Long idMezzo) {
        // Simula il fallimento di connessione se l'ID del mezzo è 888 (per test Sequenza 3.a)
        return idMezzo != 888L;
    }

    // Self-Message: annullaPrenotazioneAttiva(idUtente)
    private void annullaPrenotazioneAttiva(Long idUtente) {
        log.info("Annullamento prenotazione attiva per utente ID: {}", idUtente);
        List<Prenotazione> prenotazioni = prenotazioneRepository.findByUtenteIdAndStatoTrue(idUtente);
        for (Prenotazione p : prenotazioni) {
            p.setStato(false);
            prenotazioneRepository.save(p);
        }
    }

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

    // 2. elaboraRichiestaPercorso(destinazione)
    public PercorsoDTO elaboraRichiestaPercorso(Long idMezzo, String destinazione, String partenzaManuale) {
        log.info("Elaborazione richiesta percorso verso: {}", destinazione);

        // Self-Message: validaDestinazione(destinazione)
        if (!validaDestinazione(destinazione)) {
            // Sequenza 3.a: erroreDestinazione
            throw new IllegalArgumentException("Destinazione non valida o non trovata");
        }

        // Message: getPosizione() su :Mezzo -> Return: posizioneAttuale
        String posizionePartenza = (partenzaManuale != null && !partenzaManuale.isBlank())
                ? aggiornaPosizionePartenza(partenzaManuale)
                : acquisisciPosizioneMezzo(idMezzo);

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

    // Message: aggiornaPosizionePartenza(posizionePartenza)
    public String aggiornaPosizionePartenza(String posizionePartenza) {
        log.info("Aggiornamento posizione di partenza manuale: {}", posizionePartenza);
        return posizionePartenza;
    }

    private String acquisisciPosizioneMezzo(Long idMezzo) {
        if (idMezzo != null) {
            Mezzo mezzo = mezzoRepository.findById(idMezzo).orElse(null);
            if (mezzo != null) {
                return mezzo.getPosizione();
            }
        }
        return null;
    }

    // Self-Message: validaDestinazione(destinazione)
    private boolean validaDestinazione(String destinazione) {
        return destinazione != null && destinazione.trim().length() >= 3;
    }

    // Self-Message: calcolaPercorsoOttimale(posizionePartenza, destinazione)
    private PercorsoDTO calcolaPercorsoOttimale(String partenza, String destinazione) {
        if (destinazione.equalsIgnoreCase("errore") || destinazione.equalsIgnoreCase("impossibile")) {
            return null; // Simula Sequenza 5.a (errorePercorsoImpossibile)
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

    public CorsaDTO ottieniCorsaDTO(Long idCorsa) {
        return corsaRepository.findById(idCorsa)
                .map(this::convertiInDTO)
                .orElse(null);
    }

    // ==========================================
    // UC-08 TERMINARE CORSA
    // ==========================================

    // 2. elaboraTermineCorsa(idCorsa)
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

        // Message: setStatoMezzo("BLOCCATO") -> setStato(true) (mezzo bloccato e ripristinato)
        mezzo.setStato(true);
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
            return false; // Simula Sequenza 3.a (area non consentita)
        }
        return true;
    }

    // Self-Message: inviaComandoBloccoFisico(idMezzo)
    private boolean inviaComandoBloccoFisico(Long idMezzo) {
        // Simula il fallimento della connessione hardware (Sequenza 4.a) per idMezzo = 999
        return idMezzo != 999L;
    }
    // ==========================================
    // UC-09 PAGARE
    // ==========================================

    // 2. richiediCalcoloImporto(idCorsa)
    public Double richiediCalcoloImporto(Long idCorsa) {
        log.info("Calcolo importo per corsa ID: {}", idCorsa);

        // Message: getDatiCorsa() su :Corsa
        Corsa corsa = corsaRepository.findById(idCorsa)
                .orElseThrow(() -> new IllegalArgumentException("Corsa non trovata"));

        // Self-Message: calcolaImporto(datiCorsa)
        Double importo = calcolaImporto(corsa);
        corsa.setImporto(importo);
        corsaRepository.save(corsa);

        // Return: importoTotale
        return importo;
    }

    // Self-Message: calcolaImporto(datiCorsa)
    private Double calcolaImporto(Corsa corsa) {
        if (corsa.getOraInizio() == null) return 1.50;

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

        // 7. richiediAutorizzazione(importoTotale, metodo) a SistemaPagamentoEsterno
        boolean autorizzata = richiediAutorizzazioneSistemaEsterno(importoTotale, metodo);

        if (!autorizzata) {
            // Sequenza 7.a: esitoNegativo -> erroreTransazione
            throw new IllegalStateException("Impossibile elaborare il pagamento");
        }

        // Sequenza Principale: esitoPositivo -> setStatoPagamento(true)
        corsa.setImporto(importoTotale);
        corsaRepository.save(corsa);

        // Return: pagamentoCompletato
        return true;
    }

    // Simulazione del partner SistemaPagamentoEsterno: richiediAutorizzazione(...)
    private boolean richiediAutorizzazioneSistemaEsterno(Double importo, String metodo) {
        if (metodo != null && (metodo.equalsIgnoreCase("errore") || metodo.equalsIgnoreCase("rifiutata"))) {
            return false; // Simula esitoNegativo per la Sequenza 7.a
        }
        return true;
    }

    // ==========================================
    // UC-10 VISUALIZZARE STORICO
    // ==========================================

    public List<CorsaDTO> richiediCorseConcluse(Long idUtente) {
        log.info("Recupero storico corse per utente ID: {}", idUtente);

        // Message: getCorseUtente(idUtente) su :Corsa -> Return: listaCorse
        List<Corsa> listaCorse = corsaRepository.findByUtenteIdAndOraFineIsNotNull(idUtente);

        if (listaCorse == null || listaCorse.isEmpty()) {
            // Sequenza 2.a: listaVuota
            return new ArrayList<>();
        }

        // Self-Message: ordinaCronologicamente(listaCorse)
        ordinaCronologicamente(listaCorse);

        // Return: storicoCorse (convertiti in DTO)
        return listaCorse.stream()
                .map(this::convertiInDTO)
                .collect(Collectors.toList());
    }

    // Self-Message: ordinaCronologicamente(listaCorse)
    private void ordinaCronologicamente(List<Corsa> listaCorse) {
        listaCorse.sort((c1, c2) -> {
            if (c1.getOraInizio() == null || c2.getOraInizio() == null) return 0;
            return c2.getOraInizio().compareTo(c1.getOraInizio()); // Ordine cronologico decrescente (più recenti prima)
        });
    }
}
