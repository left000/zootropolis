////package com.zootropolis.controller;
////
////import com.zootropolis.dto.PrenotazioneDTO;
////import com.zootropolis.entity.Account;
////import com.zootropolis.entity.Mezzo;
////import com.zootropolis.entity.Prenotazione;
////import com.zootropolis.entity.Utente;
////import com.zootropolis.exception.EccezioneMezzoNonDisponibile;
////import com.zootropolis.exception.ErroreValidazioneException;
////import com.zootropolis.repository.MezzoRepository;
////import com.zootropolis.repository.PrenotazioneRepository;
////import org.slf4j.Logger;
////import org.slf4j.LoggerFactory;
////import org.springframework.stereotype.Service;
////import org.springframework.transaction.annotation.Transactional;
////
////import java.time.LocalDateTime;
////import java.util.List;
////import java.util.stream.Collectors;
////
////@Service
////public class GestionePrenotazioni {
////
////    private static final Logger log = LoggerFactory.getLogger(GestionePrenotazioni.class);
////    private static final int DURATA_PRENOTAZIONE_MINUTI = 15;
////
////    private final MezzoRepository mezzoRepository;
////    private final PrenotazioneRepository prenotazioneRepository;
////
////    public GestionePrenotazioni(MezzoRepository mezzoRepository, PrenotazioneRepository prenotazioneRepository) {
////        this.mezzoRepository = mezzoRepository;
////        this.prenotazioneRepository = prenotazioneRepository;
////    }
////
////    // 2. elaboraPrenotazione(idMezzo, idUtente)
//////    @Transactional
//////    public int elaboraPrenotazione(Long idMezzo, Account utente) {
//////        log.info("Elaborazione prenotazione per mezzo ID: {} da utente ID: {}", idMezzo, utente.getId());
//////
//////        Mezzo mezzo = mezzoRepository.findById(idMezzo)
//////                .orElseThrow(() -> new EccezioneMezzoNonDisponibile("Mezzo non trovato"));
//////
//////        // getStato() + validaDisponibilita(statoMezzo)
//////        boolean disponibile = validaDisponibilita(mezzo);
//////        if (!disponibile) {
//////            // Sequenza 2.a: erroreVeicoloOccupato
//////            throw new EccezioneMezzoNonDisponibile("Il veicolo non può essere prenotato");
//////        }
//////
//////        // validaRequisitiUtente(idUtente)
//////        boolean idoneo = validaRequisitiUtente(utente);
//////        if (!idoneo) {
//////            // Sequenza 2.b: erroreRequisitiMancanti
//////            throw new ErroreValidazioneException("L'utente non rispetta i requisiti per prenotare il mezzo");
//////        }
//////
//////        // setStatoMezzo("PRENOTATO") -> setStato(false)
//////        mezzo.setStato(false); // Imposta il mezzo come occupato/non più disponibile
//////        mezzoRepository.save(mezzo);
//////
//////        // Gestione cast da Account a Utente e salvataggio Prenotazione
//////        Prenotazione nuovaPrenotazione;
//////        if (utente instanceof Utente) {
//////            nuovaPrenotazione = new Prenotazione((Utente) utente, mezzo, LocalDateTime.now(), DURATA_PRENOTAZIONE_MINUTI);
//////            prenotazioneRepository.save(nuovaPrenotazione);
//////        } else {
//////            throw new ErroreValidazioneException("L'account loggato non è un utente valido per la prenotazione");
//////        }
//////
//////        // avviaTimerPrenotazione()
//////        avviaTimerPrenotazione(nuovaPrenotazione);
//////
//////        // Return: confermaConTempo(tempoRimanente)
//////        return DURATA_PRENOTAZIONE_MINUTI;
//////    }
////    @Transactional
////    public PrenotazioneDTO elaboraPrenotazione(Long idMezzo, Account utente) {
////        log.info("Elaborazione prenotazione DTO per mezzo ID: {} da utente ID: {}", idMezzo, utente.getId());
////
////        Mezzo mezzo = mezzoRepository.findById(idMezzo)
////                .orElseThrow(() -> new EccezioneMezzoNonDisponibile("Mezzo non trovato"));
////
////        if (!validaDisponibilita(mezzo)) {
////            throw new EccezioneMezzoNonDisponibile("Il veicolo non può essere prenotato");
////        }
////
////        if (!validaRequisitiUtente(utente)) {
////            throw new ErroreValidazioneException("L'utente non rispetta i requisiti per prenotare il mezzo");
////        }
////
////        // Aggiorna stato mezzo
////        mezzo.setStato(false);
////        mezzoRepository.save(mezzo);
////
////        // Creazione entity Prenotazione tramite l'istanza Utente
////        Prenotazione nuovaPrenotazione;
////        if (utente instanceof Utente) {
////            nuovaPrenotazione = new Prenotazione((Utente) utente, mezzo, LocalDateTime.now(), DURATA_PRENOTAZIONE_MINUTI);
////            prenotazioneRepository.save(nuovaPrenotazione);
////        } else {
////            throw new ErroreValidazioneException("L'account loggato non è un utente valido per la prenotazione");
////        }
////
////        avviaTimerPrenotazione(nuovaPrenotazione);
////
////        // Restituisce il DTO alla vista
////        return convertiInDTO(nuovaPrenotazione);
////    }
////    // Self-message: validaDisponibilita(statoMezzo)
////    private boolean validaDisponibilita(Mezzo mezzo) {
////        return Boolean.TRUE.equals(mezzo.getStato());
////    }
////
////    // Self-message: validaRequisitiUtente(idUtente)
////    private boolean validaRequisitiUtente(Account utente) {
////        // Esempio requisiti: Utente registrato e non disabilitato
////        return utente != null && utente.getId() != null;
////    }
////
////    // Self-message: avviaTimerPrenotazione()
////    private void avviaTimerPrenotazione(Prenotazione prenotazione) {
////        log.info("Timer di {} minuti avviato per la prenotazione ID: {}", DURATA_PRENOTAZIONE_MINUTI, prenotazione.getId());
////    }
////
////    // Convertitore Entity -> DTO per Prenotazione
////    public PrenotazioneDTO convertiInDTO(Prenotazione prenotazione) {
////        if (prenotazione == null) return null;
////
////        PrenotazioneDTO dto = new PrenotazioneDTO();
////        dto.setId(prenotazione.getId());
////        dto.setOraInizio(prenotazione.getOraInizio());
////        dto.setOraFine(prenotazione.getOraFine());
////        dto.setCodiceSblocco(prenotazione.getCodiceSblocco());
////        dto.setStato(prenotazione.getStato());
////
////        if (prenotazione.getUtente() != null) {
////            dto.setIdUtente(prenotazione.getUtente().getId());
////        }
////        if (prenotazione.getMezzo() != null) {
////            dto.setIdMezzo(prenotazione.getMezzo().getId());
////        }
////        return dto;
////    }
////
////    // TODO: METODO DENTRO IL DOCUMENTO
////    // Recupera tutte le prenotazioni attive dell'utente come DTO
////    public List<PrenotazioneDTO> ottieniPrenotazioniAttiveDTO(Long idUtente) {
////        return prenotazioneRepository.findByUtenteIdAndStatoTrue(idUtente)
////                .stream()
////                .map(this::convertiInDTO)
////                .collect(Collectors.toList());
////    }
////
////}
//
//package com.zootropolis.controller;
//
//import com.zootropolis.dto.PrenotazioneDTO;
//import com.zootropolis.entity.Account;
//import com.zootropolis.entity.Mezzo;
//import com.zootropolis.entity.Prenotazione;
//import com.zootropolis.entity.Utente;
//import com.zootropolis.exception.EccezioneMezzoNonDisponibile;
//import com.zootropolis.exception.ErroreValidazioneException;
//import com.zootropolis.repository.MezzoRepository;
//import com.zootropolis.repository.PrenotazioneRepository;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Service
//public class GestionePrenotazioni {
//
//    private static final Logger log = LoggerFactory.getLogger(GestionePrenotazioni.class);
//    private static final int DURATA_PRENOTAZIONE_MINUTI = 15;
//
//    private final MezzoRepository mezzoRepository;
//    private final PrenotazioneRepository prenotazioneRepository;
//
//    public GestionePrenotazioni(MezzoRepository mezzoRepository, PrenotazioneRepository prenotazioneRepository) {
//        this.mezzoRepository = mezzoRepository;
//        this.prenotazioneRepository = prenotazioneRepository;
//    }
//
//    @Transactional
//    public PrenotazioneDTO elaboraPrenotazione(Long idMezzo, Account utente) {
//        log.info("Elaborazione prenotazione DTO per mezzo ID: {} da utente ID: {}", idMezzo, utente.getId());
//
//        // Controlla se la prenotazione è già attiva per questo utente e questo mezzo
//        Optional<Prenotazione> prenotazioneGiaAttiva = prenotazioneRepository.findByUtenteIdAndStatoTrue(utente.getId())
//                .stream()
//                .filter(p -> p.getMezzo() != null && p.getMezzo().getId().equals(idMezzo))
//                .findFirst();
//
//        if (prenotazioneGiaAttiva.isPresent()) {
//            log.info("Prenotazione già attiva trovata (ID: {}).", prenotazioneGiaAttiva.get().getId());
//            return convertiInDTO(prenotazioneGiaAttiva.get());
//        }
//
//        Mezzo mezzo = mezzoRepository.findById(idMezzo)
//                .orElseThrow(() -> new EccezioneMezzoNonDisponibile("Mezzo non trovato"));
//
//        if (!validaDisponibilita(mezzo)) {
//            throw new EccezioneMezzoNonDisponibile("Il veicolo non può essere prenotato");
//        }
//
//        if (!validaRequisitiUtente(utente)) {
//            throw new ErroreValidazioneException("L'utente non rispetta i requisiti per prenotare il mezzo");
//        }
//
//        mezzo.setStato(false);
//        mezzoRepository.save(mezzo);
//
//        Prenotazione nuovaPrenotazione;
//        if (utente instanceof Utente) {
//            nuovaPrenotazione = new Prenotazione((Utente) utente, mezzo, LocalDateTime.now(), DURATA_PRENOTAZIONE_MINUTI);
//            prenotazioneRepository.save(nuovaPrenotazione);
//        } else {
//            throw new ErroreValidazioneException("L'account loggato non è un utente valido per la prenotazione");
//        }
//
//        avviaTimerPrenotazione(nuovaPrenotazione);
//
//        return convertiInDTO(nuovaPrenotazione);
//    }
//
//    private boolean validaDisponibilita(Mezzo mezzo) {
//        return Boolean.TRUE.equals(mezzo.getStato());
//    }
//
//    private boolean validaRequisitiUtente(Account utente) {
//        return utente != null && utente.getId() != null;
//    }
//
//    private void avviaTimerPrenotazione(Prenotazione prenotazione) {
//        log.info("Timer di {} minuti avviato per la prenotazione ID: {}", DURATA_PRENOTAZIONE_MINUTI, prenotazione.getId());
//    }
//
//    public PrenotazioneDTO convertiInDTO(Prenotazione prenotazione) {
//        if (prenotazione == null) return null;
//
//        PrenotazioneDTO dto = new PrenotazioneDTO();
//        dto.setId(prenotazione.getId());
//        dto.setOraInizio(prenotazione.getOraInizio());
//        dto.setOraFine(prenotazione.getOraFine());
//        dto.setCodiceSblocco(prenotazione.getCodiceSblocco());
//        dto.setStato(prenotazione.getStato());
//
//        if (prenotazione.getUtente() != null) {
//            dto.setIdUtente(prenotazione.getUtente().getId());
//        }
//        if (prenotazione.getMezzo() != null) {
//            dto.setIdMezzo(prenotazione.getMezzo().getId());
//        }
//        return dto;
//    }
//
//    public List<PrenotazioneDTO> ottieniPrenotazioniAttiveDTO(Long idUtente) {
//        return prenotazioneRepository.findByUtenteIdAndStatoTrue(idUtente)
//                .stream()
//                .map(this::convertiInDTO)
//                .collect(Collectors.toList());
//    }
//}

