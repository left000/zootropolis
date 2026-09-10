package com.zootropolis.view;

import com.zootropolis.controller.GestioneCorse;
import com.zootropolis.controller.GestioneMezzi;
import com.zootropolis.controller.GestionePrenotazioni;
import com.zootropolis.dto.*;
import com.zootropolis.entity.Account;
import com.zootropolis.exception.EccezioneMezzoNonDisponibile;
import com.zootropolis.exception.ErroreValidazioneException;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class VistaUtente {

    private final GestioneMezzi gestioneMezzi;
    private final GestionePrenotazioni gestionePrenotazioni;
    private final GestioneCorse gestioneCorse;

    public VistaUtente(GestioneMezzi gestioneMezzi, GestionePrenotazioni gestionePrenotazioni, GestioneCorse gestioneCorse) {
        this.gestioneMezzi = gestioneMezzi;
        this.gestionePrenotazioni = gestionePrenotazioni;
        this.gestioneCorse = gestioneCorse;
    }

    // ==========================================
    // DASHBOARD UTENTE
    // ==========================================

    @GetMapping("/utente/dashboard")
    public String mostraDashboard(HttpSession session, Model model) {
        Object account = session.getAttribute("accountLoggato");
        if (account == null) {
            return "redirect:/login";
        }

        // TODO: METODO DENTRO IL DOCUMENTO
        model.addAttribute("account", account);
        model.addAttribute("utente", account);

        Long idUtente = null;
        if (account instanceof AccountDTO) {
            idUtente = ((AccountDTO) account).getId();
        } else if (account instanceof Account) {
            idUtente = ((Account) account).getId();
        }

        if (idUtente != null) {
            List<PrenotazioneDTO> prenotazioniAttive = gestionePrenotazioni.ottieniPrenotazioniAttiveDTO(idUtente);
            List<CorsaDTO> corseInCorso = gestioneCorse.ottieniCorseInCorsoDTO(idUtente);

            model.addAttribute("prenotazioniAttive", prenotazioniAttive);
            model.addAttribute("corseInCorso", corseInCorso);
        }

        return "dashboard";
    }

    // ==========================================
    // UC-03 CERCARE MEZZI
    // ==========================================

    @GetMapping("/utente/mezzi/cerca")
    public String cercaMezzi(Model model, HttpSession session) {
        Account account = (Account) session.getAttribute("accountLoggato");
        if (account == null) return "redirect:/login";

        List<MezzoDTO> listaMezzi = gestioneMezzi.acquisisciPosizioneERicercaDTO();
        model.addAttribute("listaMezziVicini", listaMezzi);
        model.addAttribute("raggioRicerca", gestioneMezzi.getRaggioRicerca());
        model.addAttribute("posizioneUtente", gestioneMezzi.getPosizioneAttualeUtente());
        return "cerca_mezzi";
    }

    // TODO: METODO DENTRO IL DOCUMENTO
    // Espandi raggio (+1.5 km)
    @GetMapping("/utente/mezzi/espandi-raggio")
    public String espandiRaggio() {
        gestioneMezzi.incrementaRaggioRicerca();
        return "redirect:/utente/mezzi/cerca";
    }

    // TODO: METODO DENTRO IL DOCUMENTO
    // Diminuisci raggio (-1.5 km)
    @GetMapping("/utente/mezzi/diminuisci-raggio")
    public String diminuisciRaggio() {
        gestioneMezzi.decrementaRaggioRicerca();
        return "redirect:/utente/mezzi/cerca";
    }

    // TODO: METODO DENTRO IL DOCUMENTO
    // Reset raggio (1.0 km)
    @GetMapping("/utente/mezzi/reset-raggio")
    public String resetRaggio() {
        gestioneMezzi.resetRaggioRicerca();
        return "redirect:/utente/mezzi/cerca";
    }

    // ==========================================
    // UC-04 VISUALIZZARE DETTAGLI MEZZO
    // ==========================================

    @GetMapping("/utente/mezzi/{id}")
    public String selezionaVeicolo(@PathVariable("id") Long idMezzo, HttpSession session, Model model) {
        Account account = (Account) session.getAttribute("accountLoggato");
        if (account == null) return "redirect:/login";

        try {
            MezzoDTO mezzo = gestioneMezzi.richiediDettagliDTO(idMezzo);
            model.addAttribute("mezzo", mezzo);
            return "dettagli_mezzo";
        } catch (EccezioneMezzoNonDisponibile e) {
            model.addAttribute("erroreNonDisponibile", e.getMessage());
            model.addAttribute("idMezzoErrato", idMezzo);
            return "dettagli_mezzo";
        }
    }

    @GetMapping("/utente/mezzi/dettagli/annulla")
    public String annullaVisualizzazioneDettagli(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("messaggio", "Operazione annullata. Ritorno alla ricerca.");
        return "redirect:/utente/mezzi/cerca";
    }

    // ==========================================
    // UC-05 PRENOTARE MEZZO
    // ==========================================

    @GetMapping("/utente/mezzi/prenota/{id}")
    public String richiedePrenotazione(@PathVariable("id") Long idMezzo, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Account utente = (Account) session.getAttribute("accountLoggato");
        if (utente == null) return "redirect:/login";

        try {
            PrenotazioneDTO prenotazioneDTO = gestionePrenotazioni.elaboraPrenotazione(idMezzo, utente);
            model.addAttribute("prenotazione", prenotazioneDTO);
            model.addAttribute("tempoRimanente", 15);
            return "conferma_prenotazione";
        } catch (EccezioneMezzoNonDisponibile e) {
            redirectAttributes.addFlashAttribute("errore", "Il veicolo non può essere prenotato: " + e.getMessage());
            return "redirect:/utente/mezzi/cerca";
        } catch (ErroreValidazioneException e) {
            redirectAttributes.addFlashAttribute("errore", "Operazione Annullata: " + e.getMessage());
            return "redirect:/utente/dashboard";
        }
    }

    // ==========================================
    // UC-06 SBLOCCARE MEZZO
    // ==========================================

    @GetMapping("/utente/mezzi/sblocca/{id}")
    public String richiedeSbloccoMezzo(@PathVariable("id") Long idMezzo, HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        Account utente = (Account) session.getAttribute("accountLoggato");
        if (utente == null) return "redirect:/login";

        try {
            CorsaDTO corsaDTO = gestioneCorse.elaboraSblocco(idMezzo, utente);
            model.addAttribute("corsa", corsaDTO);
            return "corsa_in_corso";
        } catch (EccezioneMezzoNonDisponibile e) {
            redirectAttributes.addFlashAttribute("errore", e.getMessage());
            return "redirect:/utente/mezzi/cerca";
        } catch (ErroreValidazioneException e) {
            redirectAttributes.addFlashAttribute("errore", e.getMessage());
            return "redirect:/utente/dashboard";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errore", e.getMessage());
            return "redirect:/utente/mezzi/cerca";
        }
    }

    // ==========================================
    // UC-07 CALCOLARE PERCORSO
    // ==========================================

    // 1. avviaCalcoloPercorso() -> 2. richiediDestinazione()
    @GetMapping("/utente/percorso/avvia")
    public String avviaCalcoloPercorso(@RequestParam(value = "idMezzo", required = false) Long idMezzo, Model model, HttpSession session) {
        if (session.getAttribute("accountLoggato") == null) return "redirect:/login";
        model.addAttribute("idMezzo", idMezzo);
        return "calcola_percorso";
    }

    // 3. fornisciDestinazione(destinazione)
    @PostMapping("/utente/percorso/calcola")
    public String calcolaPercorso(
            @RequestParam(value = "idMezzo", required = false) Long idMezzo,
            @RequestParam("destinazione") String destinazione,
            @RequestParam(value = "partenzaManuale", required = false) String partenzaManuale,
            Model model,
            HttpSession session) {

        if (session.getAttribute("accountLoggato") == null) return "redirect:/login";

        model.addAttribute("idMezzo", idMezzo);
        model.addAttribute("destinazioneInserita", destinazione);

        try {
            // elaboraRichiestaPercorso(destinazione) -> 6. mostraPercorso(datiPercorso)
            PercorsoDTO percorso = gestioneCorse.elaboraRichiestaPercorso(idMezzo, destinazione, partenzaManuale);
            model.addAttribute("percorso", percorso);
            return "calcola_percorso";

        } catch (IllegalArgumentException e) {
            // 3.a.2 informa("Destinazione non valida o non trovata")
            // 3.a.3 richiediNuovaDestinazioneOAnnulla()
            model.addAttribute("erroreDestinazione", e.getMessage());
            return "calcola_percorso";

        } catch (IllegalStateException e) {
            // 4.a.2 informa("Impossibile acquisire posizione attuale")
            // 4.a.3 richiediPartenzaManualeOAnnulla()
            model.addAttribute("errorePosizioneAssente", e.getMessage());
            model.addAttribute("richiediPartenzaManuale", true);
            return "calcola_percorso";

        } catch (RuntimeException e) {
            // 5.a.2 informa("Impossibile elaborare il percorso verso la destinazione")
            // 5.a.3 richiediNuovaDestinazioneOAnnulla()
            model.addAttribute("errorePercorsoImpossibile", e.getMessage());
            return "calcola_percorso";
        }
    }

    // annullaOperazione() -> operazioneAnnullata()
    @GetMapping("/utente/percorso/annulla")
    public String annullaCalcoloPercorso(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("messaggio", "Operazione annullata.");
        return "redirect:/utente/dashboard";
    }

    // Visualizza i dettagli della corsa in corso selezionata dalla Dashboard
    @GetMapping("/utente/corse/dettaglio/{id}")
    public String dettaglioCorsaInCorso(@PathVariable("id") Long idCorsa, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Account utente = (Account) session.getAttribute("accountLoggato");
        if (utente == null) return "redirect:/login";

        CorsaDTO corsaDTO = gestioneCorse.ottieniCorsaDTO(idCorsa);
        if (corsaDTO == null) {
            redirectAttributes.addFlashAttribute("errore", "Corsa non trovata");
            return "redirect:/utente/dashboard";
        }

        model.addAttribute("corsa", corsaDTO);
        return "corsa_in_corso";
    }

    // Gestisce l'inserimento manuale della posizione (Flusso 2.a)
    @PostMapping("/utente/mezzi/indirizzo")
    public String aggiornaPosizioneManuale(@RequestParam("indirizzo") String indirizzo) {
        gestioneMezzi.aggiornaPosizione(indirizzo);
        return "redirect:/utente/mezzi/cerca";
    }

    @GetMapping("/utente/mezzi/annulla")
    public String annullaRicercaMezzi(RedirectAttributes redirectAttributes) {
        gestioneMezzi.resetRicerca();
        redirectAttributes.addFlashAttribute("messaggio", "Ricerca annullata con successo.");
        return "redirect:/utente/dashboard";
    }

    // ==========================================
    // UC-08 TERMINARE CORSA
    // ==========================================

    // 1. richiedeTermineCorsa(idCorsa)
    @PostMapping("/utente/corse/termina/{id}")
    public String richiedeTermineCorsa(@PathVariable("id") Long idCorsa, HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        if (session.getAttribute("accountLoggato") == null) return "redirect:/login";

        try {
            // 2. elaboraTermineCorsa(idCorsa) -> 7. confermaConclusioneNoleggio()
            gestioneCorse.elaboraTermineCorsa(idCorsa);
            redirectAttributes.addFlashAttribute("messaggio", "Corsa terminata con successo! Grazie per aver viaggiato con Zootropolis.");
            return "redirect:/utente/dashboard";

        } catch (IllegalArgumentException e) {
            // Sequenza 3.a: 3.a.2 informa("Impossibile terminare: area di sosta non consentita")
            redirectAttributes.addFlashAttribute("erroreAreaNonConsentita", e.getMessage());
            return "redirect:/utente/corse/dettaglio/" + idCorsa;

        } catch (IllegalStateException e) {
            // Sequenza 4.a: 4.a.2 informa("Anomalia tecnica: connessione con il veicolo fallita")
            // 4.a.3 operazioneAnnullata()
            redirectAttributes.addFlashAttribute("erroreConnessioneHardware", e.getMessage());
            return "redirect:/utente/corse/dettaglio/" + idCorsa;
        }
    }
}