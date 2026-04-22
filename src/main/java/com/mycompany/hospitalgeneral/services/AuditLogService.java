package com.mycompany.hospitalgeneral.services;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;

/**
 *
 * @author jhonatan
 */
@Stateless
public class AuditLogService {
    
    @PersistenceContext
    private EntityManager em;

    public int countTodayLogs() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        return em.createQuery("SELECT COUNT(a) FROM AuditLog a WHERE a.timestamp >= :start", Long.class)
                .setParameter("start", startOfDay)
                .getSingleResult().intValue();
    }

    public int countTodayErrors() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        return em.createQuery("SELECT COUNT(a) FROM AuditLog a WHERE a.timestamp >= :start AND a.level = 'ERROR'", Long.class)
                .setParameter("start", startOfDay)
                .getSingleResult().intValue();
    }
}
