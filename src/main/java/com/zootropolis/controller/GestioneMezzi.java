//package com.zootropolis.controller;
//
//import com.zootropolis.dto.AreaSquilibrataDTO;
//import com.zootropolis.dto.MezzoDTO;
//import com.zootropolis.entity.Mezzo;
//import com.zootropolis.exception.EccezioneMezzoNonDisponibile;
//import com.zootropolis.exception.EccezionePosizioneMancante;
//import com.zootropolis.repository.MezzoRepository;
//import lombok.Getter;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//public class GestioneMezzi {
//
//    private static final Logger log = LoggerFactory.getLogger(GestioneMezzi.class);
//    private final MezzoRepository mezzoRepository;
//
//    @Value("${app.gps.mock.enabled:false}")
//    private boolean gpsMockEnabled;
//
//    @Value("${app.gps.mock.default-location:Via Roma 1, Zootropolis}")
//    private String defaultLocation;
//
//    @Getter
//    private double raggioRicerca = 1.0;
//    @Getter
//    private String posizioneAttualeUtente;
//
//    public GestioneMezzi(MezzoRepository mezzoRepository) {
//        this.mezzoRepository = mezzoRepository;
//    }
//
//    // Message: acquisisciPosizioneERicerca()
//    public List<Mezzo> acquisisciPosizioneERicerca() {
//        log.info("Avvio acquisizione posizione GPS...");
//
//        // Self-message: rilevaPosizioneGPS()
//        boolean gpsRilevato = rilevaPosizioneGPS();
//
//        if (!gpsRilevato) {
//            log.warn("Impossibile acquisire posizione GPS.");
//            // Return: eccezionePosizioneMancante
//            throw new EccezionePosizioneMancante("Impossibile acquisire la posizione automatica");
//        }
//
//        return ricercaMezziInternal();
//    }
//
//    // Self-message: rilevaPosizioneGPS() -> Legge le proprietà mock da file di config
//    private boolean rilevaPosizioneGPS() {
//        if (gpsMockEnabled) {
//            this.posizioneAttualeUtente = defaultLocation;
//            log.info("GPS Mock attivo! Posizione rilevata automaticamente: {}", posizioneAttualeUtente);
//            return true;
//        }
//
//        // Se non è mockato e l'utente non ha mai inserito un indirizzo
//        return posizioneAttualeUtente != null && !posizioneAttualeUtente.isBlank();
//    }
//
//    // Message: aggiornaPosizione(indirizzo)
//    public List<Mezzo> aggiornaPosizione(String indirizzo) {
//        log.info("Aggiornamento posizione manuale a: {}", indirizzo);
//        this.posizioneAttualeUtente = indirizzo;
//        return ricercaMezziInternal();
//    }
//
//    // Message: incrementaRaggioRicerca()
//    public List<Mezzo> incrementaRaggioRicerca() {
//        this.raggioRicerca += 1.5;
//        log.info("Raggio di ricerca incrementato a: {} km", raggioRicerca);
//        return ricercaMezziInternal();
//    }
//
//    private List<Mezzo> ricercaMezziInternal() {
//        List<Mezzo> tuttiIMezzi = mezzoRepository.findAll();
//
//        // Filtra i mezzi invocando getStato() e getPosizione() forniti da Lombok
//        List<Mezzo> mezziDisponibili = tuttiIMezzi.stream()
//                .filter(m -> Boolean.TRUE.equals(m.getStato())) // getStato()
//                .collect(Collectors.toList());
//
//        // Self-message: filtraMezziDisponibiliVicini(raggio)
//        return filtraMezziDisponibiliVicini(mezziDisponibili, raggioRicerca);
//    }
//
//    // Self-message: filtraMezziDisponibiliVicini(raggio)
//    // TODO: Modificare per trovare mezzi alle vicinanze utilizzando il raggio, perche questo metodo ritorna solo i mezzi con lo stato a true.
//    private List<Mezzo> filtraMezziDisponibiliVicini(List<Mezzo> mezzi, double raggio) {
//        if (posizioneAttualeUtente == null) {
//            return new ArrayList<>();
//        }
//        return mezzi;
//    }
//
//    public void resetRicerca() {
//        this.raggioRicerca = 1.0;
//        this.posizioneAttualeUtente = null;
//    }
//
//    public List<Mezzo> resetRaggioRicerca() {
//        this.raggioRicerca = 1.0;
//        log.info("Raggio di ricerca resettato al valore predefinito: {} km", raggioRicerca);
//        return ricercaMezziInternal();
//    }
//
//    // Message: richiediDettagli(idMezzo)
//    public Mezzo richiediDettagli(Long idMezzo) {
//        log.info("Richiesta dettagli per il mezzo ID: {}", idMezzo);
//
//        // Message: getDettagli() -> Return: datiMezzo
//        Mezzo mezzo = mezzoRepository.findById(idMezzo)
//                .orElseThrow(() -> new EccezioneMezzoNonDisponibile("Mezzo non trovato nel sistema"));
//
//        // Self-message: verificaDisponibilita(datiMezzo)
//        boolean disponibile = verificaDisponibilita(mezzo);
//
//        if (!disponibile) {
//            log.warn("Mezzo ID {} non disponibile per il noleggio.", idMezzo);
//            // Return: erroreNonDisponibile
//            throw new EccezioneMezzoNonDisponibile("Veicolo non più disponibile");
//        }
//
//        return mezzo;
//    }
//
//    // Self-message: verificaDisponibilita(datiMezzo)
//    private boolean verificaDisponibilita(Mezzo mezzo) {
//        // Un mezzo è disponibile se lo stato è true e la batteria è > 10%
//        return Boolean.TRUE.equals(mezzo.getStato()) &&
//                (mezzo.getPercentualeBatteria() == null || mezzo.getPercentualeBatteria() > 10);
//    }
//
//    public MezzoDTO convertiInDTO(Mezzo mezzo) {
//        if (mezzo == null) return null;
//
//        MezzoDTO dto = new MezzoDTO();
//        dto.setId(mezzo.getId());
//        dto.setTipo(mezzo.getTipo());
//        dto.setPercentualeBatteria(mezzo.getPercentualeBatteria());
//        dto.setStato(mezzo.getStato());
//        dto.getPosizione();
//        dto.setPosizione(mezzo.getPosizione());
//
//        if (mezzo.getOperatore() != null) {
//            dto.setIdOperatore(mezzo.getOperatore().getId());
//        }
//        return dto;
//    }
//    public List<MezzoDTO> acquisisciPosizioneERicercaDTO() {
//        List<Mezzo> mezziEntity = acquisisciPosizioneERicerca();
//        return mezziEntity.stream()
//                .map(this::convertiInDTO)
//                .collect(Collectors.toList());
//    }
//
//    public MezzoDTO richiediDettagliDTO(Long idMezzo) {
//        Mezzo mezzoEntity = richiediDettagli(idMezzo);
//        return convertiInDTO(mezzoEntity);
//    }
//
//    // Decrementa il raggio di ricerca (minimo 1.0 km)
//    public List<Mezzo> decrementaRaggioRicerca() {
//        if (this.raggioRicerca > 1.0) {
//            this.raggioRicerca = Math.max(1.0, this.raggioRicerca - 1.5);
//            log.info("Raggio di ricerca ridotto a: {} km", raggioRicerca);
//        }
//        return ricercaMezziInternal();
//    }
//
//    // ==========================================
//    // UC-11 VISUALIZZARE MEZZI
//    // ==========================================
//
//    // Message: richiediDatiMezzi()
//    public List<MezzoDTO> richiediDatiMezzi(boolean ignoraLocalizzazioneLive) {
//        log.info("Recupero dati mezzi per mappa operatore...");
//
//        List<Mezzo> tuttiIMezzi = mezzoRepository.findAll();
//
//        // Self-Message: verificaStatoVeicoli()
//        if (tuttiIMezzi.isEmpty()) {
//            // Sequenza 2.b: eccezioneNessunVeicolo
//            throw new IllegalStateException("Nessun mezzo da mostrare");
//        }
//
//        // Simulazione Sequenza 2.a: Se la localizzazione live fallisce e non si accettano i dati noti
//        if (!ignoraLocalizzazioneLive && "OFFLINE".equalsIgnoreCase(this.getPosizioneAttualeUtente())) {
//            // Sequenza 2.a: eccezioneLocalizzazioneAssente
//            throw new IllegalArgumentException("Impossibile mostrare dati in tempo reale");
//        }
//
//        // Message: getPosizione() -> Return: listaDatiMezzi
//        return tuttiIMezzi.stream()
//                .map(this::convertiInDTO)
//                .collect(Collectors.toList());
//    }
//
//    // Message: richiediUltimiDatiNoti()
//    public List<MezzoDTO> richiediUltimiDatiNoti() {
//        log.info("Recupero ultimi dati noti dei mezzi...");
//        return richiediDatiMezzi(true);
//    }
//
//    // ==========================================
//    // UC-12 MONITORARE MALFUNZIONAMENTI
//    // ==========================================
//
//    // Message: richiediElencoAnomalie()
//    public List<MezzoDTO> richiediElencoAnomalie() {
//        log.info("Recupero mezzi con anomalie o malfunzionamenti...");
//
//        List<Mezzo> tuttiIMezzi = mezzoRepository.findAll();
//
//        // Self-Message: filtraMezziAnomali()
//        List<Mezzo> mezziAnomali = filtraMezziAnomali(tuttiIMezzi);
//
//        if (mezziAnomali.isEmpty()) {
//            // Sequenza 2.a: listaVuota
//            return new ArrayList<>();
//        }
//
//        // Message: getDettagliDiagnostici() & Return: listaMezziAnomali
//        return mezziAnomali.stream().map(mezzo -> {
//            MezzoDTO dto = convertiInDTO(mezzo);
//            // Simula/Arricchisce con i dettagli diagnostici rilevati
//            dto.setDescrizioneAnomalia(ottieniDettagliDiagnostici(mezzo));
//            return dto;
//        }).collect(Collectors.toList());
//    }
//
//    // Self-Message: filtraMezziAnomali()
//    private List<Mezzo> filtraMezziAnomali(List<Mezzo> mezzi) {
//        return mezzi.stream()
//                .filter(m -> {
//                    // Un mezzo ha un'anomalia se lo stato è false (inattivo/guasto)
//                    // oppure se la percentuale di batteria è <= 10%
//                    boolean guasto = Boolean.FALSE.equals(m.getStato());
//                    boolean batteriaScarica = m.getPercentualeBatteria() != null && m.getPercentualeBatteria() <= 10;
//                    return guasto || batteriaScarica;
//                })
//                .collect(Collectors.toList());
//    }
//
//    // Message: getDettagliDiagnostici() su :Mezzo
//    private String ottieniDettagliDiagnostici(Mezzo mezzo) {
//        if (mezzo.getPercentualeBatteria() != null && mezzo.getPercentualeBatteria() <= 10) {
//            return "Batteria Critica (" + mezzo.getPercentualeBatteria() + "%) - Ricarica richiesta";
//        }
//        return "Anomalia Hardware/Blocco Sensori - Intervento manutenzione necessario";
//    }
//
//    // ==========================================
//    // UC-13 REDISTRIBUIRE
//    // ==========================================
//
//    // Message: analizzaDistribuzioneMezzi()
//    public List<AreaSquilibrataDTO> analizzaDistribuzioneMezzi() {
//        log.info("Analisi distribuzione mezzi per rilevamento squilibri...");
//
//        List<Mezzo> tuttiIMezzi = mezzoRepository.findAll();
//
//        // Self-Message: calcolaCarenzeEdEccedenze()
//        List<AreaSquilibrataDTO> areeSquilibrate = calcolaCarenzeEdEccedenze(tuttiIMezzi);
//
//        if (areeSquilibrate.isEmpty()) {
//            // Sequenza 2.a: distribuzioneOttimale
//            return new ArrayList<>();
//        }
//
//        // Return: listaAreeSquilibrate
//        return areeSquilibrate;
//    }
//
//    // Self-Message: calcolaCarenzeEdEccedenze()
//    private List<AreaSquilibrataDTO> calcolaCarenzeEdEccedenze(List<Mezzo> mezzi) {
//        List<AreaSquilibrataDTO> risultati = new ArrayList<>();
//
//        // Mappatura delle posizioni attuali dei mezzi
//        long contatoreCentro = mezzi.stream().filter(m -> m.getPosizione() != null && m.getPosizione().toLowerCase().contains("roma")).count();
//        long contatoreStazione = mezzi.stream().filter(m -> m.getPosizione() != null && m.getPosizione().toLowerCase().contains("stazione")).count();
//
//        // Area Centro (Capacità: 5) -> Eccedenza se > 5
//        if (contatoreCentro > 5) {
//            risultati.add(new AreaSquilibrataDTO("Zona Centro - Via Roma", (int) contatoreCentro, 5, "ECCEDENZA", (int) contatoreCentro - 5));
//        }
//
//        // Area Stazione (Capacità: 8) -> Carenza se < 2
//        if (contatoreStazione < 2) {
//            risultati.add(new AreaSquilibrataDTO("Zona Stazione Centrale", (int) contatoreStazione, 8, "CARENZA", 2 - (int) contatoreStazione));
//        }
//
//        return risultati;
//    }
//
//    // ==========================================
//    // UC-16 BLOCCARE DA REMOTO
//    // ==========================================
//
//    // Message: elaboraBlocco(idMezzo)
//    @Transactional
//    public boolean elaboraBlocco(Long idMezzo) {
//        log.info("Elaborazione blocco remoto per mezzo ID: {}", idMezzo);
//
//        // Message: getDatiMezzo() & Self-Message: verificaEsistenza(idMezzo)
//        Mezzo mezzo = mezzoRepository.findById(idMezzo)
//                .orElseThrow(() -> new IllegalArgumentException("Mezzo non trovato")); // Sequenza 4.a: erroreMezzoInesistente
//
//        // Self-Message: inviaComandoBloccoFisico()
//        boolean bloccoRiuscito = inviaComandoBloccoFisico(idMezzo);
//        if (!bloccoRiuscito) {
//            // Sequenza 5.a: erroreConnessioneHardware
//            throw new IllegalStateException("Mancata connessione con il veicolo");
//        }
//
//        // Message: setStatoMezzo("BLOCCATO") / setStato(false)
//        mezzo.setStato(false); // Imposta il mezzo come fuori servizio/bloccato
//        mezzoRepository.save(mezzo);
//
//        // Return: bloccoCompletato
//        return true;
//    }
//
//    // Self-Message: inviaComandoBloccoFisico(idMezzo)
//    private boolean inviaComandoBloccoFisico(Long idMezzo) {
//        // Simula la fallimento della connessione hardware (Sequenza 5.a) se l'ID è 999
//        if (idMezzo != null && idMezzo == 999L) {
//            log.warn("Errore di connessione hardware con la centralina del mezzo ID: {}", idMezzo);
//            return false;
//        }
//        log.info("Comando di blocco fisico inviato con successo al mezzo ID: {}", idMezzo);
//        return true;
//    }
//}

