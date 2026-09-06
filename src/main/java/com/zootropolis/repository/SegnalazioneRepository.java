package com.zootropolis.repository;

import com.zootropolis.entity.Segnalazione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SegnalazioneRepository extends JpaRepository<Segnalazione, Long> {
    List<Segnalazione> findByUtenteId(Long utenteId);
    List<Segnalazione> findByStato(Boolean stato);
}