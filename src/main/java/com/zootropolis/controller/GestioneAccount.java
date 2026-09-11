package com.zootropolis.controller;

import com.zootropolis.dto.RegistrazioneDTO;
import com.zootropolis.entity.Account;
import com.zootropolis.entity.Utente;
import com.zootropolis.exception.ErroreValidazioneException;
import com.zootropolis.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public boolean elaboraRegistrazione(RegistrazioneDTO dto) {
        log.info("Elaborazione registrazione per email: {}", dto != null ? dto.getEmail() : "null");

        // Self-Message: validaDati(dto)
        validaDati(dto);

        Utente nuovoAccount = new Utente();
        nuovoAccount.setNome(dto.getNome());
        nuovoAccount.setCognome(dto.getCognome());
        nuovoAccount.setEmail(dto.getEmail());
        nuovoAccount.setPassword(dto.getPassword());
        nuovoAccount.setDataNascita(dto.getDataNascita());
        nuovoAccount.setStatoAccount(true);

        accountRepository.save(nuovoAccount);
        log.info("Nuovo utente registrato con successo: {}", dto.getEmail());
        return true;
    }

    // Self-Message: validaDati(dto)
    private void validaDati(RegistrazioneDTO dto) {
        if (dto == null) {
            throw new ErroreValidazioneException("Dati di registrazione assenti.");
        }
        if (dto.getEmail() == null || dto.getEmail().isBlank() ||
                dto.getPassword() == null || dto.getPassword().isBlank()) {
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
    @Transactional
    public Account elaboraLogin(String email, String password) {
        log.info("Esecuzione elaboraLogin per email: {}", email);

        Optional<Account> accountOpt = accountRepository.findByEmail(email);

        if (accountOpt.isEmpty()) {
            log.warn("Login fallito: email {} non trovata", email);
            // Return: erroreValidazione
            throw new ErroreValidazioneException("Credenziali fornite non corrette");
        }

        Account account = accountOpt.get();

        // Verifico prima se l'account è disabilitato/sospeso (UC-14)
        if (Boolean.FALSE.equals(account.getStatoAccount())) {
            log.warn("Login fallito: account {} risulta sospeso", email);
            throw new ErroreValidazioneException("Account sospeso o disattivato. Contattare l'assistenza.");
        }

        // Message: getDatiAccesso() -> Return: credenzialiRegistrate
        String credenzialiRegistrate = account.getDatiAccesso();

        // Self-Message: validaCredenziali(email, password, credenzialiRegistrate)
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

    // Self-Message: validaCredenziali(...)
    private boolean validaCredenziali(String email, String passwordInserita, String credenzialiRegistrate) {
        return passwordInserita != null && passwordInserita.equals(credenzialiRegistrate);
    }

    // ==========================================
    // UC-14 BLOCCARE ACCOUNT
    // ==========================================

    // Message: elaboraSospensione(idAccount)
    @Transactional
    public boolean elaboraSospensione(Long idAccount) {
        log.info("Elaborazione sospensione account per ID: {}", idAccount);

        // Message: getDatiAccount() -> Return: statoAccount
        Account account = accountRepository.findById(idAccount)
                .orElseThrow(() -> new IllegalArgumentException("Account non trovato")); // Sequenza 4.a: erroreAccountInesistente

        // Self-Message: verificaValidita(statoAccount)
        if (Boolean.FALSE.equals(account.getStatoAccount())) {
            // Sequenza 4.b: erroreAccountGiaSospeso
            throw new IllegalStateException("Account già sospeso o disattivato");
        }

        // Message: setStatoAccount(false) / disabilita account
        account.setStatoAutenticazione(false);
        accountRepository.save(account);

        // Return: sospensioneCompletata
        log.info("Account ID {} sospeso con successo", idAccount);
        return true;
    }
}