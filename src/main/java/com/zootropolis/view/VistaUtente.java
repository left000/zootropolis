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

    // Reindirizza la radice dell'applicazione al Login
    @GetMapping("/")
    public String index() {
        return "redirect:/login";
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

    // 1. avviaRicercaMezzi()
    @GetMapping("/utente/mezzi/cerca")
    public String avviaRicercaMezzi(Model model, HttpSession session) {
        Object account = session.getAttribute("accountLoggato");
        if (account == null) return "redirect:/login";

        try {
            // 2. acquisisciPosizioneERicerca() -> Return: listaMezziVicini
            List<MezzoDTO> listaMezziVicini = gestioneMezzi.acquisisciPosizioneERicercaDTO();

            // Sequenza 3.a: Nessun veicolo nelle vicinanze (listaVuota)
            if (listaMezziVicini.isEmpty()) {
                // 3.a.1 informa("Nessun veicolo disponibile nelle vicinanze")
                // 3.a.2 chiediEspansioneRaggioOAnnulla()
                model.addAttribute("messaggioListaVuota", "Nessun veicolo disponibile nelle vicinanze.");
                model.addAttribute("chiediEspansioneOAnnulla", true);
            }

            // 4. mostraMezziMappa(listaMezziVicini)
            model.addAttribute("listaMezziVicini", listaMezziVicini);
            model.addAttribute("listaDatiMezzi", listaMezziVicini);
            model.addAttribute("raggio", gestioneMezzi.getRaggioRicerca());
            model.addAttribute("posizioneRilevata", gestioneMezzi.getPosizioneAttualeUtente());
            return "cerca_mezzi";

        } catch (com.zootropolis.exception.EccezionePosizioneMancante e) {
            // Alt: non rilevabile (Sequenza 2.a)
            // 2.a.1 informa("Impossibile acquisire posizione") -> 2.a.2 richiediIndirizzoManuale()
            model.addAttribute("errorePosizioneMancante", "Impossibile acquisire posizione");
            model.addAttribute("richiediIndirizzoManuale", true);
            return "cerca_mezzi";
        }
    }

    // 2.a: fornisciIndirizzoManuale(indirizzo)
    @PostMapping("/utente/mezzi/indirizzo")
    public String fornisciIndirizzoManuale(@RequestParam("indirizzo") String indirizzo) {
        // -> aggiornaPosizione(indirizzo)
        gestioneMezzi.aggiornaPosizione(indirizzo);
        return "redirect:/utente/mezzi/cerca";
    }

    // Alt: espande il raggio -> espandiRaggio()
    @GetMapping("/utente/mezzi/espandi-raggio")
    public String espandiRaggio() {
        // -> incrementaRaggioRicerca()
        gestioneMezzi.incrementaRaggioRicerca();
        return "redirect:/utente/mezzi/cerca";
    }

    // Alt: annulla -> annullaOperazione()
    @GetMapping("/utente/mezzi/annulla")
    public String annullaOperazione(RedirectAttributes redirectAttributes) {
        gestioneMezzi.resetRicerca();
        // Return: operazioneAnnullata()
        redirectAttributes.addFlashAttribute("messaggio", "Operazione annullata.");
        return "redirect:/utente/dashboard";
    }

    // Reset raggio opzionale per la mappa
    @GetMapping("/utente/mezzi/reset-raggio")
    public String resetRaggio() {
        gestioneMezzi.resetRaggioRicerca();
        return "redirect:/utente/mezzi/cerca";
    }

    // Diminuisci raggio opzionale per la mappa
    @GetMapping("/utente/mezzi/diminuisci-raggio")
    public String diminuisciRaggio() {
        gestioneMezzi.decrementaRaggioRicerca();
        return "redirect:/utente/mezzi/cerca";
    }

    // ==========================================
    // UC-04 VISUALIZZARE DETTAGLI MEZZO
    // ==========================================
    // 1. selezionaVeicolo(idMezzo)
    @GetMapping("/utente/mezzi/{id}")
    public String selezionaVeicolo(@PathVariable("id") Long idMezzo, HttpSession session, Model model) {
        Object account = session.getAttribute("accountLoggato");
        if (account == null) return "redirect:/login";

        try {
            // 2. richiediDettagli(idMezzo) -> Return: datiMezzo
            MezzoDTO mezzo = gestioneMezzi.richiediDettagliDTO(idMezzo);

            // 3. mostraDettagli(datiMezzo)
            model.addAttribute("mezzo", mezzo);
            return "dettagli_mezzo";

        } catch (EccezioneMezzoNonDisponibile e) {
            // Sequenza Alternativa 2.a
            // 2.a.2 mostraErrore("Veicolo non più disponibile") + chiediNuovaSelezioneOAnnulla()
            model.addAttribute("erroreNonDisponibile", "Veicolo non più disponibile");
            model.addAttribute("idMezzoErrato", idMezzo);
            return "dettagli_mezzo";
        }
    }

    // Sequenza 2.a: sceglie di annullare -> annullaOperazione()
    @GetMapping("/utente/mezzi/dettagli/annulla")
    public String annullaVisualizzazioneDettagli(RedirectAttributes redirectAttributes) {
        // Return: operazioneAnnullata()
        redirectAttributes.addFlashAttribute("messaggio", "Operazione annullata. Ritorno alla ricerca.");
        return "redirect:/utente/mezzi/cerca";
    }
//    @GetMapping("/utente/mezzi/{id}")
//    public String selezionaVeicolo(@PathVariable("id") Long idMezzo, HttpSession session, Model model) {
//        Object account = session.getAttribute("accountLoggato");
//        if (account == null) return "redirect:/login";
//
//        try {
//            MezzoDTO mezzo = gestioneMezzi.richiediDettagliDTO(idMezzo);
//            model.addAttribute("mezzo", mezzo);
//            return "dettagli_mezzo";
//        } catch (EccezioneMezzoNonDisponibile e) {
//            model.addAttribute("erroreNonDisponibile", e.getMessage());
//            model.addAttribute("idMezzoErrato", idMezzo);
//            return "dettagli_mezzo";
//        }
//    }
//
//    @GetMapping("/utente/mezzi/dettagli/annulla")
//    public String annullaVisualizzazioneDettagli(RedirectAttributes redirectAttributes) {
//        redirectAttributes.addFlashAttribute("messaggio", "Operazione annullata. Ritorno alla ricerca.");
//        return "redirect:/utente/mezzi/cerca";
//    }

    // ==========================================
    // UC-05 PRENOTARE MEZZO
    // ==========================================

    // 1. richiedePrenotazione(idMezzo)
    @GetMapping("/utente/mezzi/prenota/{id}")
    public String richiedePrenotazione(@PathVariable("id") Long idMezzo, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Object account = session.getAttribute("accountLoggato");
        if (account == null) return "redirect:/login";

        Account utente = (Account) account;

        try {
            // 2. elaboraPrenotazione(idMezzo, idUtente) -> Return: confermaConTempo(tempoRimanente)
            PrenotazioneDTO prenotazioneDTO = gestionePrenotazioni.elaboraPrenotazione(idMezzo, utente);

            // 5. confermaPrenotazione(tempoRimanente)
            model.addAttribute("prenotazione", prenotazioneDTO);
            model.addAttribute("tempoRimanente", 15);
            return "conferma_prenotazione";

        } catch (EccezioneMezzoNonDisponibile e) {
            // Sequenza 2.a: non più disponibile
            // 2.a.2 informa("Il veicolo non può essere prenotato")
            redirectAttributes.addFlashAttribute("errore", "Il veicolo non può essere prenotato: " + e.getMessage());
            return "redirect:/utente/mezzi/cerca";

        } catch (ErroreValidazioneException e) {
            // Sequenza 2.b: non soddisfatti
            // 2.b.2 operazioneAnnullata()
            redirectAttributes.addFlashAttribute("errore", "Operazione Annullata: " + e.getMessage());
            return "redirect:/utente/dashboard";
        }
    }

//    @GetMapping("/utente/mezzi/prenota/{id}")
//    public String richiedePrenotazione(@PathVariable("id") Long idMezzo, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
//        Account utente = (Account) session.getAttribute("accountLoggato");
//        if (utente == null) return "redirect:/login";
//
//        try {
//            PrenotazioneDTO prenotazioneDTO = gestionePrenotazioni.elaboraPrenotazione(idMezzo, utente);
//            model.addAttribute("prenotazione", prenotazioneDTO);
//            model.addAttribute("tempoRimanente", 15);
//            return "conferma_prenotazione";
//        } catch (EccezioneMezzoNonDisponibile e) {
//            redirectAttributes.addFlashAttribute("errore", "Il veicolo non può essere prenotato: " + e.getMessage());
//            return "redirect:/utente/mezzi/cerca";
//        } catch (ErroreValidazioneException e) {
//            redirectAttributes.addFlashAttribute("errore", "Operazione Annullata: " + e.getMessage());
//            return "redirect:/utente/dashboard";
//        }
//    }

    // ==========================================
    // UC-06 SBLOCCARE MEZZO
    // ==========================================

    // 1. richiedeSbloccoMezzo(idMezzo)
    @GetMapping("/utente/mezzi/sblocca/{id}")
    public String richiedeSbloccoMezzo(@PathVariable("id") Long idMezzo, HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        Object account = session.getAttribute("accountLoggato");
        if (account == null) return "redirect:/login";

        Account utente = (Account) account;

        try {
            // 2. elaboraSblocco(idMezzo, idUtente) -> Return: sbloccoCompletato
            CorsaDTO corsaDTO = gestioneCorse.elaboraSblocco(idMezzo, utente);

            // 5. confermaInizioNoleggio()
            model.addAttribute("corsa", corsaDTO);
            return "corsa_in_corso";

        } catch (EccezioneMezzoNonDisponibile e) {
            // Sequenza 2.a: veicolo non noleggiabile
            // 2.a.2 informa("Impossibile sbloccare: veicolo non noleggiabile")
            redirectAttributes.addFlashAttribute("errore", "Impossibile sbloccare: veicolo non noleggiabile (" + e.getMessage() + ")");
            return "redirect:/utente/mezzi/cerca";

        } catch (ErroreValidazioneException e) {
            // Sequenza 2.b: requisiti non soddisfatti
            // 2.b.2 informa("Requisiti non soddisfatti per il noleggio") + 2.b.3 operazioneAnnullata()
            redirectAttributes.addFlashAttribute("errore", "Requisiti non soddisfatti per il noleggio: " + e.getMessage());
            return "redirect:/utente/dashboard";

        } catch (RuntimeException e) {
            // Sequenza 3.a: connessione hardware fallita
            // 3.a.2 informa("Anomalia tecnica: connessione col veicolo fallita")
            redirectAttributes.addFlashAttribute("errore", "Anomalia tecnica: connessione col veicolo fallita");
            return "redirect:/utente/mezzi/cerca";
        }
    }

//    @GetMapping("/utente/mezzi/sblocca/{id}")
//    public String richiedeSbloccoMezzo(@PathVariable("id") Long idMezzo, HttpSession session, RedirectAttributes redirectAttributes, Model model) {
//        Account utente = (Account) session.getAttribute("accountLoggato");
//        if (utente == null) return "redirect:/login";
//
//        try {
//            CorsaDTO corsaDTO = gestioneCorse.elaboraSblocco(idMezzo, utente);
//            model.addAttribute("corsa", corsaDTO);
//            return "corsa_in_corso";
//        } catch (EccezioneMezzoNonDisponibile e) {
//            redirectAttributes.addFlashAttribute("errore", e.getMessage());
//            return "redirect:/utente/mezzi/cerca";
//        } catch (ErroreValidazioneException e) {
//            redirectAttributes.addFlashAttribute("errore", e.getMessage());
//            return "redirect:/utente/dashboard";
//        } catch (RuntimeException e) {
//            redirectAttributes.addFlashAttribute("errore", e.getMessage());
//            return "redirect:/utente/mezzi/cerca";
//        }
//    }

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

    // 3. fornisciDestinazione(destinazione) / fornisciPartenzaManuale(posizionePartenza)
    @PostMapping("/utente/percorso/calcola")
    public String fornisciDestinazione(
            @RequestParam(value = "idMezzo", required = false) Long idMezzo,
            @RequestParam("destinazione") String destinazione,
            @RequestParam(value = "partenzaManuale", required = false) String partenzaManuale,
            Model model,
            HttpSession session) {

        if (session.getAttribute("accountLoggato") == null) return "redirect:/login";

        model.addAttribute("idMezzo", idMezzo);
        model.addAttribute("destinazioneInserita", destinazione);

        try {
            // 2. elaboraRichiestaPercorso(destinazione) -> Return: datiPercorso
            PercorsoDTO percorso = gestioneCorse.elaboraRichiestaPercorso(idMezzo, destinazione, partenzaManuale);

            // 6. mostraPercorso(datiPercorso)
            model.addAttribute("percorso", percorso);
            return "calcola_percorso";

        } catch (IllegalArgumentException e) {
            // Sequenza 3.a: erroreDestinazione
            // 3.a.2 informa("Destinazione non valida o non trovata") + 3.a.3 richiediNuovaDestinazioneOAnnulla()
            model.addAttribute("erroreDestinazione", "Destinazione non valida o non trovata");
            return "calcola_percorso";

        } catch (IllegalStateException e) {
            // Sequenza 4.a: errorePosizioneAssente
            // 4.a.2 informa("Impossibile acquisire posizione attuale") + 4.a.3 richiediPartenzaManualeOAnnulla()
            model.addAttribute("errorePosizioneAssente", "Impossibile acquisire posizione attuale");
            model.addAttribute("richiediPartenzaManuale", true);
            return "calcola_percorso";

        } catch (RuntimeException e) {
            // Sequenza 5.a: errorePercorsoImpossibile
            // 5.a.2 informa("Impossibile elaborare il percorso verso la destinazione") + 5.a.3 richiediNuovaDestinazioneOAnnulla()
            model.addAttribute("errorePercorsoImpossibile", "Impossibile elaborare il percorso verso la destinazione");
            return "calcola_percorso";
        }
    }

    // annullaOperazione() -> operazioneAnnullata()
    @GetMapping("/utente/percorso/annulla")
    public String annullaOperazionePercorso(RedirectAttributes redirectAttributes) {
        // Return: operazioneAnnullata()
        redirectAttributes.addFlashAttribute("messaggio", "Operazione annullata.");
        return "redirect:/utente/dashboard";
    }


    @GetMapping("/utente/corse/dettaglio/{id}")
    public String dettaglioCorsaInCorso(@PathVariable("id") Long idCorsa, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Object account = session.getAttribute("accountLoggato");
        if (account == null) return "redirect:/login";

        CorsaDTO corsaDTO = gestioneCorse.ottieniCorsaDTO(idCorsa);
        if (corsaDTO == null) {
            redirectAttributes.addFlashAttribute("errore", "Corsa non trovata");
            return "redirect:/utente/dashboard";
        }

        model.addAttribute("corsa", corsaDTO);
        return "corsa_in_corso";
    }

    // ==========================================
    // UC-08 TERMINARE CORSA
    // ==========================================

    @PostMapping("/utente/corse/termina/{id}")
    public String richiedeTermineCorsa(@PathVariable("id") Long idCorsa, HttpSession session, RedirectAttributes redirectAttributes) {
        if (session.getAttribute("accountLoggato") == null) return "redirect:/login";

        try {
            // 2. elaboraTermineCorsa(idCorsa) -> Return: corsaTerminataConSuccesso
            gestioneCorse.elaboraTermineCorsa(idCorsa);

            // 7. confermaConclusioneNoleggio() -> Avvia flusso pagamento UC-09
            return "redirect:/utente/corse/paga/" + idCorsa;

        } catch (IllegalArgumentException e) {
            // Sequenza 3.a: in area non consentita
            // 3.a.2 informa("Impossibile terminare: area di sosta non consentita")
            redirectAttributes.addFlashAttribute("erroreAreaNonConsentita", e.getMessage());
            return "redirect:/utente/corse/dettaglio/" + idCorsa;

        } catch (IllegalStateException e) {
            // Sequenza 4.a: connessione hardware fallita
            // 4.a.2 informa("Anomalia tecnica: connessione con il veicolo fallita") + 4.a.3 operazioneAnnullata()
            redirectAttributes.addFlashAttribute("erroreConnessioneHardware", e.getMessage());
            return "redirect:/utente/corse/dettaglio/" + idCorsa;
        }
    }

    // ==========================================
    // UC-09 PAGARE
    // ==========================================

    // 1. avviaPagamento(idCorsa)
    @GetMapping("/utente/corse/paga/{id}")
    public String avviaPagamento(@PathVariable("id") Long idCorsa, HttpSession session, Model model) {
        if (session.getAttribute("accountLoggato") == null) return "redirect:/login";

        // 2. richiediCalcoloImporto(idCorsa) -> Return: importoTotale
        Double importoTotale = gestioneCorse.richiediCalcoloImporto(idCorsa);

        // 3. mostraImportoTotale(importoTotale) + richiediMetodoPagamento()
        model.addAttribute("idCorsa", idCorsa);
        model.addAttribute("importoTotale", importoTotale);
        return "pagamento";
    }

    // 5. confermaMetodoPagamento(metodo)
    @PostMapping("/utente/corse/paga/conferma")
    public String confermaMetodoPagamento(
            @RequestParam("idCorsa") Long idCorsa,
            @RequestParam("importoTotale") Double importoTotale,
            @RequestParam("metodo") String metodo,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (session.getAttribute("accountLoggato") == null) return "redirect:/login";

        try {
            // Message: elaboraTransazione(importoTotale, metodo) -> Return: pagamentoCompletato
            gestioneCorse.elaboraTransazione(idCorsa, importoTotale, metodo);

            // 10. notificaSuccessoPagamento()
            redirectAttributes.addFlashAttribute("messaggio", "Pagamento completato con successo! Importo addebitato: €" + importoTotale);
            return "redirect:/utente/dashboard";

        } catch (IllegalStateException e) {
            // Sequenza 7.a: esitoNegativo
            // 7.a.2 informa("Impossibile elaborare il pagamento") + 7.a.3 chiediNuovoMetodoOAnnulla()
            model.addAttribute("idCorsa", idCorsa);
            model.addAttribute("importoTotale", importoTotale);
            model.addAttribute("erroreTransazione", "Impossibile elaborare il pagamento");
            return "pagamento";
        }
    }

    // Sequenza 4.a.1 / 7.a.3: annullaOperazione() -> operazioneInterrotta()
    @GetMapping("/utente/corse/paga/annulla")
    public String annullaOperazionePagamento(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("messaggio", "Operazione di pagamento interrotta.");
        return "redirect:/utente/dashboard";
    }

    // ==========================================
    // UC-10 VISUALIZZARE STORICO
    // ==========================================

    @GetMapping("/utente/corse/storico")
    public String richiedeStoricoViaggi(HttpSession session, Model model) {
        Object account = session.getAttribute("accountLoggato");
        if (account == null) return "redirect:/login";

        Long idUtente = null;
        if (account instanceof AccountDTO) {
            idUtente = ((AccountDTO) account).getId();
        } else if (account instanceof Account) {
            idUtente = ((Account) account).getId();
        }

        if (idUtente != null) {
            // 2. richiediCorseConcluse(idUtente) -> Return: storicoCorse / listaVuota
            List<CorsaDTO> storicoCorse = gestioneCorse.richiediCorseConcluse(idUtente);

            if (storicoCorse.isEmpty()) {
                // Sequenza 2.a: 2.a.2 informa("Storico dei viaggi vuoto")
                model.addAttribute("messaggioVuoto", "Storico dei viaggi vuoto");
            } else {
                // Sequenza Principale: 3. mostraStorico(storicoCorse)
                model.addAttribute("storicoCorse", storicoCorse);
            }
        }

        return "storico_viaggi";
    }
}