package com.zootropolis.controller;

import com.zootropolis.dto.PrenotazioneDTO;
import com.zootropolis.entity.Account;
import com.zootropolis.entity.Mezzo;
import com.zootropolis.entity.Prenotazione;
import com.zootropolis.entity.Utente;
import com.zootropolis.exception.EccezioneMezzoNonDisponibile;
import com.zootropolis.exception.ErroreValidazioneException;
import com.zootropolis.repository.MezzoRepository;
import com.zootropolis.repository.PrenotazioneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GestionePrenotazioni {

    private static final Logger log = LoggerFactory.getLogger(GestionePrenotazioni.class);
    private static final int DURATA_PRENOTAZIONE_MINUTI = 15;

    private final MezzoRepository mezzoRepository;
    private final PrenotazioneRepository prenotazioneRepository;

    public GestionePrenotazioni(MezzoRepository mezzoRepository, PrenotazioneRepository prenotazioneRepository) {
        this.mezzoRepository = mezzoRepository;
        this.prenotazioneRepository = prenotazioneRepository;
    }

    // 2. elaboraPrenotazione(idMezzo, idUtente)
    @Transactional
    public PrenotazioneDTO elaboraPrenotazione(Long idMezzo, Account utente) {
        log.info("Elaborazione prenotazione per mezzo ID: {} da utente ID: {}", idMezzo, utente.getId());

        // Recupero veicolo
        Mezzo mezzo = mezzoRepository.findById(idMezzo)
                .orElseThrow(() -> new EccezioneMezzoNonDisponibile("Mezzo non trovato"));

        // Message: getStato() + Self-Message: validaDisponibilita(statoMezzo)
        Boolean statoMezzo = mezzo.getStato();
        if (!validaDisponibilita(statoMezzo)) {
            // Sequenza 2.a: erroreVeicoloOccupato
            throw new EccezioneMezzoNonDisponibile("Il veicolo non può essere prenotato");
        }

        // Self-Message: validaRequisitiUtente(idUtente)
        if (!validaRequisitiUtente(utente)) {
            // Sequenza 2.b: erroreRequisitiMancanti
            throw new ErroreValidazioneException("L'utente non rispetta i requisiti per prenotare il mezzo");
        }

        // If già attiva, ritorna la prenotazione in corso senza duplicarla
        Optional<Prenotazione> prenotazioneGiaAttiva = prenotazioneRepository.findByUtenteIdAndStatoTrue(utente.getId())
                .stream()
                .filter(p -> p.getMezzo() != null && p.getMezzo().getId().equals(idMezzo))
                .findFirst();

        if (prenotazioneGiaAttiva.isPresent()) {
            return convertiInDTO(prenotazioneGiaAttiva.get());
        }

        // Message: setStatoMezzo("PRENOTATO") -> setStato(false)
        mezzo.setStato(false);
        mezzoRepository.save(mezzo);

        // Message: create(idUtente, idMezzo, oraAttuale) -> nuovaPrenotazione
        Prenotazione nuovaPrenotazione;
        if (utente instanceof Utente) {
            nuovaPrenotazione = new Prenotazione((Utente) utente, mezzo, LocalDateTime.now(), DURATA_PRENOTAZIONE_MINUTI);
            prenotazioneRepository.save(nuovaPrenotazione);
        } else {
            throw new ErroreValidazioneException("L'account loggato non è un utente valido per la prenotazione");
        }

        // Self-Message: avviaTimerPrenotazione()
        avviaTimerPrenotazione(nuovaPrenotazione);

        // Return: confermaConTempo(tempoRimanente) tramite DTO
        return convertiInDTO(nuovaPrenotazione);
    }

    // Self-Message: validaDisponibilita(statoMezzo)
    private boolean validaDisponibilita(Boolean statoMezzo) {
        return Boolean.TRUE.equals(statoMezzo);
    }

    // Self-Message: validaRequisitiUtente(idUtente)
    private boolean validaRequisitiUtente(Account utente) {
        return utente != null && utente.getId() != null;
    }

    // Self-Message: avviaTimerPrenotazione()
    private void avviaTimerPrenotazione(Prenotazione prenotazione) {
        log.info("Timer di {} minuti avviato per la prenotazione ID: {}", DURATA_PRENOTAZIONE_MINUTI, prenotazione.getId());
    }

    public PrenotazioneDTO convertiInDTO(Prenotazione prenotazione) {
        if (prenotazione == null) return null;

        PrenotazioneDTO dto = new PrenotazioneDTO();
        dto.setId(prenotazione.getId());
        dto.setOraInizio(prenotazione.getOraInizio());
        dto.setOraFine(prenotazione.getOraFine());
        dto.setCodiceSblocco(prenotazione.getCodiceSblocco());
        dto.setStato(prenotazione.getStato());

        if (prenotazione.getUtente() != null) {
            dto.setIdUtente(prenotazione.getUtente().getId());
        }
        if (prenotazione.getMezzo() != null) {
            dto.setIdMezzo(prenotazione.getMezzo().getId());
        }
        return dto;
    }

    public List<PrenotazioneDTO> ottieniPrenotazioniAttiveDTO(Long idUtente) {
        return prenotazioneRepository.findByUtenteIdAndStatoTrue(idUtente)
                .stream()
                .map(this::convertiInDTO)
                .collect(Collectors.toList());
    }
}