package com.zootropolis.repository;

import com.zootropolis.entity.Promozione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromozioneRepository extends JpaRepository<Promozione, Long> {
}