package com.zootropolis.controller;

import com.zootropolis.dto.AreaSquilibrataDTO;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GestioneMezzi {

    private static final Logger log = LoggerFactory.getLogger(GestioneMezzi.class);
    private final MezzoRepository mezzoRepository;

    @Value("${app.gps.mock.enabled:true}")
    private boolean gpsMockEnabled;

    @Value("${app.gps.mock.default-location:Via Roma 10, Zootropolis}")
    private String defaultLocation;

    @Value("${app.gps.mock.default-lat:41.1171}")
    private double latUtenteDefault;

    @Value("${app.gps.mock.default-lon:16.8719}")
    private double lonUtenteDefault;

    @Getter
    private double raggioRicerca = 1.0;
    @Getter
    private String posizioneAttualeUtente;

    public GestioneMezzi(MezzoRepository mezzoRepository) {
        this.mezzoRepository = mezzoRepository;
    }

    public List<Mezzo> acquisisciPosizioneERicerca() {
        log.info("Avvio acquisizione posizione GPS...");

        boolean gpsRilevato = rilevaPosizioneGPS();

        if (!gpsRilevato) {
            log.warn("Impossibile acquisire posizione GPS.");
            throw new EccezionePosizioneMancante("Impossibile acquisire la posizione automatica");
        }

        return ricercaMezziInternal();
    }

    private boolean rilevaPosizioneGPS() {
        if (gpsMockEnabled || posizioneAttualeUtente == null) {
            this.posizioneAttualeUtente = defaultLocation;
            log.info("GPS attivo! Posizione rilevata automaticamente: {}", posizioneAttualeUtente);
            return true;
        }
        return !posizioneAttualeUtente.isBlank();
    }

    public List<Mezzo> aggiornaPosizione(String indirizzo) {
        log.info("Aggiornamento posizione manuale a: {}", indirizzo);
        this.posizioneAttualeUtente = indirizzo;
        return ricercaMezziInternal();
    }

    public List<Mezzo> incrementaRaggioRicerca() {
        this.raggioRicerca += 0.5;
        log.info("Raggio di ricerca incrementato a: {} km", raggioRicerca);
        return ricercaMezziInternal();
    }

    public List<Mezzo> decrementaRaggioRicerca() {
        if (this.raggioRicerca > 0.5) {
            this.raggioRicerca = Math.max(0.5, this.raggioRicerca - 0.5);
            log.info("Raggio di ricerca ridotto a: {} km", raggioRicerca);
        }
        return ricercaMezziInternal();
    }

    public List<Mezzo> resetRaggioRicerca() {
        this.raggioRicerca = 1.0;
        log.info("Raggio di ricerca resettato al valore predefinito: {} km", raggioRicerca);
        return ricercaMezziInternal();
    }

    private List<Mezzo> ricercaMezziInternal() {
        List<Mezzo> tuttiIMezzi = mezzoRepository.findAll();

        List<Mezzo> mezziDisponibili = tuttiIMezzi.stream()
                .filter(m -> Boolean.TRUE.equals(m.getStato()))
                .collect(Collectors.toList());

        return filtraMezziDisponibiliVicini(mezziDisponibili, raggioRicerca);
    }

    // Filtro effettivo basato sulla distanza Haversine (in chilometri)
    private List<Mezzo> filtraMezziDisponibiliVicini(List<Mezzo> mezzi, double raggio) {
        return mezzi.stream().filter(m -> {
            if (m.getLatitudine() == null || m.getLongitudine() == null) {
                return true; // Se non ha coordinate, lo mantieni per fallback
            }
            double distanza = calcolaDistanzaKm(latUtenteDefault, lonUtenteDefault, m.getLatitudine(), m.getLongitudine());
            return distanza <= raggio;
        }).collect(Collectors.toList());
    }

    // Formula dell'Haversine per il calcolo della distanza tra due punti GPS in KM
    private double calcolaDistanzaKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Raggio della terra in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public void resetRicerca() {
        this.raggioRicerca = 1.0;
        this.posizioneAttualeUtente = null;
    }

    public Mezzo richiediDettagli(Long idMezzo) {
        log.info("Richiesta dettagli per il mezzo ID: {}", idMezzo);

        Mezzo mezzo = mezzoRepository.findById(idMezzo)
                .orElseThrow(() -> new EccezioneMezzoNonDisponibile("Mezzo non trovato nel sistema"));

        if (!verificaDisponibilita(mezzo)) {
            log.warn("Mezzo ID {} non disponibile per il noleggio.", idMezzo);
            throw new EccezioneMezzoNonDisponibile("Veicolo non più disponibile");
        }

        return mezzo;
    }

    private boolean verificaDisponibilita(Mezzo mezzo) {
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
        dto.setPosizione(mezzo.getPosizione());
        dto.setLatitudine(mezzo.getLatitudine());
        dto.setLongitudine(mezzo.getLongitudine());

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

    // ==========================================
    // UC-11 VISUALIZZARE MEZZI
    // ==========================================

    // 2. richiediDatiMezzi()
    public List<MezzoDTO> richiediDatiMezzi(boolean ignoraLocalizzazioneLive) {
        log.info("Recupero dati mezzi per mappa operatore...");

        List<Mezzo> tuttiIMezzi = mezzoRepository.findAll();

        // Self-Message: verificaStatoVeicoli()
        verificaStatoVeicoli(tuttiIMezzi);

        // Simulazione Sequenza 2.a: Localizzazione live non disponibile
        if (!ignoraLocalizzazioneLive && "OFFLINE".equalsIgnoreCase(this.getPosizioneAttualeUtente())) {
            // Sequenza 2.a: eccezioneLocalizzazioneAssente
            throw new IllegalArgumentException("Impossibile mostrare dati in tempo reale");
        }

        // Message: getPosizione() su :Mezzo -> Return: listaDatiMezzi / datiAttualizzati
        return tuttiIMezzi.stream()
                .map(this::convertiInDTO)
                .collect(Collectors.toList());
    }

    // Self-Message: verificaStatoVeicoli()
    private void verificaStatoVeicoli(List<Mezzo> mezzi) {
        if (mezzi == null || mezzi.isEmpty()) {
            // Sequenza 2.b: eccezioneNessunVeicolo
            throw new IllegalStateException("Nessun mezzo da mostrare");
        }
    }

    // 2.a.5 richiediUltimiDatiNoti()
    public List<MezzoDTO> richiediUltimiDatiNoti() {
        log.info("Recupero ultimi dati noti dei mezzi...");
        return richiediDatiMezzi(true);
    }

//    // UC-11
//    public List<MezzoDTO> richiediDatiMezzi(boolean ignoraLocalizzazioneLive) {
//        log.info("Recupero dati mezzi per mappa operatore...");
//        List<Mezzo> tuttiIMezzi = mezzoRepository.findAll();
//
//        if (tuttiIMezzi.isEmpty()) {
//            throw new IllegalStateException("Nessun mezzo da mostrare");
//        }
//
//        return tuttiIMezzi.stream()
//                .map(this::convertiInDTO)
//                .collect(Collectors.toList());
//    }
//
//    public List<MezzoDTO> richiediUltimiDatiNoti() {
//        return richiediDatiMezzi(true);
//    }

    // ==========================================
    // UC-12 MONITORARE MALFUNZIONAMENTI
    // ==========================================

    // 2. richiediElencoAnomalie()
    public List<MezzoDTO> richiediElencoAnomalie() {
        log.info("Recupero mezzi con anomalie o malfunzionamenti...");

        List<Mezzo> tuttiIMezzi = mezzoRepository.findAll();

        // Self-Message: filtraMezziAnomali()
        List<Mezzo> mezziAnomali = filtraMezziAnomali(tuttiIMezzi);

        if (mezziAnomali == null || mezziAnomali.isEmpty()) {
            // Sequenza 2.a: listaVuota
            return new ArrayList<>();
        }

        // Message: getDettagliDiagnostici() su :Mezzo -> Return: listaMezziAnomali (via DTO)
        return mezziAnomali.stream().map(mezzo -> {
            MezzoDTO dto = convertiInDTO(mezzo);
            dto.setDescrizioneAnomalia(getDettagliDiagnostici(mezzo));
            return dto;
        }).collect(Collectors.toList());
    }

    // Self-Message: filtraMezziAnomali()
    private List<Mezzo> filtraMezziAnomali(List<Mezzo> mezzi) {
        return mezzi.stream()
                .filter(m -> {
                    // Un mezzo ha un'anomalia se lo stato è false oppure se la batteria è <= 10%
                    boolean guasto = Boolean.FALSE.equals(m.getStato());
                    boolean batteriaScarica = m.getPercentualeBatteria() != null && m.getPercentualeBatteria() <= 10;
                    return guasto || batteriaScarica;
                })
                .collect(Collectors.toList());
    }

    // Message: getDettagliDiagnostici() su :Mezzo
    private String getDettagliDiagnostici(Mezzo mezzo) {
        if (mezzo.getPercentualeBatteria() != null && mezzo.getPercentualeBatteria() <= 10) {
            return "Batteria Critica (" + mezzo.getPercentualeBatteria() + "%) - Ricarica urgente";
        }
        return "Anomalia Hardware/Blocco Sensori - Intervento di manutenzione necessario";
    }

// ==========================================
    // UC-13 REDISTRIBUIRE
    // ==========================================

    // 2. analizzaDistribuzioneMezzi()
    public List<AreaSquilibrataDTO> analizzaDistribuzioneMezzi() {
        log.info("Analisi distribuzione mezzi per rilevamento squilibri...");

        // Simulazione Message getDatiArea() su :Area -> capacitaZone
        // Simulazione Message getPosizione() su :Mezzo -> posizioniAttuali
        List<Mezzo> tuttiIMezzi = mezzoRepository.findAll();

        // Self-Message: calcolaCarenzeEdEccedenze()
        List<AreaSquilibrataDTO> areeSquilibrate = calcolaCarenzeEdEccedenze(tuttiIMezzi);

        if (areeSquilibrate == null || areeSquilibrate.isEmpty()) {
            // Sequenza 2.a: distribuzioneOttimale (lista vuota)
            return new ArrayList<>();
        }

        // Return: listaAreeSquilibrate
        return areeSquilibrate;
    }

    // Self-Message: calcolaCarenzeEdEccedenze()
    private List<AreaSquilibrataDTO> calcolaCarenzeEdEccedenze(List<Mezzo> mezzi) {
        List<AreaSquilibrataDTO> risultati = new ArrayList<>();

        // Mappatura/Conteggio delle posizioni attuali dei mezzi rispetto alla capacità dell'Area
        long contatoreCentro = mezzi.stream()
                .filter(m -> m.getPosizione() != null && m.getPosizione().toLowerCase().contains("roma"))
                .count();
        long contatoreStazione = mezzi.stream()
                .filter(m -> m.getPosizione() != null && m.getPosizione().toLowerCase().contains("stazione"))
                .count();

        // Area Centro (Capacità target: 5) -> Eccedenza se > 5
        if (contatoreCentro > 5) {
            risultati.add(new AreaSquilibrataDTO("Zona Centro - Via Roma", (int) contatoreCentro, 5, "ECCEDENZA", (int) contatoreCentro - 5));
        }

        // Area Stazione (Capacità target: 8) -> Carenza se < 2
        if (contatoreStazione < 2) {
            risultati.add(new AreaSquilibrataDTO("Zona Stazione Centrale", (int) contatoreStazione, 8, "CARENZA", 2 - (int) contatoreStazione));
        }

        return risultati;
    }

    // ==========================================
    // UC-16 BLOCCARE DA REMOTO
    // ==========================================

    // Message: elaboraBlocco(idMezzo)
    @Transactional
    public boolean elaboraBlocco(Long idMezzo) {
        log.info("Elaborazione blocco remoto per mezzo ID: {}", idMezzo);

        // Message: getDatiMezzo() su :Mezzo & Self-message: verificaEsistenza(idMezzo)
        Mezzo mezzo = mezzoRepository.findById(idMezzo)
                .orElseThrow(() -> new IllegalArgumentException("Mezzo non trovato")); // Sequenza 4.a: erroreMezzoInesistente

        // Self-Message: inviaComandoBloccoFisico()
        boolean bloccoRiuscito = inviaComandoBloccoFisico(idMezzo);
        if (!bloccoRiuscito) {
            // Sequenza 5.a: erroreConnessioneHardware
            throw new IllegalStateException("Mancata connessione con il veicolo");
        }

        // Message: setStatoMezzo("BLOCCATO") -> setStato(false) (mezzo bloccato / fuori servizio)
        mezzo.setStato(false);
        mezzoRepository.save(mezzo);

        // Return: bloccoCompletato
        return true;
    }

    // Self-Message: inviaComandoBloccoFisico()
    private boolean inviaComandoBloccoFisico(Long idMezzo) {
        // Simula la mancata connessione hardware (Sequenza 5.a) se l'ID è 999
        if (idMezzo != null && idMezzo == 999L) {
            log.warn("Errore di connessione hardware con la centralina del mezzo ID: {}", idMezzo);
            return false;
        }
        log.info("Comando di blocco fisico inviato con successo al mezzo ID: {}", idMezzo);
        return true;
    }
}