package com.zootropolis.view;

import com.zootropolis.controller.GestioneAccount;
import com.zootropolis.controller.GestioneMezzi;
import com.zootropolis.controller.GestionePrenotazioni;
import com.zootropolis.dto.RegistrazioneDTO;
import com.zootropolis.entity.Account;
import com.zootropolis.entity.Mezzo;
import com.zootropolis.exception.EccezioneMezzoNonDisponibile;
import com.zootropolis.exception.EccezionePosizioneMancante;
import com.zootropolis.exception.ErroreValidazioneException;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class VistaUtente {

    private final GestioneAccount gestioneAccount;
    private final GestioneMezzi gestioneMezzi;
    private final GestionePrenotazioni gestionePrenotazioni;

    public VistaUtente(GestioneAccount gestioneAccount, GestioneMezzi gestioneMezzi, GestionePrenotazioni gestionePrenotazioni) {
        this.gestioneAccount = gestioneAccount;
        this.gestioneMezzi = gestioneMezzi;
        this.gestionePrenotazioni = gestionePrenotazioni;
    }

    // ==========================================
    // UC-01 REGISTRAZIONE UTENTE
    // ==========================================

    @GetMapping("/utente/registrazione")
    public String avviaRegistrazione(Model model) {
        model.addAttribute("registrazioneDTO", new RegistrazioneDTO());
        return "registrazione";
    }

    @PostMapping("/utente/registrazione")
    public String fornisciDatiRegistrazione(@ModelAttribute("registrazioneDTO") RegistrazioneDTO dto,
                                            Model model,
                                            RedirectAttributes redirectAttributes) {
        try {
            gestioneAccount.elaboraRegistrazione(dto);
            return confermaCreazioneAccount(redirectAttributes);
        } catch (ErroreValidazioneException e) {
            model.addAttribute("errore", e.getMessage());
            return "registrazione";
        }
    }

    private String confermaCreazioneAccount(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("messaggio", "Account creato con successo! Ora puoi accedere.");
        return "redirect:/login";
    }

    @GetMapping("/utente/registrazione/annulla")
    public String annullaRegistrazione(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("messaggio", "Operazione annullata.");
        return "redirect:/login";
    }

    // ==========================================
    // AREA RISERVATA: DASHBOARD UTENTE
    // ==========================================

    @GetMapping("/utente/dashboard")
    public String mostraDashboard(HttpSession session, Model model) {
        Account account = (Account) session.getAttribute("accountLoggato");

        if (account == null) {
            return "redirect:/login";
        }

        model.addAttribute("account", account);
        return "dashboard";
    }

    // ==========================================
    // UC-03 CERCARE MEZZI
    // ==========================================

    // 1. avviaRicercaMezzi()
    @GetMapping("/utente/mezzi/cerca")
    public String avviaRicercaMezzi(HttpSession session, Model model) {
        Account account = (Account) session.getAttribute("accountLoggato");
        if (account == null) return "redirect:/login";

        try {
            List<Mezzo> listaMezzi = gestioneMezzi.acquisisciPosizioneERicerca();

            if (listaMezzi.isEmpty()) {
                model.addAttribute("richiediEspansione", true);
                model.addAttribute("messaggioInfo", "Nessun veicolo disponibile nelle vicinanze.");
                popolaDatiRicerca(model);
                return "cerca_mezzi";
            }

            return mostraMezziMappa(listaMezzi, model);

        } catch (EccezionePosizioneMancante e) {
            model.addAttribute("richiediIndirizzo", true);
            model.addAttribute("errorePosizione", "Impossibile acquisire la posizione automatica.");
            return "cerca_mezzi";
        }
    }

    @PostMapping("/utente/mezzi/indirizzo")
    public String fornisciIndirizzoManuale(@RequestParam String indirizzo, Model model) {
        List<Mezzo> listaMezzi = gestioneMezzi.aggiornaPosizione(indirizzo);

        if (listaMezzi.isEmpty()) {
            model.addAttribute("richiediEspansione", true);
            model.addAttribute("messaggioInfo", "Nessun veicolo disponibile nelle vicinanze.");
            popolaDatiRicerca(model);
            return "cerca_mezzi";
        }

        return mostraMezziMappa(listaMezzi, model);
    }

    @PostMapping("/utente/mezzi/espandi")
    public String espandiRaggio(Model model) {
        List<Mezzo> listaMezzi = gestioneMezzi.incrementaRaggioRicerca();

        if (listaMezzi.isEmpty()) {
            model.addAttribute("richiediEspansione", true);
            model.addAttribute("messaggioInfo", "Ancora nessun veicolo trovato con il nuovo raggio.");
            popolaDatiRicerca(model);
            return "cerca_mezzi";
        }

        return mostraMezziMappa(listaMezzi, model);
    }

    // Alt: annullaOperazione() -> operazioneAnnullata()
    @GetMapping("/utente/mezzi/annulla")
    public String annullaOperazioneMezzi(RedirectAttributes redirectAttributes) {
        gestioneMezzi.resetRicerca();
        redirectAttributes.addFlashAttribute("messaggio", "Ricerca mezzi annullata.");
        return "redirect:/utente/dashboard";
    }

    private String mostraMezziMappa(List<Mezzo> listaMezziVicini, Model model) {
        model.addAttribute("listaMezziVicini", listaMezziVicini);
        popolaDatiRicerca(model);
        return "cerca_mezzi";
    }

    // Helper per inviare Raggio e Posizione alla vista
    private void popolaDatiRicerca(Model model) {
        model.addAttribute("raggioRicerca", gestioneMezzi.getRaggioRicerca());
        model.addAttribute("posizioneUtente", gestioneMezzi.getPosizioneAttualeUtente());
    }

    @PostMapping("/utente/mezzi/reset-raggio")
    public String resetRaggio(Model model) {
        List<Mezzo> listaMezzi = gestioneMezzi.resetRaggioRicerca();

        if (listaMezzi.isEmpty()) {
            model.addAttribute("richiediEspansione", true);
            model.addAttribute("messaggioInfo", "Nessun veicolo disponibile con il raggio di default.");
            popolaDatiRicerca(model);
            return "cerca_mezzi";
        }

        return mostraMezziMappa(listaMezzi, model);
    }

    // ==========================================
    // UC-04 VISUALIZZARE DETTAGLI MEZZO
    // ==========================================

    // 1. selezionaVeicolo(idMezzo)
    @GetMapping("/utente/mezzi/{id}")
    public String selezionaVeicolo(@PathVariable("id") Long idMezzo, HttpSession session, Model model) {
        Account account = (Account) session.getAttribute("accountLoggato");
        if (account == null) return "redirect:/login";

        try {
            // 2. richiediDettagli(idMezzo)
            Mezzo mezzo = gestioneMezzi.richiediDettagli(idMezzo);

            // 3. mostraDettagli(datiMezzo)
            model.addAttribute("mezzo", mezzo);
            return "dettagli_mezzo";

        } catch (EccezioneMezzoNonDisponibile e) {
            // 2.a.2 mostraErrore("Veicolo non più disponibile")
            // chiediNuovaSelezioneOAnnulla()
            model.addAttribute("erroreNonDisponibile", e.getMessage());
            model.addAttribute("idMezzoErrato", idMezzo);
            return "dettagli_mezzo";
        }
    }

    // Alt: annullaOperazione() -> operazioneAnnullata()
    @GetMapping("/utente/mezzi/dettagli/annulla")
    public String annullaVisualizzazioneDettagli(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("messaggio", "Operazione annullata. Ritorno alla ricerca.");
        return "redirect:/utente/mezzi/cerca";
    }

    // ==========================================
    // UC-04 PRENOTA MEZZO
    // ==========================================

    // 1. richiedePrenotazione(idMezzo)
    @GetMapping("/utente/mezzi/prenota/{id}")
    public String richiedePrenotazione(@PathVariable("id") Long idMezzo, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Account utente = (Account) session.getAttribute("accountLoggato");
        if (utente == null) return "redirect:/login";

        try {
            // 2. elaboraPrenotazione(idMezzo, idUtente) -> 5. confermaPrenotazione(tempoRimanente)
            int tempoRimanente = gestionePrenotazioni.elaboraPrenotazione(idMezzo, utente);

            model.addAttribute("tempoRimanente", tempoRimanente);
            model.addAttribute("idMezzo", idMezzo);
            return "conferma_prenotazione";

        } catch (EccezioneMezzoNonDisponibile e) {
            // Sequenza 2.a: informa("Il veicolo non può essere prenotato")
            redirectAttributes.addFlashAttribute("errore", "Il veicolo non può essere prenotato: " + e.getMessage());
            return "redirect:/utente/mezzi/cerca";

        } catch (ErroreValidazioneException e) {
            // Sequenza 2.b: operazioneAnnullata()
            redirectAttributes.addFlashAttribute("errore", "Operazione Annullata: " + e.getMessage());
            return "redirect:/utente/dashboard";
        }
    }
}