//package com.zootropolis.view;
//
//import com.zootropolis.controller.GestioneAccount;
//import com.zootropolis.entity.Account;
//import com.zootropolis.entity.Amministrazione;
//import com.zootropolis.entity.Operatore;
//import com.zootropolis.entity.Utente;
//import com.zootropolis.exception.ErroreValidazioneException;
//import jakarta.servlet.http.HttpSession;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//@Controller
//public class VistaAutenticazione {
//
//    private final GestioneAccount gestioneAccount;
//
//    public VistaAutenticazione(GestioneAccount gestioneAccount) {
//        this.gestioneAccount = gestioneAccount;
//    }
//
//    // 1. avviaLogin() -> 2. richiediCredenziali()
//    @GetMapping("/login")
//    public String mostraLogin() {
//        return "login";
//    }
//
//    // 3. inserisciCredenziali(email, password)
//    @PostMapping("/login")
//    public String effettuaLogin(@RequestParam String email,
//                                @RequestParam String password,
//                                HttpSession session,
//                                Model model,
//                                RedirectAttributes redirectAttributes) {
//        try {
//            // Invoca elaboraLogin (corrispondente al diagramma UML UC-02)
//            Account account = gestioneAccount.elaboraLogin(email, password);
//
//            // Salva l'account in sessione
//            session.setAttribute("accountLoggato", account);
//
//            // Reindirizzamento polimorfico in base al ruolo dell'account
//            if (account instanceof Utente) {
//                return "redirect:/utente/dashboard";
//            } else if (account instanceof Operatore) {
//                return "redirect:/operatore/dashboard";
//            } else if (account instanceof Amministrazione) {
//                return "redirect:/amministrazione/dashboard";
//            }
//
//            return "redirect:/login";
//
//        } catch (ErroreValidazioneException e) {
//            // 4.a.1 informa("Credenziali fornite non corrette")
//            model.addAttribute("errore", e.getMessage());
//            return "login";
//        }
//    }
//
//    // Alt: annullaOperazione() -> operazioneAnnullata()
//    @GetMapping("/login/annulla")
//    public String annullaOperazione(RedirectAttributes redirectAttributes) {
//        redirectAttributes.addFlashAttribute("messaggio", "Operazione annullata.");
//        return "redirect:/login";
//    }
//
//    @GetMapping("/logout")
//    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
//        session.invalidate();
//        redirectAttributes.addFlashAttribute("messaggio", "Logout effettuato con successo.");
//        return "redirect:/login";
//    }
//
//    // 1. mostraFormRegistrazione()
//    @GetMapping("/utente/registrazione")
//    public String mostraFormRegistrazione(Model model) {
//        if (!model.containsAttribute("registrazioneDTO")) {
//            model.addAttribute("registrazioneDTO", new com.zootropolis.dto.RegistrazioneDTO());
//        }
//        return "registrazione"; // Nome del file HTML della registrazione
//    }
//
//    // 2. elaboraRegistrazione()
//    @PostMapping("/utente/registrazione")
//    public String effettuaRegistrazione(@ModelAttribute("registrazioneDTO") com.zootropolis.dto.RegistrazioneDTO dto,
//                                        Model model,
//                                        RedirectAttributes redirectAttributes) {
//        try {
//            // Invoca il metodo Service
//            gestioneAccount.elaboraRegistrazione(dto);
//
//            redirectAttributes.addFlashAttribute("messaggio", "Registrazione completata con successo! Ora puoi accedere.");
//            return "redirect:/login";
//
//        } catch (com.zootropolis.exception.ErroreValidazioneException | IllegalArgumentException e) {
//            model.addAttribute("errore", e.getMessage());
//            return "registrazione";
//        }
//    }
//
//    // Alt: annullaRegistrazione()
//    @GetMapping("/utente/registrazione/annulla")
//    public String annullaRegistrazione(RedirectAttributes redirectAttributes) {
//        redirectAttributes.addFlashAttribute("messaggio", "Registrazione annullata.");
//        return "redirect:/login";
//    }
//}

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