package com.zootropolis.repository;

import com.zootropolis.entity.Corsa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CorsaRepository extends JpaRepository<Corsa, Long> {
    List<Corsa> findByUtenteId(Long utenteId);
    List<Corsa> findByMezzoId(Long mezzoId);
    // Trova le corse in corso dell'utente (oraFine = null)
    List<Corsa> findByUtenteIdAndOraFineIsNull(Long idUtente);
}