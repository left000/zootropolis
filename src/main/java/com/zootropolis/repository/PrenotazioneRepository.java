package com.zootropolis.repository;

import com.zootropolis.entity.Prenotazione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PrenotazioneRepository extends JpaRepository<Prenotazione, Long> {
    List<Prenotazione> findByUtenteId(Long utenteId);
    List<Prenotazione> findByStato(Boolean stato);
    // Trova tutte le prenotazioni attive dell'utente (stato = true)
    List<Prenotazione> findByUtenteIdAndStatoTrue(Long idUtente);

}