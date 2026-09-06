package com.zootropolis.repository;

import com.zootropolis.entity.Notifica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificaRepository extends JpaRepository<Notifica, Long> {
    List<Notifica> findByAccountId(Long accountId);
    List<Notifica> findByAccountIdAndLetto(Long accountId, Boolean letto);
}