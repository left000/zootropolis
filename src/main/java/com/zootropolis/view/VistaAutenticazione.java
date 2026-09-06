package com.zootropolis.view;

import com.zootropolis.controller.GestioneAccount;
import com.zootropolis.entity.Account;
import com.zootropolis.entity.Amministrazione;
import com.zootropolis.entity.Operatore;
import com.zootropolis.entity.Utente;
import com.zootropolis.exception.ErroreValidazioneException;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class VistaAutenticazione {

    private final GestioneAccount gestioneAccount;

    public VistaAutenticazione(GestioneAccount gestioneAccount) {
        this.gestioneAccount = gestioneAccount;
    }

    // 1. avviaLogin() -> 2. richiediCredenziali()
    @GetMapping("/login")
    public String mostraLogin() {
        return "login";
    }

    // 3. inserisciCredenziali(email, password)
    @PostMapping("/login")
    public String effettuaLogin(@RequestParam String email,
                                @RequestParam String password,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            // Invoca elaboraLogin (corrispondente al diagramma UML UC-02)
            Account account = gestioneAccount.elaboraLogin(email, password);

            // Salva l'account in sessione
            session.setAttribute("accountLoggato", account);

            // Reindirizzamento polimorfico in base al ruolo dell'account
            if (account instanceof Utente) {
                return "redirect:/utente/dashboard";
            } else if (account instanceof Operatore) {
                return "redirect:/operatore/dashboard";
            } else if (account instanceof Amministrazione) {
                return "redirect:/amministrazione/dashboard";
            }

            return "redirect:/login";

        } catch (ErroreValidazioneException e) {
            // 4.a.1 informa("Credenziali fornite non corrette")
            model.addAttribute("errore", e.getMessage());
            return "login";
        }
    }

    // Alt: annullaOperazione() -> operazioneAnnullata()
    @GetMapping("/login/annulla")
    public String annullaOperazione(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("messaggio", "Operazione annullata.");
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("messaggio", "Logout effettuato con successo.");
        return "redirect:/login";
    }
}