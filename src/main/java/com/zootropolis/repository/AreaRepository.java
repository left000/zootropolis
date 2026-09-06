package com.zootropolis.repository;

import com.zootropolis.entity.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AreaRepository extends JpaRepository<Area, Long> {
    List<Area> findByTipo(String tipo);
    List<Area> findByStato(String stato);
}