package com.zootropolis.view;

import com.zootropolis.controller.GestioneAmministrazione;
import com.zootropolis.dto.AccountDTO;
import com.zootropolis.dto.AreaDTO;
import com.zootropolis.dto.ReportDTO;
import com.zootropolis.entity.Amministrazione;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/amministrazione")
public class VistaAmministrazione {

    private final GestioneAmministrazione gestioneAmministrazione;

    public VistaAmministrazione(GestioneAmministrazione gestioneAmministrazione) {
        this.gestioneAmministrazione = gestioneAmministrazione;
    }

    // Metodo Helper per verificare il ruolo Amministrazione
    private boolean isAmministrazione(HttpSession session) {
        Object account = session.getAttribute("accountLoggato");
        if (account == null) return false;

        // Controllo sia sull'entità JPA che su DTO/Ruolo stringa
        if (account instanceof Amministrazione) return true;
        if (account instanceof AccountDTO) {
            return "AMMINISTRAZIONE".equalsIgnoreCase(((AccountDTO) account).getRuolo());
        }
        return false;
    }

    @GetMapping("/dashboard")
    public String dashboardAmministrazione(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!isAmministrazione(session)) {
            redirectAttributes.addFlashAttribute("errore", "Accesso negato: Area riservata all'Amministrazione.");
            return "redirect:/login";
        }
        return "dashboard_amministrazione";
    }

    @PostMapping("/report/automatico")
    public String impostaRicezioneAutomatica(@RequestParam("frequenza") String frequenza,
                                             HttpSession session,
                                             RedirectAttributes redirectAttributes) {
        if (!isAmministrazione(session)) {
            redirectAttributes.addFlashAttribute("errore", "Accesso negato.");
            return "redirect:/login";
        }

        gestioneAmministrazione.configuraReportPeriodico(frequenza);
        redirectAttributes.addFlashAttribute("messaggio", "Configurazione report automatico salvata (" + frequenza + ").");
        return "redirect:/amministrazione/dashboard";
    }

    @PostMapping("/report/manuale")
    public String richiediReport(
            @RequestParam("dataInizio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInizio,
            @RequestParam("dataFine") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFine,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!isAmministrazione(session)) {
            redirectAttributes.addFlashAttribute("errore", "Accesso negato.");
            return "redirect:/login";
        }

        try {
            ReportDTO reportDTO = gestioneAmministrazione.generaReport(dataInizio, dataFine);
            model.addAttribute("datiFormattati", reportDTO.getContenuto());
            model.addAttribute("reportDto", reportDTO);
            model.addAttribute("dataInizio", dataInizio);
            model.addAttribute("dataFine", dataFine);
            return "dashboard_amministrazione";

        } catch (IllegalStateException e) {
            model.addAttribute("erroreNessunDato", e.getMessage());
            return "dashboard_amministrazione";
        }
    }

    // ==========================================
    // UC-18 INSERIRE LAVORI URBANI
    // ==========================================

    @GetMapping("/lavori-urbani")
    public String mostraFormLavoriUrbani(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!isAmministrazione(session)) {
            redirectAttributes.addFlashAttribute("errore", "Accesso negato.");
            return "redirect:/login";
        }

        // Passa la lista delle aree al modello
        model.addAttribute("areeRegistrate", gestioneAmministrazione.recuperaTutteLeAree());
        return "inserisci_lavori_urbani";
    }

    @PostMapping("/lavori-urbani")
    public String inserisciLavoriUrbani(
            @RequestParam("coordinate") String coordinate,
            @RequestParam("date") String date,
            @RequestParam(value = "nomeArea", required = false) String nomeArea,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (!isAmministrazione(session)) {
            redirectAttributes.addFlashAttribute("errore", "Accesso negato.");
            return "redirect:/login";
        }

        try {
            AreaDTO areaInserita = gestioneAmministrazione.inserisciLavoriUrbani(coordinate, date, nomeArea);
            redirectAttributes.addFlashAttribute("messaggio", "Aggiornamento completato: Area '" + areaInserita.getNome() + "' impostata su " + areaInserita.getStato() + ".");
            return "redirect:/amministrazione/lavori-urbani";

        } catch (IllegalArgumentException e) {
            model.addAttribute("erroreValidazione", e.getMessage());
            model.addAttribute("coordinateInserite", coordinate);
            model.addAttribute("dateInserite", date);
            model.addAttribute("nomeAreaInserito", nomeArea);

            // Ricarica la lista in caso di errore di validazione
            model.addAttribute("areeRegistrate", gestioneAmministrazione.recuperaTutteLeAree());
            return "inserisci_lavori_urbani";
        }
    }

//    // 1. richiedeInserimentoLavoriUrbani() -> 2. richiediDatiGeograficiETemporali()
//    @GetMapping("/lavori-urbani")
//    public String mostraFormLavoriUrbani(HttpSession session, RedirectAttributes redirectAttributes) {
//        if (!isAmministrazione(session)) {
//            redirectAttributes.addFlashAttribute("errore", "Accesso negato.");
//            return "redirect:/login";
//        }
//        return "inserisci_lavori_urbani";
//    }
//
//    // 3. fornisciDatiLavori(coordinate, date)
//    @PostMapping("/lavori-urbani")
//    public String inserisciLavoriUrbani(
//            @RequestParam("coordinate") String coordinate,
//            @RequestParam("date") String date,
//            @RequestParam(value = "nomeArea", required = false) String nomeArea,
//            HttpSession session,
//            RedirectAttributes redirectAttributes,
//            Model model) {
//
//        if (!isAmministrazione(session)) {
//            redirectAttributes.addFlashAttribute("errore", "Accesso negato.");
//            return "redirect:/login";
//        }
//
//        try {
//            // inserisciLavoriUrbani(coordinate, date) -> 6. confermaAvvenutoAggiornamento()
//            AreaDTO areaInserita = gestioneAmministrazione.inserisciLavoriUrbani(coordinate, date, nomeArea);
//            redirectAttributes.addFlashAttribute("messaggio", "Aggiornamento completato: Area '" + areaInserita.getNome() + "' impostata su " + areaInserita.getStato() + ".");
//            return "redirect:/amministrazione/lavori-urbani";
//
//        } catch (IllegalArgumentException e) {
//            // Sequenza 4.a: 4.a.2 informaErrore("Dati non validi") -> 4.a.3 richiediModificaOAnnulla()
//            model.addAttribute("erroreValidazione", e.getMessage());
//            model.addAttribute("coordinateInserite", coordinate);
//            model.addAttribute("dateInserite", date);
//            model.addAttribute("nomeAreaInserito", nomeArea);
//            return "inserisci_lavori_urbani";
//        }
//    }
//
//    // Sceglie di annullare: annullaOperazione() -> operazioneAnnullata()
//    @GetMapping("/lavori-urbani/annulla")
//    public String annullaInserimentoLavori(RedirectAttributes redirectAttributes) {
//        redirectAttributes.addFlashAttribute("messaggio", "Operazione annullata.");
//        return "redirect:/amministrazione/dashboard";
//    }


}