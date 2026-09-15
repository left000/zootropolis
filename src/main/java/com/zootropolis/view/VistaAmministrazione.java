package com.zootropolis.view;

import com.zootropolis.controller.GestioneAmministrazione;
import com.zootropolis.dto.AccountDTO;
import com.zootropolis.dto.AreaDTO;
import com.zootropolis.dto.PromozioneDTO;
import com.zootropolis.dto.ReportDTO;
import com.zootropolis.entity.Amministrazione;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    // ==========================================
    // UC-17 RICEVERE REPORT
    // ==========================================

    // 1.a.1 impostaRicezioneAutomatica(frequenza)
    @PostMapping("/report/automatico")
    public String impostaRicezioneAutomatica(@RequestParam("frequenza") String frequenza,
                                             HttpSession session,
                                             RedirectAttributes redirectAttributes) {
        if (!isAmministrazione(session)) {
            redirectAttributes.addFlashAttribute("errore", "Accesso negato.");
            return "redirect:/login";
        }

        // configuraReportPeriodico(frequenza) -> Return: configurazioneSalvata
        gestioneAmministrazione.configuraReportPeriodico(frequenza);

        // 1.a.2 confermaImpostazione()
        redirectAttributes.addFlashAttribute("messaggio", "Configurazione report automatico salvata (" + frequenza + ").");
        return "redirect:/amministrazione/dashboard";
    }

    // 1. richiediReport(dataInizio, dataFine)
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
            // 2. generaReport(dataInizio, dataFine) -> Return: datiFormattati
            ReportDTO reportDTO = gestioneAmministrazione.generaReport(dataInizio, dataFine);

            // 3. mostraReport(datiFormattati)
            model.addAttribute("datiFormattati", reportDTO.getContenuto());
            model.addAttribute("reportDto", reportDTO);
            model.addAttribute("dataInizio", dataInizio);
            model.addAttribute("dataFine", dataFine);
            return "dashboard_amministrazione";

        } catch (IllegalStateException e) {
            // Sequenza 2.a: eccezioneNessunDato
            // 2.a.2 informa("Nessun dato nel periodo selezionato")
            model.addAttribute("erroreNessunDato", "Nessun dato nel periodo selezionato");
            model.addAttribute("dataInizio", dataInizio);
            model.addAttribute("dataFine", dataFine);
            return "dashboard_amministrazione";
        }
    }

    // ==========================================
    // UC-18 INSERIRE LAVORI URBANI
    // ==========================================

    // 1. richiedeInserimentoLavoriUrbani() -> 2. richiediDatiGeograficiETemporali()
    @GetMapping("/lavori-urbani")
    public String richiedeInserimentoLavoriUrbani(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!isAmministrazione(session)) {
            redirectAttributes.addFlashAttribute("errore", "Accesso negato.");
            return "redirect:/login";
        }

        model.addAttribute("areeRegistrate", gestioneAmministrazione.recuperaTutteLeAree());
        return "inserisci_lavori_urbani";
    }

    // 3. fornisciDatiLavori(coordinate, date)
    @PostMapping("/lavori-urbani")
    public String fornisciDatiLavori(
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
            // inserisciLavoriUrbani(coordinate, date) -> Return: aggiornamentoCompletato
            AreaDTO areaInserita = gestioneAmministrazione.inserisciLavoriUrbani(coordinate, date, nomeArea);

            // 6. confermaAvvenutoAggiornamento()
            redirectAttributes.addFlashAttribute("messaggio", "Aggiornamento completato: Area '" + areaInserita.getNome() + "' impostata su " + areaInserita.getStato() + ".");
            return "redirect:/amministrazione/lavori-urbani";

        } catch (IllegalArgumentException e) {
            // Sequenza 4.a: erroreValidazione
            // 4.a.2 informaErrore("Dati non validi") + 4.a.3 richiediModificaOAnnulla()
            model.addAttribute("erroreValidazione", "Dati non validi");
            model.addAttribute("coordinateInserite", coordinate);
            model.addAttribute("dateInserite", date);
            model.addAttribute("nomeAreaInserito", nomeArea);
            model.addAttribute("areeRegistrate", gestioneAmministrazione.recuperaTutteLeAree());
            return "inserisci_lavori_urbani";
        }
    }

    // annullaOperazione() -> operazioneAnnullata()
    @GetMapping("/lavori-urbani/annulla")
    public String annullaInserimentoLavori(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("messaggio", "Operazione annullata.");
        return "redirect:/amministrazione/dashboard";
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
// ==========================================
    // UC-19 INSERIRE INCENTIVI
    // ==========================================

    // 1. richiedeCreazioneIncentivo() -> 2. richiediParametriIncentivo()
    @GetMapping("/incentivi")
    public String richiedeCreazioneIncentivo(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!isAmministrazione(session)) {
            redirectAttributes.addFlashAttribute("errore", "Accesso negato.");
            return "redirect:/login";
        }

        model.addAttribute("listaIncentivi", gestioneAmministrazione.recuperaTuttiGliIncentivi());
        return "inserisci_incentivo";
    }

    // 3. fornisciParametri(datiIncentivo)
    @PostMapping("/incentivi")
    public String fornisciParametri(
            @RequestParam("descrizione") String descrizione,
            @RequestParam("sconto") Float sconto,
            @RequestParam("dataInizio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInizio,
            @RequestParam("dataFine") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFine,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (!isAmministrazione(session)) {
            redirectAttributes.addFlashAttribute("errore", "Accesso negato.");
            return "redirect:/login";
        }

        PromozioneDTO dto = new PromozioneDTO();
        dto.setDescrizione(descrizione);
        dto.setSconto(sconto);
        dto.setDataInizio(dataInizio);
        dto.setDataFine(dataFine);

        try {
            // creaIncentivo(datiIncentivo) -> Return: salvataggioCompletato
            PromozioneDTO incentivoCreato = gestioneAmministrazione.creaIncentivo(dto);

            // 6. confermaSalvataggioIncentivo()
            redirectAttributes.addFlashAttribute("messaggio", "Incentivo promozionale '" + incentivoCreato.getDescrizione() + "' salvato e impostato su ATTIVA.");
            return "redirect:/amministrazione/incentivi";

        } catch (IllegalArgumentException e) {
            // Sequenza 4.a: erroreValidazione
            // 4.a.2 informaErrore("Parametri non validi o incongruenti") + 4.a.3 richiediModificaOAnnulla()
            model.addAttribute("erroreValidazione", "Parametri non validi o incongruenti");
            model.addAttribute("descrizioneInserita", descrizione);
            model.addAttribute("scontoInserito", sconto);
            model.addAttribute("dataInizioInserita", dataInizio);
            model.addAttribute("dataFineInserita", dataFine);
            model.addAttribute("listaIncentivi", gestioneAmministrazione.recuperaTuttiGliIncentivi());
            return "inserisci_incentivo";
        }
    }

    // annullaOperazione() -> operazioneAnnullata()
    @GetMapping("/incentivi/annulla")
    public String annullaInserimentoIncentivo(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("messaggio", "Operazione annullata.");
        return "redirect:/amministrazione/dashboard";
    }


    // ==========================================
    // UC-20 INSERIRE AREE VIETATE
    // ==========================================

    // 1. richiedeConfigurazioneZonaVietata() -> 2. richiediDatiGeografici()
    @GetMapping("/aree-vietate")
    public String richiedeConfigurazioneZonaVietata(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        if (!isAmministrazione(session)) {
            redirectAttributes.addFlashAttribute("errore", "Accesso negato.");
            return "redirect:/login";
        }

        model.addAttribute("listaAreeVietate", gestioneAmministrazione.recuperaAreeVietate());
        return "inserisci_area_vietata";
    }

    // 3. fornisciDatiGeografici(coordinate)
    @PostMapping("/aree-vietate")
    public String fornisciDatiGeografici(
            @RequestParam("coordinate") String coordinate,
            @RequestParam(value = "nomeArea", required = false) String nomeArea,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (!isAmministrazione(session)) {
            redirectAttributes.addFlashAttribute("errore", "Accesso negato.");
            return "redirect:/login";
        }

        try {
            // creaZonaVietata(coordinate) -> Return: salvataggioCompletato
            AreaDTO areaInserita = gestioneAmministrazione.creaZonaVietata(coordinate, nomeArea);

            // 6. confermaSalvataggioZona()
            redirectAttributes.addFlashAttribute("messaggio", "Salvataggio completato: Area '" + areaInserita.getNome() + "' configurata come " + areaInserita.getStato() + ".");
            return "redirect:/amministrazione/aree-vietate";

        } catch (IllegalArgumentException e) {
            // Sequenza 4.a: erroreValidazione
            // 4.a.2 informaErrore("Dati geografici non validi") + 4.a.3 richiediModificaOAnnulla()
            model.addAttribute("erroreValidazione", "Dati geografici non validi");
            model.addAttribute("coordinateInserite", coordinate);
            model.addAttribute("nomeAreaInserito", nomeArea);
            model.addAttribute("listaAreeVietate", gestioneAmministrazione.recuperaAreeVietate());
            return "inserisci_area_vietata";
        }
    }

    // annullaOperazione() -> operazioneAnnullata()
    @GetMapping("/aree-vietate/annulla")
    public String annullaInserimentoAreaVietata(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("messaggio", "Operazione annullata.");
        return "redirect:/amministrazione/dashboard";
    }
}