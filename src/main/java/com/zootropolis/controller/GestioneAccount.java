package com.zootropolis.controller;

import com.zootropolis.dto.RegistrazioneDTO;
import com.zootropolis.entity.Account;
import com.zootropolis.entity.Utente;
import com.zootropolis.exception.ErroreValidazioneException;
import com.zootropolis.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GestioneAccount {

    private static final Logger log = LoggerFactory.getLogger(GestioneAccount.class);
    private final AccountRepository accountRepository;

    public GestioneAccount(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // ==========================================
    // UC-01 REGISTRARSI
    // ==========================================

    public boolean elaboraRegistrazione(RegistrazioneDTO dto) {
        validaDati(dto);

        Utente nuovoAccount = new Utente();
        nuovoAccount.setNome(dto.getNome());
        nuovoAccount.setCognome(dto.getCognome());
        nuovoAccount.setEmail(dto.getEmail());
        nuovoAccount.setPassword(dto.getPassword());
        nuovoAccount.setDataNascita(dto.getDataNascita());

        accountRepository.save(nuovoAccount);
        return true;
    }

    private void validaDati(RegistrazioneDTO dto) {
        if (dto.getEmail() == null || dto.getEmail().isBlank() || dto.getPassword() == null) {
            throw new ErroreValidazioneException("Tutti i campi obbligatori devono essere compilati.");
        }
        if (accountRepository.existsByEmail(dto.getEmail())) {
            throw new ErroreValidazioneException("Dati non validi o identificativo già in uso.");
        }
    }

    // ==========================================
    // UC-02 LOGIN
    // ==========================================

    // Message: elaboraLogin(email, password)
    public Account elaboraLogin(String email, String password) {
        log.info("Esecuzione elaboraLogin per email: {}", email);

        Optional<Account> accountOpt = accountRepository.findByEmail(email);

        if (accountOpt.isEmpty()) {
            log.warn("Login fallito: email {} non trovata", email);
            // Return: erroreValidazione
            throw new ErroreValidazioneException("Credenziali fornite non corrette");
        }

        Account account = accountOpt.get();

        // Message: getDatiAccesso() -> Return: credenzialiRegistrate
        String credenzialiRegistrate = account.getDatiAccesso();

        // Self-message: validaCredenziali(email, password, credenzialiRegistrate)
        if (!validaCredenziali(email, password, credenzialiRegistrate)) {
            log.warn("Login fallito: password errata per {}", email);
            // Return: erroreValidazione
            throw new ErroreValidazioneException("Credenziali fornite non corrette");
        }

        // Message: setStatoAutenticazione(true) -> Return: statoAggiornato
        account.setStatoAutenticazione(true);
        accountRepository.save(account);

        // Return: autenticazioneCompletata
        log.info("Autenticazione completata con successo per: {}", email);
        return account;
    }

    // Self-message: validaCredenziali(...)
    private boolean validaCredenziali(String email, String passwordInserita, String credenzialiRegistrate) {
        return passwordInserita != null && passwordInserita.equals(credenzialiRegistrate);
    }
}