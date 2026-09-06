package com.zootropolis.repository;

import com.zootropolis.entity.Mezzo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MezzoRepository extends JpaRepository<Mezzo, Long> {
    List<Mezzo> findByStato(Boolean stato);
    List<Mezzo> findByTipo(String tipo);
}