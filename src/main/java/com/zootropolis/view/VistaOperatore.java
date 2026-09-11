package com.zootropolis.view;

import com.zootropolis.controller.GestioneMezzi;
import com.zootropolis.dto.AreaSquilibrataDTO;
import com.zootropolis.dto.MezzoDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class VistaOperatore {

    private final GestioneMezzi gestioneMezzi;

    public VistaOperatore(GestioneMezzi gestioneMezzi) {
        this.gestioneMezzi = gestioneMezzi;
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
}