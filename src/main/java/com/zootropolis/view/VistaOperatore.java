package com.zootropolis.view;

import com.zootropolis.controller.GestioneAccount;
import com.zootropolis.controller.GestioneMezzi;
import com.zootropolis.controller.GestioneSegnalazioni;
import com.zootropolis.dto.AccountDTO;
import com.zootropolis.dto.AreaSquilibrataDTO;
import com.zootropolis.dto.DettagliAllarmeDTO;
import com.zootropolis.dto.MezzoDTO;
import com.zootropolis.entity.Operatore;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class VistaOperatore {

    private final GestioneMezzi gestioneMezzi;
    private final GestioneAccount gestioneAccount;
    private final GestioneSegnalazioni gestioneSegnalazioni;

    public VistaOperatore(GestioneMezzi gestioneMezzi, GestioneAccount gestioneAccount, GestioneSegnalazioni gestioneSegnalazioni) {
        this.gestioneMezzi = gestioneMezzi;
        this.gestioneAccount = gestioneAccount;
        this.gestioneSegnalazioni = gestioneSegnalazioni;
    }


    // Metodo Helper per verificare se l'utente in sessione è un Operatore
    private boolean isOperatore(HttpSession session) {
        Object account = session.getAttribute("accountLoggato");
        if (account == null) return false;

        // Controllo se l'entità a DB è istanza di Operatore o ha il ruolo dedicato
        if (account instanceof Operatore) return true;
        if (account instanceof AccountDTO) {
            return "OPERATORE".equalsIgnoreCase(((AccountDTO) account).getRuolo());
        }
        return false;
    }

    // DASHBOARD OPERATORE
    @GetMapping("/operatore/dashboard")
    public String dashboardOperatore(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!isOperatore(session)) {
            redirectAttributes.addFlashAttribute("errore", "Accesso negato: Area riservata esclusivamente agli operatori.");
            return "redirect:/utente/dashboard";
        }

        Object account = session.getAttribute("accountLoggato");
        model.addAttribute("operatore", account);
        return "dashboard_operatore";
    }






    // ==========================================
    // UC-11 VISUALIZZARE MEZZI
    // ==========================================

    // 1. richiedeVisualizzazioneMappa()
    @GetMapping("/operatore/mezzi/mappa")
    public String richiedeVisualizzazioneMappa(
            @RequestParam(value = "usaDatiNoti", required = false, defaultValue = "false") boolean usaDatiNoti,
            HttpSession session,
            Model model) {

        if (session.getAttribute("accountLoggato") == null) {
            return "redirect:/login";
        }

        try {
            // 2. richiediDatiMezzi() / 2.a.5 richiediUltimiDatiNoti()
            List<MezzoDTO> listaDatiMezzi = usaDatiNoti ?
                    gestioneMezzi.richiediUltimiDatiNoti() :
                    gestioneMezzi.richiediDatiMezzi(false);

            // 3. mostraMappa(listaDatiMezzi) / 2.a.6 mostraMappa()
            model.addAttribute("listaDatiMezzi", listaDatiMezzi);
            model.addAttribute("datiNotiUsati", usaDatiNoti);
            return "mappa_operatore";

        } catch (IllegalStateException e) {
            // Sequenza 2.b: 2.b.2 informa("Nessun mezzo da mostrare")
            model.addAttribute("erroreNessunVeicolo", e.getMessage());
            return "mappa_operatore";

        } catch (IllegalArgumentException e) {
            // Sequenza 2.a: 2.a.2 informa("Impossibile mostrare dati in tempo reale")
            // 2.a.3 chiedi("Mostrare ultimi dati noti?")
            model.addAttribute("erroreLocalizzazioneAssente", e.getMessage());
            model.addAttribute("chiediUltimiDatiNoti", true);
            return "mappa_operatore";
        }
    }

    // 2.a.4 rifiutaUltimiDati() -> operazioneAnnullata()
    @GetMapping("/operatore/mezzi/mappa/annulla")
    public String annullaVisualizzazioneMappa(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("messaggio", "Operazione annullata.");
        return "redirect:/utente/dashboard";
    }


    // ==========================================
    // UC-12 MONITORARE MALFUNZIONAMENTI
    // ==========================================

    // 1. richiedeMezziConAnomalie()
    @GetMapping("/operatore/anomalie")
    public String richiedeMezziConAnomalie(HttpSession session, Model model) {
        // Controllo sessione utente/operatore
        if (session.getAttribute("accountLoggato") == null) {
            return "redirect:/login";
        }

        // 2. richiediElencoAnomalie() -> Return: listaMezziAnomali / listaVuota
        List<MezzoDTO> listaAnomalie = gestioneMezzi.richiediElencoAnomalie();

        // 3. mostraElencoAnomalie() / 2.a.2 informa("Nessuna anomalia presente")
        model.addAttribute("listaMezziAnomali", listaAnomalie);

        return "monitora_anomalie";
    }

    // ==========================================
    // UC-13 REDISTRIBUIRE
    // ==========================================

    // 1. richiedeAreeConSquilibri()
    @GetMapping("/operatore/redistribuisci")
    public String richiedeAreeConSquilibri(HttpSession session, Model model) {
        if (session.getAttribute("accountLoggato") == null) {
            return "redirect:/login";
        }

        // 2. analizzaDistribuzioneMezzi() -> Return: listaAreeSquilibrate / distribuzioneOttimale
        List<AreaSquilibrataDTO> areeSquilibrate = gestioneMezzi.analizzaDistribuzioneMezzi();

        // 3. mostraMappaSquilibri() / 2.a.2 informa("Distribuzione dei mezzi ottimale")
        model.addAttribute("areeSquilibrate", areeSquilibrate);

        return "redistribuisci_mezzi";
    }

    // ==========================================
    // UC-14 BLOCCARE ACCOUNT
    // ==========================================

    // 1. richiediSospensioneAccount() -> 2. richiediIdentificativoAccount()
    @GetMapping("/operatore/account/sospendi")
    public String richiediSospensioneAccount(HttpSession session) {
        if (session.getAttribute("accountLoggato") == null) return "redirect:/login";
        return "sospendi_account";
    }

    // 3. fornisciIdentificativo(idAccount)
    @PostMapping("/operatore/account/sospendi")
    public String elaboraSospensioneAccount(
            @RequestParam("idAccount") Long idAccount,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (session.getAttribute("accountLoggato") == null) return "redirect:/login";

        try {
            // elaboraSospensione(idAccount) -> 6. confermaBloccoEseguito()
            gestioneAccount.elaboraSospensione(idAccount);
            redirectAttributes.addFlashAttribute("messaggio", "Blocco eseguito con successo per l'account #" + idAccount);
            return "redirect:/operatore/account/sospendi";

        } catch (IllegalArgumentException e) {
            // Sequenza 4.a: 4.a.2 informa("Account non trovato") -> 4.a.3 richiediNuovoIDoAnnulla()
            model.addAttribute("erroreAccountInesistente", e.getMessage());
            return "sospendi_account";

        } catch (IllegalStateException e) {
            // Sequenza 4.b: 4.b.2 informa("Account già sospeso o disattivato")
            model.addAttribute("erroreAccountGiaSospeso", e.getMessage());
            return "sospendi_account";
        }
    }

    // annullaOperazione() -> operazioneAnnullata()
    @GetMapping("/operatore/account/sospendi/annulla")
    public String annullaSospensioneAccount(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("messaggio", "Operazione annullata.");
        return "redirect:/utente/dashboard";
    }

    // ==========================================
    // UC-15 GESTIRE SPOSTAMENTI ANOMALI
    // ==========================================

    // 1. mostraAvvisoSpostamento() & 2. richiediDettagliAllarme()
    @GetMapping("/operatore/allarmi/dettaglio/{id}")
    public String richiediDettagliAllarme(@PathVariable("id") Long idMezzo, HttpSession session, Model model) {
        if (session.getAttribute("accountLoggato") == null) {
            return "redirect:/login";
        }

        model.addAttribute("idMezzo", idMezzo);
        model.addAttribute("avvisoAnomalia", gestioneSegnalazioni.generaAvvisoAnomalia(idMezzo));

        try {
            // 3. recuperaDettagliAllarme() -> 4. mostraDettagliMezzo(dettagliAllarme)
            DettagliAllarmeDTO dettagliAllarme = gestioneSegnalazioni.recuperaDettagliAllarme(idMezzo);
            model.addAttribute("dettagliAllarme", dettagliAllarme);
            return "dettagli_allarme";

        } catch (IllegalStateException e) {
            // Sequenza 3.a: 3.a.2 informa("Dati non recuperabili, mezzo non rintracciabile")
            model.addAttribute("erroreNonRintracciabile", e.getMessage());
            return "dettagli_allarme";

        } catch (IllegalArgumentException e) {
            // Sequenza 3.b: 3.b.2 informa("Allarme non più attivo: mezzo in area consentita")
            model.addAttribute("allarmeRisolto", e.getMessage());
            return "dettagli_allarme";
        }
    }

    // ==========================================
    // UC-16 BLOCCARE DA REMOTO
    // ==========================================

    // 1. richiediBloccoForzato() -> 2. richiediIdentificativoMezzo()
    @GetMapping("/operatore/mezzi/blocco-remoto")
    public String richiediBloccoForzato(HttpSession session) {
        if (session.getAttribute("accountLoggato") == null) return "redirect:/login";
        return "blocco_remoto_mezzo";
    }

    // 3. fornisciIdentificativo(idMezzo)
    @PostMapping("/operatore/mezzi/blocco-remoto")
    public String elaboraBloccoRemoto(
            @RequestParam("idMezzo") Long idMezzo,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (session.getAttribute("accountLoggato") == null) return "redirect:/login";

        try {
            // elaboraBlocco(idMezzo) -> 7. confermaBloccoEseguito()
            gestioneMezzi.elaboraBlocco(idMezzo);
            redirectAttributes.addFlashAttribute("messaggio", "Blocco da remoto eseguito con successo per il veicolo #" + idMezzo);
            return "redirect:/operatore/mezzi/blocco-remoto";

        } catch (IllegalArgumentException e) {
            // Sequenza 4.a: 4.a.2 informa("Mezzo non trovato") -> 4.a.3 chiediNuovoIDoAnnulla()
            model.addAttribute("erroreMezzoInesistente", e.getMessage());
            return "blocco_remoto_mezzo";

        } catch (IllegalStateException e) {
            // Sequenza 5.a: 5.a.2 segnalaAnomalia("Mancata connessione con il veicolo") -> 5.a.3 operazioneAnnullata()
            model.addAttribute("erroreConnessioneHardware", e.getMessage());
            return "blocco_remoto_mezzo";
        }
    }

    // annullaOperazione() -> operazioneAnnullata()
    @GetMapping("/operatore/mezzi/blocco-remoto/annulla")
    public String annullaBloccoRemoto(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("messaggio", "Operazione annullata.");
        return "redirect:/utente/dashboard";
    }
}