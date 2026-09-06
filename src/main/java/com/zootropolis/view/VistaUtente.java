package com.zootropolis.view;

import com.zootropolis.controller.GestioneAccount;
import com.zootropolis.dto.RegistrazioneDTO;
import com.zootropolis.entity.Account;
import com.zootropolis.exception.ErroreValidazioneException;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class VistaUtente {

    private final GestioneAccount gestioneAccount;

    public VistaUtente(GestioneAccount gestioneAccount) {
        this.gestioneAccount = gestioneAccount;
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
}