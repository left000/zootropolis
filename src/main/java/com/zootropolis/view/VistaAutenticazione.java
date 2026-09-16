package com.zootropolis.view;

import com.zootropolis.controller.GestioneAccount;
import com.zootropolis.dto.RegistrazioneDTO;
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

    // ==========================================
    // UC-02 LOGIN
    // ==========================================

    // 1. avviaLogin() -> 2. richiediCredenziali()
    @GetMapping("/login")
    public String avviaLogin() {
        return "login";
    }

    // 3. inserisciCredenziali(email, password)
    @PostMapping("/login")
    public String inserisciCredenziali(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            // Message: elaboraLogin(email, password) -> Return: autenticazioneCompletata
            Account account = gestioneAccount.elaboraLogin(email, password);

            // Salva l'account in sessione
            session.setAttribute("accountLoggato", account);

            // 5. confermaAccesso() -> Reindirizzamento in base al ruolo
            if (account instanceof Utente) {
                return "redirect:/utente/dashboard";
            } else if (account instanceof Operatore) {
                return "redirect:/operatore/dashboard";
            } else if (account instanceof Amministrazione) {
                return "redirect:/amministrazione/dashboard";
            }

            return "redirect:/login";

        } catch (ErroreValidazioneException e) {
            // Sequenza 4.a: erroreValidazione
            // 4.a.1 informa("Credenziali fornite non corrette") + 4.a.2 richiediNuovoInserimentoOAnnulla()
            model.addAttribute("errore", "Credenziali fornite non corrette");
            return "login";
        }
    }

    // annullaOperazione() -> operazioneAnnullata()
    @GetMapping("/login/annulla")
    public String annullaOperazioneLogin(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("messaggio", "Operazione annullata.");
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("messaggio", "Logout effettuato con successo.");
        return "redirect:/login";
    }

    // ==========================================
    // UC-01 REGISTRARSI (Sincronizzato al 100% con SD)
    // ==========================================

    // 1. avviaRegistrazione() -> 2. richiediDatiRegistrazione()
    @GetMapping("/utente/registrazione")
    public String avviaRegistrazione(Model model) {
        if (!model.containsAttribute("registrazioneDTO")) {
            model.addAttribute("registrazioneDTO", new RegistrazioneDTO());
        }
        return "registrazione";
    }

    // 3. fornisciDatiRegistrazione(datiPersonali, credenziali)
    @PostMapping("/utente/registrazione")
    public String fornisciDatiRegistrazione(
            @ModelAttribute("registrazioneDTO") RegistrazioneDTO dto,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            // Message: elaboraRegistrazione(datiPersonali, credenziali) -> Return: registrazioneCompletata
            gestioneAccount.elaboraRegistrazione(dto);

            // 5. confermaCreazioneAccount()
            redirectAttributes.addFlashAttribute("messaggio", "Registrazione completata con successo! Ora puoi accedere.");
            return "redirect:/login";

        } catch (ErroreValidazioneException | IllegalArgumentException e) {
            // Sequenza 4.a: erroreValidazione
            // 4.a.1 informa("Dati non validi o identificativo già in uso") + 4.a.2 richiediNuovoInserimentoOAnnulla()
            model.addAttribute("errore", "Dati non validi o identificativo già in uso");
            return "registrazione";
        }
    }

    // Sequenza 4.a: annullaOperazione() -> operazioneAnnullata()
    @GetMapping("/utente/registrazione/annulla")
    public String annullaOperazioneRegistrazione(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("messaggio", "Operazione di registrazione annullata.");
        return "redirect:/login";
    }